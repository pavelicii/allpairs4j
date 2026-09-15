# AllPairs4J

[![GitHub Workflow Status](https://img.shields.io/github/actions/workflow/status/pavelicii/allpairs4j/build-checkstyle.yaml?branch=master&logo=GitHub)](https://github.com/pavelicii/allpairs4j/actions/workflows/build-checkstyle.yaml)
[![Maven Central](https://img.shields.io/maven-central/v/io.github.pavelicii/allpairs4j)](https://central.sonatype.com/artifact/io.github.pavelicii/allpairs4j)

AllPairs4J is an open source Java library that generates a compact set of test cases with n-wise coverage.

## Pairwise Testing • [pairwise.org](https://www.pairwise.org/)

Assuming you want to create test cases for web browser testing, the domain can be described with parameters:

```text
Browser:    Chrome, Safari, Edge
OS:         Windows, Linux, macOS
RAM:        4096, 8192, 16384
Drive:      HDD, SSD
```

Testing every possible case would mean hundreds of tests. Many bugs involve just one or two parameters,
so covering every pair can be a useful alternative to testing every possible case.
For example, `{Chrome, Windows}` and `{4096, SSD}` are two parameter-value combinations (pairs). A single test case,
`{Chrome, Windows, 4096, SSD}`, covers both and several other pairs too.
By combining pairs this way, you can test them all with far fewer cases.

## Features

* Add **case constraints** to exclude cases that don't make sense for your tests (none by default).\
  For example, allow `Browser=Safari` only with `OS=macOS`, and `Browser=Edge` only with `OS=Windows`.
* Add **parameter constraints** to exclude parameters from cases where they don't apply.\
  For example, if we add a `WindowsEdition` parameter, include it only with `OS=Windows`.
* Make parameters **optional** with `Parameter.ABSENT`, keeping an absent parameter different from a `null` value.
* Choose whether combinations with **absent parameters** need to be covered (on by default).
* Generate test cases with **pair**wise, **triple**wise, **n**-wise coverage (pairwise by default).
* Turn on **randomization** to get more variety in your test cases (off by default).
* Print **generation details** to see the generated cases and the combinations they cover (off by default).

## Installation

### Requirements

Java 8 or higher.

### Gradle

```kotlin
implementation("io.github.pavelicii:allpairs4j:2.0.0")
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

* Define parameters and values with `AllPairsBuilder`, then call `build()` to generate cases.
* Iterate over `allPairs` and read values with `testCase.get(name)` to use in your tests.
* Use `getGeneratedCases()` for a `List<Case>`, or `System.out.println(allPairs)` to print the cases.

#### Sample code:

```java
AllPairs allPairs = new AllPairs.AllPairsBuilder()
        .withParameters(Arrays.asList(
                new Parameter("Browser", "Chrome", "Safari", "Edge"),
                new Parameter("OS", "Windows", "Linux", "macOS"),
                new Parameter("RAM", 4096, 8192, 16384),
                new Parameter("Drive", "HDD", "SSD")))
        .build();

// Get a case by index:
List<Case> cases = allPairs.getGeneratedCases();
Case firstCase = cases.get(0);

// Or iterate over all cases:
for (Case testCase : allPairs) {
    String browser = (String) testCase.get("Browser");
    String os = (String) testCase.get("OS");
    int ram = (Integer) testCase.get("RAM");
    String drive = (String) testCase.get("Drive");
}

// Or print all cases:
System.out.println(allPairs);
```

#### Output:

<details><summary>Generated cases</summary>

```text
  1: {Browser=Chrome, OS=Windows, RAM=4096, Drive=HDD}
  2: {Browser=Safari, OS=Linux, RAM=8192, Drive=HDD}
  3: {Browser=Edge, OS=macOS, RAM=16384, Drive=HDD}
  4: {Browser=Edge, OS=Linux, RAM=4096, Drive=SSD}
  5: {Browser=Safari, OS=Windows, RAM=16384, Drive=SSD}
  6: {Browser=Chrome, OS=macOS, RAM=8192, Drive=SSD}
  7: {Browser=Chrome, OS=Linux, RAM=16384, Drive=SSD}
  8: {Browser=Safari, OS=macOS, RAM=4096, Drive=HDD}
  9: {Browser=Edge, OS=Windows, RAM=8192, Drive=HDD}
```

</details>

<details><summary>Generation details</summary>

```text
=== Required combinations (45) ===

  1: {Browser=Chrome, OS=Windows}
  2: {Browser=Chrome, OS=Linux}
  3: {Browser=Chrome, OS=macOS}
  4: {Browser=Safari, OS=Windows}
  5: {Browser=Safari, OS=Linux}
  6: {Browser=Safari, OS=macOS}
  7: {Browser=Edge, OS=Windows}
  8: {Browser=Edge, OS=Linux}
  9: {Browser=Edge, OS=macOS}
 10: {Browser=Chrome, RAM=4096}
 11: {Browser=Chrome, RAM=8192}
 12: {Browser=Chrome, RAM=16384}
 13: {Browser=Safari, RAM=4096}
 14: {Browser=Safari, RAM=8192}
 15: {Browser=Safari, RAM=16384}
 16: {Browser=Edge, RAM=4096}
 17: {Browser=Edge, RAM=8192}
 18: {Browser=Edge, RAM=16384}
 19: {Browser=Chrome, Drive=HDD}
 20: {Browser=Chrome, Drive=SSD}
 21: {Browser=Safari, Drive=HDD}
 22: {Browser=Safari, Drive=SSD}
 23: {Browser=Edge, Drive=HDD}
 24: {Browser=Edge, Drive=SSD}
 25: {OS=Windows, RAM=4096}
 26: {OS=Windows, RAM=8192}
 27: {OS=Windows, RAM=16384}
 28: {OS=Linux, RAM=4096}
 29: {OS=Linux, RAM=8192}
 30: {OS=Linux, RAM=16384}
 31: {OS=macOS, RAM=4096}
 32: {OS=macOS, RAM=8192}
 33: {OS=macOS, RAM=16384}
 34: {OS=Windows, Drive=HDD}
 35: {OS=Windows, Drive=SSD}
 36: {OS=Linux, Drive=HDD}
 37: {OS=Linux, Drive=SSD}
 38: {OS=macOS, Drive=HDD}
 39: {OS=macOS, Drive=SSD}
 40: {RAM=4096, Drive=HDD}
 41: {RAM=4096, Drive=SSD}
 42: {RAM=8192, Drive=HDD}
 43: {RAM=8192, Drive=SSD}
 44: {RAM=16384, Drive=HDD}
 45: {RAM=16384, Drive=SSD}

=== Generated cases (9) ===

Case 1: {Browser=Chrome, OS=Windows, RAM=4096, Drive=HDD}
  New combinations (6):
      1: {Browser=Chrome, OS=Windows}
      2: {Browser=Chrome, RAM=4096}
      3: {Browser=Chrome, Drive=HDD}
      4: {OS=Windows, RAM=4096}
      5: {OS=Windows, Drive=HDD}
      6: {RAM=4096, Drive=HDD}

Case 2: {Browser=Safari, OS=Linux, RAM=8192, Drive=HDD}
  New combinations (6):
      1: {Browser=Safari, OS=Linux}
      2: {Browser=Safari, RAM=8192}
      3: {Browser=Safari, Drive=HDD}
      4: {OS=Linux, RAM=8192}
      5: {OS=Linux, Drive=HDD}
      6: {RAM=8192, Drive=HDD}

Case 3: {Browser=Edge, OS=macOS, RAM=16384, Drive=HDD}
  New combinations (6):
      1: {Browser=Edge, OS=macOS}
      2: {Browser=Edge, RAM=16384}
      3: {Browser=Edge, Drive=HDD}
      4: {OS=macOS, RAM=16384}
      5: {OS=macOS, Drive=HDD}
      6: {RAM=16384, Drive=HDD}

Case 4: {Browser=Edge, OS=Linux, RAM=4096, Drive=SSD}
  New combinations (6):
      1: {Browser=Edge, OS=Linux}
      2: {Browser=Edge, RAM=4096}
      3: {Browser=Edge, Drive=SSD}
      4: {OS=Linux, RAM=4096}
      5: {OS=Linux, Drive=SSD}
      6: {RAM=4096, Drive=SSD}

Case 5: {Browser=Safari, OS=Windows, RAM=16384, Drive=SSD}
  New combinations (6):
      1: {Browser=Safari, OS=Windows}
      2: {Browser=Safari, RAM=16384}
      3: {Browser=Safari, Drive=SSD}
      4: {OS=Windows, RAM=16384}
      5: {OS=Windows, Drive=SSD}
      6: {RAM=16384, Drive=SSD}

Case 6: {Browser=Chrome, OS=macOS, RAM=8192, Drive=SSD}
  New combinations (6):
      1: {Browser=Chrome, OS=macOS}
      2: {Browser=Chrome, RAM=8192}
      3: {Browser=Chrome, Drive=SSD}
      4: {OS=macOS, RAM=8192}
      5: {OS=macOS, Drive=SSD}
      6: {RAM=8192, Drive=SSD}

Case 7: {Browser=Chrome, OS=Linux, RAM=16384, Drive=SSD}
  New combinations (3):
      1: {Browser=Chrome, OS=Linux}
      2: {Browser=Chrome, RAM=16384}
      3: {OS=Linux, RAM=16384}

Case 8: {Browser=Safari, OS=macOS, RAM=4096, Drive=HDD}
  New combinations (3):
      1: {Browser=Safari, OS=macOS}
      2: {Browser=Safari, RAM=4096}
      3: {OS=macOS, RAM=4096}

Case 9: {Browser=Edge, OS=Windows, RAM=8192, Drive=HDD}
  New combinations (3):
      1: {Browser=Edge, OS=Windows}
      2: {Browser=Edge, RAM=8192}
      3: {OS=Windows, RAM=8192}
```

</details>

### Add Case Constraints

* Use `builder.withConstraint(predicate)` to exclude unwanted cases. Return `true` to exclude a case; `false` means this constraint doesn't exclude it.
* If any case constraint excludes a case, AllPairs4J looks for another one while generating cases.
* Use it only to check values, not to change data or perform other actions.

For example, add case constraints so that:

* `Browser=Safari` is tested only with `OS=macOS`.
* `Browser=Edge` is tested only with `OS=Windows`.
* `RAM` must be at least `8000`.

#### Sample code:

```java
AllPairs allPairs = new AllPairs.AllPairsBuilder()
        .withParameter(new Parameter("Browser", "Chrome", "Safari", "Edge"))
        .withParameter(new Parameter("OS", "Windows", "Linux", "macOS"))
        .withParameter(new Parameter("RAM", 4096, 8192, 16384))
        .withParameter(new Parameter("Drive", "HDD", "SSD"))
        .withConstraint(c -> c.get("Browser").equals("Safari") && !c.get("OS").equals("macOS"))
        .withConstraint(c -> c.get("Browser").equals("Edge") && !c.get("OS").equals("Windows"))
        .withConstraint(c -> (int) c.get("RAM") < 8000)
        .build();
```

#### Output:

<details><summary>Generated cases</summary>

```text
  1: {Browser=Chrome, OS=Windows, RAM=8192, Drive=HDD}
  2: {Browser=Safari, OS=macOS, RAM=16384, Drive=HDD}
  3: {Browser=Edge, OS=Windows, RAM=8192, Drive=SSD}
  4: {Browser=Safari, OS=macOS, RAM=8192, Drive=SSD}
  5: {Browser=Chrome, OS=Linux, RAM=16384, Drive=SSD}
  6: {Browser=Edge, OS=Windows, RAM=16384, Drive=HDD}
  7: {Browser=Chrome, OS=macOS, RAM=8192, Drive=HDD}
  8: {Browser=Chrome, OS=Linux, RAM=8192, Drive=SSD}
  9: {Browser=Chrome, OS=Linux, RAM=16384, Drive=HDD}
```

</details>

<details><summary>Generation details</summary>

```text
=== Required combinations (33) ===

  1: {Browser=Chrome, OS=Windows}
  2: {Browser=Chrome, OS=Linux}
  3: {Browser=Chrome, OS=macOS}
  4: {Browser=Safari, OS=macOS}
  5: {Browser=Edge, OS=Windows}
  6: {Browser=Chrome, RAM=8192}
  7: {Browser=Chrome, RAM=16384}
  8: {Browser=Safari, RAM=8192}
  9: {Browser=Safari, RAM=16384}
 10: {Browser=Edge, RAM=8192}
 11: {Browser=Edge, RAM=16384}
 12: {Browser=Chrome, Drive=HDD}
 13: {Browser=Chrome, Drive=SSD}
 14: {Browser=Safari, Drive=HDD}
 15: {Browser=Safari, Drive=SSD}
 16: {Browser=Edge, Drive=HDD}
 17: {Browser=Edge, Drive=SSD}
 18: {OS=Windows, RAM=8192}
 19: {OS=Windows, RAM=16384}
 20: {OS=Linux, RAM=8192}
 21: {OS=Linux, RAM=16384}
 22: {OS=macOS, RAM=8192}
 23: {OS=macOS, RAM=16384}
 24: {OS=Windows, Drive=HDD}
 25: {OS=Windows, Drive=SSD}
 26: {OS=Linux, Drive=HDD}
 27: {OS=Linux, Drive=SSD}
 28: {OS=macOS, Drive=HDD}
 29: {OS=macOS, Drive=SSD}
 30: {RAM=8192, Drive=HDD}
 31: {RAM=8192, Drive=SSD}
 32: {RAM=16384, Drive=HDD}
 33: {RAM=16384, Drive=SSD}

=== Generated cases (9) ===

Case 1: {Browser=Chrome, OS=Windows, RAM=8192, Drive=HDD}
  New combinations (6):
      1: {Browser=Chrome, OS=Windows}
      2: {Browser=Chrome, RAM=8192}
      3: {Browser=Chrome, Drive=HDD}
      4: {OS=Windows, RAM=8192}
      5: {OS=Windows, Drive=HDD}
      6: {RAM=8192, Drive=HDD}

Case 2: {Browser=Safari, OS=macOS, RAM=16384, Drive=HDD}
  New combinations (6):
      1: {Browser=Safari, OS=macOS}
      2: {Browser=Safari, RAM=16384}
      3: {Browser=Safari, Drive=HDD}
      4: {OS=macOS, RAM=16384}
      5: {OS=macOS, Drive=HDD}
      6: {RAM=16384, Drive=HDD}

Case 3: {Browser=Edge, OS=Windows, RAM=8192, Drive=SSD}
  New combinations (5):
      1: {Browser=Edge, OS=Windows}
      2: {Browser=Edge, RAM=8192}
      3: {Browser=Edge, Drive=SSD}
      4: {OS=Windows, Drive=SSD}
      5: {RAM=8192, Drive=SSD}

Case 4: {Browser=Safari, OS=macOS, RAM=8192, Drive=SSD}
  New combinations (4):
      1: {Browser=Safari, RAM=8192}
      2: {Browser=Safari, Drive=SSD}
      3: {OS=macOS, RAM=8192}
      4: {OS=macOS, Drive=SSD}

Case 5: {Browser=Chrome, OS=Linux, RAM=16384, Drive=SSD}
  New combinations (6):
      1: {Browser=Chrome, OS=Linux}
      2: {Browser=Chrome, RAM=16384}
      3: {Browser=Chrome, Drive=SSD}
      4: {OS=Linux, RAM=16384}
      5: {OS=Linux, Drive=SSD}
      6: {RAM=16384, Drive=SSD}

Case 6: {Browser=Edge, OS=Windows, RAM=16384, Drive=HDD}
  New combinations (3):
      1: {Browser=Edge, RAM=16384}
      2: {Browser=Edge, Drive=HDD}
      3: {OS=Windows, RAM=16384}

Case 7: {Browser=Chrome, OS=macOS, RAM=8192, Drive=HDD}
  New combinations (1):
      1: {Browser=Chrome, OS=macOS}

Case 8: {Browser=Chrome, OS=Linux, RAM=8192, Drive=SSD}
  New combinations (1):
      1: {OS=Linux, RAM=8192}

Case 9: {Browser=Chrome, OS=Linux, RAM=16384, Drive=HDD}
  New combinations (1):
      1: {OS=Linux, Drive=HDD}
```

</details>

### Optional Values and Absence Coverage

* Add `Parameter.ABSENT` to let a parameter be left out of some cases.
  `AllPairs.toString()` and generation details show `<absent>` only when absence coverage is enabled.
  The resulting `Case` has no key for an absent parameter in either mode.
* `null` is different: the parameter is still included, with `null` as its value.
* `withAbsenceCoverage(true)` (default) requires cases to also cover combinations where optional parameters are absent.
* `withAbsenceCoverage(false)` doesn't require coverage of those combinations, but parameters can still be left out of cases.

#### Sample code: absence coverage enabled

```java
AllPairs allPairs = new AllPairs.AllPairsBuilder()
        .withParameter(new Parameter("Browser", "Chrome", "Safari", "Edge"))
        .withParameter(new Parameter("OS", "Windows", "Linux", "macOS"))
        .withParameter(new Parameter("RAM", 4096, 8192, 16384))
        .withParameter(new Parameter("Drive", "HDD", "SSD", null, Parameter.ABSENT))
        .withAbsenceCoverage(true)
        .build();
```

#### Output:

<details><summary>Generated cases</summary>

```text
  1: {Browser=Chrome, OS=Windows, RAM=4096, Drive=HDD}
  2: {Browser=Chrome, OS=Linux, RAM=8192, Drive=SSD}
  3: {Browser=Chrome, OS=macOS, RAM=16384, Drive=null}
  4: {Browser=Chrome, OS=macOS, RAM=8192, Drive=<absent>}
  5: {Browser=Safari, OS=Linux, RAM=16384, Drive=HDD}
  6: {Browser=Safari, OS=Windows, RAM=4096, Drive=SSD}
  7: {Browser=Safari, OS=Windows, RAM=8192, Drive=null}
  8: {Browser=Safari, OS=Linux, RAM=4096, Drive=<absent>}
  9: {Browser=Safari, OS=macOS, RAM=4096, Drive=null}
 10: {Browser=Edge, OS=macOS, RAM=8192, Drive=HDD}
 11: {Browser=Edge, OS=Windows, RAM=16384, Drive=<absent>}
 12: {Browser=Edge, OS=Linux, RAM=4096, Drive=null}
 13: {Browser=Edge, OS=macOS, RAM=16384, Drive=SSD}
```

</details>

<details><summary>Generation details</summary>

```text
=== Required combinations (63) ===

  1: {Browser=Chrome, OS=Windows}
  2: {Browser=Chrome, OS=Linux}
  3: {Browser=Chrome, OS=macOS}
  4: {Browser=Safari, OS=Windows}
  5: {Browser=Safari, OS=Linux}
  6: {Browser=Safari, OS=macOS}
  7: {Browser=Edge, OS=Windows}
  8: {Browser=Edge, OS=Linux}
  9: {Browser=Edge, OS=macOS}
 10: {Browser=Chrome, RAM=4096}
 11: {Browser=Chrome, RAM=8192}
 12: {Browser=Chrome, RAM=16384}
 13: {Browser=Safari, RAM=4096}
 14: {Browser=Safari, RAM=8192}
 15: {Browser=Safari, RAM=16384}
 16: {Browser=Edge, RAM=4096}
 17: {Browser=Edge, RAM=8192}
 18: {Browser=Edge, RAM=16384}
 19: {Browser=Chrome, Drive=HDD}
 20: {Browser=Chrome, Drive=SSD}
 21: {Browser=Chrome, Drive=null}
 22: {Browser=Chrome, Drive=<absent>}
 23: {Browser=Safari, Drive=HDD}
 24: {Browser=Safari, Drive=SSD}
 25: {Browser=Safari, Drive=null}
 26: {Browser=Safari, Drive=<absent>}
 27: {Browser=Edge, Drive=HDD}
 28: {Browser=Edge, Drive=SSD}
 29: {Browser=Edge, Drive=null}
 30: {Browser=Edge, Drive=<absent>}
 31: {OS=Windows, RAM=4096}
 32: {OS=Windows, RAM=8192}
 33: {OS=Windows, RAM=16384}
 34: {OS=Linux, RAM=4096}
 35: {OS=Linux, RAM=8192}
 36: {OS=Linux, RAM=16384}
 37: {OS=macOS, RAM=4096}
 38: {OS=macOS, RAM=8192}
 39: {OS=macOS, RAM=16384}
 40: {OS=Windows, Drive=HDD}
 41: {OS=Windows, Drive=SSD}
 42: {OS=Windows, Drive=null}
 43: {OS=Windows, Drive=<absent>}
 44: {OS=Linux, Drive=HDD}
 45: {OS=Linux, Drive=SSD}
 46: {OS=Linux, Drive=null}
 47: {OS=Linux, Drive=<absent>}
 48: {OS=macOS, Drive=HDD}
 49: {OS=macOS, Drive=SSD}
 50: {OS=macOS, Drive=null}
 51: {OS=macOS, Drive=<absent>}
 52: {RAM=4096, Drive=HDD}
 53: {RAM=4096, Drive=SSD}
 54: {RAM=4096, Drive=null}
 55: {RAM=4096, Drive=<absent>}
 56: {RAM=8192, Drive=HDD}
 57: {RAM=8192, Drive=SSD}
 58: {RAM=8192, Drive=null}
 59: {RAM=8192, Drive=<absent>}
 60: {RAM=16384, Drive=HDD}
 61: {RAM=16384, Drive=SSD}
 62: {RAM=16384, Drive=null}
 63: {RAM=16384, Drive=<absent>}

=== Generated cases (13) ===

Case 1: {Browser=Chrome, OS=Windows, RAM=4096, Drive=HDD}
  New combinations (6):
      1: {Browser=Chrome, OS=Windows}
      2: {Browser=Chrome, RAM=4096}
      3: {Browser=Chrome, Drive=HDD}
      4: {OS=Windows, RAM=4096}
      5: {OS=Windows, Drive=HDD}
      6: {RAM=4096, Drive=HDD}

Case 2: {Browser=Chrome, OS=Linux, RAM=8192, Drive=SSD}
  New combinations (6):
      1: {Browser=Chrome, OS=Linux}
      2: {Browser=Chrome, RAM=8192}
      3: {Browser=Chrome, Drive=SSD}
      4: {OS=Linux, RAM=8192}
      5: {OS=Linux, Drive=SSD}
      6: {RAM=8192, Drive=SSD}

Case 3: {Browser=Chrome, OS=macOS, RAM=16384, Drive=null}
  New combinations (6):
      1: {Browser=Chrome, OS=macOS}
      2: {Browser=Chrome, RAM=16384}
      3: {Browser=Chrome, Drive=null}
      4: {OS=macOS, RAM=16384}
      5: {OS=macOS, Drive=null}
      6: {RAM=16384, Drive=null}

Case 4: {Browser=Chrome, OS=macOS, RAM=8192, Drive=<absent>}
  New combinations (4):
      1: {Browser=Chrome, Drive=<absent>}
      2: {OS=macOS, RAM=8192}
      3: {OS=macOS, Drive=<absent>}
      4: {RAM=8192, Drive=<absent>}

Case 5: {Browser=Safari, OS=Linux, RAM=16384, Drive=HDD}
  New combinations (6):
      1: {Browser=Safari, OS=Linux}
      2: {Browser=Safari, RAM=16384}
      3: {Browser=Safari, Drive=HDD}
      4: {OS=Linux, RAM=16384}
      5: {OS=Linux, Drive=HDD}
      6: {RAM=16384, Drive=HDD}

Case 6: {Browser=Safari, OS=Windows, RAM=4096, Drive=SSD}
  New combinations (5):
      1: {Browser=Safari, OS=Windows}
      2: {Browser=Safari, RAM=4096}
      3: {Browser=Safari, Drive=SSD}
      4: {OS=Windows, Drive=SSD}
      5: {RAM=4096, Drive=SSD}

Case 7: {Browser=Safari, OS=Windows, RAM=8192, Drive=null}
  New combinations (5):
      1: {Browser=Safari, RAM=8192}
      2: {Browser=Safari, Drive=null}
      3: {OS=Windows, RAM=8192}
      4: {OS=Windows, Drive=null}
      5: {RAM=8192, Drive=null}

Case 8: {Browser=Safari, OS=Linux, RAM=4096, Drive=<absent>}
  New combinations (4):
      1: {Browser=Safari, Drive=<absent>}
      2: {OS=Linux, RAM=4096}
      3: {OS=Linux, Drive=<absent>}
      4: {RAM=4096, Drive=<absent>}

Case 9: {Browser=Safari, OS=macOS, RAM=4096, Drive=null}
  New combinations (3):
      1: {Browser=Safari, OS=macOS}
      2: {OS=macOS, RAM=4096}
      3: {RAM=4096, Drive=null}

Case 10: {Browser=Edge, OS=macOS, RAM=8192, Drive=HDD}
  New combinations (5):
      1: {Browser=Edge, OS=macOS}
      2: {Browser=Edge, RAM=8192}
      3: {Browser=Edge, Drive=HDD}
      4: {OS=macOS, Drive=HDD}
      5: {RAM=8192, Drive=HDD}

Case 11: {Browser=Edge, OS=Windows, RAM=16384, Drive=<absent>}
  New combinations (6):
      1: {Browser=Edge, OS=Windows}
      2: {Browser=Edge, RAM=16384}
      3: {Browser=Edge, Drive=<absent>}
      4: {OS=Windows, RAM=16384}
      5: {OS=Windows, Drive=<absent>}
      6: {RAM=16384, Drive=<absent>}

Case 12: {Browser=Edge, OS=Linux, RAM=4096, Drive=null}
  New combinations (4):
      1: {Browser=Edge, OS=Linux}
      2: {Browser=Edge, RAM=4096}
      3: {Browser=Edge, Drive=null}
      4: {OS=Linux, Drive=null}

Case 13: {Browser=Edge, OS=macOS, RAM=16384, Drive=SSD}
  New combinations (3):
      1: {Browser=Edge, Drive=SSD}
      2: {OS=macOS, Drive=SSD}
      3: {RAM=16384, Drive=SSD}
```

</details>

#### Sample code: absence coverage disabled

```java
AllPairs allPairs = new AllPairs.AllPairsBuilder()
        .withParameter(new Parameter("Browser", "Chrome", "Safari", "Edge"))
        .withParameter(new Parameter("OS", "Windows", "Linux", "macOS"))
        .withParameter(new Parameter("RAM", 4096, 8192, 16384))
        .withParameter(new Parameter("Drive", "HDD", "SSD", null, Parameter.ABSENT))
        .withAbsenceCoverage(false)
        .build();
```

#### Output:

<details><summary>Generated cases</summary>

```text
  1: {Browser=Chrome, OS=Windows, RAM=4096, Drive=HDD}
  2: {Browser=Chrome, OS=Linux, RAM=8192, Drive=SSD}
  3: {Browser=Chrome, OS=macOS, RAM=16384, Drive=null}
  4: {Browser=Safari, OS=Windows, RAM=16384, Drive=SSD}
  5: {Browser=Safari, OS=Linux, RAM=4096, Drive=null}
  6: {Browser=Safari, OS=macOS, RAM=8192, Drive=HDD}
  7: {Browser=Edge, OS=Windows, RAM=8192, Drive=null}
  8: {Browser=Edge, OS=Linux, RAM=16384, Drive=HDD}
  9: {Browser=Edge, OS=macOS, RAM=4096, Drive=SSD}
```

</details>

<details><summary>Generation details</summary>

```text
=== Required combinations (54) ===

  1: {Browser=Chrome, OS=Windows}
  2: {Browser=Chrome, OS=Linux}
  3: {Browser=Chrome, OS=macOS}
  4: {Browser=Safari, OS=Windows}
  5: {Browser=Safari, OS=Linux}
  6: {Browser=Safari, OS=macOS}
  7: {Browser=Edge, OS=Windows}
  8: {Browser=Edge, OS=Linux}
  9: {Browser=Edge, OS=macOS}
 10: {Browser=Chrome, RAM=4096}
 11: {Browser=Chrome, RAM=8192}
 12: {Browser=Chrome, RAM=16384}
 13: {Browser=Safari, RAM=4096}
 14: {Browser=Safari, RAM=8192}
 15: {Browser=Safari, RAM=16384}
 16: {Browser=Edge, RAM=4096}
 17: {Browser=Edge, RAM=8192}
 18: {Browser=Edge, RAM=16384}
 19: {Browser=Chrome, Drive=HDD}
 20: {Browser=Chrome, Drive=SSD}
 21: {Browser=Chrome, Drive=null}
 22: {Browser=Safari, Drive=HDD}
 23: {Browser=Safari, Drive=SSD}
 24: {Browser=Safari, Drive=null}
 25: {Browser=Edge, Drive=HDD}
 26: {Browser=Edge, Drive=SSD}
 27: {Browser=Edge, Drive=null}
 28: {OS=Windows, RAM=4096}
 29: {OS=Windows, RAM=8192}
 30: {OS=Windows, RAM=16384}
 31: {OS=Linux, RAM=4096}
 32: {OS=Linux, RAM=8192}
 33: {OS=Linux, RAM=16384}
 34: {OS=macOS, RAM=4096}
 35: {OS=macOS, RAM=8192}
 36: {OS=macOS, RAM=16384}
 37: {OS=Windows, Drive=HDD}
 38: {OS=Windows, Drive=SSD}
 39: {OS=Windows, Drive=null}
 40: {OS=Linux, Drive=HDD}
 41: {OS=Linux, Drive=SSD}
 42: {OS=Linux, Drive=null}
 43: {OS=macOS, Drive=HDD}
 44: {OS=macOS, Drive=SSD}
 45: {OS=macOS, Drive=null}
 46: {RAM=4096, Drive=HDD}
 47: {RAM=4096, Drive=SSD}
 48: {RAM=4096, Drive=null}
 49: {RAM=8192, Drive=HDD}
 50: {RAM=8192, Drive=SSD}
 51: {RAM=8192, Drive=null}
 52: {RAM=16384, Drive=HDD}
 53: {RAM=16384, Drive=SSD}
 54: {RAM=16384, Drive=null}

=== Generated cases (9) ===

Case 1: {Browser=Chrome, OS=Windows, RAM=4096, Drive=HDD}
  New combinations (6):
      1: {Browser=Chrome, OS=Windows}
      2: {Browser=Chrome, RAM=4096}
      3: {Browser=Chrome, Drive=HDD}
      4: {OS=Windows, RAM=4096}
      5: {OS=Windows, Drive=HDD}
      6: {RAM=4096, Drive=HDD}

Case 2: {Browser=Chrome, OS=Linux, RAM=8192, Drive=SSD}
  New combinations (6):
      1: {Browser=Chrome, OS=Linux}
      2: {Browser=Chrome, RAM=8192}
      3: {Browser=Chrome, Drive=SSD}
      4: {OS=Linux, RAM=8192}
      5: {OS=Linux, Drive=SSD}
      6: {RAM=8192, Drive=SSD}

Case 3: {Browser=Chrome, OS=macOS, RAM=16384, Drive=null}
  New combinations (6):
      1: {Browser=Chrome, OS=macOS}
      2: {Browser=Chrome, RAM=16384}
      3: {Browser=Chrome, Drive=null}
      4: {OS=macOS, RAM=16384}
      5: {OS=macOS, Drive=null}
      6: {RAM=16384, Drive=null}

Case 4: {Browser=Safari, OS=Windows, RAM=16384, Drive=SSD}
  New combinations (6):
      1: {Browser=Safari, OS=Windows}
      2: {Browser=Safari, RAM=16384}
      3: {Browser=Safari, Drive=SSD}
      4: {OS=Windows, RAM=16384}
      5: {OS=Windows, Drive=SSD}
      6: {RAM=16384, Drive=SSD}

Case 5: {Browser=Safari, OS=Linux, RAM=4096, Drive=null}
  New combinations (6):
      1: {Browser=Safari, OS=Linux}
      2: {Browser=Safari, RAM=4096}
      3: {Browser=Safari, Drive=null}
      4: {OS=Linux, RAM=4096}
      5: {OS=Linux, Drive=null}
      6: {RAM=4096, Drive=null}

Case 6: {Browser=Safari, OS=macOS, RAM=8192, Drive=HDD}
  New combinations (6):
      1: {Browser=Safari, OS=macOS}
      2: {Browser=Safari, RAM=8192}
      3: {Browser=Safari, Drive=HDD}
      4: {OS=macOS, RAM=8192}
      5: {OS=macOS, Drive=HDD}
      6: {RAM=8192, Drive=HDD}

Case 7: {Browser=Edge, OS=Windows, RAM=8192, Drive=null}
  New combinations (6):
      1: {Browser=Edge, OS=Windows}
      2: {Browser=Edge, RAM=8192}
      3: {Browser=Edge, Drive=null}
      4: {OS=Windows, RAM=8192}
      5: {OS=Windows, Drive=null}
      6: {RAM=8192, Drive=null}

Case 8: {Browser=Edge, OS=Linux, RAM=16384, Drive=HDD}
  New combinations (6):
      1: {Browser=Edge, OS=Linux}
      2: {Browser=Edge, RAM=16384}
      3: {Browser=Edge, Drive=HDD}
      4: {OS=Linux, RAM=16384}
      5: {OS=Linux, Drive=HDD}
      6: {RAM=16384, Drive=HDD}

Case 9: {Browser=Edge, OS=macOS, RAM=4096, Drive=SSD}
  New combinations (6):
      1: {Browser=Edge, OS=macOS}
      2: {Browser=Edge, RAM=4096}
      3: {Browser=Edge, Drive=SSD}
      4: {OS=macOS, RAM=4096}
      5: {OS=macOS, Drive=SSD}
      6: {RAM=4096, Drive=SSD}
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
        .withParameter(new Parameter("RAM", 4096, 8192, 16384))
        .withParameter(new Parameter("Drive", "HDD", "SSD"))
        .withParameter(new Parameter("WindowsEdition", "Home", "Pro").withConstraint(c -> !c.get("OS").equals("Windows")))
        .build();
```

#### Output:

<details><summary>Generated cases</summary>

```text
  1: {Browser=Chrome, OS=Windows, RAM=4096, Drive=HDD, WindowsEdition=Home}
  2: {Browser=Chrome, OS=Linux, RAM=8192, Drive=SSD, WindowsEdition=<absent>}
  3: {Browser=Chrome, OS=macOS, RAM=16384, Drive=SSD, WindowsEdition=<absent>}
  4: {Browser=Chrome, OS=Windows, RAM=16384, Drive=HDD, WindowsEdition=Pro}
  5: {Browser=Safari, OS=Windows, RAM=8192, Drive=SSD, WindowsEdition=Pro}
  6: {Browser=Safari, OS=Linux, RAM=16384, Drive=HDD, WindowsEdition=<absent>}
  7: {Browser=Safari, OS=macOS, RAM=4096, Drive=SSD, WindowsEdition=<absent>}
  8: {Browser=Safari, OS=Windows, RAM=8192, Drive=SSD, WindowsEdition=Home}
  9: {Browser=Edge, OS=Windows, RAM=16384, Drive=HDD, WindowsEdition=Home}
 10: {Browser=Edge, OS=Linux, RAM=4096, Drive=SSD, WindowsEdition=<absent>}
 11: {Browser=Edge, OS=macOS, RAM=8192, Drive=HDD, WindowsEdition=<absent>}
 12: {Browser=Edge, OS=Windows, RAM=4096, Drive=HDD, WindowsEdition=Pro}
```

</details>

<details><summary>Generation details</summary>

```text
=== Required combinations (73) ===

  1: {Browser=Chrome, OS=Windows}
  2: {Browser=Chrome, OS=Linux}
  3: {Browser=Chrome, OS=macOS}
  4: {Browser=Safari, OS=Windows}
  5: {Browser=Safari, OS=Linux}
  6: {Browser=Safari, OS=macOS}
  7: {Browser=Edge, OS=Windows}
  8: {Browser=Edge, OS=Linux}
  9: {Browser=Edge, OS=macOS}
 10: {Browser=Chrome, RAM=4096}
 11: {Browser=Chrome, RAM=8192}
 12: {Browser=Chrome, RAM=16384}
 13: {Browser=Safari, RAM=4096}
 14: {Browser=Safari, RAM=8192}
 15: {Browser=Safari, RAM=16384}
 16: {Browser=Edge, RAM=4096}
 17: {Browser=Edge, RAM=8192}
 18: {Browser=Edge, RAM=16384}
 19: {Browser=Chrome, Drive=HDD}
 20: {Browser=Chrome, Drive=SSD}
 21: {Browser=Safari, Drive=HDD}
 22: {Browser=Safari, Drive=SSD}
 23: {Browser=Edge, Drive=HDD}
 24: {Browser=Edge, Drive=SSD}
 25: {Browser=Chrome, WindowsEdition=Home}
 26: {Browser=Chrome, WindowsEdition=Pro}
 27: {Browser=Chrome, WindowsEdition=<absent>}
 28: {Browser=Safari, WindowsEdition=Home}
 29: {Browser=Safari, WindowsEdition=Pro}
 30: {Browser=Safari, WindowsEdition=<absent>}
 31: {Browser=Edge, WindowsEdition=Home}
 32: {Browser=Edge, WindowsEdition=Pro}
 33: {Browser=Edge, WindowsEdition=<absent>}
 34: {OS=Windows, RAM=4096}
 35: {OS=Windows, RAM=8192}
 36: {OS=Windows, RAM=16384}
 37: {OS=Linux, RAM=4096}
 38: {OS=Linux, RAM=8192}
 39: {OS=Linux, RAM=16384}
 40: {OS=macOS, RAM=4096}
 41: {OS=macOS, RAM=8192}
 42: {OS=macOS, RAM=16384}
 43: {OS=Windows, Drive=HDD}
 44: {OS=Windows, Drive=SSD}
 45: {OS=Linux, Drive=HDD}
 46: {OS=Linux, Drive=SSD}
 47: {OS=macOS, Drive=HDD}
 48: {OS=macOS, Drive=SSD}
 49: {OS=Windows, WindowsEdition=Home}
 50: {OS=Windows, WindowsEdition=Pro}
 51: {OS=Linux, WindowsEdition=<absent>}
 52: {OS=macOS, WindowsEdition=<absent>}
 53: {RAM=4096, Drive=HDD}
 54: {RAM=4096, Drive=SSD}
 55: {RAM=8192, Drive=HDD}
 56: {RAM=8192, Drive=SSD}
 57: {RAM=16384, Drive=HDD}
 58: {RAM=16384, Drive=SSD}
 59: {RAM=4096, WindowsEdition=Home}
 60: {RAM=4096, WindowsEdition=Pro}
 61: {RAM=4096, WindowsEdition=<absent>}
 62: {RAM=8192, WindowsEdition=Home}
 63: {RAM=8192, WindowsEdition=Pro}
 64: {RAM=8192, WindowsEdition=<absent>}
 65: {RAM=16384, WindowsEdition=Home}
 66: {RAM=16384, WindowsEdition=Pro}
 67: {RAM=16384, WindowsEdition=<absent>}
 68: {Drive=HDD, WindowsEdition=Home}
 69: {Drive=HDD, WindowsEdition=Pro}
 70: {Drive=HDD, WindowsEdition=<absent>}
 71: {Drive=SSD, WindowsEdition=Home}
 72: {Drive=SSD, WindowsEdition=Pro}
 73: {Drive=SSD, WindowsEdition=<absent>}

=== Generated cases (12) ===

Case 1: {Browser=Chrome, OS=Windows, RAM=4096, Drive=HDD, WindowsEdition=Home}
  New combinations (10):
      1: {Browser=Chrome, OS=Windows}
      2: {Browser=Chrome, RAM=4096}
      3: {Browser=Chrome, Drive=HDD}
      4: {Browser=Chrome, WindowsEdition=Home}
      5: {OS=Windows, RAM=4096}
      6: {OS=Windows, Drive=HDD}
      7: {OS=Windows, WindowsEdition=Home}
      8: {RAM=4096, Drive=HDD}
      9: {RAM=4096, WindowsEdition=Home}
     10: {Drive=HDD, WindowsEdition=Home}

Case 2: {Browser=Chrome, OS=Linux, RAM=8192, Drive=SSD, WindowsEdition=<absent>}
  New combinations (10):
      1: {Browser=Chrome, OS=Linux}
      2: {Browser=Chrome, RAM=8192}
      3: {Browser=Chrome, Drive=SSD}
      4: {Browser=Chrome, WindowsEdition=<absent>}
      5: {OS=Linux, RAM=8192}
      6: {OS=Linux, Drive=SSD}
      7: {OS=Linux, WindowsEdition=<absent>}
      8: {RAM=8192, Drive=SSD}
      9: {RAM=8192, WindowsEdition=<absent>}
     10: {Drive=SSD, WindowsEdition=<absent>}

Case 3: {Browser=Chrome, OS=macOS, RAM=16384, Drive=SSD, WindowsEdition=<absent>}
  New combinations (7):
      1: {Browser=Chrome, OS=macOS}
      2: {Browser=Chrome, RAM=16384}
      3: {OS=macOS, RAM=16384}
      4: {OS=macOS, Drive=SSD}
      5: {OS=macOS, WindowsEdition=<absent>}
      6: {RAM=16384, Drive=SSD}
      7: {RAM=16384, WindowsEdition=<absent>}

Case 4: {Browser=Chrome, OS=Windows, RAM=16384, Drive=HDD, WindowsEdition=Pro}
  New combinations (6):
      1: {Browser=Chrome, WindowsEdition=Pro}
      2: {OS=Windows, RAM=16384}
      3: {OS=Windows, WindowsEdition=Pro}
      4: {RAM=16384, Drive=HDD}
      5: {RAM=16384, WindowsEdition=Pro}
      6: {Drive=HDD, WindowsEdition=Pro}

Case 5: {Browser=Safari, OS=Windows, RAM=8192, Drive=SSD, WindowsEdition=Pro}
  New combinations (8):
      1: {Browser=Safari, OS=Windows}
      2: {Browser=Safari, RAM=8192}
      3: {Browser=Safari, Drive=SSD}
      4: {Browser=Safari, WindowsEdition=Pro}
      5: {OS=Windows, RAM=8192}
      6: {OS=Windows, Drive=SSD}
      7: {RAM=8192, WindowsEdition=Pro}
      8: {Drive=SSD, WindowsEdition=Pro}

Case 6: {Browser=Safari, OS=Linux, RAM=16384, Drive=HDD, WindowsEdition=<absent>}
  New combinations (7):
      1: {Browser=Safari, OS=Linux}
      2: {Browser=Safari, RAM=16384}
      3: {Browser=Safari, Drive=HDD}
      4: {Browser=Safari, WindowsEdition=<absent>}
      5: {OS=Linux, RAM=16384}
      6: {OS=Linux, Drive=HDD}
      7: {Drive=HDD, WindowsEdition=<absent>}

Case 7: {Browser=Safari, OS=macOS, RAM=4096, Drive=SSD, WindowsEdition=<absent>}
  New combinations (5):
      1: {Browser=Safari, OS=macOS}
      2: {Browser=Safari, RAM=4096}
      3: {OS=macOS, RAM=4096}
      4: {RAM=4096, Drive=SSD}
      5: {RAM=4096, WindowsEdition=<absent>}

Case 8: {Browser=Safari, OS=Windows, RAM=8192, Drive=SSD, WindowsEdition=Home}
  New combinations (3):
      1: {Browser=Safari, WindowsEdition=Home}
      2: {RAM=8192, WindowsEdition=Home}
      3: {Drive=SSD, WindowsEdition=Home}

Case 9: {Browser=Edge, OS=Windows, RAM=16384, Drive=HDD, WindowsEdition=Home}
  New combinations (5):
      1: {Browser=Edge, OS=Windows}
      2: {Browser=Edge, RAM=16384}
      3: {Browser=Edge, Drive=HDD}
      4: {Browser=Edge, WindowsEdition=Home}
      5: {RAM=16384, WindowsEdition=Home}

Case 10: {Browser=Edge, OS=Linux, RAM=4096, Drive=SSD, WindowsEdition=<absent>}
  New combinations (5):
      1: {Browser=Edge, OS=Linux}
      2: {Browser=Edge, RAM=4096}
      3: {Browser=Edge, Drive=SSD}
      4: {Browser=Edge, WindowsEdition=<absent>}
      5: {OS=Linux, RAM=4096}

Case 11: {Browser=Edge, OS=macOS, RAM=8192, Drive=HDD, WindowsEdition=<absent>}
  New combinations (5):
      1: {Browser=Edge, OS=macOS}
      2: {Browser=Edge, RAM=8192}
      3: {OS=macOS, RAM=8192}
      4: {OS=macOS, Drive=HDD}
      5: {RAM=8192, Drive=HDD}

Case 12: {Browser=Edge, OS=Windows, RAM=4096, Drive=HDD, WindowsEdition=Pro}
  New combinations (2):
      1: {Browser=Edge, WindowsEdition=Pro}
      2: {RAM=4096, WindowsEdition=Pro}
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
                new Parameter("RAM", 4096, 8192, 16384),
                new Parameter("Drive", "HDD", "SSD")))
        .build();
```

#### Output:

<details><summary>Generated cases</summary>

```text
  1: {Browser=Chrome, OS=Windows, RAM=4096, Drive=HDD}
  2: {Browser=Edge, OS=macOS, RAM=8192, Drive=SSD}
  3: {Browser=Safari, OS=Linux, RAM=4096, Drive=SSD}
  4: {Browser=Chrome, OS=Windows, RAM=16384, Drive=SSD}
  5: {Browser=Safari, OS=Linux, RAM=16384, Drive=HDD}
  6: {Browser=Edge, OS=macOS, RAM=4096, Drive=HDD}
  7: {Browser=Chrome, OS=Windows, RAM=8192, Drive=HDD}
  8: {Browser=Chrome, OS=Linux, RAM=4096, Drive=HDD}
  9: {Browser=Safari, OS=Linux, RAM=8192, Drive=SSD}
 10: {Browser=Edge, OS=macOS, RAM=16384, Drive=SSD}
 11: {Browser=Chrome, OS=Linux, RAM=8192, Drive=HDD}
 12: {Browser=Chrome, OS=Linux, RAM=16384, Drive=HDD}
 13: {Browser=Chrome, OS=macOS, RAM=4096, Drive=SSD}
 14: {Browser=Chrome, OS=macOS, RAM=8192, Drive=SSD}
 15: {Browser=Chrome, OS=macOS, RAM=16384, Drive=HDD}
 16: {Browser=Safari, OS=Windows, RAM=4096, Drive=HDD}
 17: {Browser=Safari, OS=Windows, RAM=8192, Drive=SSD}
 18: {Browser=Safari, OS=Windows, RAM=16384, Drive=SSD}
 19: {Browser=Safari, OS=macOS, RAM=4096, Drive=HDD}
 20: {Browser=Safari, OS=macOS, RAM=8192, Drive=HDD}
 21: {Browser=Safari, OS=macOS, RAM=16384, Drive=SSD}
 22: {Browser=Edge, OS=Windows, RAM=4096, Drive=SSD}
 23: {Browser=Edge, OS=Windows, RAM=16384, Drive=HDD}
 24: {Browser=Edge, OS=Windows, RAM=8192, Drive=HDD}
 25: {Browser=Edge, OS=Linux, RAM=4096, Drive=SSD}
 26: {Browser=Edge, OS=Linux, RAM=8192, Drive=HDD}
 27: {Browser=Edge, OS=Linux, RAM=16384, Drive=SSD}
 28: {Browser=Chrome, OS=Linux, RAM=4096, Drive=SSD}
```

</details>

<details><summary>Generation details</summary>

```text
=== Required combinations (81) ===

  1: {Browser=Chrome, OS=Windows, RAM=4096}
  2: {Browser=Chrome, OS=Windows, RAM=8192}
  3: {Browser=Chrome, OS=Windows, RAM=16384}
  4: {Browser=Chrome, OS=Linux, RAM=4096}
  5: {Browser=Chrome, OS=Linux, RAM=8192}
  6: {Browser=Chrome, OS=Linux, RAM=16384}
  7: {Browser=Chrome, OS=macOS, RAM=4096}
  8: {Browser=Chrome, OS=macOS, RAM=8192}
  9: {Browser=Chrome, OS=macOS, RAM=16384}
 10: {Browser=Safari, OS=Windows, RAM=4096}
 11: {Browser=Safari, OS=Windows, RAM=8192}
 12: {Browser=Safari, OS=Windows, RAM=16384}
 13: {Browser=Safari, OS=Linux, RAM=4096}
 14: {Browser=Safari, OS=Linux, RAM=8192}
 15: {Browser=Safari, OS=Linux, RAM=16384}
 16: {Browser=Safari, OS=macOS, RAM=4096}
 17: {Browser=Safari, OS=macOS, RAM=8192}
 18: {Browser=Safari, OS=macOS, RAM=16384}
 19: {Browser=Edge, OS=Windows, RAM=4096}
 20: {Browser=Edge, OS=Windows, RAM=8192}
 21: {Browser=Edge, OS=Windows, RAM=16384}
 22: {Browser=Edge, OS=Linux, RAM=4096}
 23: {Browser=Edge, OS=Linux, RAM=8192}
 24: {Browser=Edge, OS=Linux, RAM=16384}
 25: {Browser=Edge, OS=macOS, RAM=4096}
 26: {Browser=Edge, OS=macOS, RAM=8192}
 27: {Browser=Edge, OS=macOS, RAM=16384}
 28: {Browser=Chrome, OS=Windows, Drive=HDD}
 29: {Browser=Chrome, OS=Windows, Drive=SSD}
 30: {Browser=Chrome, OS=Linux, Drive=HDD}
 31: {Browser=Chrome, OS=Linux, Drive=SSD}
 32: {Browser=Chrome, OS=macOS, Drive=HDD}
 33: {Browser=Chrome, OS=macOS, Drive=SSD}
 34: {Browser=Safari, OS=Windows, Drive=HDD}
 35: {Browser=Safari, OS=Windows, Drive=SSD}
 36: {Browser=Safari, OS=Linux, Drive=HDD}
 37: {Browser=Safari, OS=Linux, Drive=SSD}
 38: {Browser=Safari, OS=macOS, Drive=HDD}
 39: {Browser=Safari, OS=macOS, Drive=SSD}
 40: {Browser=Edge, OS=Windows, Drive=HDD}
 41: {Browser=Edge, OS=Windows, Drive=SSD}
 42: {Browser=Edge, OS=Linux, Drive=HDD}
 43: {Browser=Edge, OS=Linux, Drive=SSD}
 44: {Browser=Edge, OS=macOS, Drive=HDD}
 45: {Browser=Edge, OS=macOS, Drive=SSD}
 46: {Browser=Chrome, RAM=4096, Drive=HDD}
 47: {Browser=Chrome, RAM=4096, Drive=SSD}
 48: {Browser=Chrome, RAM=8192, Drive=HDD}
 49: {Browser=Chrome, RAM=8192, Drive=SSD}
 50: {Browser=Chrome, RAM=16384, Drive=HDD}
 51: {Browser=Chrome, RAM=16384, Drive=SSD}
 52: {Browser=Safari, RAM=4096, Drive=HDD}
 53: {Browser=Safari, RAM=4096, Drive=SSD}
 54: {Browser=Safari, RAM=8192, Drive=HDD}
 55: {Browser=Safari, RAM=8192, Drive=SSD}
 56: {Browser=Safari, RAM=16384, Drive=HDD}
 57: {Browser=Safari, RAM=16384, Drive=SSD}
 58: {Browser=Edge, RAM=4096, Drive=HDD}
 59: {Browser=Edge, RAM=4096, Drive=SSD}
 60: {Browser=Edge, RAM=8192, Drive=HDD}
 61: {Browser=Edge, RAM=8192, Drive=SSD}
 62: {Browser=Edge, RAM=16384, Drive=HDD}
 63: {Browser=Edge, RAM=16384, Drive=SSD}
 64: {OS=Windows, RAM=4096, Drive=HDD}
 65: {OS=Windows, RAM=4096, Drive=SSD}
 66: {OS=Windows, RAM=8192, Drive=HDD}
 67: {OS=Windows, RAM=8192, Drive=SSD}
 68: {OS=Windows, RAM=16384, Drive=HDD}
 69: {OS=Windows, RAM=16384, Drive=SSD}
 70: {OS=Linux, RAM=4096, Drive=HDD}
 71: {OS=Linux, RAM=4096, Drive=SSD}
 72: {OS=Linux, RAM=8192, Drive=HDD}
 73: {OS=Linux, RAM=8192, Drive=SSD}
 74: {OS=Linux, RAM=16384, Drive=HDD}
 75: {OS=Linux, RAM=16384, Drive=SSD}
 76: {OS=macOS, RAM=4096, Drive=HDD}
 77: {OS=macOS, RAM=4096, Drive=SSD}
 78: {OS=macOS, RAM=8192, Drive=HDD}
 79: {OS=macOS, RAM=8192, Drive=SSD}
 80: {OS=macOS, RAM=16384, Drive=HDD}
 81: {OS=macOS, RAM=16384, Drive=SSD}

=== Generated cases (28) ===

Case 1: {Browser=Chrome, OS=Windows, RAM=4096, Drive=HDD}
  New combinations (4):
      1: {Browser=Chrome, OS=Windows, RAM=4096}
      2: {Browser=Chrome, OS=Windows, Drive=HDD}
      3: {Browser=Chrome, RAM=4096, Drive=HDD}
      4: {OS=Windows, RAM=4096, Drive=HDD}

Case 2: {Browser=Edge, OS=macOS, RAM=8192, Drive=SSD}
  New combinations (4):
      1: {Browser=Edge, OS=macOS, RAM=8192}
      2: {Browser=Edge, OS=macOS, Drive=SSD}
      3: {Browser=Edge, RAM=8192, Drive=SSD}
      4: {OS=macOS, RAM=8192, Drive=SSD}

Case 3: {Browser=Safari, OS=Linux, RAM=4096, Drive=SSD}
  New combinations (4):
      1: {Browser=Safari, OS=Linux, RAM=4096}
      2: {Browser=Safari, OS=Linux, Drive=SSD}
      3: {Browser=Safari, RAM=4096, Drive=SSD}
      4: {OS=Linux, RAM=4096, Drive=SSD}

Case 4: {Browser=Chrome, OS=Windows, RAM=16384, Drive=SSD}
  New combinations (4):
      1: {Browser=Chrome, OS=Windows, RAM=16384}
      2: {Browser=Chrome, OS=Windows, Drive=SSD}
      3: {Browser=Chrome, RAM=16384, Drive=SSD}
      4: {OS=Windows, RAM=16384, Drive=SSD}

Case 5: {Browser=Safari, OS=Linux, RAM=16384, Drive=HDD}
  New combinations (4):
      1: {Browser=Safari, OS=Linux, RAM=16384}
      2: {Browser=Safari, OS=Linux, Drive=HDD}
      3: {Browser=Safari, RAM=16384, Drive=HDD}
      4: {OS=Linux, RAM=16384, Drive=HDD}

Case 6: {Browser=Edge, OS=macOS, RAM=4096, Drive=HDD}
  New combinations (4):
      1: {Browser=Edge, OS=macOS, RAM=4096}
      2: {Browser=Edge, OS=macOS, Drive=HDD}
      3: {Browser=Edge, RAM=4096, Drive=HDD}
      4: {OS=macOS, RAM=4096, Drive=HDD}

Case 7: {Browser=Chrome, OS=Windows, RAM=8192, Drive=HDD}
  New combinations (3):
      1: {Browser=Chrome, OS=Windows, RAM=8192}
      2: {Browser=Chrome, RAM=8192, Drive=HDD}
      3: {OS=Windows, RAM=8192, Drive=HDD}

Case 8: {Browser=Chrome, OS=Linux, RAM=4096, Drive=HDD}
  New combinations (3):
      1: {Browser=Chrome, OS=Linux, RAM=4096}
      2: {Browser=Chrome, OS=Linux, Drive=HDD}
      3: {OS=Linux, RAM=4096, Drive=HDD}

Case 9: {Browser=Safari, OS=Linux, RAM=8192, Drive=SSD}
  New combinations (3):
      1: {Browser=Safari, OS=Linux, RAM=8192}
      2: {Browser=Safari, RAM=8192, Drive=SSD}
      3: {OS=Linux, RAM=8192, Drive=SSD}

Case 10: {Browser=Edge, OS=macOS, RAM=16384, Drive=SSD}
  New combinations (3):
      1: {Browser=Edge, OS=macOS, RAM=16384}
      2: {Browser=Edge, RAM=16384, Drive=SSD}
      3: {OS=macOS, RAM=16384, Drive=SSD}

Case 11: {Browser=Chrome, OS=Linux, RAM=8192, Drive=HDD}
  New combinations (2):
      1: {Browser=Chrome, OS=Linux, RAM=8192}
      2: {OS=Linux, RAM=8192, Drive=HDD}

Case 12: {Browser=Chrome, OS=Linux, RAM=16384, Drive=HDD}
  New combinations (2):
      1: {Browser=Chrome, OS=Linux, RAM=16384}
      2: {Browser=Chrome, RAM=16384, Drive=HDD}

Case 13: {Browser=Chrome, OS=macOS, RAM=4096, Drive=SSD}
  New combinations (4):
      1: {Browser=Chrome, OS=macOS, RAM=4096}
      2: {Browser=Chrome, OS=macOS, Drive=SSD}
      3: {Browser=Chrome, RAM=4096, Drive=SSD}
      4: {OS=macOS, RAM=4096, Drive=SSD}

Case 14: {Browser=Chrome, OS=macOS, RAM=8192, Drive=SSD}
  New combinations (2):
      1: {Browser=Chrome, OS=macOS, RAM=8192}
      2: {Browser=Chrome, RAM=8192, Drive=SSD}

Case 15: {Browser=Chrome, OS=macOS, RAM=16384, Drive=HDD}
  New combinations (3):
      1: {Browser=Chrome, OS=macOS, RAM=16384}
      2: {Browser=Chrome, OS=macOS, Drive=HDD}
      3: {OS=macOS, RAM=16384, Drive=HDD}

Case 16: {Browser=Safari, OS=Windows, RAM=4096, Drive=HDD}
  New combinations (3):
      1: {Browser=Safari, OS=Windows, RAM=4096}
      2: {Browser=Safari, OS=Windows, Drive=HDD}
      3: {Browser=Safari, RAM=4096, Drive=HDD}

Case 17: {Browser=Safari, OS=Windows, RAM=8192, Drive=SSD}
  New combinations (3):
      1: {Browser=Safari, OS=Windows, RAM=8192}
      2: {Browser=Safari, OS=Windows, Drive=SSD}
      3: {OS=Windows, RAM=8192, Drive=SSD}

Case 18: {Browser=Safari, OS=Windows, RAM=16384, Drive=SSD}
  New combinations (2):
      1: {Browser=Safari, OS=Windows, RAM=16384}
      2: {Browser=Safari, RAM=16384, Drive=SSD}

Case 19: {Browser=Safari, OS=macOS, RAM=4096, Drive=HDD}
  New combinations (2):
      1: {Browser=Safari, OS=macOS, RAM=4096}
      2: {Browser=Safari, OS=macOS, Drive=HDD}

Case 20: {Browser=Safari, OS=macOS, RAM=8192, Drive=HDD}
  New combinations (3):
      1: {Browser=Safari, OS=macOS, RAM=8192}
      2: {Browser=Safari, RAM=8192, Drive=HDD}
      3: {OS=macOS, RAM=8192, Drive=HDD}

Case 21: {Browser=Safari, OS=macOS, RAM=16384, Drive=SSD}
  New combinations (2):
      1: {Browser=Safari, OS=macOS, RAM=16384}
      2: {Browser=Safari, OS=macOS, Drive=SSD}

Case 22: {Browser=Edge, OS=Windows, RAM=4096, Drive=SSD}
  New combinations (4):
      1: {Browser=Edge, OS=Windows, RAM=4096}
      2: {Browser=Edge, OS=Windows, Drive=SSD}
      3: {Browser=Edge, RAM=4096, Drive=SSD}
      4: {OS=Windows, RAM=4096, Drive=SSD}

Case 23: {Browser=Edge, OS=Windows, RAM=16384, Drive=HDD}
  New combinations (4):
      1: {Browser=Edge, OS=Windows, RAM=16384}
      2: {Browser=Edge, OS=Windows, Drive=HDD}
      3: {Browser=Edge, RAM=16384, Drive=HDD}
      4: {OS=Windows, RAM=16384, Drive=HDD}

Case 24: {Browser=Edge, OS=Windows, RAM=8192, Drive=HDD}
  New combinations (2):
      1: {Browser=Edge, OS=Windows, RAM=8192}
      2: {Browser=Edge, RAM=8192, Drive=HDD}

Case 25: {Browser=Edge, OS=Linux, RAM=4096, Drive=SSD}
  New combinations (2):
      1: {Browser=Edge, OS=Linux, RAM=4096}
      2: {Browser=Edge, OS=Linux, Drive=SSD}

Case 26: {Browser=Edge, OS=Linux, RAM=8192, Drive=HDD}
  New combinations (2):
      1: {Browser=Edge, OS=Linux, RAM=8192}
      2: {Browser=Edge, OS=Linux, Drive=HDD}

Case 27: {Browser=Edge, OS=Linux, RAM=16384, Drive=SSD}
  New combinations (2):
      1: {Browser=Edge, OS=Linux, RAM=16384}
      2: {OS=Linux, RAM=16384, Drive=SSD}

Case 28: {Browser=Chrome, OS=Linux, RAM=4096, Drive=SSD}
  New combinations (1):
      1: {Browser=Chrome, OS=Linux, Drive=SSD}
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
        .withParameter(new Parameter("RAM", 4096, 8192, 16384))
        .withParameter(new Parameter("Drive", "HDD", "SSD"))
        .withRandomization(true, 42L)
        .build();
```

#### Output:

<details><summary>Generated cases</summary>

```text
  1: {Browser=Safari, OS=Linux, RAM=16384, Drive=SSD}
  2: {Browser=Chrome, OS=macOS, RAM=8192, Drive=SSD}
  3: {Browser=Edge, OS=Windows, RAM=4096, Drive=SSD}
  4: {Browser=Safari, OS=macOS, RAM=4096, Drive=HDD}
  5: {Browser=Chrome, OS=Linux, RAM=4096, Drive=HDD}
  6: {Browser=Edge, OS=Linux, RAM=8192, Drive=HDD}
  7: {Browser=Chrome, OS=Windows, RAM=16384, Drive=HDD}
  8: {Browser=Safari, OS=Windows, RAM=8192, Drive=SSD}
  9: {Browser=Edge, OS=macOS, RAM=16384, Drive=HDD}
```

</details>

<details><summary>Generation details</summary>

```text
=== Required combinations (45) ===

  1: {Browser=Chrome, OS=Windows}
  2: {Browser=Chrome, OS=Linux}
  3: {Browser=Chrome, OS=macOS}
  4: {Browser=Safari, OS=Windows}
  5: {Browser=Safari, OS=Linux}
  6: {Browser=Safari, OS=macOS}
  7: {Browser=Edge, OS=Windows}
  8: {Browser=Edge, OS=Linux}
  9: {Browser=Edge, OS=macOS}
 10: {Browser=Chrome, RAM=4096}
 11: {Browser=Chrome, RAM=8192}
 12: {Browser=Chrome, RAM=16384}
 13: {Browser=Safari, RAM=4096}
 14: {Browser=Safari, RAM=8192}
 15: {Browser=Safari, RAM=16384}
 16: {Browser=Edge, RAM=4096}
 17: {Browser=Edge, RAM=8192}
 18: {Browser=Edge, RAM=16384}
 19: {Browser=Chrome, Drive=HDD}
 20: {Browser=Chrome, Drive=SSD}
 21: {Browser=Safari, Drive=HDD}
 22: {Browser=Safari, Drive=SSD}
 23: {Browser=Edge, Drive=HDD}
 24: {Browser=Edge, Drive=SSD}
 25: {OS=Windows, RAM=4096}
 26: {OS=Windows, RAM=8192}
 27: {OS=Windows, RAM=16384}
 28: {OS=Linux, RAM=4096}
 29: {OS=Linux, RAM=8192}
 30: {OS=Linux, RAM=16384}
 31: {OS=macOS, RAM=4096}
 32: {OS=macOS, RAM=8192}
 33: {OS=macOS, RAM=16384}
 34: {OS=Windows, Drive=HDD}
 35: {OS=Windows, Drive=SSD}
 36: {OS=Linux, Drive=HDD}
 37: {OS=Linux, Drive=SSD}
 38: {OS=macOS, Drive=HDD}
 39: {OS=macOS, Drive=SSD}
 40: {RAM=4096, Drive=HDD}
 41: {RAM=4096, Drive=SSD}
 42: {RAM=8192, Drive=HDD}
 43: {RAM=8192, Drive=SSD}
 44: {RAM=16384, Drive=HDD}
 45: {RAM=16384, Drive=SSD}

=== Generated cases (9) ===

Case 1: {Browser=Safari, OS=Linux, RAM=16384, Drive=SSD}
  New combinations (6):
      1: {Browser=Safari, OS=Linux}
      2: {Browser=Safari, RAM=16384}
      3: {Browser=Safari, Drive=SSD}
      4: {OS=Linux, RAM=16384}
      5: {OS=Linux, Drive=SSD}
      6: {RAM=16384, Drive=SSD}

Case 2: {Browser=Chrome, OS=macOS, RAM=8192, Drive=SSD}
  New combinations (6):
      1: {Browser=Chrome, OS=macOS}
      2: {Browser=Chrome, RAM=8192}
      3: {Browser=Chrome, Drive=SSD}
      4: {OS=macOS, RAM=8192}
      5: {OS=macOS, Drive=SSD}
      6: {RAM=8192, Drive=SSD}

Case 3: {Browser=Edge, OS=Windows, RAM=4096, Drive=SSD}
  New combinations (6):
      1: {Browser=Edge, OS=Windows}
      2: {Browser=Edge, RAM=4096}
      3: {Browser=Edge, Drive=SSD}
      4: {OS=Windows, RAM=4096}
      5: {OS=Windows, Drive=SSD}
      6: {RAM=4096, Drive=SSD}

Case 4: {Browser=Safari, OS=macOS, RAM=4096, Drive=HDD}
  New combinations (6):
      1: {Browser=Safari, OS=macOS}
      2: {Browser=Safari, RAM=4096}
      3: {Browser=Safari, Drive=HDD}
      4: {OS=macOS, RAM=4096}
      5: {OS=macOS, Drive=HDD}
      6: {RAM=4096, Drive=HDD}

Case 5: {Browser=Chrome, OS=Linux, RAM=4096, Drive=HDD}
  New combinations (5):
      1: {Browser=Chrome, OS=Linux}
      2: {Browser=Chrome, RAM=4096}
      3: {Browser=Chrome, Drive=HDD}
      4: {OS=Linux, RAM=4096}
      5: {OS=Linux, Drive=HDD}

Case 6: {Browser=Edge, OS=Linux, RAM=8192, Drive=HDD}
  New combinations (5):
      1: {Browser=Edge, OS=Linux}
      2: {Browser=Edge, RAM=8192}
      3: {Browser=Edge, Drive=HDD}
      4: {OS=Linux, RAM=8192}
      5: {RAM=8192, Drive=HDD}

Case 7: {Browser=Chrome, OS=Windows, RAM=16384, Drive=HDD}
  New combinations (5):
      1: {Browser=Chrome, OS=Windows}
      2: {Browser=Chrome, RAM=16384}
      3: {OS=Windows, RAM=16384}
      4: {OS=Windows, Drive=HDD}
      5: {RAM=16384, Drive=HDD}

Case 8: {Browser=Safari, OS=Windows, RAM=8192, Drive=SSD}
  New combinations (3):
      1: {Browser=Safari, OS=Windows}
      2: {Browser=Safari, RAM=8192}
      3: {OS=Windows, RAM=8192}

Case 9: {Browser=Edge, OS=macOS, RAM=16384, Drive=HDD}
  New combinations (3):
      1: {Browser=Edge, OS=macOS}
      2: {Browser=Edge, RAM=16384}
      3: {OS=macOS, RAM=16384}
```

</details>

### Generation Feedback

Enable `printGenerationDetails(true)` to print required combinations, final cases, and their counts to `System.out` (off by default).
Each case lists only combinations not covered by earlier cases.


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
