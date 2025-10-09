package com.example.aichatservice.service.impl;

import com.example.aichatservice.entity.Persona;
import com.example.aichatservice.exception.PersonaNotFoundException;
import com.example.aichatservice.exception.UnauthorizedActionException;
import com.example.aichatservice.repository.PersonaRepository;
import com.example.aichatservice.repository.UserSettingsRepository;
import com.example.aichatservice.service.UserSettingsService;
import com.example.aichatservice.setting.UserSettings;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class UserSettingsServiceImpl implements UserSettingsService {
    private static final Logger log = LoggerFactory.getLogger(UserSettingsServiceImpl.class);

    private final UserSettingsRepository userSettingsRepository;
    private final PersonaRepository personaRepository; // 需要用它来校验人设是否存在且属于该用户



    @Override
    public void setDefaultPersonaForUser(String userId, String personaId) {
        log.info("用户 '{}' 正在尝试设置默认人设为 '{}'", userId, personaId);

        // 安全校验：确保要设置的人设存在，并且属于当前用户
        Persona persona = personaRepository.findById(personaId)
                .orElseThrow(() -> new PersonaNotFoundException("ID为 '" + personaId + "' 的人设不存在"));
        if (!persona.getUserId().equals(userId)) {
            log.warn("安全警告：用户 '{}' 尝试将不属于自己的人设 '{}' 设置为默认", userId, personaId);
            throw new UnauthorizedActionException("你不能将不属于自己的人设设置为默认");
        }

        // 查找或创建用户的设置记录
        UserSettings settings = userSettingsRepository.findById(userId)
                .orElse(new UserSettings());

        settings.setUserId(userId);
        settings.setDefaultPersonaId(personaId);
        userSettingsRepository.save(settings);
        log.info("用户 '{}' 的默认人设已成功设置为 '{}'", userId, personaId);
    }

    @Override
    public Optional<String> findDefaultPersonaIdForUser(String userId) {
        log.debug("正在为用户 '{}' 查找其默认人设ID", userId);
        return userSettingsRepository.findById(userId)
                .map(UserSettings::getDefaultPersonaId);
    }
}
