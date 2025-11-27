<script setup lang="ts">
defineProps<{
  title?: string;
  subtitle?: string;
  gradient?: boolean;
}>();
</script>

<template>
  <div
    :class="[
      'rounded-2xl shadow-xl border border-white/10 overflow-hidden',
      gradient
        ? 'bg-gradient-to-br from-[#111] via-[#0f1f1a] to-[#0b1512] relative'
        : 'bg-white/5 backdrop-blur-md',
    ]"
  >
    <!-- Gradient decoration -->
    <template v-if="gradient">
      <div
        class="absolute -top-16 -left-10 h-48 w-48 rounded-full bg-emerald-500/20 blur-3xl"
      ></div>
      <div
        class="absolute -bottom-16 -right-10 h-56 w-56 rounded-full bg-amber-500/20 blur-3xl"
      ></div>
    </template>

    <!-- Header -->
    <div v-if="title || $slots.header" :class="['p-6', gradient && 'relative z-10']">
      <slot name="header">
        <h3 class="text-xl font-bold text-white">{{ title }}</h3>
        <p v-if="subtitle" class="mt-1 text-sm text-white/70">{{ subtitle }}</p>
      </slot>
    </div>

    <!-- Content -->
    <div :class="['p-6', (title || $slots.header) && 'pt-0']">
      <slot />
    </div>
  </div>
</template>
