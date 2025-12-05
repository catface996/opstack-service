package com.catface996.aiops.repository.mysql.mapper.subgraph;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.catface996.aiops.repository.mysql.po.subgraph.SubgraphPO;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 子图 Mapper 接口
 *
 * <p>提供子图数据的数据库访问操作</p>
 * <p>继承 MyBatis-Plus BaseMapper，自动提供基础 CRUD 方法</p>
 *
 * <p>需求追溯：</p>
 * <ul>
 *   <li>F08: 子图管理功能</li>
 *   <li>需求1: 子图创建</li>
 *   <li>需求2: 子图列表视图</li>
 *   <li>需求3: 子图信息编辑</li>
 *   <li>需求4: 子图删除</li>
 * </ul>
 *
 * @author AI Assistant
 * @since 2025-12-04
 */
public interface SubgraphMapper extends BaseMapper<SubgraphPO> {

    /**
     * 根据名称查询子图
     *
     * @param name 子图名称
     * @return 子图PO对象，如果不存在返回null
     */
    SubgraphPO selectByName(@Param("name") String name);

    /**
     * 查询用户有权限访问的子图列表（通过权限表关联）
     *
     * @param userId 用户ID
     * @param offset 偏移量
     * @param limit 限制数量
     * @return 子图列表
     */
    List<SubgraphPO> selectByUserId(@Param("userId") Long userId,
                                     @Param("offset") int offset,
                                     @Param("limit") int limit);

    /**
     * 统计用户有权限访问的子图数量
     *
     * @param userId 用户ID
     * @return 子图数量
     */
    long countByUserId(@Param("userId") Long userId);

    /**
     * 按关键词搜索子图（全文搜索）
     *
     * @param keyword 搜索关键词
     * @param userId 当前用户ID（用于权限过滤）
     * @param offset 偏移量
     * @param limit 限制数量
     * @return 子图列表
     */
    List<SubgraphPO> searchByKeyword(@Param("keyword") String keyword,
                                      @Param("userId") Long userId,
                                      @Param("offset") int offset,
                                      @Param("limit") int limit);

    /**
     * 统计按关键词搜索的子图数量
     *
     * @param keyword 搜索关键词
     * @param userId 当前用户ID
     * @return 子图数量
     */
    long countByKeyword(@Param("keyword") String keyword, @Param("userId") Long userId);

    /**
     * 按标签过滤子图
     *
     * @param tags 标签列表
     * @param userId 当前用户ID
     * @param offset 偏移量
     * @param limit 限制数量
     * @return 子图列表
     */
    List<SubgraphPO> filterByTags(@Param("tags") List<String> tags,
                                   @Param("userId") Long userId,
                                   @Param("offset") int offset,
                                   @Param("limit") int limit);

    /**
     * 统计按标签过滤的子图数量
     *
     * @param tags 标签列表
     * @param userId 当前用户ID
     * @return 子图数量
     */
    long countByTags(@Param("tags") List<String> tags, @Param("userId") Long userId);

    /**
     * 按所有者过滤子图
     *
     * @param ownerId 所有者用户ID
     * @param currentUserId 当前用户ID
     * @param offset 偏移量
     * @param limit 限制数量
     * @return 子图列表
     */
    List<SubgraphPO> filterByOwner(@Param("ownerId") Long ownerId,
                                    @Param("currentUserId") Long currentUserId,
                                    @Param("offset") int offset,
                                    @Param("limit") int limit);

    /**
     * 统计按所有者过滤的子图数量
     *
     * @param ownerId 所有者用户ID
     * @param currentUserId 当前用户ID
     * @return 子图数量
     */
    long countByOwner(@Param("ownerId") Long ownerId, @Param("currentUserId") Long currentUserId);

    /**
     * 更新子图（带乐观锁）
     *
     * @param po 子图PO对象
     * @return 更新的行数
     */
    int updateWithVersion(SubgraphPO po);

    /**
     * 检查名称是否存在（排除指定ID）
     *
     * @param name 子图名称
     * @param excludeId 要排除的子图ID
     * @return 存在返回1，不存在返回0
     */
    int existsByNameExcludeId(@Param("name") String name, @Param("excludeId") Long excludeId);
}
