package com.catface996.aiops.domain.impl.service.subgraph;

import com.catface996.aiops.common.enums.SubgraphErrorCode;
import com.catface996.aiops.common.exception.BusinessException;
import com.catface996.aiops.domain.model.relationship.Relationship;
import com.catface996.aiops.domain.model.subgraph.PermissionRole;
import com.catface996.aiops.domain.model.subgraph.Subgraph;
import com.catface996.aiops.domain.model.subgraph.SubgraphPermission;
import com.catface996.aiops.domain.model.subgraph.SubgraphResource;
import com.catface996.aiops.domain.model.subgraph.SubgraphTopology;
import com.catface996.aiops.domain.service.relationship.RelationshipDomainService;
import com.catface996.aiops.repository.subgraph.SubgraphRepository;
import com.catface996.aiops.repository.subgraph.SubgraphResourceRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * 子图领域服务实现类单元测试
 *
 * <p>需求追溯：</p>
 * <ul>
 *   <li>F08: 子图管理功能</li>
 *   <li>任务6-12: 领域服务层测试</li>
 * </ul>
 *
 * @author AI Assistant
 * @since 2025-12-04
 */
@DisplayName("子图领域服务测试")
class SubgraphDomainServiceImplTest {

    private SubgraphDomainServiceImpl subgraphDomainService;
    private SubgraphRepository subgraphRepository;
    private SubgraphResourceRepository subgraphResourceRepository;
    private RelationshipDomainService relationshipDomainService;

    @BeforeEach
    void setUp() {
        subgraphRepository = mock(SubgraphRepository.class);
        subgraphResourceRepository = mock(SubgraphResourceRepository.class);
        relationshipDomainService = mock(RelationshipDomainService.class);

        subgraphDomainService = new SubgraphDomainServiceImpl(
                subgraphRepository,
                subgraphResourceRepository,
                relationshipDomainService
        );
    }

    // ==================== 任务6: 创建子图测试 ====================

    @Nested
    @DisplayName("创建子图测试")
    class CreateSubgraphTest {

        @Test
        @DisplayName("应该成功创建子图")
        void shouldCreateSubgraphSuccessfully() {
            // Given
            String name = "production-network";
            String description = "生产环境网络子图";
            List<String> tags = Arrays.asList("production", "network");
            Map<String, String> metadata = new HashMap<>();
            metadata.put("environment", "prod");
            Long creatorId = 100L;
            String creatorName = "admin";

            when(subgraphRepository.existsByName(name)).thenReturn(false);
            when(subgraphRepository.save(any(Subgraph.class))).thenAnswer(invocation -> {
                Subgraph s = invocation.getArgument(0);
                s.setId(1L);
                return s;
            });
            when(subgraphRepository.savePermission(any(SubgraphPermission.class))).thenAnswer(invocation -> {
                SubgraphPermission p = invocation.getArgument(0);
                p.setId(1L);
                return p;
            });

            // When
            Subgraph result = subgraphDomainService.createSubgraph(name, description, tags, metadata, creatorId, creatorName);

            // Then
            assertNotNull(result);
            assertEquals(1L, result.getId());
            assertEquals(name, result.getName());
            assertEquals(description, result.getDescription());
            assertEquals(tags, result.getTags());
            assertEquals(metadata, result.getMetadata());
            assertEquals(creatorId, result.getCreatedBy());

            // 验证自动创建Owner权限
            verify(subgraphRepository).savePermission(argThat(permission ->
                    permission.getSubgraphId().equals(1L) &&
                    permission.getUserId().equals(creatorId) &&
                    permission.getRole() == PermissionRole.OWNER
            ));
        }

        @Test
        @DisplayName("子图名称为空时应该抛出异常")
        void shouldThrowExceptionWhenNameIsEmpty() {
            assertThrows(IllegalArgumentException.class, () ->
                    subgraphDomainService.createSubgraph("", "desc", null, null, 100L, "admin"));
            assertThrows(IllegalArgumentException.class, () ->
                    subgraphDomainService.createSubgraph(null, "desc", null, null, 100L, "admin"));
        }

        @Test
        @DisplayName("创建者ID为空时应该抛出异常")
        void shouldThrowExceptionWhenCreatorIdIsNull() {
            assertThrows(IllegalArgumentException.class, () ->
                    subgraphDomainService.createSubgraph("test", "desc", null, null, null, "admin"));
        }

        @Test
        @DisplayName("子图名称重复时应该抛出业务异常")
        void shouldThrowExceptionWhenNameConflict() {
            // Given
            when(subgraphRepository.existsByName("existing-subgraph")).thenReturn(true);

            // When & Then
            BusinessException ex = assertThrows(BusinessException.class, () ->
                    subgraphDomainService.createSubgraph("existing-subgraph", "desc", null, null, 100L, "admin"));
            assertEquals(SubgraphErrorCode.SUBGRAPH_NAME_CONFLICT.getCode(), ex.getErrorCode());
        }

        @Test
        @DisplayName("创建子图时应该自动分配Owner权限")
        void shouldAutoAssignOwnerPermission() {
            // Given
            when(subgraphRepository.existsByName("new-subgraph")).thenReturn(false);
            when(subgraphRepository.save(any(Subgraph.class))).thenAnswer(invocation -> {
                Subgraph s = invocation.getArgument(0);
                s.setId(5L);
                return s;
            });
            when(subgraphRepository.savePermission(any(SubgraphPermission.class))).thenAnswer(invocation -> {
                SubgraphPermission p = invocation.getArgument(0);
                p.setId(1L);
                return p;
            });

            // When
            subgraphDomainService.createSubgraph("new-subgraph", "desc", null, null, 200L, "user");

            // Then
            verify(subgraphRepository).savePermission(argThat(permission ->
                    permission.getSubgraphId().equals(5L) &&
                    permission.getUserId().equals(200L) &&
                    permission.getRole() == PermissionRole.OWNER &&
                    permission.getGrantedBy().equals(200L)
            ));
        }
    }

    // ==================== 任务7: 查询子图测试 ====================

    @Nested
    @DisplayName("查询子图测试")
    class QuerySubgraphTest {

        @Test
        @DisplayName("应该成功获取用户有权限的子图列表")
        void shouldListSubgraphsForUser() {
            // Given
            Long userId = 100L;
            List<Subgraph> expectedSubgraphs = Arrays.asList(
                    createTestSubgraph(1L, "subgraph-1"),
                    createTestSubgraph(2L, "subgraph-2")
            );
            when(subgraphRepository.findByUserId(userId, 1, 20)).thenReturn(expectedSubgraphs);

            // When
            List<Subgraph> result = subgraphDomainService.listSubgraphs(userId, 1, 20);

            // Then
            assertEquals(2, result.size());
            assertEquals("subgraph-1", result.get(0).getName());
        }

        @Test
        @DisplayName("应该规范化分页参数")
        void shouldNormalizePaginationParams() {
            // Given
            Long userId = 100L;
            when(subgraphRepository.findByUserId(userId, 1, 20)).thenReturn(Collections.emptyList());

            // When
            subgraphDomainService.listSubgraphs(userId, -1, 0);

            // Then
            verify(subgraphRepository).findByUserId(userId, 1, 20);
        }

        @Test
        @DisplayName("应该限制最大页面大小为100")
        void shouldLimitMaxPageSize() {
            // Given
            Long userId = 100L;
            when(subgraphRepository.findByUserId(userId, 1, 100)).thenReturn(Collections.emptyList());

            // When
            subgraphDomainService.listSubgraphs(userId, 1, 200);

            // Then
            verify(subgraphRepository).findByUserId(userId, 1, 100);
        }

        @Test
        @DisplayName("应该成功搜索子图")
        void shouldSearchSubgraphs() {
            // Given
            String keyword = "production";
            Long userId = 100L;
            List<Subgraph> expectedSubgraphs = Collections.singletonList(
                    createTestSubgraph(1L, "production-network")
            );
            when(subgraphRepository.searchByKeyword(keyword, userId, 1, 20)).thenReturn(expectedSubgraphs);

            // When
            List<Subgraph> result = subgraphDomainService.searchSubgraphs(keyword, userId, 1, 20);

            // Then
            assertEquals(1, result.size());
            assertTrue(result.get(0).getName().contains("production"));
        }

        @Test
        @DisplayName("有权限时应该成功获取子图详情")
        void shouldGetSubgraphDetailWhenHasPermission() {
            // Given
            Long subgraphId = 1L;
            Long userId = 100L;
            Subgraph expectedSubgraph = createTestSubgraph(subgraphId, "test-subgraph");
            when(subgraphRepository.hasAnyPermission(subgraphId, userId)).thenReturn(true);
            when(subgraphRepository.findById(subgraphId)).thenReturn(Optional.of(expectedSubgraph));

            // When
            Optional<Subgraph> result = subgraphDomainService.getSubgraphDetail(subgraphId, userId);

            // Then
            assertTrue(result.isPresent());
            assertEquals("test-subgraph", result.get().getName());
        }

        @Test
        @DisplayName("无权限时应该抛出异常")
        void shouldThrowExceptionWhenNoPermission() {
            // Given
            Long subgraphId = 1L;
            Long userId = 100L;
            when(subgraphRepository.hasAnyPermission(subgraphId, userId)).thenReturn(false);

            // When & Then
            BusinessException ex = assertThrows(BusinessException.class, () ->
                    subgraphDomainService.getSubgraphDetail(subgraphId, userId));
            assertEquals(SubgraphErrorCode.SUBGRAPH_ACCESS_DENIED.getCode(), ex.getErrorCode());
        }
    }

    // ==================== 任务8: 更新子图测试 ====================

    @Nested
    @DisplayName("更新子图测试")
    class UpdateSubgraphTest {

        @Test
        @DisplayName("Owner应该成功更新子图")
        void shouldUpdateSubgraphWhenOwner() {
            // Given
            Long subgraphId = 1L;
            Long operatorId = 100L;
            Subgraph existingSubgraph = createTestSubgraph(subgraphId, "old-name");
            existingSubgraph.setVersion(1);

            when(subgraphRepository.findById(subgraphId)).thenReturn(Optional.of(existingSubgraph));
            when(subgraphRepository.hasPermission(subgraphId, operatorId, PermissionRole.OWNER)).thenReturn(true);
            when(subgraphRepository.existsByNameExcludeId("new-name", subgraphId)).thenReturn(false);
            when(subgraphRepository.update(any(Subgraph.class))).thenReturn(true);

            // When
            Subgraph result = subgraphDomainService.updateSubgraph(
                    subgraphId, "new-name", "new description", null, null, 1, operatorId, "admin"
            );

            // Then
            assertEquals("new-name", result.getName());
            assertEquals("new description", result.getDescription());
            verify(subgraphRepository).update(any(Subgraph.class));
        }

        @Test
        @DisplayName("非Owner更新子图应该抛出异常")
        void shouldThrowExceptionWhenNotOwner() {
            // Given
            Long subgraphId = 1L;
            Long operatorId = 200L;
            Subgraph existingSubgraph = createTestSubgraph(subgraphId, "test");

            when(subgraphRepository.findById(subgraphId)).thenReturn(Optional.of(existingSubgraph));
            when(subgraphRepository.hasPermission(subgraphId, operatorId, PermissionRole.OWNER)).thenReturn(false);

            // When & Then
            BusinessException ex = assertThrows(BusinessException.class, () ->
                    subgraphDomainService.updateSubgraph(subgraphId, "new-name", null, null, null, null, operatorId, "user"));
            assertEquals(SubgraphErrorCode.SUBGRAPH_EDIT_DENIED.getCode(), ex.getErrorCode());
        }

        @Test
        @DisplayName("版本冲突时应该抛出异常")
        void shouldThrowExceptionWhenVersionConflict() {
            // Given
            Long subgraphId = 1L;
            Long operatorId = 100L;
            Subgraph existingSubgraph = createTestSubgraph(subgraphId, "test");
            existingSubgraph.setVersion(2);

            when(subgraphRepository.findById(subgraphId)).thenReturn(Optional.of(existingSubgraph));
            when(subgraphRepository.hasPermission(subgraphId, operatorId, PermissionRole.OWNER)).thenReturn(true);

            // When & Then
            BusinessException ex = assertThrows(BusinessException.class, () ->
                    subgraphDomainService.updateSubgraph(subgraphId, "new-name", null, null, null, 1, operatorId, "admin"));
            assertEquals(SubgraphErrorCode.SUBGRAPH_VERSION_CONFLICT.getCode(), ex.getErrorCode());
        }

        @Test
        @DisplayName("更新名称时应该检查唯一性")
        void shouldCheckNameUniquenessWhenUpdating() {
            // Given
            Long subgraphId = 1L;
            Long operatorId = 100L;
            Subgraph existingSubgraph = createTestSubgraph(subgraphId, "old-name");

            when(subgraphRepository.findById(subgraphId)).thenReturn(Optional.of(existingSubgraph));
            when(subgraphRepository.hasPermission(subgraphId, operatorId, PermissionRole.OWNER)).thenReturn(true);
            when(subgraphRepository.existsByNameExcludeId("existing-name", subgraphId)).thenReturn(true);

            // When & Then
            BusinessException ex = assertThrows(BusinessException.class, () ->
                    subgraphDomainService.updateSubgraph(subgraphId, "existing-name", null, null, null, null, operatorId, "admin"));
            assertEquals(SubgraphErrorCode.SUBGRAPH_NAME_CONFLICT.getCode(), ex.getErrorCode());
        }
    }

    // ==================== 任务9: 删除子图测试 ====================

    @Nested
    @DisplayName("删除子图测试")
    class DeleteSubgraphTest {

        @Test
        @DisplayName("Owner应该成功删除空子图")
        void shouldDeleteEmptySubgraphWhenOwner() {
            // Given
            Long subgraphId = 1L;
            Long operatorId = 100L;
            Subgraph existingSubgraph = createTestSubgraph(subgraphId, "to-delete");

            when(subgraphRepository.findById(subgraphId)).thenReturn(Optional.of(existingSubgraph));
            when(subgraphRepository.hasPermission(subgraphId, operatorId, PermissionRole.OWNER)).thenReturn(true);
            when(subgraphResourceRepository.isSubgraphEmpty(subgraphId)).thenReturn(true);

            // When
            subgraphDomainService.deleteSubgraph(subgraphId, operatorId, "admin");

            // Then
            verify(subgraphRepository).delete(subgraphId);
        }

        @Test
        @DisplayName("非Owner删除子图应该抛出异常")
        void shouldThrowExceptionWhenNotOwnerDelete() {
            // Given
            Long subgraphId = 1L;
            Long operatorId = 200L;
            Subgraph existingSubgraph = createTestSubgraph(subgraphId, "test");

            when(subgraphRepository.findById(subgraphId)).thenReturn(Optional.of(existingSubgraph));
            when(subgraphRepository.hasPermission(subgraphId, operatorId, PermissionRole.OWNER)).thenReturn(false);

            // When & Then
            BusinessException ex = assertThrows(BusinessException.class, () ->
                    subgraphDomainService.deleteSubgraph(subgraphId, operatorId, "user"));
            assertEquals(SubgraphErrorCode.SUBGRAPH_DELETE_DENIED.getCode(), ex.getErrorCode());
        }

        @Test
        @DisplayName("子图非空时删除应该抛出异常")
        void shouldThrowExceptionWhenSubgraphNotEmpty() {
            // Given
            Long subgraphId = 1L;
            Long operatorId = 100L;
            Subgraph existingSubgraph = createTestSubgraph(subgraphId, "not-empty");

            when(subgraphRepository.findById(subgraphId)).thenReturn(Optional.of(existingSubgraph));
            when(subgraphRepository.hasPermission(subgraphId, operatorId, PermissionRole.OWNER)).thenReturn(true);
            when(subgraphResourceRepository.isSubgraphEmpty(subgraphId)).thenReturn(false);

            // When & Then
            BusinessException ex = assertThrows(BusinessException.class, () ->
                    subgraphDomainService.deleteSubgraph(subgraphId, operatorId, "admin"));
            assertEquals(SubgraphErrorCode.SUBGRAPH_NOT_EMPTY.getCode(), ex.getErrorCode());
        }

        @Test
        @DisplayName("删除不存在的子图应该抛出异常")
        void shouldThrowExceptionWhenSubgraphNotFound() {
            // Given
            Long subgraphId = 999L;
            when(subgraphRepository.findById(subgraphId)).thenReturn(Optional.empty());

            // When & Then
            BusinessException ex = assertThrows(BusinessException.class, () ->
                    subgraphDomainService.deleteSubgraph(subgraphId, 100L, "admin"));
            assertEquals(SubgraphErrorCode.SUBGRAPH_NOT_FOUND.getCode(), ex.getErrorCode());
        }
    }

    // ==================== 任务10: 权限管理测试 ====================

    @Nested
    @DisplayName("权限管理测试")
    class PermissionManagementTest {

        @Test
        @DisplayName("Owner应该成功添加权限")
        void shouldAddPermissionWhenOwner() {
            // Given
            Long subgraphId = 1L;
            Long userId = 200L;
            Long grantedBy = 100L;

            when(subgraphRepository.existsById(subgraphId)).thenReturn(true);
            when(subgraphRepository.hasPermission(subgraphId, grantedBy, PermissionRole.OWNER)).thenReturn(true);
            when(subgraphRepository.findPermissionBySubgraphIdAndUserId(subgraphId, userId)).thenReturn(Optional.empty());
            when(subgraphRepository.savePermission(any(SubgraphPermission.class))).thenAnswer(invocation -> {
                SubgraphPermission p = invocation.getArgument(0);
                p.setId(1L);
                return p;
            });

            // When
            subgraphDomainService.addPermission(subgraphId, userId, PermissionRole.VIEWER, grantedBy, "admin");

            // Then
            verify(subgraphRepository).savePermission(argThat(permission ->
                    permission.getSubgraphId().equals(subgraphId) &&
                    permission.getUserId().equals(userId) &&
                    permission.getRole() == PermissionRole.VIEWER
            ));
        }

        @Test
        @DisplayName("非Owner添加权限应该抛出异常")
        void shouldThrowExceptionWhenNonOwnerAddPermission() {
            // Given
            Long subgraphId = 1L;
            Long grantedBy = 200L;

            when(subgraphRepository.existsById(subgraphId)).thenReturn(true);
            when(subgraphRepository.hasPermission(subgraphId, grantedBy, PermissionRole.OWNER)).thenReturn(false);

            // When & Then
            BusinessException ex = assertThrows(BusinessException.class, () ->
                    subgraphDomainService.addPermission(subgraphId, 300L, PermissionRole.VIEWER, grantedBy, "user"));
            assertEquals(SubgraphErrorCode.SUBGRAPH_PERMISSION_DENIED.getCode(), ex.getErrorCode());
        }

        @Test
        @DisplayName("移除最后一个Owner应该抛出异常")
        void shouldThrowExceptionWhenRemovingLastOwner() {
            // Given
            Long subgraphId = 1L;
            Long userId = 100L;
            Long removedBy = 100L;
            SubgraphPermission ownerPermission = SubgraphPermission.createOwner(subgraphId, userId, userId);

            when(subgraphRepository.existsById(subgraphId)).thenReturn(true);
            when(subgraphRepository.hasPermission(subgraphId, removedBy, PermissionRole.OWNER)).thenReturn(true);
            when(subgraphRepository.findPermissionBySubgraphIdAndUserId(subgraphId, userId)).thenReturn(Optional.of(ownerPermission));
            when(subgraphRepository.countOwnersBySubgraphId(subgraphId)).thenReturn(1);

            // When & Then
            BusinessException ex = assertThrows(BusinessException.class, () ->
                    subgraphDomainService.removePermission(subgraphId, userId, removedBy, "admin"));
            assertEquals(SubgraphErrorCode.SUBGRAPH_LAST_OWNER.getCode(), ex.getErrorCode());
        }

        @Test
        @DisplayName("有多个Owner时应该成功移除Owner权限")
        void shouldRemoveOwnerWhenMultipleOwnersExist() {
            // Given
            Long subgraphId = 1L;
            Long userId = 200L;
            Long removedBy = 100L;
            SubgraphPermission ownerPermission = SubgraphPermission.createOwner(subgraphId, userId, 100L);

            when(subgraphRepository.existsById(subgraphId)).thenReturn(true);
            when(subgraphRepository.hasPermission(subgraphId, removedBy, PermissionRole.OWNER)).thenReturn(true);
            when(subgraphRepository.findPermissionBySubgraphIdAndUserId(subgraphId, userId)).thenReturn(Optional.of(ownerPermission));
            when(subgraphRepository.countOwnersBySubgraphId(subgraphId)).thenReturn(2);

            // When
            subgraphDomainService.removePermission(subgraphId, userId, removedBy, "admin");

            // Then
            verify(subgraphRepository).deletePermission(subgraphId, userId);
        }
    }

    // ==================== 任务11: 资源节点管理测试 ====================

    @Nested
    @DisplayName("资源节点管理测试")
    class ResourceManagementTest {

        @Test
        @DisplayName("Owner应该成功添加资源到子图")
        void shouldAddResourcesWhenOwner() {
            // Given
            Long subgraphId = 1L;
            Long operatorId = 100L;
            List<Long> resourceIds = Arrays.asList(10L, 20L, 30L);

            when(subgraphRepository.existsById(subgraphId)).thenReturn(true);
            when(subgraphRepository.hasPermission(subgraphId, operatorId, PermissionRole.OWNER)).thenReturn(true);
            when(subgraphResourceRepository.existsInSubgraph(eq(subgraphId), anyLong())).thenReturn(false);

            // When
            subgraphDomainService.addResources(subgraphId, resourceIds, operatorId, "admin");

            // Then
            verify(subgraphResourceRepository).addResources(argThat(resources ->
                    resources.size() == 3
            ));
        }

        @Test
        @DisplayName("添加资源时应该跳过已存在的")
        void shouldSkipExistingResources() {
            // Given
            Long subgraphId = 1L;
            Long operatorId = 100L;
            List<Long> resourceIds = Arrays.asList(10L, 20L, 30L);

            when(subgraphRepository.existsById(subgraphId)).thenReturn(true);
            when(subgraphRepository.hasPermission(subgraphId, operatorId, PermissionRole.OWNER)).thenReturn(true);
            when(subgraphResourceRepository.existsInSubgraph(subgraphId, 10L)).thenReturn(true);
            when(subgraphResourceRepository.existsInSubgraph(subgraphId, 20L)).thenReturn(false);
            when(subgraphResourceRepository.existsInSubgraph(subgraphId, 30L)).thenReturn(false);

            // When
            subgraphDomainService.addResources(subgraphId, resourceIds, operatorId, "admin");

            // Then
            verify(subgraphResourceRepository).addResources(argThat(resources ->
                    resources.size() == 2
            ));
        }

        @Test
        @DisplayName("非Owner添加资源应该抛出异常")
        void shouldThrowExceptionWhenNonOwnerAddResources() {
            // Given
            Long subgraphId = 1L;
            Long operatorId = 200L;

            when(subgraphRepository.existsById(subgraphId)).thenReturn(true);
            when(subgraphRepository.hasPermission(subgraphId, operatorId, PermissionRole.OWNER)).thenReturn(false);

            // When & Then
            BusinessException ex = assertThrows(BusinessException.class, () ->
                    subgraphDomainService.addResources(subgraphId, Arrays.asList(1L, 2L), operatorId, "user"));
            assertEquals(SubgraphErrorCode.SUBGRAPH_RESOURCE_DENIED.getCode(), ex.getErrorCode());
        }

        @Test
        @DisplayName("Owner应该成功从子图移除资源")
        void shouldRemoveResourcesWhenOwner() {
            // Given
            Long subgraphId = 1L;
            Long operatorId = 100L;
            List<Long> resourceIds = Arrays.asList(10L, 20L);

            when(subgraphRepository.existsById(subgraphId)).thenReturn(true);
            when(subgraphRepository.hasPermission(subgraphId, operatorId, PermissionRole.OWNER)).thenReturn(true);

            // When
            subgraphDomainService.removeResources(subgraphId, resourceIds, operatorId, "admin");

            // Then
            verify(subgraphResourceRepository).removeResources(subgraphId, resourceIds);
        }

        @Test
        @DisplayName("有权限时应该成功获取子图资源ID列表")
        void shouldGetResourceIdsWhenHasPermission() {
            // Given
            Long subgraphId = 1L;
            Long userId = 100L;
            List<Long> expectedResourceIds = Arrays.asList(10L, 20L, 30L);

            when(subgraphRepository.hasAnyPermission(subgraphId, userId)).thenReturn(true);
            when(subgraphResourceRepository.findResourceIdsBySubgraphId(subgraphId)).thenReturn(expectedResourceIds);

            // When
            List<Long> result = subgraphDomainService.getResourceIds(subgraphId, userId);

            // Then
            assertEquals(3, result.size());
            assertTrue(result.containsAll(expectedResourceIds));
        }

        @Test
        @DisplayName("无权限时获取资源列表应该抛出异常")
        void shouldThrowExceptionWhenNoPermissionToGetResources() {
            // Given
            Long subgraphId = 1L;
            Long userId = 200L;

            when(subgraphRepository.hasAnyPermission(subgraphId, userId)).thenReturn(false);

            // When & Then
            BusinessException ex = assertThrows(BusinessException.class, () ->
                    subgraphDomainService.getResourceIds(subgraphId, userId));
            assertEquals(SubgraphErrorCode.SUBGRAPH_ACCESS_DENIED.getCode(), ex.getErrorCode());
        }
    }

    // ==================== 任务12: 拓扑查询测试 ====================

    @Nested
    @DisplayName("拓扑查询测试")
    class TopologyQueryTest {

        @Test
        @DisplayName("有权限时应该成功获取子图拓扑")
        void shouldGetTopologyWhenHasPermission() {
            // Given
            Long subgraphId = 1L;
            Long userId = 100L;
            Subgraph subgraph = createTestSubgraph(subgraphId, "test-subgraph");
            List<Long> resourceIds = Arrays.asList(10L, 20L, 30L);

            Relationship rel1 = createTestRelationship(1L, 10L, 20L);
            Relationship rel2 = createTestRelationship(2L, 20L, 30L);
            Relationship relExternal = createTestRelationship(3L, 10L, 100L); // 外部关系

            when(subgraphRepository.findById(subgraphId)).thenReturn(Optional.of(subgraph));
            when(subgraphRepository.hasAnyPermission(subgraphId, userId)).thenReturn(true);
            when(subgraphResourceRepository.findResourceIdsBySubgraphId(subgraphId)).thenReturn(resourceIds);
            when(relationshipDomainService.getUpstreamDependencies(10L)).thenReturn(Collections.emptyList());
            when(relationshipDomainService.getDownstreamDependencies(10L)).thenReturn(Arrays.asList(rel1, relExternal));
            when(relationshipDomainService.getUpstreamDependencies(20L)).thenReturn(Collections.singletonList(rel1));
            when(relationshipDomainService.getDownstreamDependencies(20L)).thenReturn(Collections.singletonList(rel2));
            when(relationshipDomainService.getUpstreamDependencies(30L)).thenReturn(Collections.singletonList(rel2));
            when(relationshipDomainService.getDownstreamDependencies(30L)).thenReturn(Collections.emptyList());

            // When
            SubgraphTopology result = subgraphDomainService.getSubgraphTopology(subgraphId, userId);

            // Then
            assertNotNull(result);
            assertEquals(subgraphId, result.getSubgraphId());
            assertEquals("test-subgraph", result.getSubgraphName());
            assertEquals(3, result.getNodeCount());
            assertEquals(2, result.getEdgeCount()); // 只有内部关系，不包含外部关系
        }

        @Test
        @DisplayName("空子图应该返回空拓扑")
        void shouldReturnEmptyTopologyForEmptySubgraph() {
            // Given
            Long subgraphId = 1L;
            Long userId = 100L;
            Subgraph subgraph = createTestSubgraph(subgraphId, "empty-subgraph");

            when(subgraphRepository.findById(subgraphId)).thenReturn(Optional.of(subgraph));
            when(subgraphRepository.hasAnyPermission(subgraphId, userId)).thenReturn(true);
            when(subgraphResourceRepository.findResourceIdsBySubgraphId(subgraphId)).thenReturn(Collections.emptyList());

            // When
            SubgraphTopology result = subgraphDomainService.getSubgraphTopology(subgraphId, userId);

            // Then
            assertNotNull(result);
            assertEquals(0, result.getNodeCount());
            assertEquals(0, result.getEdgeCount());
        }

        @Test
        @DisplayName("无权限时应该抛出异常")
        void shouldThrowExceptionWhenNoPermissionForTopology() {
            // Given
            Long subgraphId = 1L;
            Long userId = 200L;
            Subgraph subgraph = createTestSubgraph(subgraphId, "test");

            when(subgraphRepository.findById(subgraphId)).thenReturn(Optional.of(subgraph));
            when(subgraphRepository.hasAnyPermission(subgraphId, userId)).thenReturn(false);

            // When & Then
            BusinessException ex = assertThrows(BusinessException.class, () ->
                    subgraphDomainService.getSubgraphTopology(subgraphId, userId));
            assertEquals(SubgraphErrorCode.SUBGRAPH_ACCESS_DENIED.getCode(), ex.getErrorCode());
        }

        @Test
        @DisplayName("子图不存在时应该抛出异常")
        void shouldThrowExceptionWhenSubgraphNotFoundForTopology() {
            // Given
            Long subgraphId = 999L;
            when(subgraphRepository.findById(subgraphId)).thenReturn(Optional.empty());

            // When & Then
            BusinessException ex = assertThrows(BusinessException.class, () ->
                    subgraphDomainService.getSubgraphTopology(subgraphId, 100L));
            assertEquals(SubgraphErrorCode.SUBGRAPH_NOT_FOUND.getCode(), ex.getErrorCode());
        }

        @Test
        @DisplayName("应该正确过滤外部关系")
        void shouldFilterExternalRelationships() {
            // Given
            Long subgraphId = 1L;
            Long userId = 100L;
            Subgraph subgraph = createTestSubgraph(subgraphId, "test");
            List<Long> resourceIds = Arrays.asList(10L, 20L);

            // 创建一个内部关系和多个外部关系
            Relationship internalRel = createTestRelationship(1L, 10L, 20L);
            Relationship externalRel1 = createTestRelationship(2L, 10L, 100L); // target外部
            Relationship externalRel2 = createTestRelationship(3L, 200L, 20L); // source外部

            when(subgraphRepository.findById(subgraphId)).thenReturn(Optional.of(subgraph));
            when(subgraphRepository.hasAnyPermission(subgraphId, userId)).thenReturn(true);
            when(subgraphResourceRepository.findResourceIdsBySubgraphId(subgraphId)).thenReturn(resourceIds);
            when(relationshipDomainService.getUpstreamDependencies(10L)).thenReturn(Collections.emptyList());
            when(relationshipDomainService.getDownstreamDependencies(10L)).thenReturn(Arrays.asList(internalRel, externalRel1));
            when(relationshipDomainService.getUpstreamDependencies(20L)).thenReturn(Arrays.asList(internalRel, externalRel2));
            when(relationshipDomainService.getDownstreamDependencies(20L)).thenReturn(Collections.emptyList());

            // When
            SubgraphTopology result = subgraphDomainService.getSubgraphTopology(subgraphId, userId);

            // Then
            assertEquals(2, result.getNodeCount());
            assertEquals(1, result.getEdgeCount()); // 只有internalRel
            assertTrue(result.getRelationships().stream()
                    .allMatch(r -> resourceIds.contains(r.getSourceResourceId()) &&
                                   resourceIds.contains(r.getTargetResourceId())));
        }
    }

    // ==================== 辅助方法 ====================

    private Subgraph createTestSubgraph(Long id, String name) {
        Subgraph subgraph = new Subgraph();
        subgraph.setId(id);
        subgraph.setName(name);
        subgraph.setDescription("Test description for " + name);
        subgraph.setTags(new ArrayList<>());
        subgraph.setMetadata(new HashMap<>());
        subgraph.setCreatedBy(100L);
        subgraph.setCreatedAt(LocalDateTime.now());
        subgraph.setUpdatedAt(LocalDateTime.now());
        subgraph.setVersion(0);
        return subgraph;
    }

    private Relationship createTestRelationship(Long id, Long sourceId, Long targetId) {
        Relationship relationship = new Relationship();
        relationship.setId(id);
        relationship.setSourceResourceId(sourceId);
        relationship.setTargetResourceId(targetId);
        return relationship;
    }
}
