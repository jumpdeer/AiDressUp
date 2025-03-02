package com.example.aidressup.service;

import com.example.aidressup.model.AiDressupImage;
import com.example.aidressup.model.User;
import com.example.aidressup.repository.AiDressupImageRepository;
import com.example.aidressup.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class UserService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private AiDressupImageRepository aiDressupImageRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    /**
     * 用户注册
     */
    public User register(User user) {
        // 检查用户名是否已存在
        if (userRepository.findByUsername(user.getUsername()).isPresent()) {
            throw new IllegalArgumentException("Username already exists");
        }
        // 加密密码
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        return userRepository.save(user);
    }

    public User login(String username, String password) {
        return userRepository.findByUsername(username)
                .filter(user -> passwordEncoder.matches(password, user.getPassword()))
                .orElseThrow(() -> new RuntimeException("Invalid username or password"));
    }

    /**
     * 获取用户的分页图片列表（返回图片路径）
     */
    public List<AiDressupImage> getUserImagesWithPagination(Long userId, int page, int size) {
        // 根据分页查询用户的图片
        List<AiDressupImage> images = aiDressupImageRepository.findByUserIdWithPagination(userId, page * size, size);

        // 设置图片路径
        images.forEach(image -> {
            String imagePath = image.getImageUrl();  // 直接返回存储路径
            image.setImageUrl(imagePath);  // 更新为路径（如果需要可进一步处理）
        });

        return images;
    }

    public List<AiDressupImage> getUserUploadedImages(Long userId, int page, int size) {
        return aiDressupImageRepository.findByUserIdWithPagination(userId, page * size, size);
    }
}
