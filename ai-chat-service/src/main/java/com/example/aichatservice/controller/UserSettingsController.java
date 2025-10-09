package com.example.aichatservice.controller;

import com.example.aichatservice.service.UserSettingsService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/user-settings")
@RequiredArgsConstructor
public class UserSettingsController {

    private final UserSettingsService userSettingsService;


    /**
     * 设置当前用户的默认AI人设。
     *
     * @param personaId 要设置为默认的人设ID，从路径中获取
     * @param userId    当前用户ID，从请求头获取
     * @return 成功则返回204 No Content
     */
    @PutMapping("/default-persona/{personaId}")
    public ResponseEntity<Void> setDefaultPersona(
            @PathVariable("personaId") String personaId,
            @RequestHeader("X-User-Id") String userId) {
        userSettingsService.setDefaultPersonaForUser(userId, personaId);
        return ResponseEntity.noContent().build();
    }
}
