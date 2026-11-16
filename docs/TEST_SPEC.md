# SM9 Identity-Based Signature — Test Specification

## 1. Scope and Objectives

### 1.1 System Under Test
The SM9 identity-based signature component implemented in Java using the JPBC pairing library. The component exposes three core operations:
- Key generation (KGC)
- Signing (SM9.sign)
- Verification (SM9.verify)

### 1.2 Test Objectives

Verify that the SM9 signature component behaves correctly under:
1. **Normal operational conditions** — functional correctness for valid inputs
2. **Boundary and edge-case inputs** — defensive robustness at domain boundaries
3. **Exception and security scenarios** — rejection of tampering, wrong keys, replay
4. **Protocol conformance** — mathematical compliance with GM/T 0044.2
5. **Parameter mapping correctness** — accurate configuration of JPBC with SM9 parameters
6. **Malformed input handling** — explicit rejection of encoding violations and garbage data

### 1.3 What This Specification Does Not Cover

The following are explicitly out of scope and documented as limitations:
- Formal proof of pairing computation correctness
- Timing or side-channel resistance
- Comparison with independently implemented SM9 verifiers
- Performance under adversarial load (see `batch_multithread/PERFORMANCE_NOTES.md` for preliminary exploration)

## 2. Test Strategy

### 2.1 Equivalence Class Partitioning
Inputs are partitioned into:
- **Valid equivalence class:** inputs within the domain defined by GM/T 0044.2
- **Invalid equivalence class:** inputs outside that domain

One representative from each class is tested.

### 2.2 Boundary Value Analysis
Tests focus on the exact boundaries of valid domains:
- Null (absence of value)
- Empty string (zero-length value)
- Minimum valid length
- Maximum valid length
- Just-outside-range values
- Unicode and non-ASCII encodings

### 2.3 Fault Injection
Invalid and malformed inputs are injected to verify that the component:
- Does not crash (graceful degradation)
- Does not silently accept invalid data
- Produces a controlled exception or rejection

### 2.4 Mutation Testing
PITest evaluates test suite quality by introducing small code mutations and measuring detection rate. A high kill rate indicates strong test sensitivity; surviving mutants reveal specific gaps in coverage.

## 3. Test Categories and Exit Criteria

| Category | Class | Count | Entry Criteria | Exit Criteria |
|----------|-------|-------|---------------|--------------|
| Functional | SM9FunctionalTest | 6 | Core implementation compiles and passes basic demo | All tests pass |
| Boundary | SM9BoundaryTest | 13 | Input validation layer exists | All tests pass |
| Exception | SM9ExceptionTest | 7 | Sign/verify round-trip is functional | All tests pass |
| Conformance | SM9ConformanceTest | 6 | SM9 parameters correctly mapped into JPBC | All tests pass |
| Parameter Mapping | ParameterMappingTest | 5 | JPBC library configured with SM9 parameters | All tests pass |
| Robustness | SM9RobustnessTest | 5 | Functional test suite is complete | All tests execute; gaps documented |

### Overall Exit Criteria

- [x] All 42 JUnit tests pass
- [x] Mutation testing executed and results documented
- [x] Known limitations explicitly documented in test report
- [x] Traceability matrix maps every test to a specific risk point
