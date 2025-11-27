<template>
  <header class="fixed top-0 left-0 w-full bg-white shadow z-50">
    <div class="container mx-auto flex items-center justify-between h-20 px-6">
      <!-- Logo + Brand -->
      <div class="flex items-center gap-3 cursor-pointer" @click="router.push('/')">
        <img src="@/assets/img/logo.png" alt="Logo" class="w-12 h-12 rounded-full object-cover" />
        <span class="font-serif text-2xl font-bold text-[#9f0909]">Hoa Ban Restaurant</span>
      </div>

      <!-- Desktop Menu -->
      <nav class="hidden md:flex items-center gap-8 relative">
        <!-- Moving underline background -->
        <div
          class="absolute bottom-0 h-[2px] bg-[#9f0909] rounded transition-all duration-300 ease-out"
          :style="underlineStyle"
        ></div>

        <button
          v-for="(item, index) in menuItems"
          :key="item.key"
          :ref="(el) => setMenuRef(el as HTMLElement, index)"
          class="relative text-lg font-semibold transition-colors duration-200"
          :class="activeTab === item.key ? 'text-[#9f0909]' : 'text-gray-800 hover:text-[#9f0909]'"
          @click="setActiveTab(item.key)"
          @mouseenter="onMouseEnter(index)"
          @mouseleave="onMouseLeave"
        >
          {{ item.label }}
        </button>
      </nav>

      <!-- User / Auth -->
      <div>
        <template v-if="isLoggedIn">
          <div class="relative">
            <button
              @click.stop="toggleUserMenu"
              class="flex items-center gap-2 px-4 py-2 border border-[#9f0909] rounded-full hover:bg-[#f9f9f9] transition"
            >
              <span class="font-semibold text-[#9f0909]">{{ currentUser?.fullName }}</span>
              <svg
                class="w-4 h-4 text-[#9f0909] transition-transform"
                :class="{ 'rotate-180': showUserMenu }"
                fill="none"
                viewBox="0 0 24 24"
                stroke="currentColor"
              >
                <path
                  stroke-linecap="round"
                  stroke-linejoin="round"
                  stroke-width="2"
                  d="M19 9l-7 7-7-7"
                />
              </svg>
            </button>

            <!-- Dropdown -->
            <transition name="fade">
              <div
                v-if="showUserMenu"
                class="absolute right-0 mt-2 w-48 bg-white border rounded-lg shadow-lg overflow-hidden"
              >
                <button @click="handleProfile" class="w-full px-4 py-2 text-left hover:bg-gray-100">
                  Thông tin
                </button>
                <button
                  @click="handleVouchers"
                  class="w-full px-4 py-2 text-left hover:bg-gray-100"
                >
                  Voucher
                </button>
                <button
                  @click="handleOrderHistory"
                  class="w-full px-4 py-2 text-left hover:bg-gray-100"
                >
                  Lịch sử
                </button>
                <hr />
                <button
                  @click="handleLogout"
                  class="w-full px-4 py-2 text-left text-red-600 hover:bg-red-50"
                >
                  Đăng xuất
                </button>
              </div>
            </transition>
          </div>
        </template>
        <template v-else>
          <div class="flex items-center gap-3">
            <button
              @click="toggleLoginState"
              class="px-4 py-2 border rounded-full hover:bg-gray-50"
            >
              Đăng nhập
            </button>
            <button
              @click="handleRegister"
              class="px-4 py-2 bg-[#9f0909] text-white rounded-full hover:bg-[#7d0707] transition"
            >
              Đăng ký
            </button>
          </div>
        </template>
      </div>

      <!-- Mobile Hamburger -->
      <button class="md:hidden" @click="toggleMobileMenu">
        <svg class="w-8 h-8" fill="none" stroke="currentColor" viewBox="0 0 24 24">
          <path
            stroke-linecap="round"
            stroke-linejoin="round"
            stroke-width="2"
            d="M4 6h16M4 12h16M4 18h16"
          />
        </svg>
      </button>
    </div>

    <!-- Mobile Menu -->
    <transition name="slide">
      <div v-if="mobileMenuOpen" class="md:hidden bg-white border-t shadow p-4 space-y-4">
        <button
          v-for="item in menuItems"
          :key="item.key"
          class="block w-full text-left text-lg font-medium hover:text-[#9f0909]"
          @click="
            setActiveTab(item.key);
            mobileMenuOpen = false;
          "
        >
          {{ item.label }}
        </button>
      </div>
    </transition>
  </header>

  <!-- Spacer -->
  <div class="h-20"></div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted, onUnmounted, watch, nextTick } from "vue";
import { useRouter } from "vue-router";
import { useCartStore } from "@/stores/cart";
import { useAuthStore } from "@/stores/auth";

const router = useRouter();
const auth = useAuthStore();
const cart = useCartStore();

const totalQuantity = computed(() => cart.items.reduce((n, i) => n + i.qty, 0));
const currentUser = computed(() => auth.user);
const isLoggedIn = computed(() => !!auth.token && !!auth.user);

const showUserMenu = ref(false);
const mobileMenuOpen = ref(false);

// Moving underline refs and state
const menuRefs = ref<HTMLElement[]>([]);
const hoveredIndex = ref<number | null>(null);
const underlineStyle = ref({
  left: "0px",
  width: "0px",
  opacity: "0",
});

// Helper function to set menu refs
const setMenuRef = (el: HTMLElement | null, index: number) => {
  if (el) {
    menuRefs.value[index] = el;
  }
};

const toggleUserMenu = () => (showUserMenu.value = !showUserMenu.value);
const toggleMobileMenu = () => (mobileMenuOpen.value = !mobileMenuOpen.value);

const handleLogout = async () => {
  showUserMenu.value = false;
  await auth.logout();
  router.push("/");
};
const handleProfile = () => {
  showUserMenu.value = false;
  router.push("/profile");
};
const handleVouchers = () => {
  showUserMenu.value = false;
  router.push("/vouchers");
};
const handleOrderHistory = () => {
  showUserMenu.value = false;
  router.push("/history");
};
const toggleLoginState = () => router.push("/auth/login");
const handleRegister = () => router.push("/auth/register");

const activeTab = ref("trang-chu");

// Moving underline functions
const updateUnderlinePosition = (targetIndex: number | null = null) => {
  nextTick(() => {
    const currentIndex =
      targetIndex !== null
        ? targetIndex
        : menuItems.value.findIndex((item) => item.key === activeTab.value);

    if (currentIndex >= 0 && currentIndex < menuRefs.value.length) {
      const targetElement = menuRefs.value[currentIndex];

      if (targetElement) {
        const rect = targetElement.getBoundingClientRect();
        const navElement = targetElement.parentElement;
        const navRect = navElement?.getBoundingClientRect();

        if (navRect) {
          underlineStyle.value = {
            left: `${rect.left - navRect.left}px`,
            width: `${rect.width}px`,
            opacity: "1",
          };
          return;
        }
      }
    }

    // Fallback: hide underline
    underlineStyle.value = {
      left: "0px",
      width: "0px",
      opacity: "0",
    };
  });
};

const onMouseEnter = (index: number) => {
  hoveredIndex.value = index;
  updateUnderlinePosition(index);
};

const onMouseLeave = () => {
  hoveredIndex.value = null;
  updateUnderlinePosition(); // Reset to active tab
};

const setActiveTab = (tab: string) => {
  activeTab.value = tab;
  updateUnderlinePosition(); // Update underline to new active tab

  // Navigate to different pages
  if (tab === "trang-chu") {
    router.push("/");
  } else if (tab === "thuc-don") {
    router.push("/menu");
  } else if (tab === "combo") {
    router.push("/combos");
  } else if (tab === "dat-ban") {
    router.push("/reservation");
  } else if (tab === "gio-hang") {
    router.push("/cart");
  }
};

// Scroll to section function
const scrollToSection = (sectionId: string) => {
  const element = document.getElementById(sectionId);
  if (element) {
    const headerHeight = 80; // Fixed header height
    const elementPosition = element.offsetTop - headerHeight;
    window.scrollTo({
      top: elementPosition,
      behavior: "smooth",
    });
  }
};

const menuItems = computed(() => [
  { key: "trang-chu", label: "Trang chủ" },
  { key: "thuc-don", label: "Thực đơn" },
  { key: "combo", label: "Combo" },
  { key: "dat-ban", label: "Đặt bàn" },
  {
    key: "gio-hang",
    label: `Giỏ hàng ${totalQuantity.value > 0 ? `(${totalQuantity.value})` : ""}`,
  },
]);

// Update active tab based on scroll position
const updateActiveTabOnScroll = () => {
  if (router.currentRoute.value.path !== "/") return;

  const sections: string[] = ["trang-chu", "thuc-don", "dat-ban"];
  const headerHeight = 80;

  for (let i = sections.length - 1; i >= 0; i--) {
    const sectionId = sections[i]!; // Non-null assertion since we control the array
    const element = document.getElementById(sectionId);
    if (element) {
      const rect = element.getBoundingClientRect();
      if (rect.top <= headerHeight + 100) {
        // 100px offset for better UX
        if (activeTab.value !== sectionId) {
          activeTab.value = sectionId;
          updateUnderlinePosition(); // Update underline when tab changes
        }
        break;
      }
    }
  }
};

// Close user menu when clicking outside
const handleClickOutside = (event: MouseEvent) => {
  if (showUserMenu.value) {
    showUserMenu.value = false;
  }
};

onMounted(() => {
  document.addEventListener("click", handleClickOutside);
  window.addEventListener("scroll", updateActiveTabOnScroll);
  window.addEventListener("resize", handleResize);
  // Initialize underline position
  nextTick(() => {
    updateUnderlinePosition();
  });
});

onUnmounted(() => {
  document.removeEventListener("click", handleClickOutside);
  window.removeEventListener("scroll", updateActiveTabOnScroll);
  window.removeEventListener("resize", handleResize);
  clearTimeout(resizeTimeout);
});

// Watch route changes to update active tab
watch(
  () => router.currentRoute.value.path,
  (newPath) => {
    if (newPath === "/") {
      // On home page, use scroll-based detection
      updateActiveTabOnScroll();
    } else if (newPath === "/menu") {
      activeTab.value = "thuc-don";
    } else if (newPath === "/combos" || newPath.startsWith("/combos")) {
      activeTab.value = "combo";
    } else if (newPath === "/reservation") {
      activeTab.value = "dat-ban";
    } else if (newPath === "/cart") {
      activeTab.value = "gio-hang";
    }
    // Update underline position when route changes
    nextTick(() => {
      updateUnderlinePosition();
    });
  },
  { immediate: true }
);

// Watch window resize to update underline position
let resizeTimeout: number;
const handleResize = () => {
  clearTimeout(resizeTimeout);
  resizeTimeout = setTimeout(() => {
    updateUnderlinePosition();
  }, 100);
};
</script>

<style scoped>
.fade-enter-active,
.fade-leave-active {
  transition: opacity 0.2s;
}
.fade-enter-from,
.fade-leave-to {
  opacity: 0;
}
.slide-enter-active,
.slide-leave-active {
  transition: all 0.3s ease;
}
.slide-enter-from {
  opacity: 0;
  transform: translateY(-10px);
}
.slide-leave-to {
  opacity: 0;
  transform: translateY(-10px);
}
</style>
