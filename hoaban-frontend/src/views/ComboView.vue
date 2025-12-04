<script setup lang="ts">
import { ref, onMounted } from "vue";
import { listCombos, type Combo } from "@/api/combos.service";
import ComboCard from "@/components/ComboCard.vue";
import ComboModal from "@/components/ComboModal.vue";
import { useUiStore } from "@/stores/ui";

const ui = useUiStore();

const combos = ref<Combo[]>([]);
const loading = ref(false);

// Banner images
const bannerImages = ref([
  "https://images.unsplash.com/photo-1504674900247-0877df9cc836?q=80&w=1600&auto=format&fit=crop",
  "https://images.unsplash.com/photo-1555939594-58d7cb561ad1?q=80&w=1600&auto=format&fit=crop",
  "https://images.unsplash.com/photo-1567620905732-2d1ec7ab7445?q=80&w=1600&auto=format&fit=crop",
]);
const currentBannerIndex = ref(0);

// Auto-rotate banner
let bannerInterval: ReturnType<typeof setInterval> | null = null;

const startBannerRotation = () => {
  bannerInterval = setInterval(() => {
    currentBannerIndex.value = (currentBannerIndex.value + 1) % bannerImages.value.length;
  }, 5000);
};

const stopBannerRotation = () => {
  if (bannerInterval) {
    clearInterval(bannerInterval);
    bannerInterval = null;
  }
};

const goToBanner = (index: number) => {
  currentBannerIndex.value = index;
  stopBannerRotation();
  startBannerRotation();
};

const nextBanner = () => {
  currentBannerIndex.value = (currentBannerIndex.value + 1) % bannerImages.value.length;
  stopBannerRotation();
  startBannerRotation();
};

const prevBanner = () => {
  currentBannerIndex.value =
    currentBannerIndex.value === 0 ? bannerImages.value.length - 1 : currentBannerIndex.value - 1;
  stopBannerRotation();
  startBannerRotation();
};

const fetchCombos = async () => {
  try {
    loading.value = true;
    const response = await listCombos();
    combos.value = response.data;
  } catch (error) {
    console.error("Error fetching combos:", error);
  } finally {
    loading.value = false;
  }
};

const openComboDetail = (combo: Combo) => {
  ui.openCombo(combo.id);
};

onMounted(() => {
  fetchCombos();
  startBannerRotation();
});

// Cleanup on unmount
import { onUnmounted } from "vue";
onUnmounted(() => {
  stopBannerRotation();
});
</script>

<template>
  <div class="min-h-screen bg-gray-50">
    <!-- Banner Section with Auto-Rotate -->
    <section
      class="relative w-full h-[500px] overflow-hidden bg-gradient-to-r from-[#9f0909] to-[#7a1010]"
    >
      <!-- Banner Images -->
      <div class="relative w-full h-full">
        <transition-group name="banner-fade">
          <div
            v-for="(image, index) in bannerImages"
            :key="index"
            v-show="index === currentBannerIndex"
            class="absolute inset-0 w-full h-full"
          >
            <img
              :src="image"
              :alt="`Combo Banner ${index + 1} - Hoa Ban Restaurant`"
              loading="lazy"
              class="w-full h-full object-cover"
            />
            <div class="absolute inset-0 bg-black/40"></div>
          </div>
        </transition-group>
      </div>

      <!-- Banner Content -->
      <div class="absolute inset-0 flex items-center justify-center z-10">
        <div class="text-center text-white px-4">
          <h1
            class="text-5xl md:text-7xl font-bold mb-4 font-['Inknut_Antiqua',serif] drop-shadow-lg"
          >
            Combo Đặc Biệt
          </h1>
          <p class="text-xl md:text-2xl mb-8 drop-shadow-md">
            Tiết kiệm hơn khi đặt combo - Trải nghiệm trọn vẹn hương vị Tây Bắc
          </p>
          <div class="flex items-center justify-center gap-4">
            <div class="h-1 w-20 bg-white/60"></div>
            <span class="text-lg">🍽️</span>
            <div class="h-1 w-20 bg-white/60"></div>
          </div>
        </div>
      </div>

      <!-- Navigation Arrows -->
      <button
        @click="prevBanner"
        class="absolute left-4 top-1/2 -translate-y-1/2 z-20 w-12 h-12 bg-white/20 hover:bg-white/40 backdrop-blur-sm rounded-full flex items-center justify-center transition-all"
      >
        <svg class="w-6 h-6 text-white" fill="none" stroke="currentColor" viewBox="0 0 24 24">
          <path
            stroke-linecap="round"
            stroke-linejoin="round"
            stroke-width="2"
            d="M15 19l-7-7 7-7"
          />
        </svg>
      </button>
      <button
        @click="nextBanner"
        class="absolute right-4 top-1/2 -translate-y-1/2 z-20 w-12 h-12 bg-white/20 hover:bg-white/40 backdrop-blur-sm rounded-full flex items-center justify-center transition-all"
      >
        <svg class="w-6 h-6 text-white" fill="none" stroke="currentColor" viewBox="0 0 24 24">
          <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M9 5l7 7-7 7" />
        </svg>
      </button>

      <!-- Banner Indicators -->
      <div class="absolute bottom-8 left-1/2 -translate-x-1/2 z-20 flex gap-3">
        <button
          v-for="(_, index) in bannerImages"
          :key="index"
          @click="goToBanner(index)"
          :class="[
            'h-3 rounded-full transition-all duration-300',
            index === currentBannerIndex ? 'w-12 bg-white' : 'w-3 bg-white/50 hover:bg-white/80',
          ]"
        ></button>
      </div>
    </section>

    <!-- Combos Section -->
    <section class="max-w-7xl mx-auto px-4 py-16">
      <!-- Section Header -->
      <div class="text-center mb-12">
        <h2 class="text-4xl font-bold text-gray-900 mb-4 font-['Inknut_Antiqua',serif]">
          Danh Sách Combo
        </h2>
        <p class="text-lg text-gray-600 max-w-2xl mx-auto">
          Lựa chọn combo phù hợp với bạn bè và gia đình
        </p>
      </div>

      <!-- Loading State -->
      <div v-if="loading" class="text-center py-20">
        <div
          class="inline-block animate-spin rounded-full h-12 w-12 border-b-2 border-[#9f0909]"
        ></div>
        <p class="mt-4 text-gray-600">Đang tải combo...</p>
      </div>

      <!-- Combos Grid -->
      <div
        v-else-if="combos.length > 0"
        class="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-8"
      >
        <ComboCard
          v-for="combo in combos"
          :key="combo.id"
          :id="combo.id"
          :name="combo.name"
          :description="combo.description"
          :price="combo.price"
          :imageUrl="combo.imageUrl"
          :status="combo.status"
          :items="combo.items"
          :suggestedSum="combo.suggestedSum"
          @click="openComboDetail(combo)"
        />
      </div>

      <!-- Empty State -->
      <div v-else class="text-center py-20">
        <svg
          class="mx-auto h-24 w-24 text-gray-400 mb-4"
          fill="none"
          stroke="currentColor"
          viewBox="0 0 24 24"
        >
          <path
            stroke-linecap="round"
            stroke-linejoin="round"
            stroke-width="2"
            d="M20 13V6a2 2 0 00-2-2H6a2 2 0 00-2 2v7m16 0v5a2 2 0 01-2 2H6a2 2 0 01-2-2v-5m16 0h-2.586a1 1 0 00-.707.293l-2.414 2.414a1 1 0 01-.707.293h-3.172a1 1 0 01-.707-.293l-2.414-2.414A1 1 0 006.586 13H4"
          />
        </svg>
        <h3 class="text-xl font-semibold text-gray-900 mb-2">Chưa có combo nào</h3>
        <p class="text-gray-600">Vui lòng quay lại sau</p>
      </div>
    </section>

    <!-- Combo Detail Modal -->
    <ComboModal v-if="ui.showComboModal" />
  </div>
</template>

<style scoped>
/* Banner fade animation */
.banner-fade-enter-active,
.banner-fade-leave-active {
  transition: opacity 1s ease;
}

.banner-fade-enter-from,
.banner-fade-leave-to {
  opacity: 0;
}

.banner-fade-enter-to,
.banner-fade-leave-from {
  opacity: 1;
}

/* Smooth animations */
@keyframes slideIn {
  from {
    opacity: 0;
    transform: translateY(20px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}

.animate-slide-in {
  animation: slideIn 0.6s ease-out;
}
</style>
