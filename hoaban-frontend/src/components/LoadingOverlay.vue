<template>
  <Transition name="fade">
    <div
      v-if="show"
      class="fixed inset-0 z-[9999] flex items-center justify-center bg-black/50 backdrop-blur-sm"
    >
      <div class="flex flex-col items-center justify-center">
        <!-- Spinner -->
        <div class="relative flex items-center justify-center w-24 h-24">
          <!-- Outer ring -->
          <div
            class="absolute w-24 h-24 border-4 border-white/20 rounded-full animate-ping"
            style="animation-duration: 2s"
          ></div>

          <!-- Main spinner -->
          <div
            class="absolute w-20 h-20 border-4 border-transparent border-t-white border-r-white rounded-full animate-spin"
          ></div>

          <!-- Inner circle -->
          <div class="absolute w-14 h-14 bg-white/10 rounded-full animate-pulse"></div>
        </div>

        <!-- Loading text -->
        <div class="mt-6 text-center max-w-xs mx-auto px-4">
          <p class="text-white text-lg font-medium animate-pulse whitespace-nowrap">
            {{ message || "Đang tải..." }}
          </p>

          <div class="flex gap-1 justify-center mt-2">
            <span class="w-2 h-2 bg-white rounded-full animate-bounce"></span>
            <span
              class="w-2 h-2 bg-white rounded-full animate-bounce"
              style="animation-delay: 0.1s"
            ></span>
            <span
              class="w-2 h-2 bg-white rounded-full animate-bounce"
              style="animation-delay: 0.2s"
            ></span>
          </div>
        </div>
      </div>
    </div>
  </Transition>
</template>

<script setup lang="ts">
defineProps<{
  show: boolean;
  message?: string;
}>();
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

@keyframes spin {
  to {
    transform: rotate(360deg);
  }
}

@keyframes ping {
  75%,
  100% {
    transform: scale(1.5);
    opacity: 0;
  }
}

@keyframes pulse {
  0%,
  100% {
    opacity: 1;
  }
  50% {
    opacity: 0.5;
  }
}

@keyframes bounce {
  0%,
  100% {
    transform: translateY(0);
  }
  50% {
    transform: translateY(-0.4rem);
  }
}
</style>
