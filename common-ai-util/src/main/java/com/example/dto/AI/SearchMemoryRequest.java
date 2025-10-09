package com.example.dto.AI;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * 搜索记忆的请求体 DTO。
 * <p>
 * 它封装了进行一次语义搜索所需要的所有参数。
 */
@Data
public class SearchMemoryRequest {


    /**
     * 用于查询的文本。
     * @NotBlank 注解确保客户端在发送请求时，该字段必须存在且内容不能为空字符串。
     */
    @NotBlank(message = "查询文本(query)不能为空")
    private String query;

    /**
     * 希望返回的最相似记忆的数量。
     * 默认值为 3，用于防止返回过多不相关的结果。
     */
    private int topK = 3;
}
