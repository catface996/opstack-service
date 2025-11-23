# Redis 本地环境设置

## 快速启动

在本地 Docker 环境中启动 Redis：

```bash
docker run -d --name redis-local -p 6379:6379 --restart unless-stopped redis:7.0
```

## 验证 Redis 运行状态

```bash
# 查看 Redis 容器状态
docker ps | grep redis

# 测试 Redis 连接
docker exec -it redis-local redis-cli ping
# 应该返回: PONG
```

## 停止 Redis

```bash
docker stop redis-local
```

## 删除 Redis 容器

```bash
docker rm redis-local
```

## 重启 Redis

```bash
docker restart redis-local
```

## Redis 配置说明

- **端口**: 6379 (默认)
- **主机**: localhost
- **密码**: 无 (本地开发环境)
- **数据库**: 0 (默认)
- **重启策略**: unless-stopped (除非手动停止，否则自动重启)

## 应用配置

应用的 Redis 配置在 `bootstrap/src/main/resources/application-local.yml`:

```yaml
spring:
  data:
    redis:
      host: localhost
      port: 6379
      database: 0
      password: 
```

## 测试 Redis 集成

运行集成测试验证 Redis 连接：

```bash
mvn test -Dtest=RedisConnectionTest -pl bootstrap
```

测试内容：
1. Redis 基本读写操作
2. 复杂对象序列化/反序列化
3. 数据删除操作

## 故障排查

### 问题：端口 6379 已被占用

```bash
# 查看占用端口的进程
lsof -i :6379

# 停止占用端口的 Redis
docker stop $(docker ps -q --filter "publish=6379")
```

### 问题：Redis 容器无法启动

```bash
# 查看容器日志
docker logs redis-local

# 删除旧容器重新创建
docker rm -f redis-local
docker run -d --name redis-local -p 6379:6379 --restart unless-stopped redis:7.0
```

### 问题：应用无法连接 Redis

1. 确认 Redis 容器正在运行：`docker ps | grep redis`
2. 确认端口映射正确：`docker port redis-local`
3. 测试 Redis 连接：`docker exec -it redis-local redis-cli ping`
4. 检查应用配置：`bootstrap/src/main/resources/application-local.yml`
