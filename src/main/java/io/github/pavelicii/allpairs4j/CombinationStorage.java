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

import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

class CombinationStorage {

    private final int n;
    private final Map<String, Node> nodes;
    private final List<Set<List<String>>> itemIdCombinations;

    CombinationStorage(int n) {
        this.n = n;
        nodes = new LinkedHashMap<>();
        itemIdCombinations = Stream.generate(() -> new HashSet<List<String>>())
                .limit(n)
                .collect(Collectors.toList());
    }

    List<Set<List<String>>> getItemIdCombinations() {
        return this.itemIdCombinations;
    }

    int getLength() {
        return itemIdCombinations.get(itemIdCombinations.size() - 1).size();
    }

    Node getNodeOrCreateNew(Item item) {
        return nodes.getOrDefault(item.getItemId(), new Node(item.getItemId()));
    }

    void addSequenceCombinations(List<Item> sequence) {
        for (int i = 1; i < n + 1; i++) {
            Itertools.combinations(sequence, i).forEach(this::addCombination);
        }
    }

    private void addCombination(List<Item> combination) {
        final int combinationSize = combination.size();

        if (combinationSize == 0) {
            throw new RuntimeException("Combination is empty");
        }

        itemIdCombinations
                .get(combinationSize - 1)
                .add(combination.stream().map(Item::getItemId).collect(Collectors.toList()));

        if (combinationSize == 1 && !nodes.containsKey(combination.get(0).getItemId())) {
            nodes.put(combination.get(0).getItemId(), new Node(combination.get(0).getItemId()));
            return;
        }

        final List<String> itemIds = combination.stream()
                .map(Item::getItemId)
                .collect(Collectors.toList());

        for (int i = 0; i < itemIds.size(); i++) {
            final Node currentNode = nodes.get(itemIds.get(i));
            currentNode.increaseCounter();
            currentNode.addInboundItemIds(itemIds.subList(0, i));
            currentNode.addOutboundItemIds(itemIds.subList(i + 1, itemIds.size()));
        }
    }
}
