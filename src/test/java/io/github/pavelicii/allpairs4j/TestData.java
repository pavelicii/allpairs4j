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

    static final List<Parameter> PARAMETERS_LARGE = Arrays.asList(
            new Parameter("1", "1-1", "1-2", "1-3", "1-4", "1-5", "1-6", "1-7", "1-8"),
            new Parameter("2", "2-1", "2-2", "2-3", "2-4", "2-5", "2-6", "2-7", "2-8"),
            new Parameter("3", "3-1", "3-2", "3-3", "3-4", "3-5", "3-6", "3-7", "3-8"),
            new Parameter("4", "4-1", "4-2", "4-3", "4-4", "4-5", "4-6", "4-7", "4-8"),
            new Parameter("5", "5-1", "5-2", "5-3", "5-4", "5-5", "5-6", "5-7", "5-8"),
            new Parameter("6", "6-1", "6-2", "6-3", "6-4", "6-5", "6-6", "6-7", "6-8"),
            new Parameter("7", "7-1", "7-2", "7-3", "7-4", "7-5", "7-6", "7-7", "7-8"),
            new Parameter("8", "8-1", "8-2", "8-3", "8-4", "8-5", "8-6", "8-7", "8-8"),
            new Parameter("9", "9-1", "9-2", "9-3", "9-4", "9-5", "9-6", "9-7", "9-8"),
            new Parameter("10", "10-1", "10-2", "10-3", "10-4", "10-5", "10-6", "10-7", "10-8"),
            new Parameter("11", "11-1", "11-2", "11-3", "11-4", "11-5", "11-6", "11-7", "11-8"),
            new Parameter("12", "12-1", "12-2", "12-3", "12-4", "12-5", "12-6", "12-7", "12-8"),
            new Parameter("13", "13-1", "13-2", "13-3", "13-4", "13-5", "13-6", "13-7", "13-8"),
            new Parameter("14", "14-1", "14-2", "14-3", "14-4", "14-5", "14-6", "14-7", "14-8"),
            new Parameter("15", "15-1", "15-2", "15-3", "15-4", "15-5", "15-6", "15-7", "15-8"),
            new Parameter("16", "16-1", "16-2", "16-3", "16-4", "16-5", "16-6", "16-7", "16-8"),
            new Parameter("17", "17-1", "17-2", "17-3", "17-4", "17-5", "17-6", "17-7", "17-8"),
            new Parameter("18", "18-1", "18-2", "18-3", "18-4", "18-5", "18-6", "18-7", "18-8"),
            new Parameter("19", "19-1", "19-2", "19-3", "19-4", "19-5", "19-6", "19-7", "19-8"),
            new Parameter("20", "20-1", "20-2", "20-3", "20-4", "20-5", "20-6", "20-7", "20-8")
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

    static final List<Case> EXPECTED_TRIPLEWISE_CASES = Arrays.asList(
            new Case("Browser", "Chrome", "OS", "Windows", "RAM", 2048, "Drive", "HDD"),
            new Case("Browser", "Safari", "OS", "Linux", "RAM", 4096, "Drive", "HDD"),
            new Case("Browser", "Edge", "OS", "macOS", "RAM", 8192, "Drive", "HDD"),
            new Case("Browser", "Edge", "OS", "macOS", "RAM", 16384, "Drive", "SSD"),
            new Case("Browser", "Safari", "OS", "Linux", "RAM", 16384, "Drive", "SSD"),
            new Case("Browser", "Chrome", "OS", "Windows", "RAM", 8192, "Drive", "SSD"),
            new Case("Browser", "Chrome", "OS", "Windows", "RAM", 4096, "Drive", "HDD"),
            new Case("Browser", "Safari", "OS", "Linux", "RAM", 2048, "Drive", "HDD"),
            new Case("Browser", "Edge", "OS", "macOS", "RAM", 2048, "Drive", "HDD"),
            new Case("Browser", "Edge", "OS", "macOS", "RAM", 4096, "Drive", "HDD"),
            new Case("Browser", "Safari", "OS", "Linux", "RAM", 8192, "Drive", "SSD"),
            new Case("Browser", "Chrome", "OS", "Windows", "RAM", 16384, "Drive", "SSD"),
            new Case("Browser", "Chrome", "OS", "Windows", "RAM", 16384, "Drive", "HDD"),
            new Case("Browser", "Safari", "OS", "Linux", "RAM", 4096, "Drive", "SSD"),
            new Case("Browser", "Edge", "OS", "macOS", "RAM", 2048, "Drive", "SSD"),
            new Case("Browser", "Edge", "OS", "macOS", "RAM", 8192, "Drive", "SSD"),
            new Case("Browser", "Safari", "OS", "Linux", "RAM", 8192, "Drive", "HDD"),
            new Case("Browser", "Chrome", "OS", "Windows", "RAM", 2048, "Drive", "SSD"),
            new Case("Browser", "Chrome", "OS", "Windows", "RAM", 4096, "Drive", "SSD"),
            new Case("Browser", "Safari", "OS", "Linux", "RAM", 16384, "Drive", "HDD"),
            new Case("Browser", "Edge", "OS", "macOS", "RAM", 16384, "Drive", "HDD"),
            new Case("Browser", "Edge", "OS", "macOS", "RAM", 4096, "Drive", "SSD"),
            new Case("Browser", "Safari", "OS", "Linux", "RAM", 2048, "Drive", "SSD"),
            new Case("Browser", "Chrome", "OS", "Windows", "RAM", 8192, "Drive", "HDD"),
            new Case("Browser", "Chrome", "OS", "Linux", "RAM", 8192, "Drive", "HDD"),
            new Case("Browser", "Safari", "OS", "macOS", "RAM", 2048, "Drive", "SSD"),
            new Case("Browser", "Edge", "OS", "Windows", "RAM", 4096, "Drive", "SSD"),
            new Case("Browser", "Edge", "OS", "Windows", "RAM", 16384, "Drive", "HDD"),
            new Case("Browser", "Safari", "OS", "macOS", "RAM", 16384, "Drive", "HDD"),
            new Case("Browser", "Chrome", "OS", "Linux", "RAM", 4096, "Drive", "SSD"),
            new Case("Browser", "Chrome", "OS", "Linux", "RAM", 2048, "Drive", "SSD"),
            new Case("Browser", "Safari", "OS", "macOS", "RAM", 8192, "Drive", "HDD"),
            new Case("Browser", "Edge", "OS", "Windows", "RAM", 8192, "Drive", "HDD"),
            new Case("Browser", "Safari", "OS", "macOS", "RAM", 4096, "Drive", "SSD"),
            new Case("Browser", "Edge", "OS", "Windows", "RAM", 2048, "Drive", "SSD"),
            new Case("Browser", "Chrome", "OS", "Linux", "RAM", 16384, "Drive", "HDD"),
            new Case("Browser", "Chrome", "OS", "macOS", "RAM", 16384, "Drive", "HDD"),
            new Case("Browser", "Edge", "OS", "Linux", "RAM", 2048, "Drive", "SSD"),
            new Case("Browser", "Safari", "OS", "Windows", "RAM", 4096, "Drive", "SSD"),
            new Case("Browser", "Safari", "OS", "Windows", "RAM", 8192, "Drive", "HDD"),
            new Case("Browser", "Edge", "OS", "Linux", "RAM", 8192, "Drive", "HDD"),
            new Case("Browser", "Chrome", "OS", "macOS", "RAM", 4096, "Drive", "SSD"),
            new Case("Browser", "Chrome", "OS", "macOS", "RAM", 2048, "Drive", "SSD"),
            new Case("Browser", "Edge", "OS", "Linux", "RAM", 16384, "Drive", "HDD"),
            new Case("Browser", "Safari", "OS", "Windows", "RAM", 16384, "Drive", "HDD"),
            new Case("Browser", "Safari", "OS", "Windows", "RAM", 2048, "Drive", "SSD"),
            new Case("Browser", "Edge", "OS", "Linux", "RAM", 4096, "Drive", "SSD"),
            new Case("Browser", "Chrome", "OS", "macOS", "RAM", 8192, "Drive", "HDD")
    );

    static final List<Case> EXPECTED_FILTERED_TRIPLEWISE_CASES = Arrays.asList(
            new Case("Browser", "Chrome", "OS", "Windows", "RAM", 2048, "Drive", "HDD"),
            new Case("Browser", "Safari", "OS", "macOS", "RAM", 4096, "Drive", "HDD"),
            new Case("Browser", "Edge", "OS", "Windows", "RAM", 8192, "Drive", "HDD"),
            new Case("Browser", "Edge", "OS", "Windows", "RAM", 16384, "Drive", "SSD"),
            new Case("Browser", "Safari", "OS", "macOS", "RAM", 16384, "Drive", "SSD"),
            new Case("Browser", "Chrome", "OS", "Linux", "RAM", 2048, "Drive", "HDD"),
            new Case("Browser", "Chrome", "OS", "Linux", "RAM", 8192, "Drive", "SSD"),
            new Case("Browser", "Safari", "OS", "macOS", "RAM", 2048, "Drive", "HDD"),
            new Case("Browser", "Edge", "OS", "Windows", "RAM", 2048, "Drive", "SSD"),
            new Case("Browser", "Edge", "OS", "Windows", "RAM", 4096, "Drive", "HDD"),
            new Case("Browser", "Safari", "OS", "macOS", "RAM", 8192, "Drive", "SSD"),
            new Case("Browser", "Chrome", "OS", "Linux", "RAM", 4096, "Drive", "HDD"),
            new Case("Browser", "Chrome", "OS", "Linux", "RAM", 16384, "Drive", "SSD"),
            new Case("Browser", "Safari", "OS", "macOS", "RAM", 16384, "Drive", "HDD"),
            new Case("Browser", "Edge", "OS", "Windows", "RAM", 4096, "Drive", "SSD"),
            new Case("Browser", "Edge", "OS", "Windows", "RAM", 8192, "Drive", "SSD"),
            new Case("Browser", "Safari", "OS", "macOS", "RAM", 8192, "Drive", "HDD"),
            new Case("Browser", "Safari", "OS", "macOS", "RAM", 4096, "Drive", "SSD"),
            new Case("Browser", "Edge", "OS", "Windows", "RAM", 16384, "Drive", "HDD"),
            new Case("Browser", "Edge", "OS", "Windows", "RAM", 2048, "Drive", "HDD"),
            new Case("Browser", "Safari", "OS", "macOS", "RAM", 2048, "Drive", "SSD"),
            new Case("Browser", "Chrome", "OS", "Linux", "RAM", 16384, "Drive", "HDD"),
            new Case("Browser", "Chrome", "OS", "Linux", "RAM", 8192, "Drive", "HDD"),
            new Case("Browser", "Chrome", "OS", "Windows", "RAM", 4096, "Drive", "SSD"),
            new Case("Browser", "Chrome", "OS", "Windows", "RAM", 8192, "Drive", "SSD"),
            new Case("Browser", "Chrome", "OS", "Windows", "RAM", 16384, "Drive", "SSD"),
            new Case("Browser", "Chrome", "OS", "Windows", "RAM", 2048, "Drive", "SSD"),
            new Case("Browser", "Chrome", "OS", "macOS", "RAM", 2048, "Drive", "SSD"),
            new Case("Browser", "Chrome", "OS", "macOS", "RAM", 4096, "Drive", "HDD"),
            new Case("Browser", "Chrome", "OS", "macOS", "RAM", 16384, "Drive", "SSD"),
            new Case("Browser", "Chrome", "OS", "macOS", "RAM", 8192, "Drive", "SSD")
    );

    private TestData() {
    }
}
