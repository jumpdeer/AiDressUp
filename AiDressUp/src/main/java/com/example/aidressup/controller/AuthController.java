package com.example.aidressup.controller;

import com.example.aidressup.model.AiDressupImage;
import com.example.aidressup.model.User;
import com.example.aidressup.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/auth")
public class AuthController {

    @Autowired
    private UserService userService;

    /**
     * 用户注册
     * @param user 包含用户名和密码的用户信息
     * @return 注册成功的消息
     */
    @PostMapping("/register")
    public ResponseEntity<Map<String, String>> register(@RequestBody User user) {
        try {
            userService.register(user);
            Map<String, String> response = new HashMap<>();
            response.put("message", "User registered successfully!");
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            Map<String, String> errorResponse = new HashMap<>();
            errorResponse.put("error", e.getMessage());
            return ResponseEntity.badRequest().body(errorResponse);
        }
    }

    /**
     * 用户登录
     * @param request 包含用户名和密码的登录请求
     * @param page 请求的页码，默认值为 0
     * @param size 每页图片数量，默认值为 5
     * @return 登录成功的消息和分页图片列表
     */
    @PostMapping("/login")
    public ResponseEntity<Map<String, Object>> login(@RequestBody Map<String, String> request,
                                                     @RequestParam(defaultValue = "0") int page,
                                                     @RequestParam(defaultValue = "5") int size) {
        try {
            String username = request.get("username");
            String password = request.get("password");

            // 登录验证
            User user = userService.login(username, password);


            // 获取用户id
            long id = user.getId();

            // 构建返回响应
            Map<String, Object> response = new HashMap<>();
            response.put("userId", id);   // 返回用户 ID
            response.put("message", "Login successful!"); // 登录成功的消息

            return ResponseEntity.ok(response);

        } catch (RuntimeException e) {
            // 登录失败时，返回错误消息
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("error", e.getMessage());
            return ResponseEntity.status(401).body(errorResponse); // 未授权错误
        }
    }
}
