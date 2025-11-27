<script setup lang="ts">
import { onMounted, ref, computed } from "vue";
import { useRoute } from "vue-router";
import {
  listAllReservations,
  assignTables,
  adminCancelReservation,
} from "@/api/reservations.service";
import { listTables, getAvailableTables, getAvailableTablesByArea } from "@/api/tables.admin";
import { listAreas } from "@/api/areas.admin";
import { toast } from "vue3-toastify";

type Resv = any;
type Table = {
  id: string;
  code?: string;
  name?: string;
  areaId?: string;
  seats?: number;
  capacity?: number;
  status?: string;
};
type Area = { id: string; name: string };

const route = useRoute();
const reservations = ref<Resv[]>([]);
const tables = ref<Table[]>([]);
const areas = ref<Area[]>([]);
const loading = ref(false);

// Filters
const selectedStatus = ref("PENDING");
const q = ref("");
const fromDate = ref<string>(""); // yyyy-MM-dd
const toDate = ref<string>(""); // yyyy-MM-dd

// Modal state
const showAssignModal = ref(false);
const selectedReservation = ref<Resv | null>(null);
const selectedTableIds = ref<string[]>([]);
const selectedAreaFilter = ref<string>(""); // Filter by area in modal
const loadingAvailableTables = ref(false);
const availableTablesInTimeRange = ref<Table[]>([]);
const assigningTables = ref(false);

// Status options
const statusOptions = [
  { value: "ALL", label: "Tất cả" },
  { value: "PENDING", label: "Chờ xử lý" },
  { value: "CONFIRMED", label: "Đã xác nhận" },
  { value: "SEATED", label: "Đã vào bàn" },
  { value: "CANCELLED", label: "Đã hủy" },
  { value: "COMPLETED", label: "Hoàn thành" },
];

// ==== Helpers ====
function normStartEnd(r: Resv) {
  // Nếu backend không trả endTime, giả định thời lượng 2 giờ
  const startIso: string | null = r.startTime || (r.date && r.time ? `${r.date}T${r.time}` : null);
  const endIso: string | null =
    r.endTime ||
    (startIso ? new Date(new Date(startIso).getTime() + 2 * 60 * 60 * 1000).toISOString() : null);
  return { startIso, endIso };
}
function overlaps(
  aStart?: string | null,
  aEnd?: string | null,
  bStart?: string | null,
  bEnd?: string | null
) {
  if (!aStart || !aEnd || !bStart || !bEnd) return false;
  const aS = new Date(aStart).getTime();
  const aE = new Date(aEnd).getTime();
  const bS = new Date(bStart).getTime();
  const bE = new Date(bEnd).getTime();
  return aS < bE && bS < aE; // giao nhau
}
function areaName(areaId?: string) {
  if (!areaId) return "N/A";
  return areas.value.find((a) => a.id === areaId)?.name || areaId;
}
function getTableName(tableId: string) {
  const t = tables.value.find((x) => x.id === tableId);
  if (!t) return `Table ${tableId.slice(0, 8)}…`;
  return `${t.code || t.name || t.id.slice(0, 8)} (${areaName(t.areaId)})`;
}
function tableSeats(tableId: string) {
  const t = tables.value.find((x) => x.id === tableId);
  return Number(t?.seats ?? t?.capacity ?? 0);
}
function getTableNames(r: Resv) {
  if (r.tables && Array.isArray(r.tables) && r.tables.length > 0) {
    return r.tables.map((t: any) => t.code || t.name || t.id.slice(0, 8)).join(", ");
  }
  return "Chưa gán";
}
function getStatusColor(status: string) {
  const classes: Record<string, string> = {
    PENDING: "bg-amber-500/15 text-amber-300 border border-amber-400/30",
    CONFIRMED: "bg-blue-500/15 text-blue-300 border border-blue-400/30",
    SEATED: "bg-emerald-500/15 text-emerald-300 border border-emerald-400/30",
    CANCELLED: "bg-rose-500/15 text-rose-300 border border-rose-400/30",
    COMPLETED: "bg-slate-500/15 text-slate-300 border border-slate-400/30",
  };
  return classes[status] || "bg-white/10 text-white/70 border border-white/10";
}
function getStatusLabel(status: string) {
  const labels: Record<string, string> = {
    PENDING: "Chờ xử lý",
    CONFIRMED: "Đã xác nhận",
    SEATED: "Đã vào bàn",
    CANCELLED: "Đã hủy",
    COMPLETED: "Hoàn thành",
  };
  return labels[status] || status;
}

// ==== Counters (chips) ====
const statusCounts = computed(() => {
  const c = { PENDING: 0, CONFIRMED: 0, SEATED: 0, COMPLETED: 0, CANCELLED: 0 };
  for (const r of reservations.value) {
    // @ts-ignore
    if (c[r.status] !== undefined) (c as any)[r.status] += 1;
  }
  return c;
});

// ==== Filters & list ====
const filteredReservations = computed(() => {
  let data = [...reservations.value];

  if (selectedStatus.value !== "ALL") {
    data = data.filter((r) => r.status === selectedStatus.value);
  }
  const kw = q.value.trim().toLowerCase();
  if (kw) {
    data = data.filter(
      (r) =>
        r.customerName?.toLowerCase?.().includes(kw) ||
        r.note?.toLowerCase?.().includes(kw) ||
        r.id?.toLowerCase?.().includes(kw)
    );
  }
  // Filter by date range (compare bằng ngày của startTime)
  if (fromDate.value) {
    const fromTs = new Date(fromDate.value + "T00:00:00").getTime();
    data = data.filter((r) => {
      const { startIso } = normStartEnd(r);
      return startIso ? new Date(startIso).getTime() >= fromTs : true;
    });
  }
  if (toDate.value) {
    const toTs = new Date(toDate.value + "T23:59:59").getTime();
    data = data.filter((r) => {
      const { startIso } = normStartEnd(r);
      return startIso ? new Date(startIso).getTime() <= toTs : true;
    });
  }

  return data;
});

// Filter available tables by selected area
const filteredAvailableTables = computed(() => {
  if (!selectedAreaFilter.value) {
    return availableTablesInTimeRange.value;
  }
  return availableTablesInTimeRange.value.filter((t) => t.areaId === selectedAreaFilter.value);
});

const availableTables = computed(() => tables.value.filter((t) => t.status === "AVAILABLE"));

// Bàn có xung đột thời gian với selectedReservation?
function isTableConflicting(tableId: string) {
  const sel = selectedReservation.value;
  if (!sel) return false;
  const { startIso: sS, endIso: sE } = normStartEnd(sel);

  // Xem các đặt bàn khác đã gán tableId, có trạng thái ảnh hưởng & giao thời gian
  const busyStatuses = new Set(["PENDING", "CONFIRMED", "SEATED"]);
  for (const r of reservations.value) {
    if (r.id === sel.id) continue;
    if (!busyStatuses.has(r.status)) continue;
    const hasTable =
      Array.isArray(r.tables) &&
      r.tables.some((t: any) => (t.id || t.tableId) === tableId || t === tableId);
    if (!hasTable) continue;

    const { startIso, endIso } = normStartEnd(r);
    if (overlaps(sS, sE, startIso, endIso)) return true;
  }
  return false;
}

async function load() {
  loading.value = true;
  try {
    const [reservationsRes, tablesRes, areasRes] = await Promise.all([
      listAllReservations(),
      listTables().catch(() => ({ data: [] })),
      listAreas().catch(() => ({ data: [] })),
    ]);

    reservations.value = (reservationsRes.data || [])
      .map((r: any) => {
        const { startIso, endIso } = normStartEnd(r);
        let startDisplay = "";
        let dateDisplay = "";
        let timeDisplay = "";
        if (startIso) {
          const d = new Date(startIso);
          dateDisplay = d.toLocaleDateString();
          timeDisplay = d.toLocaleTimeString([], { hour: "2-digit", minute: "2-digit" });
          startDisplay = `${dateDisplay} - ${timeDisplay}`;
        }
        return {
          ...r,
          startTime: startIso,
          endTime: endIso,
          partySize: r.partySize ?? r.guests,
          _dateDisplay: dateDisplay,
          _timeDisplay: timeDisplay,
          _startDisplay: startDisplay,
        };
      })
      .sort((a: any, b: any) => {
        const pri: Record<string, number> = {
          PENDING: 1,
          CONFIRMED: 2,
          SEATED: 3,
          COMPLETED: 4,
          CANCELLED: 5,
        };
        const ap = pri[a.status] ?? 6;
        const bp = pri[b.status] ?? 6;
        if (ap !== bp) return ap - bp;
        return (b.startTime || "").localeCompare(a.startTime || "");
      });

    tables.value = tablesRes.data || [];
    areas.value = areasRes.data || [];

    // highlight nếu có ?highlight=
    const highlightId = route.query.highlight as string | undefined;
    if (highlightId) {
      const el = document.getElementById(`reservation-${highlightId}`);
      if (el) {
        setTimeout(() => {
          el.scrollIntoView({ behavior: "smooth", block: "center" });
          el.classList.add("ring-2", "ring-emerald-400");
          setTimeout(() => el.classList.remove("ring-2", "ring-emerald-400"), 2000);
        }, 120);
      }
    }
  } catch (e: any) {
    toast.error(e?.friendlyMessage || "Không tải được đặt bàn");
  } finally {
    loading.value = false;
  }
}

async function cancelReservation(reservationId: string) {
  try {
    await adminCancelReservation(reservationId, "Cancelled by admin");
    toast.success("Đã hủy đặt bàn");
    await load();
  } catch (e: any) {
    toast.error(e?.friendlyMessage || "Lỗi hủy đặt bàn");
  }
}

async function loadAvailableTablesForReservation(r: Resv) {
  const { startIso, endIso } = normStartEnd(r);
  if (!startIso || !endIso) {
    toast.error("Không xác định được thời gian đặt bàn");
    return;
  }

  loadingAvailableTables.value = true;
  try {
    const response = await getAvailableTables(startIso, endIso);
    availableTablesInTimeRange.value = response.data || [];

    if (availableTablesInTimeRange.value.length === 0) {
      toast.warning("Không có bàn trống trong khung giờ này");
    } else {
      toast.success(`Tìm thấy ${availableTablesInTimeRange.value.length} bàn trống`);
    }
  } catch (e: any) {
    toast.error("Không thể tải danh sách bàn trống");
    availableTablesInTimeRange.value = [];
  } finally {
    loadingAvailableTables.value = false;
  }
}

async function filterTablesByArea(areaId: string) {
  if (!selectedReservation.value) return;

  const { startIso, endIso } = normStartEnd(selectedReservation.value);
  if (!startIso || !endIso || !areaId) return;

  loadingAvailableTables.value = true;
  try {
    const response = await getAvailableTablesByArea(areaId, startIso, endIso);
    availableTablesInTimeRange.value = response.data || [];
  } catch (e: any) {
    toast.error("Không thể lọc bàn theo khu vực");
  } finally {
    loadingAvailableTables.value = false;
  }
}

function onAreaFilterChange() {
  if (!selectedAreaFilter.value) {
    // Reset to all available tables
    if (selectedReservation.value) {
      loadAvailableTablesForReservation(selectedReservation.value);
    }
  } else {
    // Filter by area
    filterTablesByArea(selectedAreaFilter.value);
  }
}

function openAssignModal(r: Resv) {
  selectedReservation.value = r;
  selectedTableIds.value = [];
  selectedAreaFilter.value = "";
  availableTablesInTimeRange.value = [];
  showAssignModal.value = true;

  // Load available tables for this time range
  loadAvailableTablesForReservation(r);
}

function totalSelectedSeats() {
  return selectedTableIds.value.reduce((sum, id) => sum + tableSeats(id), 0);
}

async function saveAssignedTables() {
  if (!selectedReservation.value) return;

  if (!selectedTableIds.value.length) {
    toast.error("Chọn ít nhất một bàn");
    return;
  }

  // Check đủ ghế (không cần check trùng giờ vì BE đã filter)
  const needSeats = Number(selectedReservation.value.partySize || 0);
  const haveSeats = totalSelectedSeats();
  if (needSeats > 0 && haveSeats < needSeats) {
    toast.error(`Tổng số ghế ${haveSeats} chưa đủ cho ${needSeats} khách`);
    return;
  }

  assigningTables.value = true;
  try {
    await assignTables(selectedReservation.value.id, selectedTableIds.value);
    toast.success("Đã gán bàn & xác nhận");
    showAssignModal.value = false;
    await load();
  } catch (e: any) {
    toast.error(e?.friendlyMessage || "Lỗi gán bàn");
  } finally {
    assigningTables.value = false;
  }
}

onMounted(() => {
  // Set ngày hiện tại và 3 ngày sau
  const today = new Date();
  const after3Days = new Date();
  after3Days.setDate(today.getDate() + 3);

  fromDate.value = today.toISOString().split("T")[0]; // yyyy-MM-dd
  toDate.value = after3Days.toISOString().split("T")[0]; // yyyy-MM-dd

  load();
});
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
      <div class="relative z-10">
        <h1 class="text-2xl md:text-3xl font-extrabold tracking-tight">📅 Quản lý Đặt Bàn</h1>
        <p class="mt-1 text-white/70">Xem, lọc, gán bàn — kèm kiểm tra trùng giờ & đủ ghế</p>
      </div>
    </div>

    <!-- Chips counters -->
    <div class="grid grid-cols-2 sm:grid-cols-3 lg:grid-cols-5 gap-3">
      <div class="rounded-xl bg-white/5 border border-white/10 p-4">
        <div class="text-xs text-white/60">Chờ xử lý</div>
        <div class="text-2xl font-extrabold">{{ statusCounts.PENDING }}</div>
      </div>
      <div class="rounded-xl bg-white/5 border border-white/10 p-4">
        <div class="text-xs text-white/60">Đã xác nhận</div>
        <div class="text-2xl font-extrabold">{{ statusCounts.CONFIRMED }}</div>
      </div>
      <div class="rounded-xl bg-white/5 border border-white/10 p-4">
        <div class="text-xs text-white/60">Đã vào bàn</div>
        <div class="text-2xl font-extrabold">{{ statusCounts.SEATED }}</div>
      </div>
      <div class="rounded-xl bg-white/5 border border-white/10 p-4">
        <div class="text-xs text-white/60">Hoàn thành</div>
        <div class="text-2xl font-extrabold">{{ statusCounts.COMPLETED }}</div>
      </div>
      <div class="rounded-xl bg-white/5 border border-white/10 p-4">
        <div class="text-xs text-white/60">Đã hủy</div>
        <div class="text-2xl font-extrabold">{{ statusCounts.CANCELLED }}</div>
      </div>
    </div>

    <!-- Filters -->
    <div class="rounded-2xl bg-white/5 backdrop-blur-md border border-white/10 shadow p-5">
      <div class="grid md:grid-cols-2 lg:grid-cols-6 gap-3">
        <div class="relative lg:col-span-2">
          <span class="absolute left-3 top-2.5 opacity-70">🔎</span>
          <input
            v-model="q"
            placeholder="Tìm theo tên/ghi chú/mã đặt bàn…"
            class="w-full bg-white/5 border border-white/10 rounded-lg pl-9 pr-3 py-2 outline-none focus:border-emerald-400"
          />
        </div>
        <div class="relative">
          <span class="absolute left-3 top-2.5 opacity-70">🔖</span>
          <select
            v-model="selectedStatus"
            class="w-full bg-white/5 border border-white/10 rounded-lg pl-9 pr-3 py-2 outline-none focus:border-emerald-400"
          >
            <option
              class="bg-[#0b1512]"
              v-for="opt in statusOptions"
              :key="opt.value"
              :value="opt.value"
            >
              {{ opt.label }}
            </option>
          </select>
        </div>
        <div class="relative">
          <span class="absolute left-3 top-2.5 opacity-70">📅</span>
          <input
            v-model="fromDate"
            type="date"
            class="w-full bg-white/5 border border-white/10 rounded-lg pl-9 pr-3 py-2 outline-none focus:border-emerald-400"
          />
        </div>
        <div class="relative">
          <span class="absolute left-3 top-2.5 opacity-70">📅</span>
          <input
            v-model="toDate"
            type="date"
            class="w-full bg-white/5 border border-white/10 rounded-lg pl-9 pr-3 py-2 outline-none focus:border-emerald-400"
          />
        </div>
        <div class="flex items-center text-sm text-white/70">
          Hiển thị
          <span class="mx-1 font-semibold text-white">{{ filteredReservations.length }}</span
          >/
          <span class="mx-1 font-semibold text-white">{{ reservations.length }}</span>
          đặt bàn
        </div>
      </div>
    </div>

    <!-- Reservations List -->
    <div class="rounded-2xl bg-white/5 backdrop-blur-md border border-white/10 shadow p-6">
      <h3 class="font-semibold text-lg mb-4">Danh sách đặt bàn</h3>

      <div v-if="loading" class="text-white/70 text-center py-10">Đang tải…</div>

      <div v-else-if="!filteredReservations.length" class="text-white/60 text-center py-10">
        {{
          selectedStatus === "ALL"
            ? "Chưa có đặt bàn nào"
            : "Không có đặt bàn nào với trạng thái này"
        }}
      </div>

      <div v-else class="space-y-4">
        <div
          v-for="r in filteredReservations"
          :key="r.id"
          :id="`reservation-${r.id}`"
          class="rounded-xl border border-white/10 bg-white/5 hover:bg-white/10 transition p-4"
        >
          <div class="flex items-start justify-between gap-4">
            <div class="flex-1">
              <div class="flex flex-wrap items-center gap-3 mb-2">
                <div class="font-semibold text-lg">{{ r._startDisplay || "—" }}</div>
                <span
                  :class="getStatusColor(r.status)"
                  class="px-2 py-1 rounded-lg text-xs font-medium"
                >
                  {{ getStatusLabel(r.status) }}
                </span>
                <span
                  v-if="r.customerName || r.user?.name"
                  class="px-2 py-1 rounded-lg text-xs bg-emerald-500/15 border border-emerald-400/30 text-emerald-300"
                >
                  👤 {{ r.customerName || r.user?.name }}
                </span>
              </div>

              <div class="grid sm:grid-cols-2 gap-3 text-sm">
                <div class="space-y-1">
                  <div class="text-white/80">
                    Số khách: <span class="font-medium text-white">{{ r.partySize }}</span>
                  </div>
                  <div class="text-white/80 break-words">
                    Ghi chú: <span class="font-medium text-white">{{ r.note || "Không có" }}</span>
                  </div>
                </div>
                <div class="space-y-1">
                  <div class="text-white/80">
                    Bàn: <span class="font-medium text-white">{{ getTableNames(r) }}</span>
                  </div>
                  <div class="text-white/60">
                    ID:
                    <span class="font-mono text-xs text-white/80">{{ r.id?.slice(0, 8) }}…</span>
                  </div>
                </div>
              </div>
            </div>

            <!-- Actions -->
            <div class="flex flex-col gap-2 shrink-0">
              <template v-if="r.status === 'PENDING'">
                <button
                  class="px-3 py-1.5 rounded-lg bg-emerald-600 hover:bg-emerald-700 border border-emerald-400/30"
                  @click="openAssignModal(r)"
                >
                  🪑 Gán bàn
                </button>
                <button
                  class="px-3 py-1.5 rounded-lg bg-rose-600 hover:bg-rose-700 border border-rose-400/30"
                  @click="cancelReservation(r.id)"
                >
                  ❌ Hủy
                </button>
              </template>

              <template v-else-if="r.status === 'CONFIRMED'">
                <button
                  class="px-3 py-1.5 rounded-lg bg-blue-600 hover:bg-blue-700 border border-blue-400/30"
                  @click="openAssignModal(r)"
                >
                  🔁 Đổi bàn
                </button>
                <div class="text-xs text-emerald-300 font-medium text-center">✓ Đã xếp bàn</div>
              </template>

              <template v-else-if="r.status === 'SEATED'">
                <div class="text-xs text-blue-300 font-medium text-center">👥 Khách đã vào bàn</div>
              </template>

              <template v-else-if="r.status === 'COMPLETED'">
                <div class="text-xs text-slate-300 font-medium text-center">✅ Hoàn thành</div>
              </template>

              <template v-else-if="r.status === 'CANCELLED'">
                <div class="text-xs text-rose-300 font-medium text-center">⛔ Đã hủy</div>
              </template>
            </div>
          </div>
        </div>
      </div>
    </div>

    <!-- Assign Table Modal -->
    <div
      v-if="showAssignModal"
      class="fixed inset-0 z-50 grid place-items-center overflow-y-auto py-8"
    >
      <div
        class="absolute inset-0 bg-black/60 backdrop-blur-sm"
        @click="showAssignModal = false"
      ></div>

      <div
        class="relative w-full max-w-3xl mx-4 rounded-2xl border border-white/10 bg-gradient-to-br from-[#111] via-[#0f1f1a] to-[#0b1512] text-white shadow-2xl"
      >
        <!-- Header -->
        <div class="p-6 border-b border-white/10">
          <div>
            <h3 class="text-xl font-bold flex items-center gap-2">
              <span class="text-2xl">🪑</span> Gán bàn cho đặt bàn
            </h3>
            <p class="text-sm text-white/60 mt-1">Chọn bàn trống trong khung giờ</p>
          </div>
        </div>

        <div class="p-6 space-y-5">
          <!-- Reservation Info Card -->
          <div
            v-if="selectedReservation"
            class="rounded-xl bg-gradient-to-r from-emerald-500/15 to-blue-500/15 border border-emerald-400/30 p-4"
          >
            <div class="flex items-center justify-between mb-3">
              <div class="flex items-center gap-3">
                <div
                  class="w-12 h-12 rounded-xl bg-emerald-500/20 flex items-center justify-center"
                >
                  <span class="text-2xl">📅</span>
                </div>
                <div>
                  <div class="font-bold text-lg">{{ selectedReservation._startDisplay }}</div>
                  <div class="text-sm text-white/70">{{ selectedReservation._timeDisplay }}</div>
                </div>
              </div>
              <div class="text-right">
                <div class="text-2xl font-bold text-emerald-300">
                  {{ selectedReservation.partySize }}
                </div>
                <div class="text-xs text-white/60">khách</div>
              </div>
            </div>
            <div
              v-if="selectedReservation.customerName"
              class="text-sm text-white/80 flex items-center gap-2"
            >
              <svg class="w-4 h-4" fill="currentColor" viewBox="0 0 20 20">
                <path
                  fill-rule="evenodd"
                  d="M10 9a3 3 0 100-6 3 3 0 000 6zm-7 9a7 7 0 1114 0H3z"
                  clip-rule="evenodd"
                ></path>
              </svg>
              <span>{{ selectedReservation.customerName }}</span>
            </div>
          </div>

          <!-- Area Filter -->
          <div class="space-y-2">
            <label class="font-semibold text-sm flex items-center gap-2">
              <svg class="w-4 h-4" fill="currentColor" viewBox="0 0 20 20">
                <path
                  fill-rule="evenodd"
                  d="M5.05 4.05a7 7 0 119.9 9.9L10 18.9l-4.95-4.95a7 7 0 010-9.9zM10 11a2 2 0 100-4 2 2 0 000 4z"
                  clip-rule="evenodd"
                ></path>
              </svg>
              Lọc theo khu vực:
            </label>
            <select
              v-model="selectedAreaFilter"
              @change="onAreaFilterChange"
              class="w-full bg-white/5 border border-white/10 rounded-xl px-4 py-3 outline-none focus:border-emerald-400 transition-colors"
            >
              <option value="" class="bg-[#0b1512]">🌐 Tất cả khu vực</option>
              <option v-for="area in areas" :key="area.id" :value="area.id" class="bg-[#0b1512]">
                📍 {{ area.name }}
              </option>
            </select>
          </div>

          <!-- Available Tables Summary -->
          <div
            class="flex items-center justify-between p-3 rounded-lg bg-white/5 border border-white/10"
          >
            <div class="flex items-center gap-2 text-sm">
              <svg class="w-5 h-5 text-emerald-400" fill="currentColor" viewBox="0 0 20 20">
                <path
                  fill-rule="evenodd"
                  d="M10 18a8 8 0 100-16 8 8 0 000 16zm3.707-9.293a1 1 0 00-1.414-1.414L9 10.586 7.707 9.293a1 1 0 00-1.414 1.414l2 2a1 1 0 001.414 0l4-4z"
                  clip-rule="evenodd"
                ></path>
              </svg>
              <span class="text-white/80">Bàn trống:</span>
              <span class="font-bold text-emerald-300">{{ filteredAvailableTables.length }}</span>
            </div>
            <div
              v-if="loadingAvailableTables"
              class="flex items-center gap-2 text-sm text-white/60"
            >
              <svg class="animate-spin w-4 h-4" fill="none" viewBox="0 0 24 24">
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
              Đang tải...
            </div>
          </div>

          <!-- Tables Selection -->
          <div class="space-y-2">
            <label class="font-semibold text-sm flex items-center justify-between">
              <span class="flex items-center gap-2">
                <svg class="w-4 h-4" fill="currentColor" viewBox="0 0 20 20">
                  <path
                    d="M10.394 2.08a1 1 0 00-.788 0l-7 3a1 1 0 000 1.84L5.25 8.051a.999.999 0 01.356-.257l4-1.714a1 1 0 11.788 1.838L7.667 9.088l1.94.831a1 1 0 00.787 0l7-3a1 1 0 000-1.838l-7-3zM3.31 9.397L5 10.12v4.102a8.969 8.969 0 00-1.05-.174 1 1 0 01-.89-.89 11.115 11.115 0 01.25-3.762zM9.3 16.573A9.026 9.026 0 007 14.935v-3.957l1.818.78a3 3 0 002.364 0l5.508-2.361a11.026 11.026 0 01.25 3.762 1 1 0 01-.89.89 8.968 8.968 0 00-5.35 2.524 1 1 0 01-1.4 0zM6 18a1 1 0 001-1v-2.065a8.935 8.935 0 00-2-.712V17a1 1 0 001 1z"
                  ></path>
                </svg>
                Chọn bàn (có thể chọn nhiều):
              </span>
              <span class="text-xs text-white/60">{{ selectedTableIds.length }} bàn đã chọn</span>
            </label>

            <!-- Loading State -->
            <div v-if="loadingAvailableTables" class="text-center py-8 text-white/60">
              <svg class="animate-spin w-8 h-8 mx-auto mb-2" fill="none" viewBox="0 0 24 24">
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
              <p>Đang tải bàn trống...</p>
            </div>

            <!-- No Tables Available -->
            <div v-else-if="!filteredAvailableTables.length" class="text-center py-8">
              <div class="text-4xl mb-2">🚫</div>
              <div class="text-amber-300 font-medium">Không có bàn trống</div>
              <div class="text-sm text-white/60 mt-1">
                Vui lòng chọn khung giờ khác hoặc khu vực khác
              </div>
            </div>

            <!-- Tables Grid -->
            <div
              v-else
              class="max-h-80 overflow-auto rounded-xl border border-white/10 bg-white/5 p-3"
            >
              <div class="grid sm:grid-cols-2 gap-2">
                <label
                  v-for="t in filteredAvailableTables"
                  :key="t.id"
                  class="relative flex items-center gap-3 p-3 rounded-lg border border-white/10 bg-white/5 hover:bg-white/10 transition-all cursor-pointer"
                  :class="
                    selectedTableIds.includes(t.id)
                      ? 'ring-2 ring-emerald-400 bg-emerald-500/10'
                      : ''
                  "
                >
                  <input
                    type="checkbox"
                    :value="t.id"
                    v-model="selectedTableIds"
                    class="rounded border-white/20 bg-transparent text-emerald-500 focus:ring-emerald-400 w-5 h-5"
                  />
                  <div class="flex-1">
                    <div class="font-medium flex items-center gap-2">
                      <span>{{ t.code || t.name || t.id.slice(0, 8) }}</span>
                      <span v-if="selectedTableIds.includes(t.id)" class="text-emerald-400">✓</span>
                    </div>
                    <div class="text-xs text-white/60 flex items-center gap-2 mt-1">
                      <span>📍 {{ areaName(t.areaId) }}</span>
                      <span>•</span>
                      <span>🪑 {{ t.seats ?? t.capacity ?? "-" }} chỗ</span>
                    </div>
                  </div>
                </label>
              </div>
            </div>
          </div>

          <!-- Selected Summary -->
          <div
            class="rounded-xl bg-gradient-to-r from-blue-500/15 to-purple-500/15 border border-blue-400/30 p-4"
          >
            <div class="grid grid-cols-3 gap-4 text-center">
              <div>
                <div class="text-2xl font-bold text-blue-300">{{ selectedTableIds.length }}</div>
                <div class="text-xs text-white/60 mt-1">Bàn đã chọn</div>
              </div>
              <div>
                <div class="text-2xl font-bold text-purple-300">{{ totalSelectedSeats() }}</div>
                <div class="text-xs text-white/60 mt-1">Tổng số ghế</div>
              </div>
              <div>
                <div
                  class="text-2xl font-bold"
                  :class="
                    totalSelectedSeats() >= (selectedReservation?.partySize || 0)
                      ? 'text-emerald-300'
                      : 'text-amber-300'
                  "
                >
                  {{ selectedReservation?.partySize || 0 }}
                </div>
                <div class="text-xs text-white/60 mt-1">Cần có</div>
              </div>
            </div>
            <div
              v-if="selectedTableIds.length > 0"
              class="mt-3 pt-3 border-t border-white/10 text-center text-sm"
              :class="
                totalSelectedSeats() >= (selectedReservation?.partySize || 0)
                  ? 'text-emerald-300'
                  : 'text-amber-300'
              "
            >
              <span v-if="totalSelectedSeats() >= (selectedReservation?.partySize || 0)">
                ✓ Đủ chỗ ngồi
              </span>
              <span v-else>
                ⚠️ Cần thêm {{ (selectedReservation?.partySize || 0) - totalSelectedSeats() }} ghế
                nữa
              </span>
            </div>
          </div>

          <!-- Actions -->
          <div class="flex gap-3 pt-2">
            <button
              class="flex-1 px-6 py-3 rounded-xl font-semibold bg-emerald-600 hover:bg-emerald-700 border border-emerald-400/30 disabled:opacity-50 disabled:cursor-not-allowed transition-all flex items-center justify-center gap-2"
              @click="saveAssignedTables"
              :disabled="!selectedTableIds.length || loadingAvailableTables || assigningTables"
            >
              <template v-if="assigningTables">
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
              <template v-else>
                <svg class="w-5 h-5" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                  <path
                    stroke-linecap="round"
                    stroke-linejoin="round"
                    stroke-width="2"
                    d="M5 13l4 4L19 7"
                  ></path>
                </svg>
                Xác nhận gán bàn
              </template>
            </button>
            <button
              class="px-6 py-3 rounded-xl font-semibold bg-white/10 hover:bg-white/15 border border-white/10 transition-all disabled:opacity-50 disabled:cursor-not-allowed"
              @click="showAssignModal = false"
              :disabled="assigningTables"
            >
              Hủy
            </button>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>
