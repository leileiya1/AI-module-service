package com.example.aichatservice.repository;

import com.example.aichatservice.entity.Persona;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Persona 数据访问接口
 * 继承 MongoRepository，自动获得对 Persona 对象的 CRUD (增删改查) 功能。
 * Spring Data MongoDB 会在运行时自动为这个接口创建实现。
 */
@Repository
public interface PersonaRepository extends MongoRepository<Persona, String> {
    /**
     * 新增方法：根据用户ID查找所有属于该用户的人设。
     * Spring Data会根据方法名自动实现这个查询。
     *
     * @param userId 用户的唯一ID
     * @return 该用户创建的人设列表
     */
    List<Persona> findByUserId(String userId);
}

