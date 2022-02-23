/*
 * Copyright 2022 Pavel Nazimok - @pavelicii
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

/**
 * Class for constraint definition.
 * Provides limited interface to {@link Case} to be used when defining constraints.
 */
public class ConstrainableCase {

    // Defined as a constant to cause as little performance penalty as possible
    private static final NoSuchParameterNameException NO_SUCH_PARAMETER_NAME_EXCEPTION =
            new NoSuchParameterNameException();

    /** {@link Case} to test constraint against. Can be incomplete. */
    private final Case aCase;

    ConstrainableCase(Case aCase) {
        this.aCase = new Case(aCase);
    }

    /**
     * Get {@code Case} value mapped to {@code Parameter} name.
     * <p>
     * If {@link Case} contains no mapping to the {@code Parameter} name, throws {@link NoSuchParameterNameException}.
     * When used to define constraint, the Exception is handled internally in {@code AllPairs#isValidCase(List)}.
     * This could happen when {@code Case} is incomplete or when a user specified non-existent {@code Parameter} name.
     * The Exception is used as control flow to allow constraints to be defined as {@code Predicate}.
     *
     * @param parameterName {@link Parameter} name mapped to the value to return
     * @return value mapped to the {@code Parameter name}
     * @see AllPairs.AllPairsBuilder#withConstraint(java.util.function.Predicate)
     */
    public Object get(String parameterName) {
        if (!aCase.containsKey(parameterName)) {
            throw NO_SUCH_PARAMETER_NAME_EXCEPTION;
        }
        return aCase.get(parameterName);
    }

    /**
     * Exception to be used as control flow to test constraints.
     * <p>
     * Constructs new {@link RuntimeException} with fixed detail message, no cause, disabled suppression
     * and no stack trace to cause as little performance penalty as possible.
     */
    static final class NoSuchParameterNameException extends RuntimeException {

        private NoSuchParameterNameException() {
            super("Case doesn't contain provided key (Parameter#name)", null, false, false);
        }
    }
}
