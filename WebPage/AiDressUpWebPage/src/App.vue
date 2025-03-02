<template>
  <div id="app">
    <!-- 粒子背景 -->
    <div id="particle-bg"></div>

    <!-- 顶边栏 -->
    <header class="navbar">
      <h1 class="title">AI Dress-Up</h1>
      <div class="user-info">
        <span>Welcome, {{ username }}</span>
      </div>
    </header>

    <!-- 左侧按钮容器：只有登录后 (isLoggedIn = true) 才显示 -->
    <div class="left-button-container" v-if="isLoggedIn">
      <!-- 两个功能按钮，使用 <router-link> 跳转 -->
      <router-link to="/main" class="function-button">随机换装</router-link>
      <router-link to="/add" class="function-button">图片换装</router-link>
      
    </div>

    <!-- 内容容器：路由视图 -->
    <div id="content">
      <router-view :key="$route.fullPath"></router-view>
    </div>
  </div>
</template>

<script>
export default {
  name: "App",
  data() {
    return {
      isLoggedIn: false,
      username: "",
    };
  },
  mounted() {
    // 粒子背景逻辑 (与之前相同)
    const particleCount = 150;
    const particleBg = document.getElementById("particle-bg");
    for (let i = 0; i < particleCount; i++) {
      const particle = document.createElement("div");
      particle.classList.add("particle");
      particle.style.top = `${Math.random() * 100}vh`;
      particle.style.left = `${Math.random() * 100}vw`;
      particle.style.animationDuration = `${Math.random() * 10 + 5}s`;
      particle.style.animationDelay = `${Math.random() * 5}s`;
      particleBg.appendChild(particle);
    }

    // 初次检查登录状态
    this.checkLogin();
  },
  watch: {
    // 路由变化时重新检查登录状态
    "$route.fullPath"() {
      this.checkLogin();
    },
  },
  methods: {
    checkLogin() {
      const userId = localStorage.getItem("userId");
      if (userId) {
        this.isLoggedIn = true;
        this.username = localStorage.getItem("username") || "User";
      } else {
        this.isLoggedIn = false;
        this.username = "";
      }
    },
    logout() {
      // 清除 localStorage
      localStorage.removeItem("userId");
      localStorage.removeItem("username");
      // 更新状态并跳转到登录页
      this.isLoggedIn = false;
      this.username = "";
      this.$router.push("/login");
    },
  },
  created(){
    localStorage.removeItem("userId");
    localStorage.removeItem("username");
  }
};
</script>

<style>
/* 保持你原先的背景与粒子动画样式不变 */

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

/* 全局样式 */
body, html {
  margin: 0;
  padding: 0;
  width: 100%;
  height: 100%;
  overflow: hidden;
}

#app {
  position: relative;
  width: 100%;
  height: 100%;
}

/* 粒子背景容器 */
#particle-bg {
  position: fixed;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  background: linear-gradient(135deg, #ff7eb3, #ff758c, #ffdf7e);
  background-size: 400% 400%;
  animation: gradientShift 10s ease infinite;
  z-index: -1; /* 确保背景位于最底层 */
}

/* 动态渐变动画 */
@keyframes gradientShift {
  0% {
    background-position: 0% 50%;
  }
  50% {
    background-position: 100% 50%;
  }
  100% {
    background-position: 0% 50%;
  }
}

/* 粒子样式 */
.particle {
  position: absolute;
  width: 10px;
  height: 10px;
  background: rgba(255, 255, 255, 0.6);
  border-radius: 50%;
  animation: moveParticles 10s linear infinite;
}

@keyframes moveParticles {
  0% {
    transform: translateY(0) translateX(0);
  }
  100% {
    transform: translateY(100vh) translateX(calc(100vw - 10px));
  }
}

/* ========== 左侧按钮容器样式 ========== */
.left-button-container {
  position: fixed;
  left: 20px;
  top: 50%;
  transform: translateY(-50%);
  display: flex;
  flex-direction: column; /* 垂直排列 */
  align-items: center;
  gap: 20px;
  z-index: 999;
}

/* 功能按钮 */
.function-button {
  display: inline-block;
  padding: 12px 16px;
  color: #fff;
  background-color: #007bff;
  text-decoration: none;
  font-size: 16px;
  border-radius: 8px;
  text-align: center;
  transition: background 0.3s;
  min-width: 120px; /* 保证按钮有一定宽度 */
}
.function-button:hover {
  background-color: #0056b3;
}

/* 登出按钮（可选） */
.logout-button {
  padding: 12px 16px;
  background-color: #dc3545;
  color: #fff;
  border: none;
  cursor: pointer;
  border-radius: 8px;
  font-size: 16px;
}
.logout-button:hover {
  background-color: #c82333;
}

/* 内容容器 */
#content {
  position: relative;
  z-index: 1; /* 内容在背景之上 */
  width: 100%;
  height: 100%;
  display: flex;
  justify-content: center;
  align-items: center;
}
</style>
