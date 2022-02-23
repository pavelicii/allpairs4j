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
import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;

/**
 * Analogue of Python's {@code itertools} with methods needed for AllPairs algorithm.
 */
final class Itertools {

    private Itertools() {
    }

    /**
     * Returns the {@code Iterator} over {@code r} length subsequences of elements from the input {@code List}.
     * <p>
     * Elements are treated as unique based on their position, not on their value.
     * So if the input elements are unique, there will be no repeat values in each combination.
     * <ul>
     *     <li>{@code combinations(Arrays.asList("A", "B", "C", "D"), 2)} --> AB AC AD BC BD CD
     *     <li>{@code combinations(Arrays.asList(0, 1, 2, 3), 3)} --> 012 013 023 123
     * </ul>
     *
     * @param elements the {@code List} of elements to create combinations from
     * @param r the length of each combination to return
     * @param <T> the type of elements in the {@code List}
     * @return the {@code Iterator} over combinations — the {@code List} of elements of type {@code T}
     */
    static <T> Iterable<List<T>> combinations(List<T> elements, int r) {
        return () -> new Iterator<List<T>>() {
            private int currentPosition = 0;
            private int[] currentCombination = new int[r];
            private final int totalCombinations = Math.toIntExact(calculateCombinationsNumber(elements.size(), r));
            private boolean hasNext = r <= elements.size() && r >= 1;

            @Override
            public boolean hasNext() {
                return hasNext;
            }

            @Override
            public List<T> next() {
                if (!hasNext()) {
                    throw new NoSuchElementException("No next combination found");
                }
                if (currentPosition == 0) {
                    for (int i = 0; i < currentCombination.length; i++) {
                        currentCombination[i] = i + 1;
                    }
                } else {
                    currentCombination = generateNextCombination(currentCombination, elements.size(), r);
                }
                final List<T> result = new ArrayList<>();
                for (int i : currentCombination) {
                    result.add(elements.get(i - 1));
                }
                currentPosition++;
                if (currentPosition >= totalCombinations) {
                    hasNext = false;
                }
                return result;
            }
        };
    }

    /**
     * Returns the {@code Iterator} over Cartesian product of elements from the input {@code List}s.
     *
     * @param elementLists the {@code List} of element {@code List}s to create Cartesian product from
     * @param <T> the type of elements in the {@code List}s
     * @return the {@code Iterator} over products — the {@code List} of elements of type {@code T}
     */
    static <T> Iterable<List<T>> product(List<List<T>> elementLists) {
        int total = 1;
        final int[] max = new int[elementLists.size()];
        for (int i = 0; i < elementLists.size(); i++) {
            max[i] = elementLists.get(i).size();
        }
        final int[] initProduct = new int[elementLists.size()];
        Arrays.fill(initProduct, 1);
        for (List<T> list : elementLists) {
            total *= list.size();
        }
        final int totalProducts = total;
        return () -> new Iterator<List<T>>() {
            private int currentPosition = 0;
            private int[] currentProduct;

            @Override
            public boolean hasNext() {
                return currentPosition < totalProducts;
            }

            @Override
            public List<T> next() {
                if (!hasNext()) {
                    throw new NoSuchElementException("No next product found");
                }
                if (currentPosition == 0) {
                    currentProduct = initProduct;
                } else {
                    currentProduct = generateNextProduct(currentProduct, max);
                }
                final List<T> result = new ArrayList<>();
                for (int i = 0; i < currentProduct.length; i++) {
                    result.add(elementLists.get(i).get(currentProduct[i] - 1));
                }
                currentPosition++;
                return result;
            }
        };
    }

    private static long calculateCombinationsNumber(int n, int r) {
        int nn = n;
        int rr = r;
        long nCr = 1;
        rr = Math.min(rr, nn - rr);
        for (int i = 1; i <= rr; i++, nn--) {
            if (nn % i == 0) {
                nCr = Math.multiplyExact(nCr, nn / i);
            } else if (nCr % i == 0) {
                nCr = Math.multiplyExact(nCr / i, nn);
            } else {
                nCr = Math.multiplyExact(nCr, nn / i);
            }
        }
        return nCr;
    }

    private static int[] generateNextCombination(int[] currentCombination, int n, int r) {
        final int[] nextCombination = Arrays.copyOf(currentCombination, currentCombination.length);
        int nn = n;
        int rr = r;
        while (nextCombination[rr - 1] == nn) {
            nn--;
            rr--;
        }
        nextCombination[rr - 1]++;
        for (int i = rr; i < r; i++) {
            nextCombination[i] = nextCombination[i - 1] + 1;
        }
        return nextCombination;
    }

    private static int[] generateNextProduct(int[] currentProduct, int[] max) {
        final int[] nextProduct = Arrays.copyOf(currentProduct, currentProduct.length);
        final int n = nextProduct.length - 1;
        nextProduct[n]++;
        for (int i = n; i > 0; i--) {
            if (nextProduct[i] > max[i]) {
                nextProduct[i] = 1;
                nextProduct[i - 1]++;
            }
        }
        return nextProduct;
    }
}
