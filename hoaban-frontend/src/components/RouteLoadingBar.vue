<template>
  <Transition name="fade">
    <div v-if="isLoading" class="fixed top-0 left-0 right-0 z-[10000]">
      <!-- Progress bar -->
      <div
        class="h-1 bg-gradient-to-r from-red-500 via-orange-500 to-red-500 animate-shimmer"
      ></div>

      <!-- Loading overlay -->
      <div class="fixed inset-0 bg-black/30 backdrop-blur-sm flex items-center justify-center">
        <div class="bg-white rounded-2xl shadow-2xl p-8 max-w-sm w-full mx-4">
          <!-- Spinner -->
          <div class="flex justify-center mb-4">
            <div class="relative w-16 h-16">
              <div class="absolute inset-0 border-4 border-red-200 rounded-full"></div>
              <div
                class="absolute inset-0 border-4 border-transparent border-t-red-600 border-r-red-600 rounded-full animate-spin"
              ></div>
            </div>
          </div>

          <!-- Text -->
          <p class="text-center text-gray-700 font-medium">Đang tải trang...</p>
          <div class="flex gap-1 justify-center mt-3">
            <span class="w-2 h-2 bg-red-600 rounded-full animate-bounce"></span>
            <span
              class="w-2 h-2 bg-red-600 rounded-full animate-bounce"
              style="animation-delay: 0.1s"
            ></span>
            <span
              class="w-2 h-2 bg-red-600 rounded-full animate-bounce"
              style="animation-delay: 0.2s"
            ></span>
          </div>
        </div>
      </div>
    </div>
  </Transition>
</template>

<script setup lang="ts">
import { ref, watch } from "vue";
import { useRouter } from "vue-router";

const router = useRouter();
const isLoading = ref(false);
let loadingTimer: number | null = null;

// Watch for route changes
router.beforeEach((to, from) => {
  // Skip loading for same route or initial load
  if (from.name === undefined || to.name === from.name) {
    return true;
  }

  isLoading.value = true;

  // Clear any existing timer
  if (loadingTimer) {
    window.clearTimeout(loadingTimer);
    loadingTimer = null;
  }

  return true;
});

router.afterEach(() => {
  // Đợi navigation hoàn tất, sau đó đợi thêm 1 giây để mượt
  loadingTimer = window.setTimeout(() => {
    isLoading.value = false;
    loadingTimer = null;
  }, 1000);
});
</script>

<style scoped>
.fade-enter-active,
.fade-leave-active {
  transition: opacity 0.3s ease;
}

.fade-enter-from,
.fade-leave-to {
  opacity: 0;
}

@keyframes shimmer {
  0% {
    background-position: -1000px 0;
  }
  100% {
    background-position: 1000px 0;
  }
}

.animate-shimmer {
  background-size: 1000px 100%;
  animation: shimmer 2s infinite linear;
}

@keyframes spin {
  to {
    transform: rotate(360deg);
  }
}

@keyframes bounce {
  0%,
  100% {
    transform: translateY(0);
  }
  50% {
    transform: translateY(-0.5rem);
  }
}
</style>
