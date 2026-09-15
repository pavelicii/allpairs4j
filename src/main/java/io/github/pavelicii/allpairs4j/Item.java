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

/** Holds one parameter value or absence, its name and indices, and weights used to rank it during generation. */
class Item implements Comparable<Item> {

    private static final Object INAPPLICABLE = new Object();

    private final int parameterIndex;
    private final int valueIndex;
    private final int globalValueIndex;
    private final Object value;
    private final String name;
    private long[] weights = new long[0];

    Item(int parameterIndex, int valueIndex, int globalValueIndex, Object value, String name) {
        this.parameterIndex = parameterIndex;
        this.valueIndex = valueIndex;
        this.globalValueIndex = globalValueIndex;
        this.value = value;
        this.name = name;
    }

    static Item absent(int parameterIndex, int valueIndex, int globalValueIndex, String name) {
        return new Item(parameterIndex, valueIndex, globalValueIndex, INAPPLICABLE, name);
    }

    boolean isPresent() {
        return this.value != INAPPLICABLE;
    }

    int getParameterIndex() {
        return this.parameterIndex;
    }

    int getValueIndex() {
        return this.valueIndex;
    }

    int getGlobalValueIndex() {
        return this.globalValueIndex;
    }

    Object getValue() {
        return this.value;
    }

    String getName() {
        return this.name;
    }

    void setWeights(long[] weights) {
        this.weights = weights;
    }

    @Override
    public int compareTo(Item otherItem) {
        for (int i = 0; i < Math.min(this.weights.length, otherItem.weights.length); i++) {
            final int comparison = Long.compare(this.weights[i], otherItem.weights[i]);
            if (comparison != 0) {
                return comparison;
            }
        }
        return Integer.compare(this.weights.length, otherItem.weights.length);
    }
}
