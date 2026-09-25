# MyRedis — A Redis Clone Built From Scratch in Java

A from-scratch reimplementation of Redis's core functionality — TCP server,
RESP protocol, a custom hash table, concurrency control, LRU eviction, and
persistence — built without any framework, to genuinely understand how a
key-value store works under the hood rather than just using one.

## Why this project

Everyone uses Redis. Very few people have actually built the pieces that
make it work. This project implements those pieces one at a time: the wire
protocol, the data structure, the concurrency model, the eviction policy,
and the persistence layer — each with real bugs hit and fixed along the way,
not just a working demo copied from a tutorial.

## Architecture

```
        client (redis-cli) ──TCP──▶ ServerSocket (port 6379)
                                          │
                              accept() ──▶ new Thread per connection
                                          │
                              BufferedReader ──▶ RespParser (decodes RESP)
                                          │
                                   processCommand()
                                          │
                              ┌───────────┼───────────┐
                              ▼           ▼           ▼
                            SET         GET          DEL
                              │           │           │
                              └───────────┼───────────┘
                                          ▼
                                    Storage
                                          │
                                     LRUCache
                              (HashMap + Doubly Linked List)
                                          │
                              ┌───────────┴───────────┐
                              ▼                         ▼
                     evict on capacity           saveToDisk() / loadFromDisk()
                        (LRU policy)                (snapshot persistence)
```

## What's implemented

| Feature               | Details                                                                                                                                                                                                                                                                                |
| --------------------- | -------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------- |
| **RESP protocol**     | Custom parser and writer for Redis's wire protocol — Simple Strings, Bulk Strings, Arrays, Errors, Null Bulk Strings.                                                                                                                                                                  |
| **Custom hash table** | Built without `java.util.HashMap` — separate chaining for collisions, dynamic resizing at 0.75 load factor, verified via unit tests.                                                                                                                                                   |
| **Concurrency**       | Thread-per-connection model. Started with a single `synchronized` lock (correct but coarse-grained), upgraded to per-bucket locking with `ReentrantLock` for parallel access across different buckets, with double-checked locking to prevent redundant resizes under concurrent load. |
| **LRU eviction**      | HashMap + Doubly Linked List combo for O(1) get/put/evict. Verified that both reads and writes correctly mark a key as recently used.                                                                                                                                                  |
| **Persistence**       | Snapshot-based — full dataset written to a flat file on every write, reloaded on startup. Verified full crash/restart cycles preserve data.                                                                                                                                            |
| **Logging**           | SLF4J + Logback — structured logs with timestamps, thread names, and levels (INFO/WARN/ERROR), written to both console and file.                                                                                                                                                       |
| **Testing**           | Unit tests for the hash table, LRU cache, and storage layer; a dedicated concurrency test using 100 parallel threads to catch race conditions; a full integration test that starts a real server and talks to it over an actual socket.                                                |
| **Containerization**  | Multi-stage Docker build producing a self-contained fat JAR (via `maven-shade-plugin`, since this is a plain Java app with no framework to bundle dependencies automatically).                                                                                                         |

## Concurrency: the two real bugs I hit (worth knowing cold for interviews)

**Bug 1 — lost updates under concurrent inserts.** Before adding any locking,
100 threads inserting concurrently sometimes produced a hash table with fewer
than 100 entries — two threads would read the same bucket's head pointer
before either wrote back, and one insert would silently overwrite the other.
No crash, no exception — just quietly wrong data. Fixed initially with a
single `synchronized` lock on `put`/`get`/`remove`.

**Bug 2 — redundant resizes under concurrent load.** After switching to
per-bucket locking (so unrelated buckets don't block each other), inserting
100 keys concurrently triggered **18 resizes** instead of the expected 2-3.
Multiple threads were each checking the load factor _after_ releasing their
bucket lock, and several of them would independently decide a resize was
needed before any of them had actually performed one. Fixed with
double-checked locking: the resize check is repeated _inside_ the
synchronized `resize()` method itself, so a thread that arrives after
another thread already resized simply returns without doing anything.

## Honest limitations

- **Persistence rewrites the entire dataset on every write.** This is fine
  for a learning project but would not scale — real Redis either takes
  periodic snapshots (RDB) or appends only the new command to a log (AOF).
- **Per-bucket locks are not perfectly optimal under heavy concurrent
  resizing** — a small number of redundant resizes can still occur in rare
  timing windows, though data correctness is unaffected (verified by
  `ConcurrencyTest`).
- **Not a full RESP implementation.** Core commands (SET, GET, DEL, PING)
  are properly implemented; `redis-cli`'s internal handshake commands
  (COMMAND, HELLO) are given simplified dummy responses rather than full
  spec-compliant ones, since replicating the entire protocol was out of
  scope for this project.

## Benchmark results

Compared against real Redis using `redis-benchmark`, both running in Docker
containers for a fair, same-environment comparison.

| Metric          | Real Redis | MyRedis  |
| --------------- | ---------- | -------- |
| SET req/sec     | 68,027     | 12,135   |
| GET req/sec     | 72,992     | 45,248   |
| SET p50 latency | 0.575 ms   | 4.407 ms |

### Why SET is so much slower than GET in MyRedis specifically

This gap is larger than the general Redis-vs-MyRedis architecture gap — it's
caused by a specific, known design choice: every `SET` triggers
`saveToDisk()`, which rewrites the **entire dataset** to a flat file on every
single write (see Persistence, above). `GET` never touches disk, so it's
close to memory-lookup speed. In a production system, this would be fixed by
either taking periodic snapshots or appending only the new command to a log
(AOF-style) instead of rewriting everything on every write.

### Why the remaining GET-vs-GET gap exists

Real Redis is written in C with no JVM overhead, uses a single-threaded
event loop (epoll) rather than thread-per-connection, and its RESP parser
operates on raw bytes rather than line-by-line String parsing. It also
handles far more responsibility per request (persistence bookkeeping, memory
accounting, replication hooks) that this project does not implement.

## Running locally

```bash
docker compose up --build
```

Test it:

```bash
redis-cli -p 6379
PING
SET name Shaili
GET name
```

## Running tests

```bash
mvn test
```

Covers: hash table correctness (including resize and collision handling),
LRU eviction order, storage integration, concurrent access safety (100
threads), and a full end-to-end socket-level integration test.

## Tech stack

Java 21, raw `java.net.ServerSocket`/`Socket` (no framework), SLF4J +
Logback, JUnit 5, Docker, Maven Shade Plugin.
