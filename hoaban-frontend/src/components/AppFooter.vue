<template>
  <footer class="text-white">
    <!-- Top -->
    <div class="bg-gradient-to-b from-[#9f0909] to-[#6f0a0a]">
      <div class="mx-auto max-w-7xl px-6 py-14">
        <div class="grid grid-cols-1 gap-8 md:grid-cols-2 lg:grid-cols-5">
          <!-- Brand -->
          <section class="space-y-4">
            <div class="flex items-center gap-3">
              <div
                class="size-[70px] rounded-full overflow-hidden border border-white/30 flex items-center justify-center bg-white/10 ring-1 ring-white/10"
              >
                <img
                  v-if="logo"
                  :src="logo"
                  :alt="brandName"
                  class="w-[70px] h-[70px] object-cover"
                />
                <span v-else class="font-bold text-xl text-white">HB</span>
              </div>
              <span class="font-['Inknut_Antiqua',serif] text-2xl leading-none font-bold">
                {{ brandName }}
              </span>
            </div>

            <p class="text-sm/6 text-white/85">Ẩm thực thực dưỡng – tinh tế & cân bằng.</p>

            <address class="not-italic text-sm/6 text-white/85">
              <button
                class="inline-flex items-center gap-2 hover:text-white/100 hover:underline underline-offset-4"
                @click="openMap()"
              >
                <IconLocation class="size-5" />
                <span>{{ address }}</span>
              </button>
            </address>
          </section>

          <!-- Google Map - Always Show -->
          <section class="lg:col-span-2">
            <h4 class="text-xl font-semibold mb-4">📍 Vị trí nhà hàng</h4>
            <GoogleMap
              :address="address"
              :lat="16.0434"
              :lng="108.2019"
              :zoom="16"
              :always-show="true"
              :show-info="true"
              :show-toggle="false"
              class="h-48 lg:h-56"
            />
          </section>

          <!-- Categories -->
          <section class="space-y-3">
            <h4 class="text-xl font-semibold">Danh mục món ăn</h4>
            <div v-if="categoriesLoading" class="text-sm text-white/70">Đang tải...</div>
            <div v-else class="space-y-2">
              <router-link
                v-for="category in displayCategories"
                :key="category.id"
                :to="{ name: 'menu', query: { category: category.id } }"
                class="block text-sm text-white/85 hover:text-white hover:underline underline-offset-4 transition-colors"
              >
                {{ category.name }}
              </router-link>
              <router-link
                :to="{ name: 'menu' }"
                class="block text-sm text-white/70 hover:text-white hover:underline underline-offset-4 transition-colors font-medium"
              >
                Xem tất cả →
              </router-link>
            </div>
          </section>

          <!-- Contact & Hours & Social -->
          <section class="space-y-6">
            <!-- Contact -->
            <div class="space-y-3">
              <h4 class="text-xl font-semibold">Liên hệ</h4>
              <p>
                <a
                  :href="`tel:${phone}`"
                  class="inline-flex items-center gap-2 hover:underline underline-offset-4"
                >
                  <IconPhone class="size-4" />
                  <span class="text-sm">{{ phone }}</span>
                </a>
              </p>
              <p>
                <a
                  :href="`mailto:${email}`"
                  class="inline-flex items-center gap-2 hover:underline underline-offset-4"
                >
                  <IconMail class="size-4" />
                  <span class="text-sm">{{ email }}</span>
                </a>
              </p>
            </div>

            <!-- Hours -->
            <div class="space-y-3">
              <h4 class="text-lg font-semibold">Giờ mở cửa</h4>
              <p class="text-sm text-white/90">{{ hours }}</p>
              <div
                class="rounded-lg bg-white/10 ring-1 ring-white/10 px-3 py-2 text-xs text-white/90"
              >
                🎉 Ưu đãi tiệc sinh nhật từ 10 khách
              </div>
            </div>

            <!-- Social -->
            <div class="space-y-3">
              <h4 class="text-lg font-semibold">Theo dõi chúng tôi</h4>
              <div class="flex items-center gap-3">
                <a
                  :href="`https://www.facebook.com/HOABANRESTAURANT`"
                  class="w-9 h-9 flex items-center justify-center rounded-full bg-white/20 hover:bg-white/30 transition-colors"
                  title="Facebook"
                >
                  <IconFacebook class="text-white w-4 h-4" />
                </a>
                <a
                  href="#"
                  class="w-9 h-9 flex items-center justify-center rounded-full bg-white/20 hover:bg-white/30 transition-colors"
                  title="WhatsApp"
                >
                  <IconWhatsApp class="text-white w-4 h-4" />
                </a>
                <a
                  href="#"
                  class="w-9 h-9 flex items-center justify-center rounded-full bg-white/20 hover:bg-white/30 transition-colors"
                  title="Instagram"
                >
                  <IconInstagram class="text-white w-4 h-4" />
                </a>
              </div>
            </div>
          </section>
        </div>
      </div>
    </div>

    <!-- Bottom -->
    <div class="bg-[#5b0808]/95 border-t border-white/10">
      <div
        class="mx-auto max-w-7xl px-6 py-4 text-center text-sm text-white/80 flex flex-col md:flex-row items-center justify-between gap-3"
      >
        <p>© {{ currentYear }} {{ brandName }}. All rights reserved.</p>
        <nav class="flex items-center gap-4">
          <a href="/privacy" class="hover:text-white">Chính sách bảo mật</a>
          <span class="opacity-40">•</span>
          <a href="/terms" class="hover:text-white">Điều khoản</a>
        </nav>
      </div>
    </div>
  </footer>
</template>

<script setup lang="ts">
import { ref, computed, onMounted } from "vue";
import { listCategories } from "@/api/categories.admin";
import type { Category } from "@/types";
import GoogleMap from "@/components/GoogleMap.vue";
import logoUrl from "@/assets/img/logo.png";

/* ====== Categories from API ====== */
const categories = ref<Category[]>([]);
const categoriesLoading = ref(true);

const displayCategories = computed(() => {
  return categories.value.slice(0, 7); // Giới hạn tối đa 7 categories
});

async function loadCategories() {
  try {
    categoriesLoading.value = true;
    const { data } = await listCategories();
    categories.value = data;
  } catch (error) {
    console.error("Failed to load categories:", error);
  } finally {
    categoriesLoading.value = false;
  }
}

onMounted(() => {
  loadCategories();
});

/* ====== Data (mock cứng) ====== */
const currentYear = new Date().getFullYear();
const brandName = "Hoa Ban Restaurant";
const address =
  "Nguyễn Phước Lan/Lô 50 Cầu Trung Lương, ngay chân, Cẩm Lệ, Đà Nẵng 550000, Việt Nam";
const phone = "0868030043";
const email = "HoaBan@gmail.com";
const hours = "9:00 – 22:00 mỗi ngày";

// Logo
const logo = logoUrl as string;

/* Mở Google Maps theo địa chỉ */
function openMap() {
  const q = encodeURIComponent(address);
  window.open(`https://maps.google.com/?q=${q}`, "_blank");
}

/* ====== Inline SVG Icons (nhẹ, sắc nét) ====== */
</script>

<!-- Icons as local components -->
<script lang="ts">
import { defineComponent, h } from "vue";

const base = "w-5 h-5";

export const IconPhone = defineComponent({
  name: "IconPhone",
  setup: () => () =>
    h("svg", { viewBox: "0 0 24 24", class: base, fill: "currentColor" }, [
      h("path", {
        d: "M6.6 10.8c1.3 2.6 3.4 4.7 6 6l1.9-1.9c.3-.3.7-.4 1.1-.2 1 .4 2.1.6 3.2.6.6 0 1 .5 1 1v3.1c0 .5-.4.9-.9 1C17 21.9 11.7 20 7.8 16.2A18 18 0 0 1 2.6 6.1c.1-.5.5-.9 1-1H6.7c.6 0 1 .5 1 1 0 1.1.2 2.2.6 3.2.1.4 0 .8-.2 1.1l-1.5 1.4Z",
      }),
    ]),
});

export const IconMail = defineComponent({
  name: "IconMail",
  setup: () => () =>
    h("svg", { viewBox: "0 0 24 24", class: base, fill: "currentColor" }, [
      h("path", {
        d: "M20 4H4a2 2 0 0 0-2 2v12c0 1.1.9 2 2 2h16a2 2 0 0 0 2-2V6a2 2 0 0 0-2-2Zm0 4-8 5L4 8V6l8 5 8-5v2Z",
      }),
    ]),
});

export const IconLocation = defineComponent({
  name: "IconLocation",
  setup: () => () =>
    h("svg", { viewBox: "0 0 24 24", class: base, fill: "currentColor" }, [
      h("path", {
        d: "M12 2a7 7 0 0 0-7 7c0 5.2 7 13 7 13s7-7.8 7-13a7 7 0 0 0-7-7Zm0 9.5a2.5 2.5 0 1 1 0-5 2.5 2.5 0 0 1 0 5Z",
      }),
    ]),
});

export const IconFacebook = defineComponent({
  name: "IconFacebook",
  setup: () => () =>
    h("svg", { viewBox: "0 0 24 24", class: base, fill: "currentColor" }, [
      h("path", {
        d: "M22 12a10 10 0 1 0-11.6 9.9v-7H7.9V12h2.5V9.8c0-2.5 1.5-3.9 3.8-3.9 1.1 0 2.2.2 2.2.2v2.4h-1.3c-1.2 0-1.6.8-1.6 1.6V12h2.7l-.4 2.9h-2.3v7A10 10 0 0 0 22 12Z",
      }),
    ]),
});

export const IconWhatsApp = defineComponent({
  name: "IconWhatsApp",
  setup: () => () =>
    h("svg", { viewBox: "0 0 24 24", class: base, fill: "currentColor" }, [
      h("path", {
        d: "M20.5 3.5A10 10 0 0 0 3.1 17.8L2 22l4.3-1.1A10 10 0 0 0 20.5 3.5Zm-8.5 16a8 8 0 1 1 6.7-3.6l-.3.4a8 8 0 0 1-6.4 3.2ZM8.8 7.7c.2-.5.5-.5.8-.5h.7c.2 0 .5 0 .6.5.2.5.7 1.7.8 1.8.1.2.1.3 0 .5-.2.2-.4.6-.6.8-.2.3-.4.5-.2.8.2.2.7 1.2 1.7 1.9 1.2.8 1.9 1 2.2.8.3-.2.5-.6.7-.8.2-.2.4-.2.6-.1l1.8.9c.2.1.5.2.6.4.1.2.1.9-.2 1.7-.3.7-1.3 1.3-2.2 1.3-1 .1-2.6-.4-4.2-1.5-1.6-1.1-2.9-3-3.3-3.6-.4-.6-1-1.8-1.1-2.7 0-.8.3-1.5.5-1.9Z",
      }),
    ]),
});

export const IconInstagram = defineComponent({
  name: "IconInstagram",
  setup: () => () =>
    h("svg", { viewBox: "0 0 24 24", class: base, fill: "currentColor" }, [
      h("path", {
        d: "M12 2.2c3 0 3.3 0 4.5.1 1.1.1 1.8.2 2.4.5.6.3 1 .6 1.5 1.1.5.5.8.9 1.1 1.5.3.6.4 1.3.5 2.4.1 1.2.1 1.5.1 4.5s0 3.3-.1 4.5c-.1 1.1-.2 1.8-.5 2.4a4 4 0 0 1-1.1 1.5 4 4 0 0 1-1.5 1.1c-.6.3-1.3.4-2.4.5-1.2.1-1.5.1-4.5.1s-3.3 0-4.5-.1c-1.1-.1-1.8-.2-2.4-.5a4 4 0 0 1-1.5-1.1 4 4 0 0 1-1.1-1.5c-.3-.6-.4-1.3-.5-2.4C2.2 15.3 2.2 15 2.2 12s0-3.3.1-4.5c.1-1.1.2-1.8.5-2.4.3-.6.6-1 .1-1.5.5-.5.9-.8 1.5-1.1.6-.3 1.3-.4 2.4-.5C8.7 2.2 9 2.2 12 2.2Zm0 5.1a4.7 4.7 0 1 1 0 9.4 4.7 4.7 0 0 1 0-9.4Zm0 1.8a2.9 2.9 0 1 0 0 5.8 2.9 2.9 0 0 0 0-5.8Zm5.3-2.2a1.1 1.1 0 1 1 0 2.2 1.1 1.1 0 0 1 0-2.2Z",
      }),
    ]),
});

export default {
  components: { IconPhone, IconMail, IconLocation, IconFacebook, IconWhatsApp, IconInstagram },
};
</script>
