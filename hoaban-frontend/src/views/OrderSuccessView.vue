<script setup lang="ts">
import { onMounted, ref } from "vue";
import { useRoute, useRouter } from "vue-router";
import { detailOrder } from "@/api/orders.service";

const route = useRoute();
const router = useRouter();
const order = ref<any>(null);
const loading = ref(true);
const error = ref<string>("");

const resolveImage = (imagePath: string) => {
  try {
    return new URL(imagePath, import.meta.url).href;
  } catch (e) {
    return new URL("/src/assets/img/logo.png", import.meta.url).href;
  }
};

const formatCurrency = (amount: number) => {
  return new Intl.NumberFormat("vi-VN", {
    style: "currency",
    currency: "VND",
  }).format(amount || 0);
};

const goToOrderHistory = () => {
  router.push("/history");
};

const goToHome = () => {
  router.push("/");
};

onMounted(async () => {
  try {
    loading.value = true;
    const { data } = await detailOrder(route.params.id as string);
    order.value = data;
    console.log("Order details loaded:", data);
  } catch (err: any) {
    console.error("Error loading order:", err);
    error.value = err?.response?.data?.message || "Không thể tải thông tin đơn hàng";
  } finally {
    loading.value = false;
  }
});
</script>

<template>
  <div class="min-h-screen bg-gray-100 flex items-center justify-center p-4">
    <div class="max-w-md w-full bg-white rounded-2xl shadow-lg p-8 text-center">
      <!-- Logo -->
      <div class="mb-6">
        <img
          :src="resolveImage('/src/assets/img/logo.png')"
          alt="Hoa Ban Restaurant"
          class="w-20 h-20 mx-auto rounded-full border-4 border-[#9f0909]"
        />
        <h3 class="text-lg font-semibold text-gray-800 mt-3">Hoa Ban Restaurant</h3>
      </div>

      <!-- Success Icon & Message -->
      <div v-if="!loading && !error && order" class="mb-6">
        <!-- PAID Status -->
        <template v-if="order.status === 'PAID'">
          <div class="text-green-500 text-4xl mb-3">✅</div>
          <h1 class="text-2xl font-bold text-green-600 mb-2">Thanh toán thành công!</h1>
          <p class="text-gray-600 text-sm">Cảm ơn bạn đã đặt bàn món ăn tại Hoa Ban Restaurant</p>
        </template>

        <!-- UNPAID Status -->
        <template v-else-if="order.status === 'UNPAID'">
          <div class="text-amber-500 text-4xl mb-3">⏳</div>
          <h1 class="text-2xl font-bold text-amber-600 mb-2">
            Đơn hàng sẽ nhanh chống giao đến cho bạn
          </h1>
          <p class="text-gray-600 text-sm">Đơn hàng của bạn đang chờ xác nhận thanh toán</p>
        </template>

        <!-- OPEN Status -->
        <template v-else-if="order.status === 'OPEN'">
          <div class="text-blue-500 text-4xl mb-3">📝</div>
          <h1 class="text-2xl font-bold text-blue-600 mb-2">Đơn hàng đang mở!</h1>
          <p class="text-gray-600 text-sm">Đơn hàng của bạn đang được xử lý</p>
        </template>

        <!-- Other Status -->
        <template v-else>
          <div class="text-gray-500 text-4xl mb-3">📋</div>
          <h1 class="text-2xl font-bold text-gray-600 mb-2">Đơn hàng đã được xử lý!</h1>
          <p class="text-gray-600 text-sm">Cảm ơn bạn đã sử dụng dịch vụ của Hoa Ban Restaurant</p>
        </template>
      </div>

      <!-- Loading State -->
      <div v-if="loading" class="mb-8">
        <div
          class="animate-spin rounded-full h-8 w-8 border-b-2 border-[#9f0909] mx-auto mb-3"
        ></div>
        <p class="text-gray-600 text-sm">Đang tải thông tin đơn hàng...</p>
      </div>

      <!-- Error State -->
      <div v-else-if="error" class="mb-8">
        <div class="text-red-500 text-2xl mb-3">❌</div>
        <p class="text-red-600 font-medium mb-2">Có lỗi xảy ra</p>
        <p class="text-sm text-gray-600">{{ error }}</p>
      </div>

      <!-- Order Info -->
      <div v-else-if="order" class="mb-8">
        <div class="bg-gray-50 rounded-lg p-4 mb-4">
          <p class="text-gray-700 font-medium mb-2">
            Mã đơn:
            <span class="text-[#9f0909] font-bold"
              >#{{ order.code || order.id.slice(0, 8).toUpperCase() }}</span
            >
          </p>
          <p class="text-sm text-gray-600 mb-2">
            💰 Tổng tiền:
            <span class="text-green-600 font-bold">{{ formatCurrency(order.total) }}</span>
          </p>
          <p class="text-sm mb-2">
            <span
              class="inline-block px-2 py-1 rounded text-xs font-medium"
              :class="{
                'bg-green-100 text-green-700': order.status === 'PAID',
                'bg-amber-100 text-amber-700': order.status === 'UNPAID',
                'bg-blue-100 text-blue-700': order.status === 'OPEN',
                'bg-gray-100 text-gray-700': !['PAID', 'UNPAID', 'OPEN'].includes(order.status),
              }"
            >
              {{
                order.status === "PAID"
                  ? "✅ Đã thanh toán"
                  : order.status === "UNPAID"
                  ? "⏳ Chưa thanh toán"
                  : order.status === "OPEN"
                  ? "📝 Đang mở"
                  : order.status
              }}
            </span>
          </p>
          <p v-if="order.appliedVoucherCode" class="text-sm text-orange-600 mb-2">
            🎫 Đã áp dụng voucher: <span class="font-medium">{{ order.appliedVoucherCode }}</span>
          </p>
          <p class="text-sm text-gray-500">
            📅 {{ new Date(order.createdAt || Date.now()).toLocaleDateString("vi-VN") }}
            {{
              new Date(order.createdAt || Date.now()).toLocaleTimeString("vi-VN", {
                hour: "2-digit",
                minute: "2-digit",
              })
            }}
          </p>
        </div>

        <div v-if="order.note" class="bg-blue-50 border-l-4 border-blue-400 p-3 mb-4">
          <p class="text-sm text-blue-800 font-medium mb-1">Chú ý:</p>
          <p class="text-sm text-blue-700">{{ order.note }}</p>
        </div>
      </div>

      <!-- Action Buttons -->
      <div v-if="!loading" class="space-y-3">
        <button
          @click="goToHome"
          class="w-full border-2 border-[#9f0909] text-[#9f0909] hover:bg-[#9f0909] hover:text-white py-3 px-6 rounded-lg font-semibold transition-colors flex items-center justify-center gap-2"
        >
          <svg class="w-5 h-5" fill="none" stroke="currentColor" viewBox="0 0 24 24">
            <path
              stroke-linecap="round"
              stroke-linejoin="round"
              stroke-width="2"
              d="M3 12l2-2m0 0l7-7 7 7M5 10v10a1 1 0 001 1h3m10-11l2 2m-2-2v10a1 1 0 01-1 1h-3m-6 0a1 1 0 001-1v-4a1 1 0 011-1h2a1 1 0 011 1v4a1 1 0 001 1m-6 0h6"
            />
          </svg>
          Về trang chủ
        </button>
        <button
          v-if="!error"
          @click="goToOrderHistory"
          class="w-full bg-[#9f0909] hover:bg-[#800808] text-white py-3 px-6 rounded-lg font-semibold transition-colors flex items-center justify-center gap-2"
        >
          <svg class="w-5 h-5" fill="none" stroke="currentColor" viewBox="0 0 24 24">
            <path
              stroke-linecap="round"
              stroke-linejoin="round"
              stroke-width="2"
              d="M9 5H7a2 2 0 00-2 2v12a2 2 0 002 2h10a2 2 0 002-2V7a2 2 0 00-2-2h-2M9 5a2 2 0 002 2h2a2 2 0 002-2M9 5a2 2 0 012-2h2a2 2 0 012 2"
            />
          </svg>
          Xem lịch sử đơn hàng
        </button>
      </div>
    </div>
  </div>
</template>
