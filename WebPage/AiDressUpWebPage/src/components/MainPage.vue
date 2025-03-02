<template>
  <div class="main-page">
    <!-- 顶边栏
    <header class="navbar">
      <h1 class="title">AI Dress-Up</h1>
      <div class="user-info">
        <span>Welcome, {{ username }}</span>
      </div>
    </header> -->

    <div class="content-wrapper">
      <!-- 上传图片模块 -->
      <div>
        <div v-if="!uploadedImage" class="common-container" @click="triggerFileInput">
          <div class="placeholder">
            <p>请上传你的图片</p>
          </div>
        </div>
        <div v-else class="image-preview-container" @click="triggerFileInput">
          <img :src="uploadedImage" alt="Uploaded Preview" class="preview-image" />
        </div>
      </div>
      <input type="file" accept="image/*" @change="handleFileUpload" ref="fileInput" hidden />

      <!-- 换装结果展示 -->
      <div>
        <!-- 换装中动画 -->
        <div v-if="isLoading" class="loading-container">
          <div class="loading-animation"></div>
          <p>换装中...</p>
        </div>

        <div v-else-if="!aiResultImage" class="common-container">
          <div class="placeholder">
            <p>换装后的图像将显示在这里</p>
          </div>
        </div>
        <div v-else class="image-preview-container">
          <img :src="aiResultImage" alt="AI Result Image" class="preview-image" />
        </div>
      </div>
    </div>

    

    <!-- 提示词输入框 -->
    <div class="prompt-section">
      <!-- <textarea v-model="promptText" class="prompt-input" placeholder="请输入您的提示词，如‘换成汉服风格’"></textarea> -->
      <button class="dressup-button" @click="applyDressUp">随机换装</button>
    </div>

    <!-- 历史图片展示 -->
    <section class="history-section">
      <h2>Your History</h2>
      <div class="history-grid">
        <div
          v-for="(image, index) in historyImages"
          :key="index"
          class="history-item"
          @click="selectHistoryImage(image)"
        >
          <img :src="image.imageUrl" alt="History Image" />
        </div>
      </div>

      <!-- 分页控制 -->
      <div class="pagination">
        <button @click="fetchPreviousPage" :disabled="currentPage === 0">Previous</button>
        <span>Page {{ currentPage + 1 }}</span>
        <button @click="fetchNextPage" :disabled="historyImages.length < pageSize">Next</button>
      </div>
    </section>
  </div>
</template>

<script>
import axios from "axios"; // 确保引入 axios

export default {
  name: "MainPage",
  data() {
    return {
      username: "User",
      historyImages: [],
      userId: 0,
      currentPage: 0,
      pageSize: 5,
      uploadedImage: null,
      uploadedFile: null,
      aiResultImage: null,
      promptText: "",
      isLoading: false, // 控制换装动画
    };
  },
  methods: {
    loadDataFromStorage() {
      this.token = localStorage.getItem("token");
      this.userId = localStorage.getItem("userId");
      this.username = localStorage.getItem("username") || "User";
      this.currentPage = parseInt(localStorage.getItem("currentPage"), 10) || 0;
      this.pageSize = parseInt(localStorage.getItem("pageSize"), 10) || 5;
    },
    triggerFileInput() {
      this.$refs.fileInput.click();
    },
    handleFileUpload(event) {
      const file = event.target.files[0];
      if (file) {
        this.uploadedFile = file;
        this.uploadedImage = URL.createObjectURL(file);
      }
    },
    handleFileUploadAPI(event){
      const file = event.target.files[0];
      if (file) {
        this.uploadedFileAPI = file;
        this.uploadedImageAPI = URL.createObjectURL(file);
      }
    },
    async gethistory(){

      try{
        let response = await axios.post("http://localhost:8080/upload/history", {
          userId: Number(this.userId),  // **确保 userId 是数值**
          page: this.currentPage,
          size: this.pageSize
        }, {
          headers: {
            "Content-Type": "application/json"
          }
        });

        console.log("🔍 服务器返回数据:", response.data);

        if (response.status === 200 && response.data.success === true) {
          console.log("获取历史记录成功", response.data);
          this.historyImages = response.data.images
          
        } else {
          console.error("获取失败", response);
        }
      } catch (error) {
        console.error("❌ 请求错误", error);
        alert("服务器错误，请检查后端！");
      }
    },
    async applyDressUp() {
      if (!this.uploadedFile) {
        alert("请先上传图片！");
        return;
      }
      // if (!this.promptText.trim()) {
      //   alert("请输入提示词！");
      //   return;
      // }
      if (!this.userId) {
        alert("用户ID未找到，请重新登录！");
        return;
      }

      console.log("🚀 发送换装请求...");
      this.isLoading = true; // 显示换装动画

      let formData = new FormData();
      formData.append("userId", this.userId);  // 用户ID
      formData.append("file", this.uploadedFile);  // 图片文件

      try {
        // 发送 POST 请求到后端
        let response = await axios.post("http://localhost:8080/upload/random", formData, {
          headers: {
            "Content-Type": "multipart/form-data", // 设置请求头
          }
        });

        console.log("🔍 服务器返回数据:", response.data);

        // 判断后端是否返回成功
        if (response.status === 200 && response.data.success === true) {
          console.log("✅ 换装成功", response.data);
          this.aiResultImage = response.data.imageUrl;  // 显示 AI 生成的图片
          console.log(this.aiResultImage)
        } else {
          console.error("❌ 换装失败", response);
          alert(response.data.message || "换装失败，请稍后再试！");
        }
      } catch (error) {
        console.error("❌ 请求错误", error);
        alert("服务器错误，请检查后端！");
      } finally {
        this.isLoading = false; // 关闭加载动画
      }
    },
    async controlnetDressUp(){
      if (!this.uploadedFile) {
        alert("请先上传图片！");
        return;
      }
      if (!this.promptText.trim()) {
        alert("请输入提示词！");
        return;
      }
      if (!this.userId) {
        alert("用户ID未找到，请重新登录！");
        return;
      }

      let formData = new FormData();
      formData.append("userId", this.userId);  // 用户ID
      formData.append("prompt", this.promptText); // prompt
      formData.append("file", this.uploadedFile);  // 图片文件

      try{
        //发送post请求到后端
        let response = await axios.post("http://localhost:8080/upload/image", formData, {
          headers:{
            "Content-Type": "multipart/form-data", // 设置请求头
          }
        })
      }catch(error){

      }
    },
    async fetchNextPage(){
      this.currentPage++;
      try {
        let response = await axios.post("http://localhost:8080/upload/history", {
          userId: Number(this.userId),  // **确保 userId 是数值**
          page: this.currentPage,
          size: this.pageSize
        }, {
          headers: {
            "Content-Type": "application/json"
          }
        })
        console.log("🔍 服务器返回数据:", response.data);

        if (response.status === 200 && response.data.success === true) {
          console.log("切换下一页成功", response.data);
          this.historyImages = response.data.images
          
        } else {
          console.error("获取失败", response);
        }
      } catch(error) {
        console.error("❌ 请求错误", error);
        alert("服务器错误，请检查后端！");
      }
    },
    async fetchPreviousPage(){
      this.currentPage--;
      try {
        let response = await axios.post("http://localhost:8080/upload/history", {
          userId: Number(this.userId),  // **确保 userId 是数值**
          page: this.currentPage,
          size: this.pageSize
        }, {
          headers: {
            "Content-Type": "application/json"
          }
        })
        console.log("🔍 服务器返回数据:", response.data);

        if (response.status === 200 && response.data.success === true) {
          console.log("切换前一页成功", response.data);
          this.historyImages = response.data.images
          
        } else {
          console.error("获取失败", response);
        }
      } catch(error) {
        console.error("❌ 请求错误", error);
        alert("服务器错误，请检查后端！");
      }
    }
  },
  created() {
    this.loadDataFromStorage();
    this.gethistory()
  }
};
</script>

<style scoped>
/* 顶边栏样式 */
.navbar {
  position: fixed;
  top: 0;
  left: 0;
  width: 100%;
  background-color: #007bff;
  color: white;
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 10px 20px;
  z-index: 100;
  box-shadow: 0px 4px 6px rgba(0, 0, 0, 0.1);
}

.title {
  font-size: 24px;
  font-weight: bold;
  flex-grow: 1;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.user-info {
  display: flex;
  justify-content: flex-end;
  min-width: 150px;
  max-width: 250px;
  padding-right: 20px;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
  flex-shrink: 0;
}

/* 主体内容样式 */
.content-wrapper {
  display: flex;
  justify-content: center;
  gap: 50px;
  align-items: flex-start;
  margin-top: 80px;
}

/* 提示词输入框 */
.prompt-section {
  display: flex;
  flex-direction: column;
  align-items: center;
  margin-top: 20px;
  gap: 10px;
}

.prompt-input {
  width: 80%;
  max-width: 400px;
  height: 80px;
  border: 2px solid #ccc;
  border-radius: 8px;
  padding: 10px;
  font-size: 16px;
  resize: none;
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

/* 统一白框样式 */
.common-container {
  width: 300px;
  height: 300px;
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
  width: 300px;
  height: 300px;
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

/* 换装中动画 */
.loading-container {
  width: 300px;
  height: 300px;
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
  width: 50px;
  height: 50px;
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

/* 历史记录整体居中 */
.history-section {
  margin-top: 30px;
  text-align: center;
  display: flex;
  flex-direction: column;
  align-items: center; /* 确保内容水平居中 */
  justify-content: center;
  width: 100%; /* 让历史记录占满整个宽度 */
  padding: 20px; /* 给整个区域一些间距 */
}

/* 历史记录网格居中 */
.history-grid {
  display: flex;
  flex-wrap: wrap;
  gap: 10px;
  justify-content: center; /* 让图片网格居中 */
  align-items: center; /* 垂直居中对齐 */
  width: 100%; /* 保证整个容器占满 */
  margin-top: 20px; /* 给历史图片之间一些间距 */
}

.history-item {
  /* 固定一个缩略图大小 */
  width: 150px;
  height: 150px;
  /* 如果想给每个历史图片加个统一背景或边框，可以在这里加 */
  background-color: #fff;
  border: 1px solid #ccc;
  border-radius: 8px;
  overflow: hidden; /* 若图片超出，会被隐藏 */
  cursor: pointer;
  display: flex; /* 若需要让图片在容器里居中，可用 flex */
  align-items: center;
  justify-content: center;
}

.history-item img {
  /* 在固定尺寸下自适应 */
  max-width: 100%;
  max-height: 100%;
  object-fit: contain; 
  /* or object-fit: cover; 根据需求决定要不要裁剪 */
}

</style>
