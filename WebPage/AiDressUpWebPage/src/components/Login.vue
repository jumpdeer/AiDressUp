<template>
  <div class="login-container">
    <h2>Login</h2>
    <form @submit.prevent="handleLogin">
      <div>
        <label for="username">Username:</label>
        <input
          type="text"
          id="username"
          v-model="username"
          placeholder="Enter your username"
          required
        />
      </div>
      <div>
        <label for="password">Password:</label>
        <input
          type="password"
          id="password"
          v-model="password"
          placeholder="Enter your password"
          required
        />
      </div>
      <button type="submit">Login</button>
    </form>
    <p v-if="errorMessage" class="error-message">{{ errorMessage }}</p>
    <p>
      Don't have an account? 
      <a class="link" @click.prevent="goToRegister">Register here</a>
    </p>
  </div>
</template>

<script>
import axios from "axios";

export default {
  name: "Login",
  data() {
    return {
      username: "",
      password: "",
      errorMessage: "",
    };
  },
  methods: {
    async handleLogin() {
      try {
        const response = await axios.post("http://localhost:8080/auth/login", {
          username: this.username,
          password: this.password,
        },
        {
          headers: {"Content-Type": "application/json",}
        });

        console.log(response)
        // 处理后端返回的登录数据
        const { userId, message } = response.data;

        localStorage.setItem("token", "dummy-token"); // 假设后端未来会返回 token
        localStorage.setItem("userId", userId )  // 保存用户Id
        localStorage.setItem("username",this.username);
        

        // 跳转到主页面
        this.$router.push({path: "/main"});

      } catch (error) {
        this.errorMessage = error.response?.data?.error || "Login failed. Please try again.";
        console.error("Login error:", error.response || error.message);
      }
    },
    goToRegister() {
      this.$router.push({ name: "register"});
    },
  }
};
</script>



<style scoped>
/* 保留原有样式 */
.login-container {
  background-color: #ffffff;
  background-size: cover;
  background-position: center;
  border-radius: 12px;
  padding: 40px;
  box-shadow: 0px 8px 20px rgba(0, 0, 0, 0.15);
  min-width: 450px;
  max-width: 500px;
  width: 100%;
}
form div {
  margin-bottom: 20px;
}
input {
  width: 100%;
  padding: 10px;
  border: 1px solid #ddd;
  border-radius: 5px;
  box-sizing: border-box;
}
button {
  width: 100%;
  padding: 12px;
  background-color: #007bff;
  color: white;
  border: none;
  cursor: pointer;
  border-radius: 5px;
  font-size: 16px;
  font-weight: bold;
}
button:hover {
  background-color: #0056b3;
}
.error-message {
  color: red;
  margin-top: 20px;
  text-align: center;
}
.link {
  color: #007bff;
  text-decoration: underline;
  cursor: pointer;
}
.link:hover {
  color: #0056b3;
}
</style>
