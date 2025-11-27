<script setup lang="ts">
import { useCartStore } from "@/stores/cart";
import { computed, onMounted, ref } from "vue";
import { useAuthStore } from "@/stores/auth";
import { useRouter } from "vue-router";
import CartItem from "@/components/CartItem.vue";
import LoadingOverlay from "@/components/LoadingOverlay.vue";

const cart = useCartStore();
const auth = useAuthStore();
const router = useRouter();
const loading = ref(false);

const itemCount = computed(() => cart.items.reduce((n, i) => n + i.qty, 0));
const total = computed(() => cart.total);

const formatCurrency = (v: number) =>
  new Intl.NumberFormat("vi-VN", { style: "currency", currency: "VND" }).format(v);

async function cleanupDuplicates() {
  loading.value = true;
  try {
    await cart.cleanupDuplicates();
  } finally {
    loading.value = false;
  }
}

async function ensureLoaded() {
  if (!auth.token) return;
  loading.value = true;
  try {
    await cart.load();
  } finally {
    loading.value = false;
  }
}

async function onIncrease(p: { id: string | number; quantity: number }) {
  await cart.setQty(String(p.id), p.quantity);
}

async function onDecrease(p: { id: string | number; quantity: number }) {
  await cart.setQty(String(p.id), p.quantity);
}

async function onRemove(id: string | number) {
  await cart.remove(String(id));
}

function checkout() {
  if (!cart.items.length) return;
  router.push({ name: "checkout" });
}

onMounted(() => {
  ensureLoaded();
});
</script>

<template>
  <LoadingOverlay :show="loading" message="Đang tải giỏ hàng..." />

  <div class="max-w-5xl mx-auto">
    <div class="flex items-center justify-between mb-6">
      <h1 class="text-3xl font-bold text-gray-900">Giỏ hàng</h1>
    </div>

    <div class="bg-white rounded-xl shadow p-6">
      <div class="grid grid-cols-1 md:grid-cols-3 gap-6">
        <!-- Items list -->
        <div class="md:col-span-2 space-y-4">
          <div v-if="!cart.items.length" class="text-gray-500">Chưa có món nào.</div>
          <div v-else class="space-y-4">
            <CartItem
              v-for="it in cart.items"
              :key="it.id"
              :id="it.id"
              :name="it.name"
              :price="it.price"
              :image="(it as any).imageUrl"
              :quantity="it.qty"
              :type="it.type"
              @increaseQuantity="onIncrease"
              @decreaseQuantity="onDecrease"
              @removeItem="onRemove"
            />
          </div>
        </div>

        <!-- Summary -->
        <div class="md:col-span-1">
          <div class="border rounded-lg p-4">
            <div class="text-center font-semibold border-b pb-3">Hóa đơn</div>
            <div class="flex justify-between py-3">
              <span>Số lượng :</span>
              <span>{{ itemCount }}</span>
            </div>
            <div class="flex justify-between py-2 text-red-600 font-bold">
              <span>Tổng tiền :</span>
              <span>{{ formatCurrency(total) }}</span>
            </div>
            <button
              class="w-full mt-4 bg-red-700 hover:bg-red-800 text-white py-2 rounded disabled:opacity-50 disabled:cursor-not-allowed transition-opacity"
              :disabled="loading || !cart.items.length"
              @click="checkout"
            >
              Thanh Toán
            </button>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>
