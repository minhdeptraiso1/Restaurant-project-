<script setup lang="ts">
import { onMounted, ref, computed } from "vue";
import { listDishesAdmin, createDish, updateDish, deleteDish } from "@/api/dishes.admin";
import { listCategories } from "@/api/categories.admin";
import { toast } from "vue3-toastify";
import ConfirmModal from "@/components/ConfirmModal.vue";

type Dish = any;
type Category = any;

const dishes = ref<Dish[]>([]);
const categories = ref<Category[]>([]);

// pagination
const currentPage = ref(0);
const pageSize = ref(10);
const totalPages = ref(0);

const form = ref({
  categoryId: "",
  name: "",
  description: "",
  unit: "PORTION" as "PORTION" | "PLATE" | "BOWL" | "GLASS",
  price: 0,
  imageUrl: "",
  status: "ACTIVE" as "ACTIVE" | "INACTIVE",
  signature: false,
});
const editingId = ref<string>("");
const loading = ref(false);
const submitting = ref(false);

// image picker (ẩn)
const fileInput = ref<HTMLInputElement | null>(null);

// filters
const q = ref("");
const statusFilter = ref<"ALL" | "ACTIVE" | "INACTIVE">("ALL");
const categoryFilter = ref<string>("ALL");
const sortKey = ref<"name" | "price">("name");
const sortDir = ref<"asc" | "desc">("asc");

// Delete confirmation modal
const showDeleteModal = ref(false);
const dishToDelete = ref<{ id: string; name: string } | null>(null);
const deleting = ref(false);

const UNIT_MAP: Record<string, string> = {
  PORTION: "Phần",
  PLATE: "Dĩa",
  BOWL: "Tô",
  GLASS: "Ly",
};
function getUnitText(u: string) {
  return UNIT_MAP[u] || u;
}

const STATUS_MAP: Record<string, { label: string; color: string }> = {
  ACTIVE: { label: "Hoạt động", color: "emerald" },
  INACTIVE: { label: "Tạm dừng", color: "gray" },
};
function getStatusInfo(status: string) {
  return STATUS_MAP[status] || { label: status, color: "gray" };
}

function vnd(n: number) {
  try {
    return new Intl.NumberFormat("vi-VN").format(n) + " đ";
  } catch {
    return `${n} đ`;
  }
}

async function load() {
  loading.value = true;
  try {
    const [dishesRes, categoriesRes] = await Promise.all([
      listDishesAdmin({
        page: currentPage.value,
        size: pageSize.value,
        sortBy: sortKey.value,
        direction: sortDir.value,
      }),
      listCategories(),
    ]);

    const apiData = dishesRes.data || dishesRes;
    dishes.value = apiData.content || [];
    totalPages.value = apiData.totalPages || 0;

    categories.value = categoriesRes.data || [];
  } catch (e: any) {
    toast.error(e?.friendlyMessage || "Không tải được dữ liệu");
  } finally {
    loading.value = false;
  }
}

function validateForm() {
  if (!form.value.categoryId) return toast.error("Vui lòng chọn danh mục"), false;
  if (!form.value.name.trim()) return toast.error("Vui lòng nhập tên món"), false;
  if (form.value.price <= 0) return toast.error("Giá phải lớn hơn 0"), false;
  return true;
}

async function submit() {
  if (!validateForm()) return;
  submitting.value = true;
  try {
    if (editingId.value) {
      await updateDish(editingId.value, form.value);
      toast.success("Cập nhật món thành công");
    } else {
      await createDish(form.value);
      toast.success("Tạo món thành công");
    }
    resetForm();
    await load();
  } catch (e: any) {
    toast.error(e?.friendlyMessage || "Lỗi khi lưu món");
  } finally {
    submitting.value = false;
  }
}

function resetForm() {
  form.value = {
    categoryId: "",
    name: "",
    description: "",
    unit: "PORTION",
    price: 0,
    imageUrl: "",
    status: "ACTIVE",
    signature: false,
  };
  editingId.value = "";
}

function editDish(dish: Dish) {
  form.value = {
    categoryId: dish.categoryId,
    name: dish.name,
    description: dish.description || "",
    unit: dish.unit,
    price: Number(dish.price),
    imageUrl: dish.imageUrl || "",
    status: dish.status,
    signature: dish.signature || false,
  };
  editingId.value = dish.id;
  window.scrollTo({ top: 0, behavior: "smooth" });
}

function openDeleteModal(id: string, name: string) {
  dishToDelete.value = { id, name };
  showDeleteModal.value = true;
}

function closeDeleteModal() {
  showDeleteModal.value = false;
  dishToDelete.value = null;
}

async function confirmDeleteDish() {
  if (!dishToDelete.value) return;
  deleting.value = true;
  try {
    await deleteDish(dishToDelete.value.id);
    toast.success("Xóa món thành công");
    await load();
    closeDeleteModal();
  } catch (e: any) {
    toast.error(e?.friendlyMessage || "Lỗi khi xóa món");
  } finally {
    deleting.value = false;
  }
}

// === ẢNH: chọn file và convert => data URL ===
function triggerPickImage() {
  fileInput.value?.click();
}
function handleFileChange(ev: Event) {
  const input = ev.target as HTMLInputElement;
  const file = input.files?.[0];
  if (!file) return;

  if (!file.type.startsWith("image/")) {
    toast.error("Vui lòng chọn đúng tệp ảnh (jpg, png, webp...)");
    input.value = "";
    return;
  }
  const TWO_MB = 2 * 1024 * 1024;
  if (file.size > TWO_MB) {
    toast.warning("Ảnh > 2MB, vẫn sẽ tải nhưng có thể nặng. Hãy cân nhắc nén ảnh.");
  }

  const reader = new FileReader();
  reader.onload = () => {
    form.value.imageUrl = String(reader.result || "");
    toast.success("Đã lấy ảnh từ máy!");
  };
  reader.onerror = () => toast.error("Không đọc được tệp ảnh.");
  reader.readAsDataURL(file);
  input.value = "";
}

// ====== FILTER + SORT ======
const filtered = computed(() => {
  let data = [...dishes.value];

  const kw = q.value.trim().toLowerCase();
  if (kw) {
    data = data.filter(
      (d) => d.name?.toLowerCase().includes(kw) || d.description?.toLowerCase().includes(kw)
    );
  }

  if (statusFilter.value !== "ALL") {
    data = data.filter((d) => d.status === statusFilter.value);
  }

  if (categoryFilter.value !== "ALL") {
    data = data.filter((d) => d.categoryId === categoryFilter.value);
  }

  data.sort((a, b) => {
    const dir = sortDir.value === "asc" ? 1 : -1;
    if (sortKey.value === "name") {
      return a.name.localeCompare(b.name) * dir;
    }
    // price
    return (Number(a.price) - Number(b.price)) * dir;
  });

  return data;
});

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
        <h2 class="text-2xl md:text-3xl font-extrabold tracking-tight">🍽️ Quản lý món ăn</h2>
        <p class="mt-1 text-white/70">Thêm/sửa món, giá, đơn vị, trạng thái, hình ảnh.</p>
      </div>
    </div>

    <!-- Form tạo / sửa -->
    <div
      class="rounded-2xl bg-white/5 backdrop-blur-md border border-white/10 shadow p-6 space-y-4"
    >
      <div class="flex items-center justify-between gap-3">
        <h3 class="font-semibold text-lg">
          {{ editingId ? "Chỉnh sửa món ăn" : "Tạo món ăn mới" }}
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

      <div class="grid sm:grid-cols-2 lg:grid-cols-3 gap-3">
        <div class="relative">
          <span class="absolute left-3 top-2.5 opacity-70">🗂️</span>
          <select
            class="w-full bg-white/5 border border-white/10 rounded-lg pl-9 pr-3 py-2 outline-none focus:border-emerald-400"
            v-model="form.categoryId"
          >
            <option class="bg-[#0b1512]" value="">-- Chọn danh mục --</option>
            <option class="bg-[#0b1512]" v-for="c in categories" :key="c.id" :value="c.id">
              {{ c.name }}
            </option>
          </select>
        </div>

        <div class="relative">
          <span class="absolute left-3 top-2.5 opacity-70">🍳</span>
          <input
            class="w-full bg-white/5 border border-white/10 rounded-lg pl-9 pr-3 py-2 outline-none focus:border-emerald-400"
            placeholder="Tên món *"
            v-model="form.name"
          />
        </div>

        <div class="relative">
          <span class="absolute left-3 top-2.5 opacity-70">📝</span>
          <input
            class="w-full bg-white/5 border border-white/10 rounded-lg pl-9 pr-3 py-2 outline-none focus:border-emerald-400"
            placeholder="Mô tả"
            v-model="form.description"
          />
        </div>

        <div class="relative">
          <span class="absolute left-3 top-2.5 opacity-70">⚖️</span>
          <select
            class="w-full bg-white/5 border border-white/10 rounded-lg pl-9 pr-3 py-2 outline-none focus:border-emerald-400"
            v-model="form.unit"
          >
            <option class="bg-[#0b1512]" value="PORTION">Phần</option>
            <option class="bg-[#0b1512]" value="PLATE">Dĩa</option>
            <option class="bg-[#0b1512]" value="BOWL">Tô</option>
            <option class="bg-[#0b1512]" value="GLASS">Ly</option>
          </select>
        </div>

        <div class="relative">
          <span class="absolute left-3 top-2.5 opacity-70">💵</span>
          <input
            class="w-full bg-white/5 border border-white/10 rounded-lg pl-9 pr-3 py-2 outline-none focus:border-emerald-400"
            type="number"
            min="0"
            step="1000"
            placeholder="Giá (VNĐ) *"
            v-model.number="form.price"
          />
        </div>

        <div class="relative">
          <span class="absolute left-3 top-2.5 opacity-70">🔄</span>
          <select
            class="w-full bg-white/5 border border-white/10 rounded-lg pl-9 pr-3 py-2 outline-none focus:border-emerald-400"
            v-model="form.status"
          >
            <option class="bg-[#0b1512]" value="ACTIVE">Hoạt động</option>
            <option class="bg-[#0b1512]" value="INACTIVE">Tạm dừng</option>
          </select>
        </div>

        <!-- ✨ Món đặc trưng (Best Seller) -->
        <div class="flex items-center gap-2">
          <input
            type="checkbox"
            id="signature"
            v-model="form.signature"
            class="w-4 h-4 text-yellow-600 bg-gray-700 border-gray-600 rounded focus:ring-yellow-500 focus:ring-2"
          />
          <label
            for="signature"
            class="text-sm font-medium text-white/80 cursor-pointer flex items-center gap-1"
          >
            <span class="text-yellow-400">⭐</span> Món đặc trưng (Best Seller)
          </label>
        </div>

        <!-- Ảnh + nút chọn -->
        <div class="sm:col-span-2 lg:col-span-3">
          <label class="block text-sm text-white/70 mb-1">Ảnh món</label>
          <div class="flex flex-col sm:flex-row gap-2">
            <input
              class="w-full bg-white/5 border border-white/10 rounded-lg px-3 py-2 outline-none focus:border-emerald-400"
              placeholder="URL hình ảnh hoặc chọn từ máy"
              v-model="form.imageUrl"
            />
            <button
              type="button"
              class="shrink-0 px-3 py-2 rounded-lg bg-emerald-600 hover:bg-emerald-700 border border-emerald-400/30 transition"
              @click="triggerPickImage"
            >
              📁 Chọn ảnh từ máy
            </button>
            <input
              ref="fileInput"
              type="file"
              accept="image/*"
              class="hidden"
              @change="handleFileChange"
            />
          </div>

          <!-- Preview -->
          <div v-if="form.imageUrl" class="mt-3 flex items-center gap-3">
            <img
              :src="form.imageUrl"
              alt="preview"
              class="w-20 h-20 object-cover rounded-lg border border-white/10"
            />
            <div class="text-xs text-white/60">
              Dung lượng lớn có thể làm chậm tải trang. Khuyến nghị nén ảnh trước khi lưu.
            </div>
          </div>
        </div>
      </div>

      <div class="pt-2 flex items-center gap-2">
        <button
          class="px-4 py-2 rounded-lg bg-emerald-600 hover:bg-emerald-700 transition disabled:opacity-50 disabled:cursor-not-allowed"
          :disabled="submitting"
          @click="submit"
        >
          <span v-if="submitting" class="inline-flex items-center gap-2">
            <span
              class="w-4 h-4 inline-block rounded-full border-2 border-white border-t-transparent animate-spin"
            ></span>
            Đang lưu...
          </span>
          <span v-else>{{ editingId ? "Cập nhật" : "Tạo món" }}</span>
        </button>

        <button
          v-if="editingId"
          class="px-4 py-2 rounded-lg bg-white/10 hover:bg-white/15 border border-white/10"
          @click="resetForm"
        >
          Hủy
        </button>
      </div>
    </div>

    <!-- Bộ lọc + sắp xếp -->
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
          <span class="absolute left-3 top-2.5 opacity-70">🗂️</span>
          <select
            class="w-full bg-white/5 border border-white/10 rounded-lg pl-9 pr-3 py-2 outline-none focus:border-emerald-400"
            v-model="categoryFilter"
          >
            <option class="bg-[#0b1512]" value="ALL">Tất cả danh mục</option>
            <option class="bg-[#0b1512]" v-for="c in categories" :key="c.id" :value="c.id">
              {{ c.name }}
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
            <option class="bg-[#0b1512]" value="ACTIVE">Hoạt động</option>
            <option class="bg-[#0b1512]" value="INACTIVE">Tạm dừng</option>
          </select>
        </div>
        <div class="flex gap-2">
          <div class="relative grow">
            <span class="absolute left-3 top-2.5 opacity-70">↕️</span>
            <select
              class="w-full bg-white/5 border border-white/10 rounded-lg pl-9 pr-3 py-2 outline-none focus:border-emerald-400"
              v-model="sortKey"
            >
              <option class="bg-[#0b1512]" value="name">Sắp xếp theo tên</option>
              <option class="bg-[#0b1512]" value="price">Sắp xếp theo giá</option>
            </select>
          </div>
          <button
            class="px-3 rounded-lg bg-white/10 hover:bg-white/15 border border-white/10"
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
      <h3 class="font-semibold text-lg mb-3">
        Danh sách món <span class="text-white/60">({{ filtered.length }})</span>
      </h3>

      <div v-if="loading" class="text-white/70 text-center py-4">Đang tải...</div>
      <div v-else-if="!filtered.length" class="text-white/60 text-center py-6">
        Không có món nào phù hợp.
      </div>

      <div v-else class="overflow-x-auto">
        <table class="min-w-full text-sm">
          <thead>
            <tr class="text-left text-white/70 border-b border-white/10">
              <th class="py-3 px-2">Hình ảnh</th>
              <th class="py-3 px-2">Tên món</th>
              <th class="py-3 px-2">Danh mục</th>
              <th class="py-3 px-2">Đơn vị</th>
              <th class="py-3 px-2">Giá</th>
              <th class="py-3 px-2">Trạng thái</th>
              <th class="py-3 px-2">Thao tác</th>
            </tr>
          </thead>
          <tbody>
            <tr
              v-for="d in filtered"
              :key="d.id"
              class="border-b border-white/5 hover:bg-white/5 transition"
            >
              <td class="py-3 px-2">
                <img
                  v-if="d.imageUrl"
                  :src="d.imageUrl"
                  class="w-12 h-12 object-cover rounded-lg border border-white/10"
                  :alt="d.name"
                />
                <div
                  v-else
                  class="w-12 h-12 bg-white/10 rounded-lg border border-white/10 grid place-items-center text-white/50 text-[10px]"
                >
                  No img
                </div>
              </td>
              <td class="py-3 px-2 font-medium">
                <div class="flex items-center gap-2">
                  <span>🍳</span>
                  <span>{{ d.name }}</span>
                </div>
              </td>
              <td class="py-3 px-2">
                {{ categories.find((c) => c.id === d.categoryId)?.name || "N/A" }}
              </td>
              <td class="py-3 px-2">
                <span class="px-2 py-1 rounded-lg text-xs bg-white/10 border border-white/10">
                  {{ getUnitText(d.unit) }}
                </span>
              </td>
              <td class="py-3 px-2 font-semibold">
                {{ vnd(Number(d.price)) }}
              </td>
              <td class="py-3 px-2">
                <span
                  class="px-2 py-1 rounded-lg text-xs"
                  :class="
                    d.status === 'ACTIVE'
                      ? 'bg-emerald-500/15 text-emerald-300 border border-emerald-400/30'
                      : 'bg-rose-500/15 text-rose-300 border border-rose-400/30'
                  "
                >
                  {{ d.status === "ACTIVE" ? "Hoạt động" : "Tạm dừng" }}
                </span>
              </td>
              <td class="py-3 px-2">
                <div class="flex flex-wrap gap-1">
                  <button
                    class="px-2 py-1 text-xs rounded-lg border border-blue-400/40 text-blue-300 hover:bg-blue-500/10"
                    @click="editDish(d)"
                  >
                    ✏️ Sửa
                  </button>
                  <button
                    class="px-2 py-1 text-xs rounded-lg border border-rose-400/40 text-rose-300 hover:bg-rose-500/10"
                    @click="openDeleteModal(d.id, d.name)"
                  >
                    🗑️ Xóa
                  </button>
                </div>
              </td>
            </tr>
          </tbody>
        </table>
      </div>

      <!-- Pagination -->
      <div class="flex items-center justify-between gap-4 mt-4 px-2">
        <div class="text-sm text-white/70">Trang {{ currentPage + 1 }} / {{ totalPages || 1 }}</div>
        <div class="flex gap-2">
          <button
            class="px-3 py-1.5 text-xs rounded-lg bg-white/10 border border-white/10 hover:bg-white/15 disabled:opacity-50 disabled:cursor-not-allowed"
            :disabled="currentPage === 0"
            @click="
              currentPage--;
              load();
            "
          >
            ← Trước
          </button>
          <button
            class="px-3 py-1.5 text-xs rounded-lg bg-white/10 border border-white/10 hover:bg-white/15 disabled:opacity-50 disabled:cursor-not-allowed"
            :disabled="currentPage >= totalPages - 1"
            @click="
              currentPage++;
              load();
            "
          >
            Sau →
          </button>
        </div>
      </div>
    </div>
  </div>

  <!-- Confirm Delete Modal -->
  <ConfirmModal
    :show="showDeleteModal"
    :message="`Bạn có chắc chắn muốn xóa món ăn này không?`"
    :itemName="dishToDelete?.name"
    :loading="deleting"
    @confirm="confirmDeleteDish"
    @cancel="closeDeleteModal"
  />
</template>
