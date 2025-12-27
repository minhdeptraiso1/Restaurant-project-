<script setup lang="ts">
import { onMounted, ref, computed } from "vue";
import { listAreas, createArea, updateArea } from "@/api/areas.admin";
import { toast } from "vue3-toastify";

type Area = {
  id: string;
  name: string;
  description?: string;
  status: "ACTIVE" | "INACTIVE";
  createdAt?: string;
  updatedAt?: string;
};

const areas = ref<Area[]>([]);
const loading = ref(false);
const submitting = ref(false);

const form = ref<{ id?: string; name: string; description: string; status: "ACTIVE" | "INACTIVE" }>(
  {
    name: "",
    description: "",
    status: "ACTIVE",
  }
);

const q = ref("");
const statusFilter = ref<"ALL" | "ACTIVE" | "INACTIVE">("ALL");
const editingId = ref<string>("");

const isValid = computed(() => form.value.name.trim().length >= 2);

async function load() {
  loading.value = true;
  try {
    const { data } = await listAreas();
    areas.value = data || [];
  } catch (e: any) {
    toast.error(e?.friendlyMessage || "Không tải được khu vực");
  } finally {
    loading.value = false;
  }
}

function startEdit(a: Area) {
  form.value = {
    id: a.id,
    name: a.name,
    description: a.description || "",
    status: a.status,
  };
  editingId.value = a.id;
  window.scrollTo({ top: 0, behavior: "smooth" });
}

function resetForm() {
  form.value = { name: "", description: "", status: "ACTIVE" };
  editingId.value = "";
}

async function submit() {
  if (!isValid.value) {
    toast.warning("Tên khu vực tối thiểu 2 ký tự");
    return;
  }
  submitting.value = true;
  try {
    if (editingId.value) {
      // cập nhật
      await updateArea(editingId.value, {
        name: form.value.name,
        description: form.value.description,
        status: form.value.status,
      });
      toast.success("Cập nhật khu vực thành công");
    } else {
      // tạo mới
      await createArea({
        name: form.value.name,
        description: form.value.description,
        status: form.value.status,
      });
      toast.success("Tạo khu vực thành công");
    }
    resetForm();
    await load();
  } catch (e: any) {
    toast.error(e?.friendlyMessage || "Lưu khu vực thất bại");
  } finally {
    submitting.value = false;
  }
}

const filtered = computed(() => {
  let data = [...areas.value];
  const kw = q.value.trim().toLowerCase();
  if (kw) {
    data = data.filter(
      (a) => a.name?.toLowerCase().includes(kw) || a.description?.toLowerCase().includes(kw)
    );
  }
  if (statusFilter.value !== "ALL") {
    data = data.filter((a) => a.status === statusFilter.value);
  }
  // sort theo tên
  data.sort((a, b) => a.name.localeCompare(b.name));
  return data;
});

function statusBadge(status?: string) {
  switch (status) {
    case "ACTIVE":
      return "bg-emerald-500/15 text-emerald-300 border border-emerald-400/30";
    case "INACTIVE":
      return "bg-rose-500/15 text-rose-300 border border-rose-400/30";
    default:
      return "bg-white/10 text-white/70 border border-white/10";
  }
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
        class="absolute -bottom-16 -right-10 h-56 w-56 rounded-full bg-amber-500/20 blur-3xl"
      ></div>
      <div class="relative z-10">
        <h2 class="text-2xl md:text-3xl font-extrabold tracking-tight">🏢 Quản lý khu vực</h2>
        <p class="mt-1 text-white/70">Tạo/sửa khu vực nhà hàng, bật/tắt trạng thái hoạt động.</p>
      </div>
    </div>

    <!-- Form -->
    <div
      class="rounded-2xl bg-white/5 backdrop-blur-md border border-white/10 shadow p-6 space-y-4"
    >
      <div class="flex items-center justify-between">
        <h3 class="font-semibold text-lg">
          {{ editingId ? "Chỉnh sửa khu vực" : "Tạo khu vực mới" }}
        </h3>
        <button
          v-if="editingId"
          class="text-xs px-3 py-1.5 rounded-lg bg-white/10 hover:bg-white/15 border border-white/10"
          @click="resetForm"
          title="Hủy chỉnh sửa"
        >
          ✖ Hủy
        </button>
      </div>

      <div class="grid sm:grid-cols-3 gap-3">
        <div class="relative">
          <span class="absolute left-3 top-2.5 opacity-70">🏷️</span>
          <input
            class="w-full bg-white/5 border border-white/10 rounded-lg pl-9 pr-3 py-2 outline-none focus:border-emerald-400"
            placeholder="Tên khu vực *"
            v-model="form.name"
          />
        </div>

        <div class="relative sm:col-span-2">
          <span class="absolute left-3 top-2.5 opacity-70">📝</span>
          <input
            class="w-full bg-white/5 border border-white/10 rounded-lg pl-9 pr-3 py-2 outline-none focus:border-emerald-400"
            placeholder="Mô tả"
            v-model="form.description"
          />
        </div>

        <div class="relative">
          <span class="absolute left-3 top-2.5 opacity-70">🔖</span>
          <select
            class="w-full bg-white/5 border border-white/10 rounded-lg pl-9 pr-3 py-2 outline-none focus:border-emerald-400"
            v-model="form.status"
          >
            <option class="bg-[#0b1512]" value="ACTIVE">Hoạt động</option>
            <option class="bg-[#0b1512]" value="INACTIVE">Tạm dừng</option>
          </select>
        </div>
      </div>

      <div class="pt-2">
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
          <span v-else>{{ editingId ? "Cập nhật khu vực" : "Tạo khu vực" }}</span>
        </button>
        <span v-if="!isValid" class="ml-3 text-xs text-white/60">Tên khu vực ≥ 2 ký tự</span>
      </div>
    </div>

    <!-- Bộ lọc -->
    <div class="rounded-2xl bg-white/5 backdrop-blur-md border border-white/10 shadow p-6">
      <div class="grid md:grid-cols-2 lg:grid-cols-4 gap-3">
        <div class="relative">
          <span class="absolute left-3 top-2.5 opacity-70">🔎</span>
          <input
            class="w-full bg-white/5 border border-white/10 rounded-lg pl-9 pr-3 py-2 outline-none focus:border-emerald-400"
            placeholder="Tìm theo tên hoặc mô tả…"
            v-model="q"
          />
        </div>
        <div class="relative">
          <span class="absolute left-3 top-2.5 opacity-70">🔖</span>
          <select
            class="w-full bg-white/5 border border-white/10 rounded-lg pl-9 pr-3 py-2 outline-none focus:border-emerald-400"
            v-model="statusFilter"
          >
            <option class="bg-[#0b1512]" value="ALL">Tất cả trạng thái</option>
            <option class="bg-[#0b1512]" value="ACTIVE">Hoạt động</option>
            <option class="bg-[#0b1512]" value="INACTIVE">Tạm dừng</option>
          </select>
        </div>
      </div>
    </div>

    <!-- Danh sách -->
    <div class="rounded-2xl bg-white/5 backdrop-blur-md border border-white/10 shadow p-6">
      <h3 class="font-semibold text-lg mb-3">
        Danh sách khu vực <span class="text-white/60">({{ filtered.length }})</span>
      </h3>

      <div v-if="loading" class="text-white/70 text-center py-4">Đang tải...</div>
      <div v-else-if="!filtered.length" class="text-white/60 text-center py-6">
        Không có khu vực nào phù hợp.
      </div>

      <div v-else class="overflow-x-auto">
        <table class="min-w-full text-sm">
          <thead>
            <tr class="text-left text-white/70 border-b border-white/10">
              <th class="py-3 px-2">Tên</th>
              <th class="py-3 px-2">Mô tả</th>
              <th class="py-3 px-2">Trạng thái</th>
              <th class="py-3 px-2">Thao tác</th>
            </tr>
          </thead>
          <tbody>
            <tr
              v-for="a in filtered"
              :key="a.id"
              class="border-b border-white/5 hover:bg-white/5 transition"
            >
              <td class="py-3 px-2 font-medium">
                <div class="flex items-center gap-2">
                  <span>🏷️</span>
                  <span>{{ a.name }}</span>
                </div>
              </td>
              <td class="py-3 px-2">
                <span class="text-white/80">{{ a.description || "—" }}</span>
              </td>
              <td class="py-3 px-2">
                <span class="px-2 py-1 rounded-lg text-xs" :class="statusBadge(a.status)">
                  {{ a.status === "ACTIVE" ? "Hoạt động" : "Tạm dừng" }}
                </span>
              </td>
              <td class="py-3 px-2">
                <button
                  class="px-2 py-1 text-xs rounded-lg border border-blue-400/40 text-blue-300 hover:bg-blue-500/10"
                  @click="startEdit(a)"
                >
                  ✏️ Sửa
                </button>
              </td>
            </tr>
          </tbody>
        </table>
      </div>
    </div>
  </div>
</template>
