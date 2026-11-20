# SM9 Identity-Based Signature — Configuration, Testing & Verification

[![build](https://github.com/marry-tech/sm9-signature-java/actions/workflows/build.yml/badge.svg?branch=main)](https://github.com/marry-tech/sm9-signature-java/actions/workflows/build.yml)

This repository presents a systematic verification case study of the SM9 identity-based signature scheme (Chinese national standard GM/T 0044.2). Built on the JPBC pairing library, it expands a baseline implementation with a rigorous test suite, mutation analysis, standard conformance checks and performance benchmarks, to explore the boundary between "tests pass" and "implementation correctness".

> **Last updated: November 2026** — undergraduate thesis project (2021–22), revisited in 2026 as a systematic verification exercise.

## Verification at a glance

| Metric | Result |
|---|---|
| JUnit test methods | **42**, across six systematic categories |
| Mutation testing | **28 of 33 mutants killed** — mutation score 84.8%, test strength 100% (PITest, scoped to `SM9`, `KGC`, `SM9Curve`) |
| Signature verification | **≈1067 ms** per operation, single-threaded (≈0.9 ops/s) |
| Parallel scaling | 0.9 → 2 ops/s from 1 to 4 threads; 8 threads shows no further gain |
| Standard | GM/T 0044.2 (SM9 identity-based signature, part 2) |
| Official test vector | `h` and `S` match **GM/T 0044.5 Annex A byte-for-byte** — asserted in `SM9ConformanceTest#testOfficialTestVectorGMT00445AnnexA` |


## Background

This repository documents a two-phase verification exercise:

- **Phase 1 (2021–22, graduation thesis):** Configured an SM9 signature implementation in Java using the JPBC pairing library. JPBC's default Type-A curve (k=2, supersingular, symmetric pairing) is structurally incompatible with SM9's Type-F parameters (BN curve, k=12, asymmetric R-ate pairing), so the standard parameter set was loaded explicitly via `PropertiesParameters`. Output was verified against the official deterministic test vectors from GM/T 0044.5 Annex A, with preliminary input validation and abnormal-input tests added.
- **Phase 2 (2026):** Returned to the project to address a persistent question: if all functional tests pass, how do I know the implementation is actually correct? Expanded the preliminary work into a systematic verification suite: explicit input validation, 42 JUnit tests across six categories, mutation testing, robustness probing, and a batch-processing performance benchmark. Documented what the tests can and cannot prove.

**Core realisation:** functional test coverage measures exhaustiveness, not correctness. Passing tests do not guarantee the absence of bugs — a limitation that motivates graduate-level training in formal verification and systematic software reliability engineering.

## Repository Structure

| Directory / File | Description |
|---|---|
| `src/main/java/com/sm9/` | Core SM9 implementation (Java, JPBC) |
| `src/main/java/com/sm9pairing/`, `com/transfer/`, `com/aa/` | Pairing backend, hex conversion utility, console output helper (retained from the original implementation) |
| `src/test/java/com/sm9/` | 6 JUnit test classes, 42 test methods |
| `src/test/java/com/sm9/SM9Test.java` | Legacy manual demo (not part of the JUnit suite) |
| `src/main/java/com/sm9/batch/` | Serial and parallel batch benchmark (compiled in CI) |
| `docs/TEST_REPORT.md` | Test classification, traceability matrix, results |
| `docs/MUTATION_TESTING.md` | PITest report and surviving-mutant analysis |
| `docs/TEST_SPEC.md` | Test objectives, strategy, exit criteria |
| `docs/LESSONS_LEARNED.md` | Reflections on verification limits |

## Quick Start

```bash
# Compile
mvn compile

# Run all tests
mvn test

# Run a single test class
mvn test -Dtest=SM9FunctionalTest

# Run mutation testing
mvn test-compile org.pitest:pitest-maven:mutationCoverage
```

Every push to `main` re-runs this suite on GitHub Actions; the badge at the top of this file always reflects the latest result.

## Design Philosophy

This project treats SM9 not as a product but as a testbed for verification methodology. Every design decision is documented and traceable to a specific risk point:

- **Traceability:** each test case maps to an identifiable risk (e.g., "cryptographic forgery acceptance"), documented in source comments and the test report.
- **Risk-driven testing:** tests are designed from failure modes, not from happy-path requirements.
- **Honest limitations:** the repository explicitly documents what the test suite cannot detect — including protocol-level deviations, mathematical errors that still produce valid-looking outputs, and side-channel leaks.

## What Was Done

### 1. Parameter Mapping & Standard Compliance

Mapped the SM9 standard curve parameters into JPBC's configuration, replacing its default Type-A parameter set. Because Type-A (k=2, symmetric) cannot represent the SM9 BN curve (k=12, asymmetric R-ate pairing), the standard parameters are loaded explicitly via `PropertiesParameters` (type=f). Output verified against the official deterministic test vectors from GM/T 0044.5 Annex A: SM9ConformanceTest#testOfficialTestVectorGMT00445AnnexA asserts the signature h and S byte-for-byte against the published values.


### 2. Systematic Test Suite

42 JUnit tests in 6 categories:
All test cases are designed from failure modes and risk points, not just happy-path functionality:

| Category | Count | Focus |
|---|---|---|
| Functional | 6 | Normal sign/verify workflows |
| Boundary | 13 | Null, empty, oversize, unicode edge inputs |
| Exception | 7 | Tampering, wrong keys, replay attacks |
| Conformance | 6 | Standard compliance (G1 membership, pairing equations) |
| Parameter Mapping | 5 | JPBC vs SM9 parameter correctness |
| Robustness | 5 | Malformed encoding, mixed charset, control characters |
| **Total** | **42** | |

### 3. Mutation Testing (PITest)

28 of 33 generated mutants were killed (mutation score 84.8%, test strength 100%). Five mutants survived, revealing gaps in protocol-level conformance verification and boundary-value tightness. See `docs/MUTATION_TESTING.md` for per-class analysis.

Mutation score is the more meaningful measure of test strength. Core behavioural classes reach 92% (KGC) and 84% (SM9), with all 5 surviving mutants fully analyzed in `docs/MUTATION_TESTING.md`.

### 4. Performance Exploration

The benchmark sources compile as part of the Maven build (`src/main/java/com/sm9/batch/`), but they are not part of the 42-test suite and are not executed in CI.

Observed scaling is 0.9 → 2 ops/s from 1 to 4 threads, with no further gain at 8 threads. That is far below linear, consistent with a sequential fraction dominated by the pairing computation — an Amdahl's-law limit that measurement alone cannot remove. Two measurement conditions (cold start and steady state) are reported separately in `docs/PERFORMANCE_NOTES.md`, because the first run absorbs a one-off curve-setup cost that later runs do not.


### 5. Input Validation

Added explicit null checks and argument validation to core cryptographic methods (`KGC.java`, `SM9.java`).

## What Was Not Done (and Why)

The following are explicitly out of scope. They require methodologies beyond the current skill set — and are part of the motivation for graduate study:

- **Formal verification:** no model checking, theorem proving, or invariant specification
- **Performance optimisation:** no algorithmic improvement, native implementation, or GPU acceleration
- **Side-channel resistance:** no timing analysis, power analysis, or constant-time implementation
- **Cryptographic extensions:** no encryption, key encapsulation, or additional SM9 modes

These gaps in formal methods and software reliability engineering are exactly what I aim to address through graduate-level study.

## Limitations & Known Issues

- The baseline cryptographic implementation is derived from publicly available reference materials. This work focuses on verification methodology and test system construction, not original cryptographic algorithm research.
- Functional tests cannot verify protocol-level mathematical correctness.
- Robustness tests reveal that malformed inputs are silently accepted rather than rejected.
- Mutation testing confirms that passing tests do not guarantee correctness.
- The performance benchmark is a learning exercise, not an optimised implementation.

## Licence

This repository does not include a software licence. The core cryptographic code is derived from publicly shared reference materials (CSDN) and is presented here for educational and verification purposes only. It is not intended for production cryptographic use.
