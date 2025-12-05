package com.catface996.aiops.repository.mysql.impl.subgraph;

import com.catface996.aiops.domain.model.subgraph.SubgraphResource;
import com.catface996.aiops.repository.mysql.mapper.subgraph.SubgraphResourceMapper;
import com.catface996.aiops.repository.mysql.po.subgraph.SubgraphResourcePO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * SubgraphResourceRepositoryImpl 单元测试
 *
 * <p>需求追溯：</p>
 * <ul>
 *   <li>F08: 子图管理功能</li>
 *   <li>任务5: 实现 SubgraphResourceRepository</li>
 * </ul>
 *
 * @author AI Assistant
 * @since 2025-12-04
 */
@ExtendWith(MockitoExtension.class)
class SubgraphResourceRepositoryImplTest {

    @Mock
    private SubgraphResourceMapper resourceMapper;

    @InjectMocks
    private SubgraphResourceRepositoryImpl repository;

    private SubgraphResourcePO testResourcePO;
    private SubgraphResource testResource;

    @BeforeEach
    void setUp() {
        testResourcePO = new SubgraphResourcePO();
        testResourcePO.setId(1L);
        testResourcePO.setSubgraphId(1L);
        testResourcePO.setResourceId(100L);
        testResourcePO.setAddedAt(LocalDateTime.now());
        testResourcePO.setAddedBy(10L);

        testResource = new SubgraphResource();
        testResource.setId(1L);
        testResource.setSubgraphId(1L);
        testResource.setResourceId(100L);
        testResource.setAddedAt(LocalDateTime.now());
        testResource.setAddedBy(10L);
    }

    @Nested
    @DisplayName("添加资源测试")
    class AddResourceTests {

        @Test
        @DisplayName("添加单个资源成功")
        void addResource_Success() {
            // Given
            when(resourceMapper.insert(any(SubgraphResourcePO.class))).thenAnswer(invocation -> {
                SubgraphResourcePO po = invocation.getArgument(0);
                po.setId(1L);
                return 1;
            });
            when(resourceMapper.selectById(1L)).thenReturn(testResourcePO);

            // When
            SubgraphResource result = repository.addResource(testResource);

            // Then
            assertNotNull(result);
            assertEquals(1L, result.getSubgraphId());
            assertEquals(100L, result.getResourceId());
            verify(resourceMapper, times(1)).insert(any(SubgraphResourcePO.class));
        }

        @Test
        @DisplayName("添加空资源抛出异常")
        void addResource_NullResource_ThrowsException() {
            assertThrows(IllegalArgumentException.class, () -> repository.addResource(null));
            verify(resourceMapper, never()).insert(any(SubgraphResourcePO.class));
        }

        @Test
        @DisplayName("批量添加资源成功")
        void addResources_Success() {
            // Given
            SubgraphResource resource2 = new SubgraphResource();
            resource2.setSubgraphId(1L);
            resource2.setResourceId(200L);
            resource2.setAddedBy(10L);

            List<SubgraphResource> resources = Arrays.asList(testResource, resource2);
            when(resourceMapper.batchInsert(anyList())).thenReturn(2);

            // When
            repository.addResources(resources);

            // Then
            verify(resourceMapper, times(1)).batchInsert(anyList());
        }

        @Test
        @DisplayName("批量添加空列表不执行操作")
        void addResources_EmptyList_NoOperation() {
            repository.addResources(Collections.emptyList());
            verify(resourceMapper, never()).batchInsert(anyList());
        }
    }

    @Nested
    @DisplayName("移除资源测试")
    class RemoveResourceTests {

        @Test
        @DisplayName("移除单个资源成功")
        void removeResource_Success() {
            // When
            repository.removeResource(1L, 100L);

            // Then
            verify(resourceMapper, times(1)).deleteBySubgraphIdAndResourceId(1L, 100L);
        }

        @Test
        @DisplayName("移除资源参数为空抛出异常")
        void removeResource_NullParams_ThrowsException() {
            assertThrows(IllegalArgumentException.class, () -> repository.removeResource(null, 100L));
            assertThrows(IllegalArgumentException.class, () -> repository.removeResource(1L, null));
        }

        @Test
        @DisplayName("批量移除资源成功")
        void removeResources_Success() {
            // Given
            List<Long> resourceIds = Arrays.asList(100L, 200L);
            when(resourceMapper.batchDeleteBySubgraphIdAndResourceIds(eq(1L), eq(resourceIds))).thenReturn(2);

            // When
            repository.removeResources(1L, resourceIds);

            // Then
            verify(resourceMapper, times(1)).batchDeleteBySubgraphIdAndResourceIds(1L, resourceIds);
        }

        @Test
        @DisplayName("批量移除空列表不执行操作")
        void removeResources_EmptyList_NoOperation() {
            repository.removeResources(1L, Collections.emptyList());
            verify(resourceMapper, never()).batchDeleteBySubgraphIdAndResourceIds(anyLong(), anyList());
        }
    }

    @Nested
    @DisplayName("查询资源测试")
    class FindResourceTests {

        @Test
        @DisplayName("查询子图中的资源ID列表")
        void findResourceIdsBySubgraphId_Success() {
            // Given
            List<Long> resourceIds = Arrays.asList(100L, 200L, 300L);
            when(resourceMapper.selectResourceIdsBySubgraphId(1L)).thenReturn(resourceIds);

            // When
            List<Long> result = repository.findResourceIdsBySubgraphId(1L);

            // Then
            assertEquals(3, result.size());
            assertTrue(result.contains(100L));
        }

        @Test
        @DisplayName("查询子图中的资源关联记录")
        void findBySubgraphId_Success() {
            // Given
            when(resourceMapper.selectBySubgraphId(1L)).thenReturn(Arrays.asList(testResourcePO));

            // When
            List<SubgraphResource> result = repository.findBySubgraphId(1L);

            // Then
            assertEquals(1, result.size());
            assertEquals(100L, result.get(0).getResourceId());
        }

        @Test
        @DisplayName("统计子图中的资源数量")
        void countBySubgraphId_Success() {
            // Given
            when(resourceMapper.countBySubgraphId(1L)).thenReturn(5);

            // Then
            assertEquals(5, repository.countBySubgraphId(1L));
        }

        @Test
        @DisplayName("查询包含指定资源的所有子图ID")
        void findSubgraphIdsByResourceId_Success() {
            // Given
            List<Long> subgraphIds = Arrays.asList(1L, 2L);
            when(resourceMapper.selectSubgraphIdsByResourceId(100L)).thenReturn(subgraphIds);

            // When
            List<Long> result = repository.findSubgraphIdsByResourceId(100L);

            // Then
            assertEquals(2, result.size());
        }
    }

    @Nested
    @DisplayName("存在性检查测试")
    class ExistsTests {

        @Test
        @DisplayName("资源存在于子图中")
        void existsInSubgraph_True() {
            // Given
            when(resourceMapper.existsInSubgraph(1L, 100L)).thenReturn(1);

            // Then
            assertTrue(repository.existsInSubgraph(1L, 100L));
        }

        @Test
        @DisplayName("资源不存在于子图中")
        void existsInSubgraph_False() {
            // Given
            when(resourceMapper.existsInSubgraph(1L, 999L)).thenReturn(0);

            // Then
            assertFalse(repository.existsInSubgraph(1L, 999L));
        }

        @Test
        @DisplayName("子图为空")
        void isSubgraphEmpty_True() {
            // Given
            when(resourceMapper.countBySubgraphId(1L)).thenReturn(0);

            // Then
            assertTrue(repository.isSubgraphEmpty(1L));
        }

        @Test
        @DisplayName("子图不为空")
        void isSubgraphEmpty_False() {
            // Given
            when(resourceMapper.countBySubgraphId(1L)).thenReturn(3);

            // Then
            assertFalse(repository.isSubgraphEmpty(1L));
        }
    }

    @Nested
    @DisplayName("删除资源关联测试")
    class DeleteTests {

        @Test
        @DisplayName("删除子图的所有资源关联")
        void deleteAllBySubgraphId_Success() {
            // When
            repository.deleteAllBySubgraphId(1L);

            // Then
            verify(resourceMapper, times(1)).deleteAllBySubgraphId(1L);
        }

        @Test
        @DisplayName("删除资源节点在所有子图中的关联")
        void deleteAllByResourceId_Success() {
            // When
            repository.deleteAllByResourceId(100L);

            // Then
            verify(resourceMapper, times(1)).deleteAllByResourceId(100L);
        }

        @Test
        @DisplayName("删除操作参数为空抛出异常")
        void delete_NullParams_ThrowsException() {
            assertThrows(IllegalArgumentException.class, () -> repository.deleteAllBySubgraphId(null));
            assertThrows(IllegalArgumentException.class, () -> repository.deleteAllByResourceId(null));
        }
    }
}
