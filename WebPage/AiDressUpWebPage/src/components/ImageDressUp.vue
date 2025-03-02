<template>
    <div class="image-dressup-page">
      
  
      <div class="content-wrapper">
        <!-- 模特图片上传 -->
        <div>
          <div v-if="!modelPreview" class="common-container" @click="triggerModelUpload">
            <div class="placeholder">
              <p>点击上传模特图片</p>
            </div>
          </div>
          <div v-else class="image-preview-container" @click="triggerModelUpload">
            <img :src="modelPreview" alt="Model Preview" class="preview-image" />
          </div>
        </div>
        <input
          type="file"
          accept="image/*"
          ref="modelInput"
          @change="handleModelSelected"
          hidden
        />
  
        <!-- 上衣图片上传 -->
        <div>
          <div v-if="!topPreview" class="common-container" @click="triggerTopUpload">
            <div class="placeholder">
              <p>点击上传上衣图片</p>
            </div>
          </div>
          <div v-else class="image-preview-container" @click="triggerTopUpload">
            <img :src="topPreview" alt="Top Preview" class="preview-image" />
          </div>
        </div>
        <input
          type="file"
          accept="image/*"
          ref="topInput"
          @change="handleTopSelected"
          hidden
        />
  
        <!-- 下装图片上传 -->
        <div>
          <div v-if="!bottomPreview" class="common-container" @click="triggerBottomUpload">
            <div class="placeholder">
              <p>点击上传下装图片</p>
            </div>
          </div>
          <div v-else class="image-preview-container" @click="triggerBottomUpload">
            <img :src="bottomPreview" alt="Bottom Preview" class="preview-image" />
          </div>
        </div>
        <input
          type="file"
          accept="image/*"
          ref="bottomInput"
          @change="handleBottomSelected"
          hidden
        />
  
        <!-- 换装结果展示 -->
        <div>
          <!-- 如果正在加载，就显示换装动画 -->
          <div v-if="isLoading" class="loading-container">
            <div class="loading-animation"></div>
            <p>换装中...</p>
          </div>
  
          <!-- 未加载完成，也未生成结果：显示占位框 -->
          <div v-else-if="!resultPreview" class="common-container">
            <div class="placeholder">
              <p>换装后的图像将显示在这里</p>
            </div>
          </div>
  
          <!-- 已生成结果：显示生成后的图片 -->
          <div v-else class="image-preview-container">
            <img :src="resultPreview" alt="Result Preview" class="preview-image" />
          </div>
        </div>
      </div>
  
      <!-- 换装按钮 -->
      <div class="prompt-section">
        <button class="dressup-button" @click="handleDressUp">
          开始换装
        </button>
      </div>
    </div>
  </template>
  
  <script>
  import axios from "axios";
  
  export default {
    name: "ImageDressUp",
    data() {
      return {
        userId: null,
        isLoading: false,
  
        // 模特、上衣、下装文件 & 预览
        modelFile: null,
        modelPreview: null,
  
        topFile: null,
        topPreview: null,
  
        bottomFile: null,
        bottomPreview: null,
  
        // 换装结果
        resultPreview: null,
      };
    },
    mounted() {
      // 载入用户信息
      this.userId = localStorage.getItem("userId");
    },
    methods: {
      // ---- 触发文件选择 ----
      triggerModelUpload() {
        this.$refs.modelInput.click();
      },
      triggerTopUpload() {
        this.$refs.topInput.click();
      },
      triggerBottomUpload() {
        this.$refs.bottomInput.click();
      },
  
      // ---- 获取文件并生成预览 ----
      handleModelSelected(e) {
        const file = e.target.files[0];
        if (file) {
          this.modelFile = file;
          this.modelPreview = URL.createObjectURL(file);
        }
      },
      handleTopSelected(e) {
        const file = e.target.files[0];
        if (file) {
          this.topFile = file;
          this.topPreview = URL.createObjectURL(file);
        }
      },
      handleBottomSelected(e) {
        const file = e.target.files[0];
        if (file) {
          this.bottomFile = file;
          this.bottomPreview = URL.createObjectURL(file);
        }
      },
  
      // ---- 处理换装请求 ----
      async handleDressUp() {
        // 校验
        if (!this.userId) {
          alert("用户未登录，请先登录。");
          return;
        }
        if (!this.modelFile || !this.topFile) {
          alert("请确保已上传：模特图片、上衣图片、下装图片");
          return;
        }
  
        this.isLoading = true;
        this.resultPreview = null; // 清空上次结果
  
        let formData = new FormData();
        formData.append("userId", this.userId);
        formData.append("modelImage", this.modelFile);
        formData.append("topImage", this.topFile);
        formData.append("bottomImage", this.bottomFile);
  
        try {
          // 根据你的后端接口定义，如果接口地址是 /upload/combine 或 /upload/clothes 等，
          // 这里可自行修改
          let res = await axios.post(
            "http://localhost:8080/upload/combine",
            formData,
            {
              headers: {
                "Content-Type": "multipart/form-data",
              },
            }
          );
  
          if (res.status === 200 && res.data.success) {
            // 显示生成结果
            this.resultPreview = res.data.imageUrl;
          } else {
            alert(res.data.message || "换装失败，请稍后重试。");
          }
        } catch (error) {
          console.error("换装请求错误: ", error);
          alert("换装请求失败，请检查后端是否可用。");
        } finally {
          this.isLoading = false;
        }
      },
    },
  };
  </script>
  
  <style scoped>
  .image-dressup-page {
    /* 让内部内容相对独立 */
    width: 100%;
    display: flex;
    flex-direction: column;
    align-items: center;
  }
  
  /* 子导航栏样式（若不需要，可删除） */
  .navbar-sub {
    margin-top: 80px; /* 若顶栏是固定的，这里留出高度 */
    background-color: #f7f7f7;
    width: 100%;
    text-align: center;
    padding: 10px 0;
    border-bottom: 1px solid #ccc;
  }
  
  /* 内容容器，放置 3 个上传框 + 1 个结果区域 */
  .content-wrapper {
    display: grid;
    grid-template-columns: repeat(4, 1fr);
    gap: 30px;
    margin-top: 40px; /* 让页面更美观 */
    justify-items: center; /* 水平居中 */
    align-items: start; /* 垂直顶部对齐 */
  }
  
  /* 和 MainPage 的样式保持一致 */
  .common-container {
    width: 250px;
    height: 250px;
    background-color: white;
    border: 2px dashed #ccc;
    display: flex;
    justify-content: center;
    align-items: center;
    border-radius: 10px;
    box-shadow: 0px 4px 6px rgba(0, 0, 0, 0.1);
    cursor: pointer;
  }
  .common-container:hover {
    border-color: #007bff;
  }
  
  .image-preview-container {
    width: 250px;
    height: 250px;
    display: flex;
    justify-content: center;
    align-items: center;
    background-color: white;
    border-radius: 10px;
    box-shadow: 0px 4px 6px rgba(0, 0, 0, 0.1);
    cursor: pointer;
  }
  
  .preview-image {
    width: auto;
    height: auto;
    max-width: 95%;
    max-height: 95%;
    object-fit: contain;
    border-radius: 10px;
  }
  
  /* 占位内容 */
  .placeholder p {
    color: #888;
    font-size: 16px;
    text-align: center;
  }
  
  /* 换装中动画 */
  .loading-container {
    width: 250px;
    height: 250px;
    background-color: white;
    border-radius: 10px;
    display: flex;
    justify-content: center;
    align-items: center;
    flex-direction: column;
    box-shadow: 0px 4px 6px rgba(0, 0, 0, 0.1);
    animation: pulse 1.5s infinite alternate ease-in-out;
  }
  
  @keyframes pulse {
    0% {
      background-color: #ffffff;
    }
    50% {
      background-color: #f0f0f0;
    }
    100% {
      background-color: #ffffff;
    }
  }
  
  .loading-animation {
    width: 40px;
    height: 40px;
    border: 5px solid #007bff;
    border-top-color: transparent;
    border-radius: 50%;
    animation: spin 1s linear infinite;
  }
  
  @keyframes spin {
    100% {
      transform: rotate(360deg);
    }
  }
  
  /* 按钮 */
  .prompt-section {
    margin-top: 40px;
  }
  
  .dressup-button {
    padding: 12px 20px;
    background-color: #007bff;
    color: white;
    border: none;
    border-radius: 5px;
    cursor: pointer;
    font-size: 16px;
    transition: background 0.2s;
  }
  .dressup-button:hover {
    background-color: #0056b3;
  }
  </style>
  