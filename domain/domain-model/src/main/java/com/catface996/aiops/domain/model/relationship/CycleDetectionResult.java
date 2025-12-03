package com.catface996.aiops.domain.model.relationship;

import java.util.Collections;
import java.util.List;

/**
 * 循环依赖检测结果
 *
 * @author AI Assistant
 * @since 2025-12-03
 */
public class CycleDetectionResult {

    /**
     * 是否存在循环依赖
     */
    private final boolean hasCycle;

    /**
     * 循环路径（如果存在循环）
     */
    private final List<Long> cyclePath;

    public CycleDetectionResult(boolean hasCycle, List<Long> cyclePath) {
        this.hasCycle = hasCycle;
        this.cyclePath = cyclePath != null ? cyclePath : Collections.emptyList();
    }

    /**
     * 创建无循环的结果
     */
    public static CycleDetectionResult noCycle() {
        return new CycleDetectionResult(false, Collections.emptyList());
    }

    /**
     * 创建有循环的结果
     */
    public static CycleDetectionResult withCycle(List<Long> cyclePath) {
        return new CycleDetectionResult(true, cyclePath);
    }

    public boolean hasCycle() {
        return hasCycle;
    }

    public List<Long> getCyclePath() {
        return cyclePath;
    }

    @Override
    public String toString() {
        return "CycleDetectionResult{" +
                "hasCycle=" + hasCycle +
                ", cyclePath=" + cyclePath +
                '}';
    }
}
