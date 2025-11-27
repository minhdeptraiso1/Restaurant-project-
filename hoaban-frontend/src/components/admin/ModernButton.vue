<script setup lang="ts">
defineProps<{
  variant?: "primary" | "secondary" | "danger" | "success" | "warning";
  size?: "sm" | "md" | "lg";
  loading?: boolean;
  disabled?: boolean;
  icon?: string;
}>();
</script>

<template>
  <button
    :disabled="disabled || loading"
    :class="[
      'inline-flex items-center justify-center gap-2 font-medium transition-all rounded-lg',
      'disabled:opacity-50 disabled:cursor-not-allowed',
      {
        'px-3 py-1.5 text-sm': size === 'sm',
        'px-4 py-2 text-base': size === 'md' || !size,
        'px-6 py-3 text-lg': size === 'lg',
      },
      {
        'bg-emerald-600 hover:bg-emerald-700 text-white shadow-lg shadow-emerald-500/20':
          variant === 'primary' || !variant,
        'bg-white/10 hover:bg-white/15 border border-white/20 text-white': variant === 'secondary',
        'bg-rose-600 hover:bg-rose-700 text-white shadow-lg shadow-rose-500/20':
          variant === 'danger',
        'bg-blue-600 hover:bg-blue-700 text-white shadow-lg shadow-blue-500/20':
          variant === 'success',
        'bg-amber-600 hover:bg-amber-700 text-white shadow-lg shadow-amber-500/20':
          variant === 'warning',
      },
    ]"
  >
    <span v-if="icon && !loading">{{ icon }}</span>
    <span
      v-if="loading"
      class="w-4 h-4 border-2 border-white/30 border-t-white rounded-full animate-spin"
    ></span>
    <slot />
  </button>
</template>
