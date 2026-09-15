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
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Predicate;

/** Checks partial assignments and completes them by resolving parameters requested by constraints. */
final class CaseCompleter {

    private final List<List<Item>> itemMatrix;
    private final List<Predicate<ConstrainableCase>> constraints;
    private final Set<String> parameterNames = new HashSet<>();

    CaseCompleter(List<List<Item>> itemMatrix, List<Predicate<ConstrainableCase>> constraints) {
        // Share candidate order with the greedy generator, including its latest weights and randomization.
        this.itemMatrix = itemMatrix;
        this.constraints = constraints;
        for (List<Item> group : itemMatrix) {
            this.parameterNames.add(group.get(0).getName());
        }
    }

    /**
     * Defers constraints reading an unassigned or absent parameter.
     *
     * @param items chosen items in any order
     * @return whether no constraint rejects the partial case
     */
    boolean isValidPartialCase(List<Item> items) {
        if (this.constraints.isEmpty()) {
            return true;
        }
        final ConstrainableCase candidate = new ConstrainableCase(items);
        for (Predicate<ConstrainableCase> constraint : this.constraints) {
            try {
                if (constraint.test(candidate)) {
                    return false;
                }
            } catch (ConstrainableCase.NoSuchParameterNameException ignored) {
                validateMissingParameter(candidate);
            } catch (ConstrainableCase.InapplicableParameterException ignored) {
                // An absent value makes this constraint inapplicable.
            }
        }
        return true;
    }

    List<Item> complete(List<Item> seed) {
        return complete(seed, CheckBudget.unlimited());
    }

    /**
     * Keeps the seed's items, including chosen absence, and fills the remaining parameters.
     * Only optimization attempts may use a limited budget; required coverage must use unlimited search.
     *
     * @param seed fixed items in any order
     * @param budget shared allowance for constraint checks
     * @return a complete case ordered by parameter index, or {@code null} if search fails or exhausts its budget
     */
    List<Item> complete(List<Item> seed, CheckBudget budget) {
        if (budget.isExhausted()) {
            return null;
        }
        final Item[] ordered = new Item[this.itemMatrix.size()];
        for (Item item : seed) {
            ordered[item.getParameterIndex()] = item;
        }
        // Unlike the result, assigned follows search order and contains no unassigned slots.
        final List<Item> assigned = new ArrayList<>();
        final List<List<Item>> remaining = new ArrayList<>();
        for (int parameter = 0; parameter < this.itemMatrix.size(); parameter++) {
            final List<Item> group = this.itemMatrix.get(parameter);
            if (ordered[parameter] != null) {
                assigned.add(ordered[parameter]);
            } else if (group.size() == 1 || this.constraints.isEmpty()) {
                assigned.add(group.get(0));
            } else {
                remaining.add(group);
            }
        }
        if (!completeRemaining(assigned, remaining, budget)) {
            return null;
        }
        for (Item item : assigned) {
            ordered[item.getParameterIndex()] = item;
        }
        return new ArrayList<>(Arrays.asList(ordered));
    }

    private boolean completeRemaining(List<Item> assigned, List<List<Item>> remaining, CheckBudget budget) {
        final ConstrainableCase partialCase = new ConstrainableCase(assigned);
        int requested = -1;
        for (Predicate<ConstrainableCase> constraint : this.constraints) {
            if (!budget.tryConsume()) {
                return false;
            }
            try {
                if (constraint.test(partialCase)) {
                    return false;
                }
            } catch (ConstrainableCase.NoSuchParameterNameException ignored) {
                validateMissingParameter(partialCase);
                if (requested < 0) {
                    for (int i = 0; i < remaining.size(); i++) {
                        if (remaining.get(i).get(0).getName().equals(partialCase.getMissingParameterName())) {
                            requested = i;
                            break;
                        }
                    }
                }
            } catch (ConstrainableCase.InapplicableParameterException ignored) {
                // An assigned absent value cannot become present later in this completion branch.
            }
        }
        if (requested < 0) {
            // Pure predicates have resolved; parameters they did not read cannot change their results.
            for (List<Item> group : remaining) {
                assigned.add(group.get(0));
            }
            return true;
        }
        final List<Item> group = remaining.remove(requested);
        for (Item item : group) {
            assigned.add(item);
            if (completeRemaining(assigned, remaining, budget)) {
                remaining.add(requested, group);
                return true;
            }
            assigned.remove(assigned.size() - 1);
            if (budget.isExhausted()) {
                break;
            }
        }
        remaining.add(requested, group);
        return false;
    }

    private void validateMissingParameter(ConstrainableCase candidate) {
        final String name = candidate.getMissingParameterName();
        if (!this.parameterNames.contains(name)) {
            throw new IllegalArgumentException("Unknown parameter in case constraint: " + name);
        }
    }

    /** A shared constraint-check allowance for a series of optimization attempts. */
    static final class CheckBudget {

        private final boolean limited;
        private int remainingChecks;

        CheckBudget(int checks) {
            this.limited = true;
            this.remainingChecks = checks;
        }

        private CheckBudget() {
            this.limited = false;
        }

        static CheckBudget unlimited() {
            return new CheckBudget();
        }

        int getRemainingChecks() {
            return this.remainingChecks;
        }

        boolean isExhausted() {
            return this.limited && this.remainingChecks <= 0;
        }

        boolean tryConsume() {
            if (isExhausted()) {
                return false;
            }
            if (this.limited) {
                this.remainingChecks--;
            }
            return true;
        }
    }
}

