package com.example.aidressup.util;

import com.aliyun.oss.OSS;
import com.aliyun.oss.OSSClientBuilder;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import java.io.ByteArrayInputStream;
import java.util.UUID;

@Component
public class AliyunOSSUtil {

    @Value("${aliyun.oss.endpoint}")
    private String endpoint;

    @Value("${aliyun.oss.accessKeyId}")
    private String accessKeyId;

    @Value("${aliyun.oss.accessKeySecret}")
    private String accessKeySecret;

    @Value("${aliyun.oss.bucketName}")
    private String bucketName;

    /**
     * 上传文件到 OSS 并返回公网访问 URL
     */
    public String uploadImage(byte[] imageBytes, String fileName) {


        // 创建 OSS 客户端
        OSS ossClient = new OSSClientBuilder().build(endpoint, accessKeyId, accessKeySecret);

        try {
            // 上传图片
            ossClient.putObject(bucketName, fileName, new ByteArrayInputStream(imageBytes));

            // 生成公网 URL
            return "https://" + bucketName + "." + endpoint + "/" + fileName;
        } finally {
            ossClient.shutdown();
        }
    }
}
