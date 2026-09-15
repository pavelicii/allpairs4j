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

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Checks expected cases, repeatable results, and parameters with only one possible value.
 * Also checks that generation finishes within a time limit for large inputs.
 */
@SuppressWarnings("checkstyle:MultipleStringLiterals")
class GenerationTest {

    @Test
    void shouldGenerateAllPairwiseCasesWithoutConstraints() {
        final AllPairs allPairs = new AllPairs.AllPairsBuilder()
                .withParameters(TestData.PARAMETERS)
                .build();

        assertThat(allPairs.getGeneratedCases()).containsExactlyElementsOf(TestData.EXPECTED_PAIRWISE_CASES);
    }

    @Test
    void shouldGenerateFilteredPairwiseCasesWithConstraints() {
        final AllPairs allPairs = new AllPairs.AllPairsBuilder()
                .withParameters(TestData.PARAMETERS)
                .withConstraints(Arrays.asList(
                        c -> c.get("Browser").equals("Safari") && !c.get("OS").equals("macOS"),
                        c -> c.get("Browser").equals("Edge") && !c.get("OS").equals("Windows")
                ))
                .build();

        assertThat(allPairs.getGeneratedCases()).containsExactlyElementsOf(TestData.EXPECTED_FILTERED_PAIRWISE_CASES);
    }

    @Test
    void shouldGenerateAllTriplewiseCasesWithoutConstraints() {
        final AllPairs allPairs = new AllPairs.AllPairsBuilder()
                .withParameters(TestData.PARAMETERS)
                .withTestCombinationSize(3)
                .build();

        assertThat(allPairs.getGeneratedCases()).containsExactlyElementsOf(TestData.EXPECTED_TRIPLEWISE_CASES);
    }

    @Test
    void shouldGenerateFilteredTriplewiseCasesWithConstraints() {
        final AllPairs allPairs = new AllPairs.AllPairsBuilder()
                .withParameters(TestData.PARAMETERS)
                .withConstraints(Arrays.asList(
                        c -> c.get("Browser").equals("Safari") && !c.get("OS").equals("macOS"),
                        c -> c.get("Browser").equals("Edge") && !c.get("OS").equals("Windows"),
                        c -> c.get("OS").equals("Linux") && c.get("Drive").equals("SSD") && (int) c.get("RAM") < 8000
                ))
                .withTestCombinationSize(3)
                .build();

        assertThat(allPairs.getGeneratedCases()).containsExactlyElementsOf(TestData.EXPECTED_FILTERED_TRIPLEWISE_CASES);
    }

    @Test
    void shouldGenerateZeroCasesWithConstraintsExcludingAllSingleParameterValues() {
        final AllPairs allPairs = new AllPairs.AllPairsBuilder()
                .withParameters(TestData.PARAMETERS)
                .withConstraint(c -> c.get("Drive").equals("HDD"))
                .withConstraint(c -> c.get("Drive").equals("SSD"))
                .build();

        assertThat(allPairs.getGeneratedCases()).isEmpty();
    }

    @Test
    void shouldGenerateAllPairwiseCasesWithEmptyConstraints() {
        final AllPairs allPairs = new AllPairs.AllPairsBuilder()
                .withParameters(TestData.PARAMETERS)
                .withConstraints(Collections.emptyList())
                .build();

        assertThat(allPairs.getGeneratedCases()).containsExactlyElementsOf(TestData.EXPECTED_PAIRWISE_CASES);
    }

    @Test
    void shouldBeRepeatableByDefaultAndAfterDisablingRandomization() {
        final AllPairs.AllPairsBuilder builder = new AllPairs.AllPairsBuilder()
                .withParameters(TestData.PARAMETERS)
                .withTestCombinationSize(3);
        final List<Case> first = builder.build().getGeneratedCases();
        assertThat(builder.build().getGeneratedCases()).containsExactlyElementsOf(first);
        final AllPairs randomized = builder.withRandomization(true).build();
        assertThat(randomized.getRandomizationSeed()).isNotNull();
        TestSupport.assertCoverage(randomized, Collections.emptySet(), true, c -> true);
        assertThat(builder.withRandomization(true, randomized.getRandomizationSeed()).build().getGeneratedCases())
                .containsExactlyElementsOf(randomized.getGeneratedCases());
        assertThat(randomized.toBuilder().build().getGeneratedCases()).containsExactlyElementsOf(randomized.getGeneratedCases());
        assertThat(builder.withRandomization(false).withAbsenceCoverage(false).build().getGeneratedCases()).containsExactlyElementsOf(first);
    }

    @ParameterizedTest
    @ValueSource(longs = {0, -1, Long.MIN_VALUE, Long.MAX_VALUE})
    void shouldReuseExplicitSeed(long seed) {
        final AllPairs.AllPairsBuilder builder = new AllPairs.AllPairsBuilder()
                .withParameters(TestData.PARAMETERS)
                .withParameter(new Parameter(TestData.WINDOWS_EDITION).withConstraint(c -> !c.get("OS").equals("Windows")))
                .withConstraint(c -> c.get("Browser").equals("Safari") && !c.get("OS").equals("macOS"))
                .withRandomization(true, seed);
        final AllPairs first = builder.build();
        assertThat(first.getRandomizationSeed()).isEqualTo(seed);
        assertThat(first.toString()).startsWith("Randomization seed: " + seed + System.lineSeparator());
        assertThat(builder.build().getGeneratedCases()).containsExactlyElementsOf(first.getGeneratedCases());
        assertThat(first.toBuilder().build().getGeneratedCases()).containsExactlyElementsOf(first.getGeneratedCases());
        final AllPairs deterministic = builder.withRandomization(false, seed).build();
        assertThat(deterministic.getRandomizationSeed()).isNull();
        assertThat(deterministic.toString()).doesNotContain("Randomization seed:");
        assertThat(builder.withRandomization(false).build().getGeneratedCases())
                .containsExactlyElementsOf(deterministic.getGeneratedCases());
    }

    @Test
    void shouldGenerateAllCombinationsForTwoParameters() {
        final AllPairs allPairs = new AllPairs.AllPairsBuilder()
                .withParameter(TestData.OS)
                .withParameter(TestData.DRIVE)
                .build();
        assertThat(allPairs.getGeneratedCases()).containsExactlyInAnyOrderElementsOf(
                TestSupport.cartesianProduct(allPairs.getParameters(), Collections.emptySet())
        );
    }

    @ParameterizedTest(name = "large input, constrained={0}")
    @ValueSource(booleans = {false, true})
    @Timeout(5)
    void shouldGenerateCasesForLargeInputWithinTimeout(boolean constrained) {
        final AllPairs allPairs = new AllPairs.AllPairsBuilder()
                .withParameters(TestData.PARAMETERS_LARGE)
                .withConstraints(constrained ? Arrays.asList(
                        c -> c.get("1").equals("1-1") && c.get("2").equals("2-1"),
                        c -> c.get("3").equals("3-1") && c.get("4").equals("4-1"),
                        c -> c.get("2").equals("2-1") && c.get("3").equals("3-1") && c.get("20").equals("20-1"))
                        : Collections.emptyList())
                .build();

        assertThat(allPairs.getGeneratedCases()).isNotEmpty();
    }

    /** One case is enough to cover every four-parameter combination when each parameter has only one value. */
    @Test
    void shouldGenerateOneFourWayCaseForThirteenSingletonParameters() {
        final List<Parameter> parameters = TestData.numberedParameters(13, "P", 0);
        final AllPairs allPairs = new AllPairs.AllPairsBuilder()
                .withParameters(parameters)
                .withTestCombinationSize(4)
                .build();
        assertThat(allPairs.getGeneratedCases()).hasSize(1)
                .containsExactlyElementsOf(TestSupport.cartesianProduct(parameters, Collections.emptySet()));
    }
}
