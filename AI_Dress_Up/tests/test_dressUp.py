import unittest
from torchvision.models.segmentation import deeplabv3_resnet50
import torch.nn as nn
import torch
from utils.DataProcess.EvalPreprocess import preprocess_image,unpad_from_2n_power
import cv2
import numpy as np
from utils.DataProcess.DataTranscoding import image_to_base64,base64_to_image
import requests


class MyTestCase(unittest.TestCase):
    def test_something(self):
        self.assertEqual(True, False)  # add assertion here



    def test_dressUP(self):
        model = deeplabv3_resnet50(pretrained=True, aux_loss=True)
        model.classifier[4] = nn.Conv2d(256, 20, kernel_size=(1, 1))
        model.load_state_dict(torch.load("resource/model/deeplabv3_resnet50.pth", map_location="cuda"))
        model.to("cuda")
        model.eval()

        image_path = "../../temp/test.jpg"

        API_URL = "http://127.0.0.1:7860/sdapi/v1/img2img"
        USERNAME = "AI_dress_up"
        PASSWORD = "123456"




        input_tensor,ori_size = preprocess_image(image_path)

        with torch.no_grad():
            input_tensor = input_tensor.to("cuda")
            output = model(input_tensor)["out"]         # 获取模型输出

            output_resized = unpad_from_2n_power(output,ori_size[1],ori_size[0])
            output_resized = torch.argmax(output_resized.squeeze(), dim=0).cpu().numpy()


        # 提取上衣类别（类别值为 5）
        upper_clothes_mask = (output_resized == 5).astype(np.uint8) * 255

        if np.sum(upper_clothes_mask) == 0:
            print("Warning: No upper clothes detected in the segmentation output.")
        else:
            print("Upper clothes detected. Saving mask...")

        # 保存蒙版
        cv2.imwrite("../../temp/upper_clothes_mask.png", upper_clothes_mask)

        # 加载原图和蒙版
        original_image_path = "../../temp/test.jpg"
        mask_image_path = "../../temp/upper_clothes_mask.png"
        original_image_base64 = image_to_base64(original_image_path)
        mask_image_base64 = image_to_base64(mask_image_path)

        # 请求数据
        payload = {
            "init_images": [original_image_base64],  # 输入的原始图像
            "mask": mask_image_base64,  # 输入的蒙版
            "prompt": "red shirt",  # 重绘提示词
            "steps": 50,  # 生成步数
            "denoising_strength": 0.75,  # 重绘强度（0.0~1.0）
        }

        # 设置认证信息
        auth = (USERNAME, PASSWORD)

        # 发送请求到本地 SD 模型 API
        response = requests.post(API_URL, json=payload, auth=auth)

        # 处理响应
        if response.status_code == 200:
            result_data = response.json()
            result_image = base64_to_image(result_data["images"][0])  # 提取生成的图像
            result_image.save("result_high_res.png")  # 保存生成结果
            print("Generated image saved as 'result_high_res.png'.")
        else:
            print("Error:", response.status_code, response.text)


if __name__ == '__main__':
    unittest.main()
