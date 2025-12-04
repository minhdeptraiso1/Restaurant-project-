<script setup lang="ts">
import { onMounted, ref, computed } from "vue";
import { useRoute } from "vue-router";
import { openByQr, addOrderItem, detailOrder } from "@/api/orders.service";
import { listDishes, type Dish } from "@/api/dishes.service";
import { listCombos, type Combo } from "@/api/combos.service";
import { toast } from "vue3-toastify";
import LoadingOverlay from "@/components/LoadingOverlay.vue";

const route = useRoute();

const qr = (route.query.qr as string) || (route.query.code as string) || null;
const note = (route.query.note as string) || undefined;

const loading = ref(false);
const loadingMenu = ref(false);
const addingItem = ref(false);
const order = ref<any | null>(null);
const error = ref<string | null>(null);

// Menu data
const dishes = ref<Dish[]>([]);
const combos = ref<Combo[]>([]);
const activeTab = ref<"dishes" | "combos">("dishes");
const searchQuery = ref("");

// Cart items (temporary before adding to order)
const cart = ref<
  { itemType: "DISH" | "COMBO"; itemId: string; name: string; price: number; quantity: number }[]
>([]);

async function loadByQr() {
  if (!qr) {
    error.value = "Mã QR không được cung cấp";
    return;
  }
  loading.value = true;
  try {
    const resp = await openByQr({ qrCode: qr, note });
    order.value = resp?.data || resp || null;

    if (order.value) {
      toast.success(`Đã mở order cho ${order.value.table?.code || "bàn"}`);
      await loadMenu();
    }
  } catch (e: any) {
    console.error(e);

    const errorMsg = e?.response?.data?.friendlyMessage || e?.friendlyMessage || e?.message || "";
    const status = e?.response?.status;

    if (status === 400 && (errorMsg.includes("order mở") || errorMsg.includes("already"))) {
      // User already has an open order - allow them to continue ordering
      toast.info("Bạn đã có order đang mở. Tiếp tục đặt món...");
      // Load menu to allow adding more items
      await loadMenu();
      // Set a minimal order object to enable the UI
      if (!order.value) {
        // Extract table info from QR if possible, or use placeholder
        order.value = {
          id: "pending", // Will need to get real ID when submitting
          table: { code: "Bàn hiện tại" },
        };
      }
    } else {
      error.value = errorMsg || "Không thể mở đặt chỗ bằng QR";
      toast.error(error.value);
    }
  } finally {
    loading.value = false;
  }
}

async function loadMenu() {
  loadingMenu.value = true;
  try {
    const [dishesResp, combosResp] = await Promise.all([listDishes(), listCombos()]);
    const dishesData = dishesResp?.content || dishesResp?.data?.content || dishesResp?.data || [];
    const combosData = combosResp?.content || combosResp?.data?.content || combosResp?.data || [];

    dishes.value = Array.isArray(dishesData) ? dishesData : [];
    combos.value = Array.isArray(combosData)
      ? combosData.filter((c: Combo) => c.status === "ACTIVE")
      : [];
  } catch (e: any) {
    console.error("Load menu error:", e);
    toast.error("Không thể tải menu");
  } finally {
    loadingMenu.value = false;
  }
}

// Filter menu items
const filteredDishes = computed(() => {
  if (!searchQuery.value.trim()) return dishes.value;
  const q = searchQuery.value.toLowerCase();
  return dishes.value.filter((d) => d.name.toLowerCase().includes(q));
});

const filteredCombos = computed(() => {
  if (!searchQuery.value.trim()) return combos.value;
  const q = searchQuery.value.toLowerCase();
  return combos.value.filter((c) => c.name.toLowerCase().includes(q));
});

// Add to cart
function addToCart(item: Dish | Combo, type: "DISH" | "COMBO") {
  const existing = cart.value.find((c) => c.itemId === item.id && c.itemType === type);
  if (existing) {
    existing.quantity++;
  } else {
    cart.value.push({
      itemType: type,
      itemId: item.id,
      name: item.name,
      price: item.price,
      quantity: 1,
    });
  }
  toast.success(`Đã thêm ${item.name} vào giỏ hàng`);
}

// Remove from cart
function removeFromCart(index: number) {
  cart.value.splice(index, 1);
}

// Update quantity
function updateQuantity(index: number, delta: number) {
  const item = cart.value[index];
  if (!item) return;
  item.quantity += delta;
  if (item.quantity <= 0) {
    removeFromCart(index);
  }
}

// Total
const cartTotal = computed(() => {
  return cart.value.reduce((sum, item) => sum + item.price * item.quantity, 0);
});

// Submit order items
async function submitOrder() {
  if (!order.value || cart.value.length === 0) {
    toast.warning("Giỏ hàng trống");
    return;
  }

  addingItem.value = true;
  try {
    // If order ID is pending, need to open a new order first with the QR code
    if (order.value.id === "pending") {
      toast.info("Đang tạo order mới với món đã chọn...");
      const newOrderResp = await openByQr({ qrCode: qr!, note });
      order.value = newOrderResp?.data || newOrderResp || null;

      if (!order.value?.id) {
        throw new Error("Không thể tạo order mới");
      }
    }

    // Add each item to order
    for (const item of cart.value) {
      await addOrderItem(order.value.id, {
        itemType: item.itemType,
        itemId: item.itemId,
        quantity: item.quantity,
      });
    }

    toast.success("Đã đặt món thành công!");
    cart.value = [];

    // Reload order to get updated items
    const updated = await detailOrder(order.value.id);
    order.value = updated.data || updated;
  } catch (e: any) {
    toast.error(e?.friendlyMessage || "Đặt món thất bại");
  } finally {
    addingItem.value = false;
  }
}

onMounted(() => {
  loadByQr();
});
</script>

<template>
  <LoadingOverlay
    :show="loading || addingItem"
    :message="loading ? 'Đang mở order...' : 'Đang đặt món...'"
  />

  <div class="min-h-screen bg-gradient-to-br from-slate-900 via-slate-800 to-slate-900">
    <!-- Error State -->
    <div v-if="error" class="flex items-center justify-center min-h-screen p-6">
      <div
        class="max-w-md w-full bg-red-500/10 border border-red-500/20 rounded-xl p-6 text-center"
      >
        <div class="text-6xl mb-4">❌</div>
        <h2 class="text-xl font-bold text-red-400 mb-2">Lỗi</h2>
        <p class="text-white/80">{{ error }}</p>
      </div>
    </div>

    <!-- Order Page -->
    <div v-if="order && !loading" class="pb-32">
      <!-- Header -->
      <div class="sticky top-0 z-40 bg-gradient-to-r from-emerald-600 to-blue-600 shadow-lg">
        <div class="container mx-auto px-4 py-4">
          <div class="flex items-center justify-between">
            <div>
              <h1 class="text-2xl font-bold text-white">{{ order.table?.code || "Bàn" }}</h1>
              <p class="text-sm text-white/80">Order #{{ order.id?.substring(0, 8) }}</p>
            </div>
            <div class="text-right">
              <div class="text-sm text-white/80">Giỏ hàng</div>
              <div class="text-2xl font-bold text-white">{{ cart.length }} món</div>
            </div>
          </div>
        </div>
      </div>

      <div class="container mx-auto px-4 py-6">
        <div class="grid lg:grid-cols-3 gap-6">
          <!-- Menu Section -->
          <div class="lg:col-span-2 space-y-4">
            <!-- Search -->
            <div class="relative">
              <span class="absolute left-4 top-4 text-xl">🔍</span>
              <input
                v-model="searchQuery"
                type="text"
                placeholder="Tìm món ăn..."
                class="w-full pl-12 pr-4 py-3 bg-white/10 border border-white/20 rounded-xl text-white placeholder-white/50 focus:outline-none focus:ring-2 focus:ring-emerald-500"
              />
            </div>

            <!-- Tabs -->
            <div class="flex gap-2">
              <button
                @click="activeTab = 'dishes'"
                :class="[
                  'flex-1 py-3 px-4 rounded-xl font-medium transition',
                  activeTab === 'dishes'
                    ? 'bg-emerald-500 text-white'
                    : 'bg-white/10 text-white/60 hover:bg-white/15',
                ]"
              >
                🍽️ Món ăn ({{ filteredDishes.length }})
              </button>
              <button
                @click="activeTab = 'combos'"
                :class="[
                  'flex-1 py-3 px-4 rounded-xl font-medium transition',
                  activeTab === 'combos'
                    ? 'bg-blue-500 text-white'
                    : 'bg-white/10 text-white/60 hover:bg-white/15',
                ]"
              >
                🎁 Combo ({{ filteredCombos.length }})
              </button>
            </div>

            <!-- Dishes List -->
            <div v-if="activeTab === 'dishes'" class="grid sm:grid-cols-2 gap-4">
              <div
                v-for="dish in filteredDishes"
                :key="dish.id"
                class="bg-white/10 backdrop-blur-sm border border-white/20 rounded-xl overflow-hidden hover:shadow-xl transition group cursor-pointer"
                @click="addToCart(dish, 'DISH')"
              >
                <div
                  class="aspect-video bg-gradient-to-br from-emerald-500/20 to-blue-500/20 flex items-center justify-center"
                >
                  <img
                    v-if="dish.imageUrl"
                    :src="dish.imageUrl"
                    :alt="dish.name"
                    class="w-full h-full object-cover"
                  />
                  <span v-else class="text-6xl">🍽️</span>
                </div>
                <div class="p-4">
                  <h3 class="font-bold text-white mb-1 group-hover:text-emerald-400 transition">
                    {{ dish.name }}
                  </h3>
                  <p class="text-sm text-white/60 mb-2 line-clamp-2">{{ dish.description }}</p>
                  <div class="flex items-center justify-between">
                    <span class="text-lg font-bold text-emerald-400">
                      {{ (dish.price || 0).toLocaleString() }}đ
                    </span>
                    <button
                      @click.stop="addToCart(dish, 'DISH')"
                      class="px-3 py-1 bg-emerald-500 hover:bg-emerald-600 text-white rounded-lg text-sm transition"
                    >
                      ➕ Thêm
                    </button>
                  </div>
                </div>
              </div>

              <div
                v-if="filteredDishes.length === 0"
                class="col-span-2 text-center py-12 text-white/60"
              >
                Không tìm thấy món ăn
              </div>
            </div>

            <!-- Combos List -->
            <div v-if="activeTab === 'combos'" class="grid sm:grid-cols-2 gap-4">
              <div
                v-for="combo in filteredCombos"
                :key="combo.id"
                class="bg-white/10 backdrop-blur-sm border border-white/20 rounded-xl overflow-hidden hover:shadow-xl transition group cursor-pointer"
                @click="addToCart(combo, 'COMBO')"
              >
                <div
                  class="aspect-video bg-gradient-to-br from-blue-500/20 to-purple-500/20 flex items-center justify-center"
                >
                  <img
                    v-if="combo.imageUrl"
                    :src="combo.imageUrl"
                    :alt="combo.name"
                    class="w-full h-full object-cover"
                  />
                  <span v-else class="text-6xl">🎁</span>
                </div>
                <div class="p-4">
                  <h3 class="font-bold text-white mb-1 group-hover:text-blue-400 transition">
                    {{ combo.name }}
                  </h3>
                  <p class="text-sm text-white/60 mb-2 line-clamp-2">{{ combo.description }}</p>
                  <div class="flex items-center justify-between">
                    <span class="text-lg font-bold text-blue-400">
                      {{ (combo.price || 0).toLocaleString() }}đ
                    </span>
                    <button
                      @click.stop="addToCart(combo, 'COMBO')"
                      class="px-3 py-1 bg-blue-500 hover:bg-blue-600 text-white rounded-lg text-sm transition"
                    >
                      ➕ Thêm
                    </button>
                  </div>
                </div>
              </div>

              <div
                v-if="filteredCombos.length === 0"
                class="col-span-2 text-center py-12 text-white/60"
              >
                Không tìm thấy combo
              </div>
            </div>
          </div>

          <!-- Cart Sidebar -->
          <div class="lg:col-span-1">
            <div
              class="sticky top-24 bg-white/10 backdrop-blur-sm border border-white/20 rounded-xl p-4"
            >
              <h3 class="text-lg font-bold text-white mb-4">🛒 Giỏ hàng</h3>

              <div v-if="cart.length === 0" class="text-center py-8 text-white/60">
                Giỏ hàng trống
              </div>

              <div v-else class="space-y-3 mb-4">
                <div
                  v-for="(item, idx) in cart"
                  :key="idx"
                  class="bg-white/5 rounded-lg p-3 space-y-2"
                >
                  <div class="flex justify-between items-start">
                    <div class="flex-1">
                      <div class="font-medium text-white">{{ item.name }}</div>
                      <div class="text-sm text-emerald-400">
                        {{ (item.price || 0).toLocaleString() }}đ
                      </div>
                    </div>
                    <button
                      @click="removeFromCart(idx)"
                      class="text-red-400 hover:text-red-300 text-sm"
                    >
                      ✕
                    </button>
                  </div>
                  <div class="flex items-center gap-2">
                    <button
                      @click="updateQuantity(idx, -1)"
                      class="w-8 h-8 bg-white/10 hover:bg-white/20 rounded text-white"
                    >
                      −
                    </button>
                    <span class="flex-1 text-center text-white font-medium">{{
                      item.quantity
                    }}</span>
                    <button
                      @click="updateQuantity(idx, 1)"
                      class="w-8 h-8 bg-white/10 hover:bg-white/20 rounded text-white"
                    >
                      +
                    </button>
                  </div>
                </div>
              </div>

              <div v-if="cart.length > 0">
                <div class="border-t border-white/20 pt-4 mb-4">
                  <div class="flex justify-between items-center text-lg font-bold">
                    <span class="text-white">Tổng cộng:</span>
                    <span class="text-emerald-400">{{ cartTotal.toLocaleString() }}đ</span>
                  </div>
                </div>

                <button
                  @click="submitOrder"
                  :disabled="addingItem"
                  class="w-full py-3 bg-gradient-to-r from-emerald-500 to-blue-500 hover:from-emerald-600 hover:to-blue-600 text-white font-bold rounded-xl transition disabled:opacity-50"
                >
                  <span v-if="addingItem" class="inline-flex items-center gap-2">
                    <span class="loading-spinner"></span>
                    Đang đặt món...
                  </span>
                  <span v-else>✅ Đặt món</span>
                </button>
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<style scoped>
.loading-spinner {
  border: 3px solid rgba(255, 255, 255, 0.3);
  border-top-color: white;
  border-radius: 50%;
  width: 1.5rem;
  height: 1.5rem;
  animation: spin 1s linear infinite;
  display: inline-block;
}

@keyframes spin {
  to {
    transform: rotate(360deg);
  }
}

.line-clamp-2 {
  display: -webkit-box;
  -webkit-line-clamp: 2;
  line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
}
</style>
