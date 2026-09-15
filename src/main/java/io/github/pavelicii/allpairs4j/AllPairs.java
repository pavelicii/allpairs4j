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
import java.util.Iterator;
import java.util.List;
import java.util.Objects;
import java.util.Random;
import java.util.Set;
import java.util.StringJoiner;
import java.util.function.Predicate;
import java.util.stream.Collectors;

/**
 * Generates a compact set of test cases that covers the required combinations.
 * Use {@link AllPairsBuilder} to set up parameters and constraints, then build the results.
 */
public final class AllPairs implements Iterable<Case> {

    private final List<Parameter> parameters;
    private final List<Predicate<ConstrainableCase>> caseConstraints;
    /** Number of parameters in each test combination. */
    private final int n;
    private final Long randomizationSeed;
    private final boolean absenceCoverage;
    private final boolean printGenerationDetails;

    private final List<Case> generatedCases;

    private AllPairs(AllPairsBuilder allPairsBuilder) {
        this.parameters = copyParameters(allPairsBuilder.parameters);
        allPairsBuilder.validate(this.parameters);
        this.n = allPairsBuilder.n;
        this.randomizationSeed = allPairsBuilder.resolveRandomizationSeed();
        this.absenceCoverage = allPairsBuilder.absenceCoverage;
        this.printGenerationDetails = allPairsBuilder.printGenerationDetails;
        this.caseConstraints = Collections.unmodifiableList(new ArrayList<>(allPairsBuilder.caseConstraints));
        final List<Predicate<ConstrainableCase>> constraints = new ArrayList<>();
        final Set<String> parameterNames = this.parameters.stream().map(Parameter::getName).collect(Collectors.toSet());
        for (Predicate<ConstrainableCase> constraint : this.caseConstraints) {
            constraints.add(c -> evaluateConstraint(constraint, c, parameterNames, "case constraint"));
        }
        final Set<String> optionalParameterNames = new HashSet<>();
        for (Parameter parameter : this.parameters) {
            if (parameter.getConstraint() != null) {
                optionalParameterNames.add(parameter.getName());
                constraints.add(createParameterConstraint(parameter, parameterNames));
            }
        }
        final CaseGenerator generator = new CaseGenerator(
                this.parameters,
                constraints,
                optionalParameterNames,
                this.n,
                this.randomizationSeed,
                this.absenceCoverage
        );
        final CombinationStorage combinationStorage = generator.getCombinationStorage();
        if (this.printGenerationDetails) {
            GenerationDetailsPrinter.printRequiredCombinations(combinationStorage);
        }

        final List<List<Item>> sequences = generator.generateCases();
        if (this.printGenerationDetails) {
            GenerationDetailsPrinter.printGeneratedCases(combinationStorage, sequences, this.parameters, this.absenceCoverage);
        }
        this.generatedCases = sequences.stream().map(Case::new).collect(Collectors.toList());
    }

    private static List<Parameter> copyParameters(List<Parameter> parameters) {
        final List<Parameter> copies = new ArrayList<>(parameters.size());
        for (Parameter parameter : parameters) {
            copies.add(new Parameter(parameter));
        }
        return Collections.unmodifiableList(copies);
    }

    private static Predicate<ConstrainableCase> createParameterConstraint(Parameter parameter, Set<String> parameterNames) {
        final String name = parameter.getName();
        final String context = "parameter constraint for '" + name + "'";
        final Predicate<ConstrainableCase> parameterConstraint =
                c -> evaluateConstraint(parameter.getConstraint(), c, parameterNames, context);
        if (parameter.contains(Parameter.ABSENT)) {
            // Explicit absence remains allowed even when the parameter is applicable.
            return c -> isExcluded(parameterConstraint, c) && c.isPresent(name);
        }
        // Otherwise, reject cases where presence is not the opposite of exclusion.
        return c -> c.isPresent(name) == isExcluded(parameterConstraint, c);
    }

    private static boolean isExcluded(Predicate<ConstrainableCase> parameterConstraint, ConstrainableCase candidate) {
        try {
            return parameterConstraint.test(candidate);
        } catch (ConstrainableCase.InapplicableParameterException ignored) {
            return true;
        }
    }

    private static boolean evaluateConstraint(Predicate<ConstrainableCase> predicate,
                                              ConstrainableCase candidate, Set<String> parameterNames, String context) {
        try {
            return predicate.test(candidate);
        } catch (ConstrainableCase.NoSuchParameterNameException missing) {
            final String name = candidate.getMissingParameterName();
            if (!parameterNames.contains(name)) {
                throw new IllegalArgumentException("Unknown parameter in " + context + ": " + name, missing);
            }
            throw missing;
        } catch (ConstrainableCase.InapplicableParameterException absent) {
            throw absent; // Control flow for the generator, not a failure of the predicate.
        } catch (RuntimeException failure) {
            throw new IllegalArgumentException("Error in " + context, failure);
        }
    }

    /**
     * Sets up generation. Call {@link #build()} when the parameters and constraints are ready.
     *
     * @see AllPairsBuilder#withParameter(Parameter)
     * @see Parameter#withConstraint(Predicate)
     * @see AllPairsBuilder#withConstraint(Predicate)
     * @see AllPairsBuilder#withTestCombinationSize(int)
     * @see AllPairsBuilder#withRandomization(boolean)
     * @see AllPairsBuilder#withAbsenceCoverage(boolean)
     * @see AllPairsBuilder#printGenerationDetails(boolean)
     */
    public static class AllPairsBuilder {

        private final List<Parameter> parameters;
        private final List<Predicate<ConstrainableCase>> caseConstraints;
        private int n;
        private boolean randomization;
        private Long randomizationSeed;
        private boolean absenceCoverage = true;
        private boolean printGenerationDetails;

        public AllPairsBuilder() {
            this.parameters = new ArrayList<>();
            this.caseConstraints = new ArrayList<>();
            this.n = 2;
        }

        /**
         * Adds a parameter, its values and any {@link Parameter#withConstraint(Predicate) constraint}.
         * Include {@link Parameter#ABSENT} to allow cases without this parameter.
         * <ul>
         *     <li>Add at least two parameters.
         *     <li>Give each parameter at least one value, with no duplicates.
         *     <li>Use a unique, nonempty name for each parameter. Names cannot be {@code null}.
         * </ul>
         * For example:
         * <pre>{@code
         *     new Parameter("OS", "Windows", "Linux", "macOS")
         *     new Parameter("RAM", 2048, 4096, 8192, 16384)
         * }</pre>
         *
         * @param parameter parameter to add
         * @return this builder
         */
        public AllPairsBuilder withParameter(Parameter parameter) {
            Objects.requireNonNull(parameter, "Parameter must be non-null");
            this.parameters.add(parameter);
            return this;
        }

        /**
         * Adds several parameters. The same requirements apply as for {@link #withParameter(Parameter)}.
         *
         * @param parameters parameters to add
         * @return this builder
         * @see AllPairsBuilder#withParameter(Parameter)
         */
        public AllPairsBuilder withParameters(List<Parameter> parameters) {
            Objects.requireNonNull(parameters, "Parameters must be non-null");
            parameters.forEach(this::withParameter);
            return this;
        }

        /**
         * Adds a case constraint. Return {@code true} to reject a case;
         * {@code false} means this constraint does not reject it.
         * If any constraint rejects a case, the generator looks for another one.
         * No constraints are added by default.
         * <p>
         * For example:
         * <pre>{@code
         *     // Reject cases with both "Foo" and "Bar":
         *     c -> c.get("paramName1").equals("Foo") && c.get("paramName2").equals("Bar")
         *     // Reject cases where "paramName" is greater than 5:
         *     c -> (int) c.get("paramName") > 5
         * }</pre>
         * <p>
         * The results cover every required combination that fits in a complete case allowed by all constraints.
         * You don't need to simplify constraints for correct coverage, but simpler ones may make generation faster.
         * Complex constraints can require a search that grows exponentially.
         * <p>
         * Use constraints only to check values, not to change data or perform other actions.
         * A constraint must return the same result for the same values.
         * Do not catch exceptions from {@link ConstrainableCase#get(String)} or
         * {@link ConstrainableCase#isPresent(String)}: the generator uses them to handle missing values.
         * Reading an absent value skips that constraint for the current check.
         * Known parameters not yet chosen are resolved before finishing the check.
         * Reading an unknown parameter name throws {@link IllegalArgumentException} with that name.
         * Other runtime exceptions from the predicate are wrapped in {@code IllegalArgumentException}
         * with the constraint's context and the original exception as the cause.
         *
         * @param caseConstraint predicate that returns {@code true} for unwanted cases
         * @return this builder
         * @see ConstrainableCase
         * @see AllPairsBuilder#withParameter(Parameter)
         */
        public AllPairsBuilder withConstraint(Predicate<ConstrainableCase> caseConstraint) {
            Objects.requireNonNull(caseConstraint, "Constraint must be non-null");
            this.caseConstraints.add(caseConstraint);
            return this;
        }

        /**
         * Adds several case constraints. See {@link #withConstraint(Predicate)} for how constraints work.
         *
         * @param caseConstraints predicates that return {@code true} for unwanted cases
         * @return this builder
         * @see AllPairsBuilder#withConstraint(Predicate)
         */
        public AllPairsBuilder withConstraints(List<Predicate<ConstrainableCase>> caseConstraints) {
            Objects.requireNonNull(caseConstraints, "Constraints must be non-null");
            caseConstraints.forEach(this::withConstraint);
            return this;
        }

        /**
         * Sets how many parameters each test combination includes: 2 for pairs, 3 for triples, and so on.
         * The default is 2. The size must be at least 2 and no greater than the number of parameters.
         *
         * @param n number of parameters in each combination
         * @return this builder
         * @see AllPairsBuilder#withParameter(Parameter)
         */
        public AllPairsBuilder withTestCombinationSize(int n) {
            this.n = n;
            return this;
        }

        /**
         * Chooses randomly between equally ranked values during generation. Off by default.
         * Case contents, order, and count may change, but all constraints and required coverage still hold.
         * Different seeds may still produce identical cases.
         * <p>
         * This overload clears any previously configured seed. When enabled, each build chooses a new seed.
         * Retrieve it with {@link AllPairs#getRandomizationSeed()} and pass it to
         * {@link #withRandomization(boolean, long)} to reproduce that generation.
         *
         * @param enabled {@code true} to randomize choices between equally ranked values; {@code false} to disable it
         * @return this builder
         */
        public AllPairsBuilder withRandomization(boolean enabled) {
            this.randomization = enabled;
            this.randomizationSeed = null;
            return this;
        }

        /**
         * Chooses randomly between equally ranked values during generation. Off by default.
         * Case contents, order, and count may change, but all constraints and required coverage still hold.
         * Different seeds may still produce identical cases.
         * <p>
         * This overload uses the supplied seed. When enabled, each build reuses that seed
         * instead of choosing a new one.
         * The same seed, library version, ordered parameters and values, settings and pure constraints
         * produce the same cases in the same order.
         *
         * @param enabled {@code true} to randomize choices between equally ranked values; {@code false} to disable it
         * @param seed seed to reuse on every build for reproducible generation; ignored when {@code enabled} is false
         * @return this builder
         */
        public AllPairsBuilder withRandomization(boolean enabled, long seed) {
            this.randomization = enabled;
            this.randomizationSeed = enabled ? seed : null;
            return this;
        }

        /**
         * Sets whether combinations with absent parameters need to be covered. On by default.
         * Turning this off skips those combinations, but still allows parameters to be absent in cases.
         * For models with optional parameters, all combinations of present values from size 1 through
         * {@code n} are still covered if they fit in a valid case. A declared {@code null} counts as present.
         * If there is nothing to cover, the generator returns one valid case if any exists.
         *
         * @param enabled whether to cover combinations with absent parameters
         * @return this builder
         */
        public AllPairsBuilder withAbsenceCoverage(boolean enabled) {
            this.absenceCoverage = enabled;
            return this;
        }

        /**
         * Prints the required combinations and their count to {@code System.out}, followed by the final cases
         * and their count after removing redundant cases.
         * Under each case, lists only combinations not covered by earlier cases in the final list.
         * Off by default. Large models can produce a lot of output.
         *
         * @param enabled whether to print generation details
         * @return this builder
         */
        public AllPairsBuilder printGenerationDetails(boolean enabled) {
            this.printGenerationDetails = enabled;
            return this;
        }

        /**
         * Checks the settings and generates test cases in a new {@link AllPairs} instance.
         *
         * @return the settings and generated cases
         */
        public AllPairs build() {
            return new AllPairs(this);
        }

        private Long resolveRandomizationSeed() {
            if (!this.randomization) {
                return null;
            }
            return this.randomizationSeed != null ? this.randomizationSeed : new Random().nextLong();
        }

        private void validate(List<Parameter> parameters) {
            if (this.n < 2) {
                throw new IllegalArgumentException("Minimum test combination size is 2. Provided: " + this.n);
            }

            if (parameters.size() < this.n) {
                throw new IllegalArgumentException(String.format(
                        "The number of Parameters (%d) must be greater than or equal to the test combination size (%d)",
                        parameters.size(), this.n
                ));
            }

            parameters.forEach(parameter -> {
                if (parameter.isEmpty()) {
                    throw new IllegalArgumentException(
                            "Each Parameter must have at least one value. Provided Parameter with no values: " + parameter.getName());
                }

                if (parameter.getName() == null) {
                    throw new IllegalArgumentException("Parameter name must be non-null. Provided: " + parameter);
                }

                if (parameter.getName().isEmpty()) {
                    throw new IllegalArgumentException("Parameter name must not be empty. Provided: " + parameter);
                }

                if (parameter.stream().distinct().count() != parameter.size()) {
                    throw new IllegalArgumentException("Each Parameter must have no duplicate values. Provided: " + parameter);
                }
            });

            final Set<String> seenNames = new HashSet<>();
            final Set<String> duplicateNames = new HashSet<>();
            for (Parameter parameter : parameters) {
                if (!seenNames.add(parameter.getName())) {
                    duplicateNames.add(parameter.getName());
                }
            }
            if (!duplicateNames.isEmpty()) {
                throw new IllegalArgumentException("Parameter name must be unique. Provided non-unique names: " + duplicateNames);
            }
        }
    }

    /**
     * Creates an independent builder with the same parameters, constraints and generation settings.
     * Preserves the actual random seed, including an automatically chosen seed.
     * Call {@link AllPairsBuilder#withRandomization(boolean) withRandomization(true)} for a fresh seed on each build.
     *
     * @return a builder initialized with these settings
     */
    public AllPairsBuilder toBuilder() {
        final AllPairsBuilder builder = new AllPairsBuilder()
                .withParameters(copyParameters(this.parameters))
                .withConstraints(this.caseConstraints)
                .withTestCombinationSize(this.n)
                .withRandomization(this.randomizationSeed != null)
                .withAbsenceCoverage(this.absenceCoverage)
                .printGenerationDetails(this.printGenerationDetails);
        builder.randomizationSeed = this.randomizationSeed;
        return builder;
    }

    /**
     * Returns the parameters used for generation.
     *
     * @return the parameters used for generation
     */
    public List<Parameter> getParameters() {
        return copyParameters(this.parameters);
    }

    public int getTestCombinationSize() {
        return this.n;
    }

    /**
     * Returns the seed used for this generation, whether supplied explicitly or chosen automatically.
     *
     * @return the actual seed, or {@code null} when randomization is disabled
     */
    public Long getRandomizationSeed() {
        return this.randomizationSeed;
    }

    /**
     * Returns the user-supplied case constraints.
     *
     * @return the user-supplied case constraints
     */
    public List<Predicate<ConstrainableCase>> getConstraints() {
        return this.caseConstraints;
    }

    /**
     * Returns a new list containing a defensive copy of each generated case.
     * Changing the list or its case mappings does not change the stored results.
     * The parameter value objects themselves are shared, not deep-copied.
     *
     * @return a mutable snapshot of the generated cases
     */
    public List<Case> getGeneratedCases() {
        return this.generatedCases.stream().map(Case::new).collect(Collectors.toList());
    }

    /**
     * Iterates over a defensive snapshot, with the same copying rules as {@link #getGeneratedCases()}.
     *
     * @return an iterator over copies of the generated cases
     */
    @Override
    public Iterator<Case> iterator() {
        return getGeneratedCases().iterator();
    }

    @Override
    public String toString() {
        final StringJoiner result = new StringJoiner(System.lineSeparator());
        if (this.randomizationSeed != null) {
            result.add("Randomization seed: " + this.randomizationSeed);
        }
        for (int i = 0; i < this.generatedCases.size(); i++) {
            final String formatted = GenerationDetailsPrinter.formatCase(
                    this.parameters, this.generatedCases.get(i), this.absenceCoverage);
            result.add(String.format("%3d: %s", i + 1, formatted));
        }
        return result.toString();
    }
}
