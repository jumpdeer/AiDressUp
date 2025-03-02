import requests
from flask import Flask,request,jsonify
from torchvision.models.segmentation import deeplabv3_resnet50
import torch
from utils.DataProcess.EvalPreprocess import preprocess_image,unpad_from_2n_power
from utils.DataProcess.DataTranscoding import image_to_base64,base64_to_image
import numpy as np
import cv2
from PIL import Image

app = Flask(__name__)

# 加载分割模型
def load_segmentation_model(model_path):
    model = deeplabv3_resnet50(pretrained=False, aux_loss=True)
    model.classifier[4] = torch.nn.Conv2d(256, 20, kernel_size=(1, 1))
    model.load_state_dict(torch.load(model_path, map_location="cuda"))
    model.to("cuda")
    model.eval()
    return model


def generate_mask(model,image_path):
    input_tensor,ori_size = preprocess_image(image_path)

    with torch.no_grad():
        input_tensor = input_tensor.to("cuda")
        output = model(input_tensor)['out']

        output_resized = unpad_from_2n_power(output,ori_size[1],ori_size[0])
        output_resized = torch.argmax(output_resized.squeeze(),dim=0).cpu().numpy()

    # 提取上衣类别（类别值为5）, 裤子类别（类别值为9），裙子类别（类别值为6）
    upper_clothes_mask = (output_resized == 5).astype(np.uint8)*255
    pant_mask = (output_resized == 9).astype(np.uint8)*255
    dress_mask = (output_resized == 6).astype(np.uint8)*255

    result_mask = cv2.bitwise_or(upper_clothes_mask,pant_mask)
    result_mask = cv2.bitwise_or(result_mask,dress_mask)


    if np.sum(result_mask) == 0:
        print("Warning: No upper clothes detected in the segmentation output.")
    else:
        print("Upper clothes detected. Saving mask...")

    # **1️⃣ 修复边缘：膨胀 & 腐蚀**
    kernel = np.ones((5, 5), np.uint8)
    refined_mask = cv2.dilate(result_mask, kernel, iterations=1)  # 先膨胀填补缺口
    refined_mask = cv2.erode(refined_mask, kernel, iterations=1)  # 再腐蚀还原边界

    # **2️⃣ 去除小噪点**
    contours, _ = cv2.findContours(refined_mask, cv2.RETR_EXTERNAL, cv2.CHAIN_APPROX_SIMPLE)
    clean_mask = np.zeros_like(refined_mask)
    min_area = 200  # 过滤掉小区域
    for contour in contours:
        if cv2.contourArea(contour) > min_area:
            cv2.drawContours(clean_mask, [contour], -1, 255, thickness=cv2.FILLED)

    # **3️⃣ 平滑蒙版**
    final_mask = cv2.GaussianBlur(clean_mask, (5, 5), 0)

        # 保存蒙版
    cv2.imwrite("./result_mask.png", final_mask)

    return final_mask



def call_sd_api(image, mask, prompt , negative,sampler, sd_url, username, password):
    original_image_base64 = image_to_base64(image)
    mask_image_base64 = image_to_base64(Image.fromarray(mask).convert("L"))

    playload = {
        "init_images": [original_image_base64], # 输入的原始图像
        "mask": mask_image_base64,   # 输入的蒙版
        "prompt": prompt, # 重绘提示词
        "negative_prompt": negative,
        "sampler_name": sampler,
        "steps": 60,  # 生成步数
        "cfg_scale": 9,
        "denoising_strength": 0.75, # 重绘强度(0.0~1.0)
    }

    # 设置认证信息
    auth = (username, password)

    # 发送请求到本地 SD 模型 API
    response = requests.post(sd_url, json=playload, auth=auth)

    if response.status_code == 200:
        result_data = response.json()
        result_image = base64_to_image(result_data["images"][0])  # 提取生成的图像
        result_image.save("result_high_res.png")  # 保存生成结果
        print("Generated image saved as 'result_high_res.png'.")
    else:
        print("Error:", response.status_code, response.text)

    return result_data




@app.route('/api/random',methods=['GET','POST'])
def random_clothes():
    if 'image' not in request.files:
        return jsonify({"error": "Missing image file"}), 400

    # 获取上传的图片文件
    image_file = request.files['image']
        

    model = load_segmentation_model("./static/model/deeplabv3_resnet50.pth")

    image = Image.open(image_file.stream)

    # 生成蒙版
    mask = generate_mask(model,image)


    # 调用 Stable Diffusion 换装
    sd_result = call_sd_api(
        image=image,
        mask=mask,
        prompt="photo-realistic, high fashion, highly detailed, realistic fabric folds, clothing conforms to body shape, smooth transition, matching colors, realistic textile texture, cinematic lighting, 8K, UHD",
        negative="low quality, worst quality, blurry, bad anatomy, unrealistic wrinkles",
        sampler="DPM++ 2M SDE Heun",
        sd_url="http://127.0.0.1:7860/sdapi/v1/img2img",
        username="AI_dress_up",
        password="123456"
    )


    if "images" in sd_result:
        print("success generate")
        # 提取生成的图片并返回
        generated_image_base64 = sd_result["images"][0]
        return jsonify({
            "status":"success",
            "generated_image":generated_image_base64
        })
    else:
        print("failure generate")
        return jsonify({
            "status":"failure",
            "message": sd_result.get("message", "Unknown error")
        })



if __name__ == '__main__':
    app.run()
