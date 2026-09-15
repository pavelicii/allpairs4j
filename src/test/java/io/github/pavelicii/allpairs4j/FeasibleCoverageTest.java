package io.github.pavelicii.allpairs4j;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.Collections;
import java.util.List;
import java.util.function.Predicate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;

/**
 * Checks that required combinations fit in complete cases allowed by all constraints together.
 * Covers indirectly excluded combinations, unsatisfiable models, constraint errors, and unrelated parameters.
 */
@Timeout(15)
@SuppressWarnings("checkstyle:MultipleStringLiterals")
class FeasibleCoverageTest {

    /** Since A can only be 0, the B=0, C=0 pair cannot appear in any valid case. */
    @Test
    void shouldExcludePairWithoutFullCompletionWithSingletonParameter() {
        final AllPairs allPairs = new AllPairs.AllPairsBuilder()
                .withParameter(new Parameter("A", 0))
                .withParameter(new Parameter("B", 0, 1))
                .withParameter(new Parameter("C", 0, 1))
                .withConstraint(c -> c.get("A").equals(0) && c.get("B").equals(0) && c.get("C").equals(0))
                .build();

        assertThat(allPairs.getGeneratedCases()).containsExactlyInAnyOrder(
                new Case("A", 0, "B", 0, "C", 1),
                new Case("A", 0, "B", 1, "C", 0),
                new Case("A", 0, "B", 1, "C", 1)
        );
    }

    /**
     * Allows cases where A=B=C, unless another constraint also requires A!=C.
     *
     * @param contradictory whether to add the conflicting constraint
     */
    @ParameterizedTest
    @ValueSource(booleans = {false, true})
    void shouldResolveTransitiveEquality(boolean contradictory) {
        final AllPairs allPairs = new AllPairs.AllPairsBuilder()
                .withParameter(new Parameter("A", 0, 1))
                .withParameter(new Parameter("B", 0, 1))
                .withParameter(new Parameter("C", 0, 1))
                .withConstraint(c -> !c.get("A").equals(c.get("B")))
                .withConstraint(c -> !c.get("B").equals(c.get("C")))
                .withConstraint(c -> contradictory && c.get("A").equals(c.get("C")))
                .build();

        assertThat(allPairs.getGeneratedCases()).containsExactlyInAnyOrderElementsOf(contradictory
                ? Collections.emptyList()
                : List.of(new Case("A", 0, "B", 0, "C", 0), new Case("A", 1, "B", 1, "C", 1))
        );
    }

    /** Checks a constraint that reads different parameters depending on A's value. */
    @Test
    void shouldResolveValueDependentReads() {
        final AllPairs allPairs = new AllPairs.AllPairsBuilder()
                .withParameter(new Parameter("A", 0, 1))
                .withParameter(new Parameter("B", 0, 1))
                .withParameter(new Parameter("C", 0, 1))
                .withParameter(new Parameter("D", 0, 1))
                .withParameter(new Parameter("E", 0, 1))
                .withConstraint(c -> c.get("A").equals(0)
                        ? !c.get("B").equals(c.get("C"))
                        : !c.get("D").equals(c.get("E")))
                .withConstraint(c -> c.get("C").equals(0) || c.get("D").equals(1))
                .withConstraint(c -> c.get("B").equals(0) ^ c.get("E").equals(1))
                .build();

        assertThat(allPairs.getGeneratedCases()).containsExactlyInAnyOrder(
                new Case("A", 0, "B", 1, "C", 1, "D", 0, "E", 0),
                new Case("A", 1, "B", 1, "C", 1, "D", 0, "E", 0));
    }

    @ParameterizedTest
    @ValueSource(booleans = {false, true})
    void shouldPreserveConstraintFailureContext(boolean parameterConstraint) {
        final RuntimeException cause = new IllegalStateException("Constraint failed");
        final Predicate<ConstrainableCase> constraint = c -> {
            c.get("OS");
            throw cause;
        };
        final AllPairs.AllPairsBuilder builder = new AllPairs.AllPairsBuilder().withParameters(TestData.PARAMETERS);
        if (parameterConstraint) {
            builder.withParameter(new Parameter(TestData.WINDOWS_EDITION).withConstraint(constraint));
        } else {
            builder.withConstraint(constraint);
        }
        assertThatExceptionOfType(IllegalArgumentException.class).isThrownBy(builder::build)
                .withMessage(parameterConstraint
                        ? "Error in parameter constraint for 'WindowsEdition'" : "Error in case constraint")
                .withCause(cause);
    }

    /** Rejecting an early choice must not make the search try every value of unrelated parameters. */
    @Test
    void shouldCompleteFixedEqualityWithUnrelatedParameters() {
        final AllPairs allPairs = new AllPairs.AllPairsBuilder()
                .withParameter(new Parameter("A", 0, 1))
                .withParameters(TestData.numberedParameters(24, "D", 0, 1))
                .withParameter(new Parameter("B", 0, 1))
                .withParameter(new Parameter("C", 0, 1))
                .withConstraint(c -> !c.get("A").equals(c.get("B")))
                .withConstraint(c -> !c.get("B").equals(c.get("C")))
                .withConstraint(c -> c.get("C").equals(0))
                .build();

        assertThat(allPairs.getGeneratedCases()).isNotEmpty().allSatisfy(row -> assertThat(row)
                .hasSize(allPairs.getParameters().size())
                .containsAllEntriesOf(new Case("A", 1, "B", 1, "C", 1)));
    }

    @Test
    void shouldPreserveCoverageAfterCompletionBudgetIsExhausted() {
        final List<Predicate<ConstrainableCase>> constraints = List.of(c -> !c.get("P0").equals(c.get("P1")));
        final CaseCompleter completer = new CaseCompleter(List.of(
                List.of(new Item(0, 0, 0, 0, "P0"), new Item(0, 1, 1, 1, "P0")),
                List.of(new Item(1, 0, 2, 0, "P1"), new Item(1, 1, 3, 1, "P1"))), constraints);
        final CaseCompleter.CheckBudget budget = new CaseCompleter.CheckBudget(1);
        assertThat(completer.complete(Collections.emptyList(), budget)).isNull();
        assertThat(budget.getRemainingChecks()).isZero();
        assertThat(completer.complete(Collections.emptyList(), budget)).isNull();
        assertThat(new Case(completer.complete(Collections.emptyList()))).isEqualTo(new Case("P0", 0, "P1", 0));
        final AllPairs actual = new AllPairs.AllPairsBuilder()
                .withParameters(TestData.numberedParameters(2, "P", 0, 1))
                .withConstraints(constraints)
                .build();
        assertThat(actual.getGeneratedCases())
                .containsExactlyInAnyOrder(new Case("P0", 0, "P1", 0), new Case("P0", 1, "P1", 1));
    }
}
