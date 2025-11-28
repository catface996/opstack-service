# 需求文档

## 简介

本文档规定了会话管理功能(F01-4)的需求,该功能为已认证用户提供安全的会话生命周期管理。会话管理系统应创建、验证、刷新和销毁用户会话,同时防御常见的基于会话的攻击,包括会话固定攻击、会话劫持、CSRF和XSS。此功能对于维护用户认证状态、确保对受保护资源的安全访问以及支持Web浏览器和API客户端认证机制至关重要。

系统应支持每个用户跨不同设备的多个并发会话,具有可配置的限制和安全策略。会话数据应主要存储在Redis中以获得高性能,当Redis不可用时自动降级到MySQL。所有会话操作应满足严格的性能要求(验证 < 50ms P95,创建 < 200ms P95),同时保持99.9%的可用性。

## 术语表

- **会话管理系统(Session Management System)**: 负责创建、验证和管理用户认证会话的软件系统
- **会话(Session)**: 服务器端数据结构,在多个HTTP请求之间维护用户认证状态和相关信息
- **会话标识符(Session Identifier)**: 唯一的、加密随机令牌(UUID v4,熵值 >= 128位),用于标识特定用户会话
- **访问令牌(Access Token)**: 短期JWT令牌(15分钟),用于API认证,包含sessionId、userId和过期时间
- **刷新令牌(Refresh Token)**: 长期令牌(30天),用于在不重新认证的情况下获取新的访问令牌
- **令牌黑名单(Token Blacklist)**: 基于Redis的集合,存储已失效的令牌以防止登出后重用
- **绝对超时(Absolute Timeout)**: 会话可以存在的最大持续时间,无论活动如何(默认8小时,"记住我"时30天)
- **空闲超时(Idle Timeout)**: 会话过期前的最大不活动持续时间(默认30分钟)
- **最后活动时间(Last Activity Time)**: 最近一次会话验证的时间戳,每次请求时更新
- **会话固定攻击(Session Fixation Attack)**: 攻击者在认证前将用户的会话标识符设置为已知值的攻击
- **会话劫持(Session Hijacking)**: 攻击者窃取有效会话标识符以冒充用户的攻击
- **Redis**: 用作主要会话存储的内存数据存储,支持基于TTL的自动过期
- **MySQL**: 当Redis不可用时用作降级会话存储的关系数据库
- **HttpOnly Cookie**: 防止JavaScript访问Cookie值的Cookie标志,缓解XSS攻击
- **Secure Cookie**: 确保Cookie仅通过HTTPS连接传输的Cookie标志
- **SameSite Cookie**: 防止跨站传输的Cookie属性(Strict),缓解CSRF攻击
- **CSRF(跨站请求伪造)**: 强制已认证用户执行不需要的操作的攻击
- **XSS(跨站脚本)**: 向网页注入恶意脚本以窃取会话数据的攻击
- **设备信息(Device Info)**: 包含IP地址、User-Agent、设备类型、操作系统和浏览器信息的值对象
- **单设备模式(Single Device Mode)**: 每个用户只允许一个活跃会话的配置模式
- **严格IP检查(Strict IP Check)**: 当会话IP地址变化时需要重新认证的安全模式

## 需求

### 需求1: 会话创建

**用户故事:** 作为用户,我希望系统在我成功登录时创建安全的会话,以便我可以在多个请求之间保持认证状态而无需重复输入凭据。

#### 验收标准

1. WHEN 用户完成成功认证, THE 会话管理系统 SHALL 创建具有唯一会话标识符的新会话
2. WHEN 创建会话, THE 会话管理系统 SHALL 使用UUID v4算法生成具有至少128位熵值的加密安全随机会话标识符
3. WHEN 创建会话, THE 会话管理系统 SHALL 存储会话创建时间戳并将最后活动时间戳初始化为当前时间
4. WHEN 创建会话, THE 会话管理系统 SHALL 从创建时间起设置8小时(28,800秒)的绝对超时
5. WHEN 创建会话, THE 会话管理系统 SHALL 从最后活动时间起设置30分钟(1,800秒)的空闲超时
6. WHEN 用户在认证期间选择"记住我"选项, THE 会话管理系统 SHALL 将绝对超时延长至30天(2,592,000秒)
7. WHEN 创建会话且用户已有5个活跃会话, THE 会话管理系统 SHALL 在创建新会话前删除最旧的会话
8. WHEN 启用单设备模式且用户进行认证, THE 会话管理系统 SHALL 在创建新会话前删除该用户的所有现有会话
9. WHEN 创建会话, THE 会话管理系统 SHALL 存储设备信息,包括IP地址、User-Agent、设备类型、操作系统和浏览器
10. WHEN 会话创建在200毫秒内完成(P95), THE 会话管理系统 SHALL 向客户端返回会话标识符

### 需求2: 会话验证

**用户故事:** 作为用户,我希望系统在每次请求受保护资源时验证我的会话,以便只有已认证用户可以访问这些资源,同时保持无缝体验。

#### 验收标准

1. WHEN 用户访问受保护资源, THE 会话管理系统 SHALL 验证会话标识符在存储中存在
2. WHEN 验证会话, THE 会话管理系统 SHALL 通过比较当前时间与创建时间加绝对超时时长来验证会话未超过其绝对超时
3. WHEN 验证会话, THE 会话管理系统 SHALL 通过比较当前时间与最后活动时间加空闲超时时长来验证会话未超过其空闲超时
4. WHEN 会话验证成功, THE 会话管理系统 SHALL 将最后活动时间戳更新为当前时间
5. WHEN 会话验证因绝对超时过期而失败, THE 会话管理系统 SHALL 返回认证错误代码AUTH-SESSION-EXPIRED并要求重新登录
6. WHEN 会话验证因空闲超时过期而失败, THE 会话管理系统 SHALL 返回认证错误代码AUTH-SESSION-IDLE-TIMEOUT并要求重新登录
7. WHEN 会话验证因会话标识符不存在而失败, THE 会话管理系统 SHALL 返回认证错误代码AUTH-SESSION-NOT-FOUND并要求重新登录
8. WHEN 会话验证在50毫秒内完成(P95), THE 会话管理系统 SHALL 允许请求继续
9. WHEN 会话验证响应时间超过50毫秒(P95), THE 会话管理系统 SHALL 记录包含会话标识符和响应时间的性能警告
10. WHEN 系统接收到1,000个并发会话验证请求, THE 会话管理系统 SHALL 在每个请求50毫秒内(P95)处理所有请求

### 需求3: 会话超时控制

**用户故事:** 作为用户,我希望我的会话在一段时间不活动或达到最大持续时间后自动过期,以便即使我忘记从共享或公共设备登出,我的账户仍然保持安全。

#### 验收标准

1. WHEN 会话从创建起超过8小时的绝对超时, THE 会话管理系统 SHALL 自动销毁会话并删除所有相关数据
2. WHEN 会话从最后活动起超过30分钟的空闲超时, THE 会话管理系统 SHALL 自动销毁会话并删除所有相关数据
3. WHEN 启用"记住我"的会话从创建起超过30天, THE 会话管理系统 SHALL 自动销毁会话并删除所有相关数据
4. WHEN 会话因超时而自动销毁, THE 会话管理系统 SHALL 从Redis和MySQL存储中删除会话
5. WHEN 用户尝试使用已过期的会话, THE 会话管理系统 SHALL 返回错误代码AUTH-SESSION-EXPIRED和消息"您的会话已过期。请重新登录。"
6. WHEN 会话在过期前剩余5分钟(300秒), THE 会话管理系统 SHALL 在验证响应中包含警告标志
7. WHEN 客户端收到会话过期警告, THE 客户端 SHALL 显示带有倒计时和"延长会话"或"立即登出"选项的模态对话框
8. WHEN 用户在超时警告对话框中点击"延长会话", THE 客户端 SHALL 发送请求以刷新会话活动时间
9. WHEN 倒计时达到零而用户未采取行动, THE 客户端 SHALL 自动重定向到登录页面并显示消息"您的会话已过期。请重新登录。"
10. WHEN Redis基于TTL的过期删除会话, THE 会话管理系统 SHALL 不需要手动清理

### 需求4: 会话销毁

**用户故事:** 作为用户,我希望能够登出并结束我的会话,以便在使用完系统或离开共享设备时保护我的账户。

#### 验收标准

1. WHEN 用户通过点击登出按钮发起登出, THE 会话管理系统 SHALL 销毁用户的当前会话
2. WHEN 销毁会话, THE 会话管理系统 SHALL 从Redis存储中删除会话数据
3. WHEN 销毁会话, THE 会话管理系统 SHALL 从MySQL存储中删除会话数据
4. WHEN 销毁会话且会话有关联的JWT令牌, THE 会话管理系统 SHALL 将刷新令牌添加到黑名单,TTL等于令牌的剩余有效期
5. WHEN 销毁会话, THE 会话管理系统 SHALL 通过将Cookie过期时间设置为过去日期来清除客户端的所有认证Cookie
6. WHEN 销毁会话, THE 会话管理系统 SHALL 从客户端响应中清除所有JWT令牌
7. WHEN 登出成功完成, THE 会话管理系统 SHALL 返回HTTP状态200和表示成功登出的响应
8. WHEN 登出完成, THE 客户端 SHALL 将用户重定向到登录页面
9. WHEN 会话销毁在100毫秒内完成(P95), THE 会话管理系统 SHALL 返回成功响应
10. WHEN 用户尝试使用已销毁的会话, THE 会话管理系统 SHALL 返回错误代码AUTH-SESSION-NOT-FOUND

### 需求5: 多设备会话管理

**用户故事:** 作为用户,我希望管理跨多个设备的会话,以便我可以从不同位置(家、办公室、移动设备)访问系统,并出于安全目的控制哪些设备可以访问我的账户。

#### 验收标准

1. WHEN 用户从新设备登录, THE 会话管理系统 SHALL 创建具有唯一会话标识符的独立会话
2. WHEN 用户通过GET /api/v1/sessions请求其活跃会话列表, THE 会话管理系统 SHALL 返回该用户的所有活跃会话
3. WHEN 返回活跃会话列表, THE 会话管理系统 SHALL 为每个会话包含设备类型、浏览器名称和版本、操作系统、IP地址、登录时间戳和最后活动时间戳
4. WHEN 显示活跃会话列表, THE 会话管理系统 SHALL 用标志"isCurrent": true标记当前会话
5. WHEN 用户通过DELETE /api/v1/sessions/{sessionId}请求终止特定会话, THE 会话管理系统 SHALL 验证该会话属于请求用户
6. WHEN 用户终止特定会话, THE 会话管理系统 SHALL 仅销毁该会话同时保留所有其他活跃会话
7. WHEN 用户通过POST /api/v1/sessions/terminate-others请求终止所有其他会话, THE 会话管理系统 SHALL 销毁除当前会话外的所有会话
8. WHEN 用户的活跃会话数量达到配置的最大值(默认5), THE 会话管理系统 SHALL 按创建时间戳自动删除最旧的会话
9. WHEN 在配置中启用单设备模式, THE 会话管理系统 SHALL 在登录时创建新会话前删除所有现有用户会话
10. WHEN 用户只有一个活跃会话(当前会话), THE 会话管理系统 SHALL 显示消息"您当前只在一个设备上登录"

### 需求6: 安全防护

**用户故事:** 作为系统管理员,我希望会话管理系统防御常见的基于会话的攻击,以便用户账户保持安全并符合安全最佳实践。

#### 验收标准

1. WHEN 用户成功完成认证, THE 会话管理系统 SHALL 通过创建新UUID并删除旧会话来重新生成会话标识符
2. WHEN 设置会话Cookie, THE 会话管理系统 SHALL 添加HttpOnly属性以防止JavaScript访问
3. WHEN 设置会话Cookie, THE 会话管理系统 SHALL 添加Secure属性以确保仅通过HTTPS传输
4. WHEN 设置会话Cookie, THE 会话管理系统 SHALL 添加SameSite=Strict属性以防止跨站传输
5. WHEN 系统检测到会话的IP地址从原始IP变化, THE 会话管理系统 SHALL 记录严重性为WARNING的安全事件
6. WHEN 启用严格IP检查模式且系统检测到IP地址变化, THE 会话管理系统 SHALL 使会话失效并要求重新认证
7. WHEN 禁用严格IP检查模式且系统检测到IP地址变化, THE 会话管理系统 SHALL 更新会话的IP地址但保持会话有效
8. WHEN Redis中的会话数据损坏或无法反序列化, THE 会话管理系统 SHALL 删除损坏的会话并返回错误代码AUTH-SESSION-CORRUPTED
9. WHEN 在Redis中存储会话数据, THE 会话管理系统 SHALL 使用键格式"session:{sessionId}",TTL设置为会话的过期时间
10. WHEN 在Redis中存储用户到会话的映射, THE 会话管理系统 SHALL 使用键格式"user:sessions:{userId}",包含会话标识符集合

### 需求7: JWT令牌支持

**用户故事:** 作为开发人员,我希望会话管理系统支持基于JWT令牌的认证,以便我们可以为API客户端、移动应用和第三方集成实现无状态认证。

#### 验收标准

1. WHEN API客户端成功认证, THE 会话管理系统 SHALL 颁发有效期为15分钟(900秒)的访问令牌
2. WHEN API客户端成功认证, THE 会话管理系统 SHALL 颁发有效期为30天(2,592,000秒)的刷新令牌
3. WHEN 生成访问令牌, THE 会话管理系统 SHALL 包含声明:sessionId、userId、过期时间戳和颁发者
4. WHEN 生成刷新令牌, THE 会话管理系统 SHALL 包含声明:sessionId、userId、过期时间戳、类型"refresh"和颁发者
5. WHEN 访问令牌过期, THE 会话管理系统 SHALL 允许客户端通过POST /api/v1/auth/refresh提供有效的刷新令牌来获取新的访问令牌
6. WHEN 刷新访问令牌, THE 会话管理系统 SHALL 验证刷新令牌签名、过期时间以及未被列入黑名单
7. WHEN 刷新访问令牌, THE 会话管理系统 SHALL 验证关联的会话仍然存在且有效
8. WHEN 用户登出, THE 会话管理系统 SHALL 将刷新令牌添加到黑名单,使用键格式"token:blacklist:{tokenId}",TTL等于令牌的剩余有效期
9. WHEN 验证访问令牌, THE 会话管理系统 SHALL 使用配置的JWT密钥验证令牌签名
10. WHEN 验证访问令牌, THE 会话管理系统 SHALL 通过比较当前时间与exp声明来验证令牌未过期
11. WHEN 验证刷新令牌, THE 会话管理系统 SHALL 在接受令牌前检查令牌是否存在于黑名单中
12. WHEN 在黑名单中找到令牌, THE 会话管理系统 SHALL 拒绝该令牌并返回错误代码AUTH-TOKEN-BLACKLISTED

### 需求8: 存储和降级

**用户故事:** 作为系统运维人员,我希望会话管理系统自动处理Redis故障,以便即使主存储宕机时系统仍然可用。

#### 验收标准

1. WHEN Redis服务可用且健康, THE 会话管理系统 SHALL 在Redis中存储所有新会话
2. WHEN Redis服务不可用或返回连接错误, THE 会话管理系统 SHALL 在1秒内自动降级到MySQL存储
3. WHEN 使用Redis存储, THE 会话管理系统 SHALL 将TTL(生存时间)设置为会话的绝对超时以启用自动过期
4. WHEN 使用MySQL存储, THE 会话管理系统 SHALL 安排清理任务每小时运行一次并删除expiresAt < 当前时间的会话
5. WHEN Redis服务从故障中恢复, THE 会话管理系统 SHALL 自动恢复使用Redis作为主存储
6. WHEN 读取会话, THE 会话管理系统 SHALL 首先尝试从Redis读取,如果Redis不可用则从MySQL读取
7. WHEN 写入会话, THE 会话管理系统 SHALL 尝试同时写入Redis和MySQL以实现冗余
8. WHEN Redis写入失败但MySQL写入成功, THE 会话管理系统 SHALL 记录警告但继续操作
9. WHEN Redis和MySQL写入都失败, THE 会话管理系统 SHALL 返回错误代码SYS-STORAGE-UNAVAILABLE
10. WHEN MySQL清理任务运行, THE 会话管理系统 SHALL 删除过期会话并返回已删除会话的数量

### 需求9: 性能和容量

**用户故事:** 作为系统架构师,我希望会话管理系统满足严格的性能要求,以便会话操作不会成为应用程序的瓶颈。

#### 验收标准

1. WHEN 系统接收到1,000个并发会话验证请求, THE 会话管理系统 SHALL 在第95百分位的50毫秒内完成每个请求
2. WHEN 系统接收到100个并发会话创建请求, THE 会话管理系统 SHALL 在第95百分位的200毫秒内完成每个请求
3. WHEN 系统在Redis中存储了10,000个活跃会话, THE 会话管理系统 SHALL 继续处理新的会话请求而不降级
4. WHEN Redis内存使用超过配置的限制(默认50MB), THE 会话管理系统 SHALL 记录严重性为ERROR的告警
5. WHEN 单个会话数据大小超过5KB, THE 会话管理系统 SHALL 记录警告并拒绝会话创建
6. WHEN 系统处理会话操作, THE 会话管理系统 SHALL 在任何24小时期间保持99.9%的可用性
7. WHEN Redis不可用且系统降级到MySQL, THE 会话管理系统 SHALL 在第95百分位的200毫秒内完成会话验证
8. WHEN 将会话数据序列化为JSON以供存储, THE 会话管理系统 SHALL 在10毫秒内完成序列化
9. WHEN 从JSON反序列化会话数据, THE 会话管理系统 SHALL 在10毫秒内完成反序列化
10. WHEN 系统在Redis中存储10,000个会话, THE 会话管理系统 SHALL 消耗大约20MB的内存

### 需求10: 配置管理

**用户故事:** 作为系统运维人员,我希望会话超时时长和安全策略可配置,以便我们可以根据组织安全要求和合规需求调整设置而无需更改代码。

#### 验收标准

1. WHEN 系统启动, THE 会话管理系统 SHALL 从配置属性"aiops.session.timeout.absolute"读取绝对超时时长,默认值为28800秒
2. WHEN 系统启动, THE 会话管理系统 SHALL 从配置属性"aiops.session.timeout.idle"读取空闲超时时长,默认值为1800秒
3. WHEN 系统启动, THE 会话管理系统 SHALL 从配置属性"aiops.session.timeout.remember-me"读取"记住我"时长,默认值为2592000秒
4. WHEN 系统启动, THE 会话管理系统 SHALL 从配置属性"aiops.session.token.access-token-expiration"读取访问令牌过期时间,默认值为900秒
5. WHEN 系统启动, THE 会话管理系统 SHALL 从配置属性"aiops.session.token.refresh-token-expiration"读取刷新令牌过期时间,默认值为2592000秒
6. WHEN 系统启动, THE 会话管理系统 SHALL 从配置属性"aiops.session.device.max-devices-per-user"读取每用户最大设备数,默认值为5
7. WHEN 系统启动, THE 会话管理系统 SHALL 从配置属性"aiops.session.device.single-device-mode"读取单设备模式标志,默认值为false
8. WHEN 系统启动, THE 会话管理系统 SHALL 从配置属性"aiops.session.security.strict-ip-check"读取严格IP检查标志,默认值为false
9. WHEN 配置值无效(负数、零或非数字), THE 会话管理系统 SHALL 记录错误并使用安全的默认值
10. WHEN 配置属性缺失, THE 会话管理系统 SHALL 使用安全的默认值并记录信息消息

### 需求11: 审计和监控

**用户故事:** 作为安全审计员,我希望记录所有与会话相关的事件,以便我可以跟踪用户活动、调查安全事件并确保符合审计要求。

#### 验收标准

1. WHEN 创建会话, THE 会话管理系统 SHALL 记录级别为INFO的审计事件,包含userId、sessionId、deviceInfo、IP地址和时间戳
2. WHEN 因用户登出而销毁会话, THE 会话管理系统 SHALL 记录级别为INFO的审计事件,包含userId、sessionId、原因"USER_LOGOUT"和时间戳
3. WHEN 因超时而销毁会话, THE 会话管理系统 SHALL 记录级别为INFO的审计事件,包含userId、sessionId、原因"TIMEOUT"、超时类型(绝对/空闲)和时间戳
4. WHEN 检测到会话的IP地址变化, THE 会话管理系统 SHALL 记录级别为WARNING的安全事件,包含userId、sessionId、原始IP、新IP和时间戳
5. WHEN 会话验证因无效或过期会话而失败, THE 会话管理系统 SHALL 记录级别为WARNING的事件,包含尝试的sessionId、原因和时间戳
6. WHEN 令牌被添加到黑名单, THE 会话管理系统 SHALL 记录级别为INFO的审计事件,包含userId、tokenId、原因和时间戳
7. WHEN 会话验证响应时间超过50毫秒(P95), THE 会话管理系统 SHALL 记录级别为WARNING的性能事件,包含sessionId、响应时间和时间戳
8. WHEN Redis变得不可用且发生降级, THE 会话管理系统 SHALL 记录级别为ERROR的事件,包含错误详情和时间戳
9. WHEN 在配置中启用审计日志, THE 会话管理系统 SHALL 保留审计日志至少30天
10. WHEN 在配置中禁用审计日志, THE 会话管理系统 SHALL 仍然记录级别为WARNING或更高的安全事件(IP变化、验证失败)
