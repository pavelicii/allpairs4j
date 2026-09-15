package io.github.pavelicii.allpairs4j;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.util.List;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Checks explicit absence and parameter constraints that decide whether a parameter is applicable.
 * Also checks that having nothing to cover is different from having no valid case.
 */
@Timeout(15)
@SuppressWarnings("checkstyle:MultipleStringLiterals")
class AbsenceCoverageTest {

    @ParameterizedTest
    @ValueSource(booleans = {false, true})
    void shouldPrintAbsentParametersOnlyWhenCoverageIsEnabled(boolean absenceCoverage) {
        final Parameter drive = new Parameter(TestData.NULLABLE_DRIVE);
        drive.add(Parameter.ABSENT);
        final ByteArrayOutputStream output = new ByteArrayOutputStream();
        final PrintStream previous = System.out;
        final AllPairs actual;
        try (PrintStream captured = new PrintStream(output)) {
            System.setOut(captured);
            actual = new AllPairs.AllPairsBuilder()
                    .withParameter(TestData.OS)
                    .withParameter(drive)
                    .withParameter(new Parameter(TestData.WINDOWS_EDITION).withConstraint(c -> !c.get("OS").equals("Windows")))
                    .withAbsenceCoverage(absenceCoverage)
                    .printGenerationDetails(true)
                    .build();
        } finally {
            System.setOut(previous);
        }
        // Check case headers, not combination lines that already displayed absent values.
        final String caseHeaders = output.toString().lines().filter(line -> line.startsWith("Case "))
                .collect(Collectors.joining(System.lineSeparator()));
        for (String text : List.of(actual.toString(), caseHeaders, output.toString())) {
            assertThat(text).contains("Drive=null");
            if (absenceCoverage) {
                assertThat(text).contains("Drive=<absent>", "WindowsEdition=<absent>");
            } else {
                assertThat(text).doesNotContain("<absent>");
            }
        }
        assertThat(actual.getGeneratedCases()).anySatisfy(c -> assertThat(c).doesNotContainKey("WindowsEdition"));
    }

    @Test
    void shouldCoverAbsenceByDefaultAndAllowToggling() {
        final AllPairs.AllPairsBuilder builder = new AllPairs.AllPairsBuilder()
                .withParameters(TestData.OPTIONAL_PARAMETERS);
        final List<Case> defaultCases = builder.build().getGeneratedCases();
        assertThat(defaultCases).containsExactlyInAnyOrder(
                new Case("B", "b"),
                new Case("A", null, "B", "b"),
                new Case("A", 1, "B", "b")
        );
        assertThat(builder.withAbsenceCoverage(false).build().getGeneratedCases()).containsExactlyInAnyOrder(
                new Case("A", null, "B", "b"),
                new Case("A", 1, "B", "b")
        );
        assertThat(builder.withAbsenceCoverage(true).build().getGeneratedCases()).containsExactlyElementsOf(defaultCases);
    }

    @Test
    void shouldCoverPresentSinglesWhenNoPresentPairIsFeasible() {
        final AllPairs allPairs = new AllPairs.AllPairsBuilder()
                .withParameter(new Parameter("A", Parameter.ABSENT, 0))
                .withParameter(new Parameter("B", 1, Parameter.ABSENT))
                .withConstraint(c -> c.isPresent("A") == c.isPresent("B"))
                .withAbsenceCoverage(false)
                .build();
        assertThat(allPairs.getGeneratedCases()).containsExactlyInAnyOrder(new Case("A", 0), new Case("B", 1));
    }

    @Test
    void shouldDistinguishNoCoverageFromNoValidCase() {
        final AllPairs.AllPairsBuilder builder = new AllPairs.AllPairsBuilder()
                .withParameter(new Parameter("A", Parameter.ABSENT).withConstraint(c -> false))
                .withParameter(new Parameter("B", 1).withConstraint(c -> true))
                .withAbsenceCoverage(false);
        assertThat(builder.build().getGeneratedCases()).containsExactly(new Case());
        assertThat(builder.withConstraint(c -> true).build().getGeneratedCases()).isEmpty();
    }

    @Test
    void shouldRespectExplicitAbsenceWhenParameterIsApplicable() {
        final AllPairs allPairs = new AllPairs.AllPairsBuilder()
                .withParameter(new Parameter("A", Parameter.ABSENT, null, 1).withConstraint(c -> !(boolean) c.get("enabled")))
                .withParameter(new Parameter("enabled", false, true))
                .build();
        assertThat(allPairs.getGeneratedCases()).containsExactlyInAnyOrder(
                new Case("enabled", false), new Case("enabled", true),
                new Case("enabled", true, "A", null), new Case("enabled", true, "A", 1));
    }

    @Test
    void shouldSkipValueConstraintForExplicitAbsenceButNotForNull() {
        final AllPairs allPairs = new AllPairs.AllPairsBuilder()
                .withParameters(TestData.OPTIONAL_PARAMETERS)
                .withConstraint(c -> c.get("A") == null)
                .build();
        assertThat(allPairs.getGeneratedCases()).containsExactlyInAnyOrder(
                new Case("B", "b"),
                new Case("A", 1, "B", "b")
        );
    }
}
