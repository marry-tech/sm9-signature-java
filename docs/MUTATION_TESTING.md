# Mutation Testing Report

## Tool and Configuration

- **Tool**: PITest (pitest-maven 1.15.3)
- **Scope**: Classes in `com.sm9.*` and tests in `com.sm9.*`
- **Classes scanned**: 3 (KGC.java, SM9.java, SM9Curve.java)
- **Output format**: HTML

## Results Summary

| Metric | Value |
|--------|-------|
| Total Mutants Generated | 33 |
| Killed | 28 |
| Survived | 5 |
| Mutation Coverage | 85% |
| Test Strength | 100% |

## Breakdown by Class

| Class | Line Coverage | Mutation Coverage | Mutants Generated | Killed | Survived |
|-------|--------------|-------------------|-------------------|--------|----------|
| KGC.java | 81% (21/26) | 92% (11/12) | 12 | 11 | 1 |
| SM9.java | 88% (35/40) | 84% (16/19) | 19 | 16 | 3 |
| SM9Curve.java | 31% (16/52) | 50% (1/2) | 2 | 1 | 1 |

## Surviving Mutants Analysis

Five mutants survived across the scanned classes. Their survival reveals specific gaps in the test suite:

### 1. KGC.java — 1 surviving mutant

The surviving mutant involved the removal of a boundary condition check in key generation. The test suite validates null and empty inputs but does not tightly constrain the upper bounds of identity string length or the exact format of curve point encoding. When the check was removed, no existing test produced a visibly different output because JPBC's internal parsing masked the deviation at the API level.

**Implication**: Input validation coverage is present but not exhaustive. Edge cases at the boundary of valid domains are not fully locked down.

### 2. SM9.java — 3 surviving mutants

Two surviving mutants replaced constants in the signing and verification flow with default values. The functional tests continued to pass because the replacement values still produced mathematically valid (but non-compliant) pairing outputs. A third mutant altered the order of operations in a sequence that the current tests treat as atomic.

**Implication**: Functional tests verify that sign/verify round-trips succeed, but they do not verify that the internal steps follow the exact sequence and constants prescribed by GM/T 0044.2. Conformance testing at the protocol level is weaker than functional testing.

### 3. SM9Curve.java — 1 surviving mutant

Only 2 mutants were generated for this class due to its limited test coverage (31% line coverage). The single surviving mutant modified a parameter initialisation path that is exercised by the constructor but not explicitly verified by any test assertion.

**Implication**: Low line coverage in SM9Curve.java means fewer mutants are generated, which artificially inflates the mutation coverage percentage. This is not a strength — it is a warning that large portions of the curve configuration code are untested.

## Critical Observations

### What "100% test strength" does and does not mean

100% test strength means every mutant that was killed was killed by a test that explicitly failed. This sounds ideal, but it is partly an artifact of the small number of classes scanned (3) and the limited mutant operators applied by PITest to Java pairing-based cryptography code. Many complex arithmetic paths in JPBC's underlying library were not mutated at all because they are outside the `com.sm9` package scope.

### Mutation coverage does not prove correctness

85% mutation coverage does not mean 85% of bugs are caught. It means 85% of *automatically generated code mutations* were caught. PITest cannot generate mutants for:
- Protocol-level deviations from the GM/T 0044.2 standard
- Mathematical errors in pairing computation that still produce valid-looking outputs
- Timing or side-channel leaks
- Race conditions in multithreaded execution

These are exactly the vulnerabilities that motivated my verification work — and they are invisible to mutation testing.

## Conclusion

Mutation testing confirmed two things:

1. **What the test suite does well**: The 42 functional, boundary and exception tests detect a high proportion of simple code-level faults. Input validation and exception-path coverage are relatively strong.

2. **What the test suite cannot do**: No amount of mutation testing can verify protocol compliance, detect subtle mathematical deviations, or prove absence of bugs in cryptographic software. Surviving mutants in SM9.java exposed that functional correctness and standard conformance are not the same thing. Low line coverage in SM9Curve.java exposed that large portions of the codebase are simply untested.

This experiment reinforces the central argument of my verification work: testing is necessary but insufficient. Moving from "tests pass" to "system is correct" requires graduate-level training in formal specification, model-based verification and rigorous software engineering methodology — which is why I am applying to Politecnico di Milano.


