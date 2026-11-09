# Lessons Learned From This Project

These are the three biggest takeaways I got from working on this project, first as an undergraduate thesis and again when I revised it in 2026.

## 1. Silent failures are the worst kind of bug
When I first got the code running, I only tested with normal inputs. It worked fine, so I thought it was solid. Later I tried passing an empty message and it crashed deep inside the library with a cryptic error, not a clear message.

That taught me: if a function fails quietly or with an incomprehensible stack trace, it is not reliable — even if the happy path is correct. That is why I added explicit argument checks at every public method entry point. Clear, predictable failures are better than mysterious crashes.

## 2. Configuration errors are easy to miss and hard to debug
At the very beginning I used the default JPBC Type-A curve by accident. The code still ran, and sign and verify still matched each other — but the results had nothing to do with the SM9 standard.

This is a special case of the "silent failure" problem: the system looks like it works, but it is producing wrong results. It made me realize that parameter and configuration verification deserves its own set of tests, separate from functional tests.

## 3. More test cases ≠ guaranteed reliability
When I started the 2026 revision, my plan was simple: write as many test cases as I could. By the time I hit 37 I stopped and asked myself: what am I actually proving here?

The answer was: only the cases I already thought of. Black-box testing can show that known scenarios work, but it cannot tell you about the unknown unknowns. To really verify a system, you need methodology, not just volume.

That realization is what made me decide to apply for a master's degree focused on software verification and dependable systems.
