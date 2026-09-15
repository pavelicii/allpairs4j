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
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.Arrays;
import java.util.Collections;

import static org.assertj.core.api.Assertions.assertThatExceptionOfType;

/**
 * Checks that the builder rejects missing parameters or names, duplicate names or values,
 * {@code null} arguments, and combination sizes outside the allowed range.
 */
class ValidationTest {

    @ParameterizedTest
    @ValueSource(ints = {1, 5})
    void shouldThrowWhenTestCombinationSizeIsOutOfRange(int size) {
        assertThatExceptionOfType(IllegalArgumentException.class).isThrownBy(
                () -> new AllPairs.AllPairsBuilder()
                        .withParameters(TestData.PARAMETERS)
                        .withTestCombinationSize(size)
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

    @ParameterizedTest
    @NullAndEmptySource
    void shouldThrowWhenParameterNameIsMissing(String name) {
        assertThatExceptionOfType(IllegalArgumentException.class).isThrownBy(
                () -> new AllPairs.AllPairsBuilder()
                        .withParameters(TestData.PARAMETERS)
                        .withParameter(new Parameter(name, "Foo", "Bar"))
                        .build()
        );
    }

    @ParameterizedTest
    @ValueSource(ints = {0, 1, 2})
    void shouldThrowWhenParameterHasDuplicateValues(int index) {
        final Object value = Arrays.asList(1, null, Parameter.ABSENT).get(index);
        assertThatExceptionOfType(IllegalArgumentException.class).isThrownBy(
                () -> new AllPairs.AllPairsBuilder()
                        .withParameters(TestData.PARAMETERS)
                        .withParameter(new Parameter("Name", value, value))
                        .build()
        ).withMessageContaining("no duplicate values");
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
    void shouldRejectNullArguments() {
        final AllPairs.AllPairsBuilder builder = new AllPairs.AllPairsBuilder();
        assertThatExceptionOfType(NullPointerException.class).isThrownBy(() -> builder.withParameter(null));
        assertThatExceptionOfType(NullPointerException.class).isThrownBy(() -> builder.withParameters(null));
        assertThatExceptionOfType(NullPointerException.class).isThrownBy(() -> builder.withConstraint(null));
        assertThatExceptionOfType(NullPointerException.class).isThrownBy(() -> builder.withConstraints(null));
        assertThatExceptionOfType(NullPointerException.class)
                .isThrownBy(() -> new Parameter("Drive", TestData.DRIVE).withConstraint(null));
    }

    @ParameterizedTest
    @ValueSource(booleans = {false, true})
    void shouldRejectUnknownNameAfterResolvingKnownParameter(boolean presenceRead) {
        assertThatExceptionOfType(IllegalArgumentException.class).isThrownBy(
                () -> new AllPairs.AllPairsBuilder()
                        .withParameters(TestData.PARAMETERS)
                        .withConstraint(c -> c.get("OS").equals("Linux")
                                && (presenceRead ? c.isPresent("typo") : c.get("typo") == null))
                        .build()).withMessage("Unknown parameter in case constraint: typo");
    }

    @ParameterizedTest
    @ValueSource(booleans = {false, true})
    void shouldRejectUnknownNameInParameterConstraint(boolean presenceRead) {
        assertThatExceptionOfType(IllegalArgumentException.class).isThrownBy(
                () -> new AllPairs.AllPairsBuilder().withParameter(TestData.OS)
                        .withParameter(new Parameter("Drive", TestData.DRIVE).withConstraint(c -> presenceRead
                                ? !c.isPresent("Unknown") : c.get("Unknown") == null))
                        .build())
                .withMessageContaining("Unknown parameter in parameter constraint for 'Drive': Unknown");
    }
}
