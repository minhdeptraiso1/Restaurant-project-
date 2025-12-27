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
          <h1 class="text-2xl md:text-3xl font-extrabold tracking-tight">🎟️ Quản lý Vouchers</h1>
          <p class="text-white/70 mt-1">Tạo, chỉnh sửa và quản lý voucher khuyến mãi</p>
        </div>
        <button
          @click="openCreate()"
          class="rounded-xl px-4 py-2 bg-emerald-600 hover:bg-emerald-700 border border-emerald-400/30 transition"
        >
          ➕ Tạo Voucher
        </button>
      </div>
    </div>

    <!-- Filters -->
    <div class="rounded-2xl bg-white/5 backdrop-blur-md border border-white/10 shadow p-6">
      <div class="grid md:grid-cols-2 lg:grid-cols-4 gap-3">
        <div class="relative">
          <span class="absolute left-3 top-2.5 opacity-70">🔎</span>
          <input
            v-model="q"
            placeholder="Tìm theo mã / tên…"
            class="w-full bg-white/5 border border-white/10 rounded-lg pl-9 pr-3 py-2 outline-none focus:border-emerald-400"
          />
        </div>
        <div class="relative">
          <span class="absolute left-3 top-2.5 opacity-70">🔖</span>
          <select
            v-model="statusFilter"
            class="w-full bg-white/5 border border-white/10 rounded-lg pl-9 pr-3 py-2 outline-none focus:border-emerald-400"
          >
            <option class="bg-[#0b1512]" value="ALL">Tất cả trạng thái</option>
            <option class="bg-[#0b1512]" value="ACTIVE">Hoạt động</option>
            <option class="bg-[#0b1512]" value="INACTIVE">Tạm dừng</option>
            <option class="bg-[#0b1512]" value="DRAFT">Nháp</option>
          </select>
        </div>
        <div class="relative">
          <span class="absolute left-3 top-2.5 opacity-70">⚙️</span>
          <select
            v-model="typeFilter"
            class="w-full bg-white/5 border border-white/10 rounded-lg pl-9 pr-3 py-2 outline-none focus:border-emerald-400"
          >
            <option class="bg-[#0b1512]" value="ALL">Tất cả loại</option>
            <option class="bg-[#0b1512]" value="PERCENT">Phần trăm</option>
            <option class="bg-[#0b1512]" value="FIXED">Cố định</option>
          </select>
        </div>
        <div class="flex gap-2">
          <div class="relative grow">
            <span class="absolute left-3 top-2.5 opacity-70">↕️</span>
            <select
              v-model="sortKey"
              class="w-full bg-white/5 border border-white/10 rounded-lg pl-9 pr-3 py-2 outline-none focus:border-emerald-400"
            >
              <option class="bg-[#0b1512]" value="created">Mới nhất</option>
              <option class="bg-[#0b1512]" value="name">Tên</option>
              <option class="bg-[#0b1512]" value="value">Giá trị</option>
              <option class="bg-[#0b1512]" value="start">Ngày bắt đầu</option>
              <option class="bg-[#0b1512]" value="end">Ngày kết thúc</option>
            </select>
          </div>
          <button
            class="px-3 rounded-lg bg-white/10 hover:bg-white/15 border border-white/10"
            @click="sortDir = sortDir === 'asc' ? 'desc' : 'asc'"
            :title="sortDir === 'asc' ? 'Tăng dần' : 'Giảm dần'"
          >
            {{ sortDir === "asc" ? "⬆️" : "⬇️" }}
          </button>
        </div>
      </div>
    </div>

    <!-- List -->
    <div class="rounded-2xl bg-white/5 backdrop-blur-md border border-white/10 shadow">
      <div class="p-6">
        <div v-if="loading" class="text-center py-8 text-white/70">Đang tải…</div>

        <div v-else-if="filtered.length === 0" class="text-center py-10 text-white/60">
          Chưa có voucher nào
        </div>

        <div v-else class="overflow-x-auto">
          <table class="min-w-full text-sm">
            <thead>
              <tr class="text-left text-white/70 border-b border-white/10">
                <th class="py-3 px-3">Mã</th>
                <th class="py-3 px-3">Tên</th>
                <th class="py-3 px-3">Loại</th>
                <th class="py-3 px-3">Giá trị</th>
                <th class="py-3 px-3">Giảm tối đa</th>
                <th class="py-3 px-3">Đơn tối thiểu</th>
                <th class="py-3 px-3">Điểm đổi</th>
                <th class="py-3 px-3">Hiệu lực</th>
                <th class="py-3 px-3">Trạng thái</th>
                <th class="py-3 px-3">Thao tác</th>
              </tr>
            </thead>
            <tbody>
              <tr
                v-for="v in filtered"
                :key="v.id"
                class="border-b border-white/5 hover:bg-white/5 transition"
              >
                <td class="py-3 px-3 font-mono font-semibold">{{ v.code }}</td>
                <td class="py-3 px-3">{{ v.name }}</td>
                <td class="py-3 px-3">
                  <span
                    class="px-2 py-1 rounded-lg text-xs border"
                    :class="
                      v.type === 'PERCENT'
                        ? 'bg-blue-500/15 text-blue-300 border-blue-300/30'
                        : 'bg-emerald-500/15 text-emerald-300 border-emerald-300/30'
                    "
                  >
                    {{ v.type === "PERCENT" ? "Phần trăm" : "Cố định" }}
                  </span>
                </td>
                <td class="py-3 px-3 font-medium">
                  {{ v.type === "PERCENT" ? v.value + "%" : formatCurrency(v.value) }}
                </td>
                <td class="py-3 px-3 text-white/80">
                  {{ v.maxDiscount ? formatCurrency(v.maxDiscount) : "—" }}
                </td>
                <td class="py-3 px-3 text-white/80">
                  {{ v.minOrder ? formatCurrency(v.minOrder) : "—" }}
                </td>
                <td class="py-3 px-3 text-white/80">{{ v.pointCost ?? 0 }}</td>
                <td class="py-3 px-3 text-xs">
                  <div>{{ formatDate(v.startAt) }}</div>
                  <div class="text-white/50">→ {{ formatDate(v.endAt) }}</div>
                </td>
                <td class="py-3 px-3">
                  <span :class="statusClass(v.status)" class="px-2 py-1 rounded-lg text-xs border">
                    {{ statusLabel(v.status) }}
                  </span>
                </td>
                <td class="py-3 px-3">
                  <div class="flex gap-2">
                    <button
                      class="px-2 py-1 text-xs rounded-lg border border-blue-400/40 text-blue-300 hover:bg-blue-500/10"
                      @click="editVoucher(v)"
                    >
                      ✏️ Sửa
                    </button>
                  </div>
                </td>
              </tr>
            </tbody>
          </table>
        </div>
      </div>
    </div>

    <!-- Modal -->
    <div
      v-if="showCreateModal || showEditModal"
      class="fixed inset-0 bg-black/60 backdrop-blur-sm grid place-items-center z-50"
    >
      <div
        class="w-full max-w-2xl mx-4 rounded-2xl border border-white/10 bg-white/5 text-white shadow-xl"
      >
        <div class="flex items-center justify-between p-5 border-b border-white/10">
          <h3 class="text-lg font-semibold">
            {{ showEditModal ? "Chỉnh sửa Voucher" : "Tạo Voucher mới" }}
          </h3>
          <button class="text-white/70 hover:text-white" @click="closeModal">✖</button>
        </div>

        <form class="p-5 space-y-5" @submit.prevent="saveVoucher">
          <div class="grid grid-cols-1 md:grid-cols-3 gap-3">
            <div>
              <label class="block text-sm font-medium mb-1.5 text-white/80">
                🏷️ Mã voucher *
              </label>
              <input
                v-model="form.code"
                :disabled="showEditModal"
                required
                placeholder="VD: SALE20"
                class="w-full bg-white/5 border border-white/10 rounded-lg px-3 py-2 outline-none focus:border-emerald-400"
              />
            </div>
            <div>
              <label class="block text-sm font-medium mb-1.5 text-white/80">
                📝 Tên hiển thị *
              </label>
              <input
                v-model="form.name"
                required
                placeholder="Giảm giá cho DAU"
                class="w-full bg-white/5 border border-white/10 rounded-lg px-3 py-2 outline-none focus:border-emerald-400"
              />
            </div>
            <div>
              <label class="block text-sm font-medium mb-1.5 text-white/80">
                ⚙️ Loại giảm giá *
              </label>
              <select
                v-model="form.type"
                required
                class="w-full bg-white/5 border border-white/10 rounded-lg px-3 py-2 outline-none focus:border-emerald-400"
              >
                <option class="bg-[#0b1512]" value="">-- Chọn loại --</option>
                <option class="bg-[#0b1512]" value="PERCENT">Phần trăm (%)</option>
                <option class="bg-[#0b1512]" value="FIXED">Số tiền (VNĐ)</option>
              </select>
            </div>
          </div>

          <div class="grid grid-cols-1 md:grid-cols-4 gap-3">
            <div>
              <label class="block text-sm font-medium mb-1.5 text-white/80">
                💴 {{ form.type === "PERCENT" ? "Giảm (%)" : "Số tiền (VNĐ)" }} *
              </label>
              <input
                v-model.number="form.value"
                type="number"
                :min="form.type === 'PERCENT' ? 1 : 1000"
                :max="form.type === 'PERCENT' ? 100 : 100000000"
                :step="form.type === 'PERCENT' ? 1 : 1000"
                required
                :placeholder="form.type === 'PERCENT' ? '0-100' : '1000'"
                class="w-full bg-white/5 border border-white/10 rounded-lg px-3 py-2 outline-none focus:border-emerald-400"
              />
            </div>

            <div>
              <label class="block text-sm font-medium mb-1.5 text-white/80">
                🧾 Đơn tối thiểu (VNĐ)
              </label>
              <input
                v-model.number="form.minOrder"
                type="number"
                min="0"
                step="1000"
                placeholder="0"
                class="w-full bg-white/5 border border-white/10 rounded-lg px-3 py-2 outline-none focus:border-emerald-400"
              />
            </div>

            <div>
              <label class="block text-sm font-medium mb-1.5 text-white/80">
                💰 Giảm tối đa (VNĐ)
              </label>
              <input
                v-model.number="form.maxDiscount"
                type="number"
                min="0"
                step="1000"
                placeholder="0"
                class="w-full bg-white/5 border border-white/10 rounded-lg px-3 py-2 outline-none focus:border-emerald-400"
              />
              <p class="text-xs text-white/50 mt-1">Chỉ áp dụng cho giảm %</p>
            </div>

            <div>
              <label class="block text-sm font-medium mb-1.5 text-white/80"> ⭐ Điểm đổi </label>
              <input
                v-model.number="form.pointCost"
                type="number"
                min="0"
                placeholder="0"
                class="w-full bg-white/5 border border-white/10 rounded-lg px-3 py-2 outline-none focus:border-emerald-400"
              />
            </div>
          </div>

          <div class="grid grid-cols-1 md:grid-cols-2 gap-3">
            <div>
              <label class="block text-sm font-medium mb-1.5 text-white/80">
                🕒 Ngày bắt đầu *
              </label>
              <input
                v-model="form.startAt"
                type="datetime-local"
                required
                class="w-full bg-white/5 border border-white/10 rounded-lg px-3 py-2 outline-none focus:border-emerald-400"
              />
            </div>
            <div>
              <label class="block text-sm font-medium mb-1.5 text-white/80">
                ⏳ Ngày kết thúc *
              </label>
              <input
                v-model="form.endAt"
                type="datetime-local"
                required
                class="w-full bg-white/5 border border-white/10 rounded-lg px-3 py-2 outline-none focus:border-emerald-400"
              />
            </div>
          </div>

          <div>
            <label class="block text-sm font-medium mb-1.5 text-white/80"> 📄 Mô tả </label>
            <textarea
              v-model="form.description"
              rows="3"
              placeholder="Nhập mô tả chi tiết về voucher..."
              class="w-full bg-white/5 border border-white/10 rounded-lg px-3 py-2 outline-none focus:border-emerald-400"
            ></textarea>
          </div>

          <div class="flex justify-end gap-2 pt-2">
            <button
              type="button"
              @click="closeModal"
              class="px-4 py-2 rounded-lg bg-white/10 hover:bg-white/15 border border-white/10"
            >
              Hủy
            </button>
            <button
              type="submit"
              :disabled="saving"
              class="px-4 py-2 rounded-lg bg-emerald-600 hover:bg-emerald-700 border border-emerald-400/30 disabled:opacity-50"
            >
              {{ saving ? "Đang lưu..." : showEditModal ? "Cập nhật" : "Tạo" }}
            </button>
          </div>
        </form>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, computed } from "vue";
import { toast } from "vue3-toastify";
import { vouchersAdminAPI, type UpdateVoucherPayload } from "@/api/vouchers.admin";

// state
const vouchers = ref<any[]>([]);
const loading = ref(false);
const saving = ref(false);

const showCreateModal = ref(false);
const showEditModal = ref(false);
const editingVoucher = ref<any>(null);

// filters
const q = ref("");
const statusFilter = ref<"ALL" | "ACTIVE" | "INACTIVE" | "DRAFT">("ALL");
const typeFilter = ref<"ALL" | "PERCENT" | "FIXED">("ALL");
const sortKey = ref<"created" | "name" | "value" | "start" | "end">("created");
const sortDir = ref<"asc" | "desc">("desc");

// form
const form = ref<any>({
  code: "",
  name: "",
  type: "PERCENT",
  value: 10,
  maxDiscount: 0,
  minOrder: 0,
  pointCost: 0,
  startAt: "",
  endAt: "",
  status: "ACTIVE",
  description: "",
});

async function loadVouchers() {
  loading.value = true;
  try {
    const { data } = await vouchersAdminAPI.list(0, 100);
    vouchers.value = data.content || data || [];
  } catch (e: any) {
    toast.error(e?.response?.data?.message || "Lỗi tải danh sách voucher");
  } finally {
    loading.value = false;
  }
}

const filtered = computed(() => {
  let data = [...vouchers.value];
  const kw = q.value.trim().toLowerCase();
  if (kw) {
    data = data.filter(
      (v) => v.code?.toLowerCase().includes(kw) || v.name?.toLowerCase().includes(kw)
    );
  }
  if (statusFilter.value !== "ALL") data = data.filter((v) => v.status === statusFilter.value);
  if (typeFilter.value !== "ALL") data = data.filter((v) => v.type === typeFilter.value);

  data.sort((a, b) => {
    const dir = sortDir.value === "asc" ? 1 : -1;
    switch (sortKey.value) {
      case "name":
        return a.name.localeCompare(b.name) * dir;
      case "value":
        return (Number(a.value) - Number(b.value)) * dir;
      case "start":
        return (new Date(a.startAt).getTime() - new Date(b.startAt).getTime()) * dir;
      case "end":
        return (new Date(a.endAt).getTime() - new Date(b.endAt).getTime()) * dir;
      default: // created (không có field -> fallback endAt)
        return (
          (new Date(a.endAt || a.startAt).getTime() - new Date(b.endAt || b.startAt).getTime()) *
          dir
        );
    }
  });

  return data;
});

function openCreate() {
  resetForm();
  showCreateModal.value = true;
}

async function saveVoucher() {
  saving.value = true;
  try {
    if (showEditModal.value && editingVoucher.value) {
      const diff = buildUpdatePayload(editingVoucher.value, form.value);
      if (Object.keys(diff).length === 0) {
        toast.info("Không có thay đổi");
      } else {
        await vouchersAdminAPI.update(editingVoucher.value.id, diff);
        toast.success("Cập nhật voucher thành công");
      }
    } else {
      await vouchersAdminAPI.create({
        code: form.value.code.trim().toUpperCase(),
        name: form.value.name,
        type: form.value.type,
        value: form.value.value,
        minOrder: form.value.minOrder,
        maxDiscount: form.value.maxDiscount,
        pointCost: form.value.pointCost,
        startAt: toIso(form.value.startAt),
        endAt: toIso(form.value.endAt),
        status: form.value.status,
        description: form.value.description,
      });
      toast.success("Tạo voucher thành công");
    }
    await loadVouchers();
    closeModal();
  } catch (e: any) {
    toast.error(e?.response?.data?.message || "Lỗi lưu voucher");
  } finally {
    saving.value = false;
  }
}

function editVoucher(v: any) {
  editingVoucher.value = v;
  form.value = {
    code: v.code,
    name: v.name,
    type: v.type,
    value: v.value,
    maxDiscount: v.maxDiscount ?? 0,
    minOrder: v.minOrder ?? 0,
    pointCost: v.pointCost ?? 0,
    startAt: toLocalInput(v.startAt),
    endAt: toLocalInput(v.endAt),
    status: v.status || "ACTIVE",
    description: v.description || "",
  };
  showEditModal.value = true;
}

function closeModal() {
  showCreateModal.value = false;
  showEditModal.value = false;
  editingVoucher.value = null;
  resetForm();
}

function resetForm() {
  const now = new Date();
  const twoHoursLater = new Date(now.getTime() + 2 * 60 * 60 * 1000);
  form.value = {
    code: "",
    name: "",
    type: "PERCENT",
    value: 10,
    maxDiscount: 0,
    minOrder: 0,
    pointCost: 0,
    startAt: toLocalInput(now.toISOString()),
    endAt: toLocalInput(twoHoursLater.toISOString()),
    status: "ACTIVE",
    description: "",
  };
}

function buildUpdatePayload(original: any, current: any): UpdateVoucherPayload {
  const payload: any = {};
  const fields = [
    "name",
    "type",
    "value",
    "minOrder",
    "maxDiscount",
    "pointCost",
    "status",
    "description",
  ];
  fields.forEach((f) => {
    if (current[f] !== undefined && current[f] !== original[f]) payload[f] = current[f];
  });
  const origStartLocal = toLocalInput(original.startAt);
  const origEndLocal = toLocalInput(original.endAt);
  if (current.startAt && current.startAt !== origStartLocal)
    payload.startAt = toIso(current.startAt);
  if (current.endAt && current.endAt !== origEndLocal) payload.endAt = toIso(current.endAt);
  return payload;
}

const formatCurrency = (amount: number) =>
  new Intl.NumberFormat("vi-VN", { style: "currency", currency: "VND" }).format(amount || 0);
const formatDate = (iso: string) => (!iso ? "—" : new Date(iso).toLocaleString());
const statusLabel = (s: string) =>
  (({ ACTIVE: "Hoạt động", INACTIVE: "Tạm dừng", DRAFT: "Nháp" } as any)[s] || s);
const statusClass = (s: string) =>
  s === "ACTIVE"
    ? "bg-emerald-500/15 text-emerald-300 border border-emerald-400/30"
    : s === "DRAFT"
    ? "bg-amber-500/15 text-amber-300 border border-amber-400/30"
    : "bg-slate-500/15 text-slate-300 border border-slate-400/30";

function toLocalInput(iso: string) {
  if (!iso) return "";
  const d = new Date(iso);
  const off = d.getTimezoneOffset();
  return new Date(d.getTime() - off * 60000).toISOString().slice(0, 16);
}
function toIso(localInput: string) {
  if (!localInput) return "";
  return new Date(localInput).toISOString();
}

onMounted(loadVouchers);
</script>
