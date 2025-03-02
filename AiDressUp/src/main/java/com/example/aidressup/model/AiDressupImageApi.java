package com.example.aidressup.model;

import jakarta.persistence.Entity;
import jakarta.persistence.Table;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "ai_dressup_images_api") // 映射到数据库的表名
public class AiDressupImageApi {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;  // 主键ID

    @Column(name = "user_id", nullable = false)
    private Long userId;  // 关联的用户ID

    @Column(name = "image_url", length = 500)
    private String imageUrl; // 生成的AI换装图像URL

    @Column(name = "top_garment_url", length = 500)
    private String topGarmentUrl; // 上衣图像URL

    @Column(name = "bottom_garment_url", length = 500)
    private String bottomGarmentUrl; // 下装图像URL

    @Column(name = "person_image_url", length = 500)
    private String personImageUrl; // 模特图像URL

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt; // 创建时间

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public String getTopGarmentUrl() {
        return topGarmentUrl;
    }

    public void setTopGarmentUrl(String topGarmentUrl) {
        this.topGarmentUrl = topGarmentUrl;
    }

    public String getBottomGarmentUrl() {
        return bottomGarmentUrl;
    }

    public void setBottomGarmentUrl(String bottomGarmentUrl) {
        this.bottomGarmentUrl = bottomGarmentUrl;
    }

    public String getPersonImageUrl() {
        return personImageUrl;
    }

    public void setPersonImageUrl(String personImageUrl) {
        this.personImageUrl = personImageUrl;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}
