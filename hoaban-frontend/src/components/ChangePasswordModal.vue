<template>
  <div class="min-h-screen relative overflow-hidden">
    <!-- Background Image -->
    <div
      class="absolute inset-0 bg-cover bg-center bg-no-repeat"
      style="background-image: url('@/assets/img/Hoa_Ban_restaurant.jpg')"
    >
      <!-- Dark overlay -->
      <div class="absolute inset-0 bg-black bg-opacity-50"></div>
    </div>

    <!-- Forgot Password Form Container -->
    <div class="relative z-10 min-h-screen flex items-center justify-center px-4">
      <div
        class="bg-white bg-opacity-95 backdrop-blur-sm rounded-2xl p-8 w-full max-w-md shadow-2xl"
      >
        <!-- Restaurant Logo -->
        <div class="text-center mb-6">
          <div
            class="w-20 h-20 mx-auto mb-4 bg-[#9f0909] rounded-full flex items-center justify-center"
          >
            <img
              src="@/assets/img/logo.png"
              alt="Hoa Ban Restaurant"
              class="w-16 h-16 rounded-full object-cover"
            />
          </div>
          <h1
            class="text-2xl font-bold text-gray-800"
            style="font-family: 'Playfair Display', serif"
          >
            Quên mật khẩu
          </h1>
          <p class="text-gray-600 mt-2" style="font-family: 'Roboto', sans-serif">
            {{ currentStep === 1 ? "Nhập email để nhận mã OTP" : "Nhập mã OTP để xác thực" }}
          </p>
        </div>

        <!-- Step 1: Email Input -->
        <form v-if="currentStep === 1" @submit.prevent="handleSendOTP" class="space-y-6">
          <!-- Email Field -->
          <div>
            <label
              for="email"
              class="block text-sm font-medium text-gray-700 mb-2"
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
              class="w-full px-4 py-3 border border-gray-300 rounded-lg focus:ring-2 focus:ring-[#9f0909] focus:border-transparent outline-none transition-all"
              style="font-family: 'Roboto', sans-serif"
            />
          </div>

          <!-- Send OTP Button -->
          <button
            type="submit"
            :disabled="isLoading"
            class="w-full bg-red-500 text-white py-3 rounded-lg font-bold text-lg hover:bg-red-600 disabled:opacity-50 disabled:cursor-not-allowed transition-colors"
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
        <form v-if="currentStep === 2" @submit.prevent="handleVerifyOTP" class="space-y-6">
          <!-- OTP Info -->
          <div class="text-center mb-4">
            <p class="text-sm text-gray-600" style="font-family: 'Roboto', sans-serif">
              Mã OTP đã được gửi đến email:
            </p>
            <p class="font-medium text-[#9f0909]" style="font-family: 'Roboto', sans-serif">
              {{ forgotForm.email }}
            </p>
          </div>

          <!-- OTP Input Fields -->
          <div>
            <label
              class="block text-sm font-medium text-gray-700 mb-3 text-center"
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
                class="w-12 h-12 text-center text-xl font-bold border border-gray-300 rounded-lg focus:ring-2 focus:ring-[#9f0909] focus:border-transparent outline-none transition-all"
                @input="handleOTPInput(index, $event)"
                @keydown="handleOTPKeydown(index, $event)"
                @paste="handleOTPPaste($event)"
              />
            </div>
          </div>

          <!-- OTP Timer -->
          <div class="text-center">
            <p class="text-sm text-gray-600" style="font-family: 'Roboto', sans-serif">
              <span v-if="otpTimer > 0">
                Mã OTP sẽ hết hạn sau:
                <span class="font-medium text-[#9f0909]">{{ formatTime(otpTimer) }}</span>
              </span>
              <span v-else class="text-red-500"> Mã OTP đã hết hạn </span>
            </p>

            <!-- Resend OTP Button - Show when timer < 3 minutes OR when expired -->
            <div class="mt-3">
              <button
                v-if="otpTimer === 0"
                type="button"
                @click="handleResendOTP"
                :disabled="isResending"
                class="bg-[#9f0909] text-white px-6 py-2 rounded-lg font-medium hover:bg-[#800808] disabled:opacity-50 disabled:cursor-not-allowed transition-colors"
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
                class="bg-orange-500 text-white px-6 py-2 rounded-lg font-medium hover:bg-orange-600 disabled:opacity-50 disabled:cursor-not-allowed transition-colors"
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
                class="text-xs text-gray-500 mt-2"
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
            class="w-full bg-red-500 text-white py-3 rounded-lg font-bold text-lg hover:bg-red-600 disabled:opacity-50 disabled:cursor-not-allowed transition-colors"
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
            class="w-full text-gray-600 hover:text-gray-800 py-2 font-medium transition-colors"
          >
            ← Quay lại nhập email
          </button>
        </form>

        <!-- Login Link -->
        <div class="text-center mt-6">
          <p class="text-gray-600" style="font-family: 'Roboto', sans-serif">
            Nhớ mật khẩu?
            <router-link
              to="/auth/login"
              class="text-[#9f0909] hover:text-[#800808] font-medium ml-1"
            >
              Đăng nhập
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
const generatedOTP = ref("");
const otpTimerInterval = ref<number | null>(null);

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

    // Check if email exists
    const registeredUsers = JSON.parse(localStorage.getItem("registeredUsers") || "[]");
    const adminEmail = "admin@gmail.com";

    const userExists =
      registeredUsers.some((user: any) => user.email === forgotForm.value.email) ||
      forgotForm.value.email === adminEmail;

    if (!userExists) {
      alert("Email không tồn tại trong hệ thống!");
      return;
    }

    // Simulate API call
    await new Promise((resolve) => setTimeout(resolve, 1500));

    // Generate OTP
    generatedOTP.value = Math.floor(100000 + Math.random() * 900000).toString();

    // Save OTP with expiration
    const otpData = {
      email: forgotForm.value.email,
      otp: generatedOTP.value,
      expiresAt: Date.now() + 5 * 60 * 1000, // 5 minutes
      attempts: 0,
    };
    localStorage.setItem("forgotPasswordOTP", JSON.stringify(otpData));

    alert(
      `Mã OTP đã được gửi đến email: ${forgotForm.value.email}\n\nMã OTP (demo): ${generatedOTP.value}`
    );

    // Move to step 2
    currentStep.value = 2;
    startOTPTimer();

    // Focus first OTP input
    setTimeout(() => {
      if (otpInputs.value[0]) {
        otpInputs.value[0]?.focus();
      }
    }, 100);
  } catch (error) {
    console.error("Send OTP error:", error);
    alert("Có lỗi xảy ra khi gửi mã OTP. Vui lòng thử lại!");
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
    const otpData = JSON.parse(localStorage.getItem("forgotPasswordOTP") || "{}");

    // Check if OTP exists and not expired
    if (!otpData.otp || Date.now() > otpData.expiresAt) {
      alert("Mã OTP đã hết hạn! Vui lòng gửi lại mã OTP.");
      return;
    }

    // Check attempts
    if (otpData.attempts >= 3) {
      alert("Bạn đã nhập sai OTP quá 3 lần! Vui lòng gửi lại mã OTP.");
      localStorage.removeItem("forgotPasswordOTP");
      goBackToStep1();
      return;
    }

    // Verify OTP
    if (enteredOTP !== otpData.otp) {
      otpData.attempts = (otpData.attempts || 0) + 1;
      localStorage.setItem("forgotPasswordOTP", JSON.stringify(otpData));

      alert(`Mã OTP không đúng! Còn ${3 - otpData.attempts} lần thử.`);

      // Clear OTP inputs
      otpDigits.value = ["", "", "", "", "", ""];
      if (otpInputs.value[0]) {
        otpInputs.value[0]?.focus();
      }
      return;
    }

    // Simulate API call
    await new Promise((resolve) => setTimeout(resolve, 1000));

    // OTP verified successfully
    alert("Xác thực OTP thành công!");

    // Clean up
    if (otpTimerInterval.value) {
      clearInterval(otpTimerInterval.value);
    }
    localStorage.removeItem("forgotPasswordOTP");

    // Navigate to reset password page
    router.push({
      path: "/auth/reset-password",
      query: { email: forgotForm.value.email, verified: "true" },
    });
  } catch (error) {
    console.error("Verify OTP error:", error);
    alert("Có lỗi xảy ra khi xác thực OTP. Vui lòng thử lại!");
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

    // Simulate API call for resending OTP
    await new Promise((resolve) => setTimeout(resolve, 1000));

    // Generate new OTP
    generatedOTP.value = Math.floor(100000 + Math.random() * 900000).toString();

    // Save new OTP with expiration
    const otpData = {
      email: forgotForm.value.email,
      otp: generatedOTP.value,
      expiresAt: Date.now() + 5 * 60 * 1000, // 5 minutes
      attempts: 0,
    };
    localStorage.setItem("forgotPasswordOTP", JSON.stringify(otpData));

    alert(
      `Mã OTP mới đã được gửi đến email: ${forgotForm.value.email}\n\nMã OTP mới (demo): ${generatedOTP.value}`
    );

    // Restart timer
    startOTPTimer();

    // Focus first OTP input
    setTimeout(() => {
      if (otpInputs.value[0]) {
        otpInputs.value[0]?.focus();
      }
    }, 100);
  } catch (error) {
    console.error("Resend OTP error:", error);
    alert("Có lỗi xảy ra khi gửi lại mã OTP. Vui lòng thử lại!");
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
  // Clear any existing OTP data
  localStorage.removeItem("forgotPasswordOTP");
});

onUnmounted(() => {
  if (otpTimerInterval.value) {
    clearInterval(otpTimerInterval.value);
  }
});
</script>

<style scoped>
/* Custom animations */
@keyframes fadeIn {
  from {
    opacity: 0;
    transform: translateY(20px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}

.bg-white {
  animation: fadeIn 0.6s ease-out;
}

/* Input focus effects */
input:focus {
  transform: translateY(-1px);
  box-shadow: 0 4px 12px rgba(159, 9, 9, 0.15);
}

/* Button hover effects */
button {
  transition: all 0.3s ease;
}

button:hover {
  transform: translateY(-1px);
}

/* OTP input styling */
input[type="text"] {
  font-family: "Roboto", monospace;
}

/* OTP input valid state */
input[type="text"]:valid {
  border-color: #10b981;
  background-color: #f0fdf4;
}

/* Custom scrollbar */
::-webkit-scrollbar {
  width: 8px;
}

::-webkit-scrollbar-track {
  background: #f1f1f1;
}

::-webkit-scrollbar-thumb {
  background: #9f0909;
  border-radius: 4px;
}

::-webkit-scrollbar-thumb:hover {
  background: #800808;
}
</style>
