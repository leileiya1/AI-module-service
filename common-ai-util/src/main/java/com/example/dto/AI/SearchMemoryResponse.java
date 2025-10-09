package com.example.dto.AI;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 搜索记忆的响应体 DTO。
 * <p>
 * 列表中的每一项都代表一条被找回的记忆及其与查询的相关性。
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class SearchMemoryResponse {

    /**
     * 检索到的记忆原文。
     */
    private String content;

    /**
     * 相似度分数。
     * <p>
     * 这是计算出的余弦相似度（1 - 余弦距离），范围通常在 [-1, 1] 之间。
     * 分数越接近 1，表示语义上越相似。
     */
    private double distance;
}
