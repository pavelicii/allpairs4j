package io.github.pavelicii.allpairs4j;

import org.junit.jupiter.api.Timeout;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Set;

/** Checks validity and complete coverage against independently filtered Cartesian products of small models. */
@Timeout(15)
@SuppressWarnings("checkstyle:MultipleStringLiterals")
class CoverageOracleTest {

    @ParameterizedTest
    @CsvSource({"2,true", "2,false", "3,true", "3,false"})
    void shouldPreserveCoverageWhenParametersAreReordered(int strength, boolean absenceCoverage) {
        final Parameter drive = new Parameter(TestData.NULLABLE_DRIVE);
        drive.add(Parameter.ABSENT);
        final List<Parameter> parameters = new ArrayList<>(Arrays.asList(
                TestData.BROWSER,
                TestData.OS,
                drive,
                new Parameter(TestData.WINDOWS_EDITION).withConstraint(c -> !c.get("OS").equals("Windows"))
        ));
        for (int order = 0; order < parameters.size(); order++) {
            final AllPairs actual = new AllPairs.AllPairsBuilder()
                    .withParameters(parameters)
                    .withConstraint(c -> c.get("Browser").equals("Safari") && !c.get("OS").equals("macOS"))
                    .withTestCombinationSize(strength)
                    .withAbsenceCoverage(absenceCoverage)
                    .withRandomization(true, 17L)
                    .build();
            TestSupport.assertCoverage(actual, Set.of("WindowsEdition"), absenceCoverage,
                    c -> (!c.get("Browser").equals("Safari") || c.get("OS").equals("macOS"))
                            && c.containsKey("WindowsEdition") == c.get("OS").equals("Windows"));
            Collections.rotate(parameters, 1);
        }
    }

    @ParameterizedTest
    @CsvSource({"2,true,false", "3,false,true"})
    void shouldCoverExactlyFeasibleCombinations(int strength, boolean absenceCoverage, boolean randomized) {
        final AllPairs allPairs = new AllPairs.AllPairsBuilder()
                .withParameter(new Parameter("A", 0, 1))
                .withParameter(new Parameter("B", 0, 1))
                .withParameter(new Parameter("C", null, 1).withConstraint(c -> !c.get("Mode").equals(1)))
                .withParameter(new Parameter("D", 0, 1).withConstraint(c -> !c.get("Mode").equals(0)))
                .withParameter(new Parameter("Mode", 0, 1))
                .withRandomization(randomized)
                .withAbsenceCoverage(absenceCoverage)
                .withTestCombinationSize(strength)
                .withConstraint(c -> !c.get("A").equals(c.get("B")))
                .withConstraint(c -> Objects.equals(c.get("C"), 1) && c.get("A").equals(0))
                .build();
        TestSupport.assertCoverage(
                allPairs,
                Set.of("C", "D"),
                absenceCoverage,
                c -> c.get("A").equals(c.get("B"))
                        && (!Objects.equals(c.get("C"), 1) || c.get("A").equals(1))
                        && c.containsKey("C") == c.get("Mode").equals(1)
                        && c.containsKey("D") == c.get("Mode").equals(0)
        );
    }

    @ParameterizedTest
    @CsvSource({"2,false", "3,true"})
    void shouldCoverCyclicPresenceDependencies(int strength, boolean absenceCoverage) {
        final AllPairs allPairs = new AllPairs.AllPairsBuilder()
                .withParameter(new Parameter("A", 0, 1).withConstraint(c -> !c.isPresent("B")))
                .withParameter(new Parameter("B", 0, 1).withConstraint(c -> !c.isPresent("C")))
                .withParameter(new Parameter("C", 0, 1).withConstraint(c -> !c.isPresent("A")))
                .withTestCombinationSize(strength)
                .withAbsenceCoverage(absenceCoverage)
                .build();
        TestSupport.assertCoverage(allPairs, Set.of("A", "B", "C"), absenceCoverage, c -> c.isEmpty() || c.size() == 3);
    }

    @ParameterizedTest(name = "explicit absence: strength={0}, absenceCoverage={1}, randomized={2}")
    @CsvSource({"2,true,false", "3,false,true", "5,true,false"})
    void shouldCoverExplicitAbsenceWithParameterConstraints(int strength, boolean absenceCoverage, boolean randomized) {
        // Exercise sentinel indices at the beginning, middle and end, including an absent-only domain.
        final List<Parameter> parameters = Arrays.asList(
                new Parameter("A", Parameter.ABSENT, null, 1),
                new Parameter("B", 0, Parameter.ABSENT, 1),
                new Parameter("C", null, 0, Parameter.ABSENT),
                new Parameter("D", Parameter.ABSENT),
                new Parameter("Mode", 0, 1)
        );
        final AllPairs.AllPairsBuilder builder = new AllPairs.AllPairsBuilder()
                .withParameters(parameters.subList(0, 2))
                .withParameter(parameters.get(2).withConstraint(c -> !c.get("Mode").equals(1)))
                .withParameters(parameters.subList(3, 5))
                .withConstraint(c -> c.isPresent("A") != c.isPresent("B"))
                .withConstraint(c -> c.get("B").equals(1) && !Objects.equals(c.get("A"), 1))
                .withTestCombinationSize(strength)
                .withAbsenceCoverage(absenceCoverage)
                .withRandomization(randomized);
        TestSupport.assertCoverage(
                builder.build(),
                Collections.singleton("C"),
                absenceCoverage,
                c -> c.containsKey("A") == c.containsKey("B")
                        && (!c.containsKey("C") || c.get("Mode").equals(1))
                        && (!Objects.equals(c.get("B"), 1) || Objects.equals(c.get("A"), 1))
        );
    }
}
