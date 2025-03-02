import { createRouter, createWebHistory } from "vue-router";
import Login from "@/components/Login.vue";
import Register from "@/components/Register.vue";
import MainPage from "@/components/MainPage.vue";
import ImageDressUp from "@/components/ImageDressUp.vue"


const routes = [
  { path: "/", name: "login", component: Login },
  { path: "/register", name: "register", component: Register},
  { path: "/main", name: "main", component: MainPage },
  { path: "/add", name: "ImageDressUp", component: ImageDressUp}
];

const router = createRouter({
  history: createWebHistory(),
  routes,
});

// 路由守卫：检查登录状态
router.beforeEach((to, from, next) => {
  const token = localStorage.getItem("token");
  if (to.name !== "login" && !token) {
    next({ name: "login" });
  } else {
    next();
  }
});

export default router;
