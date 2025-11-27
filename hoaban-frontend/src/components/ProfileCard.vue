<template>
  <div
    class="relative overflow-hidden rounded-3xl bg-white/80 backdrop-blur-xl border border-white/60 shadow-[0_10px_40px_rgba(0,0,0,.08)] ring-1 ring-black/5"
  >
    <!-- Luxe gradient cover -->
    <div
      class="h-28 w-full bg-gradient-to-r from-[#9f0909] via-rose-500 to-teal-500 opacity-95"
    ></div>

    <!-- Header -->
    <div class="-mt-12 px-6 pb-6">
      <div class="flex flex-col md:flex-row md:items-end md:justify-between gap-5">
        <!-- Avatar + meta -->
        <div class="flex items-center gap-4">
          <div
            class="relative inline-flex items-center justify-center w-24 h-24 rounded-full ring-8 ring-white shadow-xl bg-gradient-to-br from-teal-500 to-emerald-500"
            aria-hidden="true"
          >
            <svg class="w-12 h-12 text-white/95" viewBox="0 0 24 24" fill="currentColor">
              <path
                d="M12 2C13.1 2 14 2.9 14 4C14 5.1 13.1 6 12 6C10.9 6 10 5.1 10 4C10 2.9 10.9 2 12 2ZM21 9V7L12 2L3 7V9C3 14.55 6.84 19.74 12 21C17.16 19.74 21 14.55 21 9ZM12 7C13.1 7 14 7.9 14 9S13.1 11 12 11 10 10.1 10 9 10.9 7 12 7ZM12 13C14.67 13 20 14.33 20 17V20H4V17C4 14.33 9.33 13 12 13Z"
              />
            </svg>
            <span
              class="absolute inset-0 rounded-full shadow-[inset_0_0_0_1px_rgba(255,255,255,.25)]"
            ></span>
          </div>

          <div>
            <h2
              class="text-2xl md:text-3xl font-bold text-gray-900 tracking-tight leading-tight"
              style="font-family: 'Playfair Display', serif"
            >
              {{ userInfo.name || "Khách hàng" }}
            </h2>
            <div class="mt-1 flex items-center gap-2 text-sm text-gray-500">
              <span
                class="inline-flex items-center gap-1 px-2.5 py-1 rounded-full bg-gray-100 border border-gray-200"
              >
                <span class="w-1.5 h-1.5 rounded-full bg-emerald-500"></span>
                Thành viên
              </span>
              <span class="hidden md:inline text-gray-300">•</span>
            </div>
          </div>
        </div>

        <!-- Actions -->
        <div class="flex items-center gap-2 flex-wrap">
          <button
            @click="$emit('editInfo')"
            class="inline-flex items-center gap-2 px-4 py-2 rounded-2xl border border-gray-200 text-gray-700 hover:bg-white active:scale-[.98] transition shadow-sm"
            aria-label="Chỉnh sửa thông tin"
          >
            <svg class="w-4 h-4" viewBox="0 0 24 24" fill="none" stroke="currentColor">
              <path
                stroke-linecap="round"
                stroke-linejoin="round"
                stroke-width="1.8"
                d="M16.862 4.487l1.651-1.651a2.2 2.2 0 113.111 3.111l-9.9 9.9L8 16l.053-3.724 8.81-7.789z"
              />
            </svg>
            Chỉnh sửa
          </button>
          <button
            @click="$emit('changePassword')"
            class="inline-flex items-center gap-2 px-4 py-2 rounded-2xl border border-orange-200 text-orange-700 hover:bg-orange-50 active:scale-[.98] transition shadow-sm"
            aria-label="Đổi mật khẩu"
          >
            <svg class="w-4 h-4" viewBox="0 0 24 24" fill="none" stroke="currentColor">
              <path
                stroke-linecap="round"
                stroke-linejoin="round"
                stroke-width="1.8"
                d="M15 7a2 2 0 012 2m4 0a6 6 0 01-7.743 5.743L11 17H9v2H7v2H4a1 1 0 01-1-1v-2.586a1 1 0 01.293-.707l5.964-5.964A6 6 0 1121 9z"
              />
            </svg>
            Đổi MK
          </button>
          <button
            @click="$emit('editAddress')"
            class="inline-flex items-center gap-2 px-4 py-2 rounded-2xl bg-[#9f0909] text-white hover:bg-[#800808] active:scale-[.98] transition shadow-md"
            aria-label="Cập nhật địa chỉ"
          >
            <svg class="w-4 h-4" viewBox="0 0 24 24" fill="currentColor">
              <path
                d="M12 2L4 7v7c0 5 8 8 8 8s8-3 8-8V7l-8-5zm0 2.18L18 7v6.82C18 17.82 14.41 20 12 20s-6-2.18-6-6.18V7l6-2.82z"
              />
            </svg>
            Địa chỉ
          </button>
        </div>
      </div>
    </div>

    <!-- Body -->
    <div class="px-6 pb-6">
      <!-- Loyalty Points Card -->
      <div
        class="mb-6 rounded-2xl border border-emerald-100 bg-gradient-to-br from-emerald-50 to-teal-50 p-6 shadow-sm"
      >
        <div class="flex items-center justify-between">
          <div class="flex items-center gap-4">
            <div
              class="flex h-14 w-14 items-center justify-center rounded-full bg-emerald-500 shadow-lg"
            >
              <svg class="w-8 h-8 text-white" viewBox="0 0 24 24" fill="currentColor">
                <path
                  d="M12 2L15.09 8.26L22 9.27L17 14.14L18.18 21.02L12 17.77L5.82 21.02L7 14.14L2 9.27L8.91 8.26L12 2Z"
                />
              </svg>
            </div>
            <div>
              <div class="text-xs font-medium text-emerald-700 uppercase tracking-wide">
                Điểm tích lũy
              </div>
              <div class="flex items-baseline gap-2 mt-1">
                <span v-if="loadingPoints" class="text-3xl font-bold text-emerald-900">
                  <div
                    class="inline-block animate-spin rounded-full h-6 w-6 border-[3px] border-emerald-300 border-t-transparent"
                  />
                </span>
                <span v-else class="text-3xl font-bold text-emerald-900">
                  {{ points?.toLocaleString() || 0 }}
                </span>
                <span class="text-sm text-emerald-600">điểm</span>
              </div>
            </div>
          </div>
        </div>
        <div class="mt-4 pt-4 border-t border-emerald-200 flex items-center justify-between">
          <p class="text-xs text-emerald-700">💡 Sử dụng điểm để đổi voucher hoặc giảm giá!</p>
          <router-link
            to="/vouchers"
            class="inline-flex items-center gap-1 px-3 py-1.5 bg-emerald-600 text-white text-xs font-medium rounded-lg hover:bg-emerald-700 transition-colors"
          >
            <svg class="w-4 h-4" fill="none" stroke="currentColor" viewBox="0 0 24 24">
              <path
                stroke-linecap="round"
                stroke-linejoin="round"
                stroke-width="2"
                d="M15 5v2m0 4v2m0 4v2M5 5a2 2 0 00-2 2v3a2 2 0 110 4v3a2 2 0 002 2h14a2 2 0 002-2v-3a2 2 0 110-4V7a2 2 0 00-2-2H5z"
              />
            </svg>
            Đổi voucher
          </router-link>
        </div>
      </div>

      <div class="grid grid-cols-1 lg:grid-cols-2 gap-6">
        <!-- Personal Info -->
        <div class="rounded-2xl border border-gray-100 bg-white p-5 shadow-sm">
          <div class="flex items-center justify-between mb-4">
            <h3
              class="text-lg font-semibold text-gray-900"
              style="font-family: 'Roboto', sans-serif"
            >
              Thông tin cá nhân
            </h3>
            <button
              @click="$emit('editInfo')"
              class="text-[#9f0909] hover:text-[#800808] text-sm font-medium"
            >
              Chỉnh sửa
            </button>
          </div>

          <div class="space-y-4">
            <div class="flex items-start gap-3">
              <div
                class="mt-1 flex h-9 w-9 items-center justify-center rounded-lg bg-gray-100 text-gray-700"
              >
                <svg class="w-4.5 h-4.5" viewBox="0 0 24 24" fill="currentColor">
                  <path
                    d="M12 12c2.761 0 5-2.686 5-6s-2.239-6-5-6-5 2.686-5 6 2.239 6 5 6zm0 2c-4 0-12 2.007-12 6v2h24v-2c0-3.993-8-6-12-6z"
                  />
                </svg>
              </div>
              <div class="w-full">
                <div class="text-xs uppercase tracking-wide text-gray-500">Họ và tên</div>
                <div class="mt-1 rounded-xl bg-gray-50 px-3 py-2.5 text-gray-900">
                  {{ userInfo.name || "Chưa cập nhật" }}
                </div>
              </div>
            </div>

            <div class="flex items-start gap-3">
              <div
                class="mt-1 flex h-9 w-9 items-center justify-center rounded-lg bg-gray-100 text-gray-700"
              >
                <svg class="w-4.5 h-4.5" viewBox="0 0 24 24" fill="currentColor">
                  <path
                    d="M12 12.713l11.985-7.99A1 1 0 0023 3H1a1 1 0 00-.985 1.723l11.985 7.99z"
                  />
                  <path d="M12 14.987L0 7.333V20a1 1 0 001 1h22a1 1 0 001-1V7.333L12 14.987z" />
                </svg>
              </div>
              <div class="w-full">
                <div class="flex items-center justify-between">
                  <div class="text-xs uppercase tracking-wide text-gray-500">Email</div>
                  <button
                    @click="toggleEmailVisibility"
                    class="text-xs font-medium text-[#9f0909] hover:text-[#800808]"
                    :aria-pressed="showEmail"
                  >
                    {{ showEmail ? "Ẩn" : "Hiện" }}
                  </button>
                </div>
                <div class="mt-1 rounded-xl bg-gray-50 px-3 py-2.5 text-gray-900 break-all">
                  {{ showEmail ? userInfo.email || "Chưa cập nhật" : maskEmail(userInfo.email) }}
                </div>
              </div>
            </div>

            <div class="flex items-start gap-3">
              <div
                class="mt-1 flex h-9 w-9 items-center justify-center rounded-lg bg-gray-100 text-gray-700"
              >
                <svg class="w-4.5 h-4.5" viewBox="0 0 24 24" fill="currentColor">
                  <path
                    d="M6.62 10.79a15.09 15.09 0 006.59 6.59l2.2-2.2a1 1 0 011.05-.24 11.36 11.36 0 003.56.57 1 1 0 011 1V20a1 1 0 01-1 1A17 17 0 013 4a1 1 0 011-1h3.49a1 1 0 011 1 11.36 11.36 0 00.57 3.56 1 1 0 01-.24 1.05l-2.2 2.18z"
                  />
                </svg>
              </div>
              <div class="w-full">
                <div class="text-xs uppercase tracking-wide text-gray-500">Số điện thoại</div>
                <div class="mt-1 rounded-xl bg-gray-50 px-3 py-2.5 text-gray-900">
                  {{ userInfo.phone || "Chưa cập nhật" }}
                </div>
              </div>
            </div>

            <div class="flex items-start gap-3">
              <div
                class="mt-1 flex h-9 w-9 items-center justify-center rounded-lg bg-gray-100 text-gray-700"
              >
                <svg class="w-4.5 h-4.5" viewBox="0 0 24 24" fill="currentColor">
                  <path
                    d="M19 4h-1V2h-2v2H8V2H6v2H5a2 2 0 00-2 2v13a2 2 0 002 2h14a2 2 0 002-2V6a2 2 0 00-2-2zM5 9h14v10H5V9z"
                  />
                </svg>
              </div>
              <div class="w-full">
                <div class="text-xs uppercase tracking-wide text-gray-500">Ngày sinh</div>
                <div class="mt-1 rounded-xl bg-gray-50 px-3 py-2.5 text-gray-900">
                  {{ userInfo.birthday || "Chưa cập nhật" }}
                </div>
              </div>
            </div>
          </div>
        </div>

        <!-- Address -->
        <div class="rounded-2xl border border-gray-100 bg-white p-5 shadow-sm">
          <div class="flex items-center justify-between mb-4">
            <h3
              class="text-lg font-semibold text-gray-900"
              style="font-family: 'Roboto', sans-serif"
            >
              Địa chỉ
            </h3>
            <button
              @click="$emit('editAddress')"
              class="text-[#9f0909] hover:text-[#800808] text-sm font-medium"
            >
              {{ userInfo.address ? "Chỉnh sửa" : "Thêm địa chỉ" }}
            </button>
          </div>

          <div v-if="userInfo.address" class="space-y-3">
            <div class="flex items-start gap-3">
              <div
                class="mt-1 flex h-9 w-9 items-center justify-center rounded-lg bg-gray-100 text-gray-700"
              >
                <svg class="w-4.5 h-4.5" viewBox="0 0 24 24" fill="currentColor">
                  <path
                    d="M12 2a7 7 0 00-7 7c0 5.25 7 13 7 13s7-7.75 7-13a7 7 0 00-7-7zm0 9.5a2.5 2.5 0 112.5-2.5A2.5 2.5 0 0112 11.5z"
                  />
                </svg>
              </div>
              <div class="w-full">
                <div class="flex items-center justify-between">
                  <div class="text-xs uppercase tracking-wide text-gray-500">Địa chỉ mặc định</div>
                  <button
                    @click="toggleAddressVisibility"
                    class="text-xs font-medium text-[#9f0909] hover:text-[#800808]"
                    :aria-pressed="showAddress"
                  >
                    {{ showAddress ? "Ẩn" : "Hiện" }}
                  </button>
                </div>
                <div class="mt-1 rounded-xl bg-gray-50 px-3 py-2.5 text-gray-900 break-words">
                  {{ showAddress ? userInfo.address : maskAddress(userInfo.address) }}
                </div>
              </div>
            </div>
          </div>

          <div v-else class="text-center py-10">
            <p class="text-gray-500">Chưa có địa chỉ nào</p>
            <button
              @click="$emit('editAddress')"
              class="mt-3 inline-flex items-center gap-2 text-[#9f0909] hover:text-[#800808] font-medium"
            >
              <svg class="w-4 h-4" viewBox="0 0 24 24" fill="currentColor">
                <path d="M19 11h-6V5h-2v6H5v2h6v6h2v-6h6z" />
              </svg>
              Thêm địa chỉ đầu tiên
            </button>
          </div>
        </div>
      </div>
    </div>

    <!-- Soft corner glows -->
    <div
      class="pointer-events-none absolute -right-10 -bottom-10 w-40 h-40 rounded-full bg-rose-100 blur-2xl"
    ></div>
    <div
      class="pointer-events-none absolute -left-10 -top-10 w-40 h-40 rounded-full bg-teal-100 blur-2xl"
    ></div>
  </div>
</template>

<script setup>
import { ref, defineProps, defineEmits } from "vue";

defineProps({
  userInfo: {
    type: Object,
    required: true,
  },
  points: {
    type: Number,
    default: 0,
  },
  loadingPoints: {
    type: Boolean,
    default: false,
  },
});

defineEmits(["editInfo", "editAddress", "changePassword"]);

const showEmail = ref(false);
const showAddress = ref(false);

const toggleEmailVisibility = () => (showEmail.value = !showEmail.value);
const toggleAddressVisibility = () => (showAddress.value = !showAddress.value);

const maskEmail = (email) => {
  if (!email) return "Chưa cập nhật";
  const [username, domain] = email.split("@");
  if (!username || !domain) return email;
  const maskedUsername =
    username.charAt(0) +
    "*".repeat(Math.max(0, username.length - 2)) +
    (username.length > 1 ? username.charAt(username.length - 1) : "");
  return `${maskedUsername}@${domain}`;
};

const maskAddress = (address) => {
  if (!address) return "Chưa cập nhật";
  if (address.length <= 10) return "*".repeat(address.length);
  return (
    address.substring(0, 5) +
    "*".repeat(Math.max(0, address.length - 10)) +
    address.substring(address.length - 5)
  );
};
</script>

<style scoped></style>
