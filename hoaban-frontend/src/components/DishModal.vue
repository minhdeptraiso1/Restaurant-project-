<script setup lang="ts">
import { watch, ref, computed } from "vue";
import { useUiStore } from "@/stores/ui";
import { useCartStore } from "@/stores/cart";
import { getDish } from "@/api/dishes.service";

const ui = useUiStore();
const cart = useCartStore();
const dish = ref<any>(null);
const loading = ref(false);
const quantity = ref(1);

watch(
  () => ui.selectedDishId,
  async (id) => {
    if (!id) return (dish.value = null);
    loading.value = true;
    quantity.value = 1;
    try {
      const { data } = await getDish(id);
      dish.value = data;
    } catch (error) {
      console.error("Error loading dish:", error);
      dish.value = null;
    } finally {
      loading.value = false;
    }
  }
);

const unitText = computed(() => {
  if (!dish.value?.unit) return "Suất";
  switch (dish.value.unit) {
    case "PORTION":
      return "Phần";
    case "PIECE":
      return "Miếng";
    case "BOWL":
      return "Tô";
    case "PLATE":
      return "Đĩa";
    case "CUP":
      return "Ly";
    default:
      return "Suất";
  }
});

const statusText = computed(() => {
  if (!dish.value?.status) return "Không xác định";
  switch (dish.value.status) {
    case "ACTIVE":
      return "Còn hàng";
    case "INACTIVE":
      return "Tạm hết";
    default:
      return "Không xác định";
  }
});

const statusColor = computed(() => {
  if (!dish.value?.status) return "text-gray-600 bg-gray-100";
  switch (dish.value.status) {
    case "ACTIVE":
      return "text-green-600 bg-green-100";
    case "INACTIVE":
      return "text-red-600 bg-red-100";
    default:
      return "text-gray-600 bg-gray-100";
  }
});

const totalPrice = computed(() => {
  return dish.value ? quantity.value * Number(dish.value.price) : 0;
});

function close() {
  ui.closeDish();
}

function addToCart() {
  if (!dish.value || dish.value.status !== "ACTIVE") return;

  for (let i = 0; i < quantity.value; i++) {
    cart.add({
      id: dish.value.id,
      name: dish.value.name,
      price: Number(dish.value.price),
      qty: 1,
    });
  }
  close();
}

function increaseQuantity() {
  quantity.value++;
}

function decreaseQuantity() {
  if (quantity.value > 1) {
    quantity.value--;
  }
}
</script>

<template>
  <div
    v-if="ui.showDishModal"
    class="fixed inset-0 z-50 flex items-center justify-center p-4 bg-black/50 backdrop-blur-sm"
  >
    <div class="absolute inset-0" @click="close"></div>
    <div
      class="bg-white rounded-2xl shadow-2xl z-50 max-w-2xl w-full max-h-[90vh] overflow-hidden"
      @click.stop
    >
      <!-- Loading State -->
      <div v-if="loading" class="p-8 text-center">
        <div
          class="animate-spin w-8 h-8 border-4 border-[#9f0909] border-t-transparent rounded-full mx-auto mb-4"
        ></div>
        <p class="text-gray-600">Đang tải thông tin món ăn...</p>
      </div>

      <!-- Dish Details -->
      <div v-else-if="dish" class="flex flex-col lg:flex-row max-h-[90vh]">
        <!-- Image Section -->
        <div class="lg:w-1/2 h-64 lg:h-auto">
          <img
            v-if="dish.imageUrl"
            :src="dish.imageUrl"
            :alt="dish.name"
            class="w-full h-full object-cover"
          />
          <div v-else class="w-full h-full bg-gray-100 flex items-center justify-center">
            <svg
              class="w-16 h-16 text-gray-400"
              fill="none"
              stroke="currentColor"
              viewBox="0 0 24 24"
            >
              <path
                stroke-linecap="round"
                stroke-linejoin="round"
                stroke-width="1.5"
                d="M19 11H5m14 0a2 2 0 012 2v6a2 2 0 01-2 2H5a2 2 0 01-2-2v-6a2 2 0 012-2m14 0V9a2 2 0 00-2-2M5 11V9a2 2 0 012-2m0 0V5a2 2 0 012-2h6a2 2 0 012 2v2M7 7h10"
              ></path>
            </svg>
          </div>
        </div>

        <!-- Content Section -->
        <div class="lg:w-1/2 p-6 overflow-y-auto">
          <!-- Header -->
          <div class="flex justify-between items-start mb-4">
            <div class="flex-1">
              <div class="flex items-center gap-3 mb-2">
                <span
                  v-if="dish.categoryName"
                  class="bg-[#9f0909] text-white px-3 py-1 rounded-full text-sm font-medium"
                >
                  {{ dish.categoryName }}
                </span>
                <span :class="['px-3 py-1 rounded-full text-sm font-medium', statusColor]">
                  {{ statusText }}
                </span>
              </div>
              <h2 class="text-2xl font-bold text-gray-900 mb-2">{{ dish.name }}</h2>
              <p v-if="dish.description" class="text-gray-600 leading-relaxed">
                {{ dish.description }}
              </p>
              <p v-else class="text-gray-500 italic">
                Món ăn chế biến từ nguyên liệu tươi ngon, đậm đà hương vị truyền thống
              </p>
            </div>

            <button
              @click="close"
              class="ml-4 w-8 h-8 rounded-full bg-gray-100 hover:bg-gray-200 flex items-center justify-center transition-colors"
            >
              <svg class="w-5 h-5" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                <path
                  stroke-linecap="round"
                  stroke-linejoin="round"
                  stroke-width="2"
                  d="M6 18L18 6M6 6l12 12"
                ></path>
              </svg>
            </button>
          </div>

          <!-- Price Info -->
          <div class="bg-gradient-to-r from-[#9f0909]/10 to-amber-50 rounded-xl p-4 mb-6">
            <div class="flex items-center justify-between">
              <span class="text-lg font-medium text-gray-700">Giá:</span>
              <div class="text-right">
                <span class="text-2xl font-bold text-[#9f0909]"
                  >{{ Number(dish.price).toLocaleString() }}đ</span
                >
                <span class="text-gray-600 text-sm ml-1">/ {{ unitText }}</span>
              </div>
            </div>
          </div>

          <!-- Dish Details -->
          <div class="space-y-4 mb-6">
            <div
              v-if="dish.unit"
              class="flex items-center justify-between p-3 bg-gray-50 rounded-lg"
            >
              <span class="font-medium text-gray-700">📏 Đơn vị tính:</span>
              <span class="text-gray-900">{{ unitText }}</span>
            </div>

            <div
              v-if="dish.categoryName"
              class="flex items-center justify-between p-3 bg-gray-50 rounded-lg"
            >
              <span class="font-medium text-gray-700">🍽️ Loại món:</span>
              <span class="text-gray-900">{{ dish.categoryName }}</span>
            </div>

            <div class="flex items-center justify-between p-3 bg-gray-50 rounded-lg">
              <span class="font-medium text-gray-700">📋 Trạng thái:</span>
              <span :class="['px-2 py-1 rounded-full text-sm font-medium', statusColor]">
                {{ statusText }}
              </span>
            </div>
          </div>

          <!-- Quantity & Add to Cart -->
          <div class="border-t pt-6">
            <!-- Quantity Selector -->
            <div class="flex items-center gap-4 mb-6">
              <span class="font-medium text-gray-700">Số lượng:</span>
              <div class="flex items-center border border-gray-300 rounded-lg">
                <button
                  @click="decreaseQuantity"
                  :disabled="quantity <= 1"
                  class="w-10 h-10 flex items-center justify-center hover:bg-gray-100 disabled:opacity-50 disabled:cursor-not-allowed transition-colors"
                >
                  -
                </button>
                <span class="w-12 text-center font-bold">{{ quantity }}</span>
                <button
                  @click="increaseQuantity"
                  class="w-10 h-10 flex items-center justify-center hover:bg-gray-100 transition-colors"
                >
                  +
                </button>
              </div>
            </div>

            <!-- Total Price -->
            <div class="flex items-center justify-between mb-6 p-4 bg-[#9f0909]/5 rounded-xl">
              <span class="text-lg font-bold text-gray-900">Tổng cộng:</span>
              <span class="text-2xl font-bold text-[#9f0909]"
                >{{ totalPrice.toLocaleString() }}đ</span
              >
            </div>

            <!-- Add to Cart Button -->
            <button
              @click="addToCart"
              :disabled="dish.status !== 'ACTIVE'"
              class="w-full bg-[#9f0909] hover:bg-[#800808] text-white py-4 px-6 rounded-xl font-bold text-lg transition-colors disabled:opacity-50 disabled:cursor-not-allowed flex items-center justify-center gap-2"
            >
              <svg class="w-5 h-5" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                <path
                  stroke-linecap="round"
                  stroke-linejoin="round"
                  stroke-width="2"
                  d="M3 3h2l.4 2M7 13h10l4-8H5.4m0 0L7 13m0 0l-1.5 6M7 13l1.5-6M17 21a1 1 0 100-2 1 1 0 000 2zM9 21a1 1 0 100-2 1 1 0 000 2z"
                ></path>
              </svg>
              <span>{{ dish.status === "ACTIVE" ? "Thêm vào giỏ hàng" : "Hết hàng" }}</span>
            </button>
          </div>
        </div>
      </div>

      <!-- Error State -->
      <div v-else class="p-8 text-center">
        <div class="text-red-500 mb-4">
          <svg class="w-12 h-12 mx-auto" fill="none" stroke="currentColor" viewBox="0 0 24 24">
            <path
              stroke-linecap="round"
              stroke-linejoin="round"
              stroke-width="2"
              d="M12 9v2m0 4h.01m-6.938 4h13.856c1.54 0 2.502-1.667 1.732-2.5L13.732 4c-.77-.833-1.99-.833-2.76 0L4.054 16.5c-.77.833.192 2.5 1.732 2.5z"
            ></path>
          </svg>
        </div>
        <p class="text-gray-600">Không thể tải thông tin món ăn</p>
        <button
          @click="close"
          class="mt-4 px-6 py-2 bg-gray-500 text-white rounded-lg hover:bg-gray-600 transition-colors"
        >
          Đóng
        </button>
      </div>
    </div>
  </div>
</template>

<style scoped>
@keyframes spin {
  to {
    transform: rotate(360deg);
  }
}

.animate-spin {
  animation: spin 1s linear infinite;
}
</style>
