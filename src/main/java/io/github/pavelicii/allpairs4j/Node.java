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

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

class Node {

    private final String nodeId;
    private int counter;
    private final Set<String> inboundItemIds;
    private final Set<String> outboundItemIds;

    Node(String nodeId) {
        this.nodeId = nodeId;
        counter = 0;
        inboundItemIds = new HashSet<>();
        outboundItemIds = new HashSet<>();
    }

    void increaseCounter() {
        counter++;
    }

    int getCounter() {
        return counter;
    }

    int getInboundItemIdsSize() {
        return inboundItemIds.size();
    }

    int getOutboundItemIdsSize() {
        return outboundItemIds.size();
    }

    void addInboundItemIds(Collection<String> itemIds) {
        inboundItemIds.addAll(itemIds);
    }

    void addOutboundItemIds(Collection<String> itemIds) {
        outboundItemIds.addAll(itemIds);
    }
}
