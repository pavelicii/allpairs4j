/*
 * Copyright 2023-2026 Pavel Nazimok - @pavelicii
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.github.pavelicii.allpairs4j;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/** Tracks covered combinations and scoring statistics; full cases must be ordered by parameter index. */
class CombinationStorage {

    private final Node[] nodes;
    private final CompactTable[] tables;
    private final List<CompactTable> coverageTables;
    private final Item[][] canonicalItems;
    private final long visitsPerRow;
    private int firstUncoveredGroup;
    private int firstUncoveredTable;

    CombinationStorage(int n,
                       List<List<Item>> matrix,
                       boolean hasConstraints,
                       boolean optional,
                       boolean absenceCoverage) {
        final int[] sizes = matrix.stream().mapToInt(List::size).toArray();
        final boolean includeSmaller = optional && !absenceCoverage;
        final int[] ignoredValues = includeSmaller ? new int[sizes.length] : null;
        if (ignoredValues != null) {
            Arrays.fill(ignoredValues, -1);
        }
        final int[] capacities = CompactTable.checkedCapacities(sizes, n);
        this.nodes = new Node[capacities[1]];
        this.canonicalItems = new Item[sizes.length][];
        for (int i = 0; i < sizes.length; i++) {
            this.canonicalItems[i] = new Item[sizes[i]];
            for (Item item : matrix.get(i)) {
                this.canonicalItems[i][item.getValueIndex()] = item;
                if (ignoredValues != null && !item.isPresent()) {
                    ignoredValues[i] = item.getValueIndex();
                }
            }
        }
        this.tables = new CompactTable[n];
        long visits = 0;
        for (int i = 0; i < n; i++) {
            this.tables[i] = new CompactTable(sizes, i + 1,
                    includeSmaller || (optional || hasConstraints) && i == n - 1, ignoredValues);
            visits = Math.addExact(visits, this.tables[i].getVisitsPerItem());
        }
        this.visitsPerRow = visits;
        this.coverageTables = includeSmaller ? Arrays.asList(this.tables) : Collections.singletonList(getTargetTable());
    }

    CompactTable getTargetTable() {
        return this.tables[this.tables.length - 1];
    }

    List<CompactTable> getCoverageTables() {
        return this.coverageTables;
    }

    long getCoveredCount() {
        return this.coverageTables.stream().mapToLong(CompactTable::getCoveredCount).sum();
    }

    long getExpectedCount() {
        return this.coverageTables.stream().mapToLong(CompactTable::getRequiredCount).sum();
    }

    Node getNodeOrCreateNew(Item item) {
        final int index = item.getGlobalValueIndex();
        if (this.nodes[index] == null) {
            this.nodes[index] = new Node();
        }
        return this.nodes[index];
    }

    int countNewCombinations(List<Item> prefix, Item candidate, int strength) {
        return this.tables[strength - 1].countNewCombinations(prefix, candidate);
    }

    boolean coversNewCombination(List<Item> sequence) {
        for (CompactTable table : this.coverageTables) {
            for (CompactTable.Group group : table.getGroups()) {
                final int index = group.indexOf(sequence);
                if (group.isRequired(index) && !group.isCovered(index)) {
                    return true;
                }
            }
        }
        return false;
    }

    void addSequenceCombinations(List<Item> sequence) {
        // Every strength visits a value C(parameters - 1, strength - 1) times, even for covered tuples.
        for (Item item : sequence) {
            final boolean seen = this.tables[0].getGroups()[item.getParameterIndex()].isCovered(item.getValueIndex());
            getNodeOrCreateNew(item).increaseCounter(this.visitsPerRow - (seen ? 0 : 1));
        }
        // Pair groups follow this same lexicographic order and deduplicate every higher-strength edge.
        if (this.tables.length > 1) {
            final CompactTable.Group[] pairs = this.tables[1].getGroups();
            int rank = 0;
            for (int i = 0; i < sequence.size(); i++) {
                final Item left = sequence.get(i);
                for (int j = i + 1; j < sequence.size(); j++) {
                    final Item right = sequence.get(j);
                    final CompactTable.Group pair = pairs[rank];
                    final int index = pair.indexOf(sequence);
                    if (pair.isRequired(index) && !pair.isCovered(index)) {
                        this.nodes[left.getGlobalValueIndex()].increaseOutboundCount();
                        this.nodes[right.getGlobalValueIndex()].increaseInboundCount();
                    }
                    rank++;
                }
            }
        }
        for (CompactTable table : this.tables) {
            for (CompactTable.Group group : table.getGroups()) {
                table.cover(group, group.indexOf(sequence));
            }
        }
    }

    void rememberFeasibleCombinations(List<Item> witness) {
        for (CompactTable table : this.coverageTables) {
            for (CompactTable.Group group : table.getGroups()) {
                table.markFeasible(group, group.indexOf(witness));
            }
        }
    }

    void removeImpliedTargets() {
        for (int i = 0; i < this.coverageTables.size() - 1; i++) {
            this.coverageTables.get(i).removeImpliedTargets(this.coverageTables.get(i + 1));
        }
    }

    // Returns only this group's parameters: a completion seed, not a full case indexed by parameter.
    List<Item> decode(CompactTable.Group group, int index) {
        return group.decode(index, this.canonicalItems);
    }

    List<Item> firstUncoveredCombination() {
        while (this.firstUncoveredTable < this.coverageTables.size()) {
            final CompactTable.Group[] groups = this.coverageTables.get(this.firstUncoveredTable).getGroups();
            while (this.firstUncoveredGroup < groups.length) {
                final CompactTable.Group group = groups[this.firstUncoveredGroup];
                final int index = group.firstUncovered();
                if (index >= 0) {
                    return decode(group, index);
                }
                this.firstUncoveredGroup++;
            }
            this.firstUncoveredTable++;
            this.firstUncoveredGroup = 0;
        }
        throw new IllegalStateException("No uncovered combination found before coverage was complete");
    }

    // Prefer the target group with the most uncovered tuples; fall back to smaller targets.
    List<Item> combinationFromMostUncoveredGroup() {
        CompactTable.Group selected = null;
        for (CompactTable.Group group : getTargetTable().getGroups()) {
            if (group.getUncoveredCount() > 0
                    && (selected == null || group.getUncoveredCount() > selected.getUncoveredCount())) {
                selected = group;
            }
        }
        return selected == null ? firstUncoveredCombination() : decode(selected, selected.firstUncovered());
    }
}
