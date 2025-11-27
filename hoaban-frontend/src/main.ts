import { createApp } from "vue";
import { createPinia } from "pinia";
import App from "./App.vue";
import router from "./router";
import "./assets/tailwind.css";
import "./assets/admin-theme.css";
import "vue3-toastify/dist/index.css";
import Vue3Toastify from "vue3-toastify";

createApp(App)
  .use(createPinia())
  .use(router)
  .use(Vue3Toastify, {
    autoClose: 3000,
    position: "top-right",
  })
  .mount("#app");
