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

import java.util.Arrays;
import java.util.List;

@SuppressWarnings("checkstyle:MultipleStringLiterals")
final class TestData {

    static final List<Parameter> PARAMETERS = Arrays.asList(
            new Parameter("Browser", "Chrome", "Safari", "Edge"),
            new Parameter("OS", "Windows", "Linux", "macOS"),
            new Parameter("RAM", 2048, 4096, 8192, 16384),
            new Parameter("Drive", "HDD", "SSD")
    );
    static final List<Case> EXPECTED_PAIRWISE_CASES = Arrays.asList(
            new Case("Browser", "Chrome", "OS", "Windows", "RAM", 2048, "Drive", "HDD"),
            new Case("Browser", "Safari", "OS", "Linux", "RAM", 4096, "Drive", "HDD"),
            new Case("Browser", "Edge", "OS", "macOS", "RAM", 8192, "Drive", "HDD"),
            new Case("Browser", "Edge", "OS", "Linux", "RAM", 16384, "Drive", "SSD"),
            new Case("Browser", "Safari", "OS", "Windows", "RAM", 16384, "Drive", "SSD"),
            new Case("Browser", "Chrome", "OS", "macOS", "RAM", 4096, "Drive", "SSD"),
            new Case("Browser", "Chrome", "OS", "Linux", "RAM", 8192, "Drive", "SSD"),
            new Case("Browser", "Safari", "OS", "macOS", "RAM", 2048, "Drive", "SSD"),
            new Case("Browser", "Edge", "OS", "Windows", "RAM", 4096, "Drive", "HDD"),
            new Case("Browser", "Edge", "OS", "Windows", "RAM", 2048, "Drive", "HDD"),
            new Case("Browser", "Safari", "OS", "macOS", "RAM", 16384, "Drive", "HDD"),
            new Case("Browser", "Chrome", "OS", "Linux", "RAM", 16384, "Drive", "SSD"),
            new Case("Browser", "Safari", "OS", "Linux", "RAM", 8192, "Drive", "SSD"),
            new Case("Browser", "Chrome", "OS", "Windows", "RAM", 8192, "Drive", "HDD"),
            new Case("Browser", "Edge", "OS", "Linux", "RAM", 2048, "Drive", "HDD")
    );
    static final List<Case> EXPECTED_FILTERED_PAIRWISE_CASES = Arrays.asList(
            new Case("Browser", "Chrome", "OS", "Windows", "RAM", 2048, "Drive", "HDD"),
            new Case("Browser", "Safari", "OS", "macOS", "RAM", 4096, "Drive", "HDD"),
            new Case("Browser", "Edge", "OS", "Windows", "RAM", 8192, "Drive", "SSD"),
            new Case("Browser", "Edge", "OS", "Windows", "RAM", 16384, "Drive", "HDD"),
            new Case("Browser", "Safari", "OS", "macOS", "RAM", 16384, "Drive", "SSD"),
            new Case("Browser", "Chrome", "OS", "Linux", "RAM", 8192, "Drive", "SSD"),
            new Case("Browser", "Safari", "OS", "macOS", "RAM", 2048, "Drive", "SSD"),
            new Case("Browser", "Edge", "OS", "Windows", "RAM", 4096, "Drive", "SSD"),
            new Case("Browser", "Chrome", "OS", "macOS", "RAM", 8192, "Drive", "HDD"),
            new Case("Browser", "Edge", "OS", "Windows", "RAM", 2048, "Drive", "HDD"),
            new Case("Browser", "Safari", "OS", "macOS", "RAM", 8192, "Drive", "SSD"),
            new Case("Browser", "Chrome", "OS", "Linux", "RAM", 4096, "Drive", "HDD"),
            new Case("Browser", "Chrome", "OS", "Linux", "RAM", 16384, "Drive", "HDD"),
            new Case("Browser", "Chrome", "OS", "Linux", "RAM", 2048, "Drive", "SSD")
    );

    private TestData() {
    }
}
