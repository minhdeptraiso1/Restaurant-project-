<script setup lang="ts">
import { ref, onMounted, computed, watch } from "vue";
import { toast } from "vue3-toastify";
import {
  openOrderByStaff,
  addOrderItem,
  payOrder,
  getAllOrders,
  clearOrder,
  updateOrderStatus as updateOrderStatusAPI,
} from "@/api/orders.service";
import { listTables } from "@/api/tables.admin";
import { listAreas } from "@/api/areas.admin";
import { listDishes } from "@/api/dishes.service";

// Data
const orders = ref<any[]>([]);
const tables = ref<any[]>([]);
const areas = ref<any[]>([]);
const dishes = ref<any[]>([]);
const loading = ref(false);
const processingPayment = ref(false);

// Filters
const selectedStatus = ref<"ALL" | "OPEN" | "PAID" | "CANCELLED" | "UNPAID">("OPEN");
const q = ref("");

// Status options
const statusOptions = [
  { value: "ALL", label: "Tất cả", emoji: "📋" },
  { value: "OPEN", label: "Đang mở", emoji: "🔵" },
  { value: "UNPAID", label: "Chưa thanh toán", emoji: "🟡" },
  { value: "PAID", label: "Đã thanh toán", emoji: "✅" },
  { value: "CANCELLED", label: "Đã hủy", emoji: "⛔" },
];

// === Open Order Modal ===
const showOpenModal = ref(false);
const selectedAreaId = ref("");
const newOrderForm = ref({
  tableId: "",
  reservationId: "",
  note: "",
});
const submitting = ref(false);

// available tables
const availableTables = computed(() => tables.value.filter((t) => t.status === "AVAILABLE"));
const availableTablesInArea = computed(() => {
  if (!selectedAreaId.value) return [];
  return availableTables.value.filter((t) => t.areaId === selectedAreaId.value);
});
watch(selectedAreaId, () => (newOrderForm.value.tableId = ""));

// === Add Item Modal ===
const showAddItemModal = ref(false);
const addItemForm = ref({ orderId: "", dishId: "", quantity: 1 });

// === Pay Modal ===
const showPayModal = ref(false);
const paymentForm = ref({ orderId: "", method: "CASH" as "CASH" | "BANK", amount: 0 });

// === Update Status Modal ===
const showStatusModal = ref(false);
const statusForm = ref({ orderId: "", status: "" });
const statusUpdateOptions = [
  { value: "OPEN", label: "Đang mở" },
  { value: "UNPAID", label: "Chưa thanh toán" },
  { value: "PAID", label: "Đã thanh toán" },
  { value: "CANCELLED", label: "Đã hủy" },
];

// Utilities
function areaName(areaId?: string) {
  if (!areaId) return "N/A";
  return areas.value.find((a) => a.id === areaId)?.name || "N/A";
}
function getTableName(tableId?: string) {
  if (!tableId) return "Mang về";
  const table = tables.value.find((t) => t.id === tableId);
  if (!table) return "Mang về";
  return `${table.code || table.name || "Bàn"} (${areaName(table.areaId)})`;
}
function getDishName(id: string) {
  return dishes.value.find((d) => d.id === id)?.name || "N/A";
}
function vnd(n?: number) {
  return new Intl.NumberFormat("vi-VN").format(Number(n || 0)) + "đ";
}
function getStatusColor(s: string) {
  return (
    {
      DRAFT: "bg-slate-500/15 text-slate-300 border border-slate-400/30",
      OPEN: "bg-blue-500/15 text-blue-300 border border-blue-400/30",
      UNPAID: "bg-amber-500/15 text-amber-300 border border-amber-400/30",
      PAID: "bg-emerald-500/15 text-emerald-300 border border-emerald-400/30",
      CANCELLED: "bg-rose-500/15 text-rose-300 border border-rose-400/30",
    }[s] || "bg-white/10 text-white/70 border border-white/10"
  );
}
function statusLabel(s: string) {
  return (
    (
      {
        DRAFT: "Nháp",
        OPEN: "Đang mở",
        UNPAID: "Chưa thanh toán",
        PAID: "Đã thanh toán",
        CANCELLED: "Đã hủy",
      } as any
    )[s] || s
  );
}

// Counts by status (for chips)
const countsByStatus = computed(() => {
  const c: Record<string, number> = { ALL: orders.value.length };
  for (const s of ["DRAFT", "OPEN", "UNPAID", "PAID", "CANCELLED"]) {
    c[s] = orders.value.filter((o) => o.status === s).length;
  }
  return c;
});

// Filtered list
const filteredOrders = computed(() => {
  let data = [...orders.value];
  if (selectedStatus.value !== "ALL") data = data.filter((o) => o.status === selectedStatus.value);
  const kw = q.value.trim().toLowerCase();
  if (kw) {
    data = data.filter(
      (o) =>
        (o.code || o.id)?.toLowerCase?.().includes(kw) ||
        (o.note || "").toLowerCase?.().includes(kw) ||
        getTableName(o.tableId).toLowerCase().includes(kw)
    );
  }

  // Sort by type (DINE_IN first, then DELIVERY), then by status order
  const statusOrder: Record<string, number> = {
    OPEN: 1,
    UNPAID: 2,
    PAID: 3,
    CANCELLED: 4,
  };

  data.sort((a, b) => {
    // First by type: DINE_IN (or no type) before DELIVERY
    const typeA = a.type === "DELIVERY" ? 1 : 0;
    const typeB = b.type === "DELIVERY" ? 1 : 0;
    if (typeA !== typeB) return typeA - typeB;

    // Then by status
    const statusA = statusOrder[a.status] || 999;
    const statusB = statusOrder[b.status] || 999;
    if (statusA !== statusB) return statusA - statusB;

    // Finally by createdAt (newest first)
    return new Date(b.createdAt || b.id).getTime() - new Date(a.createdAt || a.id).getTime();
  });

  return data;
});

async function clearEmptyOrders() {
  loading.value = true;
  try {
    await clearOrder();
    toast.success("Đã xoá các đơn hàng trống");
    await loadData();
  } catch (e: any) {
    toast.error(e?.friendlyMessage || "Xoá đơn hàng trống thất bại");
  } finally {
    loading.value = false;
  }
}

async function loadData() {
  loading.value = true;
  try {
    const [ordersRes, tablesRes, areasRes, dishesRes] = await Promise.all([
      getAllOrders().catch(() => ({ data: [] })),
      listTables().catch(() => ({ data: [] })),
      listAreas().catch(() => ({ data: [] })),
      listDishes().catch(() => ({ data: [] })),
    ]);
    orders.value = (ordersRes.data || []).sort(
      (a: any, b: any) =>
        new Date(b.createdAt || b.id).getTime() - new Date(a.createdAt || a.id).getTime()
    );
    tables.value = tablesRes.data || [];
    areas.value = areasRes.data || [];

    // Handle pagination response for dishes
    const dishesData = dishesRes?.content || dishesRes?.data?.content || dishesRes?.data || [];
    dishes.value = Array.isArray(dishesData) ? dishesData : [];
  } catch (e: any) {
    toast.error(e?.friendlyMessage || "Không tải được dữ liệu");
  } finally {
    loading.value = false;
  }
}

async function createNewOrder() {
  if (!selectedAreaId.value && newOrderForm.value.tableId) {
    toast.error("Chọn khu vực trước");
    return;
  }
  if (selectedAreaId.value && !newOrderForm.value.tableId) {
    toast.error("Vui lòng chọn bàn trong khu vực");
    return;
  }
  submitting.value = true;
  try {
    const payload = {
      tableId: newOrderForm.value.tableId || undefined,
      reservationId: newOrderForm.value.reservationId || undefined,
      note: newOrderForm.value.note || "",
    };
    const { data } = await openOrderByStaff(payload as any);
    toast.success(`Đã mở order mới: ${data.code || data.id}`);
    // prefill
    addItemForm.value.orderId = data.id;
    paymentForm.value.orderId = data.id;
    // reset + đóng modal
    selectedAreaId.value = "";
    newOrderForm.value = { tableId: "", reservationId: "", note: "" };
    showOpenModal.value = false;
    await loadData();
  } catch (e: any) {
    toast.error(e?.friendlyMessage || "Mở order thất bại");
  } finally {
    submitting.value = false;
  }
}

async function addItemToOrder() {
  if (!addItemForm.value.orderId || !addItemForm.value.dishId) {
    toast.error("Vui lòng chọn order và món ăn");
    return;
  }
  try {
    await addOrderItem(addItemForm.value.orderId, {
      itemType: "DISH",
      itemId: addItemForm.value.dishId,
      quantity: addItemForm.value.quantity,
    });
    toast.success("Đã thêm món vào order");
    addItemForm.value = { orderId: addItemForm.value.orderId, dishId: "", quantity: 1 };
    await loadData();
  } catch (e: any) {
    toast.error(e?.friendlyMessage || "Thêm món thất bại");
  }
}

async function processPayment() {
  if (!paymentForm.value.orderId || paymentForm.value.amount <= 0) {
    toast.error("Vui lòng nhập thông tin thanh toán hợp lệ");
    return;
  }
  processingPayment.value = true;
  try {
    await payOrder(paymentForm.value.orderId, {
      method: paymentForm.value.method,
      amount: paymentForm.value.amount,
    });
    toast.success("Thanh toán thành công");
    showPayModal.value = false;
    paymentForm.value = { orderId: "", method: "CASH", amount: 0 };
    await loadData();
  } catch (e: any) {
    toast.error(e?.friendlyMessage || "Thanh toán thất bại");
  } finally {
    processingPayment.value = false;
  }
}

function selectOrderFor(action: "add" | "pay" | "status", order: any) {
  if (action === "add") {
    addItemForm.value.orderId = order.id;
    showAddItemModal.value = true;
  } else if (action === "pay") {
    paymentForm.value.orderId = order.id;
    paymentForm.value.amount = Number(order.total || 0);
    showPayModal.value = true;
  } else if (action === "status") {
    statusForm.value.orderId = order.id;
    statusForm.value.status = order.status;
    showStatusModal.value = true;
  }
}

async function updateOrderStatus() {
  if (!statusForm.value.orderId || !statusForm.value.status) {
    toast.error("Vui lòng chọn trạng thái");
    return;
  }
  try {
    // Call API to update order status
    await updateOrderStatusAPI(statusForm.value.orderId, { status: statusForm.value.status });
    toast.success("Cập nhật trạng thái thành công");
    showStatusModal.value = false;
    statusForm.value = { orderId: "", status: "" };
    await loadData();
  } catch (e: any) {
    toast.error(e?.friendlyMessage || "Cập nhật trạng thái thất bại");
  }
}

onMounted(loadData);
</script>

<template>
  <div class="space-y-6 text-white">
    <!-- Header -->
    <div
      class="relative overflow-hidden rounded-2xl p-6 shadow-xl bg-gradient-to-br from-[#111] via-[#0f1f1a] to-[#0b1512] border border-white/10"
    >
      <div
        class="absolute -top-16 -left-10 h-48 w-48 rounded-full bg-emerald-500/20 blur-3xl"
      ></div>
      <div
        class="absolute -bottom-16 -right-10 h-56 w-56 rounded-full bg-amber-500/20 blur-3xl"
      ></div>
      <div class="relative z-10 flex items-center justify-between gap-3">
        <div>
          <h1 class="text-2xl md:text-3xl font-extrabold tracking-tight">🧾 Quản lý Đơn Hàng</h1>
          <p class="mt-1 text-white/70">Mở order, thêm món, thanh toán — nhanh gọn</p>
        </div>
        <button
          class="rounded-xl px-4 py-2 bg-emerald-600 hover:bg-emerald-700 border border-emerald-400/30 transition"
          @click="showOpenModal = true"
        >
          ➕ Mở order
        </button>
      </div>
    </div>

    <!-- Filters -->
    <div class="rounded-2xl bg-white/5 backdrop-blur-md border border-white/10 shadow p-5">
      <div class="grid md:grid-cols-2 lg:grid-cols-4 gap-3">
        <div class="relative lg:col-span-2">
          <span class="absolute left-3 top-2.5 opacity-70">🔎</span>
          <input
            v-model="q"
            placeholder="Tìm theo mã/ghi chú/bàn…"
            class="w-full bg-white/5 border border-white/10 rounded-lg pl-9 pr-3 py-2 outline-none focus:border-emerald-400"
          />
        </div>
        <div class="lg:col-span-2 flex flex-wrap gap-2">
          <button class="px-3 py-1.5 rounded-lg border transition" @click="clearEmptyOrders">
            Xoá đơn hàng trống
          </button>
          <button
            v-for="opt in statusOptions"
            :key="opt.value"
            class="px-3 py-1.5 rounded-lg border transition"
            :class="
              selectedStatus === opt.value
                ? 'bg-emerald-600 border-emerald-400/40'
                : 'bg-white/5 border-white/10 hover:bg-white/10'
            "
            @click="selectedStatus = opt.value as any"
            :title="opt.label"
          >
            <span class="opacity-80 mr-1">{{ opt.emoji }}</span
            >{{ opt.label }}
            <span class="ml-2 text-xs opacity-70"
              >({{ (countsByStatus as any)[opt.value] || 0 }})</span
            >
          </button>
        </div>
      </div>
    </div>

    <!-- Orders List -->
    <div class="rounded-2xl bg-white/5 backdrop-blur-md border border-white/10 shadow p-6">
      <div class="flex items-center justify-between mb-4">
        <h3 class="font-semibold text-lg">Danh sách đơn hàng</h3>
        <div class="text-sm text-white/70">
          Tổng: <span class="text-white font-medium">{{ filteredOrders.length }}</span>
        </div>
      </div>

      <div v-if="loading" class="text-white/70 text-center py-10">Đang tải…</div>
      <div v-else-if="!filteredOrders.length" class="text-white/60 text-center py-10">
        {{
          selectedStatus === "ALL"
            ? "Chưa có đơn hàng nào"
            : "Không có đơn phù hợp với trạng thái này"
        }}
      </div>

      <div v-else class="space-y-4">
        <div
          v-for="order in filteredOrders"
          :key="order.id"
          class="rounded-xl border border-white/10 bg-white/5 hover:bg-white/10 transition p-4"
        >
          <div class="flex items-start justify-between gap-4">
            <div class="flex-1">
              <div class="flex flex-wrap items-center gap-3 mb-2">
                <div class="font-semibold text-lg">
                  Order #{{ order.code || order.id.slice(0, 8) }}
                </div>
                <span
                  class="px-2 py-1 rounded-lg text-xs font-medium"
                  :class="getStatusColor(order.status)"
                >
                  {{ statusLabel(order.status) }}
                </span>
                <span class="px-2 py-1 rounded-lg text-xs bg-white/10 border border-white/10">
                  🪑 {{ getTableName(order.tableId) }}
                </span>
                <span
                  v-if="order.customerName || order.user?.name"
                  class="px-2 py-1 rounded-lg text-xs bg-emerald-500/15 border border-emerald-400/30 text-emerald-300"
                >
                  👤 {{ order.customerName || order.user?.name }}
                </span>
              </div>

              <div class="grid sm:grid-cols-2 gap-3 text-sm">
                <div class="space-y-1">
                  <div class="text-white/80">
                    Tổng tiền:
                    <span class="font-semibold text-white">{{ vnd(order.total || 0) }}</span>
                  </div>
                  <div class="text-white/80">
                    Số món:
                    <span class="font-semibold text-white">{{ order.items?.length || 0 }}</span>
                  </div>
                </div>
                <div class="space-y-1">
                  <div class="text-white/80">
                    Ghi chú:
                    <span class="font-semibold text-white">{{ order.note || "Không có" }}</span>
                  </div>
                  <div class="text-white/60">
                    ID:
                    <span class="font-mono text-xs text-white/80">{{ order.id.slice(0, 8) }}…</span>
                  </div>
                </div>
              </div>

              <div
                v-if="order.items?.length"
                class="mt-3 rounded border border-white/10 bg-white/5 p-3"
              >
                <div class="text-sm font-medium mb-2">Món đã order</div>
                <div class="space-y-1 text-xs text-white/80">
                  <div v-for="it in order.items" :key="it.id">
                    {{ getDishName(it.itemId) }} × {{ it.quantity }} — {{ vnd(it.lineTotal || 0) }}
                  </div>
                </div>
              </div>
            </div>

            <!-- Quick Actions -->
            <div class="flex flex-col gap-2 shrink-0">
              <button
                class="px-3 py-1.5 rounded-lg bg-blue-600 hover:bg-blue-700 border border-blue-400/30"
                @click="selectOrderFor('status', order)"
              >
                📝 Sửa trạng thái
              </button>
              <button
                v-if="order.status === 'OPEN' || order.status === 'PREPARING'"
                class="px-3 py-1.5 rounded-lg bg-emerald-600 hover:bg-emerald-700 border border-emerald-400/30"
                @click="selectOrderFor('add', order)"
              >
                ➕ Thêm món
              </button>
              <button
                v-if="order.status === 'OPEN' || order.status === 'READY'"
                class="px-3 py-1.5 rounded-lg bg-purple-600 hover:bg-purple-700 border border-purple-400/30"
                @click="selectOrderFor('pay', order)"
              >
                💳 Thanh toán
              </button>
            </div>
          </div>
        </div>
      </div>
    </div>

    <!-- Open Order Modal -->
    <div v-if="showOpenModal" class="fixed inset-0 z-50 grid place-items-center">
      <div
        class="absolute inset-0 bg-black/60 backdrop-blur-sm"
        @click="showOpenModal = false"
      ></div>
      <div
        class="relative w-full max-w-lg mx-4 rounded-2xl border border-white/10 bg-white/5 text-white shadow-xl"
      >
        <div class="flex items-center justify-between p-5 border-b border-white/10">
          <h3 class="text-lg font-semibold">Mở order mới</h3>
          <button class="text-white/70 hover:text-white" @click="showOpenModal = false">✖</button>
        </div>

        <div class="p-5 space-y-4">
          <div class="text-xs text-white/70 -mt-2 mb-1">
            (Để tạo **Mang về**, bỏ trống khu vực & bàn)
          </div>

          <div class="space-y-1">
            <label class="block text-sm font-medium">1. Khu vực (tuỳ chọn)</label>
            <select
              class="w-full bg-white/5 border border-white/10 rounded-lg px-3 py-2 outline-none focus:border-emerald-400"
              v-model="selectedAreaId"
            >
              <option class="bg-[#0b1512]" value="">— Bỏ trống để tạo Mang về —</option>
              <option class="bg-[#0b1512]" v-for="a in areas" :key="a.id" :value="a.id">
                {{ a.name }}
              </option>
            </select>
          </div>

          <div class="space-y-1">
            <label class="block text-sm font-medium">2. Bàn (theo khu vực)</label>
            <select
              class="w-full bg-white/5 border border-white/10 rounded-lg px-3 py-2 outline-none focus:border-emerald-400"
              v-model="newOrderForm.tableId"
              :disabled="!selectedAreaId"
            >
              <option class="bg-[#0b1512]" value="">
                {{ !selectedAreaId ? "— Chọn khu vực trước —" : "— Chọn bàn —" }}
              </option>
              <option
                class="bg-[#0b1512]"
                v-for="t in availableTablesInArea"
                :key="t.id"
                :value="t.id"
              >
                {{ getTableName(t.id) }}
              </option>
            </select>
            <p
              v-if="selectedAreaId && !availableTablesInArea.length"
              class="text-xs text-amber-300"
            >
              Không có bàn trống trong khu vực này
            </p>
          </div>

          <div class="space-y-1">
            <label class="block text-sm font-medium">3. (Tuỳ chọn) Gắn đặt bàn</label>
            <input
              class="w-full bg-white/5 border border-white/10 rounded-lg px-3 py-2 outline-none focus:border-emerald-400"
              placeholder="Reservation ID"
              v-model="newOrderForm.reservationId"
            />
          </div>

          <div class="space-y-1">
            <label class="block text-sm font-medium">4. Ghi chú</label>
            <textarea
              class="w-full bg-white/5 border border-white/10 rounded-lg px-3 py-2 outline-none focus:border-emerald-400"
              placeholder="Ghi chú cho order"
              v-model="newOrderForm.note"
              rows="2"
            ></textarea>
          </div>

          <div class="pt-2">
            <button
              class="w-full px-4 py-2 rounded-lg bg-emerald-600 hover:bg-emerald-700 border border-emerald-400/30 disabled:opacity-50"
              @click="createNewOrder"
              :disabled="submitting"
            >
              <span v-if="submitting" class="inline-flex items-center gap-2">
                <span
                  class="w-4 h-4 inline-block rounded-full border-2 border-white border-t-transparent animate-spin"
                ></span>
                Đang tạo…
              </span>
              <span v-else>✅ Tạo order</span>
            </button>
          </div>
        </div>
      </div>
    </div>

    <!-- Add Item Modal -->
    <div v-if="showAddItemModal" class="fixed inset-0 z-50 grid place-items-center">
      <div
        class="absolute inset-0 bg-black/60 backdrop-blur-sm"
        @click="showAddItemModal = false"
      ></div>
      <div
        class="relative w-full max-w-md mx-4 rounded-2xl border border-white/10 bg-white/5 text-white shadow-xl"
      >
        <div class="flex items-center justify-between p-5 border-b border-white/10">
          <h3 class="text-lg font-semibold">➕ Thêm món vào order</h3>
          <button class="text-white/70 hover:text-white" @click="showAddItemModal = false">
            ✖
          </button>
        </div>

        <div class="p-5 space-y-4">
          <div>
            <label class="block text-sm mb-1">Order ID</label>
            <input
              class="w-full bg-white/5 border border-white/10 rounded-lg px-3 py-2"
              v-model="addItemForm.orderId"
            />
          </div>
          <div>
            <label class="block text-sm mb-1">Chọn món</label>
            <select
              class="w-full bg-white/5 border border-white/10 rounded-lg px-3 py-2"
              v-model="addItemForm.dishId"
            >
              <option class="bg-[#0b1512]" value="">— Chọn món —</option>
              <option class="bg-[#0b1512]" v-for="d in dishes" :key="d.id" :value="d.id">
                {{ d.name }} — {{ vnd(d.price) }}
              </option>
            </select>
          </div>
          <div>
            <label class="block text-sm mb-1">Số lượng</label>
            <input
              class="w-full bg-white/5 border border-white/10 rounded-lg px-3 py-2"
              type="number"
              min="1"
              v-model.number="addItemForm.quantity"
            />
          </div>

          <div class="pt-1">
            <button
              class="w-full px-4 py-2 rounded-lg bg-emerald-600 hover:bg-emerald-700 border border-emerald-400/30 disabled:opacity-50"
              :disabled="!addItemForm.orderId || !addItemForm.dishId"
              @click="addItemToOrder"
            >
              Thêm món
            </button>
          </div>
        </div>
      </div>
    </div>

    <!-- Pay Modal -->
    <div v-if="showPayModal" class="fixed inset-0 z-50 grid place-items-center">
      <div
        class="absolute inset-0 bg-black/60 backdrop-blur-sm"
        @click="showPayModal = false"
      ></div>
      <div
        class="relative w-full max-w-md mx-4 rounded-2xl border border-white/10 bg-white/5 text-white shadow-xl"
      >
        <div class="flex items-center justify-between p-5 border-b border-white/10">
          <h3 class="text-lg font-semibold">💳 Thanh toán</h3>
          <button class="text-white/70 hover:text-white" @click="showPayModal = false">✖</button>
        </div>

        <div class="p-5 space-y-4">
          <div>
            <label class="block text-sm mb-1">Order ID</label>
            <input
              class="w-full bg-white/5 border border-white/10 rounded-lg px-3 py-2"
              v-model="paymentForm.orderId"
            />
          </div>
          <div>
            <label class="block text-sm mb-1">Phương thức</label>
            <select
              class="w-full bg-white/5 border border-white/10 rounded-lg px-3 py-2"
              v-model="paymentForm.method"
            >
              <option class="bg-[#0b1512]" value="CASH">Tiền mặt</option>
              <option class="bg-[#0b1512]" value="BANK">Chuyển khoản</option>
            </select>
          </div>
          <div>
            <label class="block text-sm mb-1">Số tiền</label>
            <input
              class="w-full bg-white/5 border border-white/10 rounded-lg px-3 py-2"
              type="number"
              min="0"
              step="1000"
              v-model.number="paymentForm.amount"
            />
          </div>

          <div class="pt-1">
            <button
              class="w-full px-4 py-2 rounded-lg bg-purple-600 hover:bg-purple-700 border border-purple-400/30 disabled:opacity-50 disabled:cursor-not-allowed flex items-center justify-center gap-2"
              :disabled="!paymentForm.orderId || paymentForm.amount <= 0 || processingPayment"
              @click="processPayment"
            >
              <template v-if="processingPayment">
                <svg class="animate-spin w-5 h-5" fill="none" viewBox="0 0 24 24">
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
                Đang xử lý...
              </template>
              <template v-else> Xác nhận thanh toán </template>
            </button>
          </div>
        </div>
      </div>
    </div>

    <!-- Update Status Modal -->
    <div v-if="showStatusModal" class="fixed inset-0 z-50 grid place-items-center">
      <div
        class="absolute inset-0 bg-black/60 backdrop-blur-sm"
        @click="showStatusModal = false"
      ></div>
      <div
        class="relative w-full max-w-md mx-4 rounded-2xl border border-white/10 bg-white/5 text-white shadow-xl"
      >
        <div class="flex items-center justify-between p-5 border-b border-white/10">
          <h3 class="text-lg font-semibold">📝 Cập nhật trạng thái</h3>
          <button class="text-white/70 hover:text-white" @click="showStatusModal = false">✖</button>
        </div>

        <div class="p-5 space-y-4">
          <div>
            <label class="block text-sm mb-1">Order ID</label>
            <input
              class="w-full bg-white/5 border border-white/10 rounded-lg px-3 py-2"
              v-model="statusForm.orderId"
              readonly
            />
          </div>
          <div>
            <label class="block text-sm mb-1">Trạng thái mới</label>
            <select
              class="w-full bg-white/5 border border-white/10 rounded-lg px-3 py-2"
              v-model="statusForm.status"
            >
              <option class="bg-[#0b1512]" value="">— Chọn trạng thái —</option>
              <option
                class="bg-[#0b1512]"
                v-for="opt in statusUpdateOptions"
                :key="opt.value"
                :value="opt.value"
              >
                {{ opt.label }}
              </option>
            </select>
          </div>

          <div class="pt-1">
            <button
              class="w-full px-4 py-2 rounded-lg bg-blue-600 hover:bg-blue-700 border border-blue-400/30 disabled:opacity-50"
              :disabled="!statusForm.orderId || !statusForm.status"
              @click="updateOrderStatus"
            >
              Cập nhật trạng thái
            </button>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>
