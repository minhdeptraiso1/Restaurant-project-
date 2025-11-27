<template>
  <div
    class="min-h-screen relative overflow-hidden bg-gradient-to-br from-gray-900 via-red-950 to-gray-900"
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

    <!-- Forgot Password Form Container -->
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
            Quên mật khẩu
          </h1>
          <p class="text-white/70 text-sm" style="font-family: 'Roboto', sans-serif">
            {{ currentStep === 1 ? "Nhập email để nhận mã OTP" : "Nhập mã OTP để xác thực" }}
          </p>
        </div>

        <!-- Step 1: Email Input -->
        <form v-if="currentStep === 1" @submit.prevent="handleSendOTP" class="space-y-6">
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
              v-model="forgotForm.email"
              type="email"
              required
              placeholder="Nhập email của bạn"
              class="input-glass w-full px-4 py-3.5 bg-white/5 border border-white/10 rounded-xl text-white placeholder-white/40 focus:border-red-500 focus:ring-2 focus:ring-red-500/50 outline-none transition-all backdrop-blur-sm"
              style="font-family: 'Roboto', sans-serif"
            />
          </div>

          <!-- Send OTP Button -->
          <button
            type="submit"
            :disabled="isLoading"
            class="btn-gradient w-full bg-gradient-to-r from-red-600 to-red-700 hover:from-red-700 hover:to-red-800 text-white py-3.5 rounded-xl font-bold text-lg disabled:opacity-50 disabled:cursor-not-allowed transition-all shadow-lg hover:shadow-red-500/50 transform hover:-translate-y-0.5"
            style="font-family: 'Inria Sans', sans-serif"
          >
            <span v-if="!isLoading">Gửi mã OTP</span>
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
              Đang gửi...
            </span>
          </button>
        </form>

        <!-- Step 2: OTP Input -->
        <form v-if="currentStep === 2" @submit.prevent="handleVerifyOTP" class="space-y-5">
          <!-- OTP Info -->
          <div class="text-center mb-4">
            <p class="text-sm text-white/70" style="font-family: 'Roboto', sans-serif">
              Mã OTP đã được gửi đến email:
            </p>
            <p class="font-medium text-red-400 mt-1" style="font-family: 'Roboto', sans-serif">
              {{ forgotForm.email }}
            </p>
          </div>

          <!-- OTP Input Fields -->
          <div class="form-group">
            <label
              class="block text-sm font-medium text-white/90 mb-3 text-center"
              style="font-family: 'Roboto', sans-serif"
            >
              Nhập mã OTP (6 số)
            </label>
            <div class="flex justify-center space-x-2">
              <input
                v-for="(digit, index) in otpDigits"
                :key="index"
                :ref="(el) => (otpInputs[index] = el as HTMLInputElement | null)"
                v-model="otpDigits[index]"
                type="text"
                maxlength="1"
                pattern="[0-9]"
                class="w-12 h-14 text-center text-xl font-bold bg-white/5 border border-white/20 rounded-xl text-white focus:ring-2 focus:ring-red-500 focus:border-red-500 outline-none transition-all backdrop-blur-sm"
                @input="handleOTPInput(index, $event)"
                @keydown="handleOTPKeydown(index, $event)"
                @paste="handleOTPPaste($event)"
              />
            </div>
          </div>

          <!-- New Password Fields -->
          <div class="space-y-4">
            <div class="form-group">
              <label class="block text-sm font-medium text-white/90 mb-2">Mật khẩu mới</label>
              <div class="relative">
                <input
                  :type="showNewPassword ? 'text' : 'password'"
                  v-model="newPassword"
                  required
                  minlength="6"
                  placeholder="Nhập mật khẩu mới"
                  class="input-glass w-full px-4 py-3.5 pr-12 bg-white/5 border border-white/10 rounded-xl text-white placeholder-white/40 focus:border-red-500 focus:ring-2 focus:ring-red-500/50 outline-none transition-all backdrop-blur-sm"
                />
                <button
                  type="button"
                  @click="showNewPassword = !showNewPassword"
                  class="absolute inset-y-0 right-3 my-auto text-sm text-white/60 hover:text-white/90"
                >
                  {{ showNewPassword ? "Ẩn" : "Hiện" }}
                </button>
              </div>
            </div>
            <div class="form-group">
              <label class="block text-sm font-medium text-white/90 mb-2">Xác nhận mật khẩu</label>
              <div class="relative">
                <input
                  :type="showConfirmPassword ? 'text' : 'password'"
                  v-model="confirmPassword"
                  required
                  minlength="6"
                  placeholder="Nhập lại mật khẩu mới"
                  class="input-glass w-full px-4 py-3.5 pr-12 bg-white/5 border border-white/10 rounded-xl text-white placeholder-white/40 focus:border-red-500 focus:ring-2 focus:ring-red-500/50 outline-none transition-all backdrop-blur-sm"
                />
                <button
                  type="button"
                  @click="showConfirmPassword = !showConfirmPassword"
                  class="absolute inset-y-0 right-3 my-auto text-sm text-white/60 hover:text-white/90"
                >
                  {{ showConfirmPassword ? "Ẩn" : "Hiện" }}
                </button>
              </div>
            </div>
          </div>

          <!-- OTP Timer -->
          <div class="text-center">
            <p class="text-sm text-white/70" style="font-family: 'Roboto', sans-serif">
              <span v-if="otpTimer > 0">
                Mã OTP sẽ hết hạn sau:
                <span class="font-medium text-red-400">{{ formatTime(otpTimer) }}</span>
              </span>
              <span v-else class="text-red-400"> Mã OTP đã hết hạn </span>
            </p>

            <!-- Resend OTP Button - Show when timer < 3 minutes OR when expired -->
            <div class="mt-3">
              <button
                v-if="otpTimer === 0"
                type="button"
                @click="handleResendOTP"
                :disabled="isResending"
                class="bg-gradient-to-r from-red-600 to-red-700 hover:from-red-700 hover:to-red-800 text-white px-6 py-2 rounded-xl font-medium disabled:opacity-50 disabled:cursor-not-allowed transition-all shadow-lg"
              >
                <span v-if="!isResending">Gửi lại mã OTP</span>
                <span v-else class="flex items-center justify-center">
                  <svg
                    class="animate-spin -ml-1 mr-2 h-4 w-4 text-white"
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
                  Đang gửi...
                </span>
              </button>

              <button
                v-else-if="otpTimer > 0 && otpTimer <= 180"
                type="button"
                @click="handleResendOTP"
                :disabled="isResending"
                class="bg-gradient-to-r from-orange-500 to-orange-600 hover:from-orange-600 hover:to-orange-700 text-white px-6 py-2 rounded-xl font-medium disabled:opacity-50 disabled:cursor-not-allowed transition-all shadow-lg"
              >
                <span v-if="!isResending">Gửi lại mã OTP</span>
                <span v-else class="flex items-center justify-center">
                  <svg
                    class="animate-spin -ml-1 mr-2 h-4 w-4 text-white"
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
                  Đang gửi...
                </span>
              </button>

              <p
                v-else-if="otpTimer > 180"
                class="text-xs text-white/60 mt-2"
                style="font-family: 'Roboto', sans-serif"
              >
                Bạn có thể gửi lại OTP sau {{ formatTime(otpTimer - 180) }}
              </p>
            </div>
          </div>

          <!-- Verify OTP Button -->
          <button
            type="submit"
            :disabled="isLoading || !isOTPComplete"
            class="btn-gradient w-full bg-gradient-to-r from-red-600 to-red-700 hover:from-red-700 hover:to-red-800 text-white py-3.5 rounded-xl font-bold text-lg disabled:opacity-50 disabled:cursor-not-allowed transition-all shadow-lg hover:shadow-red-500/50 transform hover:-translate-y-0.5"
            style="font-family: 'Inria Sans', sans-serif"
          >
            <span v-if="!isLoading">Xác thực OTP</span>
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
              Đang xác thực...
            </span>
          </button>

          <!-- Back to Step 1 -->
          <button
            type="button"
            @click="goBackToStep1"
            class="w-full text-white/70 hover:text-white py-2 font-medium transition-colors"
          >
            ← Quay lại nhập email
          </button>
        </form>

        <!-- Login Link -->
        <div class="text-center mt-6 pt-4 border-t border-white/10">
          <p class="text-white/70 text-sm" style="font-family: 'Roboto', sans-serif">
            Nhớ mật khẩu?
            <router-link
              to="/auth/login"
              class="text-red-400 hover:text-red-300 font-semibold ml-1 transition-colors"
            >
              Đăng nhập ngay
            </router-link>
          </p>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted, onUnmounted } from "vue";
import { useRouter } from "vue-router";
import { requestPasswordOtp, verifyPasswordOtp } from "@/api/auth.service";
const router = useRouter();

// Form data
const forgotForm = ref({
  email: "",
});

// UI states
const currentStep = ref(1);
const isLoading = ref(false);
const isResending = ref(false);
const otpDigits = ref(["", "", "", "", "", ""]);
const otpInputs = ref<(HTMLInputElement | null)[]>([]);
const otpTimer = ref(0);
const otpTimerInterval = ref<number | null>(null);
const newPassword = ref("");
const confirmPassword = ref("");
const showNewPassword = ref(false);
const showConfirmPassword = ref(false);

// Computed
const isOTPComplete = computed(() => {
  return otpDigits.value.every((digit) => digit !== "");
});

// Methods
const handleSendOTP = async () => {
  try {
    isLoading.value = true;

    // Validate email
    if (!forgotForm.value.email.trim()) {
      alert("Vui lòng nhập email!");
      return;
    }

    const emailRegex = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;
    if (!emailRegex.test(forgotForm.value.email)) {
      alert("Email không hợp lệ!");
      return;
    }

    // Call backend to send OTP
    await requestPasswordOtp({ email: forgotForm.value.email });
    alert(`Mã OTP đã được gửi đến email: ${forgotForm.value.email}`);

    // Move to step 2
    currentStep.value = 2;
    startOTPTimer();

    // Focus first OTP input
    setTimeout(() => {
      if (otpInputs.value[0]) {
        otpInputs.value[0]?.focus();
      }
    }, 100);
  } catch (error: any) {
    console.error("Send OTP error:", error);
    const msg =
      error?.friendlyMessage ||
      error?.response?.data?.message ||
      "Có lỗi xảy ra khi gửi mã OTP. Vui lòng thử lại!";
    alert(msg);
  } finally {
    isLoading.value = false;
  }
};

const handleOTPInput = (index: number, event: Event) => {
  const target = event.target as HTMLInputElement;
  const value = target.value;

  // Only allow numbers
  if (value && !/^\d$/.test(value)) {
    otpDigits.value[index] = "";
    return;
  }

  otpDigits.value[index] = value;

  // Auto focus next input
  if (value && index < 5) {
    if (otpInputs.value[index + 1]) {
      otpInputs.value[index + 1]?.focus();
    }
  }
};

const handleOTPKeydown = (index: number, event: KeyboardEvent) => {
  // Handle backspace
  if (event.key === "Backspace" && !otpDigits.value[index] && index > 0) {
    if (otpInputs.value[index - 1]) {
      otpInputs.value[index - 1]?.focus();
    }
  }
};

const handleOTPPaste = (event: ClipboardEvent) => {
  event.preventDefault();
  const pasteData = event.clipboardData?.getData("text") || "";
  const digits = pasteData.replace(/\D/g, "").slice(0, 6).split("");

  digits.forEach((digit, index) => {
    if (index < 6) {
      otpDigits.value[index] = digit;
    }
  });

  // Focus appropriate input
  const nextEmptyIndex = otpDigits.value.findIndex((digit) => digit === "");
  if (nextEmptyIndex !== -1 && otpInputs.value[nextEmptyIndex]) {
    otpInputs.value[nextEmptyIndex]?.focus();
  } else if (otpInputs.value[5]) {
    otpInputs.value[5]?.focus();
  }
};

const startOTPTimer = () => {
  otpTimer.value = 300; // 5 minutes

  otpTimerInterval.value = setInterval(() => {
    otpTimer.value--;

    if (otpTimer.value <= 0) {
      if (otpTimerInterval.value) {
        clearInterval(otpTimerInterval.value);
      }
    }
  }, 1000);
};

const formatTime = (seconds: number) => {
  const minutes = Math.floor(seconds / 60);
  const remainingSeconds = seconds % 60;
  return `${minutes}:${remainingSeconds.toString().padStart(2, "0")}`;
};

const handleVerifyOTP = async () => {
  try {
    isLoading.value = true;

    const enteredOTP = otpDigits.value.join("");

    // Validate password fields
    if (!newPassword.value || newPassword.value.length < 6) {
      alert("Mật khẩu mới phải có ít nhất 6 ký tự");
      return;
    }
    if (newPassword.value !== confirmPassword.value) {
      alert("Mật khẩu xác nhận không khớp");
      return;
    }

    // Call backend to verify OTP and reset password
    await verifyPasswordOtp({
      email: forgotForm.value.email,
      code: enteredOTP,
      newPassword: newPassword.value,
    });

    alert("Đổi mật khẩu thành công! Vui lòng đăng nhập lại.");

    // Clean up timer
    if (otpTimerInterval.value) clearInterval(otpTimerInterval.value);

    // Navigate to login
    router.push({ path: "/auth/login" });
  } catch (error: any) {
    console.error("Verify OTP error:", error);
    const msg =
      error?.friendlyMessage ||
      error?.response?.data?.message ||
      "Có lỗi xảy ra khi xác thực OTP. Vui lòng thử lại!";
    alert(msg);
  } finally {
    isLoading.value = false;
  }
};

const handleResendOTP = async () => {
  try {
    isResending.value = true;

    // Reset OTP inputs
    otpDigits.value = ["", "", "", "", "", ""];

    // Clear existing timer
    if (otpTimerInterval.value) {
      clearInterval(otpTimerInterval.value);
    }
    otpTimer.value = 0;

    // Request backend to resend OTP
    await requestPasswordOtp({ email: forgotForm.value.email });
    alert(`Mã OTP mới đã được gửi đến email: ${forgotForm.value.email}`);

    // Restart timer
    startOTPTimer();

    // Focus first OTP input
    setTimeout(() => {
      if (otpInputs.value[0]) {
        otpInputs.value[0]?.focus();
      }
    }, 100);
  } catch (error: any) {
    console.error("Resend OTP error:", error);
    const msg =
      error?.friendlyMessage ||
      error?.response?.data?.message ||
      "Có lỗi xảy ra khi gửi lại mã OTP. Vui lòng thử lại!";
    alert(msg);
  } finally {
    isResending.value = false;
  }
};

const goBackToStep1 = () => {
  currentStep.value = 1;
  otpDigits.value = ["", "", "", "", "", ""];
  if (otpTimerInterval.value) {
    clearInterval(otpTimerInterval.value);
  }
  otpTimer.value = 0;
  localStorage.removeItem("forgotPasswordOTP");
};

// Lifecycle
onMounted(() => {
  // nothing
});

onUnmounted(() => {
  if (otpTimerInterval.value) {
    clearInterval(otpTimerInterval.value);
  }
});
</script>

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

/* OTP Input Effects */
input[type="text"]:focus {
  background: rgba(255, 255, 255, 0.1);
  transform: scale(1.05);
}

input[type="text"]:valid {
  background: rgba(34, 197, 94, 0.1);
  border-color: rgba(34, 197, 94, 0.5);
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
