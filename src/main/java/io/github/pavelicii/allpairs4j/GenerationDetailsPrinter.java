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

import java.util.BitSet;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.StringJoiner;

/** Prints the required combinations and what each final case covers, without changing the generator's state. */
final class GenerationDetailsPrinter {

    private GenerationDetailsPrinter() {
    }

    static void printRequiredCombinations(CombinationStorage storage) {
        System.out.println("=== Required combinations (" + storage.getExpectedCount() + ") ===");
        System.out.println();
        int combinationNumber = 0;
        for (CompactTable table : storage.getCoverageTables()) {
            for (CompactTable.Group group : table.getGroups()) {
                for (int index = group.nextRequiredIndex(0); index >= 0; index = group.nextRequiredIndex(index + 1)) {
                    System.out.printf("%3d: %s%n", ++combinationNumber,
                            formatCombination(storage.decode(group, index), table != storage.getTargetTable()));
                }
            }
        }
    }

    static void printGeneratedCases(CombinationStorage storage, List<List<Item>> sequences,
                                    List<Parameter> parameters, boolean absenceCoverage) {
        System.out.println();
        System.out.println("=== Generated cases (" + sequences.size() + ") ===");
        // Replay coverage in final case order, independently of generation-time coverage.
        final Map<CompactTable.Group, BitSet> covered = new HashMap<>();
        int caseNumber = 0;
        for (List<Item> sequence : sequences) {
            System.out.println();
            final String formatted = formatCase(parameters, new Case(sequence), absenceCoverage);
            System.out.println("Case " + ++caseNumber + ": " + formatted);
            final StringBuilder combinations = new StringBuilder();
            int combinationNumber = 0;
            for (CompactTable table : storage.getCoverageTables()) {
                for (CompactTable.Group group : table.getGroups()) {
                    final int index = group.indexOf(sequence);
                    final BitSet seen = covered.computeIfAbsent(group, ignored -> new BitSet());
                    if (group.isRequired(index) && !seen.get(index)) {
                        seen.set(index);
                        combinations.append(String.format("    %3d: %s%n", ++combinationNumber,
                                formatCombination(storage.decode(group, index), table != storage.getTargetTable())));
                    }
                }
            }
            System.out.println("  New combinations (" + combinationNumber + "):");
            if (combinationNumber == 0) {
                System.out.println("    (none)");
            } else {
                System.out.print(combinations);
            }
        }
    }

    static String formatCase(List<Parameter> parameters, Case testCase, boolean absenceCoverage) {
        final StringJoiner result = new StringJoiner(", ", "{", "}");
        for (Parameter parameter : parameters) {
            final String name = parameter.getName();
            if (absenceCoverage || testCase.containsKey(name)) {
                result.add(name + "=" + testCase.getOrDefault(name, Parameter.ABSENT));
            }
        }
        return result.toString();
    }

    private static String formatCombination(List<Item> items, boolean smaller) {
        final StringJoiner result = new StringJoiner(", ", "{", "}");
        for (Item item : items) {
            result.add(item.getName() + "=" + (item.isPresent() ? item.getValue() : Parameter.ABSENT));
        }
        return result + (smaller ? " (no larger combination without absence)" : "");
    }
}
