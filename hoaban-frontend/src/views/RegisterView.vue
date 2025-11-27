<script setup lang="ts">
import { ref } from "vue";
import { useRouter } from "vue-router";
import { register } from "@/api/auth.service";
import { toast } from "vue3-toastify";
import LoadingOverlay from "@/components/LoadingOverlay.vue";

const router = useRouter();
const form = ref({
  fullName: "",
  email: "",
  password: "",
  confirmPassword: "",
});
const loading = ref(false);
const showPassword = ref(false);
const showConfirmPassword = ref(false);
const agreeTerms = ref(false);

const togglePassword = () => {
  showPassword.value = !showPassword.value;
};

const toggleConfirmPassword = () => {
  showConfirmPassword.value = !showConfirmPassword.value;
};

const showTerms = () => {
  toast.info(
    `Điều khoản và Điều kiện - Hoa Ban Restaurant

1. Tài khoản và bảo mật:
   - Bảo mật thông tin đăng nhập
   - Chịu trách nhiệm về hoạt động trên tài khoản

2. Đặt món và thanh toán:
   - Thông tin đặt món chính xác
   - Thanh toán đúng hạn theo phương thức đã chọn

3. Chính sách hủy đơn:
   - Hủy đơn trước 15 phút khi giao hàng
   - Hoàn tiền theo chính sách của nhà hàng

4. Bảo vệ thông tin cá nhân:
   - Thu thập và sử dụng thông tin hợp pháp
   - Không chia sẻ thông tin với bên thứ ba

Liên hệ hỗ trợ: 0868030043`,
    { autoClose: 8000 }
  );
};

const onSubmit = async () => {
  if (form.value.password !== form.value.confirmPassword) {
    toast.error("Mật khẩu xác nhận không khớp");
    return;
  }

  if (form.value.password.length < 6) {
    toast.error("Mật khẩu phải có ít nhất 6 ký tự");
    return;
  }

  if (!agreeTerms.value) {
    toast.error("Vui lòng đồng ý với điều khoản và điều kiện!");
    return;
  }

  loading.value = true;
  try {
    await register({
      fullName: form.value.fullName,
      email: form.value.email,
      password: form.value.password,
    });
    toast.success("Đăng ký thành công! Vui lòng đăng nhập.");
    router.push("/auth/login");
  } catch (e: any) {
    toast.error(e?.friendlyMessage || "Đăng ký thất bại");
  } finally {
    loading.value = false;
  }
};
</script>

<template>
  <LoadingOverlay :show="loading" message="Đang đăng ký tài khoản..." />

  <div
    class="min-h-screen relative overflow-y-auto bg-gradient-to-br from-gray-900 via-red-950 to-gray-900"
  >
    <!-- Animated Background -->
    <div class="absolute inset-0">
      <div
        class="absolute inset-0 bg-cover bg-center bg-no-repeat opacity-30"
        style="background-image: url('@/assets/img/Hoa_Ban_restaurant.jpg')"
      ></div>
      <div
        class="absolute top-0 -left-4 w-72 h-72 bg-red-600 rounded-full mix-blend-multiply filter blur-3xl opacity-20 animate-blob"
      ></div>
      <div
        class="absolute top-0 -right-4 w-72 h-72 bg-orange-600 rounded-full mix-blend-multiply filter blur-3xl opacity-20 animate-blob animation-delay-2000"
      ></div>
      <div
        class="absolute -bottom-8 left-20 w-72 h-72 bg-pink-600 rounded-full mix-blend-multiply filter blur-3xl opacity-20 animate-blob animation-delay-4000"
      ></div>
    </div>

    <!-- Register Form Container -->
    <div class="relative z-10 min-h-screen flex items-center justify-center px-4 py-8">
      <div class="glass-card rounded-3xl p-10 w-full max-w-md shadow-2xl border border-white/10">
        <!-- Restaurant Logo -->
        <div class="text-center mb-8">
          <div
            class="w-24 h-24 mx-auto mb-4 bg-gradient-to-br from-red-600 to-red-800 rounded-full flex items-center justify-center shadow-xl ring-4 ring-red-500/20 transform hover:scale-105 transition-transform duration-300"
          >
            <img
              src="@/assets/img/logo.png"
              alt="Hoa Ban Restaurant"
              class="w-20 h-20 rounded-full object-cover"
            />
          </div>
          <h1
            class="text-3xl font-bold text-white mb-2"
            style="font-family: 'Playfair Display', serif"
          >
            Đăng ký
          </h1>
          <p class="text-white/70 text-sm">Tạo tài khoản mới tại Hoa Ban Restaurant</p>
        </div>

        <!-- Register Form -->
        <form @submit.prevent="onSubmit" class="space-y-4">
          <!-- Full Name Field -->
          <div class="form-group">
            <label
              for="fullName"
              class="block text-sm font-medium text-white/90 mb-2"
              style="font-family: 'Roboto', sans-serif"
            >
              Họ và tên
            </label>
            <input
              id="fullName"
              v-model="form.fullName"
              type="text"
              required
              placeholder="Nhập họ và tên của bạn"
              class="input-glass w-full px-4 py-3.5 bg-white/5 border border-white/10 rounded-xl text-white placeholder-white/40 focus:border-red-500 focus:ring-2 focus:ring-red-500/50 outline-none transition-all backdrop-blur-sm"
              style="font-family: 'Roboto', sans-serif"
            />
          </div>

          <!-- Email Field -->
          <div class="form-group">
            <label
              for="email"
              class="block text-sm font-medium text-white/90 mb-2"
              style="font-family: 'Roboto', sans-serif"
            >
              Email
            </label>
            <input
              id="email"
              v-model="form.email"
              type="email"
              required
              placeholder="example@gmail.com"
              class="input-glass w-full px-4 py-3.5 bg-white/5 border border-white/10 rounded-xl text-white placeholder-white/40 focus:border-red-500 focus:ring-2 focus:ring-red-500/50 outline-none transition-all backdrop-blur-sm"
              style="font-family: 'Roboto', sans-serif"
            />
          </div>

          <!-- Password Field -->
          <div class="form-group">
            <label
              for="password"
              class="block text-sm font-medium text-white/90 mb-2"
              style="font-family: 'Roboto', sans-serif"
            >
              Mật khẩu
            </label>
            <div class="relative">
              <input
                id="password"
                v-model="form.password"
                :type="showPassword ? 'text' : 'password'"
                required
                placeholder="••••••••••"
                class="input-glass w-full px-4 py-3.5 pr-12 bg-white/5 border border-white/10 rounded-xl text-white placeholder-white/40 focus:border-red-500 focus:ring-2 focus:ring-red-500/50 outline-none transition-all backdrop-blur-sm"
                style="font-family: 'Roboto', sans-serif"
              />
              <button
                type="button"
                @click="togglePassword"
                class="absolute right-3 top-1/2 transform -translate-y-1/2 text-white/60 hover:text-white/90 transition-colors"
              >
                <svg
                  v-if="showPassword"
                  class="w-5 h-5"
                  fill="none"
                  stroke="currentColor"
                  viewBox="0 0 24 24"
                >
                  <path
                    stroke-linecap="round"
                    stroke-linejoin="round"
                    stroke-width="2"
                    d="M15 12a3 3 0 11-6 0 3 3 0 016 0z"
                  ></path>
                  <path
                    stroke-linecap="round"
                    stroke-linejoin="round"
                    stroke-width="2"
                    d="M2.458 12C3.732 7.943 7.523 5 12 5c4.478 0 8.268 2.943 9.542 7-1.274 4.057-5.064 7-9.542 7-4.477 0-8.268-2.943-9.542-7z"
                  ></path>
                </svg>
                <svg v-else class="w-5 h-5" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                  <path
                    stroke-linecap="round"
                    stroke-linejoin="round"
                    stroke-width="2"
                    d="M13.875 18.825A10.05 10.05 0 0112 19c-4.478 0-8.268-2.943-9.543-7a9.97 9.97 0 011.563-3.029m5.858.908a3 3 0 114.243 4.243M9.878 9.878l4.242 4.242M9.878 9.878L3 3m6.878 6.878L21 21"
                  ></path>
                </svg>
              </button>
            </div>
          </div>

          <!-- Confirm Password Field -->
          <div class="form-group">
            <label
              for="confirmPassword"
              class="block text-sm font-medium text-white/90 mb-2"
              style="font-family: 'Roboto', sans-serif"
            >
              Nhập lại mật khẩu
            </label>
            <div class="relative">
              <input
                id="confirmPassword"
                v-model="form.confirmPassword"
                :type="showConfirmPassword ? 'text' : 'password'"
                required
                placeholder="••••••••••"
                class="input-glass w-full px-4 py-3.5 pr-12 bg-white/5 border border-white/10 rounded-xl text-white placeholder-white/40 focus:border-red-500 focus:ring-2 focus:ring-red-500/50 outline-none transition-all backdrop-blur-sm"
                style="font-family: 'Roboto', sans-serif"
              />
              <button
                type="button"
                @click="toggleConfirmPassword"
                class="absolute right-3 top-1/2 transform -translate-y-1/2 text-white/60 hover:text-white/90 transition-colors"
              >
                <svg
                  v-if="showConfirmPassword"
                  class="w-5 h-5"
                  fill="none"
                  stroke="currentColor"
                  viewBox="0 0 24 24"
                >
                  <path
                    stroke-linecap="round"
                    stroke-linejoin="round"
                    stroke-width="2"
                    d="M15 12a3 3 0 11-6 0 3 3 0 016 0z"
                  ></path>
                  <path
                    stroke-linecap="round"
                    stroke-linejoin="round"
                    stroke-width="2"
                    d="M2.458 12C3.732 7.943 7.523 5 12 5c4.478 0 8.268 2.943 9.542 7-1.274 4.057-5.064 7-9.542 7-4.477 0-8.268-2.943-9.542-7z"
                  ></path>
                </svg>
                <svg v-else class="w-5 h-5" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                  <path
                    stroke-linecap="round"
                    stroke-linejoin="round"
                    stroke-width="2"
                    d="M13.875 18.825A10.05 10.05 0 0112 19c-4.478 0-8.268-2.943-9.543-7a9.97 9.97 0 011.563-3.029m5.858.908a3 3 0 114.243 4.243M9.878 9.878l4.242 4.242M9.878 9.878L3 3m6.878 6.878L21 21"
                  ></path>
                </svg>
              </button>
            </div>
          </div>

          <!-- Terms and Conditions -->
          <div class="flex items-start">
            <input
              id="agreeTerms"
              v-model="agreeTerms"
              type="checkbox"
              required
              class="w-4 h-4 text-red-600 bg-white/10 border-white/20 rounded focus:ring-red-500 focus:ring-2 mt-1"
            />
            <label
              for="agreeTerms"
              class="ml-2 text-sm text-white/80"
              style="font-family: 'Roboto', sans-serif"
            >
              Tôi đồng ý với
              <button
                type="button"
                @click="showTerms"
                class="text-red-400 hover:text-red-300 font-medium underline transition-colors"
              >
                Điều khoản và Điều kiện
              </button>
              của Hoa Ban Restaurant
            </label>
          </div>

          <!-- Register Button -->
          <button
            type="submit"
            :disabled="loading"
            class="btn-gradient w-full bg-gradient-to-r from-red-600 to-red-700 hover:from-red-700 hover:to-red-800 text-white py-3.5 rounded-xl font-bold text-lg disabled:opacity-50 disabled:cursor-not-allowed transition-all shadow-lg hover:shadow-red-500/50 transform hover:-translate-y-0.5"
            style="font-family: 'Inria Sans', sans-serif"
          >
            <span v-if="!loading">Đăng ký</span>
            <span v-else class="flex items-center justify-center">
              <svg
                class="animate-spin -ml-1 mr-3 h-5 w-5 text-white"
                xmlns="http://www.w3.org/2000/svg"
                fill="none"
                viewBox="0 0 24 24"
              >
                <circle
                  class="opacity-25"
                  cx="12"
                  cy="12"
                  r="10"
                  stroke="currentColor"
                  stroke-width="4"
                ></circle>
                <path
                  class="opacity-75"
                  fill="currentColor"
                  d="M4 12a8 8 0 018-8V0C5.373 0 0 5.373 0 12h4zm2 5.291A7.962 7.962 0 014 12H0c0 3.042 1.135 5.824 3 7.938l3-2.647z"
                ></path>
              </svg>
              Đang xử lý...
            </span>
          </button>

          <!-- Login Link -->
          <div class="text-center pt-4 border-t border-white/10">
            <p class="text-white/70 text-sm" style="font-family: 'Roboto', sans-serif">
              Đã có tài khoản?
              <RouterLink
                to="/auth/login"
                class="text-red-400 hover:text-red-300 font-semibold ml-1 transition-colors"
              >
                Đăng nhập ngay
              </RouterLink>
            </p>
          </div>
        </form>
      </div>
    </div>
  </div>
</template>

<style scoped>
/* Glass Card Effect */
.glass-card {
  background: rgba(15, 15, 15, 0.7);
  backdrop-filter: blur(20px);
  -webkit-backdrop-filter: blur(20px);
  animation: fadeInUp 0.8s ease-out;
}

/* Animated Blobs */
@keyframes blob {
  0%,
  100% {
    transform: translate(0, 0) scale(1);
  }
  33% {
    transform: translate(30px, -50px) scale(1.1);
  }
  66% {
    transform: translate(-20px, 20px) scale(0.9);
  }
}

.animate-blob {
  animation: blob 7s infinite;
}

.animation-delay-2000 {
  animation-delay: 2s;
}

.animation-delay-4000 {
  animation-delay: 4s;
}

/* Fade In Up Animation */
@keyframes fadeInUp {
  from {
    opacity: 0;
    transform: translateY(30px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}

/* Input Glass Effect */
.input-glass:focus {
  background: rgba(255, 255, 255, 0.08);
  transform: translateY(-2px);
  box-shadow: 0 8px 32px rgba(239, 68, 68, 0.2);
}

/* Button Gradient Effect */
.btn-gradient {
  background-size: 200% 200%;
  animation: gradientShift 3s ease infinite;
}

@keyframes gradientShift {
  0%,
  100% {
    background-position: 0% 50%;
  }
  50% {
    background-position: 100% 50%;
  }
}

/* Hover Effects */
button:not(:disabled):hover {
  transform: translateY(-2px);
}

/* Form Group Animation */
.form-group {
  animation: slideIn 0.4s ease-out backwards;
}

.form-group:nth-child(1) {
  animation-delay: 0.1s;
}

.form-group:nth-child(2) {
  animation-delay: 0.15s;
}

.form-group:nth-child(3) {
  animation-delay: 0.2s;
}

.form-group:nth-child(4) {
  animation-delay: 0.25s;
}

@keyframes slideIn {
  from {
    opacity: 0;
    transform: translateX(-20px);
  }
  to {
    opacity: 1;
    transform: translateX(0);
  }
}

/* Custom scrollbar */
::-webkit-scrollbar {
  width: 8px;
}

::-webkit-scrollbar-track {
  background: rgba(255, 255, 255, 0.05);
}

::-webkit-scrollbar-thumb {
  background: rgba(220, 38, 38, 0.6);
  border-radius: 4px;
}

::-webkit-scrollbar-thumb:hover {
  background: rgba(220, 38, 38, 0.8);
}
</style>
