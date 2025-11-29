---
inclusion: manual
---

# MySQL Best Practices

This document guides AI on how to write high-quality, high-performance, and secure MySQL SQL statements.

## Quick Reference

| Rule | Requirement | Priority |
|------|-------------|----------|
| Parameterized Queries | MUST use #{}, NEVER ${} or string concatenation | P0 |
| UPDATE/DELETE WHERE | MUST have WHERE clause | P0 |
| SELECT Explicit Columns | MUST specify columns, NEVER SELECT * | P0 |
| Index Usage | MUST avoid function operations on indexed columns | P0 |
| Batch Operations | MUST use batch for multiple inserts/updates | P1 |

## Critical Rules (NON-NEGOTIABLE)

| Rule | Description | ✅ Correct | ❌ Wrong |
|------|-------------|------------|----------|
| **SQL Injection Prevention** | STRICTLY use parameterized queries | `WHERE id = #{id}` | `WHERE id = ${id}` or string concatenation |
| **UPDATE Must Have WHERE** | UPDATE without WHERE is FORBIDDEN | `UPDATE user SET name = ? WHERE id = ?` | `UPDATE user SET name = ?` |
| **DELETE Must Have WHERE** | DELETE without WHERE is FORBIDDEN | `DELETE FROM user WHERE id = ?` | `DELETE FROM user` |
| **No SELECT Star** | NEVER use SELECT * in production code | `SELECT id, name, email FROM user` | `SELECT * FROM user` |
| **Index Invalidation** | NEVER use functions on indexed columns in WHERE | `WHERE create_time >= '2024-01-01'` | `WHERE DATE(create_time) = '2024-01-01'` |
| **N+1 Query Prevention** | STRICTLY use JOIN or batch queries | `JOIN` or `WHERE id IN (...)` | Query database in loop |

## Core Principles

### 1. Security First Principle

**You must comply with**:
- ❌ Never use string concatenation to build SQL (vulnerable to SQL injection attacks)
- ✅ Always use parameterized queries (PreparedStatement or MyBatis #{} syntax)
- ✅ Validate and filter user input

### 2. Performance Priority Principle

**You should consider**:
- Proper use of indexes
- Avoid full table scans
- Reduce data transfer volume
- Optimize query logic

### 3. Maintainability Principle

**You should ensure**:
- SQL statements are clear and readable
- Use meaningful aliases
- Add appropriate comments
- Follow naming conventions

## SQL Writing Standards

### Query Statements (SELECT)

**Rules you should follow**:

1. **Explicitly specify column names**
   - ✅ `SELECT id, name, email FROM user`
   - ❌ `SELECT * FROM user` (unless all columns are truly needed)

2. **Use table aliases**
   - ✅ `SELECT u.id, u.name FROM user u`
   - Must use aliases in multi-table queries

3. **Proper use of WHERE conditions**
   - Place conditions with strong filtering first
   - Avoid function operations on columns in WHERE clause
   - ✅ `WHERE create_time >= '2024-01-01'`
   - ❌ `WHERE DATE(create_time) >= '2024-01-01'` (will cause index invalidation)

4. **Use LIMIT to restrict result sets**
   - Pagination queries must use LIMIT
   - ✅ `SELECT * FROM user LIMIT 10 OFFSET 20`

5. **Avoid subqueries (prefer JOIN)**
   - ✅ `SELECT u.* FROM user u JOIN order o ON u.id = o.user_id`
   - ⚠️ `SELECT * FROM user WHERE id IN (SELECT user_id FROM order)` (poor performance)

### Insert Statements (INSERT)

**Rules you should follow**:

1. **Explicitly specify column names**
   - ✅ `INSERT INTO user (name, email) VALUES (?, ?)`
   - ❌ `INSERT INTO user VALUES (?, ?, ?)` (unclear, error-prone)

2. **Batch insert optimization**
   - ✅ `INSERT INTO user (name, email) VALUES (?, ?), (?, ?), (?, ?)`
   - ❌ Multiple single inserts (poor performance)

3. **Use ON DUPLICATE KEY UPDATE**
   - Use when need to insert or update
   - `INSERT INTO user (id, name) VALUES (?, ?) ON DUPLICATE KEY UPDATE name = VALUES(name)`

### Update Statements (UPDATE)

**Rules you should follow**:

1. **Must have WHERE condition**
   - ✅ `UPDATE user SET name = ? WHERE id = ?`
   - ❌ `UPDATE user SET name = ?` (will update all records, extremely dangerous)

2. **Avoid updating large amounts of data**
   - Large batch updates should be done in batches
   - Use LIMIT to control quantity per batch

3. **Query before update**
   - Confirm number of affected records
   - Avoid misoperations

### Delete Statements (DELETE)

**Rules you should follow**:

1. **Must have WHERE condition**
   - ✅ `DELETE FROM user WHERE id = ?`
   - ❌ `DELETE FROM user` (will delete all records, extremely dangerous)

2. **Prefer soft delete**
   - ✅ `UPDATE user SET deleted = 1 WHERE id = ?`
   - Physical deletion requires caution

3. **Query before deletion**
   - Confirm records to be deleted
   - Avoid accidental deletion

## Index Usage Standards

### Index rules you should know

1. **Conditions for index effectiveness**
   - WHERE conditions use indexed columns
   - Avoid function operations on indexed columns
   - Avoid using OR to connect conditions (may cause index invalidation)
   - Use covering indexes to reduce lookups

2. **Scenarios where indexes become invalid**
   - ❌ `WHERE YEAR(create_time) = 2024` (function operation)
   - ❌ `WHERE name LIKE '%test%'` (leading wildcard query)
   - ❌ `WHERE age + 1 = 20` (column calculation)
   - ❌ `WHERE name != 'test'` (not equal)

3. **Leftmost prefix principle for composite indexes**
   - Index (a, b, c) can support: a, a+b, a+b+c
   - Does not support: b, c, b+c

### How you should create indexes

**Index naming conventions**:
- Primary key index: `pk_table_name`
- Unique index: `uk_column_name`
- Regular index: `idx_column_name`
- Composite index: `idx_column1_column2`

**Index creation principles**:
- Frequently queried columns
- Columns in WHERE, ORDER BY, GROUP BY
- Columns with high selectivity
- Avoid too many indexes (affects write performance)

## JOIN Query Standards

### JOIN rules you should follow

1. **Explicitly specify JOIN type**
   - INNER JOIN: returns only matching records
   - LEFT JOIN: returns all records from left table
   - RIGHT JOIN: returns all records from right table (rarely used, can be replaced with LEFT JOIN)

2. **JOIN conditions must use indexes**
   - Columns in ON clause should have indexes
   - Avoid joining large tables

3. **Control number of JOIN tables**
   - Recommended not to exceed 3 tables
   - Too many JOINs affect performance

4. **Small table drives large table**
   - Place small table on left side of JOIN
   - Reduce number of loops

## Transaction Usage Standards

### Transaction rules you should follow

1. **Minimize transaction scope**
   - Only include necessary operations
   - Avoid long transactions

2. **Operations to avoid in transactions**
   - ❌ Remote calls (RPC, HTTP)
   - ❌ Complex calculations
   - ❌ File operations
   - ❌ Waiting for user input

3. **Reasonable isolation level settings**
   - READ COMMITTED: most scenarios
   - REPEATABLE READ: need repeatable reads
   - SERIALIZABLE: rarely used

## MyBatis Usage Standards

### MyBatis rules you should follow

1. **Use #{} instead of ${}**
   - ✅ `WHERE id = #{id}` (parameterized query, safe)
   - ❌ `WHERE id = ${id}` (string concatenation, unsafe)
   - Exception: ORDER BY, table names, column names must use ${}

2. **Proper use of resultMap**
   - Use when field names don't match property names
   - Use for complex object mapping
   - Avoid overuse (use resultType for simple queries)

3. **Avoid N+1 query problem**
   - Use JOIN or batch queries
   - Avoid querying database in loops

4. **Use dynamic SQL**
   - `<if>`: conditional judgment
   - `<where>`: automatically handle WHERE keyword
   - `<foreach>`: batch operations
   - `<choose>`: multi-condition selection

## Performance Optimization Standards

### Optimization principles you should follow

1. **Query optimization**
   - Only query needed columns
   - Use LIMIT to restrict result sets
   - Avoid using DISTINCT (consider using GROUP BY)
   - Avoid using HAVING (use WHERE if possible)

2. **Pagination optimization**
   - Use deferred association for deep pagination
   - ✅ `SELECT * FROM user WHERE id > #{lastId} LIMIT 10`
   - ⚠️ `SELECT * FROM user LIMIT 10000, 10` (poor performance for deep pagination)

3. **COUNT optimization**
   - ❌ `SELECT COUNT(*) FROM user` (slow for large tables)
   - ✅ Use caching or pre-calculation
   - ✅ Use approximate values (if allowed)

4. **Batch operation optimization**
   - Batch insert using multi-row VALUES
   - Batch update using CASE WHEN
   - Batch delete in batches

## Data Type Selection Standards

### Type selection principles you should follow

1. **Integer types**
   - TINYINT: -128 to 127 (for status, type)
   - INT: -2^31 to 2^31-1 (for ID, quantity)
   - BIGINT: -2^63 to 2^63-1 (for large data volume IDs)

2. **String types**
   - CHAR: fixed length (for phone numbers, ID numbers)
   - VARCHAR: variable length (for names, addresses)
   - TEXT: large text (for article content)

3. **Date time types**
   - DATE: date (for birthday)
   - DATETIME: date time (for creation time)
   - TIMESTAMP: timestamp (auto-update)

4. **Amount types**
   - ✅ DECIMAL(10,2): precise calculation
   - ❌ FLOAT/DOUBLE: imprecise, not suitable for amounts

## Naming Standards

### Naming rules you should follow

1. **Table names**
   - Lowercase letters, underscore separated
   - Use plural form or business meaning
   - Example: `user`, `order_item`, `product_category`

2. **Column names**
   - Lowercase letters, underscore separated
   - Self-explanatory
   - Example: `user_id`, `create_time`, `order_status`

3. **Index names**
   - Primary key: `pk_table_name`
   - Unique index: `uk_column_name`
   - Regular index: `idx_column_name`

4. **Constraint names**
   - Foreign key: `fk_table_column`
   - Check constraint: `ck_table_column`

## Common Errors and Corrections

### Errors you should avoid

| Error Type | Wrong Approach | Correct Approach | Reason |
|---------|---------|---------|------|
| **SQL Injection** | String concatenation SQL | Use parameterized queries | Security risk |
| **SELECT *** | `SELECT *` | Explicitly specify column names | Performance and maintainability |
| **UPDATE/DELETE without WHERE** | `UPDATE user SET ...` | Must have WHERE condition | Data safety |
| **Index invalidation** | `WHERE YEAR(time) = 2024` | `WHERE time >= '2024-01-01'` | Performance issue |
| **N+1 queries** | Query database in loop | Use JOIN or batch queries | Performance issue |
| **Deep pagination** | `LIMIT 10000, 10` | Use deferred association or cursor | Performance issue |
| **Long transactions** | RPC calls in transaction | Minimize transaction scope | Lock waiting |
| **Using ${}** | `WHERE id = ${id}` | `WHERE id = #{id}` | SQL injection risk |

## Your Checklist

When writing SQL, you should check:

### Security Check
- [ ] Use parameterized queries (#{} instead of ${})
- [ ] UPDATE/DELETE statements have WHERE conditions
- [ ] No direct concatenation of user input

### Performance Check
- [ ] Use explicit column names instead of SELECT *
- [ ] WHERE conditions use indexed columns
- [ ] Avoid function operations on indexed columns
- [ ] Use LIMIT to restrict result sets
- [ ] Avoid N+1 query problems

### Maintainability Check
- [ ] SQL statements are clear and readable
- [ ] Use meaningful table aliases
- [ ] Add comments for complex SQL
- [ ] Follow naming conventions

### Correctness Check
- [ ] JOIN conditions are correct
- [ ] WHERE condition logic is correct
- [ ] Data types match
- [ ] NULL value handling considered

## Key Principles Summary

### Security Principles
1. **Always use parameterized queries**: prevent SQL injection
2. **UPDATE/DELETE must have WHERE**: prevent misoperations
3. **Validate user input**: prevent malicious data

### Performance Principles
1. **Proper use of indexes**: improve query speed
2. **Avoid full table scans**: reduce database load
3. **Batch operation optimization**: reduce network overhead
4. **Control transaction scope**: reduce lock waiting

### Maintainability Principles
1. **Explicitly specify column names**: improve readability
2. **Use meaningful aliases**: easy to understand
3. **Follow naming conventions**: unified code style
4. **Add appropriate comments**: explain complex logic

## Key Benefits

Following these standards can achieve:

- ✅ Prevent SQL injection attacks, ensure system security
- ✅ Improve query performance, reduce database load
- ✅ Improve code readability and maintainability
- ✅ Reduce SQL errors and data issues
- ✅ Facilitate team collaboration and code review
