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
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;

/**
 * Holds single {@link Parameter} value with its name, unified ID and weights needed for AllPairs algorithm.
 */
class Item implements Comparable<Item> {

    private static final Comparator<List<Integer>> INTEGER_LIST_LEXICOGRAPHICAL_COMPARATOR = (o1, o2) -> {
        final Iterator<Integer> i1 = o1.iterator();
        final Iterator<Integer> i2 = o2.iterator();
        int result;

        do {
            if (!i1.hasNext()) {
                return !i2.hasNext() ? 0 : -1;
            }
            if (!i2.hasNext()) {
                return 1;
            }
            result = i1.next().compareTo(i2.next());
        } while (result == 0);

        return result;
    };

    private final String itemId;
    private final Object value;
    private final String name;
    private List<Integer> weights = new ArrayList<>();

    Item(String itemId, Object value, String name) {
        this.itemId = itemId;
        this.value = value;
        this.name = name;
    }

    String getItemId() {
        return itemId;
    }

    Object getValue() {
        return value;
    }

    String getName() {
        return name;
    }

    List<Integer> getWeights() {
        return weights;
    }

    void setWeights(List<Integer> weights) {
        this.weights = weights;
    }

    @Override
    public int compareTo(Item otherItem) {
        return INTEGER_LIST_LEXICOGRAPHICAL_COMPARATOR.compare(getWeights(), otherItem.getWeights());
    }
}
