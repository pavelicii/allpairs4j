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

import java.util.Collections;

import static org.assertj.core.api.Assertions.assertThatExceptionOfType;

class ValidationTest {

    @Test
    void shouldThrowWhenTestCombinationSizeIsLessThan2() {
        assertThatExceptionOfType(IllegalArgumentException.class).isThrownBy(
                () -> new AllPairs.AllPairsBuilder()
                        .withParameters(TestData.PARAMETERS)
                        .withTestCombinationSize(1)
                        .build()
        );
    }

    @Test
    void shouldThrowWhenTestCombinationSizeIsGreaterThanNumberOfParameters() {
        assertThatExceptionOfType(IllegalArgumentException.class).isThrownBy(
                () -> new AllPairs.AllPairsBuilder()
                        .withParameters(TestData.PARAMETERS)
                        .withTestCombinationSize(5)
                        .build()
        );
    }

    @Test
    void shouldThrowWhenParameterIsEmpty() {
        assertThatExceptionOfType(IllegalArgumentException.class).isThrownBy(
                () -> new AllPairs.AllPairsBuilder()
                        .withParameters(TestData.PARAMETERS)
                        .withParameter(new Parameter("Name", Collections.emptyList()))
                        .build()
        );
    }

    @Test
    void shouldThrowWhenParametersAreEmpty() {
        assertThatExceptionOfType(IllegalArgumentException.class).isThrownBy(
                () -> new AllPairs.AllPairsBuilder()
                        .withParameters(Collections.emptyList())
                        .build()
        );
    }

    @Test
    void shouldThrowWhenParameterNameIsNull() {
        assertThatExceptionOfType(IllegalArgumentException.class).isThrownBy(
                () -> new AllPairs.AllPairsBuilder()
                        .withParameters(TestData.PARAMETERS)
                        .withParameter(new Parameter(null, "Foo", "Bar"))
                        .build()
        );
    }

    @Test
    void shouldThrowWhenParameterNameIsEmpty() {
        assertThatExceptionOfType(IllegalArgumentException.class).isThrownBy(
                () -> new AllPairs.AllPairsBuilder()
                        .withParameters(TestData.PARAMETERS)
                        .withParameter(new Parameter("", "Foo", "Bar"))
                        .build()
        );
    }

    @Test
    void shouldThrowWhenParameterHasDuplicateValues() {
        assertThatExceptionOfType(IllegalArgumentException.class).isThrownBy(
                () -> new AllPairs.AllPairsBuilder()
                        .withParameters(TestData.PARAMETERS)
                        .withParameter(new Parameter("Name", 1, 1))
                        .build()
        );
    }

    @Test
    void shouldThrowWhenParameterNameIsNotUnique() {
        assertThatExceptionOfType(IllegalArgumentException.class).isThrownBy(
                () -> new AllPairs.AllPairsBuilder()
                        .withParameters(TestData.PARAMETERS)
                        .withParameter(new Parameter("Name", 1, 2))
                        .withParameter(new Parameter("Name", 3, 4))
                        .build()
        );
    }

    @Test
    void shouldThrowWhenParameterIsNull() {
        assertThatExceptionOfType(NullPointerException.class).isThrownBy(
                () -> new AllPairs.AllPairsBuilder()
                        .withParameter(null)
                        .build()
        ).withStackTraceContaining("requireNonNull");
    }

    @Test
    void shouldThrowWhenParametersAreNull() {
        assertThatExceptionOfType(NullPointerException.class).isThrownBy(
                () -> new AllPairs.AllPairsBuilder()
                        .withParameters(null)
                        .build()
        ).withStackTraceContaining("requireNonNull");
    }

    @Test
    void shouldThrowWhenConstraintIsNull() {
        assertThatExceptionOfType(NullPointerException.class).isThrownBy(
                () -> new AllPairs.AllPairsBuilder()
                        .withParameters(TestData.PARAMETERS)
                        .withConstraint(null)
                        .build()
        ).withStackTraceContaining("requireNonNull");
    }

    @Test
    void shouldThrowWhenConstraintsAreNull() {
        assertThatExceptionOfType(NullPointerException.class).isThrownBy(
                () -> new AllPairs.AllPairsBuilder()
                        .withParameters(TestData.PARAMETERS)
                        .withConstraints(null)
                        .build()
        ).withStackTraceContaining("requireNonNull");
    }
}
