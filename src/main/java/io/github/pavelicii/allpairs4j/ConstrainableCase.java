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

import java.util.List;

/** Lets case and parameter constraints check a case's values and which parameters are present. */
public class ConstrainableCase {

    // Defined as a constant to cause as little performance penalty as possible
    private static final NoSuchParameterNameException NO_SUCH_PARAMETER_NAME_EXCEPTION =
            new NoSuchParameterNameException();
    private static final InapplicableParameterException INAPPLICABLE_PARAMETER_EXCEPTION =
            new InapplicableParameterException();

    /** Values chosen for the case being checked. Some parameters may not have a value yet. */
    private final List<Item> items;
    private String missingParameterName;

    ConstrainableCase(List<Item> items) {
        this.items = items;
    }

    /**
     * Returns a parameter's value, including {@code null} if it is one of the declared values.
     * Reading an absent value skips the current case constraint or excludes the parameter
     * whose constraint is being checked.
     * If the value has not been chosen yet, the generator chooses it before finishing the check.
     *
     * @param parameterName name of the parameter to read
     * @return the parameter's value
     * @see AllPairs.AllPairsBuilder#withConstraint(java.util.function.Predicate)
     * @see Parameter#withConstraint(java.util.function.Predicate)
     */
    public Object get(String parameterName) {
        final Item item = findItem(parameterName);
        if (!item.isPresent()) {
            throw INAPPLICABLE_PARAMETER_EXCEPTION;
        }
        return item.getValue();
    }

    /**
     * Checks whether a parameter is present without reading its value.
     * If its presence has not been decided yet, the generator decides it before finishing the check.
     *
     * @param parameterName name of the parameter to check
     * @return {@code true} if present, even when its value is {@code null}; {@code false} if absent
     */
    public boolean isPresent(String parameterName) {
        return findItem(parameterName).isPresent();
    }

    private Item findItem(String parameterName) {
        for (Item item : this.items) {
            if (item.getName().equals(parameterName)) {
                return item;
            }
        }
        this.missingParameterName = parameterName;
        throw NO_SUCH_PARAMETER_NAME_EXCEPTION;
    }

    String getMissingParameterName() {
        return this.missingParameterName;
    }

    /** Tells the generator that a constraint tried to read a parameter already chosen to be absent. */
    static final class InapplicableParameterException extends RuntimeException {

        private InapplicableParameterException() {
            super("Parameter is inapplicable", null, false, false);
        }
    }

    /**
     * Tells the generator that a constraint read an unknown parameter or one not yet assigned.
     * Uses a fixed message, no cause, no suppressed exceptions, and no stack trace to keep these checks cheap.
     */
    static final class NoSuchParameterNameException extends RuntimeException {

        private NoSuchParameterNameException() {
            super("Case doesn't contain provided key (Parameter#name)", null, false, false);
        }
    }
}
