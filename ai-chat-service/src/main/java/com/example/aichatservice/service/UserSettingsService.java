package com.example.aichatservice.service;

import java.util.Optional;

public interface UserSettingsService {

    /**
     * 为指定用户设置默认的AI人设。
     *
     * @param userId    用户的ID
     * @param personaId 要设置为默认的人设ID
     */
    void setDefaultPersonaForUser(String userId, String personaId);

    /**
     * 查找指定用户的默认人设ID。
     *
     * @param userId 用户的ID
     * @return 包含默认人设ID的Optional，如果未设置则为空
     */
    Optional<String> findDefaultPersonaIdForUser(String userId);
}
