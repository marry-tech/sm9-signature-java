# SM9 Signature Test Report

## Overview
This test suite was designed to verify that the SM9 signature implementation behaves predictably across both normal and failure conditions. It was built in two stages:
- Basic boundary checks added during the original thesis work (2022)
- Full systematic expansion into 37 cases during the November 2026 revision

## Test Design Approach

The design follows the basic testing principles I learned in my undergraduate Software Engineering course:
1.  **Happy path first**: verify that the normal sign-verify flow works correctly
2.  **Equivalence partitioning & boundary value analysis**: test the edges of input domains, where bugs most often appear
3.  **Negative testing**: confirm that invalid inputs and tampered data are correctly rejected
4.  **Configuration verification**: confirm the underlying curve uses SM9 standard parameters, not the library defaults

### Test breakdown
| Test class | Count | Focus |
|---|---|---|
| Functional tests | 6 | Normal key generation, signing, verification with valid inputs |
| Boundary tests | 13 | Null values, empty strings, oversized messages, Unicode user IDs |
| Exception tests | 7 | Tampered signatures, wrong public keys, altered messages, signature reuse |
| Conformance tests | 6 | Group membership checks for G1 / G2 / Zr, standard identifier validation |
| Parameter mapping tests | 5 | Curve parameter validation, comparison against JPBC Type-A defaults |
| **Total** | **37** | |

## Results
All 37 tests pass on a clean build.
- Valid signatures are always accepted
- Invalid inputs throw explicit, predictable exceptions
- Tampered signatures and wrong identities are always rejected
- The curve parameters match the SM9 standard definition

## Observations & Limitations

### What this testing does prove
- The main flow works end-to-end
- Common user mistakes (null inputs, wrong IDs, tampered messages) fail clearly instead of silently
- The library is configured with the correct standard parameters, not the JPBC defaults

### What this testing does NOT prove
This is the most important part. Black-box functional testing alone is not enough to guarantee cryptographic correctness:
1.  It cannot catch subtle arithmetic errors that still produce internally consistent outputs. As long as sign and verify use the same flawed math, they will still match.
2.  It says nothing about side-channel resistance, timing leaks or memory safety issues.
3.  There is no systematic failure model. I am checking individual failure points one by one, not reasoning about the full failure space.

When I started this revision I thought more test cases would equal more reliability. After finishing I realized that without a proper methodology for verification, testing can only give you confidence in the cases you already thought of — not the ones you didn't.

This is exactly the gap I hope to fill through graduate study: moving from ad-hoc, case-by-case testing to systematic, principled approaches for software verification and system dependability.
