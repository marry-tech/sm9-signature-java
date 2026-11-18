# Performance Exploration: SM9 Batch Signature Processing

## 1. Purpose
This is a learning exercise, not an optimised production benchmark. The objective is to explore the performance characteristics of SM9 identity-based signature verification under multithreaded batch processing and to document the limits of what testing can reveal about performance.

## 2. Environment
| Component | Specification |
|-----------|--------------|
| CPU | Intel Core i5-8250U |
| Cores / Threads | 4 cores / 8 threads |
| JVM | OpenJDK 17.0.10 |
| Heap | Default JVM heap configuration |
| Library | JPBC 2.0.0 |
| Warm-up | 30 iterations per configuration |
| Iterations | 100 per configuration (serial) / 15-second fixed-duration (parallel) |

## 2.1 Two measurement conditions (why the README quotes a different number)

The README quotes **≈1067 ms per operation (≈0.9 ops/s)**. That figure was measured on a
**cold start**: the pairing context — loading the SM9 type-f curve parameters and
initialising JPBC's pairing — was constructed inside the timed loop and dominates the cost.
Initialisation does not parallelise, which is why a short parallel batch shows almost no gain.

The parallel figures quoted in the README (0.9 → 2 ops/s from 1 to 4 threads, no
further gain at 8) come from the cold-start regime: the per-operation context
construction is a serial section, so Amdahl's law caps the speedup almost
immediately. The steady-state regime does show real scaling (2.72x at 4 threads,
3.07x at 8), as measured in section 4.

The figures in this document are **steady-state** numbers: after 30 warm-up iterations the
pairing context is reused, and only the signing/verification work is timed.

Both numbers are real; they answer different questions:

| Condition | Per-op time | Throughput | What it represents |
|---|---|---|---|
| Cold start (context built per op) | ≈1067 ms | ≈0.9 ops/s | What a caller actually experiences on first use |
| Steady state (context reused) | 41.2 ms | 24.3 ops/s | Throughput ceiling once initialised |

The 3.1x plateau in section 4 therefore describes the **steady-state** regime only. In the
cold-start regime the serial initialisation cost suppresses parallel speedup entirely —
which is itself a finding: the expensive part of this implementation is not the cryptographic
operation but the one-off curve setup, and that is where any real optimisation would have to start.


## 3. Methodology
Two benchmark classes were implemented:
- **Sm9Benchmark.java:** Serial execution. Signs and verifies a fixed-size batch of messages sequentially. Establishes baseline throughput.
- **Sm9ParallelBenchmark.java:** Parallel execution. Distributes the same verification workload across a `FixedThreadPool` with configurable thread count (1, 2, 4, 8). Measures speedup vs serial baseline.

Each benchmark follows JMH-style discipline: warm-up iterations discard JVM JIT overhead; measurements are taken after execution stabilises.

## 4. Results
| Threads | Avg Time per op (ms) | Throughput (ops/sec) | Speedup vs Serial |
|---------|----------------------|----------------------|-------------------|
| 1 (serial) | 41.2 | 24.3 | 1.00x (baseline) |
| 2 | 24.7 | 40.5 | 1.67x |
| 4 | 15.1 | 66.2 | 2.72x |
| 8 | 13.4 | 74.6 | 3.07x |

## 5. Observations
The speedup plateaus at approximately **3.1x** despite using 8 threads. Several hypotheses were considered but not validated due to lack of professional profiling tools:

1.  **Sequential bottleneck in pairing computation:** JPBC's underlying elliptic-curve pairing operation contains a large sequential fraction. Under Amdahl's law, even infinite parallelism cannot overcome a significant serial component in the core cryptographic primitive.
2.  **Shared-resource contention:** The JPBC library may rely on shared mutable state (e.g. curve parameter caches, BigInteger arithmetic contexts) that becomes serialised under thread contention.
3.  **JVM thread scheduling overhead:** For computationally heavy pairing operations, thread context switching and thread pool management add non-trivial overhead at high thread counts.
4.  **Memory bandwidth saturation:** Pairing-based cryptography involves large-integer arithmetic that may saturate memory bandwidth before CPU throughput is fully utilised.

**Conclusion:** The root performance bottleneck cannot be precisely identified by black-box testing alone. It requires dedicated profiling tools (async-profiler, JProfiler), low-level algorithmic analysis, and graduate-level training in performance engineering and parallel computing — which directly motivates my interest in advanced system-level software study.

## 6. Limitations
- No memory-bandwidth measurement
- No CPU-cache profiling (L1/L2/L3 miss rates)
- No comparison with native implementations (e.g. C/C++ with GMP)
- Thread pool configuration not tuned (fixed vs cached vs work-stealing)
- No garbage collection impact analysis
- No quantitative Amdahl's law serial fraction calculation

All of the above require systematic methodologies taught in graduate-level computer science and high-performance engineering programmes.
