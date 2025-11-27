<script setup lang="ts">
defineProps<{
  modelValue: string;
  placeholder?: string;
  icon?: string;
  type?: string;
}>();

defineEmits<{
  "update:modelValue": [value: string];
}>();
</script>

<template>
  <div class="relative">
    <span v-if="icon" class="absolute left-3 top-1/2 -translate-y-1/2 text-white/70">
      {{ icon }}
    </span>
    <input
      :type="type || 'text'"
      :value="modelValue"
      @input="$emit('update:modelValue', ($event.target as HTMLInputElement).value)"
      :placeholder="placeholder"
      :class="[
        'w-full bg-white/5 border border-white/10 rounded-lg px-3 py-2.5 text-white',
        'placeholder:text-white/40 outline-none transition',
        'focus:border-emerald-400 focus:bg-white/10',
        icon && 'pl-10',
      ]"
    />
  </div>
</template>
