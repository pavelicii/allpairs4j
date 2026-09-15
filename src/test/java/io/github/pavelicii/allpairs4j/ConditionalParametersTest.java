package io.github.pavelicii.allpairs4j;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.Collections;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Checks parameter constraints that read other parameters, different declaration orders,
 * and {@code null} values. Also checks that redundant cases are removed.
 * {@link CoverageOracleTest} checks coverage across combination sizes and presence settings.
 */
@Timeout(15)
@SuppressWarnings("checkstyle:MultipleStringLiterals")
class ConditionalParametersTest {

    @ParameterizedTest
    @ValueSource(booleans = {false, true})
    void shouldRemoveRedundantCasesWithConditionalParameters(boolean absenceCoverage) {
        final AllPairs allPairs = new AllPairs.AllPairsBuilder()
                .withParameters(TestData.PARAMETERS)
                .withParameter(new Parameter("WindowsEdition", TestData.WINDOWS_EDITION)
                        .withConstraint(c -> !c.get("OS").equals("Windows")))
                .withAbsenceCoverage(absenceCoverage).build();
        TestSupport.assertCoverage(allPairs, Collections.singleton("WindowsEdition"), absenceCoverage,
                c -> c.containsKey("WindowsEdition") == c.get("OS").equals("Windows"));
        // Coverage alone would also accept the entire Cartesian product; guard compaction separately.
        assertThat(allPairs.getGeneratedCases()).hasSizeLessThanOrEqualTo(16);
    }

    @Test
    void shouldRequireDriveOnlyForMatchingOsAndBrowser() {
        final AllPairs allPairs = new AllPairs.AllPairsBuilder()
                .withParameter(TestData.OS)
                .withParameter(TestData.BROWSER)
                .withParameter(new Parameter("Drive", TestData.DRIVE)
                        .withConstraint(c -> !c.get("OS").equals("Windows")
                                || !c.get("Browser").equals("Chrome")))
                .withTestCombinationSize(3)
                .build();

        TestSupport.assertCoverage(allPairs, Collections.singleton("Drive"), true,
                c -> c.containsKey("Drive") == (c.get("Browser").equals("Chrome")
                        && c.get("OS").equals("Windows")));
    }

    @Test
    void shouldDistinguishPresentNullFromAbsence() {
        final AllPairs allPairs = new AllPairs.AllPairsBuilder()
                .withParameter(new Parameter("Browser", TestData.BROWSER).withConstraint(c -> c.get("Drive") != null))
                .withParameter(new Parameter("Drive", TestData.NULLABLE_DRIVE)
                        .withConstraint(c -> c.get("OS").equals("macOS")))
                .withParameter(TestData.OS)
                .withConstraint(c -> c.get("OS").equals("Linux") && c.get("Drive") != null)
                .withConstraint(c -> c.get("OS").equals("Windows") && c.get("Drive") == null)
                .build();

        assertThat(allPairs.getGeneratedCases()).containsExactlyInAnyOrder(
                new Case("OS", "Windows", "Drive", "HDD"),
                new Case("OS", "Windows", "Drive", "SSD"),
                new Case("OS", "Linux", "Drive", null, "Browser", "Chrome"),
                new Case("OS", "Linux", "Drive", null, "Browser", "Safari"),
                new Case("OS", "Linux", "Drive", null, "Browser", "Edge"),
                new Case("OS", "macOS")
        );
    }
}
