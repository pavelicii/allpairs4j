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

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;

import java.util.Arrays;
import java.util.Collections;

import static org.assertj.core.api.Assertions.assertThat;

class GenerationTest {

    @Test
    void shouldGenerateAllPairwiseCasesWithoutConstraints() {
        final AllPairs allPairs = new AllPairs.AllPairsBuilder()
                .withParameters(TestData.PARAMETERS)
                .build();

        assertThat(allPairs.getGeneratedCases()).containsExactlyElementsOf(TestData.EXPECTED_PAIRWISE_CASES);
        assertAllTestCombinationsAreGenerated(allPairs);
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
        assertAllTestCombinationsAreGenerated(allPairs);
    }

    @Test
    void shouldGenerateAllTriplewiseCasesWithoutConstraints() {
        final AllPairs allPairs = new AllPairs.AllPairsBuilder()
                .withParameters(TestData.PARAMETERS)
                .withTestCombinationSize(3)
                .build();

        assertThat(allPairs.getGeneratedCases()).containsExactlyElementsOf(TestData.EXPECTED_TRIPLEWISE_CASES);
        assertAllTestCombinationsAreGenerated(allPairs);
    }

    @Test
    void shouldGenerateFilteredTriplewiseCasesWithConstraints() {
        final AllPairs allPairs = new AllPairs.AllPairsBuilder()
                .withParameters(TestData.PARAMETERS)
                .withConstraints(Arrays.asList(
                        c -> c.get("Browser").equals("Safari") && !c.get("OS").equals("macOS"),
                        c -> c.get("Browser").equals("Edge") && !c.get("OS").equals("Windows"),
                        c -> c.get("OS").equals("Linux") && c.get("Drive").equals("SSD") && (int) c.get("RAM") < 8000))
                .withTestCombinationSize(3)
                .build();

        assertThat(allPairs.getGeneratedCases()).containsExactlyElementsOf(TestData.EXPECTED_FILTERED_TRIPLEWISE_CASES);
        assertAllTestCombinationsAreGenerated(allPairs);
    }

    @Test
    void shouldGenerateZeroCasesWithConstraintsExcludingAllSingleParameterValues() {
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
                .withConstraint(c -> (int) c.get("Non-existent Parameter #2") < 3)
                .build();

        assertThat(allPairs.getGeneratedCases()).containsExactlyElementsOf(TestData.EXPECTED_PAIRWISE_CASES);
        assertAllTestCombinationsAreGenerated(allPairs);
    }

    @Test
    void shouldGenerateAllPairwiseCasesWithEmptyConstraints() {
        final AllPairs allPairs = new AllPairs.AllPairsBuilder()
                .withParameters(TestData.PARAMETERS)
                .withConstraints(Collections.emptyList())
                .build();

        assertThat(allPairs.getGeneratedCases()).containsExactlyElementsOf(TestData.EXPECTED_PAIRWISE_CASES);
        assertAllTestCombinationsAreGenerated(allPairs);
    }

    @Test
    @Timeout(value = 5)
    void shouldGenerateAllPairwiseCasesForLargeInputWithoutConstraintsAndNotExceedTimeout() {
        final AllPairs allPairs = new AllPairs.AllPairsBuilder()
                .withParameters(TestData.PARAMETERS_LARGE)
                .build();

        assertAllTestCombinationsAreGenerated(allPairs);
    }

    @Test
    @Timeout(value = 5)
    void shouldGenerateAllPairwiseCasesForLargeInputWithConstraintsAndNotExceedTimeout() {
        final AllPairs allPairs = new AllPairs.AllPairsBuilder()
                .withParameters(TestData.PARAMETERS_LARGE)
                .withConstraint(c -> c.get("1").equals("1-1") && c.get("2").equals("2-1"))
                .withConstraint(c -> c.get("3").equals("3-1") && c.get("4").equals("4-1"))
                .withConstraint(c -> c.get("2").equals("2-1") && c.get("3").equals("3-1") && c.get("20").equals("20-1"))
                .build();

        assertAllTestCombinationsAreGenerated(allPairs);
    }

    /**
     * Asserts all {@code n}-wise test combinations are generated.
     *
     * @param allPairs {@link AllPairs} instance
     */
    private void assertAllTestCombinationsAreGenerated(AllPairs allPairs) {
        assertThat(allPairs.getGeneratedUniqueTestCombinations())
                .as(allPairs.getTestCombinationSize() + "-wise test combinations")
                .containsExactlyInAnyOrderElementsOf(allPairs.getExpectedUniqueTestCombinations());
    }
}
