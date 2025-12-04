<script setup lang="ts">
import { defineProps, computed } from "vue";
import { useUiStore } from "@/stores/ui";
import { useCartStore } from "@/stores/cart";
import { useAuthStore } from "@/stores/auth";
import { useRouter } from "vue-router";
import { toast } from "vue3-toastify";

const props = defineProps<{
  id: string;
  categoryId?: string;
  categoryName?: string;
  name: string;
  description?: string;
  unit?: string;
  price: number;
  imageUrl?: string;
  status?: string;
  signature?: boolean;
}>();

const ui = useUiStore();
const cart = useCartStore();
const auth = useAuthStore();
const router = useRouter();

const shortDesc = computed(() => (props.description ? props.description.slice(0, 80) : ""));

const unitText = computed(() => {
  switch (props.unit) {
    case "PORTION":
      return "Phần";
    case "PLATE":
      return "Dĩa";
    case "BOWL":
      return "Tô";
    case "GLASS":
      return "Ly";
    default:
      return "Phần";
  }
});

const statusText = computed(() => {
  switch (props.status) {
    case "ACTIVE":
      return "Hoạt động";
    case "INACTIVE":
      return "Tạm dừng";
    default:
      return "Món ăn";
  }
});

const statusColor = computed(() => {
  switch (props.status) {
    case "ACTIVE":
      return "bg-green-100 text-green-800";
    case "INACTIVE":
      return "bg-gray-100 text-gray-600";
    default:
      return "bg-gray-100 text-gray-600";
  }
});

function addToCart() {
  if (!auth.token) {
    toast.info("Vui lòng đăng nhập để thêm món vào giỏ hàng");
    router.push({ name: "login", query: { redirect: router.currentRoute.value.fullPath } });
    return;
  }
  cart.add({ id: props.id, name: props.name, price: props.price, qty: 1 });
}

function openDetail() {
  ui.openDish(props.id);
}
</script>

<template>
  <div
    class="bg-white rounded-2xl shadow-sm hover:shadow-lg transition-all duration-300 overflow-hidden border border-gray-100 cursor-pointer group"
    @click="openDetail"
  >
    <!-- Image -->
    <div class="h-48 bg-gray-100 overflow-hidden relative">
      <!-- ✨ Best Seller Badge - Góc trái trên -->
      <div
        v-if="signature"
        class="absolute top-3 left-3 z-20 bg-gradient-to-r from-yellow-400 to-orange-500 text-white px-3 py-1 rounded-full text-xs font-bold shadow-lg flex items-center gap-1"
      >
        <svg class="w-4 h-4" fill="currentColor" viewBox="0 0 20 20">
          <path
            d="M9.049 2.927c.3-.921 1.603-.921 1.902 0l1.07 3.292a1 1 0 00.95.69h3.462c.969 0 1.371 1.24.588 1.81l-2.8 2.034a1 1 0 00-.364 1.118l1.07 3.292c.3.921-.755 1.688-1.54 1.118l-2.8-2.034a1 1 0 00-1.175 0l-2.8 2.034c-.784.57-1.838-.197-1.539-1.118l1.07-3.292a1 1 0 00-.364-1.118L2.98 8.72c-.783-.57-.38-1.81.588-1.81h3.461a1 1 0 00.951-.69l1.07-3.292z"
          />
        </svg>
        Best Seller
      </div>

      <img
        v-if="imageUrl"
        :src="imageUrl"
        :alt="`Món ${name} - ${categoryName || 'Ẩm thực Hoa Ban'}`"
        loading="lazy"
        class="w-full h-full object-cover group-hover:scale-105 transition-transform duration-300"
      />
      <div v-else class="w-full h-full flex items-center justify-center text-gray-400">
        <svg class="w-12 h-12" fill="none" stroke="currentColor" viewBox="0 0 24 24">
          <path
            stroke-linecap="round"
            stroke-linejoin="round"
            stroke-width="1.5"
            d="M19 11H5m14 0a2 2 0 012 2v6a2 2 0 01-2 2H5a2 2 0 01-2-2v-6a2 2 0 012-2m14 0V9a2 2 0 00-2-2M5 11V9a2 2 0 012-2m0 0V5a2 2 0 012-2h6a2 2 0 012 2v2M7 7h10"
          />
        </svg>
      </div>

      <!-- Overlay with view details hint -->
      <div
        class="absolute inset-0 bg-black/20 opacity-0 group-hover:opacity-100 transition-opacity duration-300 flex items-center justify-center"
      >
        <div class="bg-white/90 backdrop-blur-sm px-4 py-2 rounded-lg text-gray-900 font-medium">
          <svg class="w-5 h-5 inline mr-2" fill="none" stroke="currentColor" viewBox="0 0 24 24">
            <path
              stroke-linecap="round"
              stroke-linejoin="round"
              stroke-width="2"
              d="M15 12a3 3 0 11-6 0 3 3 0 016 0z"
            ></path>
            <path
              stroke-linecap="round"
              stroke-linejoin="round"
              stroke-width="2"
              d="M2.458 12C3.732 7.943 7.523 5 12 5c4.478 0 8.268 2.943 9.542 7-1.274 4.057-5.064 7-9.542 7-4.477 0-8.268-2.943-9.542-7z"
            ></path>
          </svg>
          Xem chi tiết
        </div>
      </div>

      <!-- Category badge - Góc phải trên -->
      <div
        v-if="categoryName"
        class="absolute top-3 right-3 z-10 bg-[#9f0909] text-white px-3 py-1 rounded-full text-xs font-medium shadow-lg"
      >
        {{ categoryName }}
      </div>
    </div>

    <!-- Content -->
    <div class="p-4">
      <!-- Price and Status -->
      <div class="flex justify-between items-center mb-2">
        <div class="flex items-center gap-2">
          <span class="text-xl font-bold text-[#9f0909]">{{ price.toLocaleString() }}đ</span>
          <span class="text-sm text-gray-500">/{{ unitText }}</span>
        </div>
        <span :class="['text-xs px-2 py-1 rounded-full font-medium', statusColor]">
          {{ statusText }}
        </span>
      </div>

      <!-- Dish Name -->
      <h3
        class="font-bold text-gray-900 mb-2 line-clamp-1 group-hover:text-[#9f0909] transition-colors"
      >
        {{ name }}
      </h3>

      <!-- Description -->
      <p class="text-gray-600 text-sm mb-4 line-clamp-2">
        {{ shortDesc || "Món ăn chế biến từ nguyên liệu tươi ngon, đậm đà hương vị truyền thống" }}
      </p>

      <!-- Action Button -->
      <button
        @click.stop="addToCart"
        :disabled="status === 'INACTIVE'"
        class="w-full bg-[#9f0909] hover:bg-[#800808] text-white py-2.5 px-4 rounded-xl font-medium transition-colors duration-200 disabled:opacity-50 disabled:cursor-not-allowed flex items-center justify-center gap-2"
      >
        <svg class="w-4 h-4" fill="none" stroke="currentColor" viewBox="0 0 24 24">
          <path
            stroke-linecap="round"
            stroke-linejoin="round"
            stroke-width="2"
            d="M3 3h2l.4 2M7 13h10l4-8H5.4m0 0L7 13m0 0l-1.5 6M7 13l1.5-6M17 21a1 1 0 100-2 1 1 0 000 2zM9 21a1 1 0 100-2 1 1 0 000 2z"
          ></path>
        </svg>
        <span>{{ status === "INACTIVE" ? "Hết hàng" : "Thêm vào giỏ" }}</span>
      </button>
    </div>
  </div>
</template>

<style scoped>
.line-clamp-1 {
  display: -webkit-box;
  -webkit-line-clamp: 1;
  line-clamp: 1;
  -webkit-box-orient: vertical;
  overflow: hidden;
}

.line-clamp-2 {
  display: -webkit-box;
  -webkit-line-clamp: 2;
  line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
}
</style>
