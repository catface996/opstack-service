package aiops.performance

import io.gatling.core.Predef._
import io.gatling.http.Predef._
import scala.concurrent.duration._

/**
 * AIOps 认证服务性能测试
 *
 * 使用 Gatling 进行性能测试，测试场景包括：
 * 1. 用户注册性能测试
 * 2. 用户登录性能测试（含BCrypt验证）
 * 3. 会话验证性能测试
 * 4. 混合负载测试
 *
 * 性能指标要求：
 * - 登录响应时间 P95 < 2秒
 * - BCrypt 单次验证 < 500ms
 * - 系统支持 1000 并发用户
 *
 * 运行命令：
 * mvn gatling:test -Dgatling.simulationClass=aiops.performance.AuthSimulation
 *
 * @author AI Assistant
 * @since 2025-11-26
 */
class AuthSimulation extends Simulation {

  // ==================== 配置参数 ====================
  val baseUrl = System.getProperty("baseUrl", "http://localhost:8080")
  val users = System.getProperty("users", "100").toInt
  val duration = System.getProperty("duration", "60").toInt
  val rampUp = System.getProperty("rampUp", "30").toInt

  // HTTP 协议配置
  val httpProtocol = http
    .baseUrl(baseUrl)
    .acceptHeader("application/json")
    .contentTypeHeader("application/json")
    .acceptEncodingHeader("gzip, deflate")
    .userAgentHeader("Gatling/3.9.5")

  // ==================== 数据生成器 ====================
  // 用户注册数据生成器
  val registerFeeder = Iterator.continually(Map(
    "username" -> s"gatling_${System.currentTimeMillis()}_${scala.util.Random.nextInt(100000)}",
    "email" -> s"gatling_${System.currentTimeMillis()}_${scala.util.Random.nextInt(100000)}@test.com",
    "password" -> "SecureP@ss123"
  ))

  // 登录用户数据（需要预先创建这些用户）
  val loginFeeder = csv("users.csv").circular

  // ==================== HTTP 请求定义 ====================

  // 用户注册请求
  val register = exec(
    http("用户注册")
      .post("/api/v1/auth/register")
      .body(StringBody(
        """{
          "username": "${username}",
          "email": "${email}",
          "password": "${password}"
        }"""
      ))
      .check(status.is(201))
      .check(jsonPath("$.success").is("true"))
      .check(jsonPath("$.data.accountId").saveAs("accountId"))
  )

  // 用户登录请求
  val login = exec(
    http("用户登录")
      .post("/api/v1/auth/login")
      .body(StringBody(
        """{
          "identifier": "${username}",
          "password": "${password}",
          "rememberMe": false
        }"""
      ))
      .check(status.is(200))
      .check(jsonPath("$.success").is("true"))
      .check(jsonPath("$.data.token").saveAs("token"))
      .check(responseTimeInMillis.lte(2000))  // P95 < 2秒
  )

  // 会话验证请求
  val validateSession = exec(
    http("会话验证")
      .get("/api/v1/session/validate")
      .header("Authorization", "Bearer ${token}")
      .check(status.is(200))
      .check(jsonPath("$.data.valid").is("true"))
      .check(responseTimeInMillis.lte(500))  // 会话验证 < 500ms
  )

  // 用户登出请求
  val logout = exec(
    http("用户登出")
      .post("/api/v1/auth/logout")
      .header("Authorization", "Bearer ${token}")
      .check(status.is(200))
  )

  // 强制登出其他设备
  val forceLogoutOthers = exec(
    http("强制登出其他设备")
      .post("/api/v1/session/force-logout-others")
      .header("Authorization", "Bearer ${token}")
      .body(StringBody(
        """{
          "token": "Bearer ${token}",
          "password": "${password}"
        }"""
      ))
      .check(status.is(200))
      .check(jsonPath("$.data.token").saveAs("newToken"))
  )

  // ==================== 测试场景定义 ====================

  /**
   * 场景1：用户注册性能测试
   * - 100 并发用户
   * - 30秒内逐步增加
   * - 每用户执行10次注册
   */
  val registerScenario = scenario("场景1-用户注册性能测试")
    .feed(registerFeeder)
    .repeat(10) {
      exec(register)
        .pause(100.milliseconds, 500.milliseconds)
    }

  /**
   * 场景2：用户登录性能测试（BCrypt验证）
   * - 200 并发用户
   * - 60秒内逐步增加
   * - 每用户执行50次登录
   * - 验证BCrypt性能 < 500ms
   */
  val loginScenario = scenario("场景2-用户登录性能测试")
    .feed(loginFeeder)
    .repeat(50) {
      exec(login)
        .pause(200.milliseconds, 1.second)
    }

  /**
   * 场景3：会话验证性能测试
   * - 500 并发用户
   * - 60秒内逐步增加
   * - 每用户验证100次
   * - 验证响应时间 < 500ms
   */
  val sessionValidationScenario = scenario("场景3-会话验证性能测试")
    .feed(loginFeeder)
    .exec(login)  // 先登录获取Token
    .pause(500.milliseconds)
    .repeat(100) {
      exec(validateSession)
        .pause(50.milliseconds, 200.milliseconds)
    }

  /**
   * 场景4：完整业务流程测试
   * - 注册 -> 登录 -> 验证 -> 登出
   * - 模拟真实用户行为
   */
  val fullFlowScenario = scenario("场景4-完整业务流程测试")
    .feed(registerFeeder)
    .exec(register)
    .pause(1.second, 2.seconds)
    .exec(
      http("流程-登录")
        .post("/api/v1/auth/login")
        .body(StringBody(
          """{
            "identifier": "${username}",
            "password": "${password}",
            "rememberMe": false
          }"""
        ))
        .check(status.is(200))
        .check(jsonPath("$.data.token").saveAs("token"))
    )
    .pause(500.milliseconds, 1.second)
    .repeat(5) {
      exec(validateSession)
        .pause(1.second, 3.seconds)
    }
    .exec(logout)

  /**
   * 场景5：峰值负载测试
   * - 1000 并发用户
   * - 测试系统极限
   */
  val peakLoadScenario = scenario("场景5-峰值负载测试")
    .feed(loginFeeder)
    .exec(login)
    .pause(200.milliseconds)
    .repeat(20) {
      exec(validateSession)
        .pause(100.milliseconds, 300.milliseconds)
    }

  /**
   * 场景6：持续负载测试
   * - 恒定负载持续运行
   * - 用于稳定性测试
   */
  val sustainedLoadScenario = scenario("场景6-持续负载测试")
    .feed(loginFeeder)
    .forever {
      exec(login)
        .pause(1.second)
        .repeat(10) {
          exec(validateSession)
            .pause(500.milliseconds)
        }
        .exec(logout)
        .pause(2.seconds)
    }

  // ==================== 测试执行配置 ====================

  setUp(
    // 场景1：注册测试
    registerScenario.inject(
      rampUsers(100).during(30.seconds)
    ),

    // 场景2：登录测试（BCrypt性能）
    loginScenario.inject(
      rampUsers(200).during(60.seconds)
    ),

    // 场景3：会话验证测试
    sessionValidationScenario.inject(
      rampUsers(500).during(60.seconds)
    ),

    // 场景4：完整流程测试
    fullFlowScenario.inject(
      rampUsers(100).during(30.seconds)
    )

    // 场景5：峰值负载（可选，取消注释启用）
    // peakLoadScenario.inject(
    //   rampUsers(1000).during(120.seconds)
    // )

    // 场景6：持续负载（可选，取消注释启用）
    // sustainedLoadScenario.inject(
    //   constantConcurrentUsers(100).during(10.minutes)
    // )
  )
    .protocols(httpProtocol)
    .assertions(
      // 全局断言
      global.responseTime.percentile(95).lt(2000),  // P95 < 2秒
      global.successfulRequests.percent.gt(95),     // 成功率 > 95%
      global.requestsPerSec.gt(100),                // TPS > 100

      // 登录接口断言（BCrypt性能）
      details("用户登录").responseTime.percentile(95).lt(2000),
      details("用户登录").responseTime.mean.lt(500),

      // 会话验证断言
      details("会话验证").responseTime.percentile(95).lt(500),
      details("会话验证").successfulRequests.percent.gt(99)
    )
}

/**
 * BCrypt 专项性能测试
 *
 * 专门测试 BCrypt 密码验证性能
 * 要求单次验证 < 500ms
 */
class BCryptPerformanceSimulation extends Simulation {

  val baseUrl = System.getProperty("baseUrl", "http://localhost:8080")

  val httpProtocol = http
    .baseUrl(baseUrl)
    .acceptHeader("application/json")
    .contentTypeHeader("application/json")

  // 使用固定用户测试BCrypt性能
  val bcryptTestScenario = scenario("BCrypt密码验证性能测试")
    .exec(
      http("BCrypt验证")
        .post("/api/v1/auth/login")
        .body(StringBody(
          """{
            "identifier": "bcrypttest",
            "password": "SecureP@ss123",
            "rememberMe": false
          }"""
        ))
        .check(status.in(200, 401))  // 200成功或401密码错误都算完成
        .check(responseTimeInMillis.lte(500))  // BCrypt < 500ms
    )
    .pause(100.milliseconds)

  setUp(
    bcryptTestScenario.inject(
      rampUsers(50).during(10.seconds),
      constantUsersPerSec(10).during(60.seconds)
    )
  )
    .protocols(httpProtocol)
    .assertions(
      global.responseTime.percentile(99).lt(500),  // P99 < 500ms
      global.responseTime.mean.lt(200),            // 平均 < 200ms
      global.failedRequests.count.lt(10)           // 失败请求 < 10
    )
}

/**
 * 并发用户峰值测试
 *
 * 测试系统支持 1000 并发用户
 */
class ConcurrentUsersSimulation extends Simulation {

  val baseUrl = System.getProperty("baseUrl", "http://localhost:8080")

  val httpProtocol = http
    .baseUrl(baseUrl)
    .acceptHeader("application/json")
    .contentTypeHeader("application/json")

  val loginFeeder = csv("users.csv").circular

  val concurrentScenario = scenario("1000并发用户测试")
    .feed(loginFeeder)
    .exec(
      http("并发登录")
        .post("/api/v1/auth/login")
        .body(StringBody(
          """{
            "identifier": "${username}",
            "password": "${password}",
            "rememberMe": false
          }"""
        ))
        .check(status.is(200))
        .check(jsonPath("$.data.token").saveAs("token"))
    )
    .pause(500.milliseconds)
    .repeat(10) {
      exec(
        http("并发会话验证")
          .get("/api/v1/session/validate")
          .header("Authorization", "Bearer ${token}")
          .check(status.is(200))
      )
        .pause(200.milliseconds, 500.milliseconds)
    }

  setUp(
    concurrentScenario.inject(
      // 逐步增加到1000并发
      incrementConcurrentUsers(100)
        .times(10)
        .eachLevelLasting(30.seconds)
        .separatedByRampsLasting(10.seconds)
        .startingFrom(100)
    )
  )
    .protocols(httpProtocol)
    .assertions(
      global.responseTime.percentile(95).lt(3000),  // 高并发下 P95 < 3秒
      global.successfulRequests.percent.gt(90),     // 成功率 > 90%
      global.requestsPerSec.gt(500)                 // TPS > 500
    )
}
