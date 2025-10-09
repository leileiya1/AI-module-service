package com.example.aichatservice.repository;

import com.example.aichatservice.setting.UserSettings;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

/**
 * UserSettings 数据访问接口。
 * Spring Data MongoDB 会自动实现基本的CRUD操作。
 */
@Repository
public interface UserSettingsRepository extends MongoRepository<UserSettings, String> {
}
