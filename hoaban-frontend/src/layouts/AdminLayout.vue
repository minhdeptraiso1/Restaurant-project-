<script setup lang="ts">
import { ref, computed } from "vue";
import { useRouter, useRoute, RouterLink, RouterView } from "vue-router";
import { useAuthStore } from "@/stores/auth";

const auth = useAuthStore();
const route = useRoute();

const sidebarOpen = ref(true);
const mobileOpen = ref(false);
const toggleSidebar = () => (sidebarOpen.value = !sidebarOpen.value);
const toggleMobile = () => (mobileOpen.value = !mobileOpen.value);

const nav = [
  { to: "/admin", label: "Dashboard", icon: "⚡", exact: true },
  { to: "/admin/users", label: "Users", icon: "👥" },
  { to: "/admin/dishes", label: "Món ăn", icon: "🍽️" },
  { to: "/admin/combos", label: "Combo", icon: "🧩" },
  { to: "/admin/categories", label: "Danh mục", icon: "🗂️" },
  { to: "/admin/areas", label: "Khu vực", icon: "🏢" },
  { to: "/admin/tables", label: "Bàn", icon: "🪑" },
  { to: "/admin/vouchers", label: "Vouchers", icon: "🎟️" },
  { to: "/admin/orders", label: "Đơn hàng", icon: "🧾" },
  { to: "/admin/reservations", label: "Đặt bàn", icon: "📅" },
  { to: "/admin/reviews", label: "Đánh giá", icon: "⭐" },
];

const initials = computed(() => {
  const name = auth.user?.fullName || "Admin";
  return name
    .split(" ")
    .map((w) => w[0])
    .slice(-2)
    .join("")
    .toUpperCase();
});

const isActive = (itemPath: string, exact?: boolean) => {
  if (exact) {
    return route.path === itemPath;
  }
  return route.path.startsWith(itemPath);
};
</script>

<template>
  <div class="min-h-screen flex bg-gradient-to-br from-[#0a0f0d] via-[#0f1f1a] to-[#0a0f0d]">
    <!-- SIDEBAR - Desktop -->
    <aside
      :class="[
        'hidden lg:flex flex-col fixed left-0 top-0 h-screen bg-gradient-to-b from-[#0d1714] via-[#0f1f1a] to-[#0b1512] border-r border-white/10 backdrop-blur-xl transition-all duration-300 z-40',
        sidebarOpen ? 'w-64' : 'w-20',
      ]"
    >
      <!-- Animated background effects -->
      <div class="absolute inset-0 pointer-events-none overflow-hidden">
        <div
          class="absolute -top-20 -left-20 w-40 h-40 bg-emerald-500/10 rounded-full blur-3xl animate-pulse"
        ></div>
        <div
          class="absolute bottom-20 -right-20 w-48 h-48 bg-amber-500/10 rounded-full blur-3xl animate-pulse"
          style="animation-delay: 1s"
        ></div>
      </div>

      <div class="relative z-10 flex flex-col h-full">
        <!-- Brand Header -->
        <div class="flex items-center justify-between p-4 border-b border-white/10">
          <RouterLink
            to="/admin"
            :class="[
              'flex items-center gap-3 font-bold text-lg transition-all',
              sidebarOpen ? 'opacity-100' : 'opacity-0 w-0',
            ]"
          >
            <span
              class="inline-block w-3 h-3 bg-gradient-to-r from-emerald-400 to-amber-400 rounded-full shadow-[0_0_20px_rgba(52,211,153,0.6)]"
            ></span>
            <span
              class="bg-gradient-to-r from-emerald-400 via-teal-300 to-amber-400 bg-clip-text text-transparent"
            >
              Admin Portal
            </span>
          </RouterLink>
          <button
            @click="toggleSidebar"
            class="p-2 rounded-lg hover:bg-white/10 transition-colors ml-auto"
            title="Toggle Sidebar"
          >
            <svg
              class="w-5 h-5 text-white/70 transition-transform"
              :class="{ 'rotate-180': !sidebarOpen }"
              fill="none"
              stroke="currentColor"
              viewBox="0 0 24 24"
            >
              <path
                stroke-linecap="round"
                stroke-linejoin="round"
                stroke-width="2"
                d="M11 19l-7-7 7-7m8 14l-7-7 7-7"
              />
            </svg>
          </button>
        </div>

        <!-- Navigation -->
        <nav class="flex-1 p-3 space-y-1 overflow-y-auto admin-scrollbar">
          <RouterLink
            v-for="item in nav"
            :key="item.to"
            :to="item.to"
            :class="[
              'group flex items-center gap-3 px-3 py-3 rounded-xl text-sm font-medium transition-all relative overflow-hidden',
              isActive(item.to, item.exact)
                ? 'bg-gradient-to-r from-emerald-500/20 to-teal-500/20 text-white border border-emerald-400/30 shadow-lg shadow-emerald-500/10'
                : 'text-white/70 hover:text-white hover:bg-white/5 border border-transparent',
            ]"
          >
            <!-- Active indicator -->
            <span
              v-if="isActive(item.to, item.exact)"
              class="absolute left-0 top-1/2 -translate-y-1/2 w-1 h-8 bg-gradient-to-b from-emerald-400 to-teal-400 rounded-r-full shadow-[0_0_10px_rgba(52,211,153,0.5)]"
            ></span>

            <!-- Icon -->
            <span class="text-xl flex-shrink-0 ml-1">{{ item.icon }}</span>

            <!-- Label -->
            <span
              :class="[
                'transition-all duration-300 whitespace-nowrap',
                sidebarOpen ? 'opacity-100 w-auto' : 'opacity-0 w-0 overflow-hidden',
              ]"
            >
              {{ item.label }}
            </span>

            <!-- Hover glow -->
            <span
              v-if="!isActive(item.to, item.exact)"
              class="absolute inset-0 bg-gradient-to-r from-emerald-500/0 via-emerald-500/5 to-emerald-500/0 opacity-0 group-hover:opacity-100 transition-opacity pointer-events-none"
            ></span>
          </RouterLink>
        </nav>

        <!-- User Section -->
        <div class="p-4 border-t border-white/10 space-y-3">
          <!-- User Info -->
          <div
            :class="[
              'flex items-center gap-3 p-3 rounded-xl bg-gradient-to-r from-white/5 to-white/[0.02] border border-white/10',
              sidebarOpen ? '' : 'justify-center',
            ]"
          >
            <div
              class="w-10 h-10 flex-shrink-0 rounded-full bg-gradient-to-br from-emerald-500 to-teal-500 flex items-center justify-center text-sm font-bold text-white border-2 border-emerald-400/30 shadow-lg shadow-emerald-500/20"
            >
              {{ initials }}
            </div>
            <div
              :class="[
                'flex-1 min-w-0 transition-all duration-300',
                sidebarOpen ? 'opacity-100' : 'opacity-0 w-0 overflow-hidden',
              ]"
            >
              <div class="text-sm font-medium text-white truncate">
                {{ auth.user?.fullName || "Admin" }}
              </div>
              <div class="text-xs text-white/60">Hoa Ban • Admin</div>
            </div>
          </div>

          <!-- Logout Button -->
          <button
            @click="auth.logout"
            :class="[
              'w-full flex items-center gap-3 px-3 py-2.5 rounded-xl font-medium text-sm text-white bg-gradient-to-r from-rose-500/20 to-rose-600/20 hover:from-rose-500/30 hover:to-rose-600/30 border border-rose-400/30 transition-all',
              sidebarOpen ? 'justify-start' : 'justify-center',
            ]"
            title="Đăng xuất"
          >
            <span class="text-lg">🔒</span>
            <span
              :class="[
                'transition-all duration-300',
                sidebarOpen ? 'opacity-100 w-auto' : 'opacity-0 w-0 overflow-hidden',
              ]"
            >
              Đăng xuất
            </span>
          </button>
        </div>
      </div>
    </aside>

    <!-- MOBILE HEADER -->
    <div class="lg:hidden fixed top-0 left-0 right-0 z-50">
      <div
        class="backdrop-blur-xl bg-gradient-to-r from-[#0d1714]/95 via-[#0f1f1a]/95 to-[#0b1512]/95 border-b border-white/10 shadow-2xl"
      >
        <div class="flex items-center justify-between px-4 h-16">
          <!-- Brand -->
          <RouterLink to="/admin" class="flex items-center gap-2 font-bold text-lg">
            <span
              class="inline-block w-2.5 h-2.5 bg-gradient-to-r from-emerald-400 to-amber-400 rounded-full shadow-[0_0_15px_rgba(52,211,153,0.6)]"
            ></span>
            <span
              class="bg-gradient-to-r from-emerald-400 to-amber-400 bg-clip-text text-transparent"
            >
              Admin
            </span>
          </RouterLink>

          <!-- Mobile Menu Button -->
          <div class="flex items-center gap-3">
            <div
              class="w-9 h-9 rounded-full bg-gradient-to-br from-emerald-500 to-teal-500 flex items-center justify-center text-xs font-bold text-white border-2 border-emerald-400/30"
            >
              {{ initials }}
            </div>
            <button
              @click="toggleMobile"
              class="p-2 rounded-lg bg-white/10 hover:bg-white/15 border border-white/10 transition-colors"
            >
              <svg class="w-6 h-6 text-white" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                <path
                  v-if="!mobileOpen"
                  stroke-linecap="round"
                  stroke-linejoin="round"
                  stroke-width="2"
                  d="M4 6h16M4 12h16M4 18h16"
                />
                <path
                  v-else
                  stroke-linecap="round"
                  stroke-linejoin="round"
                  stroke-width="2"
                  d="M6 18L18 6M6 6l12 12"
                />
              </svg>
            </button>
          </div>
        </div>
      </div>

      <!-- Mobile Drawer -->
      <transition name="slide-fade">
        <div
          v-if="mobileOpen"
          class="fixed inset-0 top-16 bg-black/60 backdrop-blur-sm z-40"
          @click="mobileOpen = false"
        >
          <div
            class="bg-gradient-to-b from-[#0d1714] via-[#0f1f1a] to-[#0b1512] w-72 h-full border-r border-white/10 shadow-2xl overflow-y-auto"
            @click.stop
          >
            <nav class="p-4 space-y-1">
              <RouterLink
                v-for="item in nav"
                :key="item.to"
                :to="item.to"
                :class="[
                  'flex items-center gap-3 px-4 py-3 rounded-xl text-sm font-medium transition-all',
                  isActive(item.to, item.exact)
                    ? 'bg-gradient-to-r from-emerald-500/20 to-teal-500/20 text-white border border-emerald-400/30'
                    : 'text-white/70 hover:text-white hover:bg-white/5',
                ]"
                @click="mobileOpen = false"
              >
                <span class="text-xl">{{ item.icon }}</span>
                <span>{{ item.label }}</span>
              </RouterLink>

              <div class="pt-4 mt-4 border-t border-white/10">
                <button
                  @click="auth.logout"
                  class="w-full flex items-center gap-3 px-4 py-3 rounded-xl text-sm font-medium text-white bg-gradient-to-r from-rose-500/20 to-rose-600/20 border border-rose-400/30"
                >
                  <span class="text-xl">🔒</span>
                  <span>Đăng xuất</span>
                </button>
              </div>
            </nav>
          </div>
        </div>
      </transition>
    </div>

    <!-- MAIN CONTENT -->
    <main
      :class="[
        'flex-1 transition-all duration-300 lg:pt-0 pt-16',
        sidebarOpen ? 'lg:ml-64' : 'lg:ml-20',
      ]"
    >
      <div class="min-h-screen p-4 md:p-6 lg:p-8">
        <RouterView />
      </div>
    </main>
  </div>
</template>

<style scoped>
/* Slide fade animation for mobile drawer */
.slide-fade-enter-active {
  transition: all 0.3s ease-out;
}

.slide-fade-leave-active {
  transition: all 0.2s cubic-bezier(1, 0.5, 0.8, 1);
}

.slide-fade-enter-from,
.slide-fade-leave-to {
  opacity: 0;
}

.slide-fade-enter-from > div {
  transform: translateX(-100%);
}

.slide-fade-leave-to > div {
  transform: translateX(-100%);
}

/* Custom scrollbar for sidebar */
.admin-scrollbar::-webkit-scrollbar {
  width: 6px;
}

.admin-scrollbar::-webkit-scrollbar-track {
  background: rgba(255, 255, 255, 0.05);
  border-radius: 10px;
}

.admin-scrollbar::-webkit-scrollbar-thumb {
  background: rgba(255, 255, 255, 0.2);
  border-radius: 10px;
}

.admin-scrollbar::-webkit-scrollbar-thumb:hover {
  background: rgba(255, 255, 255, 0.3);
}
</style>
