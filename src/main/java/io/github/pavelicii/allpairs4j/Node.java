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
        this.counter = 0;
        this.inboundItemIds = new HashSet<>();
        this.outboundItemIds = new HashSet<>();
    }

    @Override
    public String toString() {
        return this.nodeId;
    }

    void increaseCounter() {
        this.counter++;
    }

    int getCounter() {
        return this.counter;
    }

    int getInboundItemIdsSize() {
        return this.inboundItemIds.size();
    }

    int getOutboundItemIdsSize() {
        return this.outboundItemIds.size();
    }

    void addInboundItemIds(Collection<String> itemIds) {
        this.inboundItemIds.addAll(itemIds);
    }

    void addOutboundItemIds(Collection<String> itemIds) {
        this.outboundItemIds.addAll(itemIds);
    }
}
