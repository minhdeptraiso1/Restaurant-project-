<script setup lang="ts">
import { ref, onMounted, computed } from "vue";
import { getLatestReviews, type Review } from "@/api/reviews.service";

const reviews = ref<Review[]>([]);
const loading = ref(true);
const currentSlide = ref(0);

// Fetch reviews from API
const loadReviews = async () => {
  loading.value = true;
  try {
    const { data } = await getLatestReviews();
    reviews.value = data;
  } catch (error) {
    console.error("Error loading reviews:", error);
    // Fallback to default reviews if API fails
    reviews.value = [];
  } finally {
    loading.value = false;
  }
};

// Format date to relative time
const formatDate = (dateString: string) => {
  const date = new Date(dateString);
  const now = new Date();
  const diff = now.getTime() - date.getTime();
  const days = Math.floor(diff / (1000 * 60 * 60 * 24));

  if (days === 0) return "Hôm nay";
  if (days === 1) return "1 ngày trước";
  if (days < 7) return `${days} ngày trước`;
  if (days < 30) return `${Math.floor(days / 7)} tuần trước`;
  if (days < 365) return `${Math.floor(days / 30)} tháng trước`;
  return `${Math.floor(days / 365)} năm trước`;
};

// Get star display
const getStars = (rating: number) => {
  const fullStars = Math.floor(rating);
  const hasHalfStar = rating % 1 >= 0.5;
  let stars = "★".repeat(fullStars);
  if (hasHalfStar && fullStars < 5) stars += "☆";
  while (stars.length < 5) stars += "☆";
  return stars;
};

// Group reviews into slides (4 per slide)
const slides = computed(() => {
  const result = [];
  for (let i = 0; i < reviews.value.length; i += 4) {
    result.push(reviews.value.slice(i, i + 4));
  }
  return result;
});

// Navigation
const goToSlide = (index: number) => {
  currentSlide.value = Math.max(0, Math.min(index, slides.value.length - 1));
};

const nextSlide = () => {
  currentSlide.value = (currentSlide.value + 1) % slides.value.length;
};

const prevSlide = () => {
  currentSlide.value = (currentSlide.value - 1 + slides.value.length) % slides.value.length;
};

onMounted(() => {
  loadReviews();
});
</script>

<template>
  <section class="mx-auto max-w-7xl px-4 py-20 scroll-reveal relative overflow-hidden">
    <!-- Background decoration -->
    <div class="absolute top-0 left-0 w-96 h-96 bg-amber-100/30 rounded-full blur-3xl -z-10"></div>
    <div
      class="absolute bottom-0 right-0 w-96 h-96 bg-emerald-100/30 rounded-full blur-3xl -z-10"
    ></div>

    <!-- Header -->
    <div class="text-center mb-16 animate-fade-in-up">
      <div class="inline-block mb-4">
        <span class="text-5xl md:text-6xl animate-bounce-slow">⭐</span>
      </div>
      <h2 class="text-4xl md:text-5xl font-bold text-gray-900 mb-4">
        Khách hàng nói gì về chúng tôi?
      </h2>
      <p class="text-lg md:text-xl text-gray-600 max-w-2xl mx-auto">
        Hơn <span class="font-bold text-[#9f0909]">{{ reviews.length }}</span> đánh giá từ khách
        hàng hài lòng
      </p>
      <div
        class="mt-6 h-1 w-24 mx-auto bg-gradient-to-r from-transparent via-[#9f0909] to-transparent rounded-full"
      ></div>
    </div>

    <!-- Loading State -->
    <div v-if="loading" class="mt-6 grid grid-cols-1 md:grid-cols-2 gap-4 md:gap-6">
      <div
        v-for="i in 4"
        :key="i"
        class="rounded-xl bg-white shadow-sm ring-1 ring-gray-100 p-5 animate-pulse"
      >
        <div class="h-6 bg-gray-200 rounded w-3/4 mb-3"></div>
        <div class="h-4 bg-gray-200 rounded w-1/2 mb-3"></div>
        <div class="h-16 bg-gray-200 rounded mb-3"></div>
        <div class="h-3 bg-gray-200 rounded w-2/3"></div>
      </div>
    </div>

    <!-- Empty State -->
    <div
      v-else-if="reviews.length === 0"
      class="mt-6 text-center py-16 bg-white rounded-xl shadow-sm"
    >
      <div class="text-6xl mb-4">⭐</div>
      <h3 class="text-xl font-semibold text-gray-700 mb-2">Chưa có đánh giá</h3>
      <p class="text-gray-500">Hãy là người đầu tiên đánh giá nhà hàng của chúng tôi!</p>
    </div>

    <!-- Carousel with Reviews -->
    <div v-else class="relative mt-6 group overflow-hidden">
      <!-- Track -->
      <div
        class="flex transition-transform duration-500 ease-out"
        :style="{ transform: `translateX(-${currentSlide * 100}%)` }"
      >
        <!-- Slide -->
        <div
          v-for="(slide, slideIndex) in slides"
          :key="slideIndex"
          class="min-w-full grid grid-cols-1 md:grid-cols-2 gap-4 md:gap-6"
        >
          <!-- Card -->
          <article
            v-for="(review, idx) in slide"
            :key="review.id"
            class="group relative rounded-2xl bg-gradient-to-br from-white via-white to-gray-50/50 shadow-lg ring-1 ring-gray-200/50 p-6 hover:shadow-2xl transition-all hover:-translate-y-2 duration-500 overflow-hidden animate-fade-in-up"
            :style="`animation-delay: ${idx * 0.1}s`"
          >
            <!-- Decorative gradient overlay -->
            <div
              class="absolute top-0 right-0 w-32 h-32 bg-gradient-to-br from-amber-400/10 to-orange-400/10 rounded-full blur-2xl opacity-0 group-hover:opacity-100 transition-opacity duration-500"
            ></div>
            <div
              class="absolute bottom-0 left-0 w-24 h-24 bg-gradient-to-tr from-emerald-400/10 to-teal-400/10 rounded-full blur-2xl opacity-0 group-hover:opacity-100 transition-opacity duration-500"
            ></div>

            <!-- Quote icon decoration -->
            <div class="absolute top-4 right-4 text-6xl text-amber-400/10 font-serif leading-none">
              "
            </div>

            <div class="relative z-10">
              <!-- Header -->
              <div class="flex items-start justify-between mb-4">
                <div class="flex items-center gap-3">
                  <div class="relative">
                    <div
                      class="w-14 h-14 rounded-2xl shadow-lg ring-4 ring-white group-hover:scale-110 transition-transform duration-300 overflow-hidden"
                    >
                      <img
                        v-if="review.avatarUrl"
                        :src="review.avatarUrl"
                        :alt="review.customerName"
                        class="w-full h-full object-cover"
                      />
                      <div
                        v-else
                        class="w-full h-full bg-gradient-to-br from-amber-400 via-orange-400 to-red-400 flex items-center justify-center text-white font-bold text-xl"
                      >
                        {{ review.customerName.charAt(0).toUpperCase() }}
                      </div>
                    </div>
                    <!-- Online indicator -->
                    <div
                      class="absolute -bottom-1 -right-1 w-5 h-5 bg-green-400 rounded-full border-3 border-white shadow-sm"
                    ></div>
                  </div>
                  <div>
                    <h4
                      class="font-bold text-gray-900 text-lg group-hover:text-[#9f0909] transition-colors"
                    >
                      {{ review.customerName }}
                    </h4>
                    <div class="flex items-center gap-2 text-xs text-gray-500">
                      <svg class="w-3.5 h-3.5" fill="currentColor" viewBox="0 0 20 20">
                        <path
                          fill-rule="evenodd"
                          d="M10 18a8 8 0 100-16 8 8 0 000 16zm1-12a1 1 0 10-2 0v4a1 1 0 00.293.707l2.828 2.829a1 1 0 101.415-1.415L11 9.586V6z"
                          clip-rule="evenodd"
                        ></path>
                      </svg>
                      <span>{{ formatDate(review.createdAt) }}</span>
                    </div>
                  </div>
                </div>
              </div>

              <!-- Rating with animation -->
              <div
                class="mb-4 flex items-center gap-3 p-3 bg-gradient-to-r from-amber-50 to-orange-50 rounded-xl border border-amber-200/50"
              >
                <div class="flex items-center gap-1">
                  <template v-for="star in 5" :key="star">
                    <svg
                      class="w-5 h-5 transition-all duration-300 hover:scale-125"
                      :class="
                        star <= review.rating
                          ? 'text-amber-400 fill-current drop-shadow-sm'
                          : 'text-gray-300'
                      "
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
                  </template>
                </div>
                <span
                  class="text-sm font-bold text-amber-700 bg-white px-2 py-1 rounded-lg shadow-sm"
                  >{{ review.rating }}/5</span
                >
              </div>

              <!-- Comment with better typography -->
              <div class="relative mb-4">
                <p
                  v-if="review.comment"
                  class="text-gray-700 leading-relaxed line-clamp-3 min-h-[4.5rem] text-base italic"
                >
                  "{{ review.comment }}"
                </p>
                <p v-else class="text-gray-400 italic text-sm min-h-[4.5rem]">
                  Khách hàng chưa để lại nhận xét chi tiết
                </p>
              </div>
            </div>
          </article>
        </div>
      </div>

      <!-- Navigation Buttons - Modern Style -->
      <button
        v-if="slides.length > 1"
        @click="prevSlide"
        class="absolute left-0 top-1/2 -translate-y-1/2 -translate-x-2 md:-translate-x-4 bg-gradient-to-br from-white to-gray-50 text-[#9F0909] w-14 h-14 rounded-2xl grid place-items-center shadow-2xl hover:shadow-xl hover:scale-110 opacity-80 hover:opacity-100 transition-all duration-300 border-2 border-gray-100 group/btn backdrop-blur-sm"
        aria-label="Trước"
      >
        <svg
          class="w-7 h-7 group-hover/btn:-translate-x-1 transition-transform"
          fill="none"
          stroke="currentColor"
          viewBox="0 0 24 24"
        >
          <path
            stroke-linecap="round"
            stroke-linejoin="round"
            stroke-width="2.5"
            d="M15 19l-7-7 7-7"
          />
        </svg>
      </button>
      <button
        v-if="slides.length > 1"
        @click="nextSlide"
        class="absolute right-0 top-1/2 -translate-y-1/2 translate-x-2 md:translate-x-4 bg-gradient-to-br from-white to-gray-50 text-[#9F0909] w-14 h-14 rounded-2xl grid place-items-center shadow-2xl hover:shadow-xl hover:scale-110 opacity-80 hover:opacity-100 transition-all duration-300 border-2 border-gray-100 group/btn backdrop-blur-sm"
        aria-label="Sau"
      >
        <svg
          class="w-7 h-7 group-hover/btn:translate-x-1 transition-transform"
          fill="none"
          stroke="currentColor"
          viewBox="0 0 24 24"
        >
          <path
            stroke-linecap="round"
            stroke-linejoin="round"
            stroke-width="2.5"
            d="M9 5l7 7-7 7"
          />
        </svg>
      </button>

      <!-- Dots Indicator - Modern Style -->
      <div v-if="slides.length > 1" class="mt-10 flex items-center justify-center gap-2">
        <button
          v-for="(slide, index) in slides"
          :key="index"
          @click="goToSlide(index)"
          :class="[
            'rounded-full transition-all duration-500 hover:scale-110',
            index === currentSlide
              ? 'w-12 h-3 bg-gradient-to-r from-[#9F0909] to-[#7a0707] shadow-lg'
              : 'w-3 h-3 bg-gray-300 hover:bg-gray-400',
          ]"
          :aria-label="`Trang ${index + 1}`"
        ></button>
      </div>
    </div>

    <!-- CTA - Compact -->
    <div
      v-if="reviews.length > 0"
      class="mt-12 text-center animate-fade-in-up"
      style="animation-delay: 0.4s"
    >
      <div class="flex flex-col sm:flex-row items-center justify-center gap-4">
        <router-link
          to="/menu"
          class="group relative inline-flex items-center gap-3 bg-gradient-to-r from-[#9f0909] via-[#b01010] to-[#7a0707] hover:from-[#800808] hover:to-[#9f0909] text-white px-8 py-3.5 rounded-xl font-bold transition-all transform hover:scale-105 active:scale-95 shadow-xl hover:shadow-2xl overflow-hidden"
        >
          <!-- Shimmer effect -->
          <div
            class="absolute inset-0 bg-gradient-to-r from-transparent via-white/20 to-transparent -translate-x-full group-hover:translate-x-full transition-transform duration-1000"
          ></div>

          <svg
            class="w-5 h-5 group-hover:rotate-12 transition-transform relative z-10"
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
          <span class="relative z-10">Đặt món và đánh giá ngay</span>
        </router-link>

        <a
          href="tel:0868030043"
          class="group relative inline-flex items-center gap-2 bg-white text-gray-700 px-8 py-3.5 rounded-xl font-semibold border-2 border-gray-200 hover:border-[#9f0909] hover:text-[#9f0909] transition-all shadow-md hover:shadow-xl"
        >
          <svg class="w-5 h-5" fill="none" stroke="currentColor" viewBox="0 0 24 24">
            <path
              stroke-linecap="round"
              stroke-linejoin="round"
              stroke-width="2"
              d="M3 5a2 2 0 012-2h3.28a1 1 0 01.948.684l1.498 4.493a1 1 0 01-.502 1.21l-2.257 1.13a11.042 11.042 0 005.516 5.516l1.13-2.257a1 1 0 011.21-.502l4.493 1.498a1 1 0 01.684.949V19a2 2 0 01-2 2h-1C9.716 21 3 14.284 3 6V5z"
            />
          </svg>
          <span class="relative z-10">Gọi tư vấn</span>
          <!-- Phone number tooltip on hover -->
          <span
            class="absolute -top-14 left-1/2 -translate-x-1/2 bg-gray-900 text-white px-4 py-2.5 rounded-lg text-sm font-semibold whitespace-nowrap opacity-0 group-hover:opacity-100 group-hover:-translate-y-1 transition-all duration-300 shadow-2xl pointer-events-none z-50"
          >
            📞 0868030043
            <span
              class="absolute -bottom-1 left-1/2 -translate-x-1/2 w-2 h-2 bg-gray-900 rotate-45"
            ></span>
          </span>
        </a>
      </div>
    </div>
  </section>
</template>

<style scoped>
/* Smooth animations */
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

.animate-fade-in-up {
  animation: fadeInUp 0.6s ease-out;
  animation-fill-mode: both;
}

/* Line clamp utility */
.line-clamp-3 {
  display: -webkit-box;
  -webkit-line-clamp: 3;
  line-clamp: 3;
  -webkit-box-orient: vertical;
  overflow: hidden;
}

/* Pulse animation for loading */
@keyframes pulse {
  0%,
  100% {
    opacity: 1;
  }
  50% {
    opacity: 0.5;
  }
}

.animate-pulse {
  animation: pulse 2s cubic-bezier(0.4, 0, 0.6, 1) infinite;
}
</style>
