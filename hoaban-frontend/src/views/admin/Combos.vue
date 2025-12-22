<script setup lang="ts">
import { onMounted, ref, computed } from "vue";
import { listCombos, createCombo, updateCombo, deleteCombo } from "@/api/combos.admin";
import { listDishesAdmin } from "@/api/dishes.admin";
import { toast } from "vue3-toastify";
import ConfirmModal from "@/components/ConfirmModal.vue";

type Dish = any;
type Combo = any;

const combos = ref<Combo[]>([]);
const dishes = ref<Dish[]>([]);
const loading = ref(false);
const submitting = ref(false);

// Delete confirmation modal
const showDeleteModal = ref(false);
const comboToDelete = ref<Combo | null>(null);
const deleting = ref(false);

// ----- FORM -----
const form = ref({
  name: "",
  description: "",
  price: 0,
  imageUrl: "",
  status: "ACTIVE",
  items: [] as Array<{ dishId: string; quantity: number }>,
});
const selectedDishId = ref("");
const selectedQuantity = ref(1);
const editingId = ref<string>("");

// ảnh từ máy (optional)
const fileInput = ref<HTMLInputElement | null>(null);

const totalPrice = computed(() =>
  form.value.items.reduce((total, it) => {
    const dish = dishes.value.find((d) => d.id === it.dishId);
    return total + (dish ? Number(dish.price) * it.quantity : 0);
  }, 0)
);

// ----- LIST FILTERS -----
const q = ref("");
const statusFilter = ref<"ALL" | "ACTIVE" | "INACTIVE">("ALL");

const filteredCombos = computed(() => {
  let data = [...combos.value];
  const kw = q.value.trim().toLowerCase();
  if (kw) {
    data = data.filter(
      (c) => c.name?.toLowerCase().includes(kw) || c.description?.toLowerCase().includes(kw)
    );
  }
  if (statusFilter.value !== "ALL") {
    data = data.filter((c) => c.status === statusFilter.value);
  }
  // an toàn hơn khi name bị null/undefined
  data.sort((a, b) => (a?.name || "").localeCompare(b?.name || ""));
  return data;
});

// ----- LOAD -----
async function load() {
  loading.value = true;
  try {
    const [combosRes, dishesRes] = await Promise.all([
      listCombos(),
      listDishesAdmin({ size: 1000 }), // Lấy tất cả món ăn (tối đa 1000)
    ]);
    combos.value = combosRes.data || [];
    // API trả về dạng paginated {content: [...]}
    dishes.value = dishesRes.data?.content || [];
  } catch (e: any) {
    toast.error(e?.friendlyMessage || "Không tải được dữ liệu");
  } finally {
    loading.value = false;
  }
}

// ----- FORM HELPERS -----
function addItem() {
  if (!selectedDishId.value) return toast.error("Vui lòng chọn món ăn");
  if (selectedQuantity.value < 1) return toast.error("Số lượng phải ≥ 1");

  const exist = form.value.items.find((it) => it.dishId === selectedDishId.value);
  if (exist) exist.quantity += selectedQuantity.value;
  else form.value.items.push({ dishId: selectedDishId.value, quantity: selectedQuantity.value });

  selectedDishId.value = "";
  selectedQuantity.value = 1;
}
function removeItem(i: number) {
  form.value.items.splice(i, 1);
}
function dishName(id: string) {
  return dishes.value.find((d) => d.id === id)?.name || "Không tìm thấy";
}
function dishPrice(id: string) {
  return Number(dishes.value.find((d) => d.id === id)?.price || 0);
}
function vnd(n: number) {
  try {
    return new Intl.NumberFormat("vi-VN").format(n) + "đ";
  } catch {
    return `${n}đ`;
  }
}

// ----- IMAGE PICKER (DATA URL) -----
function pickImage() {
  fileInput.value?.click();
}
function handleFileChange(ev: Event) {
  const input = ev.target as HTMLInputElement;
  const file = input.files?.[0];
  if (!file) return;
  if (!file.type.startsWith("image/")) {
    toast.error("Chỉ nhận file ảnh (jpg, png, webp...)");
    input.value = "";
    return;
  }
  const TWO_MB = 2 * 1024 * 1024;
  if (file.size > TWO_MB) toast.warning("Ảnh > 2MB, cân nhắc nén trước khi dùng.");

  const reader = new FileReader();
  reader.onload = () => {
    form.value.imageUrl = String(reader.result || "");
    toast.success("Đã chọn ảnh từ máy!");
  };
  reader.onerror = () => toast.error("Không đọc được tệp ảnh.");
  reader.readAsDataURL(file);
  input.value = "";
}

// ----- EDIT / RESET -----
function startEdit(c: Combo) {
  editingId.value = c.id;
  form.value = {
    name: c.name || "",
    description: c.description || "",
    price: Number(c.price || 0),
    imageUrl: c.imageUrl || "",
    status: c.status || "ACTIVE",
    items: (c.items || []).map((it: any) => ({
      dishId: it.dishId,
      quantity: Number(it.quantity || 1),
    })),
  };
  window.scrollTo({ top: 0, behavior: "smooth" });
}
function resetForm() {
  editingId.value = "";
  form.value = { name: "", description: "", price: 0, imageUrl: "", status: "ACTIVE", items: [] };
  selectedDishId.value = "";
  selectedQuantity.value = 1;
}

// ----- SUBMIT -----
function validate() {
  if (!form.value.name.trim()) return toast.error("Nhập tên combo"), false;
  if (form.value.price <= 0) return toast.error("Giá phải > 0"), false;
  if (!form.value.items.length) return toast.error("Combo phải có ít nhất 1 món"), false;
  return true;
}
async function submit() {
  if (!validate()) return;
  submitting.value = true;
  try {
    if (editingId.value) {
      await updateCombo(editingId.value, form.value);
      toast.success("Cập nhật combo thành công");
    } else {
      await createCombo(form.value);
      toast.success("Tạo combo thành công");
    }
    resetForm();
    await load();
  } catch (e: any) {
    toast.error(e?.friendlyMessage || "Lưu combo thất bại");
  } finally {
    submitting.value = false;
  }
}

// Delete functions
function openDeleteModal(combo: Combo) {
  comboToDelete.value = combo;
  showDeleteModal.value = true;
}

function closeDeleteModal() {
  showDeleteModal.value = false;
  comboToDelete.value = null;
}

async function confirmDeleteCombo() {
  if (!comboToDelete.value) return;
  deleting.value = true;
  try {
    await deleteCombo(comboToDelete.value.id);
    toast.success("Đã xóa combo");
    await load();
    closeDeleteModal();
  } catch (e: any) {
    toast.error(e?.friendlyMessage || "Xóa combo thất bại");
  } finally {
    deleting.value = false;
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
        <h2 class="text-2xl md:text-3xl font-extrabold tracking-tight">🧩 Quản lý combo</h2>
        <p class="mt-1 text-white/70">
          Tạo combo từ nhiều món ăn với giá ưu đãi, ảnh & trạng thái.
        </p>
      </div>
    </div>

    <!-- FORM -->
    <div
      class="rounded-2xl bg-white/5 backdrop-blur-md border border-white/10 shadow p-6 space-y-5"
    >
      <div class="flex items-center justify-between gap-3">
        <h3 class="font-semibold text-lg">{{ editingId ? "Chỉnh sửa combo" : "Tạo combo mới" }}</h3>
        <button
          v-if="editingId"
          class="text-xs px-3 py-1.5 rounded-lg bg-white/10 hover:bg-white/15 border border-white/10"
          @click="resetForm"
        >
          ✖ Hủy
        </button>
      </div>

      <!-- Basic -->
      <div class="grid grid-cols-1 lg:grid-cols-2 gap-4">
        <div class="space-y-3">
          <div class="relative">
            <span class="absolute left-3 top-2.5 opacity-70">🏷️</span>
            <input
              class="w-full bg-white/5 border border-white/10 rounded-lg pl-9 pr-3 py-2 outline-none focus:border-emerald-400"
              placeholder="Tên combo *"
              v-model="form.name"
            />
          </div>

          <div class="relative">
            <span class="absolute left-3 top-2.5 opacity-70">📝</span>
            <textarea
              class="w-full bg-white/5 border border-white/10 rounded-lg pl-9 pr-3 py-2 outline-none focus:border-emerald-400 min-h-[90px]"
              placeholder="Mô tả"
              v-model="form.description"
            ></textarea>
          </div>

          <div class="grid sm:grid-cols-2 gap-3">
            <div class="relative">
              <span class="absolute left-3 top-2.5 opacity-70">💴</span>
              <input
                class="w-full bg-white/5 border border-white/10 rounded-lg pl-9 pr-3 py-2 outline-none focus:border-emerald-400"
                type="number"
                min="0"
                step="1000"
                placeholder="Giá combo *"
                v-model.number="form.price"
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

          <!-- Ảnh -->
          <div class="space-y-2">
            <label class="block text-sm text-white/70">Hình ảnh</label>
            <div class="flex flex-col sm:flex-row gap-2">
              <input
                class="w-full bg-white/5 border border-white/10 rounded-lg px-3 py-2 outline-none focus:border-emerald-400"
                placeholder="URL ảnh hoặc chọn từ máy"
                v-model="form.imageUrl"
              />
              <button
                class="shrink-0 px-3 py-2 rounded-lg bg-emerald-600 hover:bg-emerald-700 border border-emerald-400/30 transition"
                type="button"
                @click="pickImage"
              >
                📁 Chọn ảnh
              </button>
              <input
                ref="fileInput"
                class="hidden"
                type="file"
                accept="image/*"
                @change="handleFileChange"
              />
            </div>
            <div v-if="form.imageUrl" class="flex items-center gap-3">
              <img
                :src="form.imageUrl"
                alt="preview"
                class="w-20 h-20 object-cover rounded-lg border border-white/10"
              />
              <div class="text-xs text-white/60">
                Khuyến nghị nén ảnh trước khi dùng cho hiệu năng.
              </div>
            </div>
          </div>
        </div>

        <!-- Items -->
        <div class="space-y-3">
          <div class="flex gap-2">
            <div class="relative grow">
              <span class="absolute left-3 top-2.5 opacity-70">🍽️</span>
              <select
                class="w-full bg-white/5 border border-white/10 rounded-lg pl-9 pr-3 py-2 outline-none focus:border-emerald-400"
                v-model="selectedDishId"
              >
                <option class="bg-[#0b1512]" value="">-- Chọn món ăn --</option>
                <option class="bg-[#0b1512]" v-for="d in dishes" :key="d.id" :value="d.id">
                  {{ d.name }} — {{ vnd(Number(d.price)) }}
                </option>
              </select>
            </div>
            <input
              class="w-24 bg-white/5 border border-white/10 rounded-lg px-3 py-2 outline-none focus:border-emerald-400"
              type="number"
              min="1"
              placeholder="SL"
              v-model.number="selectedQuantity"
            />
            <button
              class="px-3 py-2 rounded-lg bg-emerald-600 hover:bg-emerald-700"
              @click="addItem"
            >
              ➕ Thêm
            </button>
          </div>

          <div class="space-y-2">
            <!-- đã fix: dùng form.items.length -->
            <div v-if="!form.items.length" class="text-white/60 text-sm">
              Chưa có món nào trong combo.
            </div>

            <div
              v-for="(it, idx) in form.items"
              :key="idx"
              class="flex items-center justify-between rounded-lg border border-white/10 bg-white/5 px-3 py-2"
            >
              <div class="flex-1">
                <div class="font-medium">{{ dishName(it.dishId) }}</div>
                <div class="text-xs text-white/60">
                  {{ vnd(dishPrice(it.dishId)) }} × {{ it.quantity }} =
                  <span class="font-semibold">{{ vnd(dishPrice(it.dishId) * it.quantity) }}</span>
                </div>
              </div>
              <button
                class="px-2 py-1 text-xs rounded-lg border border-rose-400/40 text-rose-300 hover:bg-rose-500/10"
                @click="removeItem(idx)"
              >
                🗑️ Xoá
              </button>
            </div>

            <div
              v-if="form.items.length"
              class="rounded-lg bg-emerald-500/10 border border-emerald-400/30 p-3"
            >
              <div class="text-sm text-white/80">
                Tổng giá món: <b>{{ vnd(totalPrice) }}</b>
              </div>
              <div class="text-sm text-white/80">
                Giá combo: <b>{{ vnd(Number(form.price)) }}</b>
              </div>
              <div v-if="Number(form.price) < totalPrice" class="text-emerald-300 text-sm">
                🎉 Tiết kiệm: <b>{{ vnd(totalPrice - Number(form.price)) }}</b>
              </div>
              <div v-else class="text-amber-300 text-sm">
                ℹ️ Giá combo chưa rẻ hơn tổng giá món.
              </div>
            </div>
          </div>
        </div>
      </div>

      <!-- Submit -->
      <div class="pt-2 flex items-center gap-2">
        <button
          class="px-5 py-2 rounded-lg bg-emerald-600 hover:bg-emerald-700 transition disabled:opacity-50 disabled:cursor-not-allowed"
          :disabled="submitting"
          @click="submit"
        >
          <span v-if="submitting" class="inline-flex items-center gap-2">
            <span
              class="w-4 h-4 inline-block rounded-full border-2 border-white border-t-transparent animate-spin"
            ></span>
            Đang lưu...
          </span>
          <span v-else>{{ editingId ? "Cập nhật combo" : "Tạo combo" }}</span>
        </button>
        <button
          class="px-5 py-2 rounded-lg bg-white/10 hover:bg-white/15 border border-white/10"
          @click="resetForm"
        >
          Reset
        </button>
      </div>
    </div>

    <!-- FILTERS + LIST -->
    <div class="rounded-2xl bg-white/5 backdrop-blur-md border border-white/10 shadow p-6">
      <div class="grid md:grid-cols-2 lg:grid-cols-4 gap-3 mb-4">
        <div class="relative">
          <span class="absolute left-3 top-2.5 opacity-70">🔎</span>
          <input
            class="w-full bg-white/5 border border-white/10 rounded-lg pl-9 pr-3 py-2 outline-none focus:border-emerald-400"
            placeholder="Tìm theo tên / mô tả…"
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

      <div v-if="loading" class="text-white/70">Đang tải...</div>
      <div v-else-if="!filteredCombos.length" class="text-white/60">
        Không có combo nào phù hợp.
      </div>

      <div v-else class="grid grid-cols-1 md:grid-cols-2 xl:grid-cols-3 gap-4">
        <div
          v-for="c in filteredCombos"
          :key="c.id"
          class="rounded-2xl border border-white/10 bg-white/5 hover:bg-white/10 transition p-4"
        >
          <div class="flex items-start justify-between gap-3">
            <div>
              <h4 class="font-semibold text-lg">{{ c.name }}</h4>
              <p v-if="c.description" class="text-sm text-white/70 mt-1">{{ c.description }}</p>
            </div>
            <span
              class="px-2 py-1 rounded-lg text-xs"
              :class="
                c.status === 'ACTIVE'
                  ? 'bg-emerald-500/15 text-emerald-300 border border-emerald-400/30'
                  : 'bg-rose-500/15 text-rose-300 border border-rose-400/30'
              "
            >
              {{ c.status === "ACTIVE" ? "Hoạt động" : "Tạm dừng" }}
            </span>
          </div>

          <div class="mt-3 grid grid-cols-3 gap-2 text-xs text-white/70">
            <div
              v-for="(it, i) in c.items || []"
              :key="i"
              class="rounded border border-white/10 px-2 py-1"
            >
              <div class="truncate">
                {{
                  Array.isArray(dishes) ? dishes.find((d) => d.id === it.dishId)?.name : "—" || "—"
                }}
              </div>
              <div class="text-[11px]">SL: {{ it.quantity }}</div>
            </div>
          </div>

          <div class="mt-3 flex items-center justify-between">
            <div class="text-emerald-300 font-semibold">{{ vnd(Number(c.price)) }}</div>
            <div class="flex gap-2">
              <button
                class="px-2 py-1 text-xs rounded-lg border border-blue-400/40 text-blue-300 hover:bg-blue-500/10"
                @click="startEdit(c)"
              >
                ✏️ Sửa
              </button>
              <button
                class="px-2 py-1 text-xs rounded-lg border border-rose-400/40 text-rose-300 hover:bg-rose-500/10"
                @click="openDeleteModal(c)"
              >
                🗑️ Xoá
              </button>
            </div>
          </div>
        </div>
      </div>
    </div>
  </div>

  <!-- Confirm Delete Modal -->
  <ConfirmModal
    :show="showDeleteModal"
    :message="`Bạn có chắc chắn muốn xóa combo này không?`"
    :itemName="comboToDelete?.name"
    :loading="deleting"
    @confirm="confirmDeleteCombo"
    @cancel="closeDeleteModal"
  />
</template>
