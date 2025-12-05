# 实现计划 - 子图管理

## 任务列表

### 阶段1：数据模型和仓储层

- [x] 1. 创建领域模型和实体
  - 在 domain-api 中定义 Subgraph、SubgraphPermission、SubgraphResource 领域模型
  - 在 repository-api 中定义对应的 Entity 类
  - 定义 PermissionRole 枚举（OWNER, VIEWER）
  - **验证方法**：【Build Verification】执行 `mvn clean compile`，确认编译成功
  - _Requirements: 1.1, 1.2, 3.1, 5.1, 6.1_

- [x] 2. 定义 Repository 接口
  - 在 repository-api 中定义 SubgraphRepository 接口及方法签名（包含权限相关操作）
  - 定义 SubgraphResourceRepository 接口及方法签名
  - **验证方法**：【Build Verification】执行 `mvn clean compile`，确认接口定义正确
  - _Requirements: 1.1, 2.1, 3.1, 4.1, 5.1, 6.1_

- [x] 3. 创建数据库表结构
  - 创建 Flyway 迁移脚本定义 subgraph 表（包含 name 唯一索引、FULLTEXT 索引）
  - 创建 subgraph_permission 表（包含复合唯一索引、外键级联删除）
  - 创建 subgraph_resource 表（包含复合唯一索引、外键级联删除）
  - **验证方法**：【Runtime Verification】启动应用，检查数据库表创建成功，索引和外键约束正确
  - _Requirements: 1.4, 4.4, 10.1, 10.2_

- [x] 4. 实现 SubgraphRepository（包含权限操作）
  - 在 mysql-impl 中实现 SubgraphRepositoryImpl
  - 创建 SubgraphMapper 和 SubgraphPermissionMapper 接口及 XML 映射文件
  - 创建 SubgraphPO 和 SubgraphPermissionPO 持久化对象
  - 实现子图 CRUD：save、findById、findByName、findByUserId、searchByKeyword、filterByTags、filterByOwner、update、delete 方法
  - 实现权限操作：savePermission、findPermissionsBySubgraphId、deletePermission、countOwners、hasPermission 方法
  - **验证方法**：【Unit Test】编写单元测试验证子图 CRUD、搜索过滤、权限操作和级联删除
  - _Requirements: 1.1, 1.2, 1.4, 2.1, 2.2, 2.3, 2.4, 3.5, 4.4, 10.1_

- [x] 5. 实现 SubgraphResourceRepository
  - 在 mysql-impl 中实现 SubgraphResourceRepositoryImpl
  - 创建 SubgraphResourceMapper 接口和 XML 映射文件
  - 创建 SubgraphResourcePO 持久化对象
  - 实现 addResource、removeResource、findResourceIdsBySubgraphId、findSubgraphIdsByResourceId、existsInSubgraph、deleteAllBySubgraphId、deleteAllByResourceId 方法
  - **验证方法**：【Unit Test】编写单元测试验证资源关联操作和级联删除
  - _Requirements: 5.1, 5.4, 5.6, 6.3, 6.4, 10.2_

### 阶段2：领域服务层

- [x] 6. 实现子图创建功能
  - 在 domain-impl 中实现 SubgraphDomainService.createSubgraph() 方法
  - 实现名称唯一性检查逻辑
  - 实现自动创建 Owner 权限逻辑
  - 集成审计日志记录
  - **验证方法**：【Unit Test】编写单元测试验证创建逻辑、名称唯一性检查、Owner 自动分配
  - _Requirements: 1.2, 1.4, 1.6, 9.2_

- [x] 7. 实现子图查询功能
  - 实现 SubgraphDomainService.listSubgraphs() 方法（支持搜索、过滤、排序、分页）
  - 实现 SubgraphDomainService.getSubgraphDetail() 方法
  - 实现权限过滤逻辑（只返回用户有权限的子图）
  - **验证方法**：【Unit Test】编写单元测试验证查询、搜索、过滤、排序、分页和权限过滤逻辑
  - _Requirements: 2.1, 2.2, 2.3, 2.4, 2.5, 2.6, 7.1_

- [x] 8. 实现子图更新功能
  - 实现 SubgraphDomainService.updateSubgraph() 方法
  - 实现 Owner 权限检查逻辑
  - 实现名称唯一性检查（排除自身）
  - 实现乐观锁版本检查
  - 集成审计日志记录
  - **验证方法**：【Unit Test】编写单元测试验证更新逻辑、权限检查、名称唯一性、乐观锁和审计日志
  - _Requirements: 3.1, 3.2, 3.3, 3.4, 9.2, 10.4_

- [x] 9. 实现子图删除功能
  - 实现 SubgraphDomainService.deleteSubgraph() 方法
  - 实现 Owner 权限检查逻辑
  - 实现空子图检查逻辑（必须先移除所有资源节点）
  - 实现事务性删除（子图+权限记录）
  - 集成审计日志记录
  - **验证方法**：【Unit Test】编写单元测试验证删除逻辑、权限检查、空子图检查、事务回滚和审计日志
  - _Requirements: 4.1, 4.2, 4.4, 4.5, 4.6, 9.2, 10.1, 10.5_

- [x] 10. 实现权限管理功能
  - 实现 SubgraphDomainService.addPermission() 方法
  - 实现 SubgraphDomainService.removePermission() 方法
  - 实现 SubgraphDomainService.hasPermission() 方法
  - 实现防止移除最后一个 Owner 的逻辑
  - 集成审计日志记录
  - **验证方法**：【Unit Test】编写单元测试验证权限添加、移除、检查和最后 Owner 保护逻辑
  - _Requirements: 3.5, 3.6, 9.1, 9.4_

- [x] 11. 实现资源节点管理功能
  - 实现 SubgraphDomainService.addResources() 方法（支持批量添加）
  - 实现 SubgraphDomainService.removeResources() 方法（支持批量移除）
  - 实现 Owner 权限检查逻辑
  - 实现资源存在性检查（调用 ResourceService）
  - 实现重复添加检查
  - 集成审计日志记录
  - **验证方法**：【Unit Test】编写单元测试验证资源添加、移除、权限检查、重复检查和审计日志
  - _Requirements: 5.1, 5.2, 5.3, 5.4, 5.5, 6.2, 6.3, 6.5, 9.3_

- [x] 12. 实现子图拓扑查询功能
  - 实现 SubgraphDomainService.getSubgraphTopology() 方法
  - 实现权限检查逻辑（Owner 或 Viewer）
  - 查询子图中的资源节点列表
  - 调用 TopologyService 获取节点关系
  - 实现关系过滤逻辑（仅保留子图内节点的关系）
  - **验证方法**：【Unit Test】编写单元测试验证拓扑查询、权限检查和关系过滤逻辑
  - _Requirements: 7.3, 7.4, 9.1_

### 阶段3：应用服务层

- [x] 13. 定义应用服务接口和 DTO
  - 在 application-api 中定义 SubgraphApplicationService 接口
  - 定义 Command 对象（CreateSubgraphCommand、UpdateSubgraphCommand、DeleteSubgraphCommand、AddResourcesCommand、RemoveResourcesCommand、UpdatePermissionsCommand）
  - 定义 Query 对象（ListSubgraphsQuery）
  - 定义 DTO 对象（SubgraphDTO、SubgraphDetailDTO、TopologyGraphDTO、PageResult）
  - **验证方法**：【Build Verification】执行 `mvn clean compile`，确认接口和 DTO 定义正确
  - _Requirements: All functional requirements_

- [x] 14. 实现子图创建应用服务
  - 在 application-impl 中实现 createSubgraph() 方法
  - 实现 DTO 到领域对象的转换
  - 添加 @Transactional 注解控制事务边界
  - 实现异常处理和错误码映射
  - **验证方法**：【Unit Test】编写单元测试验证事务控制、DTO 转换和异常处理
  - _Requirements: 1.1, 1.2, 1.3, 1.4, 1.5, 1.6_

- [x] 15. 实现子图查询应用服务
  - 实现 listSubgraphs() 方法（支持分页）
  - 实现 getSubgraphDetail() 方法
  - 实现领域对象到 DTO 的转换
  - 实现异常处理和错误码映射
  - **验证方法**：【Unit Test】编写单元测试验证查询逻辑、分页、DTO 转换和异常处理
  - _Requirements: 2.1, 2.2, 2.3, 2.4, 2.5, 2.6, 7.1, 7.2, 7.7_

- [x] 16. 实现子图更新应用服务
  - 实现 updateSubgraph() 方法
  - 实现 updatePermissions() 方法
  - 添加 @Transactional 注解控制事务边界
  - 实现 DTO 转换和异常处理
  - **验证方法**：【Unit Test】编写单元测试验证更新逻辑、事务控制、DTO 转换和异常处理
  - _Requirements: 3.1, 3.2, 3.3, 3.4, 3.5, 3.6_

- [x] 17. 实现子图删除应用服务
  - 实现 deleteSubgraph() 方法
  - 添加 @Transactional 注解控制事务边界
  - 实现异常处理和错误码映射
  - **验证方法**：【Unit Test】编写单元测试验证删除逻辑、事务控制和异常处理
  - _Requirements: 4.1, 4.2, 4.3, 4.4, 4.5, 4.6_

- [x] 18. 实现资源节点管理应用服务
  - 实现 addResourcesToSubgraph() 方法
  - 实现 removeResourcesFromSubgraph() 方法
  - 添加 @Transactional 注解控制事务边界
  - 实现异常处理和错误码映射
  - **验证方法**：【Unit Test】编写单元测试验证资源管理逻辑、事务控制和异常处理
  - _Requirements: 5.1, 5.2, 5.3, 5.4, 5.5, 5.6, 6.1, 6.2, 6.3, 6.4, 6.5, 6.6_

- [x] 19. 实现拓扑查询应用服务
  - 实现 getSubgraphTopology() 方法
  - 实现领域对象到 TopologyGraphDTO 的转换
  - 实现异常处理和错误码映射
  - **验证方法**：【Unit Test】编写单元测试验证拓扑查询逻辑、DTO 转换和异常处理
  - _Requirements: 7.3, 7.4, 7.5, 7.6_

### 阶段4：接口层

- [x] 20. 定义 REST API 接口
  - 在 interface-http 中创建 SubgraphController
  - 定义 Request DTO（CreateSubgraphRequest、UpdateSubgraphRequest、AddResourcesRequest、RemoveResourcesRequest、UpdatePermissionsRequest）
  - 定义 Response DTO（SubgraphResponse、SubgraphListResponse、SubgraphDetailResponse、TopologyGraphResponse）
  - 添加 @RestController 和 @RequestMapping 注解
  - **验证方法**：【Build Verification】执行 `mvn clean compile`，确认 Controller 和 DTO 定义正确
  - _Requirements: All functional requirements_

- [x] 21. 实现子图创建 API
  - 实现 POST /api/v1/subgraphs 端点
  - 添加 @Valid 注解进行参数校验
  - 实现 Request DTO 到 Command 的转换
  - 实现统一响应格式封装
  - **验证方法**：【Runtime Verification】启动应用，使用 curl 或 Postman 测试创建 API，验证返回 201 状态码和正确的响应数据
  - _Requirements: 1.1, 1.2, 1.3, 1.4, 1.5_

- [x] 22. 实现子图查询 API
  - 实现 GET /api/v1/subgraphs 端点（列表查询）
  - 实现 GET /api/v1/subgraphs/{id} 端点（详情查询）
  - 添加查询参数校验（keyword、tags、ownerId、sortBy、page、pageSize）
  - 实现统一响应格式封装
  - **验证方法**：【Runtime Verification】启动应用，测试列表和详情查询 API，验证搜索、过滤、排序、分页功能正常
  - _Requirements: 2.1, 2.2, 2.3, 2.4, 2.5, 2.6, 7.1, 7.2, 7.7_

- [x] 23. 实现子图更新 API
  - 实现 PUT /api/v1/subgraphs/{id} 端点（更新基本信息）
  - 实现 PUT /api/v1/subgraphs/{id}/permissions 端点（更新权限）
  - 添加 @Valid 注解进行参数校验
  - 实现乐观锁版本冲突处理（返回 409 状态码）
  - 实现统一响应格式封装
  - **验证方法**：【Runtime Verification】启动应用，测试更新 API，验证权限检查、版本冲突处理和响应正确
  - _Requirements: 3.1, 3.2, 3.3, 3.4, 3.5, 3.6_

- [x] 24. 实现子图删除 API
  - 实现 DELETE /api/v1/subgraphs/{id} 端点
  - 实现权限检查和空子图检查的错误处理
  - 返回 204 No Content 状态码
  - **验证方法**：【Runtime Verification】启动应用，测试删除 API，验证权限检查、空子图检查和删除成功
  - _Requirements: 4.1, 4.2, 4.3, 4.4, 4.5, 4.6_

- [x] 25. 实现资源节点管理 API
  - 实现 POST /api/v1/subgraphs/{id}/resources 端点（添加资源）
  - 实现 DELETE /api/v1/subgraphs/{id}/resources 端点（移除资源）
  - 添加参数校验（resourceIds 非空）
  - 实现统一响应格式封装
  - **验证方法**：【Runtime Verification】启动应用，测试资源添加和移除 API，验证权限检查、重复检查和操作成功
  - _Requirements: 5.1, 5.2, 5.3, 5.4, 5.5, 5.6, 6.1, 6.2, 6.3, 6.4, 6.5, 6.6_

- [x] 26. 实现拓扑查询 API
  - 实现 GET /api/v1/subgraphs/{id}/topology 端点
  - 实现权限检查的错误处理
  - 实现统一响应格式封装
  - **验证方法**：【Runtime Verification】启动应用，测试拓扑查询 API，验证权限检查、节点和关系数据正确
  - _Requirements: 7.3, 7.4, 7.5, 7.6_

- [x] 27. 实现全局异常处理器
  - 在 interface-http 中创建 GlobalExceptionHandler
  - 处理 BusinessException（400、403、404、409）
  - 处理 SystemException（500）
  - 实现统一错误响应格式（包含 code、message、timestamp、path、traceId）
  - **验证方法**：【Runtime Verification】启动应用，触发各种异常场景，验证错误响应格式正确
  - _Requirements: 9.5_

### 阶段5：集成测试和端到端测试

- [x] 28. 编写集成测试
  - 使用 TestContainers 启动 MySQL 容器
  - 编写完整的子图生命周期集成测试（创建→添加节点→更新→移除节点→删除）
  - 编写权限管理集成测试
  - 编写并发修改集成测试（验证乐观锁）
  - 编写事务回滚集成测试
  - **验证方法**：【Unit Test】执行 `mvn test -Dtest=*IntegrationTest`，确认所有集成测试通过
  - _Requirements: All functional requirements_

- [x] 29. 编写端到端测试脚本
  - 创建 Shell 脚本测试完整的 API 调用流程
  - 测试创建子图、查询列表、添加资源、查询拓扑、删除子图
  - 测试权限控制（非 Owner 无法编辑/删除）
  - 测试错误场景（名称重复、非空子图删除、版本冲突）
  - **验证方法**：【Runtime Verification】启动应用，执行 Shell 脚本，验证所有测试场景通过
  - _Requirements: All functional requirements_

### 阶段6：文档和部署

- [x] 30. 生成 API 文档
  - 配置 Springdoc OpenAPI 依赖
  - 添加 API 注解（@Operation、@ApiResponse、@Schema）
  - 生成 OpenAPI 3.0 规范文档
  - **验证方法**：【Runtime Verification】启动应用，访问 /swagger-ui.html，验证 API 文档完整且可交互测试
  - _Requirements: All functional requirements_

- [x] 31. 更新数据库迁移脚本
  - 确认所有 Flyway 迁移脚本版本号正确
  - 添加回滚脚本（如果需要）
  - 更新迁移脚本文档
  - **验证方法**：【Runtime Verification】在干净数据库上启动应用，验证所有表和索引创建成功
  - _Requirements: 10.1, 10.2_

- [x] 32. 最终验证和性能测试
  - 执行完整的功能测试（所有 API 端点）
  - 执行性能测试（JMeter 或 Gatling）
  - 验证性能指标达标（列表查询<1s、详情加载<2s、拓扑渲染<3s、操作<500ms）
  - 验证并发支持（100 个并发用户）
  - **验证方法**：【Runtime Verification】执行性能测试脚本，检查测试报告，确认所有性能指标达标
  - _Requirements: 8.1, 8.2, 8.3, 8.4, 8.5_

---

## 任务执行说明

### 执行顺序
任务按阶段顺序执行，每个阶段内的任务可以并行开发（如果资源允许）。

### 检查点
- 阶段1完成后：确保所有数据库表创建成功，Repository 单元测试通过
- 阶段2完成后：确保所有领域服务单元测试通过
- 阶段3完成后：确保所有应用服务单元测试通过
- 阶段4完成后：确保所有 API 端点可以正常访问
- 阶段5完成后：确保所有集成测试和端到端测试通过
- 阶段6完成后：确保 API 文档完整，性能指标达标

### 依赖关系
- 阶段2依赖阶段1（领域服务依赖 Repository）
- 阶段3依赖阶段2（应用服务依赖领域服务）
- 阶段4依赖阶段3（接口层依赖应用服务）
- 阶段5依赖阶段4（集成测试需要完整的功能）
- 阶段6依赖阶段5（文档和部署需要测试通过）

### 预计工作量
- 阶段1：6-10 小时（5个任务）
- 阶段2：14-20 小时（7个任务）
- 阶段3：14-20 小时（7个任务）
- 阶段4：16-24 小时（8个任务）
- 阶段5：8-12 小时（2个任务）
- 阶段6：6-8 小时（3个任务）

**总计：64-94 小时（约 8-12 个工作日）**

---

**文档版本**: v1.1
**创建日期**: 2024-12-04
**最后更新**: 2024-12-05
**更新内容**: 移除 Redis 缓存相关任务（任务28-30），重新编号后续任务，更新阶段划分
