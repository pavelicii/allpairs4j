package io.github.pavelicii.allpairs4j;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Predicate;

import static org.assertj.core.api.Assertions.assertThat;

/** Generic test-data conversion and independent coverage checks for small models. */
final class TestSupport {

    private static final Object ABSENT = new Object();

    private TestSupport() {
    }

    /**
     * Converts a table into cases, keeping null values and omitting explicit absence markers.
     *
     * @param parameters column names in order
     * @param values rows with one value per parameter
     * @return cases in table order
     */
    static List<Case> cases(List<Parameter> parameters, Object[]... values) {
        final List<Case> result = new ArrayList<>();
        for (Object[] row : values) {
            assertThat(row).hasSize(parameters.size());
            final Case testCase = new Case();
            for (int i = 0; i < row.length; i++) {
                if (row[i] != Parameter.ABSENT) {
                    testCase.put(parameters.get(i).getName(), row[i]);
                }
            }
            result.add(testCase);
        }
        return result;
    }

    /**
     * Lists every possible complete case before applying any constraints.
     * Leaves out absent parameters and keeps entries whose value is {@code null}.
     *
     * @param parameters parameters and values to combine
     * @param conditionalNames names of parameters that can be absent even without an {@link Parameter#ABSENT} value
     * @return all complete cases, without filtering
     */
    static List<Case> cartesianProduct(List<Parameter> parameters, Set<String> conditionalNames) {
        List<Case> rows = Collections.singletonList(new Case());
        for (Parameter parameter : parameters) {
            final List<Case> expanded = new ArrayList<>();
            for (Case row : rows) {
                if (conditionalNames.contains(parameter.getName()) && !parameter.contains(Parameter.ABSENT)) {
                    expanded.add(new Case(row));
                }
                for (Object value : parameter) {
                    final Case next = new Case(row);
                    if (value != Parameter.ABSENT) {
                        next.put(parameter.getName(), value);
                    }
                    expanded.add(next);
                }
            }
            rows = expanded;
        }
        return rows;
    }

    /**
     * Checks generated coverage against every valid case in a small model.
     * Uses a separate predicate that returns {@code true} for valid complete cases, unlike a generator constraint.
     *
     * @param actual generation results to check
     * @param conditionalNames names of parameters that can be absent without an explicit marker
     * @param absenceCoverage whether combinations with absent parameters need coverage
     * @param isValid separate predicate for deciding whether a complete case is valid
     */
    static void assertCoverage(AllPairs actual, Set<String> conditionalNames, boolean absenceCoverage, Predicate<Case> isValid) {
        final List<Case> valid = cartesianProduct(actual.getParameters(), conditionalNames);
        valid.removeIf(isValid.negate());
        final List<Case> rows = actual.getGeneratedCases();
        assertThat(rows).doesNotHaveDuplicates().isSubsetOf(valid);
        assertThat(rows.isEmpty()).isEqualTo(valid.isEmpty());
        assertThat(combinations(rows, actual.getParameters(), actual.getTestCombinationSize(), absenceCoverage))
                .isEqualTo(combinations(valid, actual.getParameters(), actual.getTestCombinationSize(), absenceCoverage));
    }

    /**
     * Collects combinations from the cases, using a private marker to distinguish absence from {@code null}.
     * Use only for small models where trying every combination is practical.
     *
     * @param rows complete cases
     * @param parameters all declared parameters, including those absent from some cases
     * @param strength largest combination size
     * @param absenceCoverage whether to include combinations with absent parameters
     * @return unique combinations of sizes 1 through {@code strength}
     */
    private static Set<Case> combinations(List<Case> rows, List<Parameter> parameters, int strength, boolean absenceCoverage) {
        assertThat(parameters.size()).isLessThan(31);
        final Set<Case> result = new HashSet<>();
        for (Case row : rows) {
            for (int mask = 1; mask < (1 << parameters.size()); mask++) {
                if (Integer.bitCount(mask) > strength) {
                    continue;
                }
                final Case combination = new Case();
                for (int i = 0; i < parameters.size(); i++) {
                    if ((mask & (1 << i)) != 0) {
                        final String name = parameters.get(i).getName();
                        combination.put(name, row.getOrDefault(name, ABSENT));
                    }
                }
                if (absenceCoverage || !combination.containsValue(ABSENT)) {
                    result.add(combination);
                }
            }
        }
        return result;
    }
}
