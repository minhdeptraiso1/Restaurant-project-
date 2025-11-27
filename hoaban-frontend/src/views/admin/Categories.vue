<script setup lang="ts">
import { onMounted, ref, computed } from "vue";
import { listCategories, createCategory, updateCategory } from "@/api/categories.admin";
import { toast } from "vue3-toastify";

type Category = {
  id: string;
  name: string;
  description?: string;
  status?: string;
  createdAt?: string;
  updatedAt?: string;
};

const categories = ref<Category[]>([]);
const form = ref<{ name: string; description: string; status: string }>({
  name: "",
  description: "",
  status: "ACTIVE",
});
const editingId = ref<string>("");
const loading = ref(false);
const submitting = ref(false);

// search
const q = ref("");

const filtered = computed(() => {
  const kw = q.value.trim().toLowerCase();
  if (!kw) return categories.value;
  return categories.value.filter(
    (c) => c.name?.toLowerCase().includes(kw) || c.id?.toLowerCase().includes(kw)
  );
});

async function load() {
  loading.value = true;
  try {
    const { data } = await listCategories();
    categories.value = data || [];
  } catch (e: any) {
    toast.error(e?.friendlyMessage || "Không tải được danh mục");
  } finally {
    loading.value = false;
  }
}

function resetForm() {
  form.value = { name: "", description: "", status: "ACTIVE" };
  editingId.value = "";
}

async function submit() {
  const name = form.value.name.trim();
  if (!name) {
    toast.warning("Vui lòng nhập tên danh mục");
    return;
  }
  // chống trùng tên đơn giản ở client
  const exists = categories.value.some(
    (c) => c.name.trim().toLowerCase() === name.toLowerCase() && c.id !== editingId.value
  );
  if (exists) {
    toast.info("Tên danh mục đã tồn tại");
    return;
  }

  submitting.value = true;
  try {
    const payload = {
      name,
      description: form.value.description.trim() || undefined,
      status: form.value.status,
    };
    if (editingId.value) {
      await updateCategory(editingId.value, payload);
      toast.success("Cập nhật danh mục thành công");
    } else {
      await createCategory(payload);
      toast.success("Tạo danh mục thành công");
    }
    resetForm();
    await load();
  } catch (e: any) {
    toast.error(e?.friendlyMessage || "Lỗi khi lưu danh mục");
  } finally {
    submitting.value = false;
  }
}

function editCategory(c: Category) {
  form.value = {
    name: c.name,
    description: c.description || "",
    status: c.status || "ACTIVE",
  };
  editingId.value = c.id;
  window.scrollTo({ top: 0, behavior: "smooth" });
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
        <h2 class="text-2xl md:text-3xl font-extrabold tracking-tight">🗂️ Quản lý danh mục</h2>
        <p class="mt-1 text-white/70">Tạo/sửa danh mục món ăn – gọn gàng, trực quan.</p>
      </div>
    </div>

    <!-- Form -->
    <div class="rounded-2xl bg-white/5 backdrop-blur-md border border-white/10 shadow p-6">
      <div class="flex items-center justify-between gap-3 mb-3">
        <h3 class="font-semibold text-lg">
          {{ editingId ? "Chỉnh sửa danh mục" : "Tạo danh mục mới" }}
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

      <div class="space-y-4 max-w-3xl">
        <div class="grid grid-cols-1 md:grid-cols-2 gap-3">
          <div class="relative">
            <span class="absolute left-3 top-2.5 opacity-70">🏷️</span>
            <input
              class="w-full bg-white/5 border border-white/10 rounded-lg pl-9 pr-3 py-2 outline-none focus:border-emerald-400"
              placeholder="Tên danh mục *"
              v-model="form.name"
            />
          </div>

          <div class="relative">
            <span class="absolute left-3 top-2.5 opacity-70">🔄</span>
            <select
              class="w-full bg-white/5 border border-white/10 rounded-lg pl-9 pr-3 py-2 outline-none focus:border-emerald-400"
              v-model="form.status"
            >
              <option class="bg-[#0b1512]" value="ACTIVE">Hoạt động</option>
              <option class="bg-[#0b1512]" value="INACTIVE">Tạm ngưng</option>
            </select>
          </div>
        </div>

        <div class="relative">
          <span class="absolute left-3 top-2.5 opacity-70">📝</span>
          <textarea
            class="w-full bg-white/5 border border-white/10 rounded-lg pl-9 pr-3 py-2 outline-none focus:border-emerald-400 resize-none"
            placeholder="Mô tả (tùy chọn)"
            v-model="form.description"
            rows="3"
          ></textarea>
        </div>

        <button
          class="px-6 py-2 rounded-lg bg-emerald-600 hover:bg-emerald-700 transition disabled:opacity-50 disabled:cursor-not-allowed"
          @click="submit"
          :disabled="submitting || !form.name.trim()"
        >
          <span v-if="submitting" class="inline-flex items-center gap-2">
            <span
              class="w-4 h-4 inline-block rounded-full border-2 border-white border-t-transparent animate-spin"
            ></span>
            Đang lưu...
          </span>
          <span v-else>{{ editingId ? "Cập nhật" : "Tạo danh mục" }}</span>
        </button>
      </div>
    </div>

    <!-- Bộ lọc + đếm -->
    <div class="rounded-2xl bg-white/5 backdrop-blur-md border border-white/10 shadow p-6">
      <div class="flex flex-col md:flex-row md:items-center md:justify-between gap-3">
        <h3 class="font-semibold text-lg">
          Danh sách danh mục
          <span class="text-white/60">({{ filtered.length }})</span>
        </h3>

        <div class="relative w-full md:w-80">
          <span class="absolute left-3 top-2.5 opacity-70">🔎</span>
          <input
            class="w-full bg-white/5 border border-white/10 rounded-lg pl-9 pr-3 py-2 outline-none focus:border-emerald-400"
            placeholder="Tìm theo tên hoặc ID…"
            v-model="q"
          />
        </div>
      </div>
    </div>

    <!-- Bảng -->
    <div class="rounded-2xl bg-white/5 backdrop-blur-md border border-white/10 shadow p-6">
      <div v-if="loading" class="text-white/70 text-center py-4">Đang tải...</div>

      <div v-else-if="!filtered.length" class="text-white/60 text-center py-6">
        Không có danh mục nào phù hợp.
      </div>

      <div v-else class="overflow-x-auto">
        <table class="min-w-full text-sm">
          <thead>
            <tr class="text-left text-white/70 border-b border-white/10">
              <th class="py-3 px-4">Tên danh mục</th>
              <th class="py-3 px-4">Mô tả</th>
              <th class="py-3 px-4">Trạng thái</th>
              <th class="py-3 px-4">Thao tác</th>
            </tr>
          </thead>
          <tbody>
            <tr
              v-for="c in filtered"
              :key="c.id"
              class="border-b border-white/5 hover:bg-white/5 transition"
            >
              <td class="py-3 px-4 font-medium">
                <div class="flex items-center gap-2">
                  <span>🏷️</span>
                  <span>{{ c.name }}</span>
                </div>
              </td>
              <td class="py-3 px-4 text-white/70 text-sm">
                {{ c.description || "—" }}
              </td>
              <td class="py-3 px-4">
                <span
                  class="px-2 py-1 rounded-lg text-xs"
                  :class="
                    c.status === 'ACTIVE'
                      ? 'bg-emerald-500/15 text-emerald-300 border border-emerald-400/30'
                      : 'bg-gray-500/15 text-gray-300 border border-gray-400/30'
                  "
                >
                  {{ c.status === "ACTIVE" ? "Hoạt động" : "Tạm ngưng" }}
                </span>
              </td>
              <td class="py-3 px-4">
                <button
                  class="px-3 py-1.5 text-xs rounded-lg border border-blue-400/40 text-blue-300 hover:bg-blue-500/10"
                  @click="editCategory(c)"
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
