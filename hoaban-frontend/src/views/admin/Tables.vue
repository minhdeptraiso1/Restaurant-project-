<script setup lang="ts">
import { onMounted, ref, computed } from "vue";
import { listTables, createTable, updateTable, issueQrCode } from "@/api/tables.admin";
import { listAreas } from "@/api/areas.admin";
import { toast } from "vue3-toastify";
import QRCode from "qrcode";

type Table = {
  id: string;
  areaId: string;
  code: string;
  seats: number;
  status: "AVAILABLE" | "UNAVAILABLE";
};
type Area = { id: string; name: string };

const tables = ref<Table[]>([]);
const areas = ref<Area[]>([]);
const loading = ref(false);
const submitting = ref(false);
const editingId = ref<string>("");

// QR Modal
const showQrModal = ref(false);
const qrData = ref<{
  tableId: string;
  tableName: string;
  qrCode: string;
  ttlSeconds: number;
} | null>(null);
const generatingQr = ref(false);
const qrCodeImageUrl = ref<string>("");

// form
const form = ref<{ areaId: string; code: string; seats: number; status: Table["status"] }>({
  areaId: "",
  code: "",
  seats: 4,
  status: "AVAILABLE",
});

// filters
const q = ref("");
const areaFilter = ref<string>("ALL");
const statusFilter = ref<"ALL" | Table["status"]>("ALL");
const sortKey = ref<"code" | "seats">("code");
const sortDir = ref<"asc" | "desc">("asc");

async function load() {
  loading.value = true;
  try {
    const [tablesRes, areasRes] = await Promise.all([listTables(), listAreas()]);
    tables.value = tablesRes.data || [];
    areas.value = areasRes.data || [];
  } catch (e: any) {
    toast.error(e?.friendlyMessage || "Không tải được dữ liệu");
  } finally {
    loading.value = false;
  }
}

const isValid = computed(() => {
  return form.value.code.trim().length >= 2 && !!form.value.areaId && form.value.seats >= 1;
});

function resetForm() {
  form.value = { areaId: "", code: "", seats: 4, status: "AVAILABLE" };
  editingId.value = "";
}
function startEdit(t: Table) {
  editingId.value = t.id;
  form.value = {
    areaId: t.areaId,
    code: t.code,
    seats: Number(t.seats || 0),
    status: t.status,
  };
  // kéo lên form cho tiện
  window.scrollTo({ top: 0, behavior: "smooth" });
}

async function submit() {
  if (!isValid.value) {
    toast.warning("Vui lòng nhập đúng thông tin: mã bàn ≥ 2 ký tự, chọn khu vực, số ghế ≥ 1");
    return;
  }
  submitting.value = true;
  try {
    if (editingId.value) {
      await updateTable(editingId.value, {
        code: form.value.code?.trim(),
        areaId: form.value.areaId,
        seats: form.value.seats,
        status: form.value.status, // nếu cho phép đổi trạng thái
      });
      toast.success("Cập nhật bàn thành công");
    } else {
      await createTable(form.value);
      toast.success("Tạo bàn thành công");
    }
    resetForm();
    await load();
  } catch (e: any) {
    toast.error(
      e?.friendlyMessage || (editingId.value ? "Cập nhật bàn thất bại" : "Tạo bàn thất bại")
    );
  } finally {
    submitting.value = false;
  }
}

function areaName(id: string) {
  return areas.value.find((a) => a.id === id)?.name || "N/A";
}

function statusBadge(s: Table["status"]) {
  switch (s) {
    case "AVAILABLE":
      return "badge-success";
    case "UNAVAILABLE":
      return "badge-danger";
    default:
      return "badge-gray";
  }
}

// lọc + sắp xếp
const filtered = computed(() => {
  let data = [...tables.value];

  const kw = q.value.trim().toLowerCase();
  if (kw) data = data.filter((t) => t.code.toLowerCase().includes(kw));

  if (areaFilter.value !== "ALL") data = data.filter((t) => t.areaId === areaFilter.value);
  if (statusFilter.value !== "ALL") data = data.filter((t) => t.status === statusFilter.value);

  data.sort((a, b) => {
    const dir = sortDir.value === "asc" ? 1 : -1;
    if (sortKey.value === "code") return a.code.localeCompare(b.code) * dir;
    return (Number(a.seats) - Number(b.seats)) * dir;
  });

  return data;
});

// QR Functions
async function showQrCode(table: Table) {
  generatingQr.value = true;
  try {
    const response = await issueQrCode(table.id, 1800); // 30 phút
    const data = response.data || response;

    qrData.value = {
      tableId: table.id,
      tableName: table.code,
      qrCode: data.qrCode,
      ttlSeconds: data.ttlSeconds || 1800,
    };

    // Tạo URL để quét QR
    const baseUrl = window.location.origin;
    const qrUrl = `${baseUrl}/open-order?qr=${encodeURIComponent(data.qrCode)}`;

    // Generate QR code image từ URL
    qrCodeImageUrl.value = await QRCode.toDataURL(qrUrl, {
      width: 300,
      margin: 2,
      color: {
        dark: "#000000",
        light: "#FFFFFF",
      },
    });

    showQrModal.value = true;
  } catch (e: any) {
    toast.error(e?.friendlyMessage || "Không thể tạo mã QR");
  } finally {
    generatingQr.value = false;
  }
}

function closeQrModal() {
  showQrModal.value = false;
  qrData.value = null;
  qrCodeImageUrl.value = "";
}

function copyQrCode() {
  if (!qrData.value) return;
  navigator.clipboard.writeText(qrData.value.qrCode);
  toast.success("Đã copy mã QR vào clipboard");
}

function getQrUrl() {
  if (!qrData.value) return "";
  const baseUrl = window.location.origin;
  return `${baseUrl}/open-order?qr=${encodeURIComponent(qrData.value.qrCode)}`;
}

function copyQrUrl() {
  const url = getQrUrl();
  if (!url) return;
  navigator.clipboard.writeText(url);
  toast.success("Đã copy link QR vào clipboard");
}

function openQrUrl() {
  const url = getQrUrl();
  if (!url) return;
  window.open(url, "_blank");
}

onMounted(load);
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
        class="absolute -bottom-16 -right-10 h-56 w-56 rounded-full bg-blue-500/20 blur-3xl"
      ></div>
      <div class="relative z-10">
        <h2 class="text-2xl md:text-3xl font-extrabold tracking-tight">🪑 Quản lý bàn</h2>
        <p class="mt-1 text-white/70">Tạo/sửa bàn theo khu vực, theo dõi trạng thái sử dụng.</p>
      </div>
    </div>

    <!-- Form -->
    <div class="rounded-2xl bg-white/5 backdrop-blur-md border border-white/10 shadow p-6">
      <div class="flex items-center justify-between mb-4">
        <h3 class="font-semibold text-lg">
          {{ editingId ? "Chỉnh sửa bàn" : "Tạo bàn mới" }}
        </h3>
        <button v-if="editingId" class="btn-admin-secondary text-xs" @click="resetForm">
          ✖ Hủy
        </button>
      </div>

      <div class="grid sm:grid-cols-4 gap-3 mb-4">
        <div class="relative">
          <span class="absolute left-3 top-2.5 opacity-70">🏷️</span>
          <input
            class="w-full bg-white/5 border border-white/10 rounded-lg pl-9 pr-3 py-2 outline-none focus:border-emerald-400"
            placeholder="Mã bàn (T01, T02...) *"
            v-model="form.code"
          />
        </div>

        <div class="relative">
          <span class="absolute left-3 top-2.5 opacity-70">🏢</span>
          <select
            class="w-full bg-white/5 border border-white/10 rounded-lg pl-9 pr-3 py-2 outline-none focus:border-emerald-400"
            v-model="form.areaId"
          >
            <option class="bg-[#0b1512]" value="">-- Chọn khu vực --</option>
            <option class="bg-[#0b1512]" v-for="a in areas" :key="a.id" :value="a.id">
              {{ a.name }}
            </option>
          </select>
        </div>

        <div class="relative">
          <span class="absolute left-3 top-2.5 opacity-70">👥</span>
          <input
            class="w-full bg-white/5 border border-white/10 rounded-lg pl-9 pr-3 py-2 outline-none focus:border-emerald-400"
            type="number"
            min="1"
            placeholder="Số ghế *"
            v-model.number="form.seats"
          />
        </div>

        <div class="relative">
          <span class="absolute left-3 top-2.5 opacity-70">🔖</span>
          <select
            class="w-full bg-white/5 border border-white/10 rounded-lg pl-9 pr-3 py-2 outline-none focus:border-emerald-400"
            v-model="form.status"
          >
            <option class="bg-[#0b1512]" value="AVAILABLE">Có sẵn</option>
            <option class="bg-[#0b1512]" value="UNAVAILABLE">Không khả dụng</option>
          </select>
        </div>
      </div>

      <div>
        <button
          class="px-4 py-2 rounded-lg bg-emerald-600 hover:bg-emerald-700 transition disabled:opacity-50 disabled:cursor-not-allowed"
          :disabled="submitting || !isValid"
          @click="submit"
        >
          <span v-if="submitting" class="inline-flex items-center gap-2">
            <span
              class="w-4 h-4 inline-block rounded-full border-2 border-white border-t-transparent animate-spin"
            ></span>
            Đang lưu...
          </span>
          <span v-else>{{ editingId ? "Cập nhật bàn" : "Tạo bàn" }}</span>
        </button>
        <span v-if="!isValid" class="ml-3 text-xs text-white/60">Nhập đủ thông tin bắt buộc</span>
      </div>
    </div>

    <!-- Bộ lọc -->
    <div class="rounded-2xl bg-white/5 backdrop-blur-md border border-white/10 shadow p-6">
      <div class="grid md:grid-cols-2 lg:grid-cols-5 gap-3">
        <div class="relative lg:col-span-2">
          <span class="absolute left-3 top-2.5 opacity-70">🔎</span>
          <input
            class="w-full bg-white/5 border border-white/10 rounded-lg pl-9 pr-3 py-2 outline-none focus:border-emerald-400"
            placeholder="Tìm theo mã bàn…"
            v-model="q"
          />
        </div>

        <div class="relative">
          <span class="absolute left-3 top-2.5 opacity-70">🏢</span>
          <select
            class="w-full bg-white/5 border border-white/10 rounded-lg pl-9 pr-3 py-2 outline-none focus:border-emerald-400"
            v-model="areaFilter"
          >
            <option class="bg-[#0b1512]" value="ALL">Tất cả khu vực</option>
            <option class="bg-[#0b1512]" v-for="a in areas" :key="a.id" :value="a.id">
              {{ a.name }}
            </option>
          </select>
        </div>

        <div class="relative">
          <span class="absolute left-3 top-2.5 opacity-70">🔖</span>
          <select
            class="w-full bg-white/5 border border-white/10 rounded-lg pl-9 pr-3 py-2 outline-none focus:border-emerald-400"
            v-model="statusFilter"
          >
            <option class="bg-[#0b1512]" value="ALL">Tất cả trạng thái</option>
            <option class="bg-[#0b1512]" value="AVAILABLE">Có sẵn</option>
            <option class="bg-[#0b1512]" value="UNAVAILABLE">Không khả dụng</option>
          </select>
        </div>

        <div class="flex gap-2">
          <div class="relative grow">
            <span class="absolute left-3 top-2.5 opacity-70">↕️</span>
            <select
              class="w-full bg-white/5 border border-white/10 rounded-lg pl-9 pr-3 py-2 outline-none focus:border-emerald-400"
              v-model="sortKey"
            >
              <option class="bg-[#0b1512]" value="code">Sắp xếp theo mã bàn</option>
              <option class="bg-[#0b1512]" value="seats">Sắp xếp theo số ghế</option>
            </select>
          </div>
          <button
            class="px-3 rounded-lg bg-white/10 hover:bg-white/15 border border-white/10 transition"
            @click="sortDir = sortDir === 'asc' ? 'desc' : 'asc'"
            :title="sortDir === 'asc' ? 'Đang tăng dần' : 'Đang giảm dần'"
          >
            {{ sortDir === "asc" ? "⬆️" : "⬇️" }}
          </button>
        </div>
      </div>
    </div>

    <!-- Danh sách -->
    <div class="rounded-2xl bg-white/5 backdrop-blur-md border border-white/10 shadow p-6">
      <h3 class="font-semibold text-lg mb-4">
        Danh sách bàn <span class="text-white/60 font-normal">({{ filtered.length }})</span>
      </h3>

      <!-- Loading skeleton -->
      <div v-if="loading" class="space-y-2">
        <div v-for="i in 5" :key="i" class="h-12 bg-white/10 rounded animate-pulse"></div>
      </div>

      <!-- Empty state -->
      <div
        v-else-if="!filtered.length"
        class="flex flex-col items-center justify-center py-16 text-center"
      >
        <div class="text-6xl mb-4">🪑</div>
        <h3 class="text-xl font-semibold mb-2">Không có bàn nào</h3>
        <p class="text-white/60 text-sm">Không có bàn nào phù hợp với bộ lọc.</p>
      </div>

      <div v-else class="overflow-x-auto">
        <table class="table-admin">
          <thead>
            <tr>
              <th>Mã bàn</th>
              <th>Khu vực</th>
              <th>Số ghế</th>
              <th>Trạng thái</th>
              <th>Thao tác</th>
            </tr>
          </thead>
          <tbody>
            <tr v-for="t in filtered" :key="t.id">
              <td>
                <div class="flex items-center gap-2 font-medium">
                  <span>🔖</span>
                  <span>{{ t.code }}</span>
                </div>
              </td>
              <td>{{ areaName(t.areaId) }}</td>
              <td>{{ t.seats }} ghế</td>
              <td>
                <span class="badge-admin" :class="statusBadge(t.status)">
                  {{ t.status === "AVAILABLE" ? "Có sẵn" : "Không khả dụng" }}
                </span>
              </td>
              <td>
                <div class="flex flex-wrap gap-2">
                  <button
                    class="btn-admin-info text-xs"
                    @click="showQrCode(t)"
                    :disabled="generatingQr"
                  >
                    <span v-if="generatingQr" class="loading-spinner"></span>
                    <span v-else>📱 QR</span>
                  </button>

                  <button class="btn-admin-info text-xs" @click="startEdit(t)">✏️ Sửa</button>
                </div>
              </td>
            </tr>
          </tbody>
        </table>
      </div>
    </div>

    <!-- QR Code Modal -->
    <div v-if="showQrModal && qrData" class="modal-overlay">
      <div class="modal-content max-w-lg">
        <div class="modal-header">
          <h3 class="text-xl font-bold">📱 Mã QR - Bàn {{ qrData.tableName }}</h3>
          <button @click="closeQrModal" class="text-white/60 hover:text-white">✕</button>
        </div>

        <div class="modal-body space-y-4">
          <!-- QR Code Display -->
          <div class="bg-white p-6 rounded-lg flex items-center justify-center">
            <div class="text-center">
              <!-- Real QR Code that can be scanned -->
              <div v-if="qrCodeImageUrl" class="inline-block">
                <img :src="qrCodeImageUrl" alt="QR Code" class="w-64 h-64 rounded-lg shadow-lg" />
                <p class="text-sm text-gray-600 mt-3">Quét mã này để mở đơn hàng</p>
              </div>
              <div v-else class="text-gray-500">
                <div class="loading-spinner mx-auto"></div>
                <p class="mt-2">Đang tạo mã QR...</p>
              </div>
            </div>
          </div>

          <!-- QR Info -->
          <div class="glass-card space-y-2 text-sm">
            <div class="flex justify-between">
              <span class="text-white/70">Bàn:</span>
              <span class="text-white font-medium">{{ qrData.tableName }}</span>
            </div>
            <div class="flex justify-between">
              <span class="text-white/70">Thời gian hiệu lực:</span>
              <span class="text-white font-medium"
                >{{ Math.floor(qrData.ttlSeconds / 60) }} phút</span
              >
            </div>
            <div class="flex justify-between items-start">
              <span class="text-white/70">Mã QR:</span>
              <span class="text-white font-mono text-xs break-all max-w-[250px]">{{
                qrData.qrCode
              }}</span>
            </div>
          </div>

          <!-- URL Display -->
          <div class="glass-card">
            <label class="block text-sm font-medium text-white/80 mb-2">Link quét QR:</label>
            <div class="flex gap-2">
              <input :value="getQrUrl()" readonly class="input-admin flex-1 text-xs font-mono" />
              <button @click="copyQrUrl" class="btn-admin-info text-xs whitespace-nowrap">
                📋 Copy URL
              </button>
            </div>
          </div>

          <!-- Actions -->
          <div class="flex gap-2">
            <button @click="copyQrCode" class="btn-admin-success flex-1">📋 Copy mã QR</button>
            <button @click="openQrUrl" class="btn-admin-info flex-1">🔗 Mở link</button>
          </div>

          <!-- Note -->
          <div class="bg-blue-500/10 border border-blue-500/20 rounded-lg p-3">
            <p class="text-xs text-blue-200">
              💡 <strong>Hướng dẫn:</strong> Khách hàng quét mã QR này hoặc truy cập link để mở đơn
              hàng cho bàn {{ qrData.tableName }} mà không cần đăng nhập.
            </p>
          </div>
        </div>

        <div class="modal-footer">
          <button @click="closeQrModal" class="btn-admin-secondary">Đóng</button>
        </div>
      </div>
    </div>
  </div>
</template>
