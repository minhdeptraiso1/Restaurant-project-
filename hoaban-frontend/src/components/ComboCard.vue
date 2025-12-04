<script setup lang="ts">
import { useCartStore } from "@/stores/cart";
import { useUiStore } from "@/stores/ui";
import { useAuthStore } from "@/stores/auth";
import { useRouter } from "vue-router";
import { toast } from "vue3-toastify";

const props = defineProps<{
  id: string;
  name: string;
  description?: string;
  price: number;
  imageUrl?: string;
  itemsCount?: number;
  status?: string;
  items?: Array<{
    dishId: string;
    dishName: string;
    quantity: number;
  }>;
  suggestedSum?: number;
}>();

const cart = useCartStore();
const ui = useUiStore();
const auth = useAuthStore();
const router = useRouter();

function addToCart(comboId: string) {
  if (!auth.token) {
    toast.info("Vui lòng đăng nhập để thêm combo vào giỏ hàng");
    router.push({ name: "login", query: { redirect: router.currentRoute.value.fullPath } });
    return;
  }
  cart.addItem("COMBO", comboId);
}

function openComboDetail() {
  ui.openCombo(props.id);
}

function getStatusText(status?: string) {
  switch (status) {
    case "ACTIVE":
      return "Còn hàng";
    case "INACTIVE":
      return "Tạm hết";
    default:
      return "Combo";
  }
}

function getStatusColor(status?: string) {
  switch (status) {
    case "ACTIVE":
      return "bg-green-100 text-green-800";
    case "INACTIVE":
      return "bg-red-100 text-red-800";
    default:
      return "bg-amber-100 text-amber-800";
  }
}
</script>

<template>
  <div
    class="bg-white rounded-2xl shadow-sm hover:shadow-lg transition-all duration-300 overflow-hidden border border-gray-100 cursor-pointer group flex flex-col h-[580px]"
    @click="openComboDetail"
  >
    <!-- Image -->
    <div class="h-48 bg-gray-100 overflow-hidden relative flex-shrink-0">
      <img
        v-if="imageUrl"
        :src="imageUrl"
        :alt="`Combo ${name} - Hoa Ban Restaurant`"
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

      <!-- Items count badge -->
      <div
        v-if="items && items.length > 0"
        class="absolute top-3 left-3 bg-[#9f0909] text-white px-2 py-1 rounded-full text-xs font-bold"
      >
        {{ items.length }} món
      </div>
    </div>

    <!-- Content -->
    <div class="p-4 flex-1 flex flex-col">
      <!-- Price and Status -->
      <div class="flex justify-between items-center mb-2">
        <div class="flex items-center gap-2">
          <span class="text-xl font-bold text-[#9f0909]">{{ price.toLocaleString() }}đ</span>
          <span
            v-if="suggestedSum && suggestedSum > price"
            class="text-sm text-gray-500 line-through"
          >
            {{ suggestedSum.toLocaleString() }}đ
          </span>
        </div>
        <span :class="['text-xs px-2 py-1 rounded-full font-medium', getStatusColor(status)]">
          {{ getStatusText(status) }}
        </span>
      </div>

      <!-- Savings Badge -->
      <div v-if="suggestedSum && suggestedSum > price" class="mb-2">
        <span
          class="inline-block bg-green-100 text-green-800 text-xs px-2 py-1 rounded-full font-medium"
        >
          💰 Tiết kiệm {{ (suggestedSum - price).toLocaleString() }}đ
        </span>
      </div>

      <!-- Combo Name -->
      <h3
        class="font-bold text-gray-900 mb-2 line-clamp-1 group-hover:text-[#9f0909] transition-colors h-6"
      >
        {{ name }}
      </h3>

      <!-- Description -->
      <p class="text-gray-600 text-sm mb-4 line-clamp-2 h-10">
        {{ description || "Combo nhiều món ăn hấp dẫn với giá ưu đãi" }}
      </p>

      <!-- Items Preview -->
      <div v-if="items && items.length > 0" class="mb-4 h-16">
        <p class="text-xs text-gray-500 mb-1">Bao gồm:</p>
        <div class="flex flex-wrap gap-1">
          <span
            v-for="(item, index) in items.slice(0, 2)"
            :key="item.dishId"
            class="text-xs bg-gray-100 text-gray-700 px-2 py-1 rounded-full"
          >
            {{ item.dishName }}
          </span>
          <span
            v-if="items.length > 2"
            class="text-xs bg-gray-100 text-gray-700 px-2 py-1 rounded-full"
          >
            +{{ items.length - 2 }} món khác
          </span>
        </div>
      </div>

      <!-- Action Button -->
      <button
        @click.stop="addToCart(id)"
        :disabled="status === 'INACTIVE'"
        class="w-full bg-[#9f0909] hover:bg-[#800808] text-white py-2.5 px-4 rounded-xl font-medium transition-colors duration-200 disabled:opacity-50 disabled:cursor-not-allowed flex items-center justify-center gap-2 mt-auto"
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
