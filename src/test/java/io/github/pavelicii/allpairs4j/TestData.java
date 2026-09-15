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

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

/** Parameters and expected cases shared by the basic generation tests. */
@SuppressWarnings("checkstyle:MultipleStringLiterals")
final class TestData {

    static final Parameter BROWSER = new Parameter("Browser", "Chrome", "Safari", "Edge");
    static final Parameter OS = new Parameter("OS", "Windows", "Linux", "macOS");
    static final Parameter RAM = new Parameter("RAM", 2048, 4096, 8192, 16384);
    static final Parameter DRIVE = new Parameter("Drive", "HDD", "SSD");
    static final Parameter WINDOWS_EDITION = new Parameter("WindowsEdition", "Home", "Pro");
    static final Parameter NULLABLE_DRIVE = new Parameter("Drive", "HDD", "SSD", null);

    static final List<Parameter> PARAMETERS = Arrays.asList(BROWSER, OS, RAM, DRIVE);
    static final List<Parameter> OPTIONAL_PARAMETERS = Arrays.asList(
            new Parameter("A", Parameter.ABSENT, null, 1), new Parameter("B", "b"));

    static final List<Parameter> PARAMETERS_LARGE = IntStream.rangeClosed(1, 20)
            .mapToObj(index -> new Parameter(String.valueOf(index), IntStream.rangeClosed(1, 8)
                    .mapToObj(value -> index + "-" + value)
                    .collect(Collectors.toList())))
            .collect(Collectors.toList());

    static final List<Case> EXPECTED_PAIRWISE_CASES = TestSupport.cases(PARAMETERS, new Object[][] {
            {"Chrome", "Windows", 2048, "HDD"},
            {"Safari", "Linux", 4096, "HDD"},
            {"Edge", "macOS", 8192, "HDD"},
            {"Edge", "Linux", 16384, "SSD"},
            {"Safari", "Windows", 16384, "SSD"},
            {"Chrome", "macOS", 4096, "SSD"},
            {"Safari", "macOS", 2048, "SSD"},
            {"Edge", "Windows", 4096, "HDD"},
            {"Safari", "macOS", 16384, "HDD"},
            {"Chrome", "Linux", 16384, "SSD"},
            {"Safari", "Linux", 8192, "SSD"},
            {"Chrome", "Windows", 8192, "HDD"},
            {"Edge", "Linux", 2048, "HDD"}
    });

    static final List<Case> EXPECTED_FILTERED_PAIRWISE_CASES = TestSupport.cases(PARAMETERS, new Object[][] {
            {"Chrome", "Windows", 2048, "HDD"},
            {"Safari", "macOS", 4096, "HDD"},
            {"Edge", "Windows", 8192, "SSD"},
            {"Edge", "Windows", 16384, "HDD"},
            {"Safari", "macOS", 16384, "SSD"},
            {"Chrome", "Linux", 8192, "SSD"},
            {"Safari", "macOS", 2048, "SSD"},
            {"Edge", "Windows", 4096, "SSD"},
            {"Chrome", "macOS", 8192, "HDD"},
            {"Edge", "Windows", 2048, "HDD"},
            {"Safari", "macOS", 8192, "SSD"},
            {"Chrome", "Linux", 4096, "HDD"},
            {"Chrome", "Linux", 16384, "HDD"},
            {"Chrome", "Linux", 2048, "SSD"}
    });

    static final List<Case> EXPECTED_TRIPLEWISE_CASES = TestSupport.cases(PARAMETERS, new Object[][] {
            {"Safari", "Linux", 4096, "HDD"},
            {"Edge", "macOS", 8192, "HDD"},
            {"Edge", "macOS", 16384, "SSD"},
            {"Safari", "Linux", 16384, "SSD"},
            {"Chrome", "Windows", 4096, "HDD"},
            {"Edge", "macOS", 4096, "HDD"},
            {"Chrome", "Windows", 16384, "SSD"},
            {"Edge", "macOS", 2048, "SSD"},
            {"Safari", "Linux", 8192, "HDD"},
            {"Chrome", "Windows", 2048, "SSD"},
            {"Safari", "Linux", 2048, "SSD"},
            {"Chrome", "Windows", 8192, "HDD"},
            {"Chrome", "Linux", 2048, "HDD"},
            {"Chrome", "Linux", 4096, "SSD"},
            {"Chrome", "Linux", 8192, "SSD"},
            {"Chrome", "Linux", 16384, "HDD"},
            {"Chrome", "macOS", 2048, "HDD"},
            {"Chrome", "macOS", 4096, "SSD"},
            {"Chrome", "macOS", 8192, "SSD"},
            {"Chrome", "macOS", 16384, "HDD"},
            {"Safari", "Windows", 2048, "HDD"},
            {"Safari", "Windows", 4096, "SSD"},
            {"Safari", "Windows", 8192, "SSD"},
            {"Safari", "Windows", 16384, "HDD"},
            {"Safari", "macOS", 2048, "HDD"},
            {"Safari", "macOS", 4096, "SSD"},
            {"Safari", "macOS", 8192, "SSD"},
            {"Safari", "macOS", 16384, "HDD"},
            {"Edge", "Windows", 2048, "HDD"},
            {"Edge", "Windows", 16384, "SSD"},
            {"Edge", "Windows", 8192, "SSD"},
            {"Edge", "Windows", 4096, "HDD"},
            {"Edge", "Linux", 2048, "HDD"},
            {"Edge", "Linux", 4096, "SSD"},
            {"Edge", "Linux", 8192, "SSD"},
            {"Edge", "Linux", 16384, "HDD"}
    });

    static final List<Case> EXPECTED_FILTERED_TRIPLEWISE_CASES = TestSupport.cases(PARAMETERS, new Object[][] {
            {"Chrome", "Windows", 2048, "HDD"},
            {"Safari", "macOS", 4096, "HDD"},
            {"Edge", "Windows", 8192, "HDD"},
            {"Edge", "Windows", 16384, "SSD"},
            {"Safari", "macOS", 16384, "SSD"},
            {"Chrome", "Linux", 2048, "HDD"},
            {"Chrome", "Linux", 8192, "SSD"},
            {"Safari", "macOS", 2048, "HDD"},
            {"Edge", "Windows", 2048, "SSD"},
            {"Edge", "Windows", 4096, "HDD"},
            {"Safari", "macOS", 8192, "SSD"},
            {"Chrome", "Linux", 4096, "HDD"},
            {"Chrome", "Linux", 16384, "SSD"},
            {"Safari", "macOS", 16384, "HDD"},
            {"Edge", "Windows", 4096, "SSD"},
            {"Edge", "Windows", 8192, "SSD"},
            {"Safari", "macOS", 8192, "HDD"},
            {"Safari", "macOS", 4096, "SSD"},
            {"Edge", "Windows", 16384, "HDD"},
            {"Edge", "Windows", 2048, "HDD"},
            {"Safari", "macOS", 2048, "SSD"},
            {"Chrome", "Windows", 4096, "SSD"},
            {"Chrome", "Windows", 8192, "SSD"},
            {"Chrome", "Windows", 16384, "HDD"},
            {"Chrome", "macOS", 4096, "SSD"},
            {"Chrome", "macOS", 16384, "HDD"},
            {"Chrome", "macOS", 2048, "SSD"},
            {"Chrome", "macOS", 8192, "HDD"},
            {"Chrome", "Linux", 8192, "HDD"},
            {"Chrome", "Linux", 16384, "HDD"}
    });

    private TestData() {
    }

    /**
     * Creates new parameters with the same possible values and names numbered from zero.
     * For example, the prefix {@code P} gives names {@code P0}, {@code P1}, and so on.
     *
     * @param count number of parameters to create
     * @param prefix text before the number in each name
     * @param values possible values for each parameter
     * @return the new parameters
     */
    static List<Parameter> numberedParameters(int count, String prefix, Object... values) {
        return IntStream.range(0, count)
                .mapToObj(index -> new Parameter(prefix + index, values))
                .collect(Collectors.toList());
    }
}
