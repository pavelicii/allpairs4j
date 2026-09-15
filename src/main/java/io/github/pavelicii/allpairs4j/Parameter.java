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
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.function.Predicate;

/**
 * A named list of possible values for one test parameter.
 *
 * @see AllPairs.AllPairsBuilder#withParameter(Parameter)
 */
public class Parameter extends ArrayList<Object> {

    /** Add this to the values to allow an absent parameter. A {@code null} value still counts as present. */
    public static final Object ABSENT = Absence.VALUE;

    private enum Absence {

        VALUE;

        @Override
        public String toString() {
            return "<absent>";
        }
    }

    private final String name;
    private Predicate<ConstrainableCase> parameterConstraint;

    /**
     * Creates a parameter from a name and a list of possible values.
     *
     * @param name parameter name
     * @param values possible values
     */
    public Parameter(String name, List<?> values) {
        super(values);
        this.name = name;
    }

    /**
     * Creates a parameter from a name and possible values.
     *
     * @param name parameter name
     * @param values possible values
     */
    public Parameter(String name, Object... values) {
        this(name, Arrays.asList(values));
    }

    Parameter(Parameter source) {
        this(source.name, source);
        this.parameterConstraint = source.parameterConstraint;
    }

    /**
     * Excludes this parameter when the constraint returns {@code true}; otherwise, allows its declared values.
     * Replaces any constraint previously set on this parameter.
     * <p>
     * Reading an absent parameter's value excludes this parameter.
     * If a value has not been chosen yet, the generator chooses it before finishing the check.
     * Reading an unknown parameter name causes an error. Parameters may be added in any order.
     * <p>
     * When the constraint returns {@code false}, presence is required unless the values include {@link #ABSENT}.
     * An explicit {@code ABSENT} allows absence even when the parameter is applicable.
     * When the constraint returns {@code true}, only absence is allowed, with or without an explicit marker.
     * Combinations with absent parameters are covered by default;
     * see {@link AllPairs.AllPairsBuilder#withAbsenceCoverage(boolean)}.
     * Constraints must only check values and must not catch exceptions from those checks.
     * Errors identify this parameter. Unknown names cause {@link IllegalArgumentException};
     * other runtime exceptions from the predicate are wrapped in it with the original exception as the cause.
     *
     * @param parameterConstraint non-null predicate that returns {@code true} when the parameter must be absent
     * @return this parameter
     * @see AllPairs.AllPairsBuilder#withParameter(Parameter)
     * @see AllPairs.AllPairsBuilder#withParameters(List)
     * @see AllPairs.AllPairsBuilder#withConstraint(Predicate)
     */
    public Parameter withConstraint(Predicate<ConstrainableCase> parameterConstraint) {
        this.parameterConstraint = Objects.requireNonNull(parameterConstraint, "Parameter constraint must be non-null");
        return this;
    }

    Predicate<ConstrainableCase> getConstraint() {
        return this.parameterConstraint;
    }

    public String getName() {
        return this.name;
    }

    @Override
    public String toString() {
        return this.name + ": " + super.toString();
    }
}
