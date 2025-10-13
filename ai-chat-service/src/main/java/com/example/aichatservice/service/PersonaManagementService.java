package com.example.aichatservice.service;

import com.example.aichatservice.entity.Persona;
import com.example.dto.AI.ClonePersonaRequest;
import com.example.dto.AI.PersonaDto;
import com.fasterxml.jackson.core.JsonProcessingException;

import java.util.List;

public interface PersonaManagementService {
    Persona createPersona(PersonaDto personaDto, String userId);

    Persona updatePersona(String personaId, PersonaDto personaDto, String userId);

    void deletePersona(String personaId, String userId);

    List<Persona> listPersonasForUser(String userId);

    Persona getPersonaById(String personaId);

    Persona clonePersonaFromSource(ClonePersonaRequest request, String userId) throws JsonProcessingException, InterruptedException;

    /**
     * 【新增】为指定人格启动一次微调训练任务。
     *
     * @param personaId 要训练的人格ID
     * @param userId    操作者ID，用于权限校验
     */
    void startFinetuningForPersona(String personaId, String userId);

    /**
     * 【新增接口】更新指定人格的微调模型ID。
     *
     * @param personaId  要更新的人格的ID
     * @param newModelId 由微调服务生成的新模型ID
     */
    void updatePersonaModelId(String personaId, String newModelId);
}
