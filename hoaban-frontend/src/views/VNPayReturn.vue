<template>
  <div class="min-h-screen bg-gray-50 flex items-center justify-center px-4">
    <div class="max-w-md w-full">
      <!-- Loading State -->
      <div v-if="loading" class="bg-white rounded-2xl shadow-xl p-8 text-center">
        <div
          class="animate-spin rounded-full h-16 w-16 border-b-2 border-blue-500 mx-auto mb-4"
        ></div>
        <h2 class="text-xl font-semibold text-gray-900 mb-2">Đang xử lý thanh toán...</h2>
        <p class="text-gray-600">Vui lòng chờ trong giây lát</p>
      </div>

      <!-- Success State -->
      <div
        v-else-if="paymentResult?.success"
        class="bg-white rounded-2xl shadow-xl p-8 text-center"
      >
        <div
          class="w-16 h-16 bg-green-100 rounded-full flex items-center justify-center mx-auto mb-4"
        >
          <svg class="w-8 h-8 text-green-600" fill="none" stroke="currentColor" viewBox="0 0 24 24">
            <path
              stroke-linecap="round"
              stroke-linejoin="round"
              stroke-width="2"
              d="M5 13l4 4L19 7"
            />
          </svg>
        </div>

        <h2 class="text-2xl font-bold text-gray-900 mb-2">Thanh toán thành công!</h2>
        <p class="text-gray-600 mb-6">
          {{ paymentResult.message || "Giao dịch đã được xử lý thành công" }}
        </p>

        <div class="bg-green-50 border border-green-200 rounded-lg p-4 mb-6 space-y-2">
          <div class="flex justify-between">
            <span class="text-gray-600">Mã đơn hàng:</span>
            <span class="font-medium">{{ paymentResult.orderId?.slice(0, 8) || "N/A" }}</span>
          </div>
          <div class="flex justify-between">
            <span class="text-gray-600">Số tiền:</span>
            <span class="font-medium text-green-600">{{
              formatCurrency(paymentResult.amount || 0)
            }}</span>
          </div>
          <div class="flex justify-between">
            <span class="text-gray-600">Mã giao dịch:</span>
            <span class="font-medium text-xs">{{ paymentResult.transactionId || "N/A" }}</span>
          </div>
        </div>

        <div class="space-y-3">
          <button
            @click="viewOrderDetails"
            class="w-full bg-green-600 hover:bg-green-700 text-white py-3 px-6 rounded-lg font-medium transition-colors"
          >
            Xem chi tiết đơn hàng
          </button>

          <button
            @click="goToHome"
            class="w-full bg-gray-100 hover:bg-gray-200 text-gray-800 py-3 px-6 rounded-lg font-medium transition-colors"
          >
            Về trang chủ
          </button>
        </div>
      </div>

      <!-- Error State -->
      <div v-else class="bg-white rounded-2xl shadow-xl p-8 text-center">
        <div
          class="w-16 h-16 bg-red-100 rounded-full flex items-center justify-center mx-auto mb-4"
        >
          <svg class="w-8 h-8 text-red-600" fill="none" stroke="currentColor" viewBox="0 0 24 24">
            <path
              stroke-linecap="round"
              stroke-linejoin="round"
              stroke-width="2"
              d="M6 18L18 6M6 6l12 12"
            />
          </svg>
        </div>

        <h2 class="text-2xl font-bold text-gray-900 mb-2">Thanh toán thất bại</h2>
        <p class="text-gray-600 mb-6">
          {{ paymentResult?.message || "Có lỗi xảy ra trong quá trình thanh toán" }}
        </p>

        <div class="bg-red-50 border border-red-200 rounded-lg p-4 mb-6">
          <div class="flex justify-between">
            <span class="text-gray-600">Mã lỗi:</span>
            <span class="font-medium text-red-600">{{
              paymentResult?.responseCode || "UNKNOWN"
            }}</span>
          </div>
        </div>

        <div class="space-y-3">
          <button
            @click="retryPayment"
            class="w-full bg-blue-600 hover:bg-blue-700 text-white py-3 px-6 rounded-lg font-medium transition-colors"
          >
            Thử lại thanh toán
          </button>

          <button
            @click="goToHome"
            class="w-full bg-gray-100 hover:bg-gray-200 text-gray-800 py-3 px-6 rounded-lg font-medium transition-colors"
          >
            Về trang chủ
          </button>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from "vue";
import { useRoute, useRouter } from "vue-router";
import { handleVNPayReturn, type VNPayReturnResponse } from "@/api/payments.service";
import { toast } from "vue3-toastify";
import { useCartStore } from "@/stores/cart";
import { useAuthStore } from "@/stores/auth";

const route = useRoute();
const router = useRouter();
const cart = useCartStore();
const auth = useAuthStore();

const loading = ref(true);
const paymentResult = ref<VNPayReturnResponse | null>(null);

const formatCurrency = (amount: number) => {
  return new Intl.NumberFormat("vi-VN", {
    style: "currency",
    currency: "VND",
  }).format(amount);
};

const viewOrderDetails = () => {
  if (paymentResult.value?.orderId) {
    router.push(`/order-success/${paymentResult.value.orderId}`);
  } else {
    router.push("/history");
  }
};

const goToHome = () => {
  router.push("/home");
};

const retryPayment = () => {
  router.push("/checkout");
};

onMounted(async () => {
  try {
    // Get all query parameters from URL
    const queryParams = { ...route.query } as Record<string, string>;

    // Call VNPay return API
    const response = await handleVNPayReturn(queryParams);
    paymentResult.value = response.data;

    if (response.data.success) {
      // Payment successful - clear local cart
      cart.items = [];
      cart.serverOrderId = "";
      console.log("🛒 Local cart cleared after VNPay payment");

      // Refresh loyalty points if available
      const maybeRefreshPoints = (auth as Partial<{ refreshPoints?: () => Promise<void> }>)
        .refreshPoints;
      if (typeof maybeRefreshPoints === "function") {
        await maybeRefreshPoints();
      }

      // Remove stored order ID
      localStorage.removeItem("vnpayOrderId");

      toast.success("Thanh toán VNPay thành công!");
    } else {
      toast.error(`Thanh toán thất bại: ${response.data.message}`);
    }
  } catch (error: any) {
    console.error("VNPay return error:", error);
    paymentResult.value = {
      success: false,
      orderId: "",
      amount: 0,
      transactionId: "",
      responseCode: "ERROR",
      message: error?.response?.data?.message || "Có lỗi xảy ra khi xử lý kết quả thanh toán",
    };
    toast.error("Có lỗi xảy ra khi xử lý kết quả thanh toán");
  } finally {
    loading.value = false;
  }
});
</script>
