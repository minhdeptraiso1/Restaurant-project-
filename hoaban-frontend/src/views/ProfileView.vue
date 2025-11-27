<template>
  <div class="min-h-screen bg-gradient-to-br from-gray-50 via-white to-gray-100">
    <!-- Modern Hero Header -->
    <div
      class="relative overflow-hidden bg-gradient-to-r from-[#9f0909] via-rose-600 to-orange-500"
    >
      <div class="absolute inset-0 bg-black/20"></div>
      <div class="relative max-w-7xl mx-auto px-4 py-16">
        <div class="text-center">
          <!-- Avatar Image -->
          <div class="inline-block mb-6 relative">
            <div
              class="w-32 h-32 rounded-full bg-white/20 backdrop-blur-sm border-4 border-white/30 shadow-2xl overflow-hidden"
            >
              <img
                v-if="profile?.avatarUrl"
                :src="profile.avatarUrl"
                :alt="profile?.name || 'Avatar'"
                class="w-full h-full object-cover"
              />
              <div
                v-else
                class="w-full h-full flex items-center justify-center bg-gradient-to-br from-white/30 to-white/10"
              >
                <svg class="w-16 h-16 text-white" viewBox="0 0 24 24" fill="currentColor">
                  <path
                    d="M12 2C13.1 2 14 2.9 14 4C14 5.1 13.1 6 12 6C10.9 6 10 5.1 10 4C10 2.9 10.9 2 12 2ZM21 9V7L12 2L3 7V9C3 14.55 6.84 19.74 12 21C17.16 19.74 21 14.55 21 9Z"
                  />
                </svg>
              </div>
            </div>
          </div>
          <h1
            class="text-4xl md:text-5xl font-bold text-white mb-4"
            style="font-family: 'Playfair Display', serif"
          >
            {{ profile?.name || "Khách hàng" }}
          </h1>
          <p class="text-xl text-white/90 mb-6">Hồ sơ cá nhân</p>
          <div
            class="inline-flex items-center gap-2 px-4 py-2 bg-white/20 backdrop-blur-sm rounded-full border border-white/30"
          >
            <span class="w-2 h-2 bg-emerald-400 rounded-full animate-pulse"></span>
            <span class="text-white font-medium">{{ getMembershipTier(points) }}</span>
          </div>
        </div>
      </div>
      <!-- Decorative elements -->
      <div class="absolute top-10 left-10 w-20 h-20 bg-white/10 rounded-full blur-xl"></div>
      <div class="absolute bottom-10 right-10 w-32 h-32 bg-white/10 rounded-full blur-xl"></div>
    </div>

    <!-- Main Content -->
    <div class="max-w-4xl mx-auto px-4 -mt-8 relative z-10">
      <!-- Loyalty Points Card -->
      <div class="mb-8">
        <div class="bg-white rounded-3xl shadow-xl border border-gray-100 overflow-hidden">
          <div class="bg-gradient-to-r from-emerald-500 to-teal-600 p-6">
            <div class="flex items-center justify-between">
              <div class="flex items-center gap-4">
                <div
                  class="w-16 h-16 bg-white/20 backdrop-blur-sm rounded-2xl flex items-center justify-center"
                >
                  <svg class="w-8 h-8 text-white" viewBox="0 0 24 24" fill="currentColor">
                    <path
                      d="M12 2L15.09 8.26L22 9.27L17 14.14L18.18 21.02L12 17.77L5.82 21.02L7 14.14L2 9.27L8.91 8.26L12 2Z"
                    />
                  </svg>
                </div>
                <div>
                  <h3 class="text-white text-lg font-semibold">Điểm tích lũy</h3>
                  <div class="flex items-baseline gap-2">
                    <span v-if="loadingPoints" class="text-white">
                      <div
                        class="inline-block animate-spin rounded-full h-6 w-6 border-2 border-white/30 border-t-white"
                      ></div>
                    </span>
                    <span v-else class="text-3xl font-bold text-white">{{
                      points?.toLocaleString() || 0
                    }}</span>
                    <span class="text-white/80">điểm</span>
                  </div>
                </div>
              </div>
              <router-link
                to="/vouchers"
                class="px-6 py-3 bg-white text-emerald-600 rounded-2xl font-semibold hover:bg-gray-50 transition-colors shadow-lg"
              >
                Đổi voucher
              </router-link>
            </div>
          </div>
        </div>
      </div>

      <!-- Profile Quick View -->
      <div class="mb-8">
        <div class="bg-white rounded-3xl shadow-xl border border-gray-100 overflow-hidden">
          <div class="p-8">
            <h2 class="text-2xl font-bold text-gray-900 mb-6">Thông tin tài khoản</h2>

            <div class="grid md:grid-cols-2 gap-6">
              <!-- Basic Info -->
              <div class="space-y-4">
                <div class="flex items-center gap-3 p-4 bg-gray-50 rounded-2xl">
                  <div class="w-10 h-10 bg-blue-100 rounded-xl flex items-center justify-center">
                    <svg class="w-5 h-5 text-blue-600" viewBox="0 0 24 24" fill="currentColor">
                      <path
                        d="M12 12c2.21 0 4-1.79 4-4s-1.79-4-4-4-4 1.79-4 4 1.79 4 4 4zm0 2c-2.67 0-8 1.34-8 4v2h16v-2c0-2.66-5.33-4-8-4z"
                      />
                    </svg>
                  </div>
                  <div>
                    <p class="text-sm text-gray-500">Họ và tên</p>
                    <p class="font-medium text-gray-900">{{ profile?.name || "Chưa cập nhật" }}</p>
                  </div>
                </div>

                <div class="flex items-center gap-3 p-4 bg-gray-50 rounded-2xl">
                  <div class="w-10 h-10 bg-green-100 rounded-xl flex items-center justify-center">
                    <svg class="w-5 h-5 text-green-600" viewBox="0 0 24 24" fill="currentColor">
                      <path
                        d="M20 4H4c-1.1 0-1.99.9-1.99 2L2 18c0 1.1.9 2 2 2h16c1.1 0 2-.9 2-2V6c0-1.1-.9-2-2-2zm0 4l-8 5-8-5V6l8 5 8-5v2z"
                      />
                    </svg>
                  </div>
                  <div>
                    <p class="text-sm text-gray-500">Email</p>
                    <p class="font-medium text-gray-900 break-all">
                      {{ profile?.email || "Chưa cập nhật" }}
                    </p>
                  </div>
                </div>
              </div>

              <div class="space-y-4">
                <div class="flex items-center gap-3 p-4 bg-gray-50 rounded-2xl">
                  <div class="w-10 h-10 bg-purple-100 rounded-xl flex items-center justify-center">
                    <svg class="w-5 h-5 text-purple-600" viewBox="0 0 24 24" fill="currentColor">
                      <path
                        d="M6.62 10.79c1.44 2.83 3.76 5.14 6.59 6.59l2.2-2.2c.27-.27.67-.36 1.02-.24 1.12.37 2.33.57 3.57.57.55 0 1 .45 1 1V20c0 .55-.45 1-1 1-9.39 0-17-7.61-17-17 0-.55.45-1 1-1h3.5c.55 0 1 .45 1 1 0 1.25.2 2.45.57 3.57.11.35.03.74-.25 1.02l-2.2 2.2z"
                      />
                    </svg>
                  </div>
                  <div>
                    <p class="text-sm text-gray-500">Số điện thoại</p>
                    <p class="font-medium text-gray-900">{{ profile?.phone || "Chưa cập nhật" }}</p>
                  </div>
                </div>

                <div class="flex items-start gap-3 p-4 bg-gray-50 rounded-2xl">
                  <div
                    class="w-10 h-10 bg-red-100 rounded-xl flex items-center justify-center mt-1"
                  >
                    <svg class="w-5 h-5 text-red-600" viewBox="0 0 24 24" fill="currentColor">
                      <path
                        d="M12 2C8.13 2 5 5.13 5 9c0 5.25 7 13 7 13s7-7.75 7-13c0-3.87-3.13-7-7-7zm0 9.5c-1.38 0-2.5-1.12-2.5-2.5s1.12-2.5 2.5-2.5 2.5 1.12 2.5 2.5-1.12 2.5-2.5 2.5z"
                      />
                    </svg>
                  </div>
                  <div>
                    <p class="text-sm text-gray-500">Địa chỉ</p>
                    <p class="font-medium text-gray-900 leading-relaxed">
                      {{ profile?.address || "Chưa cập nhật" }}
                    </p>
                  </div>
                </div>
              </div>
            </div>
          </div>
        </div>
      </div>

      <!-- Action Cards -->
      <div class="grid md:grid-cols-2 gap-6 mb-8">
        <!-- Update Profile -->
        <router-link
          to="/update-profile"
          class="bg-white rounded-3xl shadow-xl border border-gray-100 overflow-hidden group hover:shadow-2xl transition-shadow block"
        >
          <div class="p-8">
            <div class="flex items-center gap-4 mb-4">
              <div
                class="w-14 h-14 bg-blue-100 rounded-2xl flex items-center justify-center group-hover:scale-110 transition-transform"
              >
                <svg class="w-7 h-7 text-blue-600" viewBox="0 0 24 24" fill="currentColor">
                  <path
                    d="M12 2C13.1 2 14 2.9 14 4C14 5.1 13.1 6 12 6C10.9 6 10 5.1 10 4C10 2.9 10.9 2 12 2ZM21 9V7L12 2L3 7V9C3 14.55 6.84 19.74 12 21C17.16 19.74 21 14.55 21 9Z"
                  />
                </svg>
              </div>
              <div>
                <h3 class="text-xl font-bold text-gray-900">Sửa thông tin</h3>
                <p class="text-gray-500">Cập nhật thông tin cá nhân</p>
              </div>
            </div>
            <div class="flex items-center justify-between">
              <p class="text-sm text-gray-600">Họ tên, email, số điện thoại, địa chỉ</p>
              <svg
                class="w-5 h-5 text-gray-400"
                fill="none"
                stroke="currentColor"
                viewBox="0 0 24 24"
              >
                <path
                  stroke-linecap="round"
                  stroke-linejoin="round"
                  stroke-width="2"
                  d="M9 5l7 7-7 7"
                />
              </svg>
            </div>
          </div>
        </router-link>

        <!-- Change Password -->
        <router-link
          to="/change-password"
          class="bg-white rounded-3xl shadow-xl border border-gray-100 overflow-hidden group hover:shadow-2xl transition-shadow block"
        >
          <div class="p-8">
            <div class="flex items-center gap-4 mb-4">
              <div
                class="w-14 h-14 bg-yellow-100 rounded-2xl flex items-center justify-center group-hover:scale-110 transition-transform"
              >
                <svg class="w-7 h-7 text-yellow-600" viewBox="0 0 24 24" fill="currentColor">
                  <path
                    d="M18 8h-1V6c0-2.76-2.24-5-5-5S7 3.24 7 6v2H6c-1.1 0-2 .9-2 2v10c0 1.1.9 2 2 2h12c1.1 0 2-.9 2-2V10c0-1.1-.9-2-2-2zm-6 9c-1.1 0-2-.9-2-2s.9-2 2-2 2 .9 2 2-.9 2-2 2zm3.1-9H8.9V6c0-1.71 1.39-3.1 3.1-3.1 1.71 0 3.1 1.39 3.1 3.1v2z"
                  />
                </svg>
              </div>
              <div>
                <h3 class="text-xl font-bold text-gray-900">Đổi mật khẩu</h3>
                <p class="text-gray-500">Thay đổi mật khẩu tài khoản</p>
              </div>
            </div>
            <div class="flex items-center justify-between">
              <p class="text-sm text-gray-600">Bảo mật tài khoản của bạn</p>
              <svg
                class="w-5 h-5 text-gray-400"
                fill="none"
                stroke="currentColor"
                viewBox="0 0 24 24"
              >
                <path
                  stroke-linecap="round"
                  stroke-linejoin="round"
                  stroke-width="2"
                  d="M9 5l7 7-7 7"
                />
              </svg>
            </div>
          </div>
        </router-link>
      </div>

      <!-- Additional Actions -->
      <div class="grid md:grid-cols-2 gap-6 mb-8">
        <!-- Order History -->
        <router-link
          to="/history"
          class="bg-white rounded-3xl shadow-xl border border-gray-100 overflow-hidden group hover:shadow-2xl transition-shadow block"
        >
          <div class="p-6">
            <div class="flex items-center gap-3">
              <div class="w-12 h-12 bg-green-100 rounded-xl flex items-center justify-center">
                <svg class="w-6 h-6 text-green-600" viewBox="0 0 24 24" fill="currentColor">
                  <path
                    d="M19 3H5c-1.1 0-2 .9-2 2v14c0 1.1.9 2 2 2h14c1.1 0 2-.9 2-2V5c0-1.1-.9-2-2-2zm-5 14H7v-2h7v2zm3-4H7v-2h10v2zm0-4H7V7h10v2z"
                  />
                </svg>
              </div>
              <div>
                <h4 class="font-semibold text-gray-900">Lịch sử đơn hàng</h4>
                <p class="text-sm text-gray-500">Xem đơn hàng & đặt bàn</p>
              </div>
              <svg
                class="w-5 h-5 text-gray-400 ml-auto"
                fill="none"
                stroke="currentColor"
                viewBox="0 0 24 24"
              >
                <path
                  stroke-linecap="round"
                  stroke-linejoin="round"
                  stroke-width="2"
                  d="M9 5l7 7-7 7"
                />
              </svg>
            </div>
          </div>
        </router-link>

        <!-- Logout -->
        <button
          @click="handleLogout"
          class="bg-white rounded-3xl shadow-xl border border-gray-100 overflow-hidden group hover:shadow-2xl transition-shadow block w-full text-left"
        >
          <div class="p-6">
            <div class="flex items-center gap-3">
              <div class="w-12 h-12 bg-red-100 rounded-xl flex items-center justify-center">
                <svg class="w-6 h-6 text-red-600" viewBox="0 0 24 24" fill="currentColor">
                  <path
                    d="M17 7l-1.41 1.41L18.17 11H8v2h10.17l-2.58 2.59L17 17l5-5zM4 5h8V3H4c-1.1 0-2 .9-2 2v14c0 1.1.9 2 2 2h8v-2H4V5z"
                  />
                </svg>
              </div>
              <div>
                <h4 class="font-semibold text-gray-900">Đăng xuất</h4>
                <p class="text-sm text-gray-500">Thoát khỏi tài khoản</p>
              </div>
              <svg
                class="w-5 h-5 text-gray-400 ml-auto"
                fill="none"
                stroke="currentColor"
                viewBox="0 0 24 24"
              >
                <path
                  stroke-linecap="round"
                  stroke-linejoin="round"
                  stroke-width="2"
                  d="M9 5l7 7-7 7"
                />
              </svg>
            </div>
          </div>
        </button>
      </div>
    </div>
  </div>
</template>
<script setup lang="ts">
import { computed, ref, onMounted } from "vue";
import { useAuthStore } from "@/stores/auth";
import { useRouter } from "vue-router";
import { getUserPoints } from "@/api/user.service";
import { toast } from "vue3-toastify";

const auth = useAuthStore();
const router = useRouter();
const points = ref<number>(0);
const loadingPoints = ref(false);

const profile = computed(() => {
  const u = auth.user as any;
  if (!u) return null;
  return {
    name: u.fullName || u.name,
    email: u.email,
    phone: u.phone,
    address: (u.address as string) || "",
    birthday: (u.birthday as string) || "",
    avatarUrl: u.avatarUrl || "",
  };
});

// Get membership tier based on points
const getMembershipTier = (points: number | undefined) => {
  if (!points) return "Khách hàng mới";
  if (points >= 10000) return "VIP Diamond";
  if (points >= 5000) return "VIP Gold";
  if (points >= 2000) return "VIP Silver";
  if (points >= 500) return "Thành viên";
  return "Khách hàng mới";
};

// Fetch user points
const fetchPoints = async () => {
  loadingPoints.value = true;
  try {
    const { data } = await getUserPoints();
    points.value = typeof data === "number" ? data : data?.points || 0;
  } catch (error) {
    console.error("Error fetching points:", error);
  } finally {
    loadingPoints.value = false;
  }
};

// Handle logout
const handleLogout = async () => {
  try {
    await auth.logout();
    toast.success("Đăng xuất thành công!");
    router.push("/login");
  } catch (error) {
    console.error("Error logging out:", error);
    toast.error("Có lỗi xảy ra khi đăng xuất");
  }
};

onMounted(() => {
  fetchPoints();
});
</script>

<style scoped>
/* Custom animations */
@keyframes fadeInUp {
  from {
    opacity: 0;
    transform: translateY(20px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}

@keyframes scaleIn {
  from {
    opacity: 0;
    transform: scale(0.95);
  }
  to {
    opacity: 1;
    transform: scale(1);
  }
}

.group:hover .group-hover\\:scale-110 {
  transform: scale(1.1);
}

/* Gradient background */
.bg-gradient-to-br {
  background-image: linear-gradient(to bottom right, var(--tw-gradient-stops));
}

/* Backdrop blur fallback */
@supports not (backdrop-filter: blur(8px)) {
  .backdrop-blur-sm {
    background-color: rgba(0, 0, 0, 0.1) !important;
  }
}

/* Custom scrollbar for modals */
.overflow-y-auto {
  scrollbar-width: thin;
  scrollbar-color: #e5e7eb #f9fafb;
}

.overflow-y-auto::-webkit-scrollbar {
  width: 6px;
}

.overflow-y-auto::-webkit-scrollbar-track {
  background: #f9fafb;
  border-radius: 3px;
}

.overflow-y-auto::-webkit-scrollbar-thumb {
  background: #e5e7eb;
  border-radius: 3px;
}

.overflow-y-auto::-webkit-scrollbar-thumb:hover {
  background: #d1d5db;
}

/* Focus styles */
input:focus,
textarea:focus {
  box-shadow: 0 0 0 3px rgba(159, 9, 9, 0.1);
}

/* Button animation */
button {
  transition: all 0.2s ease-in-out;
}

button:active {
  transform: scale(0.98);
}

/* Card hover effects */
.shadow-xl {
  transition: box-shadow 0.3s ease-in-out;
}

.hover\\:shadow-2xl:hover {
  box-shadow: 0 25px 50px -12px rgba(0, 0, 0, 0.25);
}
</style>
