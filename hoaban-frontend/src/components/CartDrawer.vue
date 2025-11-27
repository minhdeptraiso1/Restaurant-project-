<script setup lang="ts">
import { computed } from "vue";
import { useUiStore } from "@/stores/ui";
import { useCartStore } from "@/stores/cart";
import { useRouter } from "vue-router";

const ui = useUiStore();
const cart = useCartStore();
const router = useRouter();

const total = computed(() => cart.total);

function close() {
  ui.closeCart();
}

function checkout() {
  ui.closeCart();
  router.push({ name: "checkout" });
}
</script>

<template>
  <div v-if="ui.showCart" class="fixed inset-0 z-40 flex">
    <div class="absolute inset-0 bg-black/30" @click="close"></div>
    <div class="ml-auto w-full max-w-md bg-white h-full shadow-lg p-4 z-50">
      <div class="flex items-center justify-between">
        <div class="font-bold">Giỏ hàng</div>
        <button class="text-sm text-gray-500" @click="close">Đóng</button>
      </div>
      <div class="mt-4 space-y-3">
        <div v-if="!cart.items.length" class="text-gray-500">Giỏ trống</div>
        <div v-else>
          <div v-for="it in cart.items" :key="it.id" class="flex justify-between py-2 border-b">
            <div>{{ it.name }} x {{ it.qty }}</div>
            <div>{{ (it.price * it.qty).toLocaleString() }} đ</div>
          </div>
          <div class="flex justify-between font-semibold pt-3">
            <div>Tổng</div>
            <div>{{ total.toLocaleString() }} đ</div>
          </div>
          <div class="mt-4">
            <button class="w-full bg-black text-white px-4 py-2 rounded" @click="checkout">
              Thanh toán
            </button>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>
