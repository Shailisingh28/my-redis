# Benchmark Results — Day 14

Compared MyRedis against real Redis (Docker) using `redis-benchmark`, 10,000 requests.

| Metric          | Real Redis | MyRedis  |
| --------------- | ---------- | -------- |
| SET req/sec     | 71,428     | 67,567   |
| GET req/sec     | 70,921     | 91,743   |
| SET p50 latency | 0.599 ms   | 0.415 ms |
| GET p50 latency | 0.647 ms   | 0.215 ms |

## Important caveat

This was NOT a fully fair comparison. Real Redis was running inside a Docker
container (port 6380), while MyRedis ran natively on the host machine (port
6379). Docker's network layer adds overhead that isn't related to Redis's
own performance — so MyRedis's numbers here benefit from having no
containerization overhead.

A fair, same-environment comparison (both native, or both Dockerized) is
planned once MyRedis itself is containerized (Day 19).

## Why the numbers are this close despite that

- Real Redis handles far more responsibility per request (persistence checks,
  memory accounting, replication hooks, ACLs) that MyRedis does not implement.
- The test workload (simple SET/GET on small string values) is small enough
  that architectural differences (Redis's single-threaded event loop vs
  MyRedis's thread-per-connection model) don't show a large gap yet.
- At much higher concurrency, I'd expect MyRedis's thread-per-connection
  model to start showing overhead (thread creation/context-switching cost)
  that Redis's event loop avoids.
