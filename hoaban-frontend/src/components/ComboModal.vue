<script setup lang="ts">
import { ref, onMounted, computed, watch } from "vue";
import { useUiStore } from "@/stores/ui";
import { useCartStore } from "@/stores/cart";
import { getCombo, type Combo } from "@/api/combos.service";

const ui = useUiStore();
const cart = useCartStore();

const combo = ref<Combo | null>(null);
const quantity = ref(1);
const isLoading = ref(true);
const loadError = ref<string | null>(null);

async function fetchCombo() {
  const id = ui.selectedComboId;
  if (!id) return;
  isLoading.value = true;
  loadError.value = null;
  try {
    const { data } = await getCombo(id);
    combo.value = data;
  } catch (e: any) {
    console.error("Failed to load combo detail", e);
    loadError.value = e?.friendlyMessage || e?.message || "Không thể tải thông tin combo";
    combo.value = null;
  } finally {
    isLoading.value = false;
  }
}

const totalPrice = computed(() => quantity.value * (combo.value?.price || 0));
const savings = computed(() => {
  if (!combo.value) return 0;
  const suggested = combo.value.suggestedSum || 0;
  return Math.max(0, (suggested - combo.value.price) * quantity.value);
});

const statusText = computed(() => {
  switch (combo.value?.status) {
    case "ACTIVE":
      return "Còn hàng";
    case "INACTIVE":
      return "Tạm hết";
    default:
      return "Không xác định";
  }
});

const statusColor = computed(() => {
  switch (combo.value?.status) {
    case "ACTIVE":
      return "text-green-600 bg-green-100";
    case "INACTIVE":
      return "text-red-600 bg-red-100";
    default:
      return "text-gray-600 bg-gray-100";
  }
});

function closeModal() {
  ui.closeCombo();
}

function addToCart() {
  if (combo.value) {
    for (let i = 0; i < quantity.value; i++) {
      cart.addItem("COMBO", combo.value.id);
    }
    closeModal();
  }
}

function increaseQuantity() {
  quantity.value++;
}

function decreaseQuantity() {
  if (quantity.value > 1) {
    quantity.value--;
  }
}

onMounted(() => {
  if (ui.showComboModal && ui.selectedComboId) {
    fetchCombo();
  }
});

watch(
  () => ui.showComboModal,
  (open) => {
    if (open) {
      quantity.value = 1;
      fetchCombo();
    } else {
      combo.value = null;
      loadError.value = null;
      isLoading.value = false;
    }
  }
);
</script>

<template>
  <div
    class="fixed inset-0 z-50 flex items-center justify-center p-4 bg-black/50 backdrop-blur-sm"
    @click.self="closeModal"
  >
    <div
      class="bg-white rounded-2xl shadow-2xl max-w-4xl w-full max-h-[90vh] overflow-hidden"
      @click.stop
    >
      <!-- Loading State -->
      <div v-if="isLoading" class="p-8 text-center">
        <div
          class="animate-spin w-8 h-8 border-4 border-[#9f0909] border-t-transparent rounded-full mx-auto mb-4"
        ></div>
        <p class="text-gray-600">Đang tải thông tin combo...</p>
      </div>

      <!-- Error State -->
      <div v-else-if="loadError" class="p-8 text-center">
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
        <p class="text-gray-600 mb-4">{{ loadError }}</p>
        <button
          @click="fetchCombo"
          class="px-6 py-2 bg-[#9f0909] text-white rounded-lg hover:bg-[#800808] transition-colors"
        >
          Thử lại
        </button>
      </div>

      <!-- Combo Details -->
      <div v-else-if="combo" class="flex flex-col lg:flex-row max-h-[90vh]">
        <!-- Image Section -->
        <div class="lg:w-1/2 h-64 lg:h-auto">
          <img :src="combo.imageUrl" :alt="combo.name" class="w-full h-full object-cover" />
        </div>

        <!-- Content Section -->
        <div class="lg:w-1/2 p-6 overflow-y-auto">
          <!-- Header -->
          <div class="flex justify-between items-start mb-4">
            <div class="flex-1">
              <div class="flex items-center gap-3 mb-2">
                <span
                  class="bg-amber-100 text-amber-800 px-3 py-1 rounded-full text-sm font-medium"
                >
                  🍽️ Combo
                </span>
                <span :class="['px-3 py-1 rounded-full text-sm font-medium', statusColor]">
                  {{ statusText }}
                </span>
              </div>
              <h2 class="text-2xl font-bold text-gray-900 mb-2">{{ combo.name }}</h2>
              <p class="text-gray-600 leading-relaxed">{{ combo.description }}</p>
            </div>

            <button
              @click="closeModal"
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
            <div class="flex items-center justify-between mb-2">
              <span class="text-lg font-medium text-gray-700">Giá combo:</span>
              <span class="text-2xl font-bold text-[#9f0909]"
                >{{ combo.price.toLocaleString() }}đ</span
              >
            </div>
            <div class="flex items-center justify-between text-sm">
              <span class="text-gray-600">Giá nếu mua lẻ:</span>
              <span class="line-through text-gray-500"
                >{{ (combo.suggestedSum ?? combo.price).toLocaleString() }}đ</span
              >
            </div>
            <div class="flex items-center justify-between text-sm font-medium">
              <span class="text-green-600">Tiết kiệm:</span>
              <span class="text-green-600"
                >{{ Math.max(0, (combo.suggestedSum ?? 0) - combo.price).toLocaleString() }}đ</span
              >
            </div>
          </div>

          <!-- Combo Items -->
          <div class="mb-6">
            <h3 class="text-lg font-bold text-gray-900 mb-3">📋 Món ăn trong combo:</h3>
            <div class="space-y-3">
              <div
                v-for="item in combo.items"
                :key="item.dishId"
                class="flex items-center justify-between p-3 bg-gray-50 rounded-lg"
              >
                <div class="flex items-center gap-3">
                  <div
                    class="w-8 h-8 bg-[#9f0909] text-white rounded-full flex items-center justify-center text-sm font-bold"
                  >
                    {{ item.quantity }}
                  </div>
                  <span class="font-medium text-gray-900">{{ item.dishName }}</span>
                </div>
                <span class="text-sm text-gray-600">x{{ item.quantity }}</span>
              </div>
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
              <div class="text-right">
                <div class="text-2xl font-bold text-[#9f0909]">
                  {{ totalPrice.toLocaleString() }}đ
                </div>
                <div v-if="savings > 0" class="text-sm text-green-600 font-medium">
                  Tiết kiệm: {{ savings.toLocaleString() }}đ
                </div>
              </div>
            </div>

            <!-- Add to Cart Button -->
            <button
              @click="addToCart"
              :disabled="combo.status !== 'ACTIVE'"
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
              <span>{{ combo.status === "ACTIVE" ? "Thêm vào giỏ hàng" : "Hết hàng" }}</span>
            </button>
          </div>
        </div>
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
