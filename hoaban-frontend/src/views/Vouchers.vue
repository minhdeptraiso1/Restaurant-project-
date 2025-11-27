<template>
  <div class="min-h-screen bg-gray-50 py-8">
    <div class="container mx-auto px-4 max-w-6xl">
      <!-- Header with Points -->
      <div
        class="mb-8 bg-gradient-to-br from-emerald-500 to-teal-500 rounded-2xl p-6 shadow-xl text-white"
      >
        <div class="flex items-center justify-between">
          <div>
            <h1 class="text-3xl font-bold mb-2">Voucher của tôi</h1>
            <p class="text-emerald-50">Đổi điểm lấy voucher hoặc sử dụng voucher đã có</p>
          </div>
          <div class="text-right">
            <div class="text-sm text-emerald-50 mb-1">Điểm hiện có</div>
            <div class="flex items-center gap-2 justify-end">
              <svg class="w-6 h-6" viewBox="0 0 24 24" fill="currentColor">
                <path
                  d="M12 2L15.09 8.26L22 9.27L17 14.14L18.18 21.02L12 17.77L5.82 21.02L7 14.14L2 9.27L8.91 8.26L12 2Z"
                />
              </svg>
              <span v-if="loadingPoints" class="text-2xl font-bold">
                <div
                  class="inline-block animate-spin rounded-full h-6 w-6 border-b-2 border-white"
                ></div>
              </span>
              <span v-else class="text-2xl font-bold">{{ points?.toLocaleString() || 0 }}</span>
            </div>
          </div>
        </div>
      </div>

      <!-- Tabs -->
      <div class="mb-6 border-b border-gray-200">
        <nav class="-mb-px flex space-x-8">
          <button
            @click="activeTab = 'catalog'"
            :class="[
              'py-4 px-1 border-b-2 font-medium text-sm transition-colors',
              activeTab === 'catalog'
                ? 'border-emerald-500 text-emerald-600'
                : 'border-transparent text-gray-500 hover:text-gray-700 hover:border-gray-300',
            ]"
          >
            <span class="flex items-center gap-2">
              <svg class="w-5 h-5" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                <path
                  stroke-linecap="round"
                  stroke-linejoin="round"
                  stroke-width="2"
                  d="M12 8c-1.657 0-3 .895-3 2s1.343 2 3 2 3 .895 3 2-1.343 2-3 2m0-8c1.11 0 2.08.402 2.599 1M12 8V7m0 1v8m0 0v1m0-1c-1.11 0-2.08-.402-2.599-1M21 12a9 9 0 11-18 0 9 9 0 0118 0z"
                />
              </svg>
              Đổi voucher ({{ catalogVouchers.length }})
            </span>
          </button>
          <button
            @click="activeTab = 'my-vouchers'"
            :class="[
              'py-4 px-1 border-b-2 font-medium text-sm transition-colors',
              activeTab === 'my-vouchers'
                ? 'border-emerald-500 text-emerald-600'
                : 'border-transparent text-gray-500 hover:text-gray-700 hover:border-gray-300',
            ]"
          >
            <span class="flex items-center gap-2">
              <svg class="w-5 h-5" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                <path
                  stroke-linecap="round"
                  stroke-linejoin="round"
                  stroke-width="2"
                  d="M15 5v2m0 4v2m0 4v2M5 5a2 2 0 00-2 2v3a2 2 0 110 4v3a2 2 0 002 2h14a2 2 0 002-2v-3a2 2 0 110-4V7a2 2 0 00-2-2H5z"
                />
              </svg>
              Voucher của tôi ({{ myVouchers.length }})
            </span>
          </button>
        </nav>
      </div>

      <!-- Catalog Tab -->
      <div v-if="activeTab === 'catalog'">
        <div v-if="loadingCatalog" class="text-center py-12">
          <div
            class="inline-block animate-spin rounded-full h-12 w-12 border-b-2 border-emerald-500"
          ></div>
          <p class="mt-4 text-gray-600">Đang tải voucher...</p>
        </div>

        <div v-else-if="catalogVouchers.length === 0" class="text-center py-12">
          <svg
            class="mx-auto h-24 w-24 text-gray-400"
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
          <h3 class="mt-4 text-lg font-medium text-gray-900">Chưa có voucher khả dụng</h3>
          <p class="mt-2 text-gray-500">Tích lũy thêm điểm để đổi voucher hấp dẫn!</p>
        </div>

        <div v-else class="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-6">
          <div
            v-for="voucher in catalogVouchers"
            :key="voucher.id"
            class="bg-white rounded-lg shadow-md overflow-hidden hover:shadow-xl transition-shadow flex flex-col"
            style="min-height: 420px"
          >
            <div class="bg-gradient-to-r from-emerald-500 to-teal-500 p-4 text-white flex-shrink-0">
              <div class="flex items-center justify-between mb-2">
                <span class="text-sm font-medium">{{ voucher.code }}</span>
                <span class="px-2 py-1 bg-white/20 rounded text-xs">
                  {{ voucher.type === "PERCENT" ? "Giảm %" : "Giảm tiền" }}
                </span>
              </div>
              <h3 class="text-xl font-bold line-clamp-2" style="min-height: 56px">
                {{ voucher.name }}
              </h3>
            </div>

            <div class="p-4 flex-1 flex flex-col">
              <div class="space-y-2 mb-4 flex-shrink-0">
                <div class="flex items-center justify-between text-sm">
                  <span class="text-gray-600">Giá trị:</span>
                  <span class="font-semibold text-emerald-600">
                    {{
                      voucher.type === "PERCENT"
                        ? `${voucher.value}%`
                        : formatCurrency(voucher.value)
                    }}
                  </span>
                </div>
                <div class="flex items-center justify-between text-sm">
                  <span class="text-gray-600">Đơn tối thiểu:</span>
                  <span class="font-semibold">{{ formatCurrency(voucher.minOrder) }}</span>
                </div>
                <div class="flex items-center justify-between text-sm" style="min-height: 20px">
                  <template v-if="voucher.maxDiscount">
                    <span class="text-gray-600">Giảm tối đa:</span>
                    <span class="font-semibold text-red-600">{{
                      formatCurrency(voucher.maxDiscount)
                    }}</span>
                  </template>
                </div>
                <div class="flex items-center justify-between text-sm pt-2 border-t">
                  <span class="text-gray-600">Chi phí:</span>
                  <span class="flex items-center gap-1 font-bold text-amber-600">
                    <svg class="w-4 h-4" viewBox="0 0 24 24" fill="currentColor">
                      <path
                        d="M12 2L15.09 8.26L22 9.27L17 14.14L18.18 21.02L12 17.77L5.82 21.02L7 14.14L2 9.27L8.91 8.26L12 2Z"
                      />
                    </svg>
                    {{ voucher.pointCost?.toLocaleString() || 0 }} điểm
                  </span>
                </div>
              </div>

              <div
                class="mb-4 text-sm text-gray-600 line-clamp-2 flex-shrink-0"
                style="min-height: 40px"
              >
                {{ voucher.description || "\u00A0" }}
              </div>

              <div class="flex-1 mb-4">
                <div class="text-xs text-gray-500">HSD: {{ formatDate(voucher.endAt) }}</div>
              </div>

              <button
                @click="redeemVoucher(voucher)"
                :disabled="redeeming === voucher.id || (points || 0) < voucher.pointCost"
                class="w-full py-2 px-4 bg-emerald-600 text-white rounded-lg hover:bg-emerald-700 transition-colors disabled:opacity-50 disabled:cursor-not-allowed flex items-center justify-center gap-2 flex-shrink-0"
              >
                <svg
                  v-if="redeeming === voucher.id"
                  class="animate-spin h-4 w-4"
                  fill="none"
                  viewBox="0 0 24 24"
                >
                  <circle
                    class="opacity-25"
                    cx="12"
                    cy="12"
                    r="10"
                    stroke="currentColor"
                    stroke-width="4"
                  ></circle>
                  <path
                    class="opacity-75"
                    fill="currentColor"
                    d="M4 12a8 8 0 018-8V0C5.373 0 0 5.373 0 12h4zm2 5.291A7.962 7.962 0 014 12H0c0 3.042 1.135 5.824 3 7.938l3-2.647z"
                  ></path>
                </svg>
                <span v-if="(points || 0) < voucher.pointCost">Không đủ điểm</span>
                <span v-else>{{ redeeming === voucher.id ? "Đang đổi..." : "Đổi ngay" }}</span>
              </button>
            </div>
          </div>
        </div>
      </div>

      <!-- My Vouchers Tab -->
      <div v-if="activeTab === 'my-vouchers'">
        <div v-if="loadingMyVouchers" class="text-center py-12">
          <div
            class="inline-block animate-spin rounded-full h-12 w-12 border-b-2 border-emerald-500"
          ></div>
          <p class="mt-4 text-gray-600">Đang tải voucher của bạn...</p>
        </div>

        <div v-else-if="myVouchers.length === 0" class="text-center py-12">
          <svg
            class="mx-auto h-24 w-24 text-gray-400"
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
          <h3 class="mt-4 text-lg font-medium text-gray-900">Chưa có voucher</h3>
          <p class="mt-2 text-gray-500">Đổi voucher từ catalog để sử dụng nhé!</p>
          <button
            @click="activeTab = 'catalog'"
            class="mt-4 inline-flex items-center px-6 py-3 bg-emerald-600 text-white rounded-lg hover:bg-emerald-700 transition-colors"
          >
            Xem voucher có thể đổi
          </button>
        </div>

        <div v-else class="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-6">
          <div
            v-for="userVoucher in myVouchers"
            :key="userVoucher.id"
            class="bg-white rounded-lg shadow-md overflow-hidden hover:shadow-xl transition-shadow flex flex-col"
            :class="{ 'opacity-60': userVoucher.status !== 'UNUSED' }"
            style="min-height: 340px"
          >
            <div
              class="p-4 text-white relative overflow-hidden flex-shrink-0"
              :class="getVoucherGradient(userVoucher.status)"
            >
              <div class="flex items-center justify-between mb-2">
                <span class="text-sm font-medium">{{ userVoucher.voucher?.code || "N/A" }}</span>
                <span class="px-2 py-1 bg-white/20 rounded text-xs">
                  {{ getStatusText(userVoucher.status) }}
                </span>
              </div>
              <h3 class="text-xl font-bold line-clamp-2" style="min-height: 56px">
                {{ userVoucher.voucher?.name || "Không có tên" }}
              </h3>

              <!-- Decorative circles -->
              <div class="absolute -right-4 -top-4 w-20 h-20 bg-white/10 rounded-full"></div>
              <div class="absolute -left-4 -bottom-4 w-16 h-16 bg-white/10 rounded-full"></div>
            </div>

            <div class="p-4 flex-1 flex flex-col">
              <div
                class="mb-4 text-sm text-gray-600 line-clamp-2 flex-shrink-0"
                style="min-height: 40px"
              >
                {{ userVoucher.voucher?.description || "\u00A0" }}
              </div>

              <div class="space-y-1 text-xs text-gray-500 mb-4 flex-1">
                <div>Đổi lúc: {{ formatDateTime(userVoucher.exchangedAt) }}</div>
                <div>HSD: {{ formatDate(userVoucher.expiresAt) }}</div>
                <div v-if="userVoucher.usedAt" class="text-red-600" style="min-height: 16px">
                  Đã dùng: {{ formatDateTime(userVoucher.usedAt) }}
                </div>
                <div v-else style="min-height: 16px">\u00A0</div>
              </div>

              <div class="flex-shrink-0">
                <button
                  v-if="userVoucher.status === 'UNUSED'"
                  @click="useVoucherInCheckout(userVoucher)"
                  class="w-full py-2 px-4 bg-emerald-600 text-white rounded-lg hover:bg-emerald-700 transition-colors flex items-center justify-center gap-2"
                >
                  <svg class="w-4 h-4" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                    <path
                      stroke-linecap="round"
                      stroke-linejoin="round"
                      stroke-width="2"
                      d="M3 3h2l.4 2M7 13h10l4-8H5.4M7 13L5.4 5M7 13l-2.293 2.293c-.63.63-.184 1.707.707 1.707H17m0 0a2 2 0 100 4 2 2 0 000-4zm-8 2a2 2 0 11-4 0 2 2 0 014 0z"
                    />
                  </svg>
                  Dùng ngay
                </button>
                <div
                  v-else
                  class="w-full py-2 px-4 bg-gray-100 text-gray-500 rounded-lg text-center"
                >
                  {{ userVoucher.status === "USED" ? "Đã sử dụng" : "Hết hạn" }}
                </div>
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from "vue";
import { useRouter } from "vue-router";
import {
  getLoyaltyCatalog,
  getMyVouchers,
  getMyLoyaltyPoints,
  redeemVoucherById,
  type LoyaltyVoucher,
  type LoyaltyUserVoucher,
  extractUserVouchers,
} from "@/api/loyalty.service";
import { getVoucherById, type Voucher } from "@/api/vouchers.service";
import { toast } from "vue3-toastify";

const router = useRouter();
const activeTab = ref<"catalog" | "my-vouchers">("catalog");

const extractErrorMessage = (error: unknown, fallback: string) => {
  if (typeof error === "string" && error.trim().length > 0) {
    return error;
  }

  if (error && typeof error === "object") {
    const maybeResponse = (
      error as {
        response?: { data?: { message?: string } };
        message?: string;
      }
    ).response?.data?.message;
    if (typeof maybeResponse === "string" && maybeResponse.trim().length > 0) {
      return maybeResponse;
    }

    const maybeMessage = (error as { message?: string }).message;
    if (typeof maybeMessage === "string" && maybeMessage.trim().length > 0) {
      return maybeMessage;
    }
  }

  return fallback;
};

// State
const loadingCatalog = ref(false);
const loadingMyVouchers = ref(false);
const loadingPoints = ref(false);
const catalogVouchers = ref<LoyaltyVoucher[]>([]);
const myVouchers = ref<LoyaltyUserVoucher[]>([]);
const points = ref<number>(0);
const redeeming = ref<string | null>(null);

// Fetch data
const fetchCatalog = async () => {
  loadingCatalog.value = true;
  try {
    const { data } = await getLoyaltyCatalog(0, 100);
    catalogVouchers.value = data.content || data || [];
  } catch (error: unknown) {
    console.error("Error fetching catalog:", error);
    toast.error(extractErrorMessage(error, "Không thể tải danh sách voucher"));
  } finally {
    loadingCatalog.value = false;
  }
};

const fetchMyVouchers = async () => {
  loadingMyVouchers.value = true;
  try {
    const response = await getMyVouchers();
    const payload = response?.data ?? response;
    const userVouchers = extractUserVouchers(payload);

    // API /v1/loyalty/me/vouchers already returns full voucher details
    myVouchers.value = userVouchers;
  } catch (error: unknown) {
    console.error("Error fetching my vouchers:", error);
    toast.error(extractErrorMessage(error, "Không thể tải voucher của bạn"));
    myVouchers.value = [];
  } finally {
    loadingMyVouchers.value = false;
  }
};

const fetchPoints = async () => {
  loadingPoints.value = true;
  try {
    const { data } = await getMyLoyaltyPoints();
    points.value = typeof data === "number" ? data : data?.points || 0;
  } catch (error: unknown) {
    console.error("Error fetching points:", error);
  } finally {
    loadingPoints.value = false;
  }
};

// Redeem voucher
const redeemVoucher = async (voucher: LoyaltyVoucher) => {
  if ((points.value || 0) < voucher.pointCost) {
    toast.error("Bạn không đủ điểm để đổi voucher này");
    return;
  }

  redeeming.value = voucher.id;
  try {
    await redeemVoucherById(voucher.id);
    toast.success(`Đổi voucher "${voucher.name}" thành công!`);

    // Refresh data
    await Promise.all([fetchPoints(), fetchMyVouchers(), fetchCatalog()]);

    // Switch to my vouchers tab
    activeTab.value = "my-vouchers";
  } catch (error: unknown) {
    console.error("Error redeeming voucher:", error);
    toast.error(extractErrorMessage(error, "Không thể đổi voucher"));
  } finally {
    redeeming.value = null;
  }
};

// Use voucher in checkout
const useVoucherInCheckout = (userVoucher: LoyaltyUserVoucher) => {
  // Store selected voucher in localStorage for checkout page
  localStorage.setItem(
    "selectedVoucher",
    JSON.stringify({
      userVoucherId: userVoucher.id,
      voucher: userVoucher.voucher,
    })
  );
  toast.success("Voucher đã được chọn! Vui lòng tiếp tục thanh toán.");
  router.push("/checkout");
};

// Helpers
const formatCurrency = (value: number) => {
  return new Intl.NumberFormat("vi-VN", {
    style: "currency",
    currency: "VND",
  }).format(value);
};

const formatDate = (dateString: string) => {
  const date = new Date(dateString);
  return date.toLocaleDateString("vi-VN");
};

const formatDateTime = (dateString: string) => {
  const date = new Date(dateString);
  return date.toLocaleString("vi-VN");
};

const getStatusText = (status: string) => {
  const texts: Record<string, string> = {
    UNUSED: "Chưa dùng",
    USED: "Đã dùng",
    EXPIRED: "Hết hạn",
  };
  return texts[status] || status;
};

const getVoucherGradient = (status: string) => {
  if (status === "UNUSED") return "bg-gradient-to-r from-emerald-500 to-teal-500";
  if (status === "USED") return "bg-gradient-to-r from-gray-400 to-gray-500";
  return "bg-gradient-to-r from-red-400 to-red-500";
};

onMounted(() => {
  fetchCatalog();
  fetchMyVouchers();
  fetchPoints();
});
</script>

<style scoped>
.line-clamp-2 {
  display: -webkit-box;
  -webkit-line-clamp: 2;
  line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
}
</style>
