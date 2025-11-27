<script setup lang="ts">
import { onMounted, ref, computed } from "vue";
import {
  listUsers,
  createUser,
  deleteUser,
  updateUserRole,
  type User,
  type PaginatedResponse,
  type ApiResponse,
} from "@/api/users.admin";
import { toast } from "vue3-toastify";
import ConfirmModal from "@/components/ConfirmModal.vue";

const users = ref<User[]>([]);
const loading = ref(false);
const creating = ref(false);
const deleting = ref<string | null>(null);
const editing = ref<string | null>(null);
const searchQuery = ref("");
const roleFilter = ref("");

// Pagination
const currentPage = ref(0);
const pageSize = ref(12);
const totalElements = ref(0);
const totalPages = ref(0);

const form = ref({
  fullName: "",
  email: "",
  password: "",
  role: "CUSTOMER",
});

// Edit form
const editForm = ref({
  id: "",
  fullName: "",
  email: "",
  role: "STAFF",
  status: "ACTIVE",
});

// Modals
const showEditModal = ref(false);
const showDeleteModal = ref(false);
const userToDelete = ref<User | null>(null);

const isValid = computed(() => {
  const nameOk = form.value.fullName.trim().length >= 2;
  const mailOk = /^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(form.value.email);
  const passOk = form.value.password.length >= 6;
  return nameOk && mailOk && passOk;
});

// Validation for edit form
const isEditFormValid = computed(() => {
  return editForm.value.role.trim().length > 0;
});

async function loadData(page: number = 0) {
  loading.value = true;
  try {
    const params = {
      page,
      size: pageSize.value,
      search: searchQuery.value.trim() || undefined,
      role: roleFilter.value || undefined,
    };
    const response = await listUsers(params);

    // API trả về trực tiếp Page object (content, number, size, totalElements, totalPages)
    const apiData = response.data || response;

    users.value = apiData.content || [];
    currentPage.value = apiData.number || 0;
    totalElements.value = apiData.totalElements || 0;
    totalPages.value = apiData.totalPages || 0;
  } catch (e: any) {
    console.error(e);
    toast.error("Lỗi tải danh sách người dùng");
  } finally {
    loading.value = false;
  }
}

function clearFilters() {
  searchQuery.value = "";
  roleFilter.value = "";
  loadData(0);
}

async function submit() {
  if (!isValid.value) {
    toast.warning("Vui lòng nhập đúng và đủ thông tin");
    return;
  }
  creating.value = true;
  try {
    await createUser(form.value);
    toast.success("Tạo người dùng thành công");
    form.value = { fullName: "", email: "", password: "", role: "CUSTOMER" };
    await loadData();
  } catch (e: any) {
    toast.error(e?.friendlyMessage || "Tạo người dùng thất bại");
  } finally {
    creating.value = false;
  }
}

// Edit user functions
function openEditModal(user: User) {
  editForm.value = {
    id: user.id,
    fullName: user.fullName,
    email: user.email,
    role: user.role,
    status: user.status,
  };
  showEditModal.value = true;
}

function closeEditModal() {
  showEditModal.value = false;
  editing.value = null;
  editForm.value = {
    id: "",
    fullName: "",
    email: "",
    role: "STAFF",
    status: "ACTIVE",
  };
}

async function submitEdit() {
  if (!editForm.value.role.trim()) {
    toast.warning("Vui lòng chọn vai trò");
    return;
  }

  editing.value = editForm.value.id;
  try {
    await updateUserRole(editForm.value.id, editForm.value.role);
    toast.success("Cập nhật vai trò người dùng thành công");
    closeEditModal();
    await loadData(currentPage.value);
  } catch (e: any) {
    toast.error(e?.response?.data?.message || e?.friendlyMessage || "Cập nhật người dùng thất bại");
  } finally {
    editing.value = null;
  }
}

// Delete user functions
function openDeleteModal(user: User) {
  userToDelete.value = user;
  showDeleteModal.value = true;
}

function closeDeleteModal() {
  showDeleteModal.value = false;
  userToDelete.value = null;
  deleting.value = null;
}

async function confirmDelete() {
  if (!userToDelete.value) return;

  deleting.value = userToDelete.value.id;
  try {
    await deleteUser(userToDelete.value.id);
    toast.success("Xóa người dùng thành công");
    closeDeleteModal();
    await loadData(currentPage.value);
  } catch (e: any) {
    toast.error(e?.friendlyMessage || "Xóa người dùng thất bại");
  } finally {
    deleting.value = null;
  }
}

// Pagination
function goToPage(page: number) {
  loadData(page);
}

function roleBadge(role?: string) {
  switch (role) {
    case "ADMIN":
      return "badge-info";
    case "STAFF":
      return "badge-success";
    case "CUSTOMER":
      return "badge-warning";
    default:
      return "badge-gray";
  }
}

function statusBadge(status?: string) {
  switch (status) {
    case "ACTIVE":
      return "badge-success";
    case "INACTIVE":
    case "LOCKED":
      return "badge-danger";
    default:
      return "badge-gray";
  }
}

function roleLabel(role?: string) {
  switch (role) {
    case "ADMIN":
      return "Quản trị viên";
    case "STAFF":
      return "Nhân viên";
    case "CUSTOMER":
      return "Khách hàng";
    default:
      return role || "N/A";
  }
}

onMounted(() => loadData());
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
        <h2 class="text-2xl md:text-3xl font-extrabold tracking-tight">👥 Quản lý người dùng</h2>
        <p class="mt-1 text-white/70">Tạo nhân viên, phân quyền, xem danh sách người dùng.</p>
      </div>
    </div>

    <!-- Create form -->
    <div class="rounded-2xl bg-white/5 backdrop-blur-md border border-white/10 shadow p-6">
      <div class="flex items-center justify-between gap-3 mb-4">
        <h3 class="font-semibold text-lg">Tạo người dùng mới</h3>
        <div class="text-xs text-white/60">Mật khẩu tối thiểu 6 ký tự</div>
      </div>

      <div class="grid grid-cols-1 md:grid-cols-2 xl:grid-cols-4 gap-3 mb-4">
        <div class="relative">
          <span class="absolute left-3 top-1/2 -translate-y-1/2 opacity-70">🧑</span>
          <input
            class="w-full bg-white/5 border border-white/10 rounded-lg pl-10 pr-3 py-2 outline-none focus:border-emerald-400"
            placeholder="Họ tên *"
            v-model="form.fullName"
          />
        </div>

        <div class="relative">
          <span class="absolute left-3 top-1/2 -translate-y-1/2 opacity-70">✉️</span>
          <input
            class="w-full bg-white/5 border border-white/10 rounded-lg pl-10 pr-3 py-2 outline-none focus:border-emerald-400"
            placeholder="Email *"
            v-model="form.email"
            type="email"
          />
        </div>

        <div class="relative">
          <span class="absolute left-3 top-1/2 -translate-y-1/2 opacity-70">🔒</span>
          <input
            class="w-full bg-white/5 border border-white/10 rounded-lg pl-10 pr-3 py-2 outline-none focus:border-emerald-400"
            placeholder="Mật khẩu *"
            type="password"
            v-model="form.password"
          />
        </div>

        <div class="relative">
          <span class="absolute left-3 top-1/2 -translate-y-1/2 opacity-70">🛡️</span>
          <select
            class="w-full bg-white/5 border border-white/10 rounded-lg pl-10 pr-8 py-2 outline-none focus:border-emerald-400 appearance-none cursor-pointer"
            v-model="form.role"
          >
            <option class="bg-[#0b1512]" value="CUSTOMER">Khách hàng</option>
            <option class="bg-[#0b1512]" value="STAFF">Nhân viên</option>
            <option class="bg-[#0b1512]" value="ADMIN">Quản trị</option>
          </select>

          <!-- Icon mũi tên -->
          <span class="absolute right-3 top-1/2 -translate-y-1/2 opacity-70 pointer-events-none">
            ▼
          </span>
        </div>
      </div>

      <div class="flex items-center gap-3">
        <button
          class="px-4 py-2 rounded-lg bg-emerald-600 hover:bg-emerald-700 transition disabled:opacity-50 disabled:cursor-not-allowed"
          :disabled="creating || !isValid"
          @click="submit"
        >
          <span v-if="creating" class="inline-flex items-center gap-2">
            <span
              class="w-4 h-4 inline-block rounded-full border-2 border-white border-t-transparent animate-spin"
            ></span>
            Đang tạo...
          </span>
          <span v-else> Tạo người dùng</span>
        </button>
        <span v-if="!isValid" class="text-xs text-white/60">
          Điền đúng: Họ tên ≥ 2 ký tự, Email hợp lệ, Mật khẩu ≥ 6 ký tự
        </span>
      </div>
    </div>

    <!-- Search bar -->
    <div class="rounded-2xl bg-white/5 backdrop-blur-md border border-white/10 shadow p-6">
      <div class="flex flex-col sm:flex-row gap-3">
        <div class="relative flex-1">
          <span class="absolute left-3 top-2.5 opacity-70">🔍</span>
          <input
            class="w-full bg-white/5 border border-white/10 rounded-lg pl-9 pr-3 py-2 outline-none focus:border-emerald-400"
            placeholder="Tìm theo tên hoặc email..."
            v-model="searchQuery"
            @input="loadData"
          />
        </div>

        <div class="relative sm:w-48">
          <span class="absolute left-3 top-2.5 opacity-70">🛡️</span>
          <select
            class="w-full bg-white/5 border border-white/10 rounded-lg pl-9 pr-3 py-2 outline-none focus:border-emerald-400"
            v-model="roleFilter"
            @change="loadData"
          >
            <option class="bg-[#0b1512]" value="">Tất cả vai trò</option>
            <option class="bg-[#0b1512]" value="CUSTOMER">Khách hàng</option>
            <option class="bg-[#0b1512]" value="STAFF">Nhân viên</option>
            <option class="bg-[#0b1512]" value="ADMIN">Quản trị</option>
          </select>
        </div>

        <button
          class="px-4 py-2 rounded-lg bg-white/10 hover:bg-white/15 border border-white/10 whitespace-nowrap"
          @click="clearFilters"
        >
          🔄 Làm mới
        </button>
      </div>
    </div>

    <!-- User list -->
    <div class="rounded-2xl bg-white/5 backdrop-blur-md border border-white/10 shadow p-6">
      <!-- Loading state -->
      <div
        v-if="loading"
        class="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 xl:grid-cols-4 gap-4"
      >
        <div v-for="i in 8" :key="i" class="glass-card">
          <div class="flex items-center gap-3 mb-3">
            <div class="w-12 h-12 rounded-full bg-white/10 animate-pulse"></div>
            <div class="flex-1 space-y-2">
              <div class="h-4 bg-white/10 rounded animate-pulse w-3/4"></div>
              <div class="h-3 bg-white/10 rounded animate-pulse w-1/2"></div>
            </div>
          </div>
          <div class="flex gap-2 mb-4">
            <div class="h-6 bg-white/10 rounded animate-pulse w-20"></div>
            <div class="h-6 bg-white/10 rounded animate-pulse w-16"></div>
          </div>
          <div class="space-y-2 mb-4">
            <div class="h-3 bg-white/10 rounded animate-pulse"></div>
            <div class="h-3 bg-white/10 rounded animate-pulse"></div>
          </div>
          <div class="flex gap-2">
            <div class="h-8 bg-white/10 rounded animate-pulse flex-1"></div>
            <div class="h-8 bg-white/10 rounded animate-pulse flex-1"></div>
          </div>
        </div>
      </div>

      <div v-else>
        <!-- Empty state -->
        <div
          v-if="users.length === 0"
          class="flex flex-col items-center justify-center py-16 text-center"
        >
          <div class="text-6xl mb-4">👥</div>
          <h3 class="text-xl font-semibold mb-2">Không có người dùng nào</h3>
          <p class="text-white/60 text-sm">
            {{
              searchQuery || roleFilter
                ? "Không tìm thấy kết quả phù hợp"
                : "Chưa có người dùng nào được tạo"
            }}
          </p>
        </div>

        <div v-else>
          <!-- Cards Grid -->
          <div class="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 xl:grid-cols-4 gap-4 mb-6">
            <div v-for="user in users" :key="user.id" class="glass-card hover-lift">
              <!-- User Avatar & Name -->
              <div class="flex items-center gap-3 mb-3">
                <div
                  class="w-12 h-12 rounded-full bg-gradient-to-br from-emerald-500 to-blue-500 flex items-center justify-center text-white font-bold text-lg"
                >
                  {{ user.fullName.charAt(0).toUpperCase() }}
                </div>
                <div class="flex-1 min-w-0">
                  <h4 class="font-medium text-white truncate">{{ user.fullName }}</h4>
                  <p class="text-xs text-white/60 truncate">{{ user.email }}</p>
                </div>
              </div>

              <!-- Role & Status Badges -->
              <div class="flex items-center gap-2 mb-4">
                <span class="badge-admin" :class="roleBadge(user.role)">
                  {{ roleLabel(user.role) }}
                </span>
                <span class="badge-admin" :class="statusBadge(user.status)">
                  {{ user.status || "N/A" }}
                </span>
              </div>

              <!-- User Info -->
              <div class="space-y-1 mb-4 text-xs">
                <div class="flex items-center gap-2 text-white/70">
                  <span>📧</span>
                  <span class="truncate">{{ user.email }}</span>
                </div>
                <div class="flex items-center gap-2 text-white/70">
                  <span>📱</span>
                  <span>{{ user.phone || "Chưa có số điện thoại" }}</span>
                </div>
              </div>

              <!-- Action Buttons -->
              <div class="flex gap-2">
                <button
                  @click="openEditModal(user)"
                  :disabled="editing === user.id"
                  class="flex-1 btn-admin-info text-xs"
                  title="Sửa vai trò người dùng"
                >
                  <span v-if="editing === user.id" class="loading-spinner"></span>
                  <span v-else>👤</span>
                  Vai trò
                </button>
                <button
                  @click="openDeleteModal(user)"
                  :disabled="deleting === user.id"
                  class="flex-1 btn-admin-danger text-xs"
                >
                  <span v-if="deleting === user.id" class="loading-spinner"></span>
                  <span v-else>🗑️</span>
                  Xóa
                </button>
              </div>
            </div>
          </div>

          <!-- Pagination -->
          <div v-if="totalPages > 1" class="flex items-center justify-center gap-2">
            <button
              @click="goToPage(currentPage - 1)"
              :disabled="currentPage === 0"
              class="pagination-btn"
            >
              Trước
            </button>

            <div class="flex gap-1">
              <button
                v-for="page in Math.min(5, totalPages)"
                :key="page - 1"
                @click="goToPage(page - 1)"
                :class="['pagination-btn', page - 1 === currentPage ? 'pagination-btn-active' : '']"
              >
                {{ page }}
              </button>
            </div>

            <button
              @click="goToPage(currentPage + 1)"
              :disabled="currentPage >= totalPages - 1"
              class="pagination-btn"
            >
              Sau
            </button>
          </div>

          <!-- Pagination Info -->
          <div v-if="totalElements > 0" class="text-center mt-4">
            <p class="text-sm text-white/60">
              Hiển thị {{ currentPage * pageSize + 1 }} -
              {{ Math.min((currentPage + 1) * pageSize, totalElements) }} trong tổng số
              {{ totalElements }} người dùng
            </p>
          </div>
        </div>
      </div>
    </div>

    <!-- Edit Modal -->
    <div v-if="showEditModal" class="modal-overlay">
      <div class="modal-content">
        <div class="modal-header">
          <h3 class="text-xl font-bold">Chỉnh sửa vai trò người dùng</h3>
          <button @click="closeEditModal" class="text-white/60 hover:text-white">✕</button>
        </div>

        <div class="modal-body space-y-4">
          <!-- Display user info (read-only) -->
          <div class="glass-card">
            <div class="space-y-2 text-sm">
              <div class="flex justify-between">
                <span class="text-white/70">Họ tên:</span>
                <span class="text-white font-medium">{{ editForm.fullName }}</span>
              </div>
              <div class="flex justify-between">
                <span class="text-white/70">Email:</span>
                <span class="text-white font-medium">{{ editForm.email }}</span>
              </div>
              <div class="flex justify-between">
                <span class="text-white/70">Trạng thái:</span>
                <span class="text-white font-medium">{{ editForm.status }}</span>
              </div>
            </div>
          </div>

          <div>
            <label class="block text-sm font-medium text-white/80 mb-2">Vai trò *</label>
            <select v-model="editForm.role" class="select-admin">
              <option value="CUSTOMER">Khách hàng</option>
              <option value="STAFF">Nhân viên</option>
              <option value="ADMIN">Quản trị viên</option>
            </select>
            <p class="text-xs text-white/60 mt-1">Chỉ có thể thay đổi vai trò của người dùng</p>
          </div>
        </div>

        <div class="modal-footer">
          <button @click="closeEditModal" class="btn-admin-secondary">Hủy</button>
          <button
            @click="submitEdit"
            :disabled="editing !== null || !isEditFormValid"
            class="btn-admin-primary"
          >
            <span v-if="editing" class="loading-spinner"></span>
            Cập nhật vai trò
          </button>
        </div>
      </div>
    </div>

    <!-- Delete Confirmation Modal -->
    <ConfirmModal
      :show="showDeleteModal"
      title="Xác nhận xóa"
      :message="`Bạn có chắc chắn muốn xóa người dùng`"
      :itemName="userToDelete?.fullName"
      confirmText="Xóa"
      :loading="deleting !== null"
      variant="danger"
      @confirm="confirmDelete"
      @cancel="closeDeleteModal"
    />
  </div>
</template>
