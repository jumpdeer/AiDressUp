package com.example.aidressup.controller;

import com.example.aidressup.model.AiDressupImage;
import com.example.aidressup.model.AiDressupImageApi;
import com.example.aidressup.repository.AiDressupImageApiRepository;
import com.example.aidressup.repository.AiDressupImageRepository;
import com.example.aidressup.util.AliyunOSSUtil;
import org.apache.commons.codec.binary.Base64;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.FileSystemResource;
import org.springframework.http.*;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

import java.io.InputStream;
import java.net.URL;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@CrossOrigin(origins = "http://localhost:5173") // 允许前端访问
@RestController
@RequestMapping("/upload")
public class UploadController {

    @Autowired
    private AliyunOSSUtil aliyunOSSUtil;


    private static final String AI_SERVER_URL = "http://127.0.0.1:5000/api/random"; // Flask AI 服务器地址
    private static final String ALIYUN_AI_API_URL = "https://dashscope.aliyuncs.com/api/v1/services/aigc/image2image/image-synthesis";
    private static final String API_KEY = "sk-6dbdba14a72148c7aa2a0af558eb2173";  // 替换为你的阿里云 API Key

    @Autowired
    private AiDressupImageRepository aiDressupImageRepository;

    @Autowired
    private AiDressupImageApiRepository aiDressupImageApiRepository;



    /**
     * 用户随机生成图片
     */
    @PostMapping("/random")
    public ResponseEntity<Map<String, Object>> randomClothes(
            @RequestParam("userId") Long userId,
            @RequestParam("file") MultipartFile file) {

        Map<String, Object> response = new HashMap<>();

        try {

            // **生成唯一文件名**
            String originalFileName = UUID.randomUUID().toString() + "_original_" + file.getOriginalFilename();


            // 上传原始图片到阿里云 OSS
            String ossUrl = aliyunOSSUtil.uploadImage(file.getBytes(), "random/original/user_" + userId + "/" + originalFileName);

            String base64Image = encodeImageToBase64(file);

            // **调用 Flask AI 服务器进行处理**
            String processedImageUrl = sendToAIServer(file, userId);

            // **如果 AI 处理失败**
            if (processedImageUrl == null) {
                response.put("success", false);
                response.put("message", "AI processing failed");
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
            }

            // **存储 AI 处理后的图片到数据库**
            AiDressupImage image = new AiDressupImage();
            image.setUserId(userId);  // 设置关联的用户 ID
            image.setImageUrl(processedImageUrl); // 存储图片URL
            image.setPrompt(""); // 设置提示词
            image.setCreatedAt(LocalDateTime.now()); // 设置创建时间



            aiDressupImageRepository.save(image); // 保存到数据库

            response.put("success", true);
            response.put("message", "Image processed successfully!");
            response.put("imageUrl", processedImageUrl);  // 返回图片URL给前端
            return ResponseEntity.ok(response);

        } catch (IOException e) {
            e.printStackTrace();
            response.put("success", false);
            response.put("message", "Image upload failed: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }



    /**
     * 获取用户的历史图片记录
     */
    @PostMapping("/history")
    public ResponseEntity<Map<String, Object>> getHistory(@RequestBody Map<String, Object> requestBody) {

        Map<String, Object> response = new HashMap<>();
        System.out.println("获取历史记录");
        try {
            // **确保 userId 是 Long 类型**
            Object userIdObj = requestBody.get("userId");
            Long userId;

            if (userIdObj instanceof Integer) {
                userId = ((Integer) userIdObj).longValue();
            } else if (userIdObj instanceof String) {
                userId = Long.parseLong((String) userIdObj);
            } else {
                throw new IllegalArgumentException("Invalid userId format");
            }
            int page = requestBody.get("page") != null ? (int) requestBody.get("page") : 0;
            int size = requestBody.get("size") != null ? (int) requestBody.get("size") : 5;

            System.out.println("获取历史记录: userId=" + userId + ", page=" + page + ", size=" + size);


            // 获取用户历史图片记录，分页
            List<AiDressupImage> images = aiDressupImageRepository.findByUserIdWithPagination(userId, page * size, size);

            // 构建返回响应
            response.put("userId", userId);
            response.put("images", images); // 返回历史图片 URL
            response.put("page", page);
            response.put("size", size);
            response.put("success",true);
            response.put("message", "Images fetched successfully!");
            return ResponseEntity.ok(response);

        } catch (Exception e) {
            e.printStackTrace();
            response.put("success", false);
            response.put("message", "Failed to fetch images: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    /*
    * 调用阿里云的ai换装接口
    * */
    @PostMapping("/combine")
    public ResponseEntity<Map<String, Object>> CombineClothes(
            @RequestParam("userId") Long userId,
            @RequestParam("modelImage") MultipartFile model,
            @RequestParam("topImage") MultipartFile top,
            @RequestParam(value = "bottomImage", required = false) MultipartFile bottom){

        Map<String, Object> response = new HashMap<>();
        try{
            // **生成唯一文件名**
            String originalModel = UUID.randomUUID().toString() + "_original_Model" + model.getOriginalFilename();  // 模特原始图片
            String originalTop = UUID.randomUUID().toString() + "_original_Top" + top.getOriginalFilename();  // 上衣图
            String bottomUrl = "";


            // 上传原始图片到阿里云 OSS
            String modelUrl = aliyunOSSUtil.uploadImage(model.getBytes(), "aliApi/original/user_" + userId + "/model/" + originalModel );
            String topUrl = aliyunOSSUtil.uploadImage(top.getBytes(), "aliApi/original/user_" + userId + "/top/" + originalTop);

            
            // ** 调用阿里云api接口进行处理 ** //
            if (bottom!=null) {
                String originalBottom = UUID.randomUUID().toString() + "_original_Bottom" + bottom.getOriginalFilename(); // 下装图
                bottomUrl = aliyunOSSUtil.uploadImage(bottom.getBytes(), "aliApi/original/user_" + userId + "/bottom/" + originalBottom);
            }

            String task_id = callAliyunAiApi(topUrl,bottomUrl,modelUrl);

            // **如果 AI 处理失败**
            if (task_id == null) {
                response.put("success", false);
                response.put("message", "AI processing failed");
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
            }

            // 轮询任务状态，直到处理完成
            String processedImageUrl = pollTaskStatus(task_id);

            if(processedImageUrl == null) {
                response.put("success", false);
                response.put("message", "AI processing failed");
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
            }

            String ossUrl = aliyunOSSUtil.uploadImage(downloadImageAsBytes(processedImageUrl), "aliApi/processed/user_" + userId + "/" + UUID.randomUUID() + "_processed.jpg");

            // **存储 阿里云api 处理后的图片到数据库**
            AiDressupImageApi imageApi = new AiDressupImageApi();
            imageApi.setUserId(userId);
            imageApi.setPersonImageUrl(modelUrl);
            imageApi.setTopGarmentUrl(topUrl);
            imageApi.setBottomGarmentUrl(bottomUrl);
            imageApi.setImageUrl(ossUrl);
            imageApi.setCreatedAt(LocalDateTime.now());

            aiDressupImageApiRepository.save(imageApi); // 保存到数据库

            response.put("success", true);
            response.put("message", "Image processed successfully!");
            response.put("imageUrl", ossUrl);  // 返回图片URL给前端
            return ResponseEntity.ok(response);


        } catch (IOException e) {
            e.printStackTrace();
            response.put("success", false);
            response.put("message", "Image upload failed: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }

    }


    /**
     * 发送图片到 AI 服务器，接收处理后的图片 URL
     */
    private String sendToAIServer(MultipartFile file, Long userId) {
        try {
            RestTemplate restTemplate = new RestTemplate();

            // **创建请求体**
            MultiValueMap<String, Object> requestBody = new LinkedMultiValueMap<>();
            requestBody.add("image", new ByteArrayResource(file.getBytes()) {
                @Override
                public String getFilename() {
                    return file.getOriginalFilename();  // 保持文件原始名称
                }
            });

            // **设置请求头**
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.MULTIPART_FORM_DATA);

            HttpEntity<MultiValueMap<String, Object>> requestEntity = new HttpEntity<>(requestBody, headers);

            // **发送请求到 Flask AI 服务器**
            ResponseEntity<Map> responseEntity = restTemplate.exchange(
                    AI_SERVER_URL,
                    HttpMethod.POST,
                    requestEntity,
                    Map.class
            );

            // **解析 Flask 服务器的返回值**
            Map<String, Object> responseBody = responseEntity.getBody();

            if (responseEntity.getStatusCode() == HttpStatus.OK && responseBody != null) {
                String status = (String) responseBody.get("status");

                if ("success".equals(status)) {
                    // **获取生成的 Base64 图片**
                    String base64ProcessedImage = (String) responseBody.get("generated_image");

                    // **存储到阿里云 OSS**
                    return saveBase64ImageToOSS(base64ProcessedImage, userId);
                }
            }
            return null;

        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }


    /**
     * 调用阿里云 AI 换装 API
     */
    private String callAliyunAiApi(String topGarmentUrl, String bottomGarmentUrl, String personImageUrl) {
        try {
            RestTemplate restTemplate = new RestTemplate();

            // **设置请求头**
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.set("Authorization", API_KEY);
            headers.set("X-DashScope-Async", "enable");  // 启用异步模式

            // **构造请求体**
            Map<String, Object> requestBody = new HashMap<>();
            requestBody.put("model", "aitryon");

            Map<String, Object> input = new HashMap<>();
            input.put("top_garment_url", topGarmentUrl);
            input.put("bottom_garment_url", bottomGarmentUrl.isEmpty() ? null : bottomGarmentUrl);  // 为空时不发送该字段
            input.put("person_image_url", personImageUrl);
            requestBody.put("input", input);

            Map<String, Object> parameters = new HashMap<>();
            parameters.put("resolution", -1);  // 还原到原始图片大小
            parameters.put("restore_face", true);  // 还原人脸
            requestBody.put("parameters", parameters);


            // **创建请求**
            HttpEntity<Map<String, Object>> requestEntity = new HttpEntity<>(requestBody, headers);

            // **发送 POST 请求**
            ResponseEntity<Map> responseEntity = restTemplate.exchange(
                    ALIYUN_AI_API_URL,
                    HttpMethod.POST,
                    requestEntity,
                    Map.class
            );


            // **解析返回值**
            Map<String, Object> responseBody = responseEntity.getBody();


            if (responseBody != null && responseEntity.getStatusCode() == HttpStatus.OK) {
                Map<String, Object> outputMap = (Map<String, Object>) responseBody.get("output");

                return (String) outputMap.get("task_id"); // AI 生成的图片 task_id
            }
            return null;

        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * 将 Base64 图片保存到阿里云，并返回服务器 URL
     */
    private String saveBase64ImageToOSS(String base64Image, Long userId) {
        try {
            // 解码 Base64
            byte[] decodedBytes = Base64.decodeBase64(base64Image);

            // 生成唯一文件名
            String processedFileName = "random/processed/user_" + userId + "/" + UUID.randomUUID() + "_processed.jpg";

            // 上传到阿里云 OSS
            return aliyunOSSUtil.uploadImage(decodedBytes, processedFileName);

        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * 将图片转换为 Base64 编码
     */
    private String encodeImageToBase64(MultipartFile file) throws IOException {
        byte[] fileBytes = file.getBytes();
        return Base64.encodeBase64String(fileBytes);
    }

    /**
     * 从给定图片URL下载图片，并转为字节数组
     *
     * @param imageUrl 图片链接
     * @return 图片对应的字节数组
     * @throws IOException 读取流失败则抛出异常
     */
    public static byte[] downloadImageAsBytes(String imageUrl) throws IOException {
        URL url = new URL(imageUrl);

        try (InputStream inputStream = url.openStream();
             ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {

            byte[] buffer = new byte[4096];
            int bytesRead;

            while ((bytesRead = inputStream.read(buffer)) != -1) {
                outputStream.write(buffer, 0, bytesRead);
            }
            return outputStream.toByteArray();
        }

    }

    private String pollTaskStatus(String taskId) {
        try{
            String statusUrl = "https://dashscope.aliyuncs.com/api/v1/tasks/"+taskId;
            Map<String, String> requestBody = new HashMap<>();

            HttpHeaders headers = new HttpHeaders();
            headers.set("Authorization", "Bearer " + API_KEY);


            RestTemplate restTemplate = new RestTemplate();
            HttpEntity<Map<String, String>> requestEntity = new HttpEntity<>(requestBody, headers);

            while (true) {

                ResponseEntity<Map> responseEntity = restTemplate.exchange(
                        statusUrl,
                        HttpMethod.GET,
                        requestEntity,
                        Map.class
                );

                Map<String, Object> responseBody = responseEntity.getBody();
                Map<String, Object> outputMap = (Map<String, Object>) responseBody.get("output");


                if (responseEntity.getStatusCode() == HttpStatus.OK && outputMap != null){
                    String status = (String) outputMap.get("task_status");

                    if("SUCCEEDED".equals(status)){
                        return (String) outputMap.get("image_url");
                    }else if("FAILED".equals(status)){
                        return null; //任务失败
                    }
                }

                //
                Thread.sleep(500);
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

}
