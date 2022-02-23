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

import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

import static org.assertj.core.api.Assertions.assertThat;

class GenerationTest {

    @Test
    void shouldGenerateAllPairwiseCasesWithoutConstraints() {
        final AllPairs allPairs = new AllPairs.AllPairsBuilder()
                .withParameters(TestData.PARAMETERS)
                .build();

        assertThat(allPairs.getGeneratedCases()).containsExactlyElementsOf(TestData.EXPECTED_PAIRWISE_CASES);
        assertAllCombinationsAreGenerated(allPairs);
    }

    @Test
    void shouldGenerateFilteredPairwiseCasesWithConstraints() {
        final AllPairs allPairs = new AllPairs.AllPairsBuilder()
                .withParameters(TestData.PARAMETERS)
                .withConstraints(Arrays.asList(
                        c -> c.get("Browser").equals("Safari") && !c.get("OS").equals("macOS"),
                        c -> c.get("Browser").equals("Edge") && !c.get("OS").equals("Windows")))
                .build();

        assertThat(allPairs.getGeneratedCases()).containsExactlyElementsOf(TestData.EXPECTED_FILTERED_PAIRWISE_CASES);
        assertAllCombinationsAreGenerated(allPairs);
    }

    @Test
    void shouldGenerateAllTriplewiseCasesWithoutConstraints() {
        final AllPairs allPairs = new AllPairs.AllPairsBuilder()
                .withParameters(TestData.PARAMETERS)
                .withTestCombinationSize(3)
                .build();

        assertThat(allPairs.getGeneratedCases().size()).isEqualTo(48);
        assertAllCombinationsAreGenerated(allPairs);
    }

    @Test
    void shouldGenerateFilteredTriplewiseCasesWithConstraints() {
        final AllPairs allPairs = new AllPairs.AllPairsBuilder()
                .withParameters(TestData.PARAMETERS)
                .withConstraints(Arrays.asList(
                        c -> c.get("Browser").equals("Safari") && !c.get("OS").equals("macOS"),
                        c -> c.get("Browser").equals("Edge") && !c.get("OS").equals("Windows")))
                .withTestCombinationSize(3)
                .build();

        assertThat(allPairs.getGeneratedCases().size()).isEqualTo(32);
        assertAllCombinationsAreGenerated(allPairs);
    }

    @Test
    void shouldGenerateZeroCasesWithParameterExcludingConstraints() {
        final AllPairs allPairs = new AllPairs.AllPairsBuilder()
                .withParameters(TestData.PARAMETERS)
                .withConstraint(c -> c.get("Drive").equals("HDD"))
                .withConstraint(c -> c.get("Drive").equals("SSD"))
                .build();

        assertThat(allPairs.getGeneratedCases().size()).isEqualTo(0);
    }

    @Test
    void shouldGenerateAllPairwiseCasesWithUnrelatedConstraints() {
        final AllPairs allPairs = new AllPairs.AllPairsBuilder()
                .withParameters(TestData.PARAMETERS)
                .withConstraint(c -> c.get("Non-existent Parameter #1").equals("Foo"))
                .withConstraint(c -> c.get("Non-existent Parameter #2").equals("Bar"))
                .build();

        assertThat(allPairs.getGeneratedCases()).containsExactlyElementsOf(TestData.EXPECTED_PAIRWISE_CASES);
        assertAllCombinationsAreGenerated(allPairs);
    }

    @Test
    void shouldGenerateAllPairwiseCasesWithEmptyConstraints() {
        final AllPairs allPairs = new AllPairs.AllPairsBuilder()
                .withParameters(TestData.PARAMETERS)
                .withConstraints(Collections.emptyList())
                .build();

        assertThat(allPairs.getGeneratedCases()).containsExactlyElementsOf(TestData.EXPECTED_PAIRWISE_CASES);
        assertAllCombinationsAreGenerated(allPairs);
    }

    private void assertAllCombinationsAreGenerated(AllPairs allPairs) {
        final int n = allPairs.getTestCombinationSize();

        final List<List<Case>> parametersAsCases = allPairs.getParameters().stream()
                .map(parameter -> parameter.stream()
                        .map(value -> new Case(parameter.getName(), value))
                        .collect(Collectors.toList()))
                .collect(Collectors.toList());
        final List<List<Case>> cases = allPairs.getGeneratedCases().stream()
                .map(c -> c.entrySet().stream()
                        .map(entry -> new Case(entry.getKey(), entry.getValue()))
                        .collect(Collectors.toList()))
                .collect(Collectors.toList());

        final List<Case> expectedCombinations = StreamSupport
                .stream(Itertools.combinations(parametersAsCases, n).spliterator(), false)
                .flatMap(itemsComb -> StreamSupport.stream(Itertools.product(itemsComb).spliterator(), false))
                .map(cs -> cs.stream()
                        .reduce(new Case(), (accumulator, c) -> {
                            accumulator.putAll(c);
                            return accumulator;
                        }))
                .filter(allPairs::isValidCase)
                .collect(Collectors.toList());
        final List<Case> actualCombinations = cases.stream()
                .flatMap(c -> StreamSupport.stream(Itertools.combinations(c, n).spliterator(), false))
                .distinct()
                .map(cs -> cs.stream()
                        .reduce(new Case(), (accumulator, c) -> {
                            accumulator.putAll(c);
                            return accumulator;
                        }))
                .collect(Collectors.toList());

        assertThat(actualCombinations).containsExactlyInAnyOrderElementsOf(expectedCombinations);
    }
}
