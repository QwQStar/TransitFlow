# TransitFlow

Personal portfolio project — **not** a production traffic platform.

Companion AI demo: [TransitFlow Ops Copilot](https://github.com/QwQStar/TransitFlow-OpsCopilot) — read-only Spring AI assistant over `/api/stats`.

A small end-to-end mobility ingest pipeline used to demonstrate the patterns behind GPS / weigh-station / flight feeds:

Kafka (partition by `deviceId`) → Spring Boot consumer group → Redis short-window dedup + GEO → MySQL upsert → scheduled reconciliation → DLQ.

```text
Simulator / POST /api/ingest
        │  key = deviceId
        ▼
   Kafka topic transitflow.ingest
        │
        ▼
 Spring Boot consumer group
        ├─ Redis SET NX + TTL     short-window dedup
        ├─ MySQL UNIQUE(source, event_id)   source of truth
        ├─ Redis GEO              last known position
        └─ poison / bad JSON  →  transitflow.ingest.dlq
        │
        ▼
  GET /api/stats   GET /api/nearby
```

## Why these choices

| Topic | Choice | Reason |
| --- | --- | --- |
| Partition key | `deviceId` | Keep a single device ordered; different devices consume in parallel |
| Delivery | at-least-once + idempotent sink | Kafka retries are normal; exactly-once is not assumed |
| Dedup | Redis `SET NX` (10 min) + MySQL unique key | Redis is a fast filter; the database remains the ledger |
| GEO | Redis GEO | Nearby lookup is an approximate *view*; MySQL holds the event rows |
| Failures | 2 retries then DLQ | One bad record must not stall the partition forever |
| Drift | `consumed - landed - duplicate - dlq` | Same idea as station-upload vs platform-count mismatch |

Demo events are generated around downtown Singapore coordinates so nearby queries have something to return.

## Run

Needs **Docker Desktop** (or any Compose v2 host). Java 17 is only required if you build outside Docker.

```bash
docker compose up --build
```

Wait until `transitflow-app-1` is healthy, then:

```bash
curl http://localhost:8080/actuator/health
curl http://localhost:8080/api/stats
curl "http://localhost:8080/api/nearby?lat=1.3521&lon=103.8198&km=8"
```

Publish one event yourself:

```bash
curl -X POST http://localhost:8080/api/ingest \
  -H "Content-Type: application/json" \
  -d "{\"source\":\"GPS\",\"deviceId\":\"gps-demo\",\"eventId\":\"evt-1\",\"eventTime\":\"2026-09-11T00:00:00Z\",\"lat\":1.3521,\"lon\":103.8198,\"payload\":\"manual\"}"
```

Force a DLQ record with `"payload":"POISON"`.

Compose credentials (`transitflow` / `transitflow`) are **demo-only**.

## Stack

- Java 17, Spring Boot 3.4
- Spring Kafka, Spring Data JPA, Spring Data Redis
- MySQL 8.4, Redis 7, Kafka 3.8 (KRaft, Bitnami)
- Actuator on `/actuator/health`

## Honest scope

This is a sandbox sized for interviews: one broker, one MySQL, no Kubernetes, no claim of production SLA. The interesting part is the ingest contract (ordering, duplicates, poison messages, reconciliation), not the cluster size.

---

# TransitFlow（中文）

个人作品，**不是**公司生产系统。

把交通场景里常见的「多源接入 → 消息削峰 → 幂等落库 → 对账」缩成一条能跑、能讲的链路。分区键用 `deviceId` 保证同设备有序；Redis 做短窗口去重和 GEO 最新位置；MySQL 唯一键才是账本；坏数据进 DLQ，避免卡死分区。

本地需要 Docker Desktop：`docker compose up --build`，然后访问 `http://localhost:8080/api/stats`。
