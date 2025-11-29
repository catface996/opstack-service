---
inclusion: manual
---

# Redis Best Practices

This document guides AI on how to correctly, efficiently, and securely use Redis cache.

## Quick Reference

| Rule | Requirement | Priority |
|------|-------------|----------|
| Expiration Time | MUST set expiration for all cache keys | P0 |
| Key Naming | MUST follow business-module:function:id format | P0 |
| Cache Update | MUST update database first, then delete cache | P0 |
| Large Keys | NEVER exceed 10KB for single key value | P0 |
| Distributed Lock | MUST set expiration time, NEVER permanent | P0 |

## Critical Rules (NON-NEGOTIABLE)

| Rule | Description | ✅ Correct | ❌ Wrong |
|------|-------------|------------|----------|
| **Mandatory Expiration** | ALL cached data MUST have expiration times | `setex(key, 3600, value)` | `set(key, value)` without expiration |
| **Key Naming Standard** | STRICTLY follow colon-separated format | `user:info:1001` | `user_1001` or random naming |
| **Cache Aside Pattern** | MUST update DB first, then delete cache | 1. Update DB 2. Delete cache | Update cache directly or update cache before DB |
| **No Large Keys** | Single value MUST NOT exceed 10KB | Split large data or use Hash | Store 5MB JSON in single key |
| **No Hot Keys** | NEVER let single key handle excessive requests | Use local cache or split keys | Single key with millions of QPS |
| **Lock Expiration** | Distributed locks MUST have timeout | `SET lock NX EX 30` | `SET lock NX` without expiration |

## Core Principles

### 1. Cache Design Principles

**You must comply with**:
- ✅ Cache should be optional (system should function normally when cache fails)
- ✅ Set reasonable expiration times (avoid memory overflow)
- ✅ Use appropriate data structures (improve performance)
- ❌ Don't use Redis as a database (data persistence should be in MySQL)

### 2. Performance Priority Principle

**You should consider**:
- Avoid large keys (single key value should not exceed 10KB)
- Avoid hot keys (keys with excessively high access frequency)
- Use batch operations (reduce network overhead)
- Proper use of Pipeline (batch execute commands)

### 3. Security and Reliability Principle

**You should ensure**:
- Prevent cache penetration (querying non-existent data)
- Prevent cache breakdown (hot data expiration)
- Prevent cache avalanche (large amount of cache expiring simultaneously)
- Ensure consistency between cache and database

## Key Naming Standards

### Naming rules you should follow

**Naming format**: `business-module:function:unique-identifier`

**Examples**:
- ✅ `user:info:1001` (user information)
- ✅ `order:detail:20240101001` (order details)
- ✅ `product:stock:SKU123` (product inventory)
- ✅ `session:token:abc123` (session token)

**Naming principles**:
- Use colon `:` to separate levels
- Use lowercase letters and underscores
- Self-explanatory, easy to manage
- Avoid excessively long key names

## Data Type Selection

### How you should choose data types

| Data Type | Use Cases | Examples |
|---------|---------|------|
| **String** | Simple key-value pairs, counters, distributed locks | User info, visit count |
| **Hash** | Object storage, independently updatable fields | User details, product info |
| **List** | Message queues, timelines, latest lists | Comment lists, operation logs |
| **Set** | Deduplication, intersection/union/difference operations | Tags, following lists |
| **ZSet** | Leaderboards, weighted sets | Score rankings, popularity sorting |

### Data Type Usage Recommendations

**String type**:
- Suitable for: simple values, JSON strings, counters
- Operations: SET, GET, INCR, DECR

**Hash type**:
- Suitable for: object storage (when fields are few)
- Operations: HSET, HGET, HMGET, HINCRBY
- Advantage: can update partial fields only

**List type**:
- Suitable for: message queues, timelines
- Operations: LPUSH, RPUSH, LPOP, RPOP, LRANGE
- Note: avoid excessively long lists (recommended not to exceed 1000 elements)

**Set type**:
- Suitable for: deduplication, set operations
- Operations: SADD, SREM, SISMEMBER, SINTER, SUNION

**ZSet type**:
- Suitable for: leaderboards, priority queues
- Operations: ZADD, ZRANGE, ZREVRANGE, ZINCRBY

## Expiration Time Settings

### Expiration time rules you should follow

**Must set expiration time**:
- ✅ All cached data should have expiration times
- ❌ Don't use permanent cache (unless special requirements)

**Expiration time recommendations**:
- Hot data: 5-30 minutes
- Regular data: 1-24 hours
- Cold data: 1-7 days
- Session data: 30 minutes-2 hours

**Avoid cache avalanche**:
- Add random value to expiration time (e.g., ±5 minutes)
- Avoid large amount of cache expiring simultaneously

**Example**:
```java
// Base expiration time 30 minutes, plus 0-5 minutes random value
long expireTime = 30 * 60 + random.nextInt(5 * 60);
redisTemplate.expire(key, expireTime, TimeUnit.SECONDS);
```

## Cache Update Strategies

### Update strategies you should follow

**Cache Aside Pattern**:
- Read: read cache first, if cache doesn't exist read database, then write to cache
- Update: update database first, then delete cache (rather than update cache)

**Why delete rather than update cache**:
- Avoid data inconsistency due to concurrent updates
- Reduce unnecessary cache writes (if data is no longer accessed)

**Update process**:
```
1. Update database
2. Delete cache
3. Reload cache on next read
```

## Cache Problem Prevention

### 1. Prevent Cache Penetration

**Problem**: Querying non-existent data causes database queries every time

**Solutions you should use**:

**Solution 1: Cache null values**
- Cache null value when query result is empty
- Set shorter expiration time (e.g., 5 minutes)

**Solution 2: Bloom filter**
- Add bloom filter before cache
- Quickly determine if data exists

### 2. Prevent Cache Breakdown

**Problem**: Hot data expires, large number of requests simultaneously access database

**Solutions you should use**:

**Solution 1: Mutex lock**
- Use distributed lock, only allow one request to query database
- Other requests wait for cache rebuild completion

**Solution 2: Hot data never expires**
- Logical expiration: store expiration time in cache
- Asynchronously update cache in background

### 3. Prevent Cache Avalanche

**Problem**: Large amount of cache expires simultaneously, causing surge in database pressure

**Solutions you should use**:

**Solution 1: Add random value to expiration time**
- Avoid cache expiring simultaneously

**Solution 2: Cache preheating**
- Preload hot data when system starts

**Solution 3: Multi-level caching**
- Local cache + Redis cache
- Reduce Redis pressure

## Distributed Lock Usage

### Distributed lock rules you should follow

**Basic requirements**:
- Mutual exclusion: only one client can hold lock at same time
- Safety: only client holding lock can release it
- Fault tolerance: lock must have expiration time to avoid deadlock

**Implementation methods**:

**Solution 1: SET NX EX**
- Use SET key value NX EX seconds
- NX: only set when key doesn't exist
- EX: set expiration time

**Solution 2: Redisson**
- Use Redisson framework
- Auto-renewal, reentrant locks

**Notes**:
- Lock expiration time should be longer than business execution time
- Verify lock ownership when releasing lock
- Avoid holding lock for long time

## Batch Operation Optimization

### Batch operation methods you should use

**Pipeline**:
- Batch send commands, reduce network round trips
- Suitable for: batch reads, batch writes

**MGET/MSET**:
- Batch get/set multiple keys
- Better performance than multiple GET/SET

**HMGET/HMSET**:
- Batch get/set multiple fields of Hash

**Notes**:
- Single batch operation should not be too many (recommended not to exceed 100)
- Large batch operations should be done in batches

## Performance Optimization Standards

### Performance optimization principles you should follow

**1. Avoid large keys**
- Single String type value not exceeding 10KB
- Hash, List, Set, ZSet element count not exceeding 5000

**2. Avoid hot keys**
- Identify keys with excessively high access frequency
- Use local cache to reduce Redis pressure
- Consider splitting hot keys

**3. Use appropriate data structures**
- Choose most suitable data type based on business scenario
- Avoid using complex data structures to store simple data

**4. Proper use of persistence**
- RDB: suitable for backup, full replication
- AOF: suitable for scenarios with high data safety requirements
- Choose persistence strategy based on business needs

**5. Monitoring and alerting**
- Monitor memory usage
- Monitor slow queries
- Monitor connection count
- Set reasonable alert thresholds

## Common Use Cases

### Typical scenarios you should know

**1. Cache hot data**
- User info, product details, configuration info
- Reduce database query pressure

**2. Counters**
- Traffic statistics, likes count, inventory quantity
- Use INCR/DECR atomic operations

**3. Distributed locks**
- Prevent duplicate submissions, rate limiting, scheduled tasks
- Ensure operation mutual exclusion

**4. Message queues**
- Use List for simple queues
- Use Stream for message queues (Redis 5.0+)

**5. Leaderboards**
- Use ZSet for score rankings, popularity sorting
- Support range queries and score updates

**6. Session management**
- Store user session information
- Support distributed session sharing

**7. Rate limiting**
- Use counter for simple rate limiting
- Use sliding window for precise rate limiting

## Spring Boot Integration Standards

### Integration rules you should follow

**1. Use RedisTemplate**
- Configure serialization method (recommended JSON)
- Set reasonable connection pool parameters

**2. Use @Cacheable annotation**
- Simplify cache operations
- Note cache key generation rules

**3. Configure connection pool**
- Maximum connections: set based on concurrency
- Maximum idle connections: avoid frequent connection creation
- Connection timeout: avoid long waits

**4. Exception handling**
- Redis exceptions should not affect main business
- Degradation strategy: directly query database when Redis unavailable

## Common Errors and Corrections

### Errors you should avoid

| Error Type | Wrong Approach | Correct Approach | Reason |
|---------|---------|---------|------|
| **No expiration time** | Permanent cache | Set reasonable expiration time | Memory overflow |
| **Large keys** | Single value exceeds 1MB | Split or use Hash | Performance issue |
| **Hot keys** | Single key accessed too frequently | Use local cache or split | High Redis pressure |
| **Cache penetration** | Don't cache non-existent data | Cache null values or use bloom filter | High database pressure |
| **Cache avalanche** | Large amount of cache expires simultaneously | Add random value to expiration time | Database pressure surge |
| **Update cache** | Update cache before database | Update database then delete cache | Data inconsistency |
| **No batch operations** | Execute single commands in loop | Use Pipeline or MGET | Poor performance |
| **Distributed lock no expiration** | Lock never expires | Must set expiration time | Deadlock |

## Your Checklist

When using Redis, you should check:

### Design Check
- [ ] All caches have expiration times set
- [ ] Key naming follows standards (business-module:function:unique-identifier)
- [ ] Appropriate data type chosen
- [ ] Considered cache penetration, breakdown, avalanche prevention

### Performance Check
- [ ] Avoided large keys (single value < 10KB)
- [ ] Avoided hot keys (consider local cache)
- [ ] Used batch operations (Pipeline, MGET)
- [ ] Collection type element count reasonable (< 5000)

### Security Check
- [ ] Cache update strategy correct (update database first, then delete cache)
- [ ] Distributed lock has expiration time set
- [ ] Verified lock holder when releasing lock
- [ ] Redis exceptions have degradation handling

### Maintainability Check
- [ ] Key naming clear, easy to manage
- [ ] Expiration time set reasonably
- [ ] Have monitoring and alerting mechanism
- [ ] Have cache preheating and degradation plans

## Key Principles Summary

### Design Principles
1. **Cache is optional**: System functions normally when cache fails
2. **Must set expiration time**: avoid memory overflow
3. **Choose appropriate data type**: improve performance and maintainability
4. **Prevent three major problems**: penetration, breakdown, avalanche

### Performance Principles
1. **Avoid large keys**: single value not exceeding 10KB
2. **Avoid hot keys**: use local cache or split
3. **Use batch operations**: reduce network overhead
4. **Proper use of persistence**: balance performance and data safety

### Security Principles
1. **Update database first, then delete cache**: ensure data consistency
2. **Distributed lock must have expiration time**: avoid deadlock
3. **Redis exceptions should degrade**: don't affect main business
4. **Monitoring and alerting**: discover problems promptly

## Key Benefits

Following these standards can achieve:

- ✅ Improve system performance, reduce database pressure
- ✅ Ensure consistency between cache and database
- ✅ Prevent cache penetration, breakdown, avalanche problems
- ✅ Avoid memory overflow and performance issues
- ✅ Improve system availability and stability
- ✅ Facilitate cache management and maintenance
