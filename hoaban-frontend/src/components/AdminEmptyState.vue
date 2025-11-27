<script setup lang="ts">
defineProps<{
  icon?: string;
  title?: string;
  description?: string;
  actionText?: string;
}>();

const emit = defineEmits<{
  (e: "action"): void;
}>();
</script>

<template>
  <div class="empty-state py-20">
    <div class="text-7xl mb-4 opacity-40 animate-pulse">
      {{ icon || "📭" }}
    </div>
    <h3 class="text-2xl font-bold text-white/80 mb-2">
      {{ title || "Không có dữ liệu" }}
    </h3>
    <p class="text-white/50 mb-6">
      {{ description || "Chưa có dữ liệu để hiển thị" }}
    </p>
    <button v-if="actionText" @click="emit('action')" class="btn-admin-primary">
      {{ actionText }}
    </button>
  </div>
</template>
