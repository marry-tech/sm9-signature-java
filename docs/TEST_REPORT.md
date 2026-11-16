# SM9 Identity-Based Signature — Test Report

## Test Environment

- **Java:** OpenJDK 17
- **Build:** Maven 3.9
- **Testing framework:** JUnit 4.13.2
- **Cryptographic library:** JPBC 2.0.0 (Java Pairing-Based Cryptography)
- **Mutation testing:** PITest 1.15.3

## Test Traceability Matrix

Each test case is traceable to a specific risk point to avoid ad-hoc test creation.

| Test ID | Objective | Risk if Violated | Category | Result |
|---------|-----------|------------------|----------|--------|
| F-01 | Normal sign & verify round-trip with standard identity | Valid signatures fail or invalid signatures accepted | Functional | Pass |
| F-02 | Deterministic output across multiple runs | Non-deterministic behaviour breaks consistency | Functional | Pass |
| F-03 | Different identifiers produce different signatures | Key collision or predictable private keys | Functional | Pass |
| B-01 | Null identity input rejected | Null pointer crash or silent failure | Boundary | Pass |
| B-02 | Empty identity string rejected | Accept empty identity, produce invalid key | Boundary | Pass |
| B-03 | Minimum-length message handling | Edge case not covered | Boundary | Pass |
| B-04 | Maximum-length message handling | Buffer overflow or performance degradation | Boundary | Pass |
| B-05 | Unicode identity string handling | Encoding issues in international identifiers | Boundary | Pass |
| E-01 | Tampered signature bytes rejected | Cryptographic forgery accepted | Exception | Pass |
| E-02 | Wrong master public key rejected | Verification passes with wrong key | Exception | Pass |
| E-03 | Mismatched user identity rejected | Identity confusion attack | Exception | Pass |
| E-04 | Altered message (replay) rejected | Replay attack accepted | Exception | Pass |
| E-05 | Reused signature for different message rejected | Signature collision | Exception | Pass |
| C-01 | Private key group membership (G1) verified | Key generated outside valid group | Conformance | Pass |
| C-02 | Pairing equation consistency verified | Mathematical structure violated | Conformance | Pass |
| C-03 | Standard test vector validation | Non-compliant implementation | Conformance | Pass |
| P-01 | JPBC default vs SM9 parameter comparison | Wrong curve configuration used | Parameter Mapping | Pass |
| P-02 | Field order verification | Cryptographic strength compromised | Parameter Mapping | Pass |
| R-01 | Control-character identity handling assessed | Silent acceptance of malformed data | Robustness | Documented |
| R-02 | Mixed UTF-8/ASCII message handling | Encoding mismatch | Robustness | Pass |
| R-03 | Arbitrary byte sequence handling assessed | No message content validation | Robustness | Documented |
| R-04 | Single-byte message handling | Edge case not covered | Robustness | Pass |
| R-05 | Whitespace-only identity handling assessed | No distinction between empty and whitespace | Robustness | Documented |

(Complete list available in test class source comments.)

## Test Case Classification

### Functional Tests (6 tests)
- **Class:** `SM9FunctionalTest.java`
- **Focus:** Core sign/verify round-trip under normal operational conditions
- **Rationale:** establish that the implementation produces correct output for valid inputs before testing failure modes

### Boundary Tests (13 tests)
- **Class:** `SM9BoundaryTest.java`
- **Focus:** Null, empty, minimum, maximum, unicode, and format-variant identities and messages
- **Rationale:** identify where valid domains end and invalid domains begin; a system that accepts boundary inputs silently is not robust

### Exception Tests (7 tests)
- **Class:** `SM9ExceptionTest.java`
- **Focus:** Security violations: tampered signatures, wrong public keys, identity mismatches, replay attacks
- **Rationale:** cryptographic systems must reject invalid input, not merely accept valid input

### Conformance Tests (6 tests)
- **Class:** `SM9ConformanceTest.java`
- **Focus:** Mathematical compliance with GM/T 0044.2: G1 group membership, pairing equation consistency, standard test vectors
- **Rationale:** functional correctness and standard compliance are not equivalent; an implementation may pass functional tests while deviating from the standard

### Parameter Mapping Tests (5 tests)
- **Class:** `ParameterMappingTest.java`
- **Focus:** Correct mapping of SM9 standard parameters into JPBC's Type-A curve format; detection of default-parameter substitution
- **Rationale:** JPBC's default Type-A parameters differ from SM9 standard parameters; using defaults silently invalidates all operations

### Robustness Tests (5 tests)
- **Class:** `SM9RobustnessTest.java`
- **Focus:** Malformed encoding, mixed charsets, control characters, arbitrary byte sequences
- **Rationale:** defensive systems must reject malformed input explicitly, not merely tolerate it

## Robustness Testing

Five additional tests assess behaviour under malformed or unexpected input encoding.

| Test ID | Input | Ideal Behaviour | Actual Behaviour | Gap Identified |
|---------|-------|----------------|------------------|---------------|
| R-01 | Identity with control characters (`\u0000\u0001\u0002\u0003`) | Reject with exception | Accepted and processed | No validation for non-printable characters |
| R-02 | Mixed UTF-8/ASCII message | Normal sign/verify | Pass | — |
| R-03 | Arbitrary byte sequence (`0xFF, 0xFE, 0xFD`) | Reject or handle safely | Accepted as opaque data | No message content validation |
| R-04 | Single-byte message | Normal sign/verify | Pass | — |
| R-05 | Whitespace-only identity (`"   "`) | Reject with exception | Accepted and processed | No distinction between empty and whitespace |

**Finding:** The original implementation (based on CSDN reference materials) does not perform deep input sanitisation beyond null and empty-string checks. R-01, R-03 and R-05 reveal that malformed or edge-case inputs are silently accepted rather than rejected. This is a defensive-validation gap: the code does not crash, but it also does not enforce strict input hygiene. Systematic robustness engineering requires explicit rejection policies for each input domain, not just functional-path coverage.

## Overall Results

| Metric | Value |
|--------|-------|
| Total Test Classes | 6 |
| Total Test Methods | 42 |
| Pass Rate | 100% (functional/boundary/exception/conformance/mapping) |
| Robustness Gaps Documented | 3 (R-01, R-03, R-05) |

**Note:** A 100% pass rate on functional tests does not imply the implementation is correct. See `MUTATION_TESTING.md` for evidence that passing tests cannot detect protocol-level deviations.

## Known Limitations

- No formal specification of SM9 behaviour (only the GM/T 0044.2 standard text)
- No model checking or theorem-proving of pairing computation correctness
- No timing or side-channel analysis
- No comparison with independently implemented SM9 verifiers
