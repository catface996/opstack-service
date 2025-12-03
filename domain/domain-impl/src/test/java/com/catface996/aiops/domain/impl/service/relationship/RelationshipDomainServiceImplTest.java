package com.catface996.aiops.domain.impl.service.relationship;

import com.catface996.aiops.common.enums.RelationshipErrorCode;
import com.catface996.aiops.common.exception.BusinessException;
import com.catface996.aiops.domain.model.relationship.*;
import com.catface996.aiops.domain.model.resource.Resource;
import com.catface996.aiops.repository.relationship.RelationshipRepository;
import com.catface996.aiops.repository.resource.ResourceRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * 资源关系领域服务实现类单元测试
 *
 * @author AI Assistant
 * @since 2025-12-03
 */
@DisplayName("资源关系领域服务测试")
class RelationshipDomainServiceImplTest {

    private RelationshipDomainServiceImpl relationshipDomainService;
    private RelationshipRepository relationshipRepository;
    private ResourceRepository resourceRepository;

    @BeforeEach
    void setUp() {
        relationshipRepository = mock(RelationshipRepository.class);
        resourceRepository = mock(ResourceRepository.class);
        relationshipDomainService = new RelationshipDomainServiceImpl(
                relationshipRepository, resourceRepository);
    }

    @Nested
    @DisplayName("创建关系测试")
    class CreateRelationshipTest {

        @Test
        @DisplayName("应该成功创建关系")
        void shouldCreateRelationshipSuccessfully() {
            // Given
            Long sourceId = 1L;
            Long targetId = 2L;
            Long operatorId = 100L;

            Resource sourceResource = new Resource();
            sourceResource.setId(sourceId);
            sourceResource.setName("order-service");
            sourceResource.setCreatedBy(operatorId);

            Resource targetResource = new Resource();
            targetResource.setId(targetId);
            targetResource.setName("order-db");
            targetResource.setCreatedBy(operatorId);

            when(resourceRepository.findById(sourceId)).thenReturn(Optional.of(sourceResource));
            when(resourceRepository.findById(targetId)).thenReturn(Optional.of(targetResource));
            when(relationshipRepository.existsBySourceAndTargetAndType(sourceId, targetId, RelationshipType.DEPENDENCY))
                    .thenReturn(false);
            when(relationshipRepository.save(any(Relationship.class))).thenAnswer(invocation -> {
                Relationship r = invocation.getArgument(0);
                r.setId(1L);
                return r;
            });

            // When
            Relationship result = relationshipDomainService.createRelationship(
                    sourceId, targetId, RelationshipType.DEPENDENCY,
                    RelationshipDirection.UNIDIRECTIONAL, RelationshipStrength.STRONG,
                    "测试描述", operatorId);

            // Then
            assertNotNull(result);
            assertEquals(sourceId, result.getSourceResourceId());
            assertEquals(targetId, result.getTargetResourceId());
            assertEquals(RelationshipType.DEPENDENCY, result.getRelationshipType());
            assertEquals(RelationshipStatus.NORMAL, result.getStatus());
        }

        @Test
        @DisplayName("自引用应该抛出异常")
        void shouldThrowExceptionWhenSelfReference() {
            // When & Then
            BusinessException ex = assertThrows(BusinessException.class, () ->
                    relationshipDomainService.createRelationship(
                            1L, 1L, RelationshipType.DEPENDENCY,
                            RelationshipDirection.UNIDIRECTIONAL, RelationshipStrength.STRONG,
                            "desc", 100L));
            assertEquals(RelationshipErrorCode.SELF_REFERENCE_NOT_ALLOWED.getCode(), ex.getErrorCode());
        }

        @Test
        @DisplayName("源资源不存在时应该抛出异常")
        void shouldThrowExceptionWhenSourceNotFound() {
            // Given
            when(resourceRepository.findById(1L)).thenReturn(Optional.empty());

            // When & Then
            BusinessException ex = assertThrows(BusinessException.class, () ->
                    relationshipDomainService.createRelationship(
                            1L, 2L, RelationshipType.DEPENDENCY,
                            RelationshipDirection.UNIDIRECTIONAL, RelationshipStrength.STRONG,
                            "desc", 100L));
            assertEquals(RelationshipErrorCode.SOURCE_RESOURCE_NOT_FOUND.getCode(), ex.getErrorCode());
        }

        @Test
        @DisplayName("关系已存在时应该抛出异常")
        void shouldThrowExceptionWhenRelationshipExists() {
            // Given
            Long sourceId = 1L;
            Long targetId = 2L;

            Resource sourceResource = new Resource();
            sourceResource.setId(sourceId);
            sourceResource.setCreatedBy(100L);

            Resource targetResource = new Resource();
            targetResource.setId(targetId);
            targetResource.setCreatedBy(100L);

            when(resourceRepository.findById(sourceId)).thenReturn(Optional.of(sourceResource));
            when(resourceRepository.findById(targetId)).thenReturn(Optional.of(targetResource));
            when(relationshipRepository.existsBySourceAndTargetAndType(sourceId, targetId, RelationshipType.DEPENDENCY))
                    .thenReturn(true);

            // When & Then
            BusinessException ex = assertThrows(BusinessException.class, () ->
                    relationshipDomainService.createRelationship(
                            sourceId, targetId, RelationshipType.DEPENDENCY,
                            RelationshipDirection.UNIDIRECTIONAL, RelationshipStrength.STRONG,
                            "desc", 100L));
            assertEquals(RelationshipErrorCode.RELATIONSHIP_ALREADY_EXISTS.getCode(), ex.getErrorCode());
        }
    }

    @Nested
    @DisplayName("查询关系测试")
    class QueryRelationshipTest {

        @Test
        @DisplayName("应该成功查询关系列表")
        void shouldListRelationships() {
            // Given
            Relationship rel1 = Relationship.create(1L, 2L, RelationshipType.DEPENDENCY,
                    RelationshipDirection.UNIDIRECTIONAL, RelationshipStrength.STRONG, "desc1");
            rel1.setId(1L);

            when(relationshipRepository.findByConditions(null, null, null, null, 1, 20))
                    .thenReturn(Collections.singletonList(rel1));
            when(resourceRepository.findById(1L)).thenReturn(Optional.of(createResource(1L, "source")));
            when(resourceRepository.findById(2L)).thenReturn(Optional.of(createResource(2L, "target")));

            // When
            List<Relationship> result = relationshipDomainService.listRelationships(null, null, null, null, 1, 20);

            // Then
            assertEquals(1, result.size());
            assertEquals("source", result.get(0).getSourceResourceName());
            assertEquals("target", result.get(0).getTargetResourceName());
        }

        @Test
        @DisplayName("应该成功根据ID查询关系")
        void shouldGetRelationshipById() {
            // Given
            Relationship rel = Relationship.create(1L, 2L, RelationshipType.DEPENDENCY,
                    RelationshipDirection.UNIDIRECTIONAL, RelationshipStrength.STRONG, "desc");
            rel.setId(1L);

            when(relationshipRepository.findById(1L)).thenReturn(Optional.of(rel));
            when(resourceRepository.findById(1L)).thenReturn(Optional.of(createResource(1L, "source")));
            when(resourceRepository.findById(2L)).thenReturn(Optional.of(createResource(2L, "target")));

            // When
            Optional<Relationship> result = relationshipDomainService.getRelationshipById(1L);

            // Then
            assertTrue(result.isPresent());
            assertEquals(1L, result.get().getId());
        }
    }

    @Nested
    @DisplayName("更新关系测试")
    class UpdateRelationshipTest {

        @Test
        @DisplayName("应该成功更新关系")
        void shouldUpdateRelationshipSuccessfully() {
            // Given
            Relationship existingRel = Relationship.create(1L, 2L, RelationshipType.DEPENDENCY,
                    RelationshipDirection.UNIDIRECTIONAL, RelationshipStrength.STRONG, "old desc");
            existingRel.setId(1L);

            Resource sourceResource = createResource(1L, "source");
            sourceResource.setCreatedBy(100L);
            Resource targetResource = createResource(2L, "target");

            when(relationshipRepository.findById(1L)).thenReturn(Optional.of(existingRel));
            when(resourceRepository.findById(1L)).thenReturn(Optional.of(sourceResource));
            when(resourceRepository.findById(2L)).thenReturn(Optional.of(targetResource));
            when(relationshipRepository.update(any(Relationship.class))).thenReturn(existingRel);

            // When
            Relationship result = relationshipDomainService.updateRelationship(
                    1L, null, RelationshipStrength.WEAK, null, "new desc", 100L);

            // Then
            assertNotNull(result);
            verify(relationshipRepository).update(any(Relationship.class));
        }

        @Test
        @DisplayName("关系不存在时应该抛出异常")
        void shouldThrowExceptionWhenRelationshipNotFound() {
            // Given
            when(relationshipRepository.findById(99L)).thenReturn(Optional.empty());

            // When & Then
            BusinessException ex = assertThrows(BusinessException.class, () ->
                    relationshipDomainService.updateRelationship(99L, null, null, null, "desc", 100L));
            assertEquals(RelationshipErrorCode.RELATIONSHIP_NOT_FOUND.getCode(), ex.getErrorCode());
        }
    }

    @Nested
    @DisplayName("删除关系测试")
    class DeleteRelationshipTest {

        @Test
        @DisplayName("应该成功删除关系")
        void shouldDeleteRelationshipSuccessfully() {
            // Given
            Relationship existingRel = Relationship.create(1L, 2L, RelationshipType.DEPENDENCY,
                    RelationshipDirection.UNIDIRECTIONAL, RelationshipStrength.STRONG, "desc");
            existingRel.setId(1L);

            Resource sourceResource = createResource(1L, "source");
            sourceResource.setCreatedBy(100L);

            when(relationshipRepository.findById(1L)).thenReturn(Optional.of(existingRel));
            when(resourceRepository.findById(1L)).thenReturn(Optional.of(sourceResource));
            when(resourceRepository.findById(2L)).thenReturn(Optional.of(createResource(2L, "target")));

            // When
            relationshipDomainService.deleteRelationship(1L, 100L);

            // Then
            verify(relationshipRepository).deleteById(1L);
        }
    }

    @Nested
    @DisplayName("循环依赖检测测试")
    class CycleDetectionTest {

        @Test
        @DisplayName("无循环时应该返回false")
        void shouldReturnFalseWhenNoCycle() {
            // Given: A -> B -> C
            when(relationshipRepository.findBySourceResourceId(1L))
                    .thenReturn(Collections.singletonList(createRelWithTarget(2L)));
            when(relationshipRepository.findBySourceResourceId(2L))
                    .thenReturn(Collections.singletonList(createRelWithTarget(3L)));
            when(relationshipRepository.findBySourceResourceId(3L))
                    .thenReturn(Collections.emptyList());

            // When
            CycleDetectionResult result = relationshipDomainService.detectCycle(1L);

            // Then
            assertFalse(result.hasCycle());
            assertTrue(result.getCyclePath().isEmpty());
        }

        @Test
        @DisplayName("有循环时应该返回true和路径")
        void shouldReturnTrueWhenCycleExists() {
            // Given: A -> B -> C -> A (cycle)
            when(relationshipRepository.findBySourceResourceId(1L))
                    .thenReturn(Collections.singletonList(createRelWithTarget(2L)));
            when(relationshipRepository.findBySourceResourceId(2L))
                    .thenReturn(Collections.singletonList(createRelWithTarget(3L)));
            when(relationshipRepository.findBySourceResourceId(3L))
                    .thenReturn(Collections.singletonList(createRelWithTarget(1L)));

            // When
            CycleDetectionResult result = relationshipDomainService.detectCycle(1L);

            // Then
            assertTrue(result.hasCycle());
            assertFalse(result.getCyclePath().isEmpty());
        }
    }

    @Nested
    @DisplayName("图遍历测试")
    class TraverseTest {

        @Test
        @DisplayName("应该正确遍历图并返回各层级节点")
        void shouldTraverseGraphCorrectly() {
            // Given: A -> B, A -> C
            when(relationshipRepository.findBySourceResourceId(1L))
                    .thenReturn(Arrays.asList(createRelWithTarget(2L), createRelWithTarget(3L)));
            when(relationshipRepository.findBySourceResourceId(2L))
                    .thenReturn(Collections.emptyList());
            when(relationshipRepository.findBySourceResourceId(3L))
                    .thenReturn(Collections.emptyList());

            // When
            TraverseResult result = relationshipDomainService.traverse(1L, 5);

            // Then
            assertEquals(1L, result.getStartResourceId());
            assertEquals(3, result.getTotalNodes());
            assertTrue(result.getNodesByLevel().get(0).contains(1L));
            assertTrue(result.getNodesByLevel().get(1).contains(2L));
            assertTrue(result.getNodesByLevel().get(1).contains(3L));
        }
    }

    // Helper methods
    private Resource createResource(Long id, String name) {
        Resource r = new Resource();
        r.setId(id);
        r.setName(name);
        return r;
    }

    private Relationship createRelWithTarget(Long targetId) {
        Relationship rel = Relationship.create(1L, targetId, RelationshipType.DEPENDENCY,
                RelationshipDirection.UNIDIRECTIONAL, RelationshipStrength.STRONG, "desc");
        return rel;
    }
}
