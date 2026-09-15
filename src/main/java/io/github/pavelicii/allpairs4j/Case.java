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

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * One test case, stored as a {@link LinkedHashMap} from parameter names to chosen values.
 * Generated cases leave out absent parameters. A present parameter with a {@code null} value keeps its key.
 */
public class Case extends LinkedHashMap<String, Object> {

    public Case() {
        super();
    }

    public Case(Map<String, ?> map) {
        super(map);
    }

    /**
     * Creates a case from alternating parameter names and values.
     *
     * @param input name-value pairs; names must be strings, and the number of arguments must be even
     * @throws IllegalArgumentException if the number of arguments is odd
     */
    public Case(Object... input) {
        if ((input.length & 1) != 0) {
            throw new IllegalArgumentException("Input length is odd. Unable to create map");
        }
        for (int i = 0; i < input.length; i += 2) {
            super.put((String) input[i], input[i + 1]);
        }
    }

    Case(List<Item> items) {
        items.stream().filter(Item::isPresent).forEach(item -> {
            if (super.containsKey(item.getName())) {
                throw new IllegalStateException("Duplicate key: " + item.getName());
            }
            super.put(item.getName(), item.getValue());
        });
    }
}
