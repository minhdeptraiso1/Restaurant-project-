<template>
  <LoadingOverlay :show="loading" message="Đang cập nhật thông tin..." />

  <div class="min-h-screen flex items-center justify-center bg-gray-50 p-4">
    <div class="bg-white p-8 rounded-2xl shadow-xl w-full max-w-lg">
      <!-- Header -->
      <div class="flex items-center justify-between mb-6">
        <button @click="goBack" class="flex items-center gap-2 text-gray-600 hover:text-gray-800">
          <svg class="w-5 h-5" fill="none" stroke="currentColor" viewBox="0 0 24 24">
            <path
              stroke-linecap="round"
              stroke-linejoin="round"
              stroke-width="2"
              d="M15 19l-7-7 7-7"
            />
          </svg>
          Quay lại
        </button>
        <h2 class="text-2xl font-bold">Cập nhật thông tin</h2>
        <div class="w-16"></div>
        <!-- Spacer -->
      </div>

      <form @submit.prevent="onSubmit" class="space-y-6">
        <!-- Avatar Upload -->
        <div class="flex flex-col items-center gap-4">
          <div class="relative group">
            <div
              class="w-24 h-24 rounded-full border-4 border-gray-200 shadow-lg group-hover:border-[#9f0909] transition-colors overflow-hidden"
            >
              <img
                v-if="avatarPreview && avatarPreview !== '/default-avatar.png'"
                :src="avatarPreview"
                class="w-full h-full object-cover"
                alt="Avatar"
              />
              <div
                v-else
                class="w-full h-full flex items-center justify-center bg-gradient-to-br from-gray-100 to-gray-200"
              >
                <svg class="w-12 h-12 text-gray-400" viewBox="0 0 24 24" fill="currentColor">
                  <path
                    d="M12 2C13.1 2 14 2.9 14 4C14 5.1 13.1 6 12 6C10.9 6 10 5.1 10 4C10 2.9 10.9 2 12 2ZM21 9V7L12 2L3 7V9C3 14.55 6.84 19.74 12 21C17.16 19.74 21 14.55 21 9Z"
                  />
                </svg>
              </div>
            </div>
            <label
              class="absolute inset-0 bg-black bg-opacity-50 rounded-full flex items-center justify-center opacity-0 group-hover:opacity-100 transition-opacity cursor-pointer"
            >
              <input
                type="file"
                accept="image/jpeg,image/jpg,image/png"
                @change="onFileChange"
                class="hidden"
              />
              <svg class="w-8 h-8 text-white" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                <path
                  stroke-linecap="round"
                  stroke-linejoin="round"
                  stroke-width="2"
                  d="M3 9a2 2 0 012-2h.93a2 2 0 001.664-.89l.812-1.22A2 2 0 0110.07 4h3.86a2 2 0 011.664.89l.812 1.22A2 2 0 0018.07 7H19a2 2 0 012 2v9a2 2 0 01-2 2H5a2 2 0 01-2-2V9z"
                />
                <path
                  stroke-linecap="round"
                  stroke-linejoin="round"
                  stroke-width="2"
                  d="M15 13a3 3 0 11-6 0 3 3 0 016 0z"
                />
              </svg>
            </label>
          </div>
          <label
            class="cursor-pointer bg-gray-100 hover:bg-gray-200 px-4 py-2 rounded-lg text-sm font-medium transition-colors"
          >
            <input
              type="file"
              accept="image/jpeg,image/jpg,image/png"
              @change="onFileChange"
              class="hidden"
            />
            Chọn ảnh đại diện
          </label>
          <p class="text-xs text-gray-500 text-center">Hỗ trợ JPG, PNG. Tối đa 5MB</p>
        </div>

        <!-- Form Fields -->
        <div class="space-y-4">
          <div>
            <label class="block text-sm font-medium text-gray-700 mb-2">Họ và tên *</label>
            <input
              v-model="form.fullName"
              type="text"
              class="w-full px-4 py-3 border border-gray-300 rounded-xl focus:ring-2 focus:ring-[#9f0909] focus:border-transparent outline-none transition-colors"
              placeholder="Nhập họ và tên"
              required
            />
          </div>

          <div>
            <label class="block text-sm font-medium text-gray-700 mb-2">Email</label>
            <input
              v-model="form.email"
              type="email"
              class="w-full px-4 py-3 border border-gray-300 rounded-xl bg-gray-50 text-gray-500 cursor-not-allowed outline-none"
              placeholder="Email không thể thay đổi"
              readonly
              disabled
            />
            <p class="text-xs text-gray-500 mt-1">Email không thể thay đổi</p>
          </div>

          <div>
            <label class="block text-sm font-medium text-gray-700 mb-2">Số điện thoại *</label>
            <input
              v-model="form.phone"
              type="tel"
              class="w-full px-4 py-3 border border-gray-300 rounded-xl focus:ring-2 focus:ring-[#9f0909] focus:border-transparent outline-none transition-colors"
              placeholder="Ví dụ: 0987654321"
              pattern="[0-9]{10,11}"
              maxlength="11"
              required
            />
            <p class="text-xs text-gray-500 mt-1">Nhập 10-11 chữ số</p>
          </div>

          <div>
            <label class="block text-sm font-medium text-gray-700 mb-2">Địa chỉ</label>
            <textarea
              v-model="form.address"
              rows="3"
              class="w-full px-4 py-3 border border-gray-300 rounded-xl focus:ring-2 focus:ring-[#9f0909] focus:border-transparent outline-none transition-colors resize-none"
              placeholder="Nhập địa chỉ (tùy chọn)"
            ></textarea>
          </div>
        </div>

        <!-- Submit Button -->
        <div class="pt-4">
          <button
            type="submit"
            :disabled="loading"
            class="w-full py-3 bg-[#9f0909] text-white rounded-xl font-bold hover:bg-[#800808] transition-colors disabled:opacity-60 disabled:cursor-not-allowed flex items-center justify-center gap-2"
          >
            <template v-if="loading">
              <svg class="animate-spin w-5 h-5" fill="none" viewBox="0 0 24 24">
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
              Đang lưu...
            </template>
            <template v-else>
              <svg class="w-5 h-5" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                <path
                  stroke-linecap="round"
                  stroke-linejoin="round"
                  stroke-width="2"
                  d="M5 13l4 4L19 7"
                />
              </svg>
              Lưu thay đổi
            </template>
          </button>
        </div>
      </form>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from "vue";
import { updateUserInfo } from "@/api/user.service";
import { useAuthStore } from "@/stores/auth";
import { useRouter } from "vue-router";
import { toast } from "vue3-toastify";
import LoadingOverlay from "@/components/LoadingOverlay.vue";

const auth = useAuthStore();
const router = useRouter();

const form = reactive({
  fullName: "",
  email: "",
  phone: "",
  address: "",
  avatarUrl: "",
});

const avatarFile = ref<File | null>(null);
const avatarPreview = ref("");
const loading = ref(false);

// Load current user info
onMounted(() => {
  if (auth.user) {
    form.fullName = auth.user.fullName || "";
    form.email = auth.user.email || "";
    form.phone = (auth.user as any).phone || "";
    form.address = (auth.user as any).address || "";
    form.avatarUrl = (auth.user as any).avatarUrl || "";
    avatarPreview.value = form.avatarUrl || "/default-avatar.png";
  }
});

const onFileChange = (e: Event) => {
  const files = (e.target as HTMLInputElement).files;
  if (files && files[0]) {
    const file = files[0];

    // Validate file type
    const allowedTypes = ["image/jpeg", "image/jpg", "image/png"];
    if (!allowedTypes.includes(file.type)) {
      toast.error("Vui lòng chọn file ảnh định dạng JPG hoặc PNG");
      return;
    }

    // Validate file size (max 5MB)
    if (file.size > 5 * 1024 * 1024) {
      toast.error("Kích thước ảnh không được vượt quá 5MB");
      return;
    }

    avatarFile.value = file;

    // Create preview
    const reader = new FileReader();
    reader.onload = (e) => {
      avatarPreview.value = e.target?.result as string;
    };
    reader.readAsDataURL(file);
  }
};

const uploadImage = async (file: File): Promise<string> => {
  // Convert to base64 for demo
  // In production, upload to your server and return URL
  return new Promise((resolve, reject) => {
    const reader = new FileReader();
    reader.onload = () => resolve(reader.result as string);
    reader.onerror = reject;
    reader.readAsDataURL(file);
  });
};

const onSubmit = async () => {
  // Validate full name
  if (!form.fullName.trim()) {
    toast.error("Vui lòng nhập họ và tên");
    return;
  }

  if (form.fullName.trim().length < 2) {
    toast.error("Họ và tên phải có ít nhất 2 ký tự");
    return;
  }

  // Validate phone
  if (!form.phone.trim()) {
    toast.error("Vui lòng nhập số điện thoại");
    return;
  }

  const phoneRegex = /^[0-9]{10,11}$/;
  if (!phoneRegex.test(form.phone.trim())) {
    toast.error("Số điện thoại không hợp lệ (10-11 số)");
    return;
  }

  loading.value = true;

  try {
    let avatarUrl = form.avatarUrl;

    // Upload avatar if file selected
    if (avatarFile.value) {
      toast.info("Đang upload ảnh...");
      avatarUrl = await uploadImage(avatarFile.value);
    }

    // Update user info - không cần truyền ID
    await updateUserInfo({
      fullName: form.fullName.trim(),
      phone: form.phone.trim(),
      address: form.address.trim() || undefined,
      avatarUrl: avatarUrl || undefined,
    });

    // Refresh user data
    await auth.refreshUserInfo();

    toast.success("Cập nhật thông tin thành công!");

    // Navigate back to profile after 1.5s
    setTimeout(() => {
      router.push("/profile");
    }, 1500);
  } catch (e: any) {
    console.error("Update failed:", e);
    const errorMessage = e?.response?.data?.message || e.message || "Cập nhật thất bại";
    toast.error(errorMessage);
  } finally {
    loading.value = false;
  }
};

const goBack = () => {
  router.push("/profile");
};
</script>
