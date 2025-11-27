<script setup lang="ts">
defineProps<{
  title: string;
  description?: string;
  icon?: string;
  gradient?: string;
}>();
</script>

<template>
  <div class="admin-page-header">
    <!-- Animated Background Blobs -->
    <div class="absolute inset-0 overflow-hidden pointer-events-none">
      <div
        class="absolute -top-24 -left-24 h-72 w-72 rounded-full blur-3xl opacity-30 animate-pulse"
        :class="gradient || 'bg-emerald-500'"
        style="animation-duration: 4s"
      ></div>
      <div
        class="absolute -bottom-24 -right-24 h-96 w-96 rounded-full blur-3xl opacity-20 animate-pulse"
        :class="gradient || 'bg-blue-500'"
        style="animation-duration: 6s; animation-delay: 1s"
      ></div>
      <div
        class="absolute top-1/2 left-1/2 -translate-x-1/2 -translate-y-1/2 h-64 w-64 rounded-full blur-3xl opacity-10 animate-pulse"
        style="animation-duration: 5s; animation-delay: 2s"
        :class="gradient || 'bg-teal-500'"
      ></div>
    </div>

    <!-- Glass Effect Layer -->
    <div
      class="absolute inset-0 bg-gradient-to-br from-black/30 via-black/40 to-black/50 backdrop-blur-xl border border-white/10 rounded-2xl"
    ></div>

    <!-- Content -->
    <div class="relative z-10 flex items-start gap-5">
      <!-- Icon Badge -->
      <div
        v-if="icon"
        class="flex-shrink-0 w-20 h-20 rounded-2xl bg-gradient-to-br from-emerald-500/20 via-teal-500/20 to-blue-500/20 border-2 border-white/20 flex items-center justify-center backdrop-blur-sm shadow-2xl hover:scale-105 transition-transform duration-300"
      >
        <span class="text-5xl drop-shadow-[0_0_10px_rgba(52,211,153,0.5)]">{{ icon }}</span>
      </div>

      <!-- Text Content -->
      <div class="flex-1 min-w-0">
        <h1 class="text-3xl md:text-4xl lg:text-5xl font-extrabold tracking-tight mb-3">
          <span
            class="bg-gradient-to-r from-white via-emerald-100 to-teal-100 bg-clip-text text-transparent drop-shadow-sm"
          >
            {{ title }}
          </span>
        </h1>
        <p v-if="description" class="text-base md:text-lg text-white/70 leading-relaxed max-w-3xl">
          {{ description }}
        </p>

        <!-- Decorative line -->
        <div
          class="mt-4 h-1 w-24 bg-gradient-to-r from-emerald-400 via-teal-400 to-transparent rounded-full shadow-[0_0_10px_rgba(52,211,153,0.5)]"
        ></div>
      </div>
    </div>

    <!-- Corner Accents -->
    <div
      class="absolute top-0 right-0 w-32 h-32 bg-gradient-to-br from-emerald-400/10 to-transparent rounded-bl-full pointer-events-none"
    ></div>
    <div
      class="absolute bottom-0 left-0 w-32 h-32 bg-gradient-to-tr from-blue-400/10 to-transparent rounded-tr-full pointer-events-none"
    ></div>
  </div>
</template>

<style scoped>
.admin-page-header {
  position: relative;
  overflow: hidden;
  border-radius: 1.5rem;
  padding: 2.5rem;
  margin-bottom: 2rem;
  box-shadow: 0 25px 80px -10px rgba(0, 0, 0, 0.5), 0 0 40px rgba(16, 185, 129, 0.1);
  transition: all 0.3s ease;
}

.admin-page-header:hover {
  box-shadow: 0 30px 100px -10px rgba(0, 0, 0, 0.6), 0 0 60px rgba(16, 185, 129, 0.15);
  transform: translateY(-2px);
}

@media (max-width: 768px) {
  .admin-page-header {
    padding: 1.5rem;
  }
}
</style>
