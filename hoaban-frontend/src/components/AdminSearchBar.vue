<script setup lang="ts">
import { ref, watch } from "vue";

const props = defineProps<{
  modelValue: string;
  placeholder?: string;
  icon?: string;
}>();

const emit = defineEmits<{
  (e: "update:modelValue", value: string): void;
  (e: "search"): void;
}>();

const localValue = ref(props.modelValue);

watch(
  () => props.modelValue,
  (newVal) => {
    localValue.value = newVal;
  }
);

watch(localValue, (newVal) => {
  emit("update:modelValue", newVal);
});

function handleSearch() {
  emit("search");
}
</script>

<template>
  <div class="search-bar">
    <div class="search-icon text-xl">
      {{ icon || "🔍" }}
    </div>
    <input
      v-model="localValue"
      type="text"
      :placeholder="placeholder || 'Tìm kiếm...'"
      @keyup.enter="handleSearch"
      class="input-admin"
    />
    <button
      v-if="localValue"
      @click="
        localValue = '';
        emit('update:modelValue', '');
      "
      class="absolute right-4 top-1/2 -translate-y-1/2 text-white/40 hover:text-white/80 transition-colors"
      title="Xóa"
    >
      ✕
    </button>
  </div>
</template>
