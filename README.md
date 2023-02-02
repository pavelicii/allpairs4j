# AllPairs4J

[![GitHub Workflow Status](https://img.shields.io/github/actions/workflow/status/pavelicii/allpairs4j/build-checkstyle.yaml?branch=master&logo=GitHub)](https://github.com/pavelicii/allpairs4j/actions/workflows/build-checkstyle.yaml)
[![Maven Central](https://img.shields.io/maven-central/v/io.github.pavelicii/allpairs4j)](https://search.maven.org/artifact/io.github.pavelicii/allpairs4j)

AllPairs4J is an open source Java library for generation of minimal set of test combinations. 

AllPairs4J is a Java port of [allpairspy](https://github.com/thombashi/allpairspy) project
(with some [improvements and bugfixes](https://github.com/thombashi/allpairspy/pull/10))
developed by MetaCommunications Engineering and Tsuyoshi Hombashi.

## Pairwise Testing

In computer science, _all-pairs testing_ or _pairwise testing_ is a combinatorial method of software testing that, for 
each pair of input parameters to a system, tests all possible discrete combinations of those parameters. For example, 
if you want to create a test suite for browser testing, the domain can be described by the following parameters:

```text
Browser:    Chrome, Firefox, Safari, Edge
OS:         Windows, Linux, macOS
RAM:        1024, 2048, 4096, 8192, 16384
Drive:      HDD, SSD
```

There are hundreds of possible combinations of these values. Instead of spending unreasonable amount of time testing
them all, pairwise suggests testing all possible pairs of values. For example, `{Chrome, Windows}` is one pair, 
`{4096, SSD}` is another; together they represent a test case that covers many pairs: `{Chrome, Windows, 4096, SSD}`. 
In the end, you have good coverage while the number of test cases remains manageable.

With AllPairs4J, you are also able to add limitations on the domain to restrict generation of certain pairs. 
For example, specify that `Safari` can only be paired with `macOS`, and `Edge` can only be paired with `Windows`.

For more info on pairwise testing see https://www.pairwise.org/.

## Features

* Specify **constraints** to add limitations on the test combinations generation.
* Generate **pair**wise, **triple**wise, **n**-wise test combinations.
* Integrate with **any Java project**. There are pairwise tools that can sometimes be faster or generate smaller set
  covering all the test combinations ([pict](https://github.com/microsoft/pict) is probably your best choice). 
  However, they are missing features mentioned above and/or do not integrate with Java.

## Installation

### Requirements

Java 8 or higher.

### Gradle

```groovy
dependencies {
    implementation 'io.github.pavelicii:allpairs4j:1.0.0'
}
```

### Maven

```xml
<dependency>
    <groupId>io.github.pavelicii</groupId>
    <artifactId>allpairs4j</artifactId>
    <version>1.0.0</version>
</dependency>
```

## Usage

### Generate Pairwise Combinations

You can configure AllPairs generation using `AllPairsBuilder`. After it is built, the test cases are generated.\
As a minimal setup, you need to describe Parameters to generate test combinations from.

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
  7: {Browser=Chrome, OS=Linux, RAM=8192, Drive=SSD}
  8: {Browser=Safari, OS=macOS, RAM=2048, Drive=SSD}
  9: {Browser=Edge, OS=Windows, RAM=4096, Drive=HDD}
 10: {Browser=Edge, OS=Windows, RAM=2048, Drive=HDD}
 11: {Browser=Safari, OS=macOS, RAM=16384, Drive=HDD}
 12: {Browser=Chrome, OS=Linux, RAM=16384, Drive=SSD}
 13: {Browser=Safari, OS=Linux, RAM=8192, Drive=SSD}
 14: {Browser=Chrome, OS=Windows, RAM=8192, Drive=HDD}
 15: {Browser=Edge, OS=Linux, RAM=2048, Drive=HDD}
```

</details>

### Generate Filtered Pairwise Combinations

To limit generated pairs, you need to describe Constraints. Each Constraint is a Predicate that is tested against 
each generated Case. If it evaluates to `true`, the Case is deleted from the result.

For example, let's create limitations so that: 
* `Browser=Safari` can only be paired with `OS=macOS`
* `Browser=Edge` can only be paired with `OS=Windows`
* `RAM` can't be less than `4000`

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

int index = 1;
for (Case c : allPairs) {
    System.out.printf("%3d: %s%n", index++, c);
}
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

### Generate Triplewise Combinations

You can specify test combination size to go beyond pairs.

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

System.out.println(allPairs.toString());
```

#### Output:

<details><summary>Show</summary>

```text
  1: {Browser=Chrome, OS=Windows, RAM=2048, Drive=HDD}
  2: {Browser=Safari, OS=Linux, RAM=4096, Drive=HDD}
  3: {Browser=Edge, OS=macOS, RAM=8192, Drive=HDD}
  4: {Browser=Edge, OS=macOS, RAM=16384, Drive=SSD}
  5: {Browser=Safari, OS=Linux, RAM=16384, Drive=SSD}
  6: {Browser=Chrome, OS=Windows, RAM=8192, Drive=SSD}
  7: {Browser=Chrome, OS=Windows, RAM=4096, Drive=HDD}
  8: {Browser=Safari, OS=Linux, RAM=2048, Drive=HDD}
  9: {Browser=Edge, OS=macOS, RAM=2048, Drive=HDD}
 10: {Browser=Edge, OS=macOS, RAM=4096, Drive=HDD}
 11: {Browser=Safari, OS=Linux, RAM=8192, Drive=SSD}
 12: {Browser=Chrome, OS=Windows, RAM=16384, Drive=SSD}
 13: {Browser=Chrome, OS=Windows, RAM=16384, Drive=HDD}
 14: {Browser=Safari, OS=Linux, RAM=4096, Drive=SSD}
 15: {Browser=Edge, OS=macOS, RAM=2048, Drive=SSD}
 16: {Browser=Edge, OS=macOS, RAM=8192, Drive=SSD}
 17: {Browser=Safari, OS=Linux, RAM=8192, Drive=HDD}
 18: {Browser=Chrome, OS=Windows, RAM=2048, Drive=SSD}
 19: {Browser=Chrome, OS=Windows, RAM=4096, Drive=SSD}
 20: {Browser=Safari, OS=Linux, RAM=16384, Drive=HDD}
 21: {Browser=Edge, OS=macOS, RAM=16384, Drive=HDD}
 22: {Browser=Edge, OS=macOS, RAM=4096, Drive=SSD}
 23: {Browser=Safari, OS=Linux, RAM=2048, Drive=SSD}
 24: {Browser=Chrome, OS=Windows, RAM=8192, Drive=HDD}
 25: {Browser=Chrome, OS=Linux, RAM=8192, Drive=HDD}
 26: {Browser=Safari, OS=macOS, RAM=2048, Drive=SSD}
 27: {Browser=Edge, OS=Windows, RAM=4096, Drive=SSD}
 28: {Browser=Edge, OS=Windows, RAM=16384, Drive=HDD}
 29: {Browser=Safari, OS=macOS, RAM=16384, Drive=HDD}
 30: {Browser=Chrome, OS=Linux, RAM=4096, Drive=SSD}
 31: {Browser=Chrome, OS=Linux, RAM=2048, Drive=SSD}
 32: {Browser=Safari, OS=macOS, RAM=8192, Drive=HDD}
 33: {Browser=Edge, OS=Windows, RAM=8192, Drive=HDD}
 34: {Browser=Safari, OS=macOS, RAM=4096, Drive=SSD}
 35: {Browser=Edge, OS=Windows, RAM=2048, Drive=SSD}
 36: {Browser=Chrome, OS=Linux, RAM=16384, Drive=HDD}
 37: {Browser=Chrome, OS=macOS, RAM=16384, Drive=HDD}
 38: {Browser=Edge, OS=Linux, RAM=2048, Drive=SSD}
 39: {Browser=Safari, OS=Windows, RAM=4096, Drive=SSD}
 40: {Browser=Safari, OS=Windows, RAM=8192, Drive=HDD}
 41: {Browser=Edge, OS=Linux, RAM=8192, Drive=HDD}
 42: {Browser=Chrome, OS=macOS, RAM=4096, Drive=SSD}
 43: {Browser=Chrome, OS=macOS, RAM=2048, Drive=SSD}
 44: {Browser=Edge, OS=Linux, RAM=16384, Drive=HDD}
 45: {Browser=Safari, OS=Windows, RAM=16384, Drive=HDD}
 46: {Browser=Safari, OS=Windows, RAM=2048, Drive=SSD}
 47: {Browser=Edge, OS=Linux, RAM=4096, Drive=SSD}
 48: {Browser=Chrome, OS=macOS, RAM=8192, Drive=HDD}
```

</details>

### Configuration summary

#### Sample code:

```java
AllPairs allPairs = new AllPairs.AllPairsBuilder()
        .withParameters( List<Parameter> )        // required
        .withParameter( Parameter )               // alternative way to specify Parameters one by one
        .withConstraints( List<Predicate<Case>> ) // not required, default is no Constraints
        .withConstraint( Predicate<Case> )        // alternative way to specify Constraints one by one
        .withTestCombinationSize( int )           // not required, default is 2
        .printEachCaseDuringGeneration()          // not required, useful for debug
        .build();

List<Case> generatedCases = allPairs.getGeneratedCases();
```

#### Data types:

* **Parameter**: Named `List<Object>` storing all input values.
* **Case**: `Map<String, Object>` storing one generated test case,
  where `key` is mapped to the `Parameter` name, `value` is mapped to one of the `Parameter` values.
* **Predicate\<Case\>**: constraint to test against `Case`. When evaluates to `true`, the `Case` is considered invalid
  and is filtered out form the resulting set.

## Contributing

Pull requests are welcome. For major changes, please open an issue first to discuss what you would like to change.

Please make sure to update tests as appropriate.

[SemVer](https://semver.org/) is used for versioning. For the versions available, 
see the [releases on this repository](https://github.com/pavelicii/allpairs4j/releases).
