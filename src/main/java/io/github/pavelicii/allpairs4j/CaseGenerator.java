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

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;
import java.util.function.Predicate;

/** Discovers feasible coverage targets and chooses values greedily, falling back to constraint-driven completion. */
final class CaseGenerator {

    private final boolean hasConstraints;
    private final CaseCompleter completer;
    private final Set<String> optionalParameterNames;
    private final int n;
    private final CombinationStorage combinationStorage;
    private final List<List<Item>> itemMatrix;
    private final Random random;
    private List<Item> emptyCoverageCase;

    @SuppressWarnings("checkstyle:ParameterNumber")
    CaseGenerator(List<Parameter> parameters,
                  List<Predicate<ConstrainableCase>> constraints,
                  Set<String> optionalParameterNames,
                  int n,
                  Long randomizationSeed,
                  boolean absenceCoverage) {
        this.hasConstraints = !constraints.isEmpty();
        this.optionalParameterNames = new HashSet<>(optionalParameterNames);
        for (Parameter parameter : parameters) {
            if (parameter.contains(Parameter.ABSENT)) {
                this.optionalParameterNames.add(parameter.getName());
            }
        }
        this.n = n;
        this.random = randomizationSeed == null ? null : new Random(randomizationSeed);
        this.itemMatrix = createItemMatrix(parameters);
        this.completer = new CaseCompleter(this.itemMatrix, constraints);
        this.combinationStorage = new CombinationStorage(
                this.n,
                this.itemMatrix,
                this.hasConstraints,
                !this.optionalParameterNames.isEmpty(),
                absenceCoverage
        );
        discoverFeasibleCombinations();
    }

    CombinationStorage getCombinationStorage() {
        return this.combinationStorage;
    }

    List<List<Item>> generateCases() {
        final List<List<Item>> sequences = new ArrayList<>();
        if (this.emptyCoverageCase != null) {
            sequences.add(this.emptyCoverageCase);
            return sequences;
        }

        while (true) {
            final List<Item> nextCase = generateNextCase();
            if (nextCase == null) {
                break;
            }
            sequences.add(nextCase);
        }

        CaseCompactor.compact(sequences, this.combinationStorage.getCoverageTables(),
                this.completer, !this.optionalParameterNames.isEmpty());
        return sequences;
    }

    /**
     * Generates the next case to cover combinations not covered yet.
     *
     * @return the case's items, or {@code null} when all required combinations are covered
     */
    private List<Item> generateNextCase() {
        final long expectedCount = this.combinationStorage.getExpectedCount();
        final long coveredCount = this.combinationStorage.getCoveredCount();
        if (coveredCount > expectedCount) {
            throw new RuntimeException("Actual number of test combinations exceeded possible maximum");
        }

        if (coveredCount == expectedCount) {
            return null; // All test combinations are found
        }

        final List<Item> seed = this.optionalParameterNames.isEmpty()
                ? Collections.emptyList()
                : this.combinationStorage.combinationFromMostUncoveredGroup();
        final Item[] pinned = new Item[this.itemMatrix.size()];
        for (Item item : seed) {
            pinned[item.getParameterIndex()] = item;
        }
        final Item[] assigned = pinned.clone();
        final List<Item> chosenItems = new ArrayList<>();
        for (int i = 0; i < this.itemMatrix.size(); i++) {
            rankCandidates(chosenItems, assigned, i);
            boolean extended = false;
            for (Item item : this.itemMatrix.get(i)) {
                if (pinned[i] != null && pinned[i] != item) {
                    continue;
                }
                chosenItems.add(item);
                if (isValidWithSeed(chosenItems, seed)) {
                    assigned[i] = item;
                    extended = true;
                    break;
                }
                chosenItems.remove(chosenItems.size() - 1);
            }
            if (!extended) {
                // Leave dead ends to constraint-driven completion instead of backtracking unrelated parameters.
                return generateCaseCoveringUncoveredCombination(seed);
            }
        }
        if (!this.combinationStorage.coversNewCombination(chosenItems)) {
            return generateCaseCoveringUncoveredCombination(seed);
        }
        this.combinationStorage.addSequenceCombinations(chosenItems);
        return chosenItems;
    }

    // Prefix positions equal parameter indices; seed may also pin parameters beyond the prefix.
    private boolean isValidWithSeed(List<Item> prefix, List<Item> seed) {
        if (seed.isEmpty()) {
            return this.completer.isValidPartialCase(prefix);
        }
        final List<Item> candidate = new ArrayList<>(prefix);
        for (Item item : seed) {
            if (item.getParameterIndex() >= prefix.size()) {
                candidate.add(item);
            }
        }
        return this.completer.isValidPartialCase(candidate);
    }

    private void rankCandidates(List<Item> prefix, Item[] assigned, int parameter) {
        final CompactTable target = this.combinationStorage.getTargetTable();
        final boolean contextual = !this.optionalParameterNames.isEmpty() && target.canScoreProjections(assigned, parameter);
        final List<Item> group = this.itemMatrix.get(parameter);
        for (Item item : group) {
            item.setWeights(candidateWeights(prefix, assigned, item, contextual));
        }

        if (this.random != null) {
            // Stable sorting preserves the shuffled order only among equally weighted candidates.
            Collections.shuffle(group, this.random);
        }
        Collections.sort(group);
    }

    // Compared lexicographically: earlier entries have priority, and smaller values win.
    private long[] candidateWeights(List<Item> prefix, Item[] assigned, Item item, boolean contextual) {
        final Node node = this.combinationStorage.getNodeOrCreateNew(item);
        final long[] weights = new long[this.n + 3 + (contextual ? 1 : 0)];
        int priority = 0;
        if (contextual) {
            // Include pinned future states: prefer immediate coverage, then compatible remaining tuples.
            final CompactTable.ProjectionScore projection = this.combinationStorage.getTargetTable().scoreProjections(assigned, item);
            weights[priority++] = -projection.getCompletedCount();
            weights[priority++] = -projection.getCompatibleCount();
        } else {
            weights[priority++] = -this.combinationStorage.countNewCombinations(prefix, item, this.n);
        }
        weights[priority++] = node.getOutboundCount();
        // Break ties by reusing covered lower-strength combinations, from n-1 down to individual values.
        for (int strength = this.n - 1; strength >= 1; strength--) {
            weights[priority++] = this.combinationStorage.countNewCombinations(prefix, item, strength);
        }
        weights[priority++] = node.getCounter();
        weights[priority] = -node.getInboundCount();
        return weights;
    }

    private List<Item> generateCaseCoveringUncoveredCombination(List<Item> seed) {
        final List<Item> combination = seed.isEmpty() ? this.combinationStorage.firstUncoveredCombination() : seed;
        final List<Item> completion = this.completer.complete(combination);
        if (completion == null) {
            throw new IllegalStateException("Previously feasible combination has no valid completion");
        }
        this.combinationStorage.addSequenceCombinations(completion);
        return completion;
    }

    private List<List<Item>> createItemMatrix(List<Parameter> parameters) {
        CompactTable.checkedCapacities(parameters.stream()
                .mapToInt(parameter -> parameter.size() + (needsImplicitAbsence(parameter) ? 1 : 0))
                .toArray(), this.n);
        final List<List<Item>> matrix = new ArrayList<>();

        int i = 0;
        int globalValueIndex = 0;
        for (Parameter parameter : parameters) {
            matrix.add(new ArrayList<>());
            for (int j = 0; j < parameter.size(); j++) {
                matrix.get(i).add(parameter.get(j) == Parameter.ABSENT
                        ? Item.absent(i, j, globalValueIndex++, parameter.getName())
                        : new Item(i, j, globalValueIndex++, parameter.get(j), parameter.getName()));
            }
            if (needsImplicitAbsence(parameter)) {
                matrix.get(i).add(Item.absent(i, parameter.size(), globalValueIndex++, parameter.getName()));
            }
            i++;
        }

        return matrix;
    }

    private boolean needsImplicitAbsence(Parameter parameter) {
        return this.optionalParameterNames.contains(parameter.getName()) && !parameter.contains(Parameter.ABSENT);
    }

    private void discoverFeasibleCombinations() {
        if (!this.hasConstraints && this.optionalParameterNames.isEmpty()) {
            return;
        }
        List<Item> lastWitness = this.completer.complete(Collections.emptyList());
        if (lastWitness == null) {
            this.combinationStorage.getCoverageTables().forEach(CompactTable::excludeAll);
            return;
        }
        // Project only the first witness: projecting every witness would repeat mostly cached tuples.
        this.combinationStorage.rememberFeasibleCombinations(lastWitness);
        for (CompactTable table : this.combinationStorage.getCoverageTables()) {
            for (CompactTable.Group group : table.getGroups()) {
                for (int index = 0; index < group.getCapacity(); index++) {
                    if (group.isClassified(index)) {
                        continue;
                    }
                    if (group.indexOf(lastWitness) != index) {
                        final List<Item> combination = this.combinationStorage.decode(group, index);
                        final List<Item> witness = this.completer.complete(combination);
                        if (witness == null) {
                            table.exclude(group, index);
                            continue;
                        }
                        lastWitness = witness;
                    }
                    table.markFeasible(group, index);
                }
            }
        }
        this.combinationStorage.removeImpliedTargets();
        if (this.combinationStorage.getExpectedCount() == 0) {
            this.emptyCoverageCase = lastWitness;
        }
    }
}
