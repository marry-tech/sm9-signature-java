# SM9 Identity-Based Signature (Java)

Java implementation of the SM9 identity-based digital signature scheme (GM/T 0044-2016), built on the JPBC library for bilinear pairing operations.

## Background

This project originated as my undergraduate graduation thesis at Qilu Normal University (Sep 2021 -- Jun 2022). The core cryptographic implementation was referenced from open-source materials on CSDN. My own work focused on:

- Studying the GM/T 0044 protocol specification and mapping its mathematical structure to Java classes
- Configuring the JPBC library to use SM9-standard curve parameters instead of its default Type-A parameters
- Getting the protocol to run correctly and verifying output against the standard's test vectors

After graduation, I interviewed for IT positions at state-owned enterprises. During technical interviews, I was asked how to design testing strategies for distributed systems and what metrics to use for software reliability assessment. I could describe my projects, but could not confidently answer methodological questions on systematic quality assurance or architecture-level verification. This gap -- between writing code that passes functional tests and reasoning about system-level reliability -- became the focus of my subsequent work.

I returned to this SM9 project in 2026 and treated it as a controlled experiment: what would it take to verify that this implementation is not just functionally correct, but handles failures predictably? I added explicit input validation to core methods, then designed and wrote 37 JUnit tests covering normal operation, boundary inputs, exception scenarios, conformance checks and parameter mapping. I documented the process and limitations in an English-language test report. This experiment confirmed that systematic reliability verification requires structured methodological training beyond what I could acquire through unstructured self-study.

## What This Repository Includes

- **Core Implementation:** SM9 key generation center, signature generation and verification (Java)
- **Parameter Mapping:** Analysis and documentation of the differences between JPBC default Type-A curve parameters and the SM9 standard definition
- **Input Validation:** Explicit argument checks added to core methods to prevent silent failures from invalid inputs
- **Test Suites:** 5 JUnit test classes with 37 test methods
  - Functional tests -- normal sign/verify round trips
  - Boundary tests -- null inputs, empty strings, oversized messages, unicode identifiers
  - Exception tests -- tampered signatures, wrong keys, altered messages, replay scenarios
  - Conformance tests -- group membership verification (G1, G2, Zr) against GM/T 0044 structure
  - Parameter mapping tests -- verification that SM9 parameters are explicitly configured and differ from JPBC defaults
- **Test Report:** `docs/TEST_REPORT.md` -- analysis of test coverage and why black-box functional testing is insufficient for cryptographic reliability

## Running the Tests

Requirements: JDK 8+, Maven 3.6+

```bash
mvn clean compile
mvn test
```

Run individual test classes:
```bash
mvn test -Dtest=SM9FunctionalTest
mvn test -Dtest=SM9BoundaryTest
mvn test -Dtest=SM9ExceptionTest
mvn test -Dtest=SM9ConformanceTest
mvn test -Dtest=ParameterMappingTest
```

## Project Structure

```
src/
  main/java/com/sm9/
    SM9.java                    -- signature algorithm
    KGC.java                    -- key generation center
    SM9Curve.java               -- curve setup with SM9 parameters
    SM9CurveParameters.java     -- SM9 parameter definitions (GM/T 0044)
    JPBCDefaultComparison.java  -- demonstrates JPBC default vs SM9 param differences
    (additional helper classes)
  main/java/com/sm9pairing/
    SM9Pairing.java             -- pairing operations
    SM9RatePairingMap.java      -- pairing map utilities
  main/java/com/transfer/
    transfer.java               -- data transfer utilities
  main/java/com/aa/
    Main.java                   -- console output helper
  test/java/com/sm9/
    SM9FunctionalTest.java      -- 6 tests: normal operation paths
    SM9BoundaryTest.java        -- 13 tests: null, empty, oversized, unicode inputs
    SM9ExceptionTest.java       -- 7 tests: tampered sigs, wrong keys, replay attacks
    SM9ConformanceTest.java     -- 6 tests: group membership, standard vectors
    ParameterMappingTest.java   -- 5 tests: SM9 params vs JPBC defaults
docs/
  TEST_REPORT.md              -- test coverage analysis and limitations
```

## Key Technical Notes

**JPBC default parameters are NOT compatible with SM9.** JPBC's built-in Type-A curve uses a pre-configured parameter set optimized for general pairing-based cryptography, but it does not conform to the SM9 national standard. The field order, subgroup order and base point configuration are all different. This project uses a custom `PropertiesParameters` configuration that maps the SM9 standard parameters into JPBC's format. A comparison utility is included to show the differences side-by-side.
The signature scheme uses the SM3 cryptographic hash function (GM/T 0004) via BouncyCastle, rather than SHA-256 as used in generic pairing-based constructions.

## Limitations

- Black-box functional testing is necessary but not sufficient for cryptographic software reliability
- Error handling is basic: parameter validation and null checks exist, but there is no systematic failure classification
- No formal verification or mathematical proof of correctness
- No performance benchmarking

This is an educational implementation: it works, it passes tests, but it is not production-grade cryptography.

## References

- GM/T 0044-2016: SM9 Identity-Based Cryptographic Algorithm
- JPBC Library: http://gas.dia.unisa.it/projects/jpbc/
