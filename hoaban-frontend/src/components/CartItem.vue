<template>
  <div
    class="bg-white border border-gray-200 rounded-xl p-4 mb-4 shadow-sm hover:shadow-md transition-shadow"
  >
    <div class="flex items-center gap-4">
      <!-- Image -->
      <div class="w-16 h-16 rounded-lg overflow-hidden flex-shrink-0 bg-gray-100">
        <img
          v-if="image"
          :src="image"
          :alt="name"
          class="w-full h-full object-cover"
          loading="lazy"
        />
        <div v-else class="w-full h-full grid place-items-center text-gray-400">
          <i class="fas fa-utensils"></i>
        </div>
      </div>

      <!-- Item Info -->
      <div class="flex-1 min-w-0">
        <div class="flex items-center gap-2 mb-1">
          <h3
            class="font-semibold text-lg text-black truncate"
            style="font-family: 'Playfair Display', serif"
          >
            {{ name }}
          </h3>
          <span
            v-if="type === 'COMBO'"
            class="bg-amber-100 text-amber-800 px-2 py-1 rounded-full text-xs font-medium"
          >
            🍽️ Combo
          </span>
          <span
            v-else-if="type === 'DISH'"
            class="bg-blue-100 text-blue-800 px-2 py-1 rounded-full text-xs font-medium"
          >
            🍕 Món đơn
          </span>
        </div>
        <p class="text-red-600 font-bold text-lg" style="font-family: 'Roboto', sans-serif">
          Giá: {{ formatPrice(price) }}
        </p>
      </div>

      <!-- Quantity Controls -->
      <div class="flex items-center gap-3">
        <span
          class="text-gray-600 font-medium hidden sm:inline"
          style="font-family: 'Roboto', sans-serif"
          >Số lượng</span
        >
        <div class="flex items-center gap-2">
          <button
            @click="$emit('decreaseQuantity', { id, quantity: quantity - 1 })"
            :disabled="quantity <= 1"
            class="w-8 h-8 rounded-full border border-gray-300 flex items-center justify-center hover:bg-gray-100 transition-colors disabled:opacity-50 disabled:cursor-not-allowed"
            aria-label="Decrease"
          >
            -
          </button>
          <span class="w-8 text-center font-medium">{{ quantity }}</span>
          <button
            @click="$emit('increaseQuantity', { id, quantity: quantity + 1 })"
            class="w-8 h-8 rounded-full border border-gray-300 flex items-center justify-center hover:bg-gray-100 transition-colors"
            aria-label="Increase"
          >
            +
          </button>
        </div>
      </div>

      <!-- Remove Button -->
      <button
        @click="$emit('removeItem', id)"
        class="bg-red-500 text-white px-4 py-2 rounded-lg hover:bg-red-600 transition-colors font-medium"
        style="font-family: 'Roboto', sans-serif"
      >
        Xóa
      </button>
    </div>
  </div>
</template>

<script setup lang="ts">
// Props
const props = defineProps<{
  id: string | number;
  name: string;
  price: number;
  image?: string;
  quantity: number;
  type?: "DISH" | "COMBO"; // Add type prop
}>();

// Emits
// increaseQuantity: { id, quantity }
// decreaseQuantity: { id, quantity }
// removeItem: id
// Define for TS awareness
// eslint-disable-next-line @typescript-eslint/no-unused-vars
const emit = defineEmits<{
  (e: "increaseQuantity", payload: { id: string | number; quantity: number }): void;
  (e: "decreaseQuantity", payload: { id: string | number; quantity: number }): void;
  (e: "removeItem", id: string | number): void;
}>();

// Methods
const formatPrice = (val: number) => {
  return new Intl.NumberFormat("vi-VN").format(val) + "đ";
};
</script>

<style scoped>
button {
  transition: all 0.2s ease;
}
</style>
