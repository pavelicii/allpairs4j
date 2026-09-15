/*
 * Copyright 2026 Pavel Nazimok - @pavelicii
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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.BitSet;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

/** Removes redundant cases and tries bounded pair merges without losing required coverage. */
final class CaseCompactor {

    private static final int MAX_CONSTRAINT_CHECKS = 10_000;
    // Count group visits for both indexing and pair attempts; never enumerate the Cartesian product.
    private static final int MAX_GROUP_VISITS = 100_000;

    private CaseCompactor() {
    }

    static void compact(List<List<Item>> sequences, List<CompactTable> tables, CaseCompleter completer, boolean mergeCases) {
        final List<CompactTable.Group> groups = new ArrayList<>();
        for (CompactTable table : tables) {
            groups.addAll(Arrays.asList(table.getGroups()));
        }
        removeRedundantSequences(sequences, groups);
        if (mergeCases) {
            final int before = sequences.size();
            final CaseCompleter.CheckBudget budget = new CaseCompleter.CheckBudget(MAX_CONSTRAINT_CHECKS);
            mergePairs(sequences, groups, seed -> completer.complete(seed, budget));
            if (sequences.size() < before) {
                removeRedundantSequences(sequences, groups);
            }
        }
    }

    private static void removeRedundantSequences(List<List<Item>> sequences, List<CompactTable.Group> groups) {
        if (sequences.size() < 2) {
            return;
        }
        // Index rows by individual values, not by the much larger Cartesian space of tuples.
        final int valueCount = sequences.stream().flatMap(List::stream)
                .mapToInt(Item::getGlobalValueIndex).max().orElse(-1) + 1;
        final BitSet[] witnesses = new BitSet[valueCount];
        final int[] frequencies = new int[valueCount];
        for (int row = 0; row < sequences.size(); row++) {
            for (Item item : sequences.get(row)) {
                final int value = item.getGlobalValueIndex();
                if (witnesses[value] == null) {
                    witnesses[value] = new BitSet();
                }
                witnesses[value].set(row);
                frequencies[value]++;
            }
        }
        final BitSet retained = new BitSet(sequences.size());
        for (int row = 0; row < sequences.size(); row++) {
            if (hasUniqueCombination(sequences, row, groups, witnesses, frequencies)) {
                retained.set(row);
            } else {
                for (Item item : sequences.get(row)) {
                    final int value = item.getGlobalValueIndex();
                    witnesses[value].clear(row);
                    frequencies[value]--;
                }
            }
        }
        // Keep original row indices until every witness lookup is finished.
        int destination = 0;
        for (int row = retained.nextSetBit(0); row >= 0; row = retained.nextSetBit(row + 1)) {
            sequences.set(destination++, sequences.get(row));
        }
        sequences.subList(destination, sequences.size()).clear();
    }

    private static boolean hasUniqueCombination(List<List<Item>> sequences,
                                                int row,
                                                List<CompactTable.Group> groups,
                                                BitSet[] witnesses,
                                                int[] frequencies) {
        final List<Item> sequence = sequences.get(row);
        for (CompactTable.Group group : groups) {
            final int tuple = group.indexOf(sequence);
            if (!group.isRequired(tuple)) {
                continue;
            }
            int rarest = -1;
            for (int parameter : group.getParameters()) {
                final int value = sequence.get(parameter).getGlobalValueIndex();
                if (rarest < 0 || frequencies[value] < frequencies[rarest]) {
                    rarest = value;
                }
            }
            if (frequencies[rarest] == 1 || !hasOtherWitness(group, tuple, row, sequences, witnesses[rarest])) {
                return true;
            }
        }
        return false;
    }

    private static boolean hasOtherWitness(CompactTable.Group group, int tuple, int row,
                                          List<List<Item>> sequences, BitSet candidates) {
        for (int other = candidates.nextSetBit(0); other >= 0; other = candidates.nextSetBit(other + 1)) {
            if (other != row && group.indexOf(sequences.get(other)) == tuple) {
                return true;
            }
        }
        return false;
    }

    private static void mergePairs(List<List<Item>> sequences,
                                   List<CompactTable.Group> groups, Function<List<Item>, List<Item>> completeCase) {
        int remaining = MAX_GROUP_VISITS;
        boolean merged = true;
        while (merged && sequences.size() > 1) {
            final long indexVisits = (long) groups.size() * sequences.size();
            if (indexVisits > remaining) {
                return;
            }
            remaining -= (int) indexVisits;
            final List<Map<Integer, Integer>> counts = countTuples(sequences, groups);
            merged = false;
            for (int left = 0; left < sequences.size() && !merged; left++) {
                for (int right = left + 1; right < sequences.size(); right++) {
                    if (groups.size() > remaining) {
                        return;
                    }
                    remaining -= groups.size();
                    final List<Item> seed = mergeSeed(sequences.get(left), sequences.get(right), groups, counts);
                    if (seed != null) {
                        final List<Item> replacement = completeCase.apply(seed);
                        if (replacement != null) {
                            sequences.set(left, replacement);
                            sequences.remove(right);
                            merged = true;
                            break;
                        }
                    }
                }
            }
        }
    }

    private static List<Map<Integer, Integer>> countTuples(List<List<Item>> sequences, List<CompactTable.Group> groups) {
        final List<Map<Integer, Integer>> counts = new ArrayList<>();
        for (CompactTable.Group group : groups) {
            final Map<Integer, Integer> frequencies = new HashMap<>();
            for (List<Item> sequence : sequences) {
                final int index = group.indexOf(sequence);
                if (group.isRequired(index)) {
                    frequencies.merge(index, 1, Integer::sum);
                }
            }
            counts.add(frequencies);
        }
        return counts;
    }

    private static List<Item> mergeSeed(List<Item> left, List<Item> right,
                                       List<CompactTable.Group> groups, List<Map<Integer, Integer>> counts) {
        final Item[] pinned = new Item[left.size()];
        for (int i = 0; i < groups.size(); i++) {
            final CompactTable.Group group = groups.get(i);
            final int leftIndex = group.indexOf(left);
            final int rightIndex = group.indexOf(right);
            final Map<Integer, Integer> frequencies = counts.get(i);
            // A tuple shared by exactly these two rows also disappears when both rows are removed.
            final int removedLeftWitnesses = leftIndex == rightIndex ? 2 : 1;
            if (group.isRequired(leftIndex)
                    && frequencies.get(leftIndex) == removedLeftWitnesses
                    && !pin(pinned, left, group)) {
                return null;
            }
            if (leftIndex != rightIndex
                    && group.isRequired(rightIndex)
                    && frequencies.get(rightIndex) == 1
                    && !pin(pinned, right, group)) {
                return null;
            }
        }
        final List<Item> seed = new ArrayList<>();
        for (Item item : pinned) {
            if (item != null) {
                seed.add(item);
            }
        }
        return seed;
    }

    private static boolean pin(Item[] pinned, List<Item> sequence, CompactTable.Group group) {
        for (int parameter : group.getParameters()) {
            final Item item = sequence.get(parameter);
            if (pinned[parameter] != null && pinned[parameter].getValueIndex() != item.getValueIndex()) {
                return false;
            }
            pinned[parameter] = item;
        }
        return true;
    }
}
