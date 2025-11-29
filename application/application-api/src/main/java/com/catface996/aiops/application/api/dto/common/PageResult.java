package com.catface996.aiops.application.api.dto.common;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 分页结果
 *
 * <p>通用分页查询结果包装类。</p>
 *
 * @param <T> 数据项类型
 * @author AI Assistant
 * @since 2025-11-29
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "分页结果")
public class PageResult<T> {

    @Schema(description = "数据列表")
    private List<T> content;

    @Schema(description = "当前页码（从1开始）", example = "1")
    private int page;

    @Schema(description = "每页大小", example = "10")
    private int size;

    @Schema(description = "总记录数", example = "100")
    private long totalElements;

    @Schema(description = "总页数", example = "10")
    private int totalPages;

    @Schema(description = "是否为第一页", example = "true")
    private boolean first;

    @Schema(description = "是否为最后一页", example = "false")
    private boolean last;

    /**
     * 创建分页结果
     *
     * @param content       数据列表
     * @param page          当前页码（从1开始）
     * @param size          每页大小
     * @param totalElements 总记录数
     * @param <T>           数据项类型
     * @return 分页结果
     */
    public static <T> PageResult<T> of(List<T> content, int page, int size, long totalElements) {
        int totalPages = size > 0 ? (int) Math.ceil((double) totalElements / size) : 0;
        return PageResult.<T>builder()
                .content(content)
                .page(page)
                .size(size)
                .totalElements(totalElements)
                .totalPages(totalPages)
                .first(page == 1)
                .last(page >= totalPages)
                .build();
    }
}
