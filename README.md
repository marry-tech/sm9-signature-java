# SM9 Identity-Based Signature: Testing & Reliability Revision

This is a Java project for the SM9 identity-based digital signature standard (GM/T 0044.2), built on the JPBC pairing library. It started as my undergraduate thesis work and was re-organized, tested and documented in November 2026.

---

## Background

### Phase 1: Undergraduate Thesis (Sep 2021 – Jun 2022)
I studied the SM9 standard for my bachelor's thesis at Qilu Normal University, supervised by Associate Professor Yuquan Zhang. At that stage my work focused on:
- Reading through the GM/T 0044 specification and understanding the mathematical structure of the identity-based signature scheme
- Mapping the standard curve parameters into the JPBC library configuration, replacing its default Type-A parameter set
- Getting the full sign/verify flow to run correctly and verifying output against the standard test vector
- Adding basic boundary checks to avoid obvious crashes from invalid inputs

The core cryptographic implementation was based on open-source reference materials. My thesis did not aim to invent a new algorithm, but to understand how a pairing-based protocol works in practice and make it run reliably under different inputs.

### Phase 2: Systematic Revision (Nov 2026)
After graduation I attended technical interviews for state-owned enterprise IT positions. I was asked how to design test strategies for distributed systems and how to assess software reliability systematically. I could describe my projects, but I could not answer those methodological questions with confidence.

That experience made me realize: I could write code that works on the happy path, but I did not have a clear framework for verifying system-level reliability.

In November 2026 I came back to this project and used it as a concrete case to practice what I had been reading about software testing:
- I added explicit input validation at every core method entry point, so invalid inputs fail clearly instead of silently
- I designed 37 JUnit test cases covering normal paths, boundary inputs, exception scenarios, standard conformance and parameter configuration
- I wrote an English test report documenting the test design and the limitations I found

This exercise confirmed one thing for me: systematic reliability verification requires proper graduate-level training, not just ad-hoc self-study. That is why I am applying for this MSc programme.

> Note: The standard test-vector verification harness (`cmo/test/SM9Test.java`, `SM9WithStandardTestKey`, `KGCWithStandardTestKey`) comes from the reference implementation and is included to reproduce the GM/T 0044 test vector. The JUnit test suite, input validation, project structure reorganization, README and test report are my own work.

---

## What this repository contains

### Core implementation
- Key generation center (KGC) for signing master keys and user private key extraction
- Signature generation and verification following GM/T 0044.2
- Custom curve configuration loaded with SM9 standard parameters, not the JPBC default Type-A curve

### Testing work
5 JUnit test classes, 37 test cases in total:
- **Functional tests** (6): normal sign-verify round trips, different user identities, repeated signing
- **Boundary tests** (13): null inputs, empty strings, oversized messages, Unicode identifiers
- **Exception tests** (7): tampered signatures, wrong public keys, altered messages, signature reuse
- **Conformance tests** (6): group membership checks for G1/G2/Zr, standard identifier verification
- **Parameter mapping tests** (5): verification that SM9 parameters are correctly loaded and differ from JPBC defaults

### Documentation
- `docs/TEST_REPORT.md`: test design approach, coverage analysis and limitations of black-box testing
- `docs/THESIS_ABSTRACT.md`: original bachelor thesis abstract
  
---

## Running the project

### Requirements
- JDK 8 or higher
- Maven 3.6 or higher

> The JPBC library jars are included in the `lib/` directory and referenced via system scope in `pom.xml`, because JPBC 2.0.0 is no longer available on Maven Central. No extra repository setup is needed. The SM3 hash function used by SM9 is provided by BouncyCastle, which resolves normally from Maven Central.

### Run all tests
mvn clean test

### Run a single test class
-mvn test -Dtest=SM9FunctionalTest
-mvn test -Dtest=SM9BoundaryTest
-mvn test -Dtest=SM9ExceptionTest
-mvn test -Dtest=SM9ConformanceTest
-mvn test -Dtest=ParameterMappingTest

### Run the standard test-vector demo
-The main class reproduces the deterministic test vector from GM/T 0044.2 Appendix D with fixed random values.
-mvn compile exec:java -Dexec.mainClass="com.aa.Main"
-Expected output includes `h2 = h, 验证通过!` and `verify OK`, which confirms the implementation matches the standard specification.
-Verified locally: `mvn test` on Maven 3.9.6 / JDK 11 → 37 tests, 0 failures.

---

## Key technical notes

1. **JPBC defaults are not compatible with SM9.** The built-in Type-A curve uses a general-purpose parameter set optimized for speed, but it does not match the SM9 national standard. The field order, subgroup order and base point are all different. This project loads the standard parameters explicitly through a custom `PropertiesParameters` configuration.
2. **SM3 hash instead of SHA-256.** The signature scheme uses the SM3 cryptographic hash function (GM/T 0004) provided by BouncyCastle, not SHA-256 as used in most common pairing-based constructions.
3. **Input validation is defensive, not optional.** All public methods check for null or empty arguments and throw explicit `IllegalArgumentException`s. Silent failures from bad inputs are one of the most common and hardest-to-debug issues in cryptographic libraries.

---

## Limitations

This is an educational implementation, not production-grade cryptography.

- Black-box functional testing proves the happy path works and common failures behave predictably, but it cannot guarantee full correctness. Subtle arithmetic deviations can still produce internally consistent outputs that pass black-box checks.
- Error handling is basic. There is input validation, but no systematic failure mode classification or error taxonomy.
- No formal verification, no mathematical proof of correctness.
- No performance benchmarking or side-channel resistance analysis.

These limitations are exactly why I want to pursue graduate study in software verification and dependable systems: I want to move beyond trial-and-error testing to structured methodologies for building reliable systems.

---

## References

- GM/T 0044-2016: SM9 Identity-Based Cryptographic Algorithm
- JPBC Library: [http://gas.dia.unisa.it/projects/jpbc/](http://gas.dia.unisa.it/projects/jpbc/)



