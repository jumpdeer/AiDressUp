package com.example.aidressup.service;

import com.example.aidressup.model.AiDressupImageApi;
import com.example.aidressup.repository.AiDressupImageApiRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class AiDressupImageApiService {

    @Autowired
    private AiDressupImageApiRepository aiDressupImageApiRepository;

    /**
     * 保存用户换装图像记录
     *
     * @param aiDressupImageApi 要保存的实体对象
     * @return 保存后的实体
     */
    public AiDressupImageApi saveAiDressupImage(AiDressupImageApi aiDressupImageApi) {
        return aiDressupImageApiRepository.save(aiDressupImageApi);
    }

}
