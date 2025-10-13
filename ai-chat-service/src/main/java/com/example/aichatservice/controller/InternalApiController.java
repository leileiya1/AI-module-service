package com.example.aichatservice.controller;


import com.example.aichatservice.service.PersonaManagementService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * 内部API控制器。
 * <p>
 * 此控制器下的所有接口【不应该】直接暴露给外部用户。
 * 它们专门用于微服务之间的内部通信。
 * 在生产环境中，应在API网关层面配置安全策略（如IP白名单），确保只有受信任的服务才能访问这些接口。
 */
@RestController
@RequestMapping("/api/v1/internal/personas") // 使用独立的 "/internal" 路径前缀
@Slf4j
@RequiredArgsConstructor
public class InternalApiController {

    private final PersonaManagementService personaService;

    /**
     * 【回调接口】供 finetuning-manager-service 在微调任务成功后回调此接口，
     * 以更新人格所绑定的专属模型ID。
     *
     * @param personaId 要更新的人格ID，从URL路径中获取。
     * @param payload   包含新模型ID的请求体，格式为 {"modelId": "..."}。
     * @return 成功则返回 200 OK。
     */
    @PutMapping("/{personaId}/model")
    public ResponseEntity<Void> updatePersonaModel(
            @PathVariable("personaId") String personaId,
            @RequestBody Map<String, String> payload) {

        String newModelId = payload.get("modelId");
        log.info("【内部回调API】收到来自 finetuning-manager-service 的请求，准备更新人格 '{}' 的模型ID...", personaId);

        if (newModelId == null || newModelId.isBlank()) {
            log.error("【内部回调API】请求失败：请求体中缺少 'modelId' 字段。");
            // 返回客户端错误
            return ResponseEntity.badRequest().build();
        }

        try {
            personaService.updatePersonaModelId(personaId, newModelId);
            log.info("【内部回调API】人格 '{}' 模型ID更新成功。", personaId);
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            // 如果Service层抛出异常（如人格不存在），则记录错误并返回相应的HTTP状态码
            // 这里的具体状态码取决于你的全局异常处理器
            log.error("【内部回调API】更新人格 '{}' 模型ID时发生错误。", personaId, e);
            // 这里可以简单返回一个服务器内部错误
            return ResponseEntity.internalServerError().build();
        }
    }
}
