package com.example.dto.AI;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import org.springframework.util.StringUtils;

import java.util.Collections;
import java.util.Map;

/**
 * 【核心】一个用于封装评论模板的包装类。
 * 这个类的目的是为了优雅地处理两种模板结构：
 * 1. 旧的扁平结构: "celebrate": "你好"
 * 2. 新的嵌套结构: "comfort": { "SADNESS": "抱抱你", "default": "一切都会好的" }
 * 它对外提供一个统一、安全的方法来获取模板，将内部的复杂性完全隐藏。
 */
public class TemplateCollection {

    /**
     * 内部使用 Map<String, Object> 来存储从数据库或JSON中反序列化出的原始数据。
     * 这个字段是私有的，外部无法直接访问，保证了安全性。
     */
    private final Map<String, Object> templates;

    /**
     * @param templates 从数据库或JSON传入的原始Map
     * @JsonCreator 注解告诉 Jackson (和 Mongo-driver) 如何从一个 Map 创建本类的实例。
     * 这使得无论是新的嵌套数据还是旧的扁平数据，都能被正确地反序列化成本类的对象。
     */
    @JsonCreator
    public TemplateCollection(Map<String, Object> templates) {
        this.templates = templates != null ? templates : Collections.emptyMap();
    }

    /**
     * @return 内部存储的原始Map
     * @JsonValue 注解告诉 Jackson 在序列化（将对象转为JSON）时，
     * 只需将内部的 templates map 序列化即可。
     * 这确保了存入数据库或返回给前端的JSON结构与原始结构保持一致。
     */
    @JsonValue
    public Map<String, Object> getTemplates() {
        return templates;
    }

    /**
     * 【对外唯一安全接口】根据场景和情绪获取最匹配的模板。
     * 这个方法封装了所有的 `instanceof` 检查和回退逻辑。
     *
     * @param scenario 场景, 如 "comfort", "celebrate"
     * @param emotion  情绪, 如 "SADNESS", "JOY" (可以为null)
     * @return 找到的最佳模板字符串
     */
    @SuppressWarnings("unchecked") // 我们在此方法内部处理类型转换，所以可以安全地压制警告
    public String findTemplate(String scenario, String emotion) {
        if (!StringUtils.hasText(scenario)) {
            return null; // 如果场景为空，则无法查找
        }

        Object scenarioTemplate = this.templates.get(scenario);

        if (scenarioTemplate instanceof Map) {
            // 新的嵌套结构: {"comfort": {"SADNESS": "...", "default": "..."}}
            Map<String, String> emotionMap = (Map<String, String>) scenarioTemplate;

            // 1. 优先查找情绪精确匹配的模板
            if (StringUtils.hasText(emotion) && emotionMap.containsKey(emotion)) {
                return emotionMap.get(emotion);
            }

            // 2. 如果没有，查找该场景下的默认模板
            if (emotionMap.containsKey("default")) {
                return emotionMap.get("default");
            }
            // 如果连 default 都没有，则此场景无可用模板，返回null
            return null;

        } else if (scenarioTemplate instanceof String) {
            // 旧的扁平结构: {"comfort": "抱抱你..."}
            return (String) scenarioTemplate;
        }

        // 如果连场景都找不到，返回null
        return null;
    }
}
