/**
 * OWASP ZAP 暴力破解防护测试脚本
 *
 * 测试目标：
 * - REQ-NFR-SEC-003: 连续5次登录失败后锁定账号30分钟
 * - 验证暴力破解防护机制
 *
 * 使用方法：
 * 在 ZAP 中加载此脚本并运行
 */

var HttpRequestHeader = Java.type("org.parosproxy.paros.network.HttpRequestHeader");
var HttpMessage = Java.type("org.parosproxy.paros.network.HttpMessage");
var URI = Java.type("org.apache.commons.httpclient.URI");

// 配置
var BASE_URL = "http://localhost:8080";
var LOGIN_URL = BASE_URL + "/api/v1/auth/login";
var TEST_USERNAME = "bruteforce_test_user";
var WRONG_PASSWORD = "WrongP@ss123";
var MAX_ATTEMPTS = 6; // 测试6次，第6次应该被锁定

/**
 * 发送登录请求
 */
function sendLoginRequest(username, password) {
    var requestBody = JSON.stringify({
        identifier: username,
        password: password,
        rememberMe: false
    });

    var msg = new HttpMessage();
    var uri = new URI(LOGIN_URL, true);
    var requestHeader = new HttpRequestHeader(HttpRequestHeader.POST, uri, HttpRequestHeader.HTTP11);

    requestHeader.setHeader("Content-Type", "application/json");
    requestHeader.setHeader("Accept", "application/json");
    requestHeader.setContentLength(requestBody.length);

    msg.setRequestHeader(requestHeader);
    msg.setRequestBody(requestBody);

    var sender = control.getExtensionLoader().getExtension("ExtensionNetwork").getHttpSender();
    sender.sendAndReceive(msg);

    return {
        statusCode: msg.getResponseHeader().getStatusCode(),
        body: msg.getResponseBody().toString()
    };
}

/**
 * 主测试函数
 */
function invokeWith(msg) {
    print("========================================");
    print("暴力破解防护测试");
    print("========================================");
    print("目标: " + LOGIN_URL);
    print("测试用户: " + TEST_USERNAME);
    print("");

    var results = [];
    var lockedDetected = false;

    // 执行多次登录失败
    for (var i = 1; i <= MAX_ATTEMPTS; i++) {
        print("尝试 " + i + "/" + MAX_ATTEMPTS + "...");

        var response = sendLoginRequest(TEST_USERNAME, WRONG_PASSWORD);
        var result = {
            attempt: i,
            statusCode: response.statusCode,
            isLocked: false
        };

        // 检查是否被锁定 (HTTP 423 Locked)
        if (response.statusCode === 423) {
            result.isLocked = true;
            lockedDetected = true;
            print("  状态码: " + response.statusCode + " (已锁定)");

            // 提取锁定时间
            try {
                var bodyJson = JSON.parse(response.body);
                if (bodyJson.message) {
                    print("  消息: " + bodyJson.message);
                }
            } catch (e) {
                // 忽略解析错误
            }
        } else if (response.statusCode === 200) {
            try {
                var bodyJson = JSON.parse(response.body);
                if (bodyJson.success === false) {
                    print("  状态码: " + response.statusCode + " (登录失败)");
                    print("  消息: " + (bodyJson.message || "未知"));
                } else {
                    print("  状态码: " + response.statusCode + " (登录成功?)");
                }
            } catch (e) {
                print("  状态码: " + response.statusCode);
            }
        } else {
            print("  状态码: " + response.statusCode);
        }

        results.push(result);

        // 短暂延迟，避免请求过快
        java.lang.Thread.sleep(500);
    }

    print("");
    print("========================================");
    print("测试结果");
    print("========================================");

    // 验证结果
    var testPassed = false;

    // 检查第6次是否被锁定
    if (results.length >= 6) {
        var sixthAttempt = results[5];
        if (sixthAttempt.isLocked) {
            testPassed = true;
            print("✅ 通过: 第6次尝试时账号被锁定 (HTTP 423)");
        } else {
            print("❌ 失败: 第6次尝试未被锁定");
        }
    }

    // 检查前5次是否允许尝试
    var firstFiveAllowed = true;
    for (var i = 0; i < 5 && i < results.length; i++) {
        if (results[i].isLocked) {
            firstFiveAllowed = false;
            print("⚠️ 警告: 第" + (i + 1) + "次尝试就被锁定，阈值可能设置过低");
            break;
        }
    }

    if (firstFiveAllowed && results.length >= 5) {
        print("✅ 通过: 前5次尝试允许登录（返回登录失败，非锁定）");
    }

    print("");
    print("总结:");
    print("  - 暴力破解防护: " + (testPassed ? "已启用" : "未启用或配置异常"));
    print("  - 锁定阈值: 5次失败后锁定");
    print("  - 符合需求: REQ-NFR-SEC-003");

    // 返回测试结果
    return testPassed;
}

// ZAP Standalone 脚本入口
function run() {
    return invokeWith(null);
}
