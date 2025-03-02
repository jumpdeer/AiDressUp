import math
import torch
from PIL import Image
from torchvision import transforms
import torch.nn.functional as F


def next_power_of_2(x):
    """
    返回 >= x 的最小 2 的幂。
    例如 x=700 -> 1024, x=512 -> 512。
    """
    return 1 if x == 0 else 2 ** math.ceil(math.log2(x))

#  将图片和标签填充到2的n次幂
def paddingTo2n(data:torch.Tensor,pad_value_img: float=0.0):
    """
    对图像和标签一起进行四边方向的padding，使其高宽变为2的幂大小。

    参数:
      image: [C, H, W]  (例如3通道的RGB)
      pad_value_img: 对图像填充值, 默认为0.0(黑色)

    返回:
      padded_image: shape = [C, H_pad, W_pad]
    """


    im_height,im_width = data.shape[1],data.shape[2]

    # 计算下一个 >= H,W 的 2^n 尺寸
    H_pad = next_power_of_2(im_height)
    W_pad = next_power_of_2(im_width)

    # 需要 pad 的差值
    h_diff = H_pad - im_height
    w_diff = W_pad - im_width

    # 上下左右的 pad 分配
    top = h_diff // 2
    bottom = h_diff - top
    left = w_diff // 2
    right = w_diff - left

    # 图像4方向pad (left, right, top, bottom)
    padded_image = F.pad(
        data,
        pad=(left, right, top, bottom),
        mode='constant',
        value=pad_value_img
    )

    return padded_image


# 输入的参数为torch.Tensor，用于在模型输出之后进行的裁剪
def unpad_from_2n_power(padded:torch.Tensor,orig_height:int,orig_width:int):
    """
        仅需知道原图高宽 (orig_height, orig_width)，
        即可从对称pad的结果自动反向裁剪。
        padded: [C, H_pad, W_pad]
    """

    _,_, H_pad, W_pad = padded.shape


    # 计算差值
    h_diff = H_pad - orig_height
    w_diff = W_pad - orig_width

    # 对半分配
    top = h_diff // 2
    bottom = h_diff - top
    left = w_diff // 2
    right = w_diff - left

    # 切片
    unpadded = padded[:,:, top:(H_pad - bottom), left:(W_pad - right)]
    return unpadded


#
def preprocess_image(img):
    # img = Image.open(image_path).convert("RGB")
    ori_size = img.size

    preprocess = transforms.Compose([
        transforms.ToTensor(),
        transforms.Normalize(mean=[0.485, 0.456, 0.406], std=[0.229, 0.224, 0.225]),
    ])
    img_tensor = paddingTo2n(preprocess(img)).unsqueeze(0)  # 增加 batch 维度
    return img_tensor,ori_size