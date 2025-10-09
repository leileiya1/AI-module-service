package com.example.aichatservice.controller;

import com.example.aichatservice.entity.Persona;
import com.example.aichatservice.service.PersonaManagementService;
import com.example.dto.AI.ClonePersonaRequest;
import com.example.dto.AI.PersonaDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/personas")
@RequiredArgsConstructor
@Slf4j
public class PersonaManagementController {

    private final PersonaManagementService personaService;


    /**
     * 创建一个新的AI人设。
     * userId 从请求头 X-User-ID 获取。
     */
    @PostMapping
    public ResponseEntity<Persona> createPersona(
            @RequestBody PersonaDto personaDto,
            @RequestHeader("X-User-Id") String userId) {
        Persona createdPersona = personaService.createPersona(personaDto, userId);
        return new ResponseEntity<>(createdPersona, HttpStatus.CREATED);
    }

    /**
     * 获取当前用户创建的所有人设。
     */
    @GetMapping
    public ResponseEntity<List<Persona>> getMyPersonas(@RequestHeader("X-User-Id") String userId) {
        List<Persona> personas = personaService.listPersonasForUser(userId);
        return ResponseEntity.ok(personas);
    }

    /**
     * 更新一个已存在的人设。
     * 只有人设的创建者才能更新。
     */
    @PutMapping("/{personaId}")
    public ResponseEntity<Persona> updatePersona(
            @PathVariable("personaId") String personaId,
            @RequestBody PersonaDto personaDto,
            @RequestHeader("X-User-Id") String userId) {
        Persona updatedPersona = personaService.updatePersona(personaId, personaDto, userId);
        return ResponseEntity.ok(updatedPersona);
    }

    /**
     * 删除一个AI人设。
     * 只有人设的创建者才能删除。
     */
    @DeleteMapping("/{personaId}")
    public ResponseEntity<Void> deletePersona(
            @PathVariable("personaId") String personaId,
            @RequestHeader("X-User-Id") String userId) {
        personaService.deletePersona(personaId, userId);
        return ResponseEntity.noContent().build();
    }

    /**
     * ✨ [新增] 一键式从源内容克隆并创建新人格。
     * 这是一个同步阻塞接口，后台会完成“提交->轮询->创建”的完整流程。
     *
     * @param request 包含源类型(TEXT/URL)和内容的请求体
     * @param userId  操作用户ID
     * @return 创建成功的完整 Persona 对象
     */
    @PostMapping("/clone-from-source")
    public ResponseEntity<Persona> clonePersonaFromSource(
            @RequestBody ClonePersonaRequest request,
            @RequestHeader("X-User-Id") String userId) throws Exception {
        log.info("【API入口】收到用户 '{}' 的人格克隆请求, 类型: {}", userId, request.getType());
        Persona clonedPersona = personaService.clonePersonaFromSource(request, userId);
        return new ResponseEntity<>(clonedPersona, HttpStatus.CREATED);
    }
}
