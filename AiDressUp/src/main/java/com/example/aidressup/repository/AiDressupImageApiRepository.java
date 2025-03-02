package com.example.aidressup.repository;

import com.example.aidressup.model.AiDressupImageApi;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AiDressupImageApiRepository extends JpaRepository<AiDressupImageApi, Long> {

    // 可以在此添加其他自定义查询方法
}
