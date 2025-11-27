<script setup lang="ts">
import { ref, onMounted, computed } from "vue";
import { toast } from "vue3-toastify";
import { getAllReviews, deleteReview } from "@/api/reviews.service";
import type { Review } from "@/types";
import AdminPageHeader from "@/components/AdminPageHeader.vue";
import AdminLoadingSkeleton from "@/components/AdminLoadingSkeleton.vue";
import AdminEmptyState from "@/components/AdminEmptyState.vue";
import ConfirmModal from "@/components/ConfirmModal.vue";

// Data
const reviews = ref<Review[]>([]);
const loading = ref(false);
const searchQuery = ref("");

// Delete modal
const showDeleteModal = ref(false);
const deletingReview = ref<Review | null>(null);

// Computed
const filteredReviews = computed(() => {
  const query = searchQuery.value.toLowerCase().trim();
  if (!query) return reviews.value;

  return reviews.value.filter(
    (review) =>
      review.customerName.toLowerCase().includes(query) ||
      (review.comment && review.comment.toLowerCase().includes(query)) ||
      review.rating.toString().includes(query)
  );
});

// Methods
const loadReviews = async () => {
  loading.value = true;
  try {
    const { data } = await getAllReviews();
    reviews.value = data.sort(
      (a, b) => new Date(b.createdAt).getTime() - new Date(a.createdAt).getTime()
    );
  } catch (error: any) {
    console.error("Error loading reviews:", error);
    toast.error(error?.response?.data?.message || "Không thể tải danh sách đánh giá");
  } finally {
    loading.value = false;
  }
};

const formatDate = (dateString: string) => {
  const date = new Date(dateString);
  return date.toLocaleDateString("vi-VN", {
    year: "numeric",
    month: "2-digit",
    day: "2-digit",
    hour: "2-digit",
    minute: "2-digit",
  });
};

const getRatingStars = (rating: number) => {
  return "⭐".repeat(rating);
};

const getRatingColor = (rating: number) => {
  if (rating >= 4) return "text-green-600 bg-green-50";
  if (rating === 3) return "text-yellow-600 bg-yellow-50";
  return "text-red-600 bg-red-50";
};

const openDeleteModal = (review: Review) => {
  deletingReview.value = review;
  showDeleteModal.value = true;
};

const closeDeleteModal = () => {
  showDeleteModal.value = false;
  deletingReview.value = null;
};

const confirmDelete = async () => {
  if (!deletingReview.value) return;

  try {
    await deleteReview(deletingReview.value.id);
    toast.success("Đã xóa đánh giá thành công");
    closeDeleteModal();
    await loadReviews();
  } catch (error: any) {
    console.error("Error deleting review:", error);
    toast.error(error?.response?.data?.message || "Không thể xóa đánh giá");
  }
};

// Statistics
const averageRating = computed(() => {
  if (reviews.value.length === 0) return 0;
  const sum = reviews.value.reduce((acc, review) => acc + review.rating, 0);
  return (sum / reviews.value.length).toFixed(1);
});

const ratingDistribution = computed(() => {
  const dist = { 5: 0, 4: 0, 3: 0, 2: 0, 1: 0 };
  reviews.value.forEach((review) => {
    dist[review.rating as keyof typeof dist]++;
  });
  return dist;
});

onMounted(loadReviews);
</script>

<template>
  <div class="space-y-6 text-white">
    <!-- Header -->
    <AdminPageHeader title="⭐ Quản lý Đánh giá" subtitle="Xem và quản lý đánh giá từ khách hàng" />

    <!-- Statistics Cards -->
    <div class="grid grid-cols-1 md:grid-cols-4 gap-4">
      <div
        class="rounded-2xl bg-white/5 backdrop-blur-md border border-white/10 shadow p-5 text-center"
      >
        <div class="text-3xl font-bold text-emerald-400">{{ reviews.length }}</div>
        <div class="text-sm text-white/70 mt-1">Tổng đánh giá</div>
      </div>
      <div
        class="rounded-2xl bg-white/5 backdrop-blur-md border border-white/10 shadow p-5 text-center"
      >
        <div class="text-3xl font-bold text-yellow-400">{{ averageRating }}</div>
        <div class="text-sm text-white/70 mt-1">Điểm trung bình</div>
      </div>
      <div
        class="rounded-2xl bg-white/5 backdrop-blur-md border border-white/10 shadow p-5 text-center"
      >
        <div class="text-3xl font-bold text-green-400">{{ ratingDistribution[5] }}</div>
        <div class="text-sm text-white/70 mt-1">5 sao ⭐⭐⭐⭐⭐</div>
      </div>
      <div
        class="rounded-2xl bg-white/5 backdrop-blur-md border border-white/10 shadow p-5 text-center"
      >
        <div class="text-3xl font-bold text-blue-400">{{ ratingDistribution[4] }}</div>
        <div class="text-sm text-white/70 mt-1">4 sao ⭐⭐⭐⭐</div>
      </div>
    </div>

    <!-- Search Bar -->
    <div class="rounded-2xl bg-white/5 backdrop-blur-md border border-white/10 shadow p-5">
      <div class="relative">
        <span class="absolute left-3 top-2.5 opacity-70">🔎</span>
        <input
          v-model="searchQuery"
          placeholder="Tìm kiếm theo tên khách hàng, nội dung đánh giá..."
          class="w-full bg-white/5 border border-white/10 rounded-lg pl-9 pr-3 py-2 outline-none focus:border-emerald-400"
        />
      </div>
    </div>

    <!-- Reviews List -->
    <div class="rounded-2xl bg-white/5 backdrop-blur-md border border-white/10 shadow p-6">
      <div class="flex items-center justify-between mb-4">
        <h3 class="font-semibold text-lg">Danh sách đánh giá</h3>
        <div class="text-sm text-white/70">
          Hiển thị: <span class="text-white font-medium">{{ filteredReviews.length }}</span> /
          {{ reviews.length }}
        </div>
      </div>

      <AdminLoadingSkeleton v-if="loading" />

      <AdminEmptyState
        v-else-if="filteredReviews.length === 0 && !searchQuery"
        icon="⭐"
        title="Chưa có đánh giá"
        description="Chưa có khách hàng nào đánh giá"
      />

      <AdminEmptyState
        v-else-if="filteredReviews.length === 0 && searchQuery"
        icon="🔍"
        title="Không tìm thấy"
        description="Không có đánh giá nào phù hợp với từ khóa tìm kiếm"
      />

      <div v-else class="space-y-4">
        <div
          v-for="review in filteredReviews"
          :key="review.id"
          class="rounded-xl border border-white/10 bg-white/5 hover:bg-white/10 transition p-5"
        >
          <div class="flex items-start justify-between gap-4">
            <div class="flex-1">
              <!-- Customer Name & Rating -->
              <div class="flex items-center gap-3 mb-3">
                <div class="flex items-center gap-2">
                  <svg
                    class="w-6 h-6 text-emerald-400"
                    fill="none"
                    stroke="currentColor"
                    viewBox="0 0 24 24"
                  >
                    <path
                      stroke-linecap="round"
                      stroke-linejoin="round"
                      stroke-width="2"
                      d="M16 7a4 4 0 11-8 0 4 4 0 018 0zM12 14a7 7 0 00-7 7h14a7 7 0 00-7-7z"
                    />
                  </svg>
                  <span class="font-semibold text-lg">{{ review.customerName }}</span>
                </div>

                <span
                  :class="[
                    'px-3 py-1 rounded-full text-sm font-medium',
                    getRatingColor(review.rating),
                  ]"
                >
                  {{ getRatingStars(review.rating) }} {{ review.rating }}/5
                </span>
              </div>

              <!-- Comment -->
              <div v-if="review.comment" class="mb-3">
                <div class="bg-white/5 border border-white/10 rounded-lg p-3 text-white/90 text-sm">
                  <svg
                    class="w-5 h-5 text-emerald-400 inline-block mr-2"
                    fill="none"
                    stroke="currentColor"
                    viewBox="0 0 24 24"
                  >
                    <path
                      stroke-linecap="round"
                      stroke-linejoin="round"
                      stroke-width="2"
                      d="M7 8h10M7 12h4m1 8l-4-4H5a2 2 0 01-2-2V6a2 2 0 012-2h14a2 2 0 012 2v8a2 2 0 01-2 2h-3l-4 4z"
                    />
                  </svg>
                  {{ review.comment }}
                </div>
              </div>
              <div v-else class="mb-3 text-white/50 text-sm italic">Không có nhận xét</div>

              <!-- Date & ID -->
              <div class="flex items-center gap-4 text-xs text-white/60">
                <div class="flex items-center gap-1">
                  <svg class="w-4 h-4" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                    <path
                      stroke-linecap="round"
                      stroke-linejoin="round"
                      stroke-width="2"
                      d="M8 7V3m8 4V3m-9 8h10M5 21h14a2 2 0 002-2V7a2 2 0 00-2-2H5a2 2 0 00-2 2v12a2 2 0 002 2z"
                    />
                  </svg>
                  <span>{{ formatDate(review.createdAt) }}</span>
                </div>
                <div class="flex items-center gap-1">
                  <span class="font-mono">ID: {{ review.id.slice(0, 8) }}</span>
                </div>
              </div>
            </div>

            <!-- Actions -->
            <div class="flex flex-col gap-2 shrink-0">
              <button
                @click="openDeleteModal(review)"
                class="px-4 py-2 rounded-lg bg-red-600 hover:bg-red-700 border border-red-400/30 transition flex items-center gap-2"
              >
                <svg class="w-4 h-4" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                  <path
                    stroke-linecap="round"
                    stroke-linejoin="round"
                    stroke-width="2"
                    d="M19 7l-.867 12.142A2 2 0 0116.138 21H7.862a2 2 0 01-1.995-1.858L5 7m5 4v6m4-6v6m1-10V4a1 1 0 00-1-1h-4a1 1 0 00-1 1v3M4 7h16"
                  />
                </svg>
                Xóa
              </button>
            </div>
          </div>
        </div>
      </div>
    </div>

    <!-- Delete Confirmation Modal -->
    <ConfirmModal
      :show="showDeleteModal"
      title="Xác nhận xóa đánh giá"
      :message="`Bạn có chắc chắn muốn xóa đánh giá của ${deletingReview?.customerName}?`"
      confirm-text="Xóa"
      confirm-class="bg-red-600 hover:bg-red-700"
      @confirm="confirmDelete"
      @cancel="closeDeleteModal"
    />
  </div>
</template>
