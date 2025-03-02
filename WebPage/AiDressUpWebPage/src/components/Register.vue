<template>
  <div class="register-container">
    <h2>Register</h2>
    <form @submit.prevent="handleRegister">
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
      <div>
        <label for="confirmPassword">Confirm Password:</label>
        <input
          type="password"
          id="confirmPassword"
          v-model="confirmPassword"
          placeholder="Confirm your password"
          required
        />
      </div>
      <button type="submit">Register</button>
    </form>
    <p v-if="errorMessage" class="error-message">{{ errorMessage }}</p>
    <p>
      Already have an account? 
      <a class="link" @click.prevent="goToLogin">Login here</a>
    </p>
  </div>
</template>

<script>
import axios from "axios";

export default {
  name: "Register",
  data() {
    return {
      username: "",
      password: "",
      confirmPassword: "",
      errorMessage: "", // 错误消息
    };
  },
  methods: {
    async handleRegister() {
      if (this.password !== this.confirmPassword) {
        this.errorMessage = "Passwords do not match!";
        return;
      }
      try {
        const response = await axios.post("http://localhost:8080/auth/register", {
          username: this.username,
          password: this.password,
        });
        if (response.data.error) {
          this.errorMessage = response.data.error; // 显示后端返回的错误信息
        } else {
          alert("Registration successful!");
          this.$emit("switch-view", "login");
        }
      } catch (error) {
        this.errorMessage = "Registration failed. Please try again later.";
        console.error("Error:", error);
      }
    },
    goToLogin() {
      this.$router.push({ name: "login"});
    },
  },
};
</script>

<style scoped>
/* 样式保留 */
.register-container {
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
  background-color: #28a745;
  color: white;
  border: none;
  cursor: pointer;
  border-radius: 5px;
  font-size: 16px;
  font-weight: bold;
}
button:hover {
  background-color: #218838;
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
