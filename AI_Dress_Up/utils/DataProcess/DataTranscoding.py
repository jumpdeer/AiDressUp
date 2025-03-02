import base64
import io
from PIL import Image

# 将图像转换为 Base64 编码
def image_to_base64(image):
    """
    将 PIL.Image 转换为 Base64 字符串
    :param image: PIL.Image 对象
    :return: Base64 编码字符串
    """
    buffer = io.BytesIO()
    image.save(buffer, format="PNG")  # 将图像保存到内存中，格式为 PNG
    buffer.seek(0)
    image_base64 = base64.b64encode(buffer.read()).decode("utf-8")
    return image_base64


# 将 Base64 编码还原为图像
def base64_to_image(base64_string):
    """将 Base64 编码转换为 PIL 图像"""
    image_data = base64.b64decode(base64_string)
    return Image.open(io.BytesIO(image_data))