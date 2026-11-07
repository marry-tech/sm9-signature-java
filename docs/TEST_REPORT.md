# SM9 Implementation Test Report

## Test Objective
Verify the functional correctness of the SM9 Java implementation, and evaluate the coverage and limitations of conventional testing methods for cryptographic software reliability.

## Test Environment
- JDK version: Java 8
- Dependencies: JPBC library 2.0.0
- Reference standard: GM/T 0044 SM9 national cryptographic specification

## Test Case Classification

### 1. Normal Functional Tests (6 tests)
Supplement after graduation (2026), to verify correct output under standard valid inputs:
- Valid user identity key generation
- Normal message signature and verification
- Signature consistency across multiple executions
- Different identifiers producing different signatures

Result: All standard test cases pass, output is consistent with expected results.

### 2. Boundary Condition Tests (13 tests)
Supplement after graduation (2026), to examine system stability at input boundaries:
- Empty string and null as user identity
- Minimum-length and maximum-length message input
- Identity string with special characters and unicode
- Edge values of valid parameter range

Result: Boundary cases trigger expected error states; explicit input validation prevents silent failures. A few edge scenarios rely on underlying library exception handling.

### 3. Exception Path Tests (7 tests)
Supplement after graduation (2026), to examine fault handling capability under attack scenarios:
- Tampered signature components (modified h or S)
- Verification with wrong master public key
- Verification with mismatched user identity
- Verification with altered message (replay scenario)
- Reused signature for different message
- Single-byte message edge case

Result: Exception paths trigger appropriate error responses, but lack systematic failure classification and formal verification.

### 4. Conformance Tests (6 tests)
Supplement after graduation (2026), to verify algebraic structure compliance with GM/T 0044:
- Group membership verification for private keys (G1, G2, Zr)
- Pairing equation consistency checks
- Standard test vector validation
- Signature component structure verification

Result: Conformance checks pass when SM9-specific parameters are correctly configured.

### 5. Parameter Mapping Tests (5 tests)
Supplement after graduation (2026), to verify explicit parameter configuration:
- JPBC default Type-A parameters vs SM9 standard definition
- Field order, subgroup order and base point comparison
- Configuration validation for correct curve setup
- Test vector compatibility with mapped parameters

Result: SM9 parameters are explicitly mapped and differ from JPBC defaults. Standard test vectors pass only with correct parameter configuration.

## Test Coverage Summary
- Functional correctness: fully covered (6 tests)
- Boundary scenarios: covered (13 tests)
- Exception and failure modes: covered (7 tests)
- Conformance and protocol consistency: covered (6 tests)
- Parameter mapping verification: covered (5 tests)
- Formal verification: cannot be achieved by black-box testing alone

## Limitation Reflection
Simple input-output testing can verify that an implementation produces correct output for given input, but it cannot guarantee:
- Mathematical correctness of the underlying protocol
- Absence of side-channel vulnerabilities
- Robustness under all possible attack scenarios
- Compliance with formal security proofs

For high-security software such as cryptographic components, systematic reliability verification requires formal methods, fault modeling, protocol consistency analysis and structured quality assurance methodology. This is the area I plan to focus on in future work.
