package com.catface996.aiops.repository.mysql.impl.subgraph;

import com.catface996.aiops.domain.model.subgraph.PermissionRole;
import com.catface996.aiops.domain.model.subgraph.Subgraph;
import com.catface996.aiops.domain.model.subgraph.SubgraphPermission;
import com.catface996.aiops.repository.mysql.mapper.subgraph.SubgraphMapper;
import com.catface996.aiops.repository.mysql.mapper.subgraph.SubgraphPermissionMapper;
import com.catface996.aiops.repository.mysql.po.subgraph.SubgraphPO;
import com.catface996.aiops.repository.mysql.po.subgraph.SubgraphPermissionPO;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * SubgraphRepositoryImpl 单元测试
 *
 * <p>需求追溯：</p>
 * <ul>
 *   <li>F08: 子图管理功能</li>
 *   <li>任务4: 实现 SubgraphRepository（包含权限操作）</li>
 * </ul>
 *
 * @author AI Assistant
 * @since 2025-12-04
 */
@ExtendWith(MockitoExtension.class)
class SubgraphRepositoryImplTest {

    @Mock
    private SubgraphMapper subgraphMapper;

    @Mock
    private SubgraphPermissionMapper permissionMapper;

    @Spy
    private ObjectMapper objectMapper = new ObjectMapper();

    @InjectMocks
    private SubgraphRepositoryImpl repository;

    private SubgraphPO testSubgraphPO;
    private Subgraph testSubgraph;
    private SubgraphPermissionPO testPermissionPO;
    private SubgraphPermission testPermission;

    @BeforeEach
    void setUp() {
        // 准备子图测试数据
        testSubgraphPO = new SubgraphPO();
        testSubgraphPO.setId(1L);
        testSubgraphPO.setName("test-subgraph");
        testSubgraphPO.setDescription("Test subgraph description");
        testSubgraphPO.setTags("[\"tag1\", \"tag2\"]");
        testSubgraphPO.setMetadata("{\"env\": \"prod\", \"team\": \"devops\"}");
        testSubgraphPO.setCreatedBy(100L);
        testSubgraphPO.setCreatedAt(LocalDateTime.now());
        testSubgraphPO.setUpdatedAt(LocalDateTime.now());
        testSubgraphPO.setVersion(0);

        testSubgraph = new Subgraph();
        testSubgraph.setId(1L);
        testSubgraph.setName("test-subgraph");
        testSubgraph.setDescription("Test subgraph description");
        testSubgraph.setTags(Arrays.asList("tag1", "tag2"));
        testSubgraph.setMetadata(Map.of("env", "prod", "team", "devops"));
        testSubgraph.setCreatedBy(100L);
        testSubgraph.setCreatedAt(LocalDateTime.now());
        testSubgraph.setUpdatedAt(LocalDateTime.now());
        testSubgraph.setVersion(0);

        // 准备权限测试数据
        testPermissionPO = new SubgraphPermissionPO();
        testPermissionPO.setId(1L);
        testPermissionPO.setSubgraphId(1L);
        testPermissionPO.setUserId(100L);
        testPermissionPO.setRole("OWNER");
        testPermissionPO.setGrantedAt(LocalDateTime.now());
        testPermissionPO.setGrantedBy(100L);

        testPermission = new SubgraphPermission();
        testPermission.setId(1L);
        testPermission.setSubgraphId(1L);
        testPermission.setUserId(100L);
        testPermission.setRole(PermissionRole.OWNER);
        testPermission.setGrantedAt(LocalDateTime.now());
        testPermission.setGrantedBy(100L);
    }

    // ==================== 子图 CRUD 测试 ====================

    @Nested
    @DisplayName("子图保存测试")
    class SaveTests {

        @Test
        @DisplayName("保存新子图成功")
        void save_Success() {
            // Given
            when(subgraphMapper.insert(any(SubgraphPO.class))).thenAnswer(invocation -> {
                SubgraphPO po = invocation.getArgument(0);
                po.setId(1L);
                return 1;
            });
            when(subgraphMapper.selectById(1L)).thenReturn(testSubgraphPO);

            // When
            Subgraph result = repository.save(testSubgraph);

            // Then
            assertNotNull(result);
            assertEquals("test-subgraph", result.getName());
            verify(subgraphMapper, times(1)).insert(any(SubgraphPO.class));
        }

        @Test
        @DisplayName("保存空子图抛出异常")
        void save_NullSubgraph_ThrowsException() {
            assertThrows(IllegalArgumentException.class, () -> repository.save(null));
            verify(subgraphMapper, never()).insert(any(SubgraphPO.class));
        }
    }

    @Nested
    @DisplayName("子图查询测试")
    class FindTests {

        @Test
        @DisplayName("根据ID查询子图成功")
        void findById_Success() {
            // Given
            when(subgraphMapper.selectById(1L)).thenReturn(testSubgraphPO);

            // When
            Optional<Subgraph> result = repository.findById(1L);

            // Then
            assertTrue(result.isPresent());
            assertEquals("test-subgraph", result.get().getName());
            assertEquals(2, result.get().getTags().size());
            verify(subgraphMapper, times(1)).selectById(1L);
        }

        @Test
        @DisplayName("根据ID查询不存在的子图")
        void findById_NotFound() {
            // Given
            when(subgraphMapper.selectById(999L)).thenReturn(null);

            // When
            Optional<Subgraph> result = repository.findById(999L);

            // Then
            assertFalse(result.isPresent());
        }

        @Test
        @DisplayName("根据名称查询子图成功")
        void findByName_Success() {
            // Given
            when(subgraphMapper.selectByName("test-subgraph")).thenReturn(testSubgraphPO);

            // When
            Optional<Subgraph> result = repository.findByName("test-subgraph");

            // Then
            assertTrue(result.isPresent());
            assertEquals(1L, result.get().getId());
        }

        @Test
        @DisplayName("查询用户有权限访问的子图列表")
        void findByUserId_Success() {
            // Given
            when(subgraphMapper.selectByUserId(eq(100L), anyInt(), anyInt()))
                    .thenReturn(Arrays.asList(testSubgraphPO));

            // When
            List<Subgraph> result = repository.findByUserId(100L, 1, 10);

            // Then
            assertEquals(1, result.size());
            assertEquals("test-subgraph", result.get(0).getName());
        }

        @Test
        @DisplayName("按关键词搜索子图")
        void searchByKeyword_Success() {
            // Given
            when(subgraphMapper.searchByKeyword(eq("test"), eq(100L), anyInt(), anyInt()))
                    .thenReturn(Arrays.asList(testSubgraphPO));

            // When
            List<Subgraph> result = repository.searchByKeyword("test", 100L, 1, 10);

            // Then
            assertEquals(1, result.size());
        }

        @Test
        @DisplayName("按标签过滤子图")
        void filterByTags_Success() {
            // Given
            List<String> tags = Arrays.asList("tag1");
            when(subgraphMapper.filterByTags(eq(tags), eq(100L), anyInt(), anyInt()))
                    .thenReturn(Arrays.asList(testSubgraphPO));

            // When
            List<Subgraph> result = repository.filterByTags(tags, 100L, 1, 10);

            // Then
            assertEquals(1, result.size());
        }

        @Test
        @DisplayName("按所有者过滤子图")
        void filterByOwner_Success() {
            // Given
            when(subgraphMapper.filterByOwner(eq(100L), eq(100L), anyInt(), anyInt()))
                    .thenReturn(Arrays.asList(testSubgraphPO));

            // When
            List<Subgraph> result = repository.filterByOwner(100L, 100L, 1, 10);

            // Then
            assertEquals(1, result.size());
        }
    }

    @Nested
    @DisplayName("子图更新测试")
    class UpdateTests {

        @Test
        @DisplayName("更新子图成功（乐观锁）")
        void update_Success() {
            // Given
            when(subgraphMapper.updateWithVersion(any(SubgraphPO.class))).thenReturn(1);

            // When
            boolean result = repository.update(testSubgraph);

            // Then
            assertTrue(result);
            verify(subgraphMapper, times(1)).updateWithVersion(any(SubgraphPO.class));
        }

        @Test
        @DisplayName("更新子图版本冲突")
        void update_VersionConflict() {
            // Given
            when(subgraphMapper.updateWithVersion(any(SubgraphPO.class))).thenReturn(0);

            // When
            boolean result = repository.update(testSubgraph);

            // Then
            assertFalse(result);
        }

        @Test
        @DisplayName("更新空子图抛出异常")
        void update_NullSubgraph_ThrowsException() {
            assertThrows(IllegalArgumentException.class, () -> repository.update(null));
        }

        @Test
        @DisplayName("更新无ID子图抛出异常")
        void update_NullId_ThrowsException() {
            testSubgraph.setId(null);
            assertThrows(IllegalArgumentException.class, () -> repository.update(testSubgraph));
        }
    }

    @Nested
    @DisplayName("子图删除测试")
    class DeleteTests {

        @Test
        @DisplayName("删除子图成功")
        void delete_Success() {
            // When
            repository.delete(1L);

            // Then
            verify(subgraphMapper, times(1)).deleteById(1L);
        }

        @Test
        @DisplayName("删除空ID抛出异常")
        void delete_NullId_ThrowsException() {
            assertThrows(IllegalArgumentException.class, () -> repository.delete(null));
        }
    }

    @Nested
    @DisplayName("子图存在性检查测试")
    class ExistsTests {

        @Test
        @DisplayName("检查子图ID存在")
        void existsById_True() {
            // Given
            when(subgraphMapper.selectById(1L)).thenReturn(testSubgraphPO);

            // Then
            assertTrue(repository.existsById(1L));
        }

        @Test
        @DisplayName("检查子图ID不存在")
        void existsById_False() {
            // Given
            when(subgraphMapper.selectById(999L)).thenReturn(null);

            // Then
            assertFalse(repository.existsById(999L));
        }

        @Test
        @DisplayName("检查子图名称存在")
        void existsByName_True() {
            // Given
            when(subgraphMapper.selectByName("test-subgraph")).thenReturn(testSubgraphPO);

            // Then
            assertTrue(repository.existsByName("test-subgraph"));
        }

        @Test
        @DisplayName("检查名称存在排除指定ID")
        void existsByNameExcludeId_True() {
            // Given
            when(subgraphMapper.existsByNameExcludeId("test-subgraph", 2L)).thenReturn(1);

            // Then
            assertTrue(repository.existsByNameExcludeId("test-subgraph", 2L));
        }
    }

    // ==================== 权限操作测试 ====================

    @Nested
    @DisplayName("权限保存测试")
    class PermissionSaveTests {

        @Test
        @DisplayName("保存权限成功")
        void savePermission_Success() {
            // Given
            when(permissionMapper.insert(any(SubgraphPermissionPO.class))).thenAnswer(invocation -> {
                SubgraphPermissionPO po = invocation.getArgument(0);
                po.setId(1L);
                return 1;
            });
            when(permissionMapper.selectById(1L)).thenReturn(testPermissionPO);

            // When
            SubgraphPermission result = repository.savePermission(testPermission);

            // Then
            assertNotNull(result);
            assertEquals(PermissionRole.OWNER, result.getRole());
            verify(permissionMapper, times(1)).insert(any(SubgraphPermissionPO.class));
        }

        @Test
        @DisplayName("保存空权限抛出异常")
        void savePermission_NullPermission_ThrowsException() {
            assertThrows(IllegalArgumentException.class, () -> repository.savePermission(null));
        }
    }

    @Nested
    @DisplayName("权限查询测试")
    class PermissionFindTests {

        @Test
        @DisplayName("根据子图ID查询权限列表")
        void findPermissionsBySubgraphId_Success() {
            // Given
            when(permissionMapper.selectBySubgraphId(1L)).thenReturn(Arrays.asList(testPermissionPO));

            // When
            List<SubgraphPermission> result = repository.findPermissionsBySubgraphId(1L);

            // Then
            assertEquals(1, result.size());
            assertEquals(PermissionRole.OWNER, result.get(0).getRole());
        }

        @Test
        @DisplayName("根据子图ID和用户ID查询权限")
        void findPermissionBySubgraphIdAndUserId_Success() {
            // Given
            when(permissionMapper.selectBySubgraphIdAndUserId(1L, 100L)).thenReturn(testPermissionPO);

            // When
            Optional<SubgraphPermission> result = repository.findPermissionBySubgraphIdAndUserId(1L, 100L);

            // Then
            assertTrue(result.isPresent());
            assertEquals(PermissionRole.OWNER, result.get().getRole());
        }

        @Test
        @DisplayName("统计子图的Owner数量")
        void countOwnersBySubgraphId_Success() {
            // Given
            when(permissionMapper.countOwnersBySubgraphId(1L)).thenReturn(2);

            // When
            int count = repository.countOwnersBySubgraphId(1L);

            // Then
            assertEquals(2, count);
        }
    }

    @Nested
    @DisplayName("权限检查测试")
    class PermissionCheckTests {

        @Test
        @DisplayName("检查用户是否有Owner权限")
        void hasPermission_Owner_True() {
            // Given
            when(permissionMapper.hasPermission(1L, 100L, "OWNER")).thenReturn(1);

            // Then
            assertTrue(repository.hasPermission(1L, 100L, PermissionRole.OWNER));
        }

        @Test
        @DisplayName("检查用户是否有任何权限")
        void hasAnyPermission_True() {
            // Given
            when(permissionMapper.hasAnyPermission(1L, 100L)).thenReturn(1);

            // Then
            assertTrue(repository.hasAnyPermission(1L, 100L));
        }

        @Test
        @DisplayName("检查用户没有任何权限")
        void hasAnyPermission_False() {
            // Given
            when(permissionMapper.hasAnyPermission(1L, 999L)).thenReturn(0);

            // Then
            assertFalse(repository.hasAnyPermission(1L, 999L));
        }
    }

    @Nested
    @DisplayName("权限删除测试")
    class PermissionDeleteTests {

        @Test
        @DisplayName("删除权限成功")
        void deletePermission_Success() {
            // When
            repository.deletePermission(1L, 100L);

            // Then
            verify(permissionMapper, times(1)).deleteBySubgraphIdAndUserId(1L, 100L);
        }

        @Test
        @DisplayName("删除权限参数为空抛出异常")
        void deletePermission_NullParams_ThrowsException() {
            assertThrows(IllegalArgumentException.class, () -> repository.deletePermission(null, 100L));
            assertThrows(IllegalArgumentException.class, () -> repository.deletePermission(1L, null));
        }
    }

    // ==================== 统计方法测试 ====================

    @Nested
    @DisplayName("统计方法测试")
    class CountTests {

        @Test
        @DisplayName("统计用户有权限的子图数量")
        void countByUserId_Success() {
            // Given
            when(subgraphMapper.countByUserId(100L)).thenReturn(5L);

            // Then
            assertEquals(5L, repository.countByUserId(100L));
        }

        @Test
        @DisplayName("统计关键词搜索结果数量")
        void countByKeyword_Success() {
            // Given
            when(subgraphMapper.countByKeyword("test", 100L)).thenReturn(3L);

            // Then
            assertEquals(3L, repository.countByKeyword("test", 100L));
        }

        @Test
        @DisplayName("统计标签过滤结果数量")
        void countByTags_Success() {
            // Given
            List<String> tags = Arrays.asList("tag1");
            when(subgraphMapper.countByTags(tags, 100L)).thenReturn(2L);

            // Then
            assertEquals(2L, repository.countByTags(tags, 100L));
        }

        @Test
        @DisplayName("统计所有者过滤结果数量")
        void countByOwner_Success() {
            // Given
            when(subgraphMapper.countByOwner(100L, 100L)).thenReturn(4L);

            // Then
            assertEquals(4L, repository.countByOwner(100L, 100L));
        }
    }
}
