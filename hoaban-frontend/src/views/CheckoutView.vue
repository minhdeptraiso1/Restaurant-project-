<script setup lang="ts">
import { useCartStore } from "@/stores/cart";
import { applyUserVoucher, payOrder, detailOrder } from "@/api/orders.service";
import { getCart } from "@/api/cart.service";
import { createVNPayPayment, type PaymentMethod } from "@/api/payments.service";
import { getMyVouchers, type LoyaltyUserVoucher, extractUserVouchers } from "@/api/loyalty.service";
import { computed, ref, onMounted, watch } from "vue";
import { toast } from "vue3-toastify";
import { useRouter } from "vue-router";
import { useAuthStore } from "@/stores/auth";
import LoadingOverlay from "@/components/LoadingOverlay.vue";

const cart = useCartStore();
const auth = useAuthStore();
const router = useRouter();
const method = ref<PaymentMethod>("CASH");
const customAmount = ref<number | null>(null);
const paying = ref(false);
const paymentStep = ref<string>("Đang xử lý thanh toán...");
const showVNPayQR = ref(false);
const vnpayPaymentUrl = ref("");
const vnpayQRImage = ref("");
const userVouchers = ref<LoyaltyUserVoucher[]>([]);
const selectedVoucher = ref<string>("");
const voucherDiscount = ref(0);

const VAT_RATE = 0.08; // 8% VAT

const subtotal = computed(() => cart.total);

const vat = computed(() => {
  const base = customAmount.value || subtotal.value;
  return Math.round(base * VAT_RATE);
});

const totalBeforeDiscount = computed(() => {
  const base = customAmount.value || subtotal.value;
  return base + vat.value;
});

const total = computed(() => {
  return Math.max(0, totalBeforeDiscount.value - voucherDiscount.value);
});

const extractErrorMessage = (error: unknown, fallback: string) => {
  if (typeof error === "string" && error.trim().length > 0) {
    return error;
  }

  if (error && typeof error === "object") {
    const responseMessage = (
      error as {
        response?: { data?: { message?: string } };
        message?: string;
        friendlyMessage?: string;
      }
    ).response?.data?.message;
    if (typeof responseMessage === "string" && responseMessage.trim().length > 0) {
      return responseMessage;
    }

    const friendlyMessage = (error as { friendlyMessage?: string }).friendlyMessage;
    if (typeof friendlyMessage === "string" && friendlyMessage.trim().length > 0) {
      return friendlyMessage;
    }

    const message = (error as { message?: string }).message;
    if (typeof message === "string" && message.trim().length > 0) {
      return message;
    }
  }

  return fallback;
};

const isVoucherExpired = (userVoucher: LoyaltyUserVoucher) => {
  const now = new Date();
  const expires = new Date(userVoucher.expiresAt);
  const expired = expires <= now;
  return expired;
};

const isVoucherEligible = (userVoucher: LoyaltyUserVoucher) => {
  // Will check properly when fetching full voucher details
  return true;
};

const availableVouchers = computed(() => {
  // Simply show all UNUSED vouchers (redeemed: false from API)
  return userVouchers.value.filter((uv) => {
    const isUnused = uv.status === "UNUSED";
    const notExpired = !isVoucherExpired(uv);
    return isUnused && notExpired;
  });
});

const eligibleVoucherIds = computed(() => {
  // Since API doesn't provide minOrder, all available vouchers are eligible
  return new Set(availableVouchers.value.map((uv) => uv.id));
});

const loadUserVouchers = async () => {
  try {
    const response = await getMyVouchers();
    const payload = response?.data ?? response;
    userVouchers.value = extractUserVouchers(payload);
  } catch (error: unknown) {
    console.error("Error loading vouchers:", error);
  }
};

const onVoucherChange = () => {
  voucherDiscount.value = 0;

  if (!selectedVoucher.value) {
    return;
  }

  const voucher = userVouchers.value.find((uv) => uv.id === selectedVoucher.value);
  if (!voucher) {
    toast.error("Không tìm thấy voucher");
    selectedVoucher.value = "";
    return;
  }

  // Calculate discount based on voucher type (Frontend only - no API call)
  const voucherData = voucher.voucher;
  const baseAmount = totalBeforeDiscount.value;

  // Check minimum order requirement
  if (voucherData.minOrder && baseAmount < voucherData.minOrder) {
    toast.warning(`Voucher này yêu cầu đơn hàng tối thiểu ${formatCurrency(voucherData.minOrder)}`);
    selectedVoucher.value = "";
    return;
  }

  let discount = 0;
  const voucherType = voucherData.type?.toUpperCase();
  const voucherValue = Number(voucherData.value) || 0;

  if (voucherType === "PERCENT") {
    // Percentage discount
    discount = Math.round((baseAmount * voucherValue) / 100);
    // Apply max discount cap if exists
    const maxCap = Number(voucherData.maxDiscount) || 0;
    if (maxCap > 0 && discount > maxCap) {
      discount = maxCap;
    }
  } else if (voucherType === "FIXED") {
    // Fixed amount discount
    discount = voucherValue;
  } else {
    toast.error(`Loại voucher không hợp lệ: ${voucherData.type}`);
    selectedVoucher.value = "";
    return;
  }

  // Don't exceed total amount
  voucherDiscount.value = Math.min(discount, baseAmount);

  if (voucherDiscount.value > 0) {
    toast.success(
      `Đã chọn voucher "${voucherData.name}" - Giảm ${formatCurrency(voucherDiscount.value)}!`
    );
  } else {
    toast.warning("Voucher không áp dụng được cho đơn hàng này");
    selectedVoucher.value = "";
  }
};
const formatCurrency = (amount: number) => {
  return new Intl.NumberFormat("vi-VN", {
    style: "currency",
    currency: "VND",
  }).format(amount);
};

watch([subtotal, customAmount], () => {
  // Just reset voucher selection when order value changes significantly
  // Backend will validate when applying voucher
  if (selectedVoucher.value) {
    onVoucherChange();
  }
});

// Customer info for order note
const customerInfo = ref({
  fullName: "",
  phone: "",
  address: "",
});

const submit = async () => {
  if (!cart.items.length) return toast.info("Giỏ hàng trống");
  if (!auth.token) return toast.info("Vui lòng đăng nhập để thanh toán");

  const finalAmount = customAmount.value || subtotal.value;
  if (finalAmount <= 0) return toast.error("Số tiền không hợp lệ");

  // Validate customer info
  if (!customerInfo.value.fullName.trim()) {
    return toast.error("Vui lòng cập nhật tên trong hồ sơ trước khi thanh toán");
  }
  if (!customerInfo.value.phone.trim()) {
    return toast.error("Vui lòng cập nhật số điện thoại trong hồ sơ trước khi thanh toán");
  }
  const phoneRegex = /^[0-9]{10,11}$/;
  if (!phoneRegex.test(customerInfo.value.phone.trim())) {
    return toast.error("Số điện thoại trong hồ sơ không hợp lệ (10-11 số)");
  }

  // Create customer note
  const customerNote = `Khách hàng: ${customerInfo.value.fullName.trim()} | SĐT: ${customerInfo.value.phone.trim()}${
    customerInfo.value.address.trim() ? ` | Địa chỉ: ${customerInfo.value.address.trim()}` : ""
  }`;

  paying.value = true;
  try {
    // Step 1: Get current cart (already has items)
    paymentStep.value = "Đang lấy thông tin giỏ hàng...";
    const { data: cartOrder } = await getCart();
    const orderId = cartOrder.id;

    if (!cartOrder.items || cartOrder.items.length === 0) {
      toast.error("Giỏ hàng trống, vui lòng thêm món trước khi thanh toán");
      return;
    }

    // Step 2: Apply voucher if selected
    if (selectedVoucher.value) {
      try {
        paymentStep.value = "Đang áp dụng voucher...";
        await applyUserVoucher(orderId, selectedVoucher.value);
      } catch (error: unknown) {
        const errorMsg = extractErrorMessage(error, "Không thể áp dụng voucher");
        toast.warning(`${errorMsg}. Tiếp tục thanh toán không giảm giá.`);
        voucherDiscount.value = 0;
      }
    }

    // Step 3: Handle payment based on method
    if (method.value === "VNPAY") {
      // Create VNPay payment
      paymentStep.value = "Đang tạo thanh toán VNPay...";
      const vnpayResponse = await createVNPayPayment(orderId, {
        returnUrl: `${window.location.origin}/vnpay-return`,
        cancelUrl: `${window.location.origin}/checkout`,
      });

      // Store order ID for return handling
      localStorage.setItem("vnpayOrderId", orderId);

      // Generate QR code for the payment URL
      const qrCodeUrl = `https://api.qrserver.com/v1/create-qr-code/?size=300x300&data=${encodeURIComponent(
        vnpayResponse.paymentUrl
      )}`;
      vnpayQRImage.value = qrCodeUrl;
      vnpayPaymentUrl.value = vnpayResponse.paymentUrl;

      // Show VNPay QR modal
      showVNPayQR.value = true;
      paying.value = false;

      toast.success("Đã tạo thanh toán VNPay! Quét mã QR để thanh toán.");
      return; // Don't proceed with cart clearing yet
    } else {
      // COD payment (delivery - pay on delivery)
      paymentStep.value = "Đang xác nhận đơn hàng...";
      await payOrder(orderId, { method: "COD", amount: total.value });
      toast.success("Đơn hàng đã được tạo! Vui lòng thanh toán khi nhận hàng.");
    }

    // Step 4: Get order details after payment
    paymentStep.value = "Đang lấy thông tin đơn hàng...";
    await detailOrder(orderId);

    // Step 5: Create new empty cart for next order
    paymentStep.value = "Đang tạo giỏ hàng mới...";
    await getCart(); // Backend will create new cart since old one is PAID/UNPAID

    // Clear local cart
    cart.items = [];
    cart.serverOrderId = "";
    console.log("🛒 Cart cleared, new cart created");
    const maybeRefreshPoints = (auth as Partial<{ refreshPoints?: () => Promise<void> }>)
      .refreshPoints;
    if (typeof maybeRefreshPoints === "function") {
      await maybeRefreshPoints();
    }
    router.push(`/order-success/${orderId}`);
  } catch (error: unknown) {
    toast.error(extractErrorMessage(error, "Thanh toán thất bại"));
  } finally {
    paying.value = false;
  }
};

// VNPay functions
const closeVNPayModal = () => {
  showVNPayQR.value = false;
  vnpayPaymentUrl.value = "";
  vnpayQRImage.value = "";
};

const openVNPayPayment = () => {
  if (vnpayPaymentUrl.value) {
    window.open(vnpayPaymentUrl.value, "_blank");
  }
};

const copyVNPayUrl = async () => {
  if (vnpayPaymentUrl.value) {
    try {
      await navigator.clipboard.writeText(vnpayPaymentUrl.value);
      toast.success("Đã sao chép link thanh toán!");
    } catch (error) {
      toast.error("Không thể sao chép link");
    }
  }
};

onMounted(async () => {
  if (auth.token) {
    await loadUserVouchers();

    // Auto-fill customer info from user profile
    if (auth.user) {
      customerInfo.value.fullName = auth.user.fullName || "";
      customerInfo.value.phone = (auth.user as any).phone || "";
      customerInfo.value.address = (auth.user as any).address || "";
    }

    // Check if voucher was selected from vouchers page
    const savedVoucher = localStorage.getItem("selectedVoucher");
    if (savedVoucher) {
      try {
        const { userVoucherId } = JSON.parse(savedVoucher);
        const voucher = availableVouchers.value.find((v) => v.id === userVoucherId);
        if (voucher && isVoucherEligible(voucher)) {
          selectedVoucher.value = userVoucherId;
          onVoucherChange();
          toast.success("Voucher đã được chọn!");
        }
      } catch (error: unknown) {
        console.error("Error parsing saved voucher:", error);
      }
      localStorage.removeItem("selectedVoucher");
    }
  }
});
</script>

<template>
  <LoadingOverlay :show="paying" :message="paymentStep" />

  <div class="max-w-2xl mx-auto px-4 py-8">
    <h1 class="text-3xl font-bold text-gray-900 mb-8">Thanh toán</h1>

    <div class="bg-white rounded-xl shadow-lg p-6 space-y-6">
      <!-- Customer Information -->
      <div class="space-y-4">
        <h2 class="text-xl font-semibold text-gray-800 flex items-center gap-2">
          <svg
            class="w-5 h-5 text-emerald-600"
            fill="none"
            stroke="currentColor"
            viewBox="0 0 24 24"
          >
            <path
              stroke-linecap="round"
              stroke-linejoin="round"
              stroke-width="2"
              d="M16 7a4 4 0 11-8 0 4 4 0 018 0zM12 14a7 7 0 00-7 7h14a7 7 0 00-7-7z"
            />
          </svg>
          Thông tin khách hàng
        </h2>

        <!-- Auto-fill hint for logged in users -->
        <div
          v-if="auth.user && (customerInfo.fullName || customerInfo.phone || customerInfo.address)"
          class="bg-green-50 border border-green-200 rounded-lg p-3 mb-4"
        >
          <p class="text-sm text-green-800 flex items-center gap-2">
            <svg class="w-4 h-4" fill="none" stroke="currentColor" viewBox="0 0 24 24">
              <path
                stroke-linecap="round"
                stroke-linejoin="round"
                stroke-width="2"
                d="M5 13l4 4L19 7"
              />
            </svg>
            ✨ Thông tin đã được tự động điền từ hồ sơ cá nhân của bạn. Bạn có thể chỉnh sửa nếu
            cần.
          </p>
        </div>

        <div class="grid grid-cols-1 md:grid-cols-2 gap-4">
          <div>
            <label class="block text-sm font-medium text-gray-700 mb-2"> Tên khách hàng </label>
            <div
              class="w-full px-4 py-3 bg-gray-50 border border-gray-200 rounded-lg text-gray-900"
            >
              {{ customerInfo.fullName || "Chưa cập nhật" }}
            </div>
          </div>
          <div>
            <label class="block text-sm font-medium text-gray-700 mb-2"> Số điện thoại </label>
            <div
              class="w-full px-4 py-3 bg-gray-50 border border-gray-200 rounded-lg text-gray-900"
            >
              {{ customerInfo.phone || "Chưa cập nhật" }}
            </div>
          </div>
        </div>

        <div>
          <label class="block text-sm font-medium text-gray-700 mb-2"> Địa chỉ </label>
          <div
            class="w-full px-4 py-3 bg-gray-50 border border-gray-200 rounded-lg text-gray-900 min-h-[84px]"
          >
            {{ customerInfo.address || "Chưa cập nhật" }}
          </div>
        </div>

        <div class="bg-blue-50 border border-blue-200 rounded-lg p-3">
          <p class="text-sm text-blue-800">
            💡 Thông tin hiển thị từ hồ sơ của bạn. Vui lòng cập nhật hồ sơ nếu cần thay đổi.
          </p>
        </div>
      </div>

      <!-- Order Items -->
      <div class="space-y-3">
        <h2 class="text-xl font-semibold text-gray-800">Chi tiết đơn hàng</h2>
        <div
          v-for="it in cart.items"
          :key="it.id"
          class="flex justify-between items-center py-3 border-b border-gray-200 last:border-0"
        >
          <div class="flex-1">
            <h3 class="font-medium text-gray-900">{{ it.name }}</h3>
            <p class="text-sm text-gray-500">{{ it.type === "COMBO" ? "Combo" : "Món đơn" }}</p>
          </div>
          <div class="text-center px-4">
            <span class="text-gray-600">× {{ it.qty }}</span>
          </div>
          <div class="text-right">
            <span class="font-medium">{{ formatCurrency(it.price * it.qty) }}</span>
          </div>
        </div>
      </div>

      <!-- Custom Amount -->
      <div class="space-y-3">
        <h3 class="text-lg font-semibold text-gray-800">Tùy chỉnh số tiền</h3>
        <div class="flex items-center space-x-4">
          <label class="flex items-center">
            <input
              type="radio"
              :value="false"
              :checked="!customAmount"
              @change="
                customAmount = null;
                onVoucherChange();
              "
              class="mr-2"
            />
            <span>Theo giỏ hàng: {{ formatCurrency(subtotal) }}</span>
          </label>
        </div>
        <div class="flex items-center space-x-4">
          <label class="flex items-center">
            <input
              type="radio"
              :value="true"
              :checked="!!customAmount"
              @change="if (!customAmount) customAmount = subtotal;"
              class="mr-2"
            />
            <span>Nhập số tiền khác:</span>
          </label>
          <input
            v-if="customAmount !== null"
            v-model.number="customAmount"
            type="number"
            min="0"
            step="1000"
            @input="onVoucherChange"
            class="px-3 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-blue-500 focus:border-blue-500"
            placeholder="Nhập số tiền"
          />
        </div>
      </div>

      <!-- Voucher Selection -->
      <div class="space-y-3">
        <div class="flex items-center justify-between">
          <h3 class="text-lg font-semibold text-gray-800 flex items-center gap-2">
            <svg
              class="w-5 h-5 text-emerald-600"
              fill="none"
              stroke="currentColor"
              viewBox="0 0 24 24"
            >
              <path
                stroke-linecap="round"
                stroke-linejoin="round"
                stroke-width="2"
                d="M15 5v2m0 4v2m0 4v2M5 5a2 2 0 00-2 2v3a2 2 0 110 4v3a2 2 0 002 2h14a2 2 0 002-2v-3a2 2 0 110-4V7a2 2 0 00-2-2H5z"
              />
            </svg>
            Voucher giảm giá
          </h3>
          <router-link
            to="/vouchers"
            class="text-sm text-emerald-600 hover:text-emerald-700 font-medium flex items-center gap-1"
          >
            <svg class="w-4 h-4" fill="none" stroke="currentColor" viewBox="0 0 24 24">
              <path
                stroke-linecap="round"
                stroke-linejoin="round"
                stroke-width="2"
                d="M12 6v6m0 0v6m0-6h6m-6 0H6"
              />
            </svg>
            Xem thêm
          </router-link>
        </div>

        <div v-if="availableVouchers.length > 0" class="space-y-3">
          <select
            v-model="selectedVoucher"
            @change="onVoucherChange"
            class="w-full px-4 py-3 border-2 border-gray-200 rounded-lg focus:ring-2 focus:ring-emerald-500 focus:border-emerald-500 transition-colors"
          >
            <option value="">🎫 Chọn voucher (tùy chọn)</option>
            <option
              v-for="userVoucher in availableVouchers"
              :key="userVoucher.id"
              :value="userVoucher.id"
            >
              {{ userVoucher.voucher.name }}
              <template v-if="userVoucher.voucher.code">
                ({{ userVoucher.voucher.code }})
              </template>
            </option>
          </select>

          <p
            v-if="availableVouchers.length > 0 && eligibleVoucherIds.size === 0"
            class="text-sm text-amber-600 bg-amber-50 border border-amber-200 rounded-md p-3"
          >
            Bạn có {{ availableVouchers.length }} voucher. Chọn để xem chi tiết và áp dụng.
          </p>

          <div
            v-if="selectedVoucher"
            class="bg-gradient-to-r from-emerald-50 to-teal-50 border-2 border-emerald-200 rounded-lg p-4"
          >
            <div class="flex items-center justify-between">
              <div class="flex items-center gap-2">
                <div class="flex h-8 w-8 items-center justify-center rounded-full bg-emerald-500">
                  <svg
                    class="w-5 h-5 text-white"
                    fill="none"
                    stroke="currentColor"
                    viewBox="0 0 24 24"
                  >
                    <path
                      stroke-linecap="round"
                      stroke-linejoin="round"
                      stroke-width="2"
                      d="M5 13l4 4L19 7"
                    />
                  </svg>
                </div>
                <div>
                  <div class="text-sm font-medium text-emerald-900">Voucher đã chọn</div>
                  <div class="text-xs text-emerald-700">
                    {{ availableVouchers.find((v) => v.id === selectedVoucher)?.voucher.name }}
                  </div>
                </div>
              </div>
              <div class="text-right">
                <div class="text-lg font-bold text-emerald-700">
                  -{{ formatCurrency(voucherDiscount) }}
                </div>
              </div>
            </div>
          </div>
        </div>

        <div
          v-else
          class="bg-gray-50 border-2 border-dashed border-gray-200 rounded-lg p-4 text-center"
        >
          <svg
            class="mx-auto h-12 w-12 text-gray-400 mb-2"
            fill="none"
            stroke="currentColor"
            viewBox="0 0 24 24"
          >
            <path
              stroke-linecap="round"
              stroke-linejoin="round"
              stroke-width="2"
              d="M15 5v2m0 4v2m0 4v2M5 5a2 2 0 00-2 2v3a2 2 0 110 4v3a2 2 0 002 2h14a2 2 0 002-2v-3a2 2 0 110-4V7a2 2 0 00-2-2H5z"
            />
          </svg>
          <p class="text-sm text-gray-600 mb-2">Bạn chưa có voucher khả dụng</p>
          <router-link
            to="/vouchers"
            class="inline-flex items-center gap-1 text-sm text-emerald-600 hover:text-emerald-700 font-medium"
          >
            Đổi voucher ngay
            <svg class="w-4 h-4" fill="none" stroke="currentColor" viewBox="0 0 24 24">
              <path
                stroke-linecap="round"
                stroke-linejoin="round"
                stroke-width="2"
                d="M9 5l7 7-7 7"
              />
            </svg>
          </router-link>
        </div>
      </div>

      <!-- Order Summary -->
      <div class="bg-gray-50 rounded-lg p-4 space-y-2">
        <div class="flex justify-between text-gray-600">
          <span>Tạm tính:</span>
          <span>{{ formatCurrency(customAmount || subtotal) }}</span>
        </div>
        <div class="flex justify-between text-gray-600">
          <span>VAT (8%):</span>
          <span>{{ formatCurrency(vat) }}</span>
        </div>
        <div class="flex justify-between text-gray-700 font-medium">
          <span>Tổng trước giảm giá:</span>
          <span>{{ formatCurrency(totalBeforeDiscount) }}</span>
        </div>
        <div
          v-if="selectedVoucher && voucherDiscount > 0"
          class="flex justify-between text-green-600 font-medium"
        >
          <span>
            Giảm giá voucher
            <template v-if="availableVouchers.find((v) => v.id === selectedVoucher)">
              ({{
                availableVouchers.find((v) => v.id === selectedVoucher)?.voucher.name
              }}) </template
            >:
          </span>
          <span>-{{ formatCurrency(voucherDiscount) }}</span>
        </div>
        <hr class="border-gray-300" />
        <div class="flex justify-between font-bold text-lg text-gray-900">
          <span>Tổng thanh toán:</span>
          <span>{{ formatCurrency(total) }}</span>
        </div>
      </div>

      <!-- Payment Method -->
      <div class="space-y-4">
        <h3 class="text-lg font-semibold text-gray-800 flex items-center gap-2">
          <svg
            class="w-5 h-5 text-emerald-600"
            fill="none"
            stroke="currentColor"
            viewBox="0 0 24 24"
          >
            <path
              stroke-linecap="round"
              stroke-linejoin="round"
              stroke-width="2"
              d="M17 9V7a2 2 0 00-2-2H5a2 2 0 00-2 2v6a2 2 0 002 2h2m2 4h10a2 2 0 002-2v-6a2 2 0 00-2-2H9a2 2 0 00-2 2v6a2 2 0 002 2zm7-5a2 2 0 11-4 0 2 2 0 014 0z"
            />
          </svg>
          Phương thức thanh toán
        </h3>

        <div class="grid grid-cols-1 sm:grid-cols-2 gap-4">
          <!-- Cash Payment -->
          <label
            class="relative flex items-center p-4 bg-white border-2 border-gray-200 rounded-xl cursor-pointer hover:border-green-300 transition-colors"
            :class="{ 'border-green-500 bg-green-50': method === 'CASH' }"
          >
            <input type="radio" value="CASH" v-model="method" class="sr-only" />
            <div class="flex items-center gap-3 w-full">
              <div
                class="flex-shrink-0 w-10 h-10 bg-green-100 rounded-full flex items-center justify-center"
              >
                <svg
                  class="w-5 h-5 text-green-600"
                  fill="none"
                  stroke="currentColor"
                  viewBox="0 0 24 24"
                >
                  <path
                    stroke-linecap="round"
                    stroke-linejoin="round"
                    stroke-width="2"
                    d="M17 9V7a2 2 0 00-2-2H5a2 2 0 00-2 2v6a2 2 0 002 2h2m2 4h10a2 2 0 002-2v-6a2 2 0 00-2-2H9a2 2 0 00-2 2v6a2 2 0 002 2zm7-5a2 2 0 11-4 0 2 2 0 014 0z"
                  />
                </svg>
              </div>
              <div>
                <div class="font-medium text-gray-900">Thanh toán khi nhận hàng (COD)</div>
                <div class="text-sm text-gray-500">Trả tiền mặt cho shipper</div>
              </div>
            </div>
            <div
              v-if="method === 'CASH'"
              class="absolute top-2 right-2 w-5 h-5 bg-green-500 rounded-full flex items-center justify-center"
            >
              <svg class="w-3 h-3 text-white" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                <path
                  stroke-linecap="round"
                  stroke-linejoin="round"
                  stroke-width="2"
                  d="M5 13l4 4L19 7"
                />
              </svg>
            </div>
          </label>

          <!-- VNPay QR -->
          <label
            class="relative flex items-center p-4 bg-white border-2 border-gray-200 rounded-xl cursor-pointer hover:border-red-300 transition-colors"
            :class="{ 'border-red-500 bg-red-50': method === 'VNPAY' }"
          >
            <input type="radio" value="VNPAY" v-model="method" class="sr-only" />
            <div class="flex items-center gap-3 w-full">
              <div
                class="flex-shrink-0 w-10 h-10 bg-red-100 rounded-full flex items-center justify-center"
              >
                <svg
                  class="w-5 h-5 text-red-600"
                  fill="none"
                  stroke="currentColor"
                  viewBox="0 0 24 24"
                >
                  <path
                    stroke-linecap="round"
                    stroke-linejoin="round"
                    stroke-width="2"
                    d="M12 4v1m6 11h2m-6 0h-2v4m0-11v3m0 0h.01M12 12h4.01M16 20h4M4 12h4m12 0h.01M5 8h2a1 1 0 001-1V5a1 1 0 00-1-1H5a1 1 0 00-1 1v2a1 1 0 001 1zm12 0h2a1 1 0 001-1V5a1 1 0 00-1-1h-2a1 1 0 00-1 1v2a1 1 0 001 1zM5 20h2a1 1 0 001-1v-2a1 1 0 00-1-1H5a1 1 0 00-1 1v2a1 1 0 001 1z"
                  />
                </svg>
              </div>
              <div>
                <div class="font-medium text-gray-900">VNPay QR</div>
                <div class="text-sm text-gray-500">Quét mã QR thanh toán</div>
              </div>
            </div>
            <div
              v-if="method === 'VNPAY'"
              class="absolute top-2 right-2 w-5 h-5 bg-red-500 rounded-full flex items-center justify-center"
            >
              <svg class="w-3 h-3 text-white" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                <path
                  stroke-linecap="round"
                  stroke-linejoin="round"
                  stroke-width="2"
                  d="M5 13l4 4L19 7"
                />
              </svg>
            </div>
          </label>
        </div>

        <!-- VNPay Description -->
        <div v-if="method === 'VNPAY'" class="bg-red-50 border border-red-200 rounded-lg p-4">
          <div class="flex items-start gap-3">
            <div class="flex-shrink-0">
              <svg
                class="w-6 h-6 text-red-600"
                fill="none"
                stroke="currentColor"
                viewBox="0 0 24 24"
              >
                <path
                  stroke-linecap="round"
                  stroke-linejoin="round"
                  stroke-width="2"
                  d="M13 16h-1v-4h-1m1-4h.01M21 12a9 9 0 11-18 0 9 9 0 0118 0z"
                />
              </svg>
            </div>
            <div>
              <h4 class="font-medium text-red-900 mb-1">Thanh toán VNPay</h4>
              <ul class="text-sm text-red-800 space-y-1">
                <li>• Hỗ trợ tất cả ngân hàng trong nước</li>
                <li>• Thanh toán qua ví điện tử VNPay</li>
                <li>• Quét mã QR để thanh toán nhanh chóng</li>
                <li>• Giao dịch được mã hóa bảo mật cao</li>
              </ul>
            </div>
          </div>
        </div>
      </div>

      <!-- Submit Button -->
      <button
        :disabled="paying || total <= 0"
        @click="submit"
        class="w-full bg-gradient-to-r from-emerald-600 to-emerald-700 text-white py-4 px-6 rounded-lg font-semibold text-lg hover:from-emerald-700 hover:to-emerald-800 transition-all disabled:opacity-50 disabled:cursor-not-allowed shadow-lg"
      >
        <i v-if="paying" class="fas fa-spinner fa-spin mr-2"></i>
        <i v-else class="fas fa-credit-card mr-2"></i>
        {{ paying ? "Đang thanh toán..." : `Thanh toán ${formatCurrency(total)}` }}
      </button>

      <p class="text-center text-sm text-gray-500 mt-4">
        Bằng cách thanh toán, bạn đồng ý với các điều khoản và điều kiện của chúng tôi.
      </p>
    </div>

    <!-- VNPay QR Modal -->
    <div
      v-if="showVNPayQR"
      class="fixed inset-0 bg-black bg-opacity-50 flex items-center justify-center z-50 p-4 overflow-y-auto"
    >
      <div class="bg-white rounded-2xl shadow-2xl max-w-md w-full p-6 relative my-8">
        <!-- Close Button -->
        <button
          @click="closeVNPayModal"
          class="absolute top-4 right-4 text-gray-400 hover:text-gray-600 transition-colors"
        >
          <svg class="w-6 h-6" fill="none" stroke="currentColor" viewBox="0 0 24 24">
            <path
              stroke-linecap="round"
              stroke-linejoin="round"
              stroke-width="2"
              d="M6 18L18 6M6 6l12 12"
            />
          </svg>
        </button>

        <!-- Header -->
        <div class="text-center mb-6">
          <div
            class="w-16 h-16 bg-red-100 rounded-full flex items-center justify-center mx-auto mb-4"
          >
            <svg class="w-8 h-8 text-red-600" fill="none" stroke="currentColor" viewBox="0 0 24 24">
              <path
                stroke-linecap="round"
                stroke-linejoin="round"
                stroke-width="2"
                d="M12 4v1m6 11h2m-6 0h-2v4m0-11v3m0 0h.01M12 12h4.01M16 20h4M4 12h4m12 0h.01M5 8h2a1 1 0 001-1V5a1 1 0 00-1-1H5a1 1 0 00-1 1v2a1 1 0 001 1zm12 0h2a1 1 0 001-1V5a1 1 0 00-1-1h-2a1 1 0 00-1 1v2a1 1 0 001 1zM5 20h2a1 1 0 001-1v-2a1 1 0 00-1-1H5a1 1 0 00-1 1v2a1 1 0 001 1z"
              />
            </svg>
          </div>
          <h3 class="text-xl font-bold text-gray-900 mb-2">Thanh toán VNPay</h3>
          <p class="text-gray-600">Quét mã QR hoặc mở link để thanh toán</p>
        </div>

        <!-- QR Code -->
        <div class="text-center mb-6">
          <div class="inline-block p-4 bg-white border-2 border-gray-200 rounded-xl">
            <img
              v-if="vnpayQRImage"
              :src="vnpayQRImage"
              alt="VNPay QR Code"
              class="w-48 h-48 object-contain"
            />
            <div v-else class="w-48 h-48 bg-gray-100 rounded-lg flex items-center justify-center">
              <div class="animate-spin rounded-full h-8 w-8 border-b-2 border-red-500"></div>
            </div>
          </div>
          <p class="text-sm text-gray-500 mt-2">Sử dụng app VNPay hoặc banking để quét</p>
        </div>

        <!-- Payment Amount -->
        <div class="bg-gray-50 rounded-lg p-4 mb-6">
          <div class="flex justify-between items-center">
            <span class="text-gray-600">Số tiền thanh toán:</span>
            <span class="font-bold text-xl text-red-600">{{ formatCurrency(total) }}</span>
          </div>
        </div>

        <!-- Action Buttons -->
        <div class="space-y-3">
          <button
            @click="openVNPayPayment"
            class="w-full bg-red-600 hover:bg-red-700 text-white py-3 px-6 rounded-lg font-medium transition-colors flex items-center justify-center gap-2"
          >
            <svg class="w-5 h-5" fill="none" stroke="currentColor" viewBox="0 0 24 24">
              <path
                stroke-linecap="round"
                stroke-linejoin="round"
                stroke-width="2"
                d="M10 6H6a2 2 0 00-2 2v10a2 2 0 002 2h10a2 2 0 002-2v-4M14 4h6m0 0v6m0-6L10 14"
              />
            </svg>
            Mở link thanh toán
          </button>

          <button
            @click="copyVNPayUrl"
            class="w-full bg-gray-100 hover:bg-gray-200 text-gray-800 py-3 px-6 rounded-lg font-medium transition-colors flex items-center justify-center gap-2"
          >
            <svg class="w-5 h-5" fill="none" stroke="currentColor" viewBox="0 0 24 24">
              <path
                stroke-linecap="round"
                stroke-linejoin="round"
                stroke-width="2"
                d="M8 16H6a2 2 0 01-2-2V6a2 2 0 012-2h8a2 2 0 012 2v2m-6 12h8a2 2 0 002-2v-8a2 2 0 00-2-2h-8a2 2 0 00-2 2v8a2 2 0 002 2z"
              />
            </svg>
            Sao chép link
          </button>
        </div>

        <!-- Instructions -->
        <div class="mt-6 bg-blue-50 border border-blue-200 rounded-lg p-4">
          <h4 class="font-medium text-blue-900 mb-2">Hướng dẫn thanh toán:</h4>
          <ol class="text-sm text-blue-800 space-y-1">
            <li>1. Mở app VNPay hoặc app ngân hàng</li>
            <li>2. Chọn tính năng quét mã QR</li>
            <li>3. Quét mã QR phía trên</li>
            <li>4. Xác nhận thanh toán</li>
            <li>5. Bạn sẽ được chuyển về trang xác nhận</li>
          </ol>
        </div>

        <!-- Auto-redirect notice -->
        <div class="mt-4 text-center">
          <p class="text-xs text-gray-500">
            💡 Sau khi thanh toán thành công, bạn sẽ tự động được chuyển về trang xác nhận
          </p>
        </div>
      </div>
    </div>
  </div>
</template>
