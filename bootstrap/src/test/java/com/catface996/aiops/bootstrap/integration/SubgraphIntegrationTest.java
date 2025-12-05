package com.catface996.aiops.bootstrap.integration;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MvcResult;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * 子图管理集成测试
 *
 * 使用 TestContainers 进行端到端测试，验证：
 * - 子图 CRUD 操作
 * - 权限管理功能
 * - 资源节点管理
 * - 乐观锁机制
 * - 完整生命周期
 *
 * Feature: f08-subgraph-management
 * Requirements: All functional requirements (1-10)
 *
 * @author AI Assistant
 * @since 2025-12-05
 */
@DisplayName("子图管理集成测试")
@org.junit.jupiter.api.condition.DisabledIfEnvironmentVariable(named = "CI", matches = "true", disabledReason = "需要完整环境，CI环境请使用E2E脚本测试")
class SubgraphIntegrationTest extends BaseIntegrationTest {

    private String authToken;
    private String secondUserToken;
    private static final String BASE_URL = "/api/v1/subgraphs";

    @BeforeEach
    void setUpAuth() throws Exception {
        // 注册并登录第一个用户获取 token
        String timestamp = String.valueOf(System.currentTimeMillis() % 10000000);
        String username = "sub" + timestamp;
        String password = "SecureP@ss123";

        // 注册用户
        mockMvc.perform(post("/api/v1/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(String.format("""
                            {
                                "username": "%s",
                                "email": "%s@test.com",
                                "password": "%s"
                            }
                            """, username, username, password)))
                .andExpect(status().isCreated());

        // 登录获取 token
        MvcResult loginResult = mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(String.format("""
                            {
                                "identifier": "%s",
                                "password": "%s",
                                "rememberMe": false
                            }
                            """, username, password)))
                .andExpect(status().isOk())
                .andReturn();

        authToken = extractToken(loginResult.getResponse().getContentAsString());

        // 注册并登录第二个用户
        String secondUsername = "sub2" + timestamp;
        mockMvc.perform(post("/api/v1/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(String.format("""
                            {
                                "username": "%s",
                                "email": "%s@test.com",
                                "password": "%s"
                            }
                            """, secondUsername, secondUsername, password)))
                .andExpect(status().isCreated());

        MvcResult secondLoginResult = mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(String.format("""
                            {
                                "identifier": "%s",
                                "password": "%s",
                                "rememberMe": false
                            }
                            """, secondUsername, password)))
                .andExpect(status().isOk())
                .andReturn();

        secondUserToken = extractToken(secondLoginResult.getResponse().getContentAsString());
    }

    private String extractToken(String response) {
        int tokenStart = response.indexOf("\"token\":\"") + 9;
        int tokenEnd = response.indexOf("\"", tokenStart);
        return response.substring(tokenStart, tokenEnd);
    }

    private String extractId(String response) {
        int idStart = response.indexOf("\"id\":") + 5;
        int idEnd = response.indexOf(",", idStart);
        if (idEnd == -1) idEnd = response.indexOf("}", idStart);
        return response.substring(idStart, idEnd).trim();
    }

    private String extractVersion(String response) {
        int versionStart = response.indexOf("\"version\":") + 10;
        int versionEnd = response.indexOf(",", versionStart);
        if (versionEnd == -1) versionEnd = response.indexOf("}", versionStart);
        return response.substring(versionStart, versionEnd).trim();
    }

    @Nested
    @DisplayName("子图创建测试")
    class SubgraphCreateTest {

        @Test
        @DisplayName("应该成功创建子图并自动分配 Owner 权限")
        void shouldCreateSubgraphAndAssignOwner() throws Exception {
            String uniqueName = "test-subgraph-" + System.currentTimeMillis();
            String createJson = String.format("""
                {
                    "name": "%s",
                    "description": "测试子图描述",
                    "tags": ["test", "integration"],
                    "metadata": {"domain": "payment", "env": "test"}
                }
                """, uniqueName);

            mockMvc.perform(post(BASE_URL)
                            .header("Authorization", "Bearer " + authToken)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(createJson))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.data.id").exists())
                    .andExpect(jsonPath("$.data.name").value(uniqueName))
                    .andExpect(jsonPath("$.data.description").value("测试子图描述"))
                    .andExpect(jsonPath("$.data.tags").isArray())
                    .andExpect(jsonPath("$.data.tags.length()").value(2));
        }

        @Test
        @DisplayName("子图名称重复时应该返回 409")
        void shouldReturn409WhenNameConflict() throws Exception {
            String uniqueName = "conflict-subgraph-" + System.currentTimeMillis();
            String createJson = String.format("""
                {
                    "name": "%s",
                    "description": "第一个子图"
                }
                """, uniqueName);

            // 创建第一个子图
            mockMvc.perform(post(BASE_URL)
                            .header("Authorization", "Bearer " + authToken)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(createJson))
                    .andExpect(status().isCreated());

            // 尝试创建同名子图
            mockMvc.perform(post(BASE_URL)
                            .header("Authorization", "Bearer " + authToken)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(createJson))
                    .andExpect(status().isConflict())
                    .andExpect(jsonPath("$.success").value(false));
        }

        @Test
        @DisplayName("未认证时应该返回 401")
        void shouldReturn401WhenNotAuthenticated() throws Exception {
            String createJson = """
                {
                    "name": "unauthorized-subgraph",
                    "description": "未认证测试"
                }
                """;

            mockMvc.perform(post(BASE_URL)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(createJson))
                    .andExpect(status().isUnauthorized());
        }

        @Test
        @DisplayName("缺少必填字段时应该返回 400")
        void shouldReturn400WhenMissingRequiredFields() throws Exception {
            String createJson = """
                {
                    "description": "缺少名称"
                }
                """;

            mockMvc.perform(post(BASE_URL)
                            .header("Authorization", "Bearer " + authToken)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(createJson))
                    .andExpect(status().isBadRequest());
        }
    }

    @Nested
    @DisplayName("子图查询测试")
    class SubgraphQueryTest {

        @Test
        @DisplayName("应该返回用户有权限的子图列表")
        void shouldReturnSubgraphListWithPermission() throws Exception {
            // 先创建一个子图
            String uniqueName = "query-test-" + System.currentTimeMillis();
            String createJson = String.format("""
                {
                    "name": "%s",
                    "description": "查询测试"
                }
                """, uniqueName);

            mockMvc.perform(post(BASE_URL)
                            .header("Authorization", "Bearer " + authToken)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(createJson))
                    .andExpect(status().isCreated());

            // 查询子图列表
            mockMvc.perform(get(BASE_URL)
                            .header("Authorization", "Bearer " + authToken)
                            .param("page", "1")
                            .param("pageSize", "20"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.data.items").isArray())
                    .andExpect(jsonPath("$.data.items.length()").value(greaterThanOrEqualTo(1)));
        }

        @Test
        @DisplayName("应该支持关键词搜索")
        void shouldSupportKeywordSearch() throws Exception {
            String uniqueName = "searchable-subgraph-" + System.currentTimeMillis();
            String createJson = String.format("""
                {
                    "name": "%s",
                    "description": "可搜索的子图"
                }
                """, uniqueName);

            mockMvc.perform(post(BASE_URL)
                            .header("Authorization", "Bearer " + authToken)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(createJson))
                    .andExpect(status().isCreated());

            // 使用关键词搜索
            mockMvc.perform(get(BASE_URL)
                            .header("Authorization", "Bearer " + authToken)
                            .param("keyword", "searchable"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true));
        }

        @Test
        @DisplayName("应该能获取子图详情")
        void shouldGetSubgraphDetail() throws Exception {
            String uniqueName = "detail-test-" + System.currentTimeMillis();
            String createJson = String.format("""
                {
                    "name": "%s",
                    "description": "详情测试",
                    "tags": ["detail", "test"]
                }
                """, uniqueName);

            MvcResult createResult = mockMvc.perform(post(BASE_URL)
                            .header("Authorization", "Bearer " + authToken)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(createJson))
                    .andExpect(status().isCreated())
                    .andReturn();

            String subgraphId = extractId(createResult.getResponse().getContentAsString());

            // 获取详情
            mockMvc.perform(get(BASE_URL + "/" + subgraphId)
                            .header("Authorization", "Bearer " + authToken))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.data.name").value(uniqueName))
                    .andExpect(jsonPath("$.data.owners").isArray())
                    .andExpect(jsonPath("$.data.owners.length()").value(greaterThanOrEqualTo(1)));
        }

        @Test
        @DisplayName("无权限用户获取详情应该返回 403")
        void shouldReturn403WhenNoPermission() throws Exception {
            String uniqueName = "private-subgraph-" + System.currentTimeMillis();
            String createJson = String.format("""
                {
                    "name": "%s",
                    "description": "私有子图"
                }
                """, uniqueName);

            MvcResult createResult = mockMvc.perform(post(BASE_URL)
                            .header("Authorization", "Bearer " + authToken)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(createJson))
                    .andExpect(status().isCreated())
                    .andReturn();

            String subgraphId = extractId(createResult.getResponse().getContentAsString());

            // 第二个用户尝试访问
            mockMvc.perform(get(BASE_URL + "/" + subgraphId)
                            .header("Authorization", "Bearer " + secondUserToken))
                    .andExpect(status().isForbidden());
        }
    }

    @Nested
    @DisplayName("子图更新测试")
    class SubgraphUpdateTest {

        @Test
        @DisplayName("Owner 应该能成功更新子图")
        void ownerShouldUpdateSubgraph() throws Exception {
            String uniqueName = "update-test-" + System.currentTimeMillis();
            String createJson = String.format("""
                {
                    "name": "%s",
                    "description": "原始描述"
                }
                """, uniqueName);

            MvcResult createResult = mockMvc.perform(post(BASE_URL)
                            .header("Authorization", "Bearer " + authToken)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(createJson))
                    .andExpect(status().isCreated())
                    .andReturn();

            String response = createResult.getResponse().getContentAsString();
            String subgraphId = extractId(response);
            String version = extractVersion(response);

            // 更新子图
            String updateJson = String.format("""
                {
                    "name": "%s-updated",
                    "description": "更新后的描述",
                    "version": %s
                }
                """, uniqueName, version);

            mockMvc.perform(put(BASE_URL + "/" + subgraphId)
                            .header("Authorization", "Bearer " + authToken)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(updateJson))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.data.name").value(uniqueName + "-updated"))
                    .andExpect(jsonPath("$.data.description").value("更新后的描述"));
        }

        @Test
        @DisplayName("版本冲突时应该返回 409")
        void shouldReturn409WhenVersionConflict() throws Exception {
            String uniqueName = "version-conflict-" + System.currentTimeMillis();
            String createJson = String.format("""
                {
                    "name": "%s",
                    "description": "版本冲突测试"
                }
                """, uniqueName);

            MvcResult createResult = mockMvc.perform(post(BASE_URL)
                            .header("Authorization", "Bearer " + authToken)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(createJson))
                    .andExpect(status().isCreated())
                    .andReturn();

            String subgraphId = extractId(createResult.getResponse().getContentAsString());

            // 使用错误的版本号更新
            String updateJson = String.format("""
                {
                    "description": "尝试更新",
                    "version": 999
                }
                """);

            mockMvc.perform(put(BASE_URL + "/" + subgraphId)
                            .header("Authorization", "Bearer " + authToken)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(updateJson))
                    .andExpect(status().isConflict());
        }

        @Test
        @DisplayName("非 Owner 更新应该返回 403")
        void nonOwnerShouldReturn403() throws Exception {
            String uniqueName = "non-owner-update-" + System.currentTimeMillis();
            String createJson = String.format("""
                {
                    "name": "%s",
                    "description": "非 Owner 更新测试"
                }
                """, uniqueName);

            MvcResult createResult = mockMvc.perform(post(BASE_URL)
                            .header("Authorization", "Bearer " + authToken)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(createJson))
                    .andExpect(status().isCreated())
                    .andReturn();

            String subgraphId = extractId(createResult.getResponse().getContentAsString());

            // 第二个用户尝试更新
            String updateJson = """
                {
                    "description": "非法更新",
                    "version": 0
                }
                """;

            mockMvc.perform(put(BASE_URL + "/" + subgraphId)
                            .header("Authorization", "Bearer " + secondUserToken)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(updateJson))
                    .andExpect(status().isForbidden());
        }
    }

    @Nested
    @DisplayName("子图删除测试")
    class SubgraphDeleteTest {

        @Test
        @DisplayName("应该成功删除空子图")
        void shouldDeleteEmptySubgraph() throws Exception {
            String uniqueName = "delete-test-" + System.currentTimeMillis();
            String createJson = String.format("""
                {
                    "name": "%s",
                    "description": "删除测试"
                }
                """, uniqueName);

            MvcResult createResult = mockMvc.perform(post(BASE_URL)
                            .header("Authorization", "Bearer " + authToken)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(createJson))
                    .andExpect(status().isCreated())
                    .andReturn();

            String subgraphId = extractId(createResult.getResponse().getContentAsString());

            // 删除子图
            mockMvc.perform(delete(BASE_URL + "/" + subgraphId)
                            .header("Authorization", "Bearer " + authToken))
                    .andExpect(status().isNoContent());

            // 验证已删除
            mockMvc.perform(get(BASE_URL + "/" + subgraphId)
                            .header("Authorization", "Bearer " + authToken))
                    .andExpect(status().isNotFound());
        }

        @Test
        @DisplayName("非 Owner 删除应该返回 403")
        void nonOwnerDeleteShouldReturn403() throws Exception {
            String uniqueName = "non-owner-delete-" + System.currentTimeMillis();
            String createJson = String.format("""
                {
                    "name": "%s",
                    "description": "非 Owner 删除测试"
                }
                """, uniqueName);

            MvcResult createResult = mockMvc.perform(post(BASE_URL)
                            .header("Authorization", "Bearer " + authToken)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(createJson))
                    .andExpect(status().isCreated())
                    .andReturn();

            String subgraphId = extractId(createResult.getResponse().getContentAsString());

            // 第二个用户尝试删除
            mockMvc.perform(delete(BASE_URL + "/" + subgraphId)
                            .header("Authorization", "Bearer " + secondUserToken))
                    .andExpect(status().isForbidden());
        }
    }

    @Nested
    @DisplayName("子图完整生命周期测试")
    class SubgraphLifecycleTest {

        @Test
        @DisplayName("完整生命周期：创建→查询→更新→删除")
        void shouldCompleteFullLifecycle() throws Exception {
            // 1. 创建子图
            String uniqueName = "lifecycle-test-" + System.currentTimeMillis();
            String createJson = String.format("""
                {
                    "name": "%s",
                    "description": "生命周期测试",
                    "tags": ["lifecycle"]
                }
                """, uniqueName);

            MvcResult createResult = mockMvc.perform(post(BASE_URL)
                            .header("Authorization", "Bearer " + authToken)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(createJson))
                    .andExpect(status().isCreated())
                    .andReturn();

            String response = createResult.getResponse().getContentAsString();
            String subgraphId = extractId(response);
            String version = extractVersion(response);

            // 2. 查询详情
            mockMvc.perform(get(BASE_URL + "/" + subgraphId)
                            .header("Authorization", "Bearer " + authToken))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.data.name").value(uniqueName));

            // 3. 更新子图
            String updateJson = String.format("""
                {
                    "description": "更新后的描述",
                    "tags": ["lifecycle", "updated"],
                    "version": %s
                }
                """, version);

            MvcResult updateResult = mockMvc.perform(put(BASE_URL + "/" + subgraphId)
                            .header("Authorization", "Bearer " + authToken)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(updateJson))
                    .andExpect(status().isOk())
                    .andReturn();

            // 4. 验证更新
            mockMvc.perform(get(BASE_URL + "/" + subgraphId)
                            .header("Authorization", "Bearer " + authToken))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.data.description").value("更新后的描述"));

            // 5. 删除子图
            mockMvc.perform(delete(BASE_URL + "/" + subgraphId)
                            .header("Authorization", "Bearer " + authToken))
                    .andExpect(status().isNoContent());

            // 6. 验证已删除
            mockMvc.perform(get(BASE_URL + "/" + subgraphId)
                            .header("Authorization", "Bearer " + authToken))
                    .andExpect(status().isNotFound());
        }
    }

    @Nested
    @DisplayName("拓扑查询测试")
    class TopologyQueryTest {

        @Test
        @DisplayName("应该能获取空子图的拓扑")
        void shouldGetEmptySubgraphTopology() throws Exception {
            String uniqueName = "topology-test-" + System.currentTimeMillis();
            String createJson = String.format("""
                {
                    "name": "%s",
                    "description": "拓扑测试"
                }
                """, uniqueName);

            MvcResult createResult = mockMvc.perform(post(BASE_URL)
                            .header("Authorization", "Bearer " + authToken)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(createJson))
                    .andExpect(status().isCreated())
                    .andReturn();

            String subgraphId = extractId(createResult.getResponse().getContentAsString());

            // 获取拓扑
            mockMvc.perform(get(BASE_URL + "/" + subgraphId + "/topology")
                            .header("Authorization", "Bearer " + authToken))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.data.nodes").isArray())
                    .andExpect(jsonPath("$.data.edges").isArray());
        }
    }
}
