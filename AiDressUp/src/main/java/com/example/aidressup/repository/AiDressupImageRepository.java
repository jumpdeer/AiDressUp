package com.example.aidressup.repository;

import com.example.aidressup.model.AiDressupImage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AiDressupImageRepository extends JpaRepository<AiDressupImage, Long> {
    @Query(value = "SELECT * FROM ai_dressup_images WHERE user_id = :userId ORDER BY created_at DESC LIMIT :offset, :size", nativeQuery = true)
    List<AiDressupImage> findByUserIdWithPagination(Long userId, int offset, int size);
}
