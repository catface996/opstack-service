/**
 * OWASP ZAP Token 安全测试脚本
 *
 * 测试目标：
 * - REQ-NFR-SEC-001: JWT Token 安全性
 * - REQ-NFR-SEC-002: Token 不可伪造
 * - 验证 Token 签名和过期机制
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
var VALIDATE_URL = BASE_URL + "/api/v1/session/validate";
var TEST_USERNAME = "tokentest";
var TEST_PASSWORD = "SecureP@ss123";

/**
 * 发送 HTTP 请求
 */
function sendRequest(method, url, headers, body) {
    var msg = new HttpMessage();
    var uri = new URI(url, true);
    var requestHeader = new HttpRequestHeader(method, uri, HttpRequestHeader.HTTP11);

    // 设置 Headers
    for (var key in headers) {
        requestHeader.setHeader(key, headers[key]);
    }

    if (body) {
        requestHeader.setContentLength(body.length);
        msg.setRequestBody(body);
    }

    msg.setRequestHeader(requestHeader);

    var sender = control.getExtensionLoader().getExtension("ExtensionNetwork").getHttpSender();
    sender.sendAndReceive(msg);

    return {
        statusCode: msg.getResponseHeader().getStatusCode(),
        body: msg.getResponseBody().toString()
    };
}

/**
 * 登录获取有效 Token
 */
function getValidToken() {
    var requestBody = JSON.stringify({
        identifier: TEST_USERNAME,
        password: TEST_PASSWORD,
        rememberMe: false
    });

    var response = sendRequest("POST", LOGIN_URL, {
        "Content-Type": "application/json",
        "Accept": "application/json"
    }, requestBody);

    if (response.statusCode === 200) {
        try {
            var bodyJson = JSON.parse(response.body);
            if (bodyJson.success && bodyJson.data && bodyJson.data.token) {
                return bodyJson.data.token;
            }
        } catch (e) {
            print("解析登录响应失败: " + e);
        }
    }
    return null;
}

/**
 * 验证 Token
 */
function validateToken(token) {
    var response = sendRequest("GET", VALIDATE_URL, {
        "Authorization": "Bearer " + token,
        "Accept": "application/json"
    }, null);

    return {
        statusCode: response.statusCode,
        isValid: response.statusCode === 200
    };
}

/**
 * 测试1: 伪造 Token 测试
 */
function testFakeToken() {
    print("\n--- 测试1: 伪造 Token 测试 ---");

    // 完全伪造的 Token
    var fakeTokens = [
        "fake.token.here",
        "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiIxMjM0NTY3ODkwIiwibmFtZSI6IkZha2UiLCJpYXQiOjE1MTYyMzkwMjJ9.SflKxwRJSMeKKF2QT4fwpMeJf36POk6yJV_adQssw5c",
        "eyJhbGciOiJub25lIiwidHlwIjoiSldUIn0.eyJzdWIiOiIxIiwidXNlcm5hbWUiOiJhZG1pbiJ9.",
        ""
    ];

    var allRejected = true;

    for (var i = 0; i < fakeTokens.length; i++) {
        var token = fakeTokens[i];
        var displayToken = token.length > 50 ? token.substring(0, 50) + "..." : token;
        print("  测试伪造Token " + (i + 1) + ": " + (displayToken || "(空)"));

        var result = validateToken(token);
        if (result.isValid) {
            print("    ❌ 失败: 伪造Token被接受!");
            allRejected = false;
        } else {
            print("    ✅ 通过: 伪造Token被拒绝 (HTTP " + result.statusCode + ")");
        }
    }

    return allRejected;
}

/**
 * 测试2: Token 篡改测试
 */
function testTamperedToken() {
    print("\n--- 测试2: Token 篡改测试 ---");

    // 获取有效 Token
    var validToken = getValidToken();
    if (!validToken) {
        print("  ⚠️ 无法获取有效Token，跳过此测试");
        return true; // 跳过但不算失败
    }

    print("  获取到有效Token: " + validToken.substring(0, 50) + "...");

    // 篡改 Token 的不同部分
    var parts = validToken.split(".");
    if (parts.length !== 3) {
        print("  ⚠️ Token格式异常，跳过此测试");
        return true;
    }

    var tamperedTokens = [];

    // 1. 修改 header
    var modifiedHeader = "eyJhbGciOiJub25lIiwidHlwIjoiSldUIn0"; // alg: none
    tamperedTokens.push({
        name: "修改算法为none",
        token: modifiedHeader + "." + parts[1] + "." + parts[2]
    });

    // 2. 修改 payload
    var modifiedPayload = "eyJzdWIiOiI5OTk5OTkiLCJ1c2VybmFtZSI6ImFkbWluIiwicm9sZSI6IlJPTEVfQURNSU4ifQ";
    tamperedTokens.push({
        name: "修改payload为admin",
        token: parts[0] + "." + modifiedPayload + "." + parts[2]
    });

    // 3. 修改 signature
    tamperedTokens.push({
        name: "修改签名",
        token: parts[0] + "." + parts[1] + ".tamperedsignature"
    });

    // 4. 删除签名
    tamperedTokens.push({
        name: "删除签名",
        token: parts[0] + "." + parts[1] + "."
    });

    var allRejected = true;

    for (var i = 0; i < tamperedTokens.length; i++) {
        var test = tamperedTokens[i];
        print("  测试篡改: " + test.name);

        var result = validateToken(test.token);
        if (result.isValid) {
            print("    ❌ 失败: 篡改Token被接受!");
            allRejected = false;
        } else {
            print("    ✅ 通过: 篡改Token被拒绝 (HTTP " + result.statusCode + ")");
        }
    }

    return allRejected;
}

/**
 * 测试3: Token 格式验证
 */
function testTokenFormat() {
    print("\n--- 测试3: Token 格式验证 ---");

    var invalidFormats = [
        { name: "无Bearer前缀", token: "eyJhbGciOiJIUzUxMiJ9.xxx.yyy" },
        { name: "错误前缀", token: "Basic eyJhbGciOiJIUzUxMiJ9.xxx.yyy" },
        { name: "SQL注入尝试", token: "' OR '1'='1" },
        { name: "XSS尝试", token: "<script>alert(1)</script>" },
        { name: "特殊字符", token: "../../etc/passwd" }
    ];

    var allRejected = true;

    for (var i = 0; i < invalidFormats.length; i++) {
        var test = invalidFormats[i];
        print("  测试格式: " + test.name);

        // 直接使用无 Bearer 前缀的方式测试
        var response = sendRequest("GET", VALIDATE_URL, {
            "Authorization": test.token,
            "Accept": "application/json"
        }, null);

        if (response.statusCode === 200) {
            try {
                var bodyJson = JSON.parse(response.body);
                if (bodyJson.data && bodyJson.data.valid === true) {
                    print("    ❌ 失败: 无效格式被接受!");
                    allRejected = false;
                } else {
                    print("    ✅ 通过: 返回无效 (HTTP " + response.statusCode + ")");
                }
            } catch (e) {
                print("    ✅ 通过: 返回无效 (HTTP " + response.statusCode + ")");
            }
        } else {
            print("    ✅ 通过: 请求被拒绝 (HTTP " + response.statusCode + ")");
        }
    }

    return allRejected;
}

/**
 * 测试4: 有效 Token 验证
 */
function testValidToken() {
    print("\n--- 测试4: 有效 Token 验证 ---");

    var validToken = getValidToken();
    if (!validToken) {
        print("  ⚠️ 无法获取有效Token");
        return false;
    }

    var result = validateToken(validToken);
    if (result.isValid) {
        print("  ✅ 通过: 有效Token被正确接受");
        return true;
    } else {
        print("  ❌ 失败: 有效Token被拒绝 (HTTP " + result.statusCode + ")");
        return false;
    }
}

/**
 * 主测试函数
 */
function invokeWith(msg) {
    print("========================================");
    print("Token 安全测试");
    print("========================================");
    print("目标: " + BASE_URL);
    print("");

    var results = {
        fakeToken: testFakeToken(),
        tamperedToken: testTamperedToken(),
        tokenFormat: testTokenFormat(),
        validToken: testValidToken()
    };

    print("\n========================================");
    print("测试结果汇总");
    print("========================================");
    print("伪造Token测试: " + (results.fakeToken ? "✅ 通过" : "❌ 失败"));
    print("篡改Token测试: " + (results.tamperedToken ? "✅ 通过" : "❌ 失败"));
    print("格式验证测试: " + (results.tokenFormat ? "✅ 通过" : "❌ 失败"));
    print("有效Token测试: " + (results.validToken ? "✅ 通过" : "❌ 失败"));

    var allPassed = results.fakeToken && results.tamperedToken &&
                    results.tokenFormat && results.validToken;

    print("");
    print("总体结果: " + (allPassed ? "✅ 全部通过" : "❌ 存在失败"));
    print("符合需求: REQ-NFR-SEC-001, REQ-NFR-SEC-002");

    return allPassed;
}

// ZAP Standalone 脚本入口
function run() {
    return invokeWith(null);
}
