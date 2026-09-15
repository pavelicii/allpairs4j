/*
 * Copyright 2026 Pavel Nazimok - @pavelicii
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
import java.util.BitSet;
import java.util.Collections;
import java.util.List;

/**
 * Tracks coverage in groups of parameter subsets ordered by their indices.
 * Within each group, mixed-radix indexing gives each value combination a slot without gaps.
 */
class CompactTable {

    private static final int MAX_CAPACITY = Integer.MAX_VALUE - 8;
    private static final int MAX_PROJECTION_VISITS = 100_000;

    private final int[] domainSizes;
    private final int strength;
    private final int[][] binomial;
    private final Group[] groups;
    private Group[][] incidentGroups;
    private int coveredCount;
    private int feasibleCount;
    private int requiredCount;

    CompactTable(int[] domainSizes, int strength, boolean constrained, int[] ignoredValues) {
        final int[] capacities = checkedCapacities(domainSizes, strength);
        this.domainSizes = domainSizes.clone();
        this.strength = strength;
        this.binomial = new int[strength + 1][domainSizes.length + 1];
        Arrays.fill(this.binomial[0], 1);
        for (int size = 1; size <= strength; size++) {
            for (int count = size; count <= domainSizes.length; count++) {
                this.binomial[size][count] = Math.addExact(this.binomial[size][count - 1], this.binomial[size - 1][count - 1]);
            }
        }
        this.groups = new Group[this.binomial[strength][domainSizes.length]];
        final int[] parameters = new int[strength];
        for (int i = 0; i < strength; i++) {
            parameters[i] = i;
        }
        for (int i = 0; i < this.groups.length; i++) {
            this.groups[i] = new Group(parameters, this.domainSizes, constrained, ignoredValues);
            this.requiredCount += this.groups[i].uncoveredCount;
            advance(parameters, domainSizes.length);
        }
        this.feasibleCount = constrained ? 0 : capacities[strength];
    }

    static int[] checkedCapacities(int[] domainSizes, int strength) {
        if (strength < 1 || strength > domainSizes.length) {
            throw new IllegalArgumentException("Unsupported combination strength: " + strength);
        }
        final int[] capacities = new int[strength + 1];
        capacities[0] = 1;
        int processed = 0;
        for (int size : domainSizes) {
            if (size < 1) {
                throw new IllegalArgumentException("Parameter domains must be nonempty");
            }
            processed++;
            for (int i = Math.min(processed, strength); i > 0; i--) {
                final long capacity = capacities[i] + (long) size * capacities[i - 1];
                if (capacity > MAX_CAPACITY) {
                    throw new IllegalArgumentException("Combination capacity exceeds supported integer indexing at strength " + i);
                }
                capacities[i] = (int) capacity;
            }
        }
        return capacities;
    }

    private static boolean advance(int[] indices, int size) {
        for (int i = indices.length - 1; i >= 0; i--) {
            if (indices[i] < size - indices.length + i) {
                indices[i]++;
                for (int j = i + 1; j < indices.length; j++) {
                    indices[j] = indices[j - 1] + 1;
                }
                return true;
            }
        }
        return false;
    }

    Group[] getGroups() {
        return this.groups;
    }

    int getRequiredCount() {
        return this.requiredCount;
    }

    int getCoveredCount() {
        return this.coveredCount;
    }

    // Called before generation, in ascending strength order, while the larger table is not yet reduced.
    void removeImpliedTargets(CompactTable larger) {
        for (Group extension : larger.groups) {
            int stride = 1;
            for (int omitted = extension.parameters.length - 1; omitted >= 0; omitted--) {
                final Group projection = groupWithout(extension.parameters, omitted);
                final int block = stride * this.domainSizes[extension.parameters[omitted]];
                for (int index = extension.nextRequiredIndex(0); index >= 0; index = extension.nextRequiredIndex(index + 1)) {
                    final int projected = index / block * stride + index % stride;
                    if (projection.isRequired(projected)) {
                        if (projection.implied == null) {
                            projection.implied = new BitSet();
                        }
                        projection.implied.set(projected);
                        projection.uncoveredCount--;
                        this.requiredCount--;
                    }
                }
                stride = block;
            }
        }
    }

    private Group groupWithout(int[] parameters, int omitted) {
        int rank = this.groups.length - 1;
        int position = 0;
        for (int i = 0; i < parameters.length; i++) {
            if (i != omitted) {
                rank -= this.binomial[this.strength - position][this.domainSizes.length - parameters[i] - 1];
                position++;
            }
        }
        return this.groups[rank];
    }

    void cover(Group group, int index) {
        if (!group.isFeasible(index)) {
            throw new IllegalStateException("Cannot cover a combination without a feasible completion");
        }
        if (!group.isCoverageTarget(index)) {
            return;
        }
        if (!group.covered.get(index)) {
            group.covered.set(index);
            group.uncoveredCount--;
            this.coveredCount++;
        }
    }

    void markFeasible(Group group, int index) {
        if (group.excluded != null && group.excluded.get(index)) {
            throw new IllegalStateException("Previously excluded combination has a feasible completion");
        }
        if (!group.isFeasible(index)) {
            group.feasible.set(index);
            this.feasibleCount++;
            if (group.isCoverageTarget(index)) {
                group.uncoveredCount++;
                this.requiredCount++;
            }
        }
    }

    void exclude(Group group, int index) {
        if (group.isFeasible(index)) {
            throw new IllegalStateException("Previously feasible combination has no valid completion");
        }
        group.excluded.set(index);
    }

    void excludeAll() {
        if (this.feasibleCount != 0) {
            throw new IllegalStateException("Cannot exclude feasible combinations");
        }
        for (Group group : this.groups) {
            group.excluded.set(0, group.capacity);
        }
    }

    int countNewCombinations(List<Item> prefix, Item candidate) {
        if (this.strength == 1) {
            final Group group = this.groups[candidate.getParameterIndex()];
            return group.isCoverageTarget(candidate.getValueIndex()) && !group.isCovered(candidate.getValueIndex()) ? 1 : 0;
        }
        if (this.strength == 2) {
            return countNewPairs(prefix, candidate);
        }
        if (prefix.size() < this.strength - 1) {
            return 0;
        }
        final int[] indices = new int[this.strength - 1];
        for (int i = 0; i < indices.length; i++) {
            indices[i] = i;
        }
        int count = 0;
        do {
            int rank = this.groups.length - 1;
            int index = 0;
            for (int i = 0; i < indices.length; i++) {
                final Item item = prefix.get(indices[i]);
                rank -= this.binomial[this.strength - i][this.domainSizes.length - item.getParameterIndex() - 1];
                index = index * this.domainSizes[item.getParameterIndex()] + item.getValueIndex();
            }
            rank -= this.domainSizes.length - candidate.getParameterIndex() - 1;
            index = index * this.domainSizes[candidate.getParameterIndex()] + candidate.getValueIndex();
            if (this.groups[rank].isCoverageTarget(index) && !this.groups[rank].isCovered(index)) {
                count++;
            }
        } while (advance(indices, prefix.size()));
        return count;
    }

    private int countNewPairs(List<Item> prefix, Item candidate) {
        int count = 0;
        final int right = candidate.getParameterIndex();
        for (final Item item : prefix) {
            final int left = item.getParameterIndex();
            final int rank = (int) ((long) left * (2L * this.domainSizes.length - left - 1) / 2) + right - left - 1;
            final int index = item.getValueIndex() * this.domainSizes[right] + candidate.getValueIndex();
            if (this.groups[rank].isCoverageTarget(index) && !this.groups[rank].isCovered(index)) {
                count++;
            }
        }
        return count;
    }

    long getVisitsPerItem() {
        return this.binomial[this.strength - 1][this.domainSizes.length - 1];
    }

    private Group[] incidentGroups(int parameter) {
        if (this.incidentGroups == null) {
            this.incidentGroups = new Group[this.domainSizes.length][(int) getVisitsPerItem()];
            final int[] counts = new int[this.domainSizes.length];
            for (Group group : this.groups) {
                for (int index : group.parameters) {
                    this.incidentGroups[index][counts[index]++] = group;
                }
            }
        }
        return this.incidentGroups[parameter];
    }

    // Estimate work for every candidate together, so all candidates use the same scoring rule.
    boolean canScoreProjections(Item[] assigned, int parameter) {
        final long limit = MAX_PROJECTION_VISITS / this.domainSizes[parameter];
        if (getVisitsPerItem() > limit) {
            return false;
        }
        long visits = 0;
        for (Group group : incidentGroups(parameter)) {
            long product = 1;
            for (int index : group.parameters) {
                if (index != parameter && assigned[index] == null) {
                    if (product > limit / this.domainSizes[index]) {
                        return false;
                    }
                    product *= this.domainSizes[index];
                }
            }
            visits += product;
            if (visits > limit) {
                return false;
            }
        }
        return true;
    }

    /**
     * Counts uncovered combinations that the candidate completes or could still help cover.
     * Only combinations involving the candidate's parameter are counted.
     * A {@code null} slot means no item has been chosen yet. Absence and a present {@code null} value
     * each have their own non-null {@link Item}.
     * Temporarily puts the candidate in {@code assigned}, then restores the previous item.
     *
     * @param assigned chosen items, including any fixed values for later parameters
     * @param candidate item to score
     * @return newly completed combinations and all compatible uncovered combinations, including completed ones
     */
    ProjectionScore scoreProjections(Item[] assigned, Item candidate) {
        int completedCount = 0;
        int compatibleCount = 0;
        final int parameter = candidate.getParameterIndex();
        final Item previous = assigned[parameter];
        assigned[parameter] = candidate;
        try {
            for (Group group : incidentGroups(parameter)) {
                if (group.uncoveredCount == 0) {
                    continue;
                }
                final int compatible = group.countProjection(assigned, 0, 0);
                compatibleCount += compatible;
                boolean complete = true;
                for (int index : group.parameters) {
                    if (assigned[index] == null) {
                        complete = false;
                        break;
                    }
                }
                if (complete && compatible > 0) {
                    completedCount++;
                }
            }
        } finally {
            assigned[parameter] = previous;
        }
        return new ProjectionScore(completedCount, compatibleCount);
    }

    static final class ProjectionScore {

        private final int completedCount;
        private final int compatibleCount;

        private ProjectionScore(int completedCount, int compatibleCount) {
            this.completedCount = completedCount;
            this.compatibleCount = compatibleCount;
        }

        int getCompletedCount() {
            return this.completedCount;
        }

        int getCompatibleCount() {
            return this.compatibleCount;
        }
    }

    static final class Group {

        private final int[] parameters;
        private final int[] domainSizes;
        private final int capacity;
        private final BitSet covered;
        private final BitSet feasible;
        private final BitSet excluded;
        private final int[] ignoredValues;
        private BitSet implied;
        private int uncoveredCount;

        private Group(int[] parameters, int[] domainSizes, boolean constrained, int[] ignoredValues) {
            this.parameters = parameters.clone();
            this.domainSizes = domainSizes;
            this.ignoredValues = ignoredValues;
            int product = 1;
            int targets = 1;
            for (int parameter : parameters) {
                product = Math.multiplyExact(product, domainSizes[parameter]);
                targets *= domainSizes[parameter] - (ignoredValues != null && ignoredValues[parameter] >= 0 ? 1 : 0);
            }
            this.capacity = product;
            this.covered = new BitSet(product);
            this.feasible = constrained ? new BitSet(product) : null;
            this.excluded = constrained ? new BitSet(product) : null;
            this.uncoveredCount = constrained ? 0 : targets;
        }

        int getUncoveredCount() {
            return this.uncoveredCount;
        }

        int getCapacity() {
            return this.capacity;
        }

        int[] getParameters() {
            return this.parameters;
        }

        boolean isCovered(int index) {
            return this.covered.get(index);
        }

        boolean isFeasible(int index) {
            return this.feasible == null || this.feasible.get(index);
        }

        boolean isCoverageTarget(int index) {
            if (this.implied != null && this.implied.get(index)) {
                return false;
            }
            if (this.ignoredValues == null) {
                return true;
            }
            int remaining = index;
            for (int i = this.parameters.length - 1; i >= 0; i--) {
                final int parameter = this.parameters[i];
                if (remaining % this.domainSizes[parameter] == this.ignoredValues[parameter]) {
                    return false;
                }
                remaining /= this.domainSizes[parameter];
            }
            return true;
        }

        boolean isRequired(int index) {
            return isFeasible(index) && isCoverageTarget(index);
        }

        int nextFeasibleIndex(int fromIndex) {
            final int index = this.feasible == null ? fromIndex : this.feasible.nextSetBit(fromIndex);
            return index < this.capacity ? index : -1;
        }

        int nextRequiredIndex(int fromIndex) {
            int index = nextFeasibleIndex(fromIndex);
            while (index >= 0 && !isCoverageTarget(index)) {
                index = nextFeasibleIndex(index + 1);
            }
            return index;
        }

        boolean isClassified(int index) {
            return isFeasible(index) || this.excluded.get(index);
        }

        int firstUncovered() {
            int index = this.covered.nextClearBit(0);
            while (index < this.capacity) {
                if (isRequired(index)) {
                    return index;
                }
                index = nextRequiredIndex(index + 1);
                if (index < 0) {
                    return -1;
                }
                index = this.covered.nextClearBit(index);
            }
            return -1;
        }

        // Requires a complete case: position equals parameter index, including explicit absent items.
        int indexOf(List<Item> sequence) {
            int index = 0;
            for (int parameter : this.parameters) {
                index = index * this.domainSizes[parameter] + sequence.get(parameter).getValueIndex();
            }
            return index;
        }

        private int countProjection(Item[] assigned, int position, int index) {
            if (position == this.parameters.length) {
                return isRequired(index) && !isCovered(index) ? 1 : 0;
            }
            final int parameter = this.parameters[position];
            final int base = index * this.domainSizes[parameter];
            final Item item = assigned[parameter];
            if (item != null) {
                return countProjection(assigned, position + 1, base + item.getValueIndex());
            }
            int count = 0;
            for (int value = 0; value < this.domainSizes[parameter]; value++) {
                count += countProjection(assigned, position + 1, base + value);
            }
            return count;
        }

        // Absent states have canonical domain indices just like present values.
        List<Item> decode(int index, Item[][] canonicalItems) {
            if (index < 0 || index >= this.capacity) {
                throw new IndexOutOfBoundsException("Tuple index: " + index);
            }
            final List<Item> items = new ArrayList<>(this.parameters.length);
            int remainder = index;
            for (int i = this.parameters.length - 1; i >= 0; i--) {
                final int parameter = this.parameters[i];
                items.add(canonicalItems[parameter][remainder % this.domainSizes[parameter]]);
                remainder /= this.domainSizes[parameter];
            }
            Collections.reverse(items);
            return items;
        }
    }
}
