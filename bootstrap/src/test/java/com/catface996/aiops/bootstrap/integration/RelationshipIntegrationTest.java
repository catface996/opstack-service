package com.catface996.aiops.bootstrap.integration;

import com.catface996.aiops.application.api.dto.relationship.request.CreateRelationshipRequest;
import com.catface996.aiops.application.api.dto.relationship.request.UpdateRelationshipRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.web.servlet.MvcResult;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * 资源关系管理集成测试
 *
 * @author AI Assistant
 * @since 2025-12-03
 */
@DisplayName("资源关系管理集成测试")
class RelationshipIntegrationTest extends BaseIntegrationTest {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    private String authToken;
    private Long testResourceId1;
    private Long testResourceId2;
    private Long testResourceId3;

    @BeforeEach
    void setUpTestData() throws Exception {
        // 1. 注册测试用户
        String uniqueSuffix = String.valueOf(System.currentTimeMillis());
        String registerJson = String.format("""
            {
                "username": "reltest%s",
                "email": "reltest%s@test.com",
                "password": "SecureP@ss123"
            }
            """, uniqueSuffix, uniqueSuffix);

        mockMvc.perform(post("/api/v1/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(registerJson))
                .andExpect(status().isCreated());

        // 2. 登录获取 token
        String loginJson = String.format("""
            {
                "identifier": "reltest%s",
                "password": "SecureP@ss123",
                "rememberMe": false
            }
            """, uniqueSuffix);

        MvcResult loginResult = mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(loginJson))
                .andExpect(status().isOk())
                .andReturn();

        String responseBody = loginResult.getResponse().getContentAsString();
        authToken = objectMapper.readTree(responseBody).get("data").get("token").asText();

        // 3. 创建测试资源
        testResourceId1 = createTestResource("order-service-" + uniqueSuffix, 2L); // APPLICATION
        testResourceId2 = createTestResource("order-db-" + uniqueSuffix, 3L); // DATABASE
        testResourceId3 = createTestResource("user-service-" + uniqueSuffix, 2L); // APPLICATION
    }

    private Long createTestResource(String name, Long typeId) throws Exception {
        String json = String.format("""
            {
                "name": "%s",
                "resourceTypeId": %d,
                "description": "Test resource",
                "status": "RUNNING"
            }
            """, name, typeId);

        MvcResult result = mockMvc.perform(post("/api/v1/resources")
                        .header("Authorization", "Bearer " + authToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isCreated())
                .andReturn();

        String responseBody = result.getResponse().getContentAsString();
        return objectMapper.readTree(responseBody).get("data").get("id").asLong();
    }

    @Nested
    @DisplayName("创建关系接口测试")
    class CreateRelationshipApiTest {

        @Test
        @DisplayName("应该成功创建资源关系")
        void shouldCreateRelationshipSuccessfully() throws Exception {
            CreateRelationshipRequest request = new CreateRelationshipRequest();
            request.setSourceResourceId(testResourceId1);
            request.setTargetResourceId(testResourceId2);
            request.setRelationshipType("DEPENDENCY");
            request.setDirection("UNIDIRECTIONAL");
            request.setStrength("STRONG");
            request.setDescription("订单服务依赖订单数据库");

            mockMvc.perform(post("/api/v1/relationships")
                            .header("Authorization", "Bearer " + authToken)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(toJson(request)))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.data.sourceResourceId").value(testResourceId1))
                    .andExpect(jsonPath("$.data.targetResourceId").value(testResourceId2))
                    .andExpect(jsonPath("$.data.relationshipType").value("DEPENDENCY"))
                    .andExpect(jsonPath("$.data.strength").value("STRONG"))
                    .andExpect(jsonPath("$.data.status").value("NORMAL"));
        }

        @Test
        @DisplayName("无效的关系类型应该返回错误")
        void shouldReturnErrorForInvalidType() throws Exception {
            String json = String.format("""
                {
                    "sourceResourceId": %d,
                    "targetResourceId": %d,
                    "relationshipType": "INVALID_TYPE",
                    "direction": "UNIDIRECTIONAL",
                    "strength": "STRONG"
                }
                """, testResourceId1, testResourceId2);

            mockMvc.perform(post("/api/v1/relationships")
                            .header("Authorization", "Bearer " + authToken)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(json))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(false));
        }
    }

    @Nested
    @DisplayName("查询关系接口测试")
    class QueryRelationshipApiTest {

        @Test
        @DisplayName("应该成功查询关系列表")
        void shouldListRelationships() throws Exception {
            // 先创建一个关系
            createTestRelationship(testResourceId1, testResourceId2);

            mockMvc.perform(get("/api/v1/relationships")
                            .header("Authorization", "Bearer " + authToken))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.data.content").isArray());
        }

        @Test
        @DisplayName("应该成功查询资源的所有关系")
        void shouldGetResourceRelationships() throws Exception {
            // 创建关系
            createTestRelationship(testResourceId1, testResourceId2);
            createTestRelationship(testResourceId1, testResourceId3);

            mockMvc.perform(get("/api/v1/relationships/resource/" + testResourceId1)
                            .header("Authorization", "Bearer " + authToken))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.data.resourceId").value(testResourceId1))
                    .andExpect(jsonPath("$.data.downstreamDependencies").isArray())
                    .andExpect(jsonPath("$.data.downstreamCount").value(greaterThanOrEqualTo(2)));
        }
    }

    @Nested
    @DisplayName("循环依赖检测测试")
    class CycleDetectionApiTest {

        @Test
        @DisplayName("无循环时应该返回 hasCycle=false")
        void shouldDetectNoCycle() throws Exception {
            // 创建线性依赖: A -> B
            createTestRelationship(testResourceId1, testResourceId2);

            mockMvc.perform(get("/api/v1/relationships/resource/" + testResourceId1 + "/cycle-detection")
                            .header("Authorization", "Bearer " + authToken))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.data.hasCycle").value(false))
                    .andExpect(jsonPath("$.data.cyclePath").isEmpty());
        }

        @Test
        @DisplayName("有循环时应该返回 hasCycle=true")
        void shouldDetectCycle() throws Exception {
            // 创建循环: A -> B -> C -> A
            createTestRelationship(testResourceId1, testResourceId2);
            createTestRelationship(testResourceId2, testResourceId3);
            createTestRelationship(testResourceId3, testResourceId1);

            mockMvc.perform(get("/api/v1/relationships/resource/" + testResourceId1 + "/cycle-detection")
                            .header("Authorization", "Bearer " + authToken))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.data.hasCycle").value(true))
                    .andExpect(jsonPath("$.data.cyclePath").isNotEmpty());
        }
    }

    @Nested
    @DisplayName("图遍历测试")
    class TraverseApiTest {

        @Test
        @DisplayName("应该正确遍历关系图")
        void shouldTraverseGraph() throws Exception {
            // 创建依赖: A -> B, A -> C
            createTestRelationship(testResourceId1, testResourceId2);
            createTestRelationship(testResourceId1, testResourceId3);

            mockMvc.perform(get("/api/v1/relationships/resource/" + testResourceId1 + "/traverse")
                            .param("maxDepth", "5")
                            .header("Authorization", "Bearer " + authToken))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.data.startResourceId").value(testResourceId1))
                    .andExpect(jsonPath("$.data.totalNodes").value(greaterThanOrEqualTo(3)));
        }
    }

    @Nested
    @DisplayName("更新和删除关系测试")
    class UpdateDeleteApiTest {

        @Test
        @DisplayName("应该成功更新关系")
        void shouldUpdateRelationship() throws Exception {
            // 创建关系
            Long relationshipId = createTestRelationship(testResourceId1, testResourceId2);

            UpdateRelationshipRequest updateRequest = new UpdateRelationshipRequest();
            updateRequest.setStrength("WEAK");
            updateRequest.setDescription("更新后的描述");

            mockMvc.perform(put("/api/v1/relationships/" + relationshipId)
                            .header("Authorization", "Bearer " + authToken)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(toJson(updateRequest)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.data.strength").value("WEAK"))
                    .andExpect(jsonPath("$.data.description").value("更新后的描述"));
        }

        @Test
        @DisplayName("应该成功删除关系")
        void shouldDeleteRelationship() throws Exception {
            // 创建关系
            Long relationshipId = createTestRelationship(testResourceId1, testResourceId2);

            // 删除关系
            mockMvc.perform(delete("/api/v1/relationships/" + relationshipId)
                            .header("Authorization", "Bearer " + authToken))
                    .andExpect(status().isNoContent());

            // 验证已删除
            mockMvc.perform(get("/api/v1/relationships/" + relationshipId)
                            .header("Authorization", "Bearer " + authToken))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(false));
        }
    }

    private Long createTestRelationship(Long sourceId, Long targetId) throws Exception {
        String json = String.format("""
            {
                "sourceResourceId": %d,
                "targetResourceId": %d,
                "relationshipType": "DEPENDENCY",
                "direction": "UNIDIRECTIONAL",
                "strength": "STRONG",
                "description": "Test relationship"
            }
            """, sourceId, targetId);

        MvcResult result = mockMvc.perform(post("/api/v1/relationships")
                        .header("Authorization", "Bearer " + authToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isCreated())
                .andReturn();

        String responseBody = result.getResponse().getContentAsString();
        return objectMapper.readTree(responseBody).get("data").get("id").asLong();
    }
}
