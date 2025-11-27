<script setup lang="ts">
import { onMounted, onUnmounted, ref, computed } from "vue";
import { listUsers } from "@/api/users.admin";
import { listDishesAdmin } from "@/api/dishes.admin";
import { listCategories } from "@/api/categories.admin";
import { listAreas } from "@/api/areas.admin";
import { listTables } from "@/api/tables.admin";
import http from "@/api/http";
import { toast } from "vue3-toastify";
import AdminPageHeader from "@/components/AdminPageHeader.vue";
import AdminLoadingSkeleton from "@/components/AdminLoadingSkeleton.vue";

const stats = ref({
  users: 0,
  dishes: 0,
  categories: 0,
  areas: 0,
  tables: 0,
});

const serverHealth = ref<any>(null);
const healthLoading = ref(false);
const healthError = ref(false);
const loading = ref(false);
let healthInterval: any = null;
let cleanupInterval: any = null;

async function loadStats() {
  loading.value = true;
  try {
    const [u, d, c, a, t] = await Promise.all([
      listUsers({ page: 0, size: 1000 }).catch(() => ({ data: { content: [], totalElements: 0 } })),
      listDishesAdmin({ page: 0, size: 1000 }).catch(() => ({
        data: { content: [], totalElements: 0 },
      })),
      listCategories().catch(() => ({ data: [] })),
      listAreas().catch(() => ({ data: [] })),
      listTables().catch(() => ({ data: [] })),
    ]);

    // Lấy totalElements từ paginated response (chính xác số lượng thực tế)
    stats.value = {
      users: u.data?.totalElements || 0,
      dishes: d.data?.totalElements || 0,
      categories: Array.isArray(c.data) ? c.data.length : c.data?.content?.length || 0,
      areas: Array.isArray(a.data) ? a.data.length : a.data?.content?.length || 0,
      tables: Array.isArray(t.data) ? t.data.length : t.data?.content?.length || 0,
    };
  } catch (e) {
    console.warn("Lỗi tải stats:", e);
  } finally {
    loading.value = false;
  }
}

async function loadServerHealth() {
  healthLoading.value = true;
  healthError.value = false;
  try {
    const response = await http.get("/health");
    serverHealth.value = response.data;
    healthError.value = false;
  } catch (e: any) {
    healthError.value = true;
    console.warn("Không thể lấy health status:", e);
  } finally {
    healthLoading.value = false;
  }
}

async function cleanupEmptyOrders() {
  try {
    const response = await http.delete("/v1/orders/cleanup-empty");
    const message = response.data || "Đã dọn dẹp đơn hàng trống";
    console.log("🧹 Auto cleanup:", message);
  } catch (e: any) {
    // Silent fail - không hiển thị lỗi cho user vì chạy tự động
    // API này có thể chưa được implement hoặc đã bị disable
    console.debug("Cleanup endpoint not available or disabled:", e?.message);
  }
}

function startHealthMonitoring() {
  loadServerHealth();
  healthInterval = setInterval(() => {
    loadServerHealth();
  }, 15000); // Refresh mỗi 15 giây
}

function startAutoCleanup() {
  // Tạm thời disable auto cleanup vì endpoint có thể chưa ready
  // Uncomment khi backend đã có endpoint này
  // cleanupEmptyOrders();
  // cleanupInterval = setInterval(() => {
  //   cleanupEmptyOrders();
  // }, 15 * 60 * 1000); // 15 phút = 900,000ms
}

function stopHealthMonitoring() {
  if (healthInterval) {
    clearInterval(healthInterval);
    healthInterval = null;
  }
}

function stopAutoCleanup() {
  if (cleanupInterval) {
    clearInterval(cleanupInterval);
    cleanupInterval = null;
  }
}

onMounted(() => {
  loadStats();
  startHealthMonitoring();
  startAutoCleanup();
});

onUnmounted(() => {
  stopHealthMonitoring();
  stopAutoCleanup();
});

const totalAll = computed(
  () =>
    stats.value.users +
    stats.value.dishes +
    stats.value.categories +
    stats.value.areas +
    stats.value.tables
);

const systemStatus = computed(() => {
  if (!serverHealth.value) return "unknown";
  return serverHealth.value.status?.toLowerCase() || "unknown";
});

const statusColor = computed(() => {
  switch (systemStatus.value) {
    case "up":
      return { bg: "bg-emerald-500/20", text: "text-emerald-400", border: "border-emerald-500/30" };
    case "down":
      return { bg: "bg-rose-500/20", text: "text-rose-400", border: "border-rose-500/30" };
    default:
      return { bg: "bg-amber-500/20", text: "text-amber-400", border: "border-amber-500/30" };
  }
});

const dbStatus = computed(() => {
  const db = serverHealth.value?.components?.db;
  if (!db) return null;
  return {
    status: db.status,
    database: db.details?.database || "Unknown",
    validationQuery: db.details?.validationQuery,
  };
});

const diskSpace = computed(() => {
  const disk = serverHealth.value?.components?.diskSpace;
  if (!disk) return null;
  const total = disk.details?.total || 0;
  const free = disk.details?.free || 0;
  const used = total - free;
  const usedPercent = total > 0 ? Math.round((used / total) * 100) : 0;
  return {
    status: disk.status,
    total: formatBytes(total),
    free: formatBytes(free),
    used: formatBytes(used),
    usedPercent,
    threshold: formatBytes(disk.details?.threshold || 0),
  };
});

const pingStatus = computed(() => {
  const ping = serverHealth.value?.components?.ping;
  if (!ping) return null;
  return {
    status: ping.status,
  };
});

function formatBytes(bytes: number) {
  if (bytes === 0) return "0 B";
  const k = 1024;
  const sizes = ["B", "KB", "MB", "GB", "TB"];
  const i = Math.floor(Math.log(bytes) / Math.log(k));
  return Math.round((bytes / Math.pow(k, i)) * 100) / 100 + " " + sizes[i];
}

function getStatusBadge(status: string) {
  switch (status?.toLowerCase()) {
    case "up":
      return {
        text: "Hoạt động",
        class: "bg-emerald-500/20 text-emerald-300 border-emerald-500/40",
      };
    case "down":
      return { text: "Lỗi", class: "bg-rose-500/20 text-rose-300 border-rose-500/40" };
    default:
      return { text: "Không rõ", class: "bg-amber-500/20 text-amber-300 border-amber-500/40" };
  }
}
</script>

<template>
  <div class="space-y-6">
    <!-- Header -->
    <AdminPageHeader
      icon="⚡"
      title="System Dashboard"
      description="Giám sát trạng thái hệ thống và server real-time"
    />

    <!-- Loading State -->
    <AdminLoadingSkeleton v-if="loading && healthLoading" type="card" :count="3" />

    <div v-else class="space-y-6">
      <!-- SERVER HEALTH STATUS - HERO SECTION -->
      <div
        class="relative overflow-hidden rounded-2xl border border-white/10 bg-gradient-to-br from-slate-900/50 via-slate-800/30 to-slate-900/50 backdrop-blur-xl shadow-2xl"
      >
        <div class="absolute inset-0 bg-grid-white/[0.02] bg-[size:32px_32px]"></div>

        <!-- Animated Background Effects -->
        <div
          class="absolute -top-32 -left-32 h-64 w-64 rounded-full bg-emerald-500/10 blur-3xl animate-pulse"
        ></div>
        <div
          class="absolute -bottom-32 -right-32 h-64 w-64 rounded-full bg-blue-500/10 blur-3xl animate-pulse"
          style="animation-delay: 1s"
        ></div>

        <div class="relative z-10 p-8">
          <div class="flex items-start justify-between mb-8">
            <div>
              <h2 class="text-3xl font-bold text-white mb-2 flex items-center gap-3">
                <span class="text-4xl">🖥️</span>
                Server Status
              </h2>
              <p class="text-white/60">Real-time monitoring • Auto refresh mỗi 15s</p>
            </div>

            <!-- Status Badge -->
            <div
              v-if="!healthError"
              :class="[
                'px-6 py-3 rounded-full border-2 flex items-center gap-3 transition-all duration-300',
                statusColor.bg,
                statusColor.border,
              ]"
            >
              <div class="relative">
                <div
                  :class="[
                    'w-3 h-3 rounded-full',
                    systemStatus === 'up' ? 'bg-emerald-400' : 'bg-rose-400',
                  ]"
                ></div>
                <div
                  v-if="systemStatus === 'up'"
                  class="absolute inset-0 w-3 h-3 rounded-full bg-emerald-400 animate-ping"
                ></div>
              </div>
              <span :class="['font-bold uppercase tracking-wider text-sm', statusColor.text]">
                {{
                  systemStatus === "up" ? "ONLINE" : systemStatus === "down" ? "OFFLINE" : "UNKNOWN"
                }}
              </span>
            </div>

            <div
              v-else
              class="px-6 py-3 rounded-full border-2 bg-rose-500/20 border-rose-500/40 flex items-center gap-3"
            >
              <span class="text-rose-300 font-bold text-sm">⚠️ KHÔNG THỂ KẾT NỐI</span>
            </div>
          </div>

          <!-- Health Components Grid -->
          <div v-if="serverHealth && !healthError" class="grid md:grid-cols-3 gap-6">
            <!-- Database Status -->
            <div
              v-if="dbStatus"
              class="group relative overflow-hidden rounded-xl border border-white/10 bg-white/5 backdrop-blur-sm p-6 hover:bg-white/10 transition-all duration-300 hover:-translate-y-1"
            >
              <div
                class="absolute inset-0 bg-gradient-to-br from-blue-500/10 to-transparent opacity-0 group-hover:opacity-100 transition-opacity"
              ></div>
              <div class="relative">
                <div class="flex items-center justify-between mb-4">
                  <div class="text-4xl">🗄️</div>
                  <span
                    :class="[
                      'px-3 py-1 rounded-lg text-xs font-bold border',
                      getStatusBadge(dbStatus.status).class,
                    ]"
                  >
                    {{ getStatusBadge(dbStatus.status).text }}
                  </span>
                </div>
                <h3 class="text-lg font-bold text-white mb-1">Database</h3>
                <p class="text-sm text-white/60">{{ dbStatus.database }}</p>
                <div
                  v-if="dbStatus.validationQuery"
                  class="mt-3 text-xs font-mono text-white/40 bg-black/20 px-2 py-1 rounded"
                >
                  {{ dbStatus.validationQuery }}
                </div>
              </div>
            </div>

            <!-- Disk Space -->
            <div
              v-if="diskSpace"
              class="group relative overflow-hidden rounded-xl border border-white/10 bg-white/5 backdrop-blur-sm p-6 hover:bg-white/10 transition-all duration-300 hover:-translate-y-1"
            >
              <div
                class="absolute inset-0 bg-gradient-to-br from-purple-500/10 to-transparent opacity-0 group-hover:opacity-100 transition-opacity"
              ></div>
              <div class="relative">
                <div class="flex items-center justify-between mb-4">
                  <div class="text-4xl">💾</div>
                  <span
                    :class="[
                      'px-3 py-1 rounded-lg text-xs font-bold border',
                      getStatusBadge(diskSpace.status).class,
                    ]"
                  >
                    {{ getStatusBadge(diskSpace.status).text }}
                  </span>
                </div>
                <h3 class="text-lg font-bold text-white mb-3">Disk Space</h3>

                <!-- Progress Bar -->
                <div class="mb-3">
                  <div class="flex justify-between text-xs text-white/60 mb-1">
                    <span>Đã dùng {{ diskSpace.usedPercent }}%</span>
                    <span>{{ diskSpace.used }} / {{ diskSpace.total }}</span>
                  </div>
                  <div class="h-2 bg-white/10 rounded-full overflow-hidden">
                    <div
                      class="h-full rounded-full transition-all duration-500"
                      :class="
                        diskSpace.usedPercent > 80
                          ? 'bg-rose-500'
                          : diskSpace.usedPercent > 60
                          ? 'bg-amber-500'
                          : 'bg-emerald-500'
                      "
                      :style="{ width: diskSpace.usedPercent + '%' }"
                    ></div>
                  </div>
                </div>

                <div class="text-xs text-white/50">
                  <div>📊 Còn trống: {{ diskSpace.free }}</div>
                </div>
              </div>
            </div>

            <!-- Ping Status -->
            <div
              v-if="pingStatus"
              class="group relative overflow-hidden rounded-xl border border-white/10 bg-white/5 backdrop-blur-sm p-6 hover:bg-white/10 transition-all duration-300 hover:-translate-y-1"
            >
              <div
                class="absolute inset-0 bg-gradient-to-br from-emerald-500/10 to-transparent opacity-0 group-hover:opacity-100 transition-opacity"
              ></div>
              <div class="relative">
                <div class="flex items-center justify-between mb-4">
                  <div class="text-4xl">📡</div>
                  <span
                    :class="[
                      'px-3 py-1 rounded-lg text-xs font-bold border',
                      getStatusBadge(pingStatus.status).class,
                    ]"
                  >
                    {{ getStatusBadge(pingStatus.status).text }}
                  </span>
                </div>
                <h3 class="text-lg font-bold text-white mb-1">Network Ping</h3>
                <p class="text-sm text-white/60">Kết nối mạng ổn định</p>
                <div class="mt-4 flex items-center gap-2 text-emerald-400">
                  <div class="w-2 h-2 bg-emerald-400 rounded-full animate-pulse"></div>
                  <span class="text-xs font-medium">Latency: Normal</span>
                </div>
              </div>
            </div>
          </div>

          <!-- Error State -->
          <div v-else-if="healthError" class="text-center py-12">
            <div class="text-6xl mb-4">⚠️</div>
            <h3 class="text-xl font-bold text-rose-300 mb-2">Không thể kết nối đến server</h3>
            <p class="text-white/60 mb-4">Vui lòng kiểm tra kết nối hoặc đăng nhập lại</p>
            <button
              @click="loadServerHealth"
              class="px-6 py-2 bg-emerald-600 hover:bg-emerald-700 text-white rounded-lg transition-colors"
            >
              🔄 Thử lại
            </button>
          </div>

          <!-- Loading State -->
          <div v-else-if="healthLoading" class="text-center py-12">
            <div
              class="inline-block w-12 h-12 border-4 border-white/20 border-t-emerald-500 rounded-full animate-spin mb-4"
            ></div>
            <p class="text-white/60">Đang tải trạng thái server...</p>
          </div>
        </div>
      </div>

      <!-- STATISTICS CARDS -->
      <div class="grid sm:grid-cols-2 lg:grid-cols-5 gap-4">
        <div
          v-for="item in [
            { label: 'Người dùng', value: stats.users, icon: '👥', color: 'blue' },
            { label: 'Món ăn', value: stats.dishes, icon: '🍽️', color: 'emerald' },
            { label: 'Danh mục', value: stats.categories, icon: '🗂️', color: 'purple' },
            { label: 'Khu vực', value: stats.areas, icon: '🏢', color: 'amber' },
            { label: 'Bàn ăn', value: stats.tables, icon: '🪑', color: 'rose' },
          ]"
          :key="item.label"
          class="group relative overflow-hidden rounded-xl border border-white/10 bg-white/5 backdrop-blur-sm p-6 hover:bg-white/10 transition-all duration-300 hover:-translate-y-1"
        >
          <div
            :class="[
              'absolute inset-0 bg-gradient-to-br opacity-0 group-hover:opacity-100 transition-opacity',
              item.color === 'blue'
                ? 'from-blue-500/10'
                : item.color === 'emerald'
                ? 'from-emerald-500/10'
                : item.color === 'purple'
                ? 'from-purple-500/10'
                : item.color === 'amber'
                ? 'from-amber-500/10'
                : 'from-rose-500/10',
            ]"
          ></div>

          <div class="relative">
            <div class="text-4xl mb-3">{{ item.icon }}</div>
            <div class="text-3xl font-bold text-white mb-1">{{ item.value }}</div>
            <div class="text-sm text-white/60">{{ item.label }}</div>
          </div>
        </div>
      </div>

      <!-- Quick Actions -->
      <div class="grid md:grid-cols-2 lg:grid-cols-4 gap-4">
        <RouterLink
          v-for="link in [
            { to: '/admin/users', label: 'Người dùng', icon: '👥', color: 'blue' },
            { to: '/admin/dishes', label: 'Thực đơn', icon: '🍽️', color: 'emerald' },
            { to: '/admin/tables', label: 'Bàn ăn', icon: '🪑', color: 'purple' },
            { to: '/admin/orders', label: 'Đơn hàng', icon: '🧾', color: 'amber' },
          ]"
          :key="link.to"
          :to="link.to"
          class="group relative overflow-hidden rounded-xl border border-white/10 bg-white/5 backdrop-blur-sm p-6 hover:bg-white/10 transition-all duration-300 hover:-translate-y-1"
        >
          <div class="flex items-center justify-between">
            <div>
              <div class="text-3xl mb-2">{{ link.icon }}</div>
              <h3 class="text-lg font-bold text-white">{{ link.label }}</h3>
            </div>
            <svg
              class="w-6 h-6 text-white/40 group-hover:text-white/80 group-hover:translate-x-1 transition-all"
              fill="none"
              stroke="currentColor"
              viewBox="0 0 24 24"
            >
              <path
                stroke-linecap="round"
                stroke-linejoin="round"
                stroke-width="2"
                d="M9 5l7 7-7 7"
              ></path>
            </svg>
          </div>
        </RouterLink>
      </div>
    </div>
  </div>
</template>

<style scoped>
.bg-grid-white {
  background-image: linear-gradient(to right, rgba(255, 255, 255, 0.05) 1px, transparent 1px),
    linear-gradient(to bottom, rgba(255, 255, 255, 0.05) 1px, transparent 1px);
}

@keyframes pulse {
  0%,
  100% {
    opacity: 0.3;
  }
  50% {
    opacity: 0.6;
  }
}

@keyframes ping {
  75%,
  100% {
    transform: scale(2);
    opacity: 0;
  }
}

.animate-pulse {
  animation: pulse 3s cubic-bezier(0.4, 0, 0.6, 1) infinite;
}

.animate-ping {
  animation: ping 1.5s cubic-bezier(0, 0, 0.2, 1) infinite;
}
</style>
