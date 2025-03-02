package com.example.aidressup.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "ai_dressup_images") // 映射到数据库表
public class AiDressupImage {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY) // 自增主键
    private Long id;

    @Column(nullable = false) // 关联用户 ID
    private Long userId;

    @Column(nullable = false) // 图片文件路径
    private String imageUrl;

    @Transient // 非数据库字段，仅用于动态生成 Base64 数据
    private String imageData;

    @Column(nullable = true)
    private String prompt; // 记录 AI 生成图片的提示词

    @Column(name = "created_at", updatable = false) // 图片创建时间
    private LocalDateTime createdAt = LocalDateTime.now();

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

    public String getImageData() {
        return imageData;
    }

    public void setImageData(String imageData) {
        this.imageData = imageData;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public String getPrompt() {
        return prompt;
    }

    public void setPrompt(String prompt) {
        this.prompt = prompt;
    }

    @Override
    public String toString() {
        return "AiDressupImage{" +
                "id=" + id +
                ", userId=" + userId +
                ", imageUrl='" + imageUrl + '\'' +
                ", createdAt=" + createdAt +
                ", imageData='" + (imageData != null ? "[Base64 data]" : null) + '\'' +
                '}';
    }
}
