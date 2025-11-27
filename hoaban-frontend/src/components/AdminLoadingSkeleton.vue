<script setup lang="ts">
import { computed } from "vue";

const props = defineProps<{
  type?: "card" | "table" | "list";
  count?: number;
}>();

const items = computed(() => Array.from({ length: props.count || 3 }));
</script>

<template>
  <!-- Card Skeleton -->
  <div v-if="type === 'card'" class="grid gap-6 md:grid-cols-2 lg:grid-cols-3">
    <div v-for="(_, i) in items" :key="i" class="card-admin p-6 space-y-4 animate-pulse">
      <div class="h-6 bg-white/10 rounded-lg w-3/4"></div>
      <div class="h-4 bg-white/5 rounded w-full"></div>
      <div class="h-4 bg-white/5 rounded w-5/6"></div>
      <div class="flex gap-2 pt-2">
        <div class="h-10 bg-white/10 rounded-lg flex-1"></div>
        <div class="h-10 bg-white/10 rounded-lg w-20"></div>
      </div>
    </div>
  </div>

  <!-- Table Skeleton -->
  <div v-else-if="type === 'table'" class="card-admin overflow-hidden">
    <div class="animate-pulse">
      <div class="p-4 border-b border-white/10">
        <div class="h-4 bg-white/10 rounded w-1/4"></div>
      </div>
      <div v-for="(_, i) in items" :key="i" class="p-4 border-b border-white/5">
        <div class="flex items-center gap-4">
          <div class="h-12 w-12 bg-white/10 rounded-full"></div>
          <div class="flex-1 space-y-2">
            <div class="h-4 bg-white/10 rounded w-1/3"></div>
            <div class="h-3 bg-white/5 rounded w-1/2"></div>
          </div>
          <div class="h-8 w-24 bg-white/10 rounded"></div>
        </div>
      </div>
    </div>
  </div>

  <!-- List Skeleton -->
  <div v-else class="space-y-4">
    <div v-for="(_, i) in items" :key="i" class="card-admin p-6 animate-pulse">
      <div class="flex items-center gap-4">
        <div class="h-16 w-16 bg-white/10 rounded-xl flex-shrink-0"></div>
        <div class="flex-1 space-y-3">
          <div class="h-5 bg-white/10 rounded w-1/4"></div>
          <div class="h-3 bg-white/5 rounded w-3/4"></div>
          <div class="h-3 bg-white/5 rounded w-1/2"></div>
        </div>
        <div class="space-y-2">
          <div class="h-10 w-24 bg-white/10 rounded-lg"></div>
          <div class="h-10 w-24 bg-white/10 rounded-lg"></div>
        </div>
      </div>
    </div>
  </div>
</template>
