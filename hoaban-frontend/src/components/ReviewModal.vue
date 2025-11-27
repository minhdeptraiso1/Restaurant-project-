<template>
  <div
    v-if="show"
    class="fixed inset-0 bg-black bg-opacity-50 flex items-center justify-center z-50 p-4"
    @click.self="close"
  >
    <div class="bg-white rounded-lg shadow-xl max-w-md w-full p-6">
      <div class="flex items-center justify-between mb-4">
        <h3 class="text-xl font-bold text-gray-900">Đánh giá đơn hàng</h3>
        <button @click="close" class="text-gray-400 hover:text-gray-600 transition-colors">
          <svg class="w-6 h-6" fill="none" stroke="currentColor" viewBox="0 0 24 24">
            <path
              stroke-linecap="round"
              stroke-linejoin="round"
              stroke-width="2"
              d="M6 18L18 6M6 6l12 12"
            />
          </svg>
        </button>
      </div>

      <form @submit.prevent="submitReview" class="space-y-4">
        <!-- Order ID Display -->
        <div class="bg-gray-50 rounded-lg p-3">
          <div class="text-sm text-gray-600">Mã đơn hàng</div>
          <div class="font-medium text-gray-900">{{ orderId.slice(0, 8) }}</div>
        </div>

        <!-- Rating -->
        <div>
          <label class="block text-sm font-medium text-gray-700 mb-2">
            Đánh giá <span class="text-red-500">*</span>
          </label>
          <div class="flex items-center gap-2">
            <button
              v-for="star in 5"
              :key="star"
              type="button"
              @click="form.rating = star"
              class="transition-transform hover:scale-110"
            >
              <svg
                class="w-10 h-10"
                :class="star <= form.rating ? 'text-yellow-400 fill-current' : 'text-gray-300'"
                fill="none"
                stroke="currentColor"
                viewBox="0 0 24 24"
              >
                <path
                  stroke-linecap="round"
                  stroke-linejoin="round"
                  stroke-width="2"
                  d="M11.049 2.927c.3-.921 1.603-.921 1.902 0l1.519 4.674a1 1 0 00.95.69h4.915c.969 0 1.371 1.24.588 1.81l-3.976 2.888a1 1 0 00-.363 1.118l1.518 4.674c.3.922-.755 1.688-1.538 1.118l-3.976-2.888a1 1 0 00-1.176 0l-3.976 2.888c-.783.57-1.838-.197-1.538-1.118l1.518-4.674a1 1 0 00-.363-1.118l-3.976-2.888c-.784-.57-.38-1.81.588-1.81h4.914a1 1 0 00.951-.69l1.519-4.674z"
                />
              </svg>
            </button>
          </div>
          <div class="mt-2 text-sm text-gray-600">
            {{ getRatingText(form.rating) }}
          </div>
        </div>

        <!-- Comment -->
        <div>
          <label class="block text-sm font-medium text-gray-700 mb-2"> Nhận xét (tùy chọn) </label>
          <textarea
            v-model="form.comment"
            rows="4"
            class="w-full px-3 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-emerald-500 focus:border-transparent resize-none"
            placeholder="Chia sẻ trải nghiệm của bạn..."
          ></textarea>
        </div>

        <!-- Submit Button -->
        <div class="flex gap-3 justify-end pt-2">
          <button
            type="button"
            @click="close"
            class="px-4 py-2 border border-gray-300 text-gray-700 rounded-lg hover:bg-gray-50 transition-colors"
          >
            Hủy
          </button>
          <button
            type="submit"
            :disabled="submitting || form.rating === 0"
            class="px-6 py-2 bg-emerald-600 text-white rounded-lg hover:bg-emerald-700 transition-colors disabled:opacity-50 disabled:cursor-not-allowed flex items-center gap-2"
          >
            <svg v-if="submitting" class="animate-spin h-4 w-4" fill="none" viewBox="0 0 24 24">
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
            <span>{{ submitting ? "Đang gửi..." : "Gửi đánh giá" }}</span>
          </button>
        </div>
      </form>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, watch } from "vue";
import { createReview } from "@/api/reviews.service";
import { toast } from "vue3-toastify";
import { useAuthStore } from "@/stores/auth";

interface Props {
  show: boolean;
  orderId: string;
}

const props = defineProps<Props>();
const emit = defineEmits<{
  (e: "close"): void;
  (e: "success"): void;
}>();

const authStore = useAuthStore();
const submitting = ref(false);
const form = ref({
  rating: 0,
  comment: "",
});

// Reset form when modal opens
watch(
  () => props.show,
  (newVal) => {
    if (newVal) {
      form.value = {
        rating: 0,
        comment: "",
      };
    }
  }
);

const getRatingText = (rating: number) => {
  const texts: Record<number, string> = {
    0: "Chọn số sao để đánh giá",
    1: "⭐ Rất tệ",
    2: "⭐⭐ Tệ",
    3: "⭐⭐⭐ Trung bình",
    4: "⭐⭐⭐⭐ Tốt",
    5: "⭐⭐⭐⭐⭐ Xuất sắc",
  };
  return texts[rating] || "";
};

const submitReview = async () => {
  if (form.value.rating === 0) {
    toast.error("Vui lòng chọn số sao đánh giá");
    return;
  }

  submitting.value = true;
  try {
    await createReview({
      orderId: props.orderId,
      rating: form.value.rating,
      comment: form.value.comment || undefined,
    });
    toast.success("Cảm ơn bạn đã đánh giá!");
    emit("success");
    close();
  } catch (error: any) {
    console.error("Error submitting review:", error);
    toast.error(error?.response?.data?.message || "Không thể gửi đánh giá");
  } finally {
    submitting.value = false;
  }
};

const close = () => {
  emit("close");
};
</script>
