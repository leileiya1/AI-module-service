package com.example.aichatservice.service.impl;

import com.example.aichatservice.entity.Persona;
import com.example.aichatservice.exception.PersonaNotFoundException;
import com.example.aichatservice.exception.UnauthorizedActionException;
import com.example.aichatservice.repository.PersonaRepository;
import com.example.aichatservice.service.PersonaManagementService;
import com.example.apis.FinetuningManagerClient;
import com.example.apis.InsightServiceClient;
import com.example.dto.AI.*;
import com.example.exception.ResourceNotFoundException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Slf4j
@RequiredArgsConstructor
public class PersonaManagementServiceImpl implements PersonaManagementService {

    private final PersonaRepository personaRepository;
    private final InsightServiceClient insightServiceClient; // ✨ 注入新的 Feign 客户端
    private final ObjectMapper objectMapper; // ✨ 注入 ObjectMapper 用于解析JSON
    private final FinetuningManagerClient finetuningManagerClient; // ✨ 注入Feign客户端


    @Override
    public Persona createPersona(PersonaDto dto, String userId) {
        log.info("用户 '{}' 正在创建新的人设: {}", userId, dto.getName());
        Persona persona = new Persona();
        persona.setUserId(userId);
        return getPersona(dto, persona);
    }

    @Override
    public Persona updatePersona(String personaId, PersonaDto dto, String userId) {
        log.info("用户 '{}' 正在尝试更新人设 ID: {}", userId, personaId);
        Persona existingPersona = findAndVerifyOwnership(personaId, userId);

        return getPersona(dto, existingPersona);
    }

    @NotNull
    private Persona getPersona(PersonaDto dto, Persona existingPersona) {
        existingPersona.setName(dto.getName());
        existingPersona.setRole(dto.getRole());
        existingPersona.setTone(dto.getTone());
        existingPersona.setStyle(dto.getStyle());
        existingPersona.setCommentTemplates(dto.getCommentTemplates());
        existingPersona.setMemoryEnabled(dto.isMemoryEnabled());
        existingPersona.setKnowledgeBaseId(dto.getKnowledgeBaseId());
        return personaRepository.save(existingPersona);
    }

    @Override
    public void deletePersona(String personaId, String userId) {
        log.info("用户 '{}' 正在尝试删除人设 ID: {}", userId, personaId);
        Persona personaToDelete = findAndVerifyOwnership(personaId, userId);
        personaRepository.delete(personaToDelete);
        log.info("人设 ID: {} 已被用户 '{}' 成功删除", personaId, userId);
    }

    @Override
    public List<Persona> listPersonasForUser(String userId) {
        log.info("正在为用户 '{}' 查询其所有的人设", userId);
        return personaRepository.findByUserId(userId);
    }

    @Override
    public Persona getPersonaById(String personaId) {
        return personaRepository.findById(personaId)
                .orElseThrow(() -> new PersonaNotFoundException("未找到ID为 '" + personaId + "' 的人设"));
    }

    @Override
    public Persona clonePersonaFromSource(ClonePersonaRequest request, String userId) throws JsonProcessingException, InterruptedException {
        long startTime = System.currentTimeMillis();
        log.info("【人格克隆】[用户:{}] 开始执行克隆流程...", userId);

        // 1. 调用 insight-service 提交分析任务
        CreateInsightJobRequest jobRequest = new CreateInsightJobRequest(request.getType(), request.getContent());
        CreateJobResponse jobResponse = insightServiceClient.createJob(jobRequest, userId);
        String jobId = jobResponse.getJobId();
        log.info("【人格克隆】[用户:{}] 已成功提交分析任务到 insight-service, JobID: {}", userId, jobId);

        // 2. 轮询任务结果 (同步阻塞)
        // 注意：在生产环境中，对于长任务，更好的方式是使用WebSocket或回调，但轮询是简单有效的起步方案。
        final int MAX_ATTEMPTS = 10; // 最多轮询10次
        final long POLLING_INTERVAL_MS = 2000; // 每次间隔2秒

        for (int attempt = 1; attempt <= MAX_ATTEMPTS; attempt++) {
            log.info("【人格克隆】[用户:{}] [JobID:{}] 第 {}/{} 次查询任务状态...", userId, jobId, attempt, MAX_ATTEMPTS);
            InsightJobStatusResponse statusResponse = insightServiceClient.getJobStatus(jobId, userId);

            if ("COMPLETED".equals(statusResponse.getStatus())) {
                log.info("【人格克隆】[用户:{}] [JobID:{}] 任务分析成功！", userId, jobId);

                // 3. 将分析结果JSON转换为 PersonaDto
                PersonaDto clonedDto = objectMapper.treeToValue(statusResponse.getResult(), PersonaDto.class);

                // 4. 调用现有的 createPersona 方法创建新人格
                log.info("【人格克隆】[用户:{}] 正在根据分析结果创建新的人格 '{}'...", userId, clonedDto.getName());
                Persona newPersona = this.createPersona(clonedDto, userId); // 复用创建逻辑

                long duration = System.currentTimeMillis() - startTime;
                log.info("【人格克隆】[用户:{}] 人格 '{}' (ID: {}) 克隆并创建成功！总耗时: {}ms", userId, newPersona.getName(), newPersona.getId(), duration);
                return newPersona;
            }

            if ("FAILED".equals(statusResponse.getStatus())) {
                log.error("【人格克隆】[用户:{}] [JobID:{}] 任务分析失败！原因: {}", userId, jobId, statusResponse.getErrorMessage());
                throw new RuntimeException("人格风格分析失败: " + statusResponse.getErrorMessage());
            }

            // 等待一段时间再进行下一次查询
            Thread.sleep(POLLING_INTERVAL_MS);
        }

        // 如果循环结束任务仍未完成，则超时
        log.error("【人格克隆】[用户:{}] [JobID:{}] 任务处理超时！", userId, jobId);
        throw new RuntimeException("人格风格分析超时，请稍后再试。");
    }

    @Override
    public void startFinetuningForPersona(String personaId, String userId) {
        log.info("【微调触发】用户 '{}' 正在为人格 '{}' 启动微调训练...", userId, personaId);

        // 1. 权限校验：确保用户只能为自己的人格启动训练
        Persona persona = findAndVerifyOwnership(personaId, userId);

        // 2. 构建请求并调用 finetuning-manager-service
        CreateFinetuningJobRequest request = new CreateFinetuningJobRequest(userId, personaId);
        try {
            CreateFinetuningJobResponse response = finetuningManagerClient.createJob(request);
            log.info("【微调触发】已成功向 finetuning-manager-service 提交任务, JobID: {}", response.getJobId());
        } catch (Exception e) {
            log.error("【微调触发】向 finetuning-manager-service 提交任务失败！", e);
            throw new RuntimeException("启动微调任务失败，请稍后重试。");
        }
    }

    /**
     * 【新增实现】更新人格模型ID的核心逻辑。
     */
    @Override
    @Transactional
    public void updatePersonaModelId(String personaId, String newModelId) {
        log.info("【内部更新】收到更新人格 '{}' 的模型ID请求，新模型ID为: '{}'", personaId, newModelId);

        // 1. 查找人格
        // findById返回一个Optional，这是处理可能不存在对象的最安全方式。
        Persona persona = personaRepository.findById(personaId)
                .orElseThrow(() -> {
                    log.error("【内部更新】更新失败：未能在数据库中找到ID为 '{}' 的人格。", personaId);
                    return new ResourceNotFoundException("更新模型ID失败：人格不存在。");
                });

        // 2. 更新模型ID字段
        persona.setFineTunedModelId(newModelId);

        // 3. 保存更新后的人格回数据库
        personaRepository.save(persona);

        log.info("【内部更新】人格 '{}' 的 fineTunedModelId 已成功更新为 '{}'", personaId, newModelId);
    }

    /**
     * 内部辅助方法：查找人设并验证其所有权。
     */
    private Persona findAndVerifyOwnership(String personaId, String userId) {
        Persona persona = getPersonaById(personaId);
        if (!persona.getUserId().equals(userId)) {
            log.warn("安全警告：用户 '{}' 尝试操作不属于自己的的人设 ID: {}", userId, personaId);
            throw new UnauthorizedActionException("你没有权限操作此人设");
        }
        return persona;
    }
}
