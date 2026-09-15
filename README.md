# AllPairs4J

[![GitHub Workflow Status](https://img.shields.io/github/actions/workflow/status/pavelicii/allpairs4j/build-checkstyle.yaml?branch=master&logo=GitHub)](https://github.com/pavelicii/allpairs4j/actions/workflows/build-checkstyle.yaml)
[![Maven Central](https://img.shields.io/maven-central/v/io.github.pavelicii/allpairs4j)](https://search.maven.org/artifact/io.github.pavelicii/allpairs4j)

AllPairs4J is an open source Java library that generates a compact set of test cases with n-wise coverage.

## Pairwise Testing • [pairwise.org](https://www.pairwise.org/)

Assuming you want to create test cases for web browser testing, the domain can be described with parameters:

```text
Browser:    Chrome, Firefox, Safari, Edge
OS:         Windows, Linux, macOS
RAM:        1024, 2048, 4096, 8192, 16384
Drive:      HDD, SSD
Screen:     1024x768, 1366x768, 1680x1050, 1920x1080, 2560x1440, 3840x2160
```

Testing every possible case would mean hundreds of tests. Many bugs involve just one or two parameters,
so covering every pair can be a useful alternative to testing every possible case.
For example, `{Chrome, Windows}` and `{4096, SSD}` are two parameter-value combinations (pairs). A single test case,
`{Chrome, Windows, 4096, SSD, 2560x1440}`, covers both and several other pairs too.
By combining pairs this way, you can test them all with far fewer cases.

## Features

* Add **case constraints** to exclude cases that don't make sense for your tests (none by default).\
  For example, allow `Browser=Safari` only with `OS=macOS`, and `Browser=Edge` only with `OS=Windows`.
* Add **parameter constraints** to exclude parameters from cases where they don't apply.\
  For example, if we add a `WindowsEdition` parameter, include it only with `OS=Windows`.
* Make parameters **optional** with `Parameter.ABSENT`, keeping a missing parameter different from a `null` value.
* Generate test cases with **pair**wise, **triple**wise, **n**-wise coverage (pairwise by default).
* Choose whether combinations with **missing parameters** need to be covered (on by default).
* Turn on **randomization** to get more variety in your test cases (off by default).
* Print **generation details** to see the generated cases and the combinations they cover (off by default).

## Installation

### Requirements

Java 8 or higher.

### Gradle

```groovy
dependencies {
    implementation("io.github.pavelicii:allpairs4j:2.0.0")
}
```

### Maven

```xml
<dependency>
    <groupId>io.github.pavelicii</groupId>
    <artifactId>allpairs4j</artifactId>
    <version>2.0.0</version>
</dependency>
```

## Usage

### Generate Pairwise Test Cases

Use `AllPairsBuilder` to define parameters and their possible values, then call `build()` to generate test cases.
By default, the cases cover every pair of parameter values that can occur in a valid case.

#### Sample code:

```java
AllPairs allPairs = new AllPairs.AllPairsBuilder()
        .withParameters(Arrays.asList(
                new Parameter("Browser", "Chrome", "Safari", "Edge"),
                new Parameter("OS", "Windows", "Linux", "macOS"),
                new Parameter("RAM", 2048, 4096, 8192, 16384),
                new Parameter("Drive", "HDD", "SSD")))
        .build();

System.out.println(allPairs);
```

#### Output:

<details><summary>Show</summary>

```text
  1: {Browser=Chrome, OS=Windows, RAM=2048, Drive=HDD}
  2: {Browser=Safari, OS=Linux, RAM=4096, Drive=HDD}
  3: {Browser=Edge, OS=macOS, RAM=8192, Drive=HDD}
  4: {Browser=Edge, OS=Linux, RAM=16384, Drive=SSD}
  5: {Browser=Safari, OS=Windows, RAM=16384, Drive=SSD}
  6: {Browser=Chrome, OS=macOS, RAM=4096, Drive=SSD}
  7: {Browser=Safari, OS=macOS, RAM=2048, Drive=SSD}
  8: {Browser=Edge, OS=Windows, RAM=4096, Drive=HDD}
  9: {Browser=Safari, OS=macOS, RAM=16384, Drive=HDD}
 10: {Browser=Chrome, OS=Linux, RAM=16384, Drive=SSD}
 11: {Browser=Safari, OS=Linux, RAM=8192, Drive=SSD}
 12: {Browser=Chrome, OS=Windows, RAM=8192, Drive=HDD}
 13: {Browser=Edge, OS=Linux, RAM=2048, Drive=HDD}
```

</details>

### Add Case Constraints

* Use `builder.withConstraint(predicate)` to exclude unwanted cases. Return `true` to exclude a case; `false` means this constraint doesn't exclude it.
* If any case constraint excludes a case, AllPairs4J looks for another one while generating cases.
* Use it only to check values, not to change data or perform other actions.

For example, add case constraints so that:

* `Browser=Safari` is tested only with `OS=macOS`.
* `Browser=Edge` is tested only with `OS=Windows`.
* `RAM` must be at least `4000`.

#### Sample code:

```java
AllPairs allPairs = new AllPairs.AllPairsBuilder()
        .withParameter(new Parameter("Browser", "Chrome", "Safari", "Edge"))
        .withParameter(new Parameter("OS", "Windows", "Linux", "macOS"))
        .withParameter(new Parameter("RAM", 2048, 4096, 8192, 16384))
        .withParameter(new Parameter("Drive", "HDD", "SSD"))
        .withConstraint(c -> c.get("Browser").equals("Safari") && !c.get("OS").equals("macOS"))
        .withConstraint(c -> c.get("Browser").equals("Edge") && !c.get("OS").equals("Windows"))
        .withConstraint(c -> (int) c.get("RAM") < 4000)
        .build();

System.out.println(allPairs);
```

#### Output:

<details><summary>Show</summary>

```text
  1: {Browser=Chrome, OS=Windows, RAM=4096, Drive=HDD}
  2: {Browser=Safari, OS=macOS, RAM=8192, Drive=HDD}
  3: {Browser=Edge, OS=Windows, RAM=16384, Drive=SSD}
  4: {Browser=Edge, OS=Windows, RAM=8192, Drive=SSD}
  5: {Browser=Safari, OS=macOS, RAM=4096, Drive=SSD}
  6: {Browser=Chrome, OS=Linux, RAM=16384, Drive=HDD}
  7: {Browser=Edge, OS=Windows, RAM=4096, Drive=HDD}
  8: {Browser=Chrome, OS=macOS, RAM=16384, Drive=SSD}
  9: {Browser=Safari, OS=macOS, RAM=16384, Drive=HDD}
 10: {Browser=Chrome, OS=Linux, RAM=8192, Drive=SSD}
 11: {Browser=Chrome, OS=Linux, RAM=4096, Drive=SSD}
```

</details>

### Optional Values and Absence Coverage

* Add `Parameter.ABSENT` to let a parameter be left out of some cases.
  `AllPairs.toString()` and generation details show `<absent>` only when absence coverage is enabled.
  The resulting `Case` has no key for an absent parameter in either mode.
* `null` is different: the parameter is still included, with `null` as its value.
* `withAbsenceCoverage(true)` (default) requires cases to also cover combinations where optional parameters are missing.
* `withAbsenceCoverage(false)` doesn't require coverage of those combinations, but parameters can still be left out of cases.

#### Sample code: absence coverage enabled

```java
AllPairs allPairs = new AllPairs.AllPairsBuilder()
        .withParameter(new Parameter("Browser", "Chrome", "Firefox"))
        .withParameter(new Parameter("OS", "Windows", "Linux"))
        .withParameter(new Parameter("RAM", 8192, 16384))
        .withParameter(new Parameter("Drive", Parameter.ABSENT, null, "SSD"))
        .withAbsenceCoverage(true)
        .build();

System.out.println(allPairs);
```

#### Output:

<details><summary>Show</summary>

```text
  1: {Browser=Chrome, OS=Windows, RAM=8192, Drive=<absent>}
  2: {Browser=Chrome, OS=Linux, RAM=16384, Drive=null}
  3: {Browser=Chrome, OS=Linux, RAM=8192, Drive=SSD}
  4: {Browser=Firefox, OS=Linux, RAM=16384, Drive=<absent>}
  5: {Browser=Firefox, OS=Windows, RAM=8192, Drive=null}
  6: {Browser=Firefox, OS=Windows, RAM=16384, Drive=SSD}
```

</details>

#### Sample code: absence coverage disabled

```java
AllPairs allPairs = new AllPairs.AllPairsBuilder()
        .withParameter(new Parameter("Browser", "Chrome", "Firefox"))
        .withParameter(new Parameter("OS", "Windows", "Linux"))
        .withParameter(new Parameter("RAM", 8192, 16384))
        .withParameter(new Parameter("Drive", Parameter.ABSENT, null, "SSD"))
        .withAbsenceCoverage(false)
        .build();

System.out.println(allPairs);
```

#### Output:

<details><summary>Show</summary>

```text
  1: {Browser=Chrome, OS=Windows, RAM=8192, Drive=null}  // null is still tested; it does not mean Drive is missing.
  2: {Browser=Chrome, OS=Linux, RAM=16384, Drive=SSD}
  3: {Browser=Firefox, OS=Windows, RAM=16384, Drive=SSD}
  4: {Browser=Firefox, OS=Linux, RAM=8192, Drive=SSD}
  5: {Browser=Firefox, OS=Linux, RAM=16384, Drive=null}
```

</details>

### Add Parameter Constraints

* `parameter.withConstraint(predicate)` excludes the parameter from a case when the predicate is `true`.
  Otherwise, the parameter is required unless its values include `Parameter.ABSENT`.
  Add the parameter via `withParameter` or `withParameters`.
* Without a parameter constraint or `Parameter.ABSENT`, the parameter is always included.
* `c.isPresent(name)` checks whether a parameter is present, not whether its value is non-null.
  It returns `false` for an absent parameter, and `true` for any included value, even `null`.
* `c.get(name)` returns the value. Reading an absent value excludes the parameter whose constraint is being checked,
  or skips the current case constraint.

For example, `WindowsEdition` (`Home` or `Pro`) is required only when `OS=Windows`.

#### Sample code:

```java
AllPairs allPairs = new AllPairs.AllPairsBuilder()
        .withParameter(new Parameter("Browser", "Chrome", "Safari", "Edge"))
        .withParameter(new Parameter("OS", "Windows", "Linux", "macOS"))
        .withParameter(new Parameter("RAM", 2048, 4096, 8192, 16384))
        .withParameter(new Parameter("Drive", "HDD", "SSD"))
        .withParameter(new Parameter("WindowsEdition", "Home", "Pro").withConstraint(c -> !c.get("OS").equals("Windows")))
        .build();

System.out.println(allPairs);
```

#### Output:

<details><summary>Show</summary>

```text
  1: {Browser=Chrome, OS=Windows, RAM=2048, Drive=HDD, WindowsEdition=Home}
  2: {Browser=Chrome, OS=Linux, RAM=4096, Drive=SSD, WindowsEdition=<absent>}
  3: {Browser=Chrome, OS=macOS, RAM=8192, Drive=SSD, WindowsEdition=<absent>}
  4: {Browser=Chrome, OS=Windows, RAM=16384, Drive=SSD, WindowsEdition=Pro}
  5: {Browser=Safari, OS=macOS, RAM=2048, Drive=HDD, WindowsEdition=<absent>}
  6: {Browser=Safari, OS=Windows, RAM=4096, Drive=HDD, WindowsEdition=Pro}
  7: {Browser=Safari, OS=Linux, RAM=8192, Drive=HDD, WindowsEdition=<absent>}
  8: {Browser=Edge, OS=Windows, RAM=2048, Drive=SSD, WindowsEdition=Pro}
  9: {Browser=Edge, OS=Windows, RAM=8192, Drive=HDD, WindowsEdition=Home}
 10: {Browser=Edge, OS=Linux, RAM=2048, Drive=SSD, WindowsEdition=<absent>}
 11: {Browser=Edge, OS=Windows, RAM=4096, Drive=SSD, WindowsEdition=Home}
 12: {Browser=Safari, OS=Linux, RAM=16384, Drive=HDD, WindowsEdition=<absent>}
 13: {Browser=Edge, OS=macOS, RAM=4096, Drive=HDD, WindowsEdition=<absent>}
 14: {Browser=Safari, OS=Windows, RAM=8192, Drive=SSD, WindowsEdition=Pro}
 15: {Browser=Edge, OS=macOS, RAM=16384, Drive=SSD, WindowsEdition=<absent>}
 16: {Browser=Safari, OS=Windows, RAM=16384, Drive=HDD, WindowsEdition=Home}
```

</details>

### Generate Triplewise Test Cases

Set `withTestCombinationSize(n)` to generate test cases covering `n`-parameter combinations instead of pairs.

#### Sample code:

```java
AllPairs allPairs = new AllPairs.AllPairsBuilder()
        .withTestCombinationSize(3)
        .withParameters(Arrays.asList(
                new Parameter("Browser", "Chrome", "Safari", "Edge"),
                new Parameter("OS", "Windows", "Linux", "macOS"),
                new Parameter("RAM", 2048, 4096, 8192, 16384),
                new Parameter("Drive", "HDD", "SSD")))
        .build();

System.out.println(allPairs);
```

#### Output:

<details><summary>Show</summary>

```text
  1: {Browser=Safari, OS=Linux, RAM=4096, Drive=HDD}
  2: {Browser=Edge, OS=macOS, RAM=8192, Drive=HDD}
  3: {Browser=Edge, OS=macOS, RAM=16384, Drive=SSD}
  4: {Browser=Safari, OS=Linux, RAM=16384, Drive=SSD}
  5: {Browser=Chrome, OS=Windows, RAM=4096, Drive=HDD}
  6: {Browser=Edge, OS=macOS, RAM=4096, Drive=HDD}
  7: {Browser=Chrome, OS=Windows, RAM=16384, Drive=SSD}
  8: {Browser=Edge, OS=macOS, RAM=2048, Drive=SSD}
  9: {Browser=Safari, OS=Linux, RAM=8192, Drive=HDD}
 10: {Browser=Chrome, OS=Windows, RAM=2048, Drive=SSD}
 11: {Browser=Safari, OS=Linux, RAM=2048, Drive=SSD}
 12: {Browser=Chrome, OS=Windows, RAM=8192, Drive=HDD}
 13: {Browser=Chrome, OS=Linux, RAM=2048, Drive=HDD}
 14: {Browser=Chrome, OS=Linux, RAM=4096, Drive=SSD}
 15: {Browser=Chrome, OS=Linux, RAM=8192, Drive=SSD}
 16: {Browser=Chrome, OS=Linux, RAM=16384, Drive=HDD}
 17: {Browser=Chrome, OS=macOS, RAM=2048, Drive=HDD}
 18: {Browser=Chrome, OS=macOS, RAM=4096, Drive=SSD}
 19: {Browser=Chrome, OS=macOS, RAM=8192, Drive=SSD}
 20: {Browser=Chrome, OS=macOS, RAM=16384, Drive=HDD}
 21: {Browser=Safari, OS=Windows, RAM=2048, Drive=HDD}
 22: {Browser=Safari, OS=Windows, RAM=4096, Drive=SSD}
 23: {Browser=Safari, OS=Windows, RAM=8192, Drive=SSD}
 24: {Browser=Safari, OS=Windows, RAM=16384, Drive=HDD}
 25: {Browser=Safari, OS=macOS, RAM=2048, Drive=HDD}
 26: {Browser=Safari, OS=macOS, RAM=4096, Drive=SSD}
 27: {Browser=Safari, OS=macOS, RAM=8192, Drive=SSD}
 28: {Browser=Safari, OS=macOS, RAM=16384, Drive=HDD}
 29: {Browser=Edge, OS=Windows, RAM=2048, Drive=HDD}
 30: {Browser=Edge, OS=Windows, RAM=16384, Drive=SSD}
 31: {Browser=Edge, OS=Windows, RAM=8192, Drive=SSD}
 32: {Browser=Edge, OS=Windows, RAM=4096, Drive=HDD}
 33: {Browser=Edge, OS=Linux, RAM=2048, Drive=HDD}
 34: {Browser=Edge, OS=Linux, RAM=4096, Drive=SSD}
 35: {Browser=Edge, OS=Linux, RAM=8192, Drive=SSD}
 36: {Browser=Edge, OS=Linux, RAM=16384, Drive=HDD}
```

</details>

### Randomized Generation

* By default, the same inputs in the same order produce the same cases.
* Use `withRandomization(true)` to vary generated cases while preserving constraints and required coverage.
  Results can repeat: random choices between equally ranked values may coincide, or constraints may leave no alternatives.
* Use `withRandomization(true, 42L)` for repeatable results with the same inputs, settings and library version.
* `getRandomizationSeed()` returns the seed used for generation; `toBuilder()` preserves it.

```java
AllPairs allPairs = new AllPairs.AllPairsBuilder()
        .withParameter(new Parameter("Browser", "Chrome", "Safari", "Edge"))
        .withParameter(new Parameter("OS", "Windows", "Linux", "macOS"))
        .withParameter(new Parameter("RAM", 2048, 4096, 8192, 16384))
        .withRandomization(true)
        .build();
```

### Generation Feedback

Turn on `printGenerationDetails(true)` to see the generated cases, the combinations they cover and their counts (off by default).

```java
AllPairs allPairs = new AllPairs.AllPairsBuilder()
        .withParameter(new Parameter("Browser", "Chrome", "Safari", "Edge"))
        .withParameter(new Parameter("OS", "Windows", "Linux", "macOS"))
        .withParameter(new Parameter("RAM", 2048, 4096, 8192, 16384))
        .withParameter(new Parameter("Drive", "HDD", "SSD"))
        .withParameter(new Parameter("WindowsEdition", "Home", "Pro").withConstraint(c -> !c.get("OS").equals("Windows")))
        .withConstraint(c -> c.get("Browser").equals("Safari") && !c.get("OS").equals("macOS"))
        .withConstraint(c -> c.get("Browser").equals("Edge") && !c.get("OS").equals("Windows"))
        .printGenerationDetails(true)
        .build();
```

Each case lists only combinations not covered by earlier cases.

<details><summary>Show output</summary>

```text
=== Required combinations (77) ===
  {Browser=Chrome, OS=Windows}
  {Browser=Chrome, OS=Linux}
  {Browser=Chrome, OS=macOS}
  {Browser=Safari, OS=macOS}
  {Browser=Edge, OS=Windows}
  {Browser=Chrome, RAM=2048}
  {Browser=Chrome, RAM=4096}
  {Browser=Chrome, RAM=8192}
  {Browser=Chrome, RAM=16384}
  {Browser=Safari, RAM=2048}
  {Browser=Safari, RAM=4096}
  {Browser=Safari, RAM=8192}
  {Browser=Safari, RAM=16384}
  {Browser=Edge, RAM=2048}
  {Browser=Edge, RAM=4096}
  {Browser=Edge, RAM=8192}
  {Browser=Edge, RAM=16384}
  {Browser=Chrome, Drive=HDD}
  {Browser=Chrome, Drive=SSD}
  {Browser=Safari, Drive=HDD}
  {Browser=Safari, Drive=SSD}
  {Browser=Edge, Drive=HDD}
  {Browser=Edge, Drive=SSD}
  {Browser=Chrome, WindowsEdition=Home}
  {Browser=Chrome, WindowsEdition=Pro}
  {Browser=Chrome, WindowsEdition=<absent>}
  {Browser=Safari, WindowsEdition=<absent>}
  {Browser=Edge, WindowsEdition=Home}
  {Browser=Edge, WindowsEdition=Pro}
  {OS=Windows, RAM=2048}
  {OS=Windows, RAM=4096}
  {OS=Windows, RAM=8192}
  {OS=Windows, RAM=16384}
  {OS=Linux, RAM=2048}
  {OS=Linux, RAM=4096}
  {OS=Linux, RAM=8192}
  {OS=Linux, RAM=16384}
  {OS=macOS, RAM=2048}
  {OS=macOS, RAM=4096}
  {OS=macOS, RAM=8192}
  {OS=macOS, RAM=16384}
  {OS=Windows, Drive=HDD}
  {OS=Windows, Drive=SSD}
  {OS=Linux, Drive=HDD}
  {OS=Linux, Drive=SSD}
  {OS=macOS, Drive=HDD}
  {OS=macOS, Drive=SSD}
  {OS=Windows, WindowsEdition=Home}
  {OS=Windows, WindowsEdition=Pro}
  {OS=Linux, WindowsEdition=<absent>}
  {OS=macOS, WindowsEdition=<absent>}
  {RAM=2048, Drive=HDD}
  {RAM=2048, Drive=SSD}
  {RAM=4096, Drive=HDD}
  {RAM=4096, Drive=SSD}
  {RAM=8192, Drive=HDD}
  {RAM=8192, Drive=SSD}
  {RAM=16384, Drive=HDD}
  {RAM=16384, Drive=SSD}
  {RAM=2048, WindowsEdition=Home}
  {RAM=2048, WindowsEdition=Pro}
  {RAM=2048, WindowsEdition=<absent>}
  {RAM=4096, WindowsEdition=Home}
  {RAM=4096, WindowsEdition=Pro}
  {RAM=4096, WindowsEdition=<absent>}
  {RAM=8192, WindowsEdition=Home}
  {RAM=8192, WindowsEdition=Pro}
  {RAM=8192, WindowsEdition=<absent>}
  {RAM=16384, WindowsEdition=Home}
  {RAM=16384, WindowsEdition=Pro}
  {RAM=16384, WindowsEdition=<absent>}
  {Drive=HDD, WindowsEdition=Home}
  {Drive=HDD, WindowsEdition=Pro}
  {Drive=HDD, WindowsEdition=<absent>}
  {Drive=SSD, WindowsEdition=Home}
  {Drive=SSD, WindowsEdition=Pro}
  {Drive=SSD, WindowsEdition=<absent>}

=== Generated cases (17) ===

Case 1: {Browser=Chrome, OS=Windows, RAM=2048, Drive=HDD, WindowsEdition=Home}
  New combinations:
    {Browser=Chrome, OS=Windows}
    {Browser=Chrome, RAM=2048}
    {Browser=Chrome, Drive=HDD}
    {Browser=Chrome, WindowsEdition=Home}
    {OS=Windows, RAM=2048}
    {OS=Windows, Drive=HDD}
    {OS=Windows, WindowsEdition=Home}
    {RAM=2048, Drive=HDD}
    {RAM=2048, WindowsEdition=Home}
    {Drive=HDD, WindowsEdition=Home}

Case 2: {Browser=Chrome, OS=Linux, RAM=4096, Drive=SSD, WindowsEdition=<absent>}
  New combinations:
    {Browser=Chrome, OS=Linux}
    {Browser=Chrome, RAM=4096}
    {Browser=Chrome, Drive=SSD}
    {Browser=Chrome, WindowsEdition=<absent>}
    {OS=Linux, RAM=4096}
    {OS=Linux, Drive=SSD}
    {OS=Linux, WindowsEdition=<absent>}
    {RAM=4096, Drive=SSD}
    {RAM=4096, WindowsEdition=<absent>}
    {Drive=SSD, WindowsEdition=<absent>}

Case 3: {Browser=Chrome, OS=macOS, RAM=8192, Drive=SSD, WindowsEdition=<absent>}
  New combinations:
    {Browser=Chrome, OS=macOS}
    {Browser=Chrome, RAM=8192}
    {OS=macOS, RAM=8192}
    {OS=macOS, Drive=SSD}
    {OS=macOS, WindowsEdition=<absent>}
    {RAM=8192, Drive=SSD}
    {RAM=8192, WindowsEdition=<absent>}

Case 4: {Browser=Chrome, OS=Windows, RAM=16384, Drive=SSD, WindowsEdition=Pro}
  New combinations:
    {Browser=Chrome, RAM=16384}
    {Browser=Chrome, WindowsEdition=Pro}
    {OS=Windows, RAM=16384}
    {OS=Windows, Drive=SSD}
    {OS=Windows, WindowsEdition=Pro}
    {RAM=16384, Drive=SSD}
    {RAM=16384, WindowsEdition=Pro}
    {Drive=SSD, WindowsEdition=Pro}

Case 5: {Browser=Safari, OS=macOS, RAM=2048, Drive=HDD, WindowsEdition=<absent>}
  New combinations:
    {Browser=Safari, OS=macOS}
    {Browser=Safari, RAM=2048}
    {Browser=Safari, Drive=HDD}
    {Browser=Safari, WindowsEdition=<absent>}
    {OS=macOS, RAM=2048}
    {OS=macOS, Drive=HDD}
    {RAM=2048, WindowsEdition=<absent>}
    {Drive=HDD, WindowsEdition=<absent>}

Case 6: {Browser=Safari, OS=macOS, RAM=4096, Drive=HDD, WindowsEdition=<absent>}
  New combinations:
    {Browser=Safari, RAM=4096}
    {OS=macOS, RAM=4096}
    {RAM=4096, Drive=HDD}

Case 7: {Browser=Edge, OS=Windows, RAM=2048, Drive=SSD, WindowsEdition=Pro}
  New combinations:
    {Browser=Edge, OS=Windows}
    {Browser=Edge, RAM=2048}
    {Browser=Edge, Drive=SSD}
    {Browser=Edge, WindowsEdition=Pro}
    {RAM=2048, Drive=SSD}
    {RAM=2048, WindowsEdition=Pro}

Case 8: {Browser=Edge, OS=Windows, RAM=4096, Drive=HDD, WindowsEdition=Home}
  New combinations:
    {Browser=Edge, RAM=4096}
    {Browser=Edge, Drive=HDD}
    {Browser=Edge, WindowsEdition=Home}
    {OS=Windows, RAM=4096}
    {RAM=4096, WindowsEdition=Home}

Case 9: {Browser=Edge, OS=Windows, RAM=8192, Drive=HDD, WindowsEdition=Pro}
  New combinations:
    {Browser=Edge, RAM=8192}
    {OS=Windows, RAM=8192}
    {RAM=8192, Drive=HDD}
    {RAM=8192, WindowsEdition=Pro}
    {Drive=HDD, WindowsEdition=Pro}

Case 10: {Browser=Chrome, OS=Linux, RAM=2048, Drive=HDD, WindowsEdition=<absent>}
  New combinations:
    {OS=Linux, RAM=2048}
    {OS=Linux, Drive=HDD}

Case 11: {Browser=Edge, OS=Windows, RAM=4096, Drive=HDD, WindowsEdition=Pro}
  New combinations:
    {RAM=4096, WindowsEdition=Pro}

Case 12: {Browser=Safari, OS=macOS, RAM=8192, Drive=SSD, WindowsEdition=<absent>}
  New combinations:
    {Browser=Safari, RAM=8192}
    {Browser=Safari, Drive=SSD}

Case 13: {Browser=Chrome, OS=Linux, RAM=8192, Drive=SSD, WindowsEdition=<absent>}
  New combinations:
    {OS=Linux, RAM=8192}

Case 14: {Browser=Edge, OS=Windows, RAM=8192, Drive=SSD, WindowsEdition=Home}
  New combinations:
    {RAM=8192, WindowsEdition=Home}
    {Drive=SSD, WindowsEdition=Home}

Case 15: {Browser=Safari, OS=macOS, RAM=16384, Drive=HDD, WindowsEdition=<absent>}
  New combinations:
    {Browser=Safari, RAM=16384}
    {OS=macOS, RAM=16384}
    {RAM=16384, Drive=HDD}
    {RAM=16384, WindowsEdition=<absent>}

Case 16: {Browser=Edge, OS=Windows, RAM=16384, Drive=SSD, WindowsEdition=Home}
  New combinations:
    {Browser=Edge, RAM=16384}
    {RAM=16384, WindowsEdition=Home}

Case 17: {Browser=Chrome, OS=Linux, RAM=16384, Drive=SSD, WindowsEdition=<absent>}
  New combinations:
    {OS=Linux, RAM=16384}
```

</details>

### Configuration Summary

#### Builder:

```java
AllPairs allPairs = new AllPairs.AllPairsBuilder()
        .withParameter( Parameter )                            // adds values and any constraint set on the Parameter
        .withParameters( List<Parameter> )                     // alternative way to specify multiple Parameters as List
        .withConstraint( Predicate<ConstrainableCase> )        // excludes a case when the predicate returns true
        .withConstraints( List<Predicate<ConstrainableCase>> ) // alternative way to specify multiple Constraints as List
        .withTestCombinationSize( int )                        // parameters per covered combination, default 2 (pair)
        .withRandomization( boolean )                          // random tie-breaking, default false
        .withRandomization( boolean, long )                    // same, with a fixed seed
        .withAbsenceCoverage( boolean )                        // covers allowed absence states, default true
        .printGenerationDetails( boolean )                     // prints required combinations and final Cases, default false
        .build();

List<Case> generatedCases = allPairs.getGeneratedCases();      // work with resulting List of Cases
for (Case c : allPairs) { ... }                                // or use Iterator
```

Use `toBuilder()` to start from an existing configuration:

```java
AllPairs randomized = allPairs.toBuilder()
        .withRandomization(true)
        .build();
```

#### Data types:

* **Parameter**: a name and a list of possible values (`List<Object>`). Add `Parameter.ABSENT` to allow it to be left out,
  and use `withConstraint(predicate)` to exclude it when the predicate is `true`.
* **Case**: one generated test case (`Map<String, Object>`), with parameter names as keys and chosen values as values.
* **ConstrainableCase**: the case your constraint checks. Use `get(name)` for a value and `isPresent(name)` to check if it's included.
* **Predicate\<ConstrainableCase\>**: in `withConstraint`, `true` excludes the parameter from a case or excludes the entire case, depending on where the constraint is set.

## Contributing

Pull requests are welcome. For major changes, please open an issue first to discuss what you would like to change.

Please make sure to update tests as appropriate.

[SemVer](https://semver.org/) is used for versioning. For the versions available, 
see the [releases](https://github.com/pavelicii/allpairs4j/releases) on this repository.
