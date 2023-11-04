/*
 * Copyright 2023 Pavel Nazimok - @pavelicii
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
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

/**
 * Main class containing AllPairs algorithm and providing generated test {@link Case}s.
 * Must be instantiated using {@link AllPairsBuilder}.
 */
public final class AllPairs implements Iterable<Case> {

    private final List<Parameter> parameters;
    private final List<Predicate<ConstrainableCase>> constraints;
    /** Test combination size. */
    private final int n;
    private final boolean printEachCaseDuringGeneration;

    private final CombinationStorage combinationStorage;
    /** Expected unique {@code n}-wise test combinations (considering constraints). */
    private final List<Map<String, Object>> expectedUniqueTestCombinations;
    /** Generated unique {@code n}-wise test combinations. */
    private List<Map<String, Object>> generatedUniqueTestCombinations;
    private final List<List<Item>> itemMatrix;

    private final List<Case> generatedCases;

    private AllPairs(AllPairsBuilder allPairsBuilder) {
        this.parameters = allPairsBuilder.parameters;
        this.constraints = allPairsBuilder.constraints;
        this.n = allPairsBuilder.n;
        this.printEachCaseDuringGeneration = allPairsBuilder.printEachCaseDuringGeneration;

        this.combinationStorage = new CombinationStorage(this.n);
        this.itemMatrix = createItemMatrix(this.parameters);
        this.expectedUniqueTestCombinations = findExpectedUniqueTestCombinations();

        this.generatedCases = generateCases();
    }

    /**
     * The entry point for users.
     *
     * @see AllPairsBuilder#withParameter(Parameter)
     * @see AllPairsBuilder#withConstraint(Predicate)
     * @see AllPairsBuilder#withTestCombinationSize(int)
     */
    public static class AllPairsBuilder {

        private final List<Parameter> parameters;
        private final List<Predicate<ConstrainableCase>> constraints;
        private int n;
        private boolean printEachCaseDuringGeneration;

        public AllPairsBuilder() {
            this.parameters = new ArrayList<>();
            this.constraints = new ArrayList<>();
            this.n = 2;
            this.printEachCaseDuringGeneration = false;
        }

        /**
         * Adds one {@link Parameter}.
         * <ul>
         *     <li>Must provide at least two {@link Parameter}s
         *     <li>Each {@link Parameter} must have at least one value
         *     <li>Each {@link Parameter} must have no duplicate values
         *     <li>{@link Parameter} {@code name} must be unique
         * </ul>
         * <strong>Examples:</strong>
         * <pre>{@code
         *     new Parameter("OS", "Windows", "Linux", "macOS")
         *     new Parameter("RAM", 2048, 4096, 8192, 16384)
         * }</pre>
         *
         * @param parameter {@link Parameter}
         * @return a reference to {@link AllPairsBuilder} object
         */
        public AllPairsBuilder withParameter(Parameter parameter) {
            Objects.requireNonNull(parameter, "Parameter must be non-null");
            this.parameters.add(parameter);
            return this;
        }

        /**
         * Adds {@link List} of {@link Parameter}s.
         * <p>
         * For detailed description see {@link AllPairsBuilder#withParameter(Parameter)}.
         *
         * @param parameters {@link List} of {@link Parameter}s
         * @return a reference to {@link AllPairsBuilder} object
         * @see AllPairsBuilder#withParameter(Parameter)
         */
        public AllPairsBuilder withParameters(List<Parameter> parameters) {
            Objects.requireNonNull(parameters, "Parameters must be non-null");
            this.parameters.addAll(parameters);
            return this;
        }

        /**
         * Adds one test {@link Case} constraint as {@link Predicate}. Each potential test {@link Case} is tested
         * against it. If test evaluates to {@code true}, the {@link Case} under test won't be present in the result
         * and the algorithm will search for another {@link Case}, so that in the end all possible {@code n}-wise test
         * combinations are covered (considering all constraints).
         * <ul>
         *     <li> If not specified, all possible test combinations will be generated
         *     <li> If non-existing {@link Parameter} name is provided, it will always evaluate to {@code false}
         * </ul>
         * <strong>Examples:</strong>
         * <pre>{@code
         *     // Cases with "Foo" and "Bar" pair won't be generated:
         *     c -> c.get("paramName1").equals("Foo") && c.get("paramName2").equals("Bar")
         *     // Cases with "paramName"'s value greater than 5 won't be generated:
         *     c -> c.get("paramName") > 5
         * }</pre>
         * <p>
         * <strong>Recommended usage:</strong>
         * <p>
         * Try to simplify constraints as much as possible. Too complicated constraints might cause longer algorithm
         * processing time, especially on a large input of {@link Parameter}s. For example, consider two different
         * constraints for the following input:
         * <pre>{@code
         *     Browser: "Chrome"
         *     OS:      "Linux", "macOS"
         *     Drive:   "HDD", "SSD"
         *
         *     // complicatedConstraint (filter out 'Chrome-Linux-HDD' combination):
         *     c -> c.get("Browser").equals("Chrome") && c.get("OS").equals("Linux") && c.get("Drive").equals("HDD")
         *     // simplifiedConstraint (filter out 'Linux-HDD' pair):
         *     c -> c.get("OS").equals("Linux") && c.get("Drive").equals("HDD")
         * }</pre>
         * It is better to use {@code simplifiedConstraint}, because the usage of {@code complicatedConstraint}
         * implies there might be pairs including non-{@code 'Chrome'} browsers, while in fact there is only one
         * possible browser.
         *
         * @param constraint {@link Predicate}{@code <ConstrainableCase>} to filter out unwanted {@link Case}s
         * @return a reference to {@link AllPairsBuilder} object
         * @see ConstrainableCase
         * @see AllPairsBuilder#withParameter(Parameter)
         */
        public AllPairsBuilder withConstraint(Predicate<ConstrainableCase> constraint) {
            Objects.requireNonNull(constraint, "Constraint must be non-null");
            this.constraints.add(constraint);
            return this;
        }

        /**
         * Adds {@link List} of {@link Case} constraints to filter {@code n}-wise test combinations.
         * <p>
         * For detailed description see {@link AllPairsBuilder#withConstraint(Predicate)}.
         *
         * @param constraints {@link List} of {@link Predicate}s to filter out unwanted {@link Case}s
         * @return a reference to {@link AllPairsBuilder} object
         * @see AllPairsBuilder#withConstraint(Predicate)
         */
        public AllPairsBuilder withConstraints(List<Predicate<ConstrainableCase>> constraints) {
            Objects.requireNonNull(constraints, "Constraints must be non-null");
            this.constraints.addAll(constraints);
            return this;
        }

        /**
         * Specifies test combination length. 2 - pairwise, 3 - triplewise, etc.
         * <ul>
         *     <li>Must be greater than or equal to 2
         *     <li>Must be less than or equal to the number of {@link Parameter}s
         *     <li>If not specified, the default value 2 will be used
         * </ul>
         *
         * @param n length of n-wise test combination
         * @return a reference to {@link AllPairsBuilder} object
         * @see AllPairsBuilder#withParameter(Parameter)
         */
        public AllPairsBuilder withTestCombinationSize(int n) {
            this.n = n;
            return this;
        }

        /**
         * Specifies that each {@link Case} should be printed during generation.
         * It could be useful for debug or to identify problems when generation takes too long.
         * <p>
         * If not specified, printing will be disabled.
         *
         * @return a reference to {@link AllPairsBuilder} object
         */
        public AllPairsBuilder printEachCaseDuringGeneration() {
            this.printEachCaseDuringGeneration = true;
            return this;
        }

        /**
         * Using provided configuration, builds a new instance of {@link AllPairs} with generated test {@link Case}s.
         *
         * @return {@link AllPairs} instance
         */
        public AllPairs build() {
            validate();
            return new AllPairs(this);
        }

        private void validate() {
            if (this.n < 2) {
                throw new IllegalArgumentException("Minimum test combination size is 2. Provided: " + this.n);
            }

            if (this.parameters.size() < this.n) {
                throw new IllegalArgumentException(String.format(
                        "The number of Parameters (%d) must be greater than ot equal to the test combination size (%d)",
                        this.parameters.size(), this.n
                ));
            }

            this.parameters.forEach(parameter -> {
                if (parameter.isEmpty()) {
                    throw new IllegalArgumentException("Each Parameter must have at least one value. "
                            + "Provided Parameter with no values: " + parameter.getName());
                }

                if (parameter.getName() == null) {
                    throw new IllegalArgumentException("Parameter name must be non-null. Provided: " + parameter);
                }

                if (parameter.getName().isEmpty()) {
                    throw new IllegalArgumentException("Parameter name must not be empty. Provided: " + parameter);
                }

                if (parameter.stream().distinct().count() != parameter.size()) {
                    throw new IllegalArgumentException(
                            "Each Parameter must have no duplicate values. Provided: " + parameter
                    );
                }
            });

            final Set<String> nonUniqueParameterNamesFiller = new HashSet<>();
            final Set<String> nonUniqueParameterNames = this.parameters.stream()
                    .map(Parameter::getName)
                    .filter(name -> !nonUniqueParameterNamesFiller.add(name))
                    .collect(Collectors.toSet());
            if (!nonUniqueParameterNames.isEmpty()) {
                throw new IllegalArgumentException(
                        "Parameter name must be unique. Provided non-unique names: " + nonUniqueParameterNames
                );
            }
        }
    }

    public List<Parameter> getParameters() {
        return this.parameters;
    }

    public int getTestCombinationSize() {
        return this.n;
    }

    public List<Predicate<ConstrainableCase>> getConstraints() {
        return this.constraints;
    }

    /**
     * Returns generated test {@link Case}s.
     *
     * @return {@link List} of {@link Case}s
     */
    public List<Case> getGeneratedCases() {
        return this.generatedCases;
    }

    /**
     * Returns expected unique {@code n}-wise tests combinations (considering constraints).
     *
     * @return {@link List} of {@link Map}s of {@code n}-wise test combinations, where key and value corresponds to
     *     a {@link Parameter}'s name and one of its values respectively
     */
    List<Map<String, Object>> getExpectedUniqueTestCombinations() {
        return this.expectedUniqueTestCombinations;
    }

    /**
     * Returns generated unique {@code n}-wise tests combinations.
     *
     * @return {@link List} of {@link Map}s of {@code n}-wise test combinations, where key and value corresponds to
     *     a {@link Parameter}'s name and one of its values respectively
     */
    List<Map<String, Object>> getGeneratedUniqueTestCombinations() {
        if (this.generatedUniqueTestCombinations == null) {
            this.generatedUniqueTestCombinations = findGeneratedUniqueTestCombinations();
        }
        return this.generatedUniqueTestCombinations;
    }

    @Override
    public Iterator<Case> iterator() {
        return this.generatedCases.iterator();
    }

    @Override
    public String toString() {
        if (this.generatedCases != null) {
            final AtomicInteger index = new AtomicInteger(1);
            return this.generatedCases.stream()
                    .map(c -> String.format("%3d: %s", index.getAndIncrement(), c.toString()))
                    .collect(Collectors.joining(System.lineSeparator()));
        } else {
            return "Cases are not generated yet";
        }
    }

    private List<Case> generateCases() {
        final List<Case> cases = new ArrayList<>();

        int caseCount = 0;
        while (true) {
            final Case nextCase = generateNextCase();
            if (nextCase == null) {
                break;
            }
            if (this.printEachCaseDuringGeneration) {
                System.out.printf("%3d: %s%n", ++caseCount, nextCase);
            }
            cases.add(nextCase);
        }

        return cases;
    }

    /**
     * Generates next test {@link Case} using AllPairs algorithm.
     *
     * @return {@link Case} or {@code null} if all {@link Case}s are already found
     */
    private Case generateNextCase() {
        if (this.combinationStorage.getLength() > this.expectedUniqueTestCombinations.size()) {
            throw new RuntimeException("Actual number of test combinations exceeded possible maximum");
        }

        if (this.combinationStorage.getLength() == this.expectedUniqueTestCombinations.size()) {
            return null; // All test combinations are found
        }

        final int previousUniqueTestCombinationsCount = this.combinationStorage.getLength();
        final List<Item> chosenItems = new ArrayList<>();
        final List<Integer> itemIndexes = new ArrayList<>();
        for (int i = 0; i < this.itemMatrix.size(); i++) {
            chosenItems.add(null);
            itemIndexes.add(null);
        }

        int direction = 1;
        int i = 0; // Item group index

        while (i > -1 && i < this.itemMatrix.size()) {
            if (direction == 1) {
                updateWeightsAndReSortItemMatrix(chosenItems.subList(0, i), i);
                itemIndexes.set(i, 0);
            } else {
                itemIndexes.set(i, itemIndexes.get(i) + 1);
                if (itemIndexes.get(i) >= this.itemMatrix.get(i).size()) {
                    direction = -1;
                    if (i == 0) {
                        return null; // Can't find more new test combinations after all values brute force
                    }
                    i += direction;
                    continue;
                }
            }

            chosenItems.set(i, this.itemMatrix.get(i).get(itemIndexes.get(i)));

            if (this.constraints == null || this.constraints.isEmpty() || isValidCase(chosenItems.subList(0, i + 1))) {
                direction = 1;
            } else {
                direction = 0;
            }

            i += direction;

            if (i == this.itemMatrix.size()) {
                this.combinationStorage.addSequenceCombinations(chosenItems);
                // Chosen items didn't produce new test combinations
                if (this.combinationStorage.getLength() == previousUniqueTestCombinationsCount) {
                    direction = -1;
                    i += direction;
                }
            }
        }

        return new Case(chosenItems);
    }

    private void updateWeightsAndReSortItemMatrix(List<Item> chosenItems, int itemGroupIndex) {
        for (Item item : this.itemMatrix.get(itemGroupIndex)) {
            final Node node = this.combinationStorage.getNodeOrCreateNew(item);

            final List<Set<List<String>>> newItemIdCombinations = new ArrayList<>();
            for (int i = 0; i < this.n; i++) {
                final List<Item> items = new ArrayList<>(chosenItems);
                items.add(item);

                final Set<List<String>> newItemIdCombination = StreamSupport
                        .stream(Itertools.combinations(items, i + 1).spliterator(), false)
                        .map(itemCombination -> itemCombination.stream()
                                .map(Item::getItemId)
                                .collect(Collectors.toList()))
                        .collect(Collectors.toSet());
                newItemIdCombination.removeAll(this.combinationStorage.getItemIdCombinations().get(i));

                newItemIdCombinations.add(newItemIdCombination);
            }

            final List<Integer> weights = new ArrayList<>();
            // Node that creates most new test combinations is the best
            weights.add(-newItemIdCombinations.get(newItemIdCombinations.size() - 1).size());
            // Less used outbound connections are most likely to produce more test combinations
            weights.add(node.getOutboundItemIdsSize());
            if (newItemIdCombinations.size() >= 2) {
                for (int i = newItemIdCombinations.size() - 2; i >= 0; i--) {
                    weights.add(newItemIdCombinations.get(i).size());
                }
            }
            weights.add(node.getCounter()); // Less used node is better
            weights.add(-node.getInboundItemIdsSize()); // Prefer node with most free inbound connections

            item.setWeights(weights);
        }

        Collections.sort(this.itemMatrix.get(itemGroupIndex));
    }

    private List<List<Item>> createItemMatrix(List<Parameter> parameters) {
        final List<List<Item>> matrix = new ArrayList<>();

        int i = 0;
        for (Parameter parameter : parameters) {
            matrix.add(new ArrayList<>());
            for (int j = 0; j < parameter.size(); j++) {
                matrix.get(i).add(new Item(String.format("a%dv%d", i, j), parameter.get(j), parameter.getName()));
            }
            i++;
        }

        return matrix;
    }

    /**
     * Tests all constraints.
     *
     * @param items {@link Item}s {@link List} representing possible {@link Case} to test constraint against
     * @return {@code false} if met at least one constraint, {@code true} if met no constraints
     */
    private boolean isValidCase(List<Item> items) {
        if (this.constraints.isEmpty()) {
            return true;
        }

        final ConstrainableCase constrainableCase = new ConstrainableCase(items);
        for (Predicate<ConstrainableCase> constraint : this.constraints) {
            try {
                if (constraint.test(constrainableCase)) {
                    return false;
                }
            } catch (ConstrainableCase.NoSuchParameterNameException ignored) {
                // NoSuchKeyInCaseException is used for program flow to allow Constraints to work as Predicates
                // NoSuchKeyInCaseException is optimized to cause as little performance penalty as possible
            }
        }

        return true;
    }

    private List<Map<String, Object>> findExpectedUniqueTestCombinations() {
        return StreamSupport
                .stream(Itertools.combinations(this.itemMatrix, this.n).spliterator(), false)
                .flatMap(itemsComb -> StreamSupport.stream(Itertools.product(itemsComb).spliterator(), false))
                .filter(this::isValidCase)
                .map(items -> items.stream().collect(Collectors.toMap(
                        Item::getName,
                        Item::getValue,
                        (key1, key2) -> key1,
                        LinkedHashMap::new)))
                .collect(Collectors.toList());
    }

    private List<Map<String, Object>> findGeneratedUniqueTestCombinations() {
        return this.generatedCases.stream()
                .map(aCase -> aCase.entrySet().stream()
                        .map(entry -> {
                            final Map<String, Object> parameter = new HashMap<>();
                            parameter.put(entry.getKey(), entry.getValue());
                            return parameter;
                        }).collect(Collectors.toList()))
                .flatMap(caseAsMapPerValue ->
                        StreamSupport.stream(Itertools.combinations(caseAsMapPerValue, this.n).spliterator(), false))
                .distinct()
                .map(testCombinationAsMapPerValue -> testCombinationAsMapPerValue.stream()
                        .reduce(new LinkedHashMap<>(), (testCombination, value) -> {
                            testCombination.putAll(value);
                            return testCombination;
                        }))
                .collect(Collectors.toList());
    }
}
