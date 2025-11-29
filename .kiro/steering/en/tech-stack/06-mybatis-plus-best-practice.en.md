---
inclusion: manual
---

# MyBatis-Plus Best Practices Guide

## Quick Reference

| Rule | Requirement | Priority |
|------|-------------|----------|
| SQL in XML | MUST define conditional queries in Mapper XML | P0 |
| MyBatis-Plus for Simple Ops | Use MyBatis-Plus API ONLY for insert/update by ID/query by ID | P0 |
| No Wrapper in Service | NEVER use QueryWrapper/UpdateWrapper in Service layer | P0 |
| Logical Delete | MUST use @TableLogic for delete operations | P1 |
| Auto-Fill Fields | MUST use MetaObjectHandler for create_time/update_time | P1 |

## Critical Rules (NON-NEGOTIABLE)

| Rule | Description | ✅ Correct | ❌ Wrong |
|------|-------------|------------|----------|
| **Conditional Queries in XML** | ALL conditional queries MUST be defined in Mapper XML | Define SQL in `UserMapper.xml` | Use `lambdaQuery().eq().list()` in Service |
| **No Wrapper in Business Code** | STRICTLY FORBIDDEN to use QueryWrapper/UpdateWrapper | `userMapper.selectByUsername(username)` | `lambdaQuery().eq(User::getUsername, username)` |
| **MyBatis-Plus Allowed Ops** | ONLY use for insert/updateById/getById operations | `save()`, `updateById()`, `getById()` | `update(wrapper)`, `list(wrapper)` |
| **XML for All Complex Queries** | Multi-table JOIN, subqueries MUST be in XML | Define in Mapper XML file | Build complex queries with Wrapper |
| **Unified SQL Management** | All SQL MUST be centrally managed for review | All queries visible in XML files | SQL scattered in Java code |

## Why Choose MyBatis-Plus

MyBatis-Plus (MP for short) is an enhancement tool for MyBatis that only adds features without making changes.

### Core Advantages

- **Non-invasive**: Only enhances without changing, introduction doesn't affect existing projects
- **Low loss**: Automatically injects basic CRUD operations on startup, performance loss minimal
- **Powerful CRUD operations**: Built-in generic Mapper and Service, most single-table CRUD operations achieved with minimal configuration
- **Lambda support**: Write query conditions through Lambda expressions, no need to worry about field typos
- **Auto-generated primary keys**: Supports up to 4 primary key strategies, freely configurable
- **ActiveRecord support**: Entity classes only need to extend Model class for powerful CRUD operations
- **Custom global operations**: Supports global generic method injection
- **Built-in code generator**: Quickly generate Mapper, Model, Service, Controller layer code via code or Maven plugin
- **Built-in pagination plugin**: Based on MyBatis physical pagination, developers don't need to worry about specific operations
- **Built-in performance analysis plugin**: Outputs SQL statements and execution time
- **Built-in global interceptor plugin**: Intelligent analysis and blocking of full table delete/update operations
- **Built-in SQL injection stripper**: Supports SQL injection stripping, effectively prevents SQL injection attacks

### Applicable Scenarios

- Projects with frequent single-table CRUD operations
- Projects requiring rapid development
- Persistence layer development in microservice architecture
- Team projects requiring unified data access standards

---

## Dependency Management

### Maven Dependency Configuration

#### Parent POM Dependency Management (pom.xml)

```xml
<properties>
    <!-- MyBatis-Plus version -->
    <mybatis-plus.version>3.5.7</mybatis-plus.version>
    <!-- Druid version -->
    <druid.version>1.2.20</druid.version>
</properties>

<dependencyManagement>
    <dependencies>
        <!-- MyBatis-Plus Spring Boot 3 Starter -->
        <dependency>
            <groupId>com.baomidou</groupId>
            <artifactId>mybatis-plus-spring-boot3-starter</artifactId>
            <version>${mybatis-plus.version}</version>
        </dependency>

        <!-- Druid database connection pool -->
        <dependency>
            <groupId>com.alibaba</groupId>
            <artifactId>druid-spring-boot-starter</artifactId>
            <version>${druid.version}</version>
        </dependency>
    </dependencies>
</dependencyManagement>
```

#### Sub-module Dependency Configuration (infrastructure/repository/mysql-impl/pom.xml)

```xml
<dependencies>
    <!-- MyBatis-Plus Spring Boot 3 Starter -->
    <dependency>
        <groupId>com.baomidou</groupId>
        <artifactId>mybatis-plus-spring-boot3-starter</artifactId>
    </dependency>

    <!-- Druid database connection pool -->
    <dependency>
        <groupId>com.alibaba</groupId>
        <artifactId>druid-spring-boot-starter</artifactId>
    </dependency>

    <!-- MySQL driver -->
    <dependency>
        <groupId>com.mysql</groupId>
        <artifactId>mysql-connector-j</artifactId>
        <scope>runtime</scope>
    </dependency>

    <!-- Lombok -->
    <dependency>
        <groupId>org.projectlombok</groupId>
        <artifactId>lombok</artifactId>
        <optional>true</optional>
    </dependency>
</dependencies>
```

### Version Selection Recommendations

- **JDK**: 21 (LTS version, fully supported by Spring Boot 3.4.1)
- **Spring Boot**: 3.4.1 (latest stable version)
- **Spring Cloud**: 2025.0.0 (compatible with Spring Boot 3.4.1)
- **MyBatis-Plus**: 3.5.7 (supports Spring Boot 3, must use `mybatis-plus-spring-boot3-starter`)
- **Druid**: 1.2.20 (Alibaba database connection pool, supports Spring Boot 3)
- **MySQL Connector/J**: Managed by Spring Boot BOM (Spring Boot 3.4.1 defaults to 8.x version)

### Version Compatibility Notes

1. **Spring Boot 3.x + MyBatis-Plus 3.5.7**:
   - ✅ **Must use**: `mybatis-plus-spring-boot3-starter` (dedicated for Spring Boot 3)
   - ❌ **Cannot use**: `mybatis-plus-boot-starter` (only supports Spring Boot 2)
   - **Reason**: Spring Boot 3's Jakarta EE support differs from Spring Boot 2, must use dedicated starter

2. **MySQL driver class name**:
   - ✅ **Correct**: `com.mysql.cj.jdbc.Driver` (MySQL Connector/J 8.x)
   - ❌ **Wrong**: `com.mysql.jdbc.Driver` (deprecated)
   - **Reason**: MySQL Connector/J 8.x uses new driver class name

3. **JDK version requirements**:
   - Spring Boot 3.4.1 minimum requirement JDK 17
   - Recommended JDK 21 (LTS version)
   - MyBatis-Plus 3.5.7 supports JDK 8+, fully compatible with JDK 21

4. **Dependency version management principles**:
   - All version numbers defined in parent POM's `<properties>`
   - Declare dependencies in parent POM's `<dependencyManagement>`
   - Sub-modules don't specify version numbers, inherit from parent POM
   - Spring Boot and Spring Cloud manage dependency versions through BOM

---

## Basic Configuration

### application.yml Configuration Example

```yaml
spring:
  datasource:
    # Datasource configuration
    url: jdbc:mysql://localhost:3306/mydb?useUnicode=true&characterEncoding=utf-8&useSSL=false&serverTimezone=Asia/Shanghai
    username: root
    password: password
    driver-class-name: com.mysql.cj.jdbc.Driver

    # HikariCP connection pool configuration (optional)
    hikari:
      minimum-idle: 5
      maximum-pool-size: 20
      idle-timeout: 600000
      max-lifetime: 1800000
      connection-timeout: 30000

mybatis-plus:
  # Mapper XML file location
  mapper-locations: classpath*:/mapper/**/*.xml

  # Entity class package path
  type-aliases-package: com.example.project.domain.entity

  # Global configuration
  global-config:
    # Database related configuration
    db-config:
      # Primary key type (AUTO: database auto-increment, ASSIGN_ID: snowflake algorithm, ASSIGN_UUID: UUID)
      id-type: ASSIGN_ID

      # Table name prefix
      table-prefix: t_

      # Logical delete configuration
      logic-delete-field: deleted
      logic-delete-value: 1
      logic-not-delete-value: 0

    # Whether to print Banner
    banner: false

  # MyBatis native configuration
  configuration:
    # Camel case naming conversion
    map-underscore-to-camel-case: true

    # Cache configuration
    cache-enabled: false

    # Log implementation
    log-impl: org.apache.ibatis.logging.stdout.StdOutImpl
```

### Configuration Class

```java
package com.example.project.config;

import com.baomidou.mybatisplus.annotation.DbType;
import com.baomidou.mybatisplus.extension.plugins.MybatisPlusInterceptor;
import com.baomidou.mybatisplus.extension.plugins.inner.BlockAttackInnerInterceptor;
import com.baomidou.mybatisplus.extension.plugins.inner.OptimisticLockerInnerInterceptor;
import com.baomidou.mybatisplus.extension.plugins.inner.PaginationInnerInterceptor;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@MapperScan("com.example.project.infrastructure.repository.mapper")
public class MybatisPlusConfig {

    /**
     * MyBatis-Plus interceptor configuration
     */
    @Bean
    public MybatisPlusInterceptor mybatisPlusInterceptor() {
        MybatisPlusInterceptor interceptor = new MybatisPlusInterceptor();

        // 1. Pagination plugin (must be first)
        PaginationInnerInterceptor paginationInnerInterceptor = new PaginationInnerInterceptor(DbType.MYSQL);
        paginationInnerInterceptor.setMaxLimit(500L); // Max single page limit
        paginationInnerInterceptor.setOverflow(false); // Handle overflow after total pages
        interceptor.addInnerInterceptor(paginationInnerInterceptor);

        // 2. Optimistic lock plugin
        interceptor.addInnerInterceptor(new OptimisticLockerInnerInterceptor());

        // 3. Block full table update and delete plugin
        interceptor.addInnerInterceptor(new BlockAttackInnerInterceptor());

        return interceptor;
    }

    /**
     * Meta object handler (auto-fill creation time, update time, etc.)
     */
    @Bean
    public MetaObjectHandler metaObjectHandler() {
        return new CustomMetaObjectHandler();
    }
}
```

### Meta Object Auto-fill Handler

```java
package com.example.project.config;

import com.baomidou.mybatisplus.core.handlers.MetaObjectHandler;
import org.apache.ibatis.reflection.MetaObject;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

public class CustomMetaObjectHandler implements MetaObjectHandler {

    @Override
    public void insertFill(MetaObject metaObject) {
        // Create time
        this.strictInsertFill(metaObject, "createTime", LocalDateTime.class, LocalDateTime.now());
        // Update time
        this.strictInsertFill(metaObject, "updateTime", LocalDateTime.class, LocalDateTime.now());
        // Creator (can get from context)
        this.strictInsertFill(metaObject, "createBy", String.class, getCurrentUser());
        // Updater
        this.strictInsertFill(metaObject, "updateBy", String.class, getCurrentUser());
    }

    @Override
    public void updateFill(MetaObject metaObject) {
        // Update time
        this.strictUpdateFill(metaObject, "updateTime", LocalDateTime.class, LocalDateTime.now());
        // Updater
        this.strictUpdateFill(metaObject, "updateBy", String.class, getCurrentUser());
    }

    private String getCurrentUser() {
        // Get current user from Spring Security or other context
        // Simplified here
        return "system";
    }
}
```

---

## Entity Class Design Standards

### Base Entity Class

```java
package com.example.project.domain.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * Base entity class
 * Contains common fields for all tables
 */
@Data
public abstract class BaseEntity implements Serializable {

    /**
     * Primary key ID (generated by snowflake algorithm)
     */
    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    /**
     * Create time (auto-filled)
     */
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    /**
     * Update time (auto-filled)
     */
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;

    /**
     * Creator (auto-filled)
     */
    @TableField(fill = FieldFill.INSERT)
    private String createBy;

    /**
     * Updater (auto-filled)
     */
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private String updateBy;

    /**
     * Logical delete flag (0: not deleted, 1: deleted)
     */
    @TableLogic
    private Integer deleted;

    /**
     * Optimistic lock version number
     */
    @Version
    private Integer version;
}
```

### Business Entity Class Example

```java
package com.example.project.domain.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * User entity
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("t_user")
public class UserEntity extends BaseEntity {

    /**
     * Username
     */
    private String username;

    /**
     * Password (encrypted storage)
     */
    private String password;

    /**
     * Nickname
     */
    private String nickname;

    /**
     * Email
     */
    private String email;

    /**
     * Phone number
     */
    private String phone;

    /**
     * Status (0: disabled, 1: enabled)
     */
    private Integer status;
}
```

---

## Mapper Layer Development Standards

### Basic Mapper Interface

```java
package com.example.project.infrastructure.repository.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.project.domain.entity.UserEntity;
import org.apache.ibatis.annotations.Mapper;

/**
 * User Mapper
 */
@Mapper
public interface UserMapper extends BaseMapper<UserEntity> {
    // After extending BaseMapper, automatically has basic CRUD methods
    // Can add custom methods here
}
```

### Custom SQL Methods

**Important Standard**: Except for inserts, updates, and queries by primary key, all other data operations should be defined in Mapper XML.

```java
@Mapper
public interface UserMapper extends BaseMapper<UserEntity> {

    // ✅ Recommended: Define SQL in XML
    /**
     * Query user by username
     */
    UserEntity selectByUsername(@Param("username") String username);

    /**
     * Query user list by status
     */
    List<UserEntity> selectByStatus(@Param("status") Integer status);

    /**
     * Count users by conditions
     */
    Long countByCondition(@Param("username") String username,
                          @Param("status") Integer status,
                          @Param("startTime") LocalDateTime startTime,
                          @Param("endTime") LocalDateTime endTime);
}
```

### Mapper XML File

**All conditional queries and complex queries should be defined in XML for unified management and performance optimization.**

```xml
<?xml version="1.0" encoding="UTF-8"?>
<!DOCTYPE mapper PUBLIC "-//mybatis.org//DTD Mapper 3.0//EN"
    "http://mybatis.org/dtd/mybatis-3-mapper.dtd">
<mapper namespace="com.example.project.infrastructure.repository.mapper.UserMapper">

    <!-- Result mapping -->
    <resultMap id="BaseResultMap" type="com.example.project.domain.entity.UserEntity">
        <id column="id" property="id"/>
        <result column="username" property="username"/>
        <result column="password" property="password"/>
        <result column="nickname" property="nickname"/>
        <result column="email" property="email"/>
        <result column="phone" property="phone"/>
        <result column="status" property="status"/>
        <result column="create_time" property="createTime"/>
        <result column="update_time" property="updateTime"/>
        <result column="create_by" property="createBy"/>
        <result column="update_by" property="updateBy"/>
        <result column="deleted" property="deleted"/>
        <result column="version" property="version"/>
    </resultMap>

    <!-- Query by username -->
    <select id="selectByUsername" resultMap="BaseResultMap">
        SELECT * FROM t_user
        WHERE username = #{username}
        AND deleted = 0
    </select>

    <!-- Query by status -->
    <select id="selectByStatus" resultMap="BaseResultMap">
        SELECT * FROM t_user
        WHERE status = #{status}
        AND deleted = 0
        ORDER BY create_time DESC
    </select>

    <!-- Count by conditions -->
    <select id="countByCondition" resultType="java.lang.Long">
        SELECT COUNT(1) FROM t_user
        WHERE deleted = 0
        <if test="username != null and username != ''">
            AND username LIKE CONCAT('%', #{username}, '%')
        </if>
        <if test="status != null">
            AND status = #{status}
        </if>
        <if test="startTime != null">
            AND create_time &gt;= #{startTime}
        </if>
        <if test="endTime != null">
            AND create_time &lt;= #{endTime}
        </if>
    </select>

</mapper>
```

---

## Service Layer Development Standards

### Service Implementation Class

**Follow data operation standards: conditional queries use Mapper XML, inserts and updates use MyBatis-Plus API.**

```java
package com.example.project.application.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.project.application.service.UserService;
import com.example.project.domain.entity.UserEntity;
import com.example.project.infrastructure.repository.mapper.UserMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * User service implementation
 */
@Slf4j
@Service
public class UserServiceImpl extends ServiceImpl<UserMapper, UserEntity> implements UserService {

    @Override
    public UserEntity getByUsername(String username) {
        // ✅ Recommended: Use method defined in Mapper XML
        return baseMapper.selectByUsername(username);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean register(UserEntity user) {
        // Check if username already exists
        UserEntity existUser = getByUsername(user.getUsername());
        if (existUser != null) {
            throw new RuntimeException("Username already exists");
        }

        // ✅ Insert operation: Use MyBatis-Plus API
        return this.save(user);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean updateUserStatus(Long userId, Integer status) {
        // ✅ Update by primary key: Use MyBatis-Plus API
        UserEntity user = new UserEntity();
        user.setId(userId);
        user.setStatus(status);
        return this.updateById(user);
    }
}
```

---

## Data Operation Standards (Core Best Practice)

### Standard Description

For unified management, code review, and performance analysis, data operations in the project should strictly follow these standards:

**Allowed to use MyBatis-Plus API**:
- ✅ Insert operations: `save()`, `saveBatch()`, `saveOrUpdate()`, etc.
- ✅ Update operations: `updateById()`, `updateBatchById()`, etc.
- ✅ Query by primary key: `getById()`, `listByIds()`, etc.

**Must be implemented in Mapper XML**:
- ❌ All conditional queries (don't use Wrapper)
- ❌ All conditional updates (don't use UpdateWrapper)
- ❌ All conditional deletes (don't use QueryWrapper)
- ❌ All complex queries (multi-table joins, subqueries, aggregations, etc.)

### Reasons for Standards

1. **Unified management**: All SQL statements centralized in XML files
2. **Code review**: DBAs and technical leads can quickly review all SQL
3. **Performance analysis**: Easy to analyze SQL performance with tools
4. **Maintainability**: SQL statements clearly visible, easy to understand
5. **Team collaboration**: Unified development standards

---

## Pagination Query

### Pagination Query Standards

**Important**: According to project data operation standards, all pagination queries must define SQL in Mapper XML, prohibited from using Wrapper to construct query conditions.

### Custom Pagination Query

```java
// Mapper interface
@Mapper
public interface UserMapper extends BaseMapper<UserEntity> {

    /**
     * Custom pagination query (need to write SQL in XML)
     */
    IPage<UserEntity> selectUserPage(Page<?> page, @Param("username") String username);
}

// XML file
<select id="selectUserPage" resultType="com.example.project.domain.entity.UserEntity">
    SELECT * FROM t_user
    WHERE deleted = 0
    <if test="username != null and username != ''">
        AND username LIKE CONCAT('%', #{username}, '%')
    </if>
    ORDER BY create_time DESC
</select>

// Service call
public IPage<UserEntity> getUserPage(Integer current, Integer size, String username) {
    Page<UserEntity> page = new Page<>(current, size);
    return baseMapper.selectUserPage(page, username);
}
```

---

## Performance Optimization

### Performance Optimization Principles

1. **Proper use of indexes**: Add indexes for commonly queried fields
2. **Avoid querying all fields**: Only query needed columns
3. **Batch operations**: Batch insert, batch update
4. **Use pagination**: Avoid querying large amounts of data at once
5. **Enable second-level cache**: Use cautiously

---

## Common Issues and Solutions

### 1. Logical Delete Not Working

**Problem**: After delete operation, data is physically deleted instead of logically deleted.

**Solution**:
```java
// Ensure entity class has @TableLogic annotation
@TableLogic
private Integer deleted;

// Ensure configuration file has logical delete configured
mybatis-plus:
  global-config:
    db-config:
      logic-delete-field: deleted
      logic-delete-value: 1
      logic-not-delete-value: 0
```

### 2. Auto-fill Not Working

**Problem**: Fields like create time, update time not auto-filled.

**Solution**:
```java
// 1. Ensure entity class has @TableField annotation
@TableField(fill = FieldFill.INSERT)
private LocalDateTime createTime;

// 2. Ensure MetaObjectHandler configured
@Bean
public MetaObjectHandler metaObjectHandler() {
    return new CustomMetaObjectHandler();
}

// 3. Ensure MetaObjectHandler implementation correct
@Override
public void insertFill(MetaObject metaObject) {
    this.strictInsertFill(metaObject, "createTime", LocalDateTime.class, LocalDateTime.now());
}
```

---

## Best Practices Summary

### Development Standards

1. **Entity class standards**: Inherit unified base entity class, use Lombok, properly use annotations
2. **Mapper standards**: Extend BaseMapper, complex queries use XML
3. **Service standards**: Extend IService and ServiceImpl, business logic in Service layer
4. **Data operation standards (Important)**:
   - Allowed to use MyBatis-Plus API: inserts, updates by primary key, queries by primary key
   - Must implement in Mapper XML: conditional queries, conditional updates, conditional deletes, complex queries
5. **Pagination query standards**: Must limit max single page count

### Performance Optimization

1. **Query optimization**: Only query needed fields, properly use indexes
2. **Cache strategy**: Hot data use Redis cache
3. **Connection pool configuration**: Properly set connection pool size

### Security Standards

1. **SQL injection prevention**: Use parameterized queries
2. **Data permissions**: Implement data permission interceptor
3. **Protection measures**: Enable block full table update/delete plugin

---

## References

- [MyBatis-Plus Official Documentation](https://baomidou.com/)
- [MyBatis-Plus GitHub](https://github.com/baomidou/mybatis-plus)
- [MyBatis Official Documentation](https://mybatis.org/mybatis-3/zh/index.html)
- [Spring Boot Official Documentation](https://spring.io/projects/spring-boot)

---

**Last Updated**: 2024-11-10
