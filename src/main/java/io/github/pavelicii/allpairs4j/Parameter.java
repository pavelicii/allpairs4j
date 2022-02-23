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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * An {@link ArrayList} with {@link Parameter#name} containing possible parameter values.
 *
 * @see AllPairs.AllPairsBuilder#withParameter(Parameter)
 */
public class Parameter extends ArrayList<Object> {

    private final String name;

    /**
     * Constructs {@code Parameter} with specified name and {@code List} of possible values.
     *
     * @param name {@code Parameter} name
     * @param values {@code List} of possible {@code Parameter} values
     */
    public Parameter(String name, List<?> values) {
        super(values);
        this.name = name;
    }

    /**
     * Constructs {@code Parameter} with specified name and possible values.
     *
     * @param name {@code Parameter} name
     * @param values possible {@code Parameter} values
     */
    public Parameter(String name, Object... values) {
        this(name, Arrays.asList(values));
    }

    public String getName() {
        return name;
    }

    @Override
    public String toString() {
        return name + ": " + super.toString();
    }
}
