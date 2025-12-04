<script setup lang="ts">
import { ref, onMounted, nextTick, watch, onUnmounted } from "vue";
import { listDishes } from "@/api/dishes.service";
import { getSuggestedMenu, listCombos } from "@/api/combos.service";
import { createReservation } from "@/api/reservations.service";
import { listAreas } from "@/api/areas.admin";
import type { Dish } from "@/types/dish.types";
import type { Combo } from "@/types/combo.types";
import type { AreaDto } from "@/types/area.types";
import { toast } from "vue3-toastify";
import DishModal from "@/components/DishModal.vue";
import ComboCard from "@/components/ComboCard.vue";
import { useUiStore } from "@/stores/ui";
import { useRouter } from "vue-router";
import { useAuthStore } from "@/stores/auth";
import CustomerReviews from "@/components/CustomerReviews.vue";

const ui = useUiStore();
const router = useRouter();
const auth = useAuthStore();
const featuredDishes = ref<Dish[]>([]);
const featuredCombos = ref<Combo[]>([]);
const loading = ref(true);
const areas = ref<AreaDto[]>([]);

// Form data for reservation
const customerName = ref("");
const customerPhone = ref("");
const date = ref("");
const time = ref("");
const partySize = ref(2);
const area = ref("");
const note = ref("");
const isLoading = ref(false);

// Scroll reveal observer
let observer: IntersectionObserver | null = null;

const setupScrollReveal = () => {
  // Add js-enabled class to enable scroll reveal effects
  document.documentElement.classList.add("js-enabled");

  const options = {
    root: null,
    rootMargin: "0px",
    threshold: 0.1,
  };

  observer = new IntersectionObserver((entries) => {
    entries.forEach((entry) => {
      if (entry.isIntersecting) {
        entry.target.classList.add("is-visible");
      }
    });
  }, options);

  // Observe all elements with scroll-reveal classes
  const elements = document.querySelectorAll(
    ".scroll-reveal, .scroll-reveal-left, .scroll-reveal-right, .scroll-reveal-scale"
  );
  elements.forEach((el) => observer?.observe(el));
};

const resolveImage = (imagePath: string) => {
  try {
    return new URL(imagePath, import.meta.url).href;
  } catch (e) {
    return new URL("/src/assets/img/logo.png", import.meta.url).href;
  }
};

// Banner carousel state
const currentSlide = ref(0);
const totalSlides = 5;
let slideInterval: number | null = null;

// Auto-advance slides
const startSlideshow = () => {
  stopSlideshow();
  slideInterval = window.setInterval(() => {
    nextSlide();
  }, 5000); // Change slide every 5 seconds
};

const stopSlideshow = () => {
  if (slideInterval) {
    clearInterval(slideInterval);
    slideInterval = null;
  }
};

const nextSlide = () => {
  currentSlide.value = (currentSlide.value + 1) % totalSlides;
};

const prevSlide = () => {
  currentSlide.value = (currentSlide.value - 1 + totalSlides) % totalSlides;
};

// Banner image (legacy, kept for compatibility)
const bannerImg = new URL("../assets/img/phong_vip.jpg", import.meta.url).href;

const fetchFeaturedItems = async () => {
  try {
    loading.value = true;

    // Try to get suggested menu first (requires auth)
    let suggestedData: { dishes: any[]; combos: any[] } = { dishes: [], combos: [] };
    let areasData: any[] = [];

    try {
      console.log("📊 Fetching suggested menu...");
      const [suggestedMenuResponse, areasResponse] = await Promise.all([
        getSuggestedMenu(),
        listAreas(),
      ]);

      console.log("✅ Suggested menu response:", suggestedMenuResponse);
      console.log("✅ Areas response:", areasResponse);

      suggestedData = suggestedMenuResponse?.data || { dishes: [], combos: [] };
      areasData = areasResponse?.data || [];
    } catch (suggestedError: any) {
      // If suggested menu fails (e.g., user not logged in), fall back to list APIs
      console.log("⚠️ Suggested menu failed, using fallback lists");
      console.log("Error:", suggestedError);

      const [dishesResponse, combosResponse, areasResponse] = await Promise.all([
        listDishes({ page: 0, size: 8 }),
        listCombos(),
        listAreas(),
      ]);

      // Get top 4 dishes from list
      const dishesData = dishesResponse?.data?.content || [];
      suggestedData.dishes = (Array.isArray(dishesData) ? dishesData : []).slice(0, 4);

      // Get top 4 active combos
      const combosData = combosResponse?.data || combosResponse || [];
      suggestedData.combos = (Array.isArray(combosData) ? combosData : [])
        .filter((combo: any) => combo.status === "ACTIVE")
        .slice(0, 4);

      areasData = areasResponse?.data || [];
    }

    // Set data
    featuredDishes.value = Array.isArray(suggestedData.dishes) ? suggestedData.dishes : [];
    featuredCombos.value = Array.isArray(suggestedData.combos) ? suggestedData.combos : [];
    areas.value = Array.isArray(areasData) ? areasData : [];

    console.log("Featured dishes count:", featuredDishes.value.length);
    console.log("Featured combos count:", featuredCombos.value.length);
    console.log("Areas count:", areas.value.length);
  } catch (error) {
    console.error("Error fetching featured items:", error);
    toast.error("Không thể tải dữ liệu món ăn. Vui lòng thử lại!");
  } finally {
    loading.value = false;
  }
};

const handleBooking = () => {
  router.push("/reservation");
};

// Generate time slots from 10:00 to 22:00
const generateTimeSlots = () => {
  const slots = [];
  for (let hour = 10; hour <= 22; hour++) {
    for (let minute of [0, 30]) {
      if (hour === 22 && minute === 30) break;
      slots.push(`${String(hour).padStart(2, "0")}:${String(minute).padStart(2, "0")}`);
    }
  }
  return slots;
};

const timeSlots = ref(generateTimeSlots());

// Form utilities
function toIsoLocal(d: string, t: string) {
  if (!d || !t) return "";
  const dt = new Date(`${d}T${t}:00`);
  return dt.toISOString();
}

// Auto-fill user information if logged in
const autoFillUserInfo = () => {
  if (auth.user && auth.token) {
    if (auth.user.fullName && !customerName.value) {
      customerName.value = auth.user.fullName;
    }
  }
};

// Form submission for reservation
async function submitReservation() {
  try {
    // Check if user is logged in
    if (!auth.token) {
      toast.info("Vui lòng đăng nhập để đặt bàn");
      router.push({ name: "login", query: { redirect: "/reservation" } });
      return;
    }

    if (
      !customerName.value ||
      !customerPhone.value ||
      !date.value ||
      !time.value ||
      !partySize.value
    ) {
      toast.error("Vui lòng điền đầy đủ thông tin!");
      return;
    }

    if (partySize.value < 1 || partySize.value > 20) {
      toast.error("Số lượng khách phải từ 1 đến 20 người");
      return;
    }

    isLoading.value = true;

    const startTime = toIsoLocal(date.value, time.value);

    // Tính endTime = startTime + 5 tiếng
    const startDate = new Date(startTime);
    const endDate = new Date(startDate.getTime() + 5 * 60 * 60 * 1000); // +5 hours
    const endTime = endDate.toISOString();

    // Tạo note gộp thông tin khách hàng
    const customerInfo = `Khách: ${customerName.value} | SĐT: ${customerPhone.value}`;
    const areaInfo = area.value ? ` | Khu vực: ${area.value}` : "";
    const userNote = note.value?.trim() ? ` | Ghi chú: ${note.value.trim()}` : "";
    const reservationNote = customerInfo + areaInfo + userNote;

    const payload = {
      startTime,
      endTime,
      partySize: partySize.value,
      note: reservationNote,
    };

    console.log("Reservation payload:", payload);

    await createReservation(payload);

    toast.success("Đặt bàn thành công! Chúng tôi sẽ liên hệ với bạn sớm nhất.");

    // Reset form but keep user info if logged in
    if (!auth.user || !auth.token) {
      customerName.value = "";
    }
    customerPhone.value = "";
    date.value = "";
    time.value = "";
    partySize.value = 2;
    area.value = "";
    note.value = "";

    // Auto-fill user info again after reset
    autoFillUserInfo();
  } catch (error: any) {
    toast.error(error?.response?.data?.message || "Có lỗi xảy ra khi đặt bàn");
  } finally {
    isLoading.value = false;
  }
}

// Gallery functionality
const setupGallery = () => {
  const images = [
    "https://images.unsplash.com/photo-1559339352-11d035aa65de?q=80&w=1400&auto=format&fit=crop",
    "https://images.unsplash.com/photo-1524231757912-21f4fe3a7200?q=80&w=1400&auto=format&fit=crop",
    "https://images.unsplash.com/photo-1504674900247-0877df9cc836?q=80&w=1400&auto=format&fit=crop",
    "https://images.unsplash.com/photo-1544025162-d76694265947?q=80&w=1400&auto=format&fit=crop",
  ];

  const mainImg = document.getElementById("gal-main") as HTMLImageElement | null;
  const prevBtn = document.getElementById("gal-prev") as HTMLButtonElement | null;
  const nextBtn = document.getElementById("gal-next") as HTMLButtonElement | null;
  const thumbsContainer = document.getElementById("gal-thumbs") as HTMLElement | null;

  if (!mainImg || !prevBtn || !nextBtn || !thumbsContainer) return;

  let currentIndex = 0;

  // Create thumbnails with improved styling
  images.forEach((src, index) => {
    const thumb = document.createElement("img");
    thumb.src = src;
    thumb.className = `w-full h-20 object-cover rounded-lg cursor-pointer ring-2 transition-all duration-300 ${
      index === 0 ? "ring-amber-400 shadow-lg scale-105" : "ring-transparent hover:ring-white/50"
    }`;
    thumb.addEventListener("click", () => setImage(index));
    thumbsContainer.appendChild(thumb);
  });

  // Set initial image counter
  const totalImgSpan = document.getElementById("total-img");
  if (totalImgSpan) {
    totalImgSpan.textContent = String(images.length);
  }

  const setImage = (index: number) => {
    currentIndex = index;
    const imgSrc = images[index];
    if (mainImg && imgSrc) {
      mainImg.src = imgSrc;
    }

    // Update image counter
    const currentImgSpan = document.getElementById("current-img");
    if (currentImgSpan) {
      currentImgSpan.textContent = String(index + 1);
    }

    // Update thumbnail highlights with improved styling
    Array.from(thumbsContainer.children).forEach((thumb, i) => {
      thumb.className = `w-full h-20 object-cover rounded-lg cursor-pointer ring-2 transition-all duration-300 ${
        i === index
          ? "ring-amber-400 shadow-lg scale-105 brightness-110"
          : "ring-transparent hover:ring-white/50 hover:scale-102"
      }`;
    });
  };

  prevBtn.addEventListener("click", () => {
    const newIndex = currentIndex === 0 ? images.length - 1 : currentIndex - 1;
    setImage(newIndex);
  });

  nextBtn.addEventListener("click", () => {
    const newIndex = currentIndex === images.length - 1 ? 0 : currentIndex + 1;
    setImage(newIndex);
  });
};

// Enhanced modal functionality
const setupModals = () => {
  const modalArea = document.getElementById("modal-area");
  const modalMap = document.getElementById("modal-map");
  const btnMap = document.getElementById("btn-map");
  const btnCall = document.getElementById("btn-call");

  // Setup area modal with API data
  setupAreaModal();

  // Set minimum date to today
  const dateInput = document.getElementById("reservation-date") as HTMLInputElement;
  if (dateInput) {
    const today = new Date().toISOString().split("T")[0];
    if (today) {
      dateInput.min = today;
      dateInput.value = today;
    }
  }

  // Call functionality
  btnCall?.addEventListener("click", () => {
    window.open("tel:+84901234567", "_self");
  });

  // Modal controls
  const closeButtons = document.querySelectorAll("[data-close]");
  closeButtons.forEach((btn) => {
    btn.addEventListener("click", () => {
      modalArea?.classList.add("hidden");
      modalMap?.classList.add("hidden");
    });
  });

  // Open map modal
  btnMap?.addEventListener("click", () => {
    modalMap?.classList.remove("hidden");
  });

  // Close on backdrop click
  [modalArea, modalMap].forEach((modal) => {
    modal?.addEventListener("click", (e) => {
      if (e.target === modal) {
        modal.classList.add("hidden");
      }
    });
  });

  // Form submission
  const form = document.getElementById("reserve-form") as HTMLFormElement;
  form?.addEventListener("submit", (e) => {
    e.preventDefault();
    submitReservation();
  });
};

// Setup area modal with API data
const setupAreaModal = () => {
  const areaList = document.getElementById("area-list");
  if (!areaList) return;

  // Default images for areas
  const areaDefaultImages: Record<string, string> = {
    outdoor:
      "https://images.unsplash.com/photo-1517248135467-4c7edcad34c4?q=80&w=1400&auto=format&fit=crop",
    indoor:
      "https://images.unsplash.com/photo-1555396273-367ea4eb4db5?q=80&w=1400&auto=format&fit=crop",
    private:
      "https://images.unsplash.com/photo-1559339352-11d035aa65de?q=80&w=1400&auto=format&fit=crop",
    vip: "https://images.unsplash.com/photo-1559339352-11d035aa65de?q=80&w=1400&auto=format&fit=crop",
    upstairs:
      "https://images.unsplash.com/photo-1571997478779-2adcbbe9ab2f?q=80&w=1400&auto=format&fit=crop",
    default:
      "https://images.unsplash.com/photo-1414235077428-338989a2e8c0?q=80&w=1400&auto=format&fit=crop",
  };

  // Hàm mapping tên khu vực thành ảnh
  const getAreaImage = (areaName: string): string => {
    const name = areaName.toLowerCase();

    // Kiểm tra các từ khóa trong tên
    if (name.includes("ngoài") || name.includes("outdoor") || name.includes("trời")) {
      return areaDefaultImages.outdoor!;
    }
    if (name.includes("trong") || name.includes("indoor") || name.includes("nhà")) {
      return areaDefaultImages.indoor!;
    }
    if (name.includes("vip") || name.includes("riêng") || name.includes("private")) {
      return areaDefaultImages.vip!;
    }
    if (name.includes("lầu") || name.includes("tầng") || name.includes("upstairs")) {
      return areaDefaultImages.upstairs!;
    }

    // Fallback: thử match trực tiếp với key
    return areaDefaultImages[name] || areaDefaultImages.default!;
  };

  // Clear existing content
  areaList.innerHTML = "";

  // Filter only ACTIVE areas
  const activeAreas = areas.value.filter((a: AreaDto) => a.status === "ACTIVE");

  if (activeAreas.length === 0) {
    areaList.innerHTML = '<p class="text-gray-500 text-sm">Không có khu vực nào</p>';
    return;
  }

  // Render area buttons
  activeAreas.forEach((areaData: AreaDto, index: number) => {
    const button = document.createElement("button");
    button.className =
      "w-full text-left p-3 rounded-lg border-2 border-gray-200 hover:border-[#9f0909] hover:bg-gray-50 transition-all";
    button.innerHTML = `
      <div class="font-semibold text-gray-900">${areaData.name}</div>
      <div class="text-sm text-gray-600 mt-1">${
        areaData.description || "Không gian thoải mái và sang trọng"
      }</div>
    `;

    button.addEventListener("click", () => {
      // Update preview
      const previewImg = document.getElementById("area-prev-img") as HTMLImageElement;
      const title = document.getElementById("area-title");
      const meta = document.getElementById("area-meta");

      const imageUrl = getAreaImage(areaData.name);
      console.log(`Selected area "${areaData.name}", using image:`, imageUrl);

      if (previewImg) previewImg.src = imageUrl;
      if (title) title.textContent = areaData.name;
      if (meta) meta.textContent = areaData.description || "Không gian thoải mái và sang trọng";

      // Highlight selected button
      areaList.querySelectorAll("button").forEach((btn) => {
        btn.classList.remove("border-[#9f0909]", "bg-gray-50");
      });
      button.classList.add("border-[#9f0909]", "bg-gray-50");

      // Setup choose button
      const chooseBtn = document.getElementById("area-choose");
      if (chooseBtn) {
        chooseBtn.onclick = () => {
          const areaSelect = document.getElementById("area-select") as HTMLSelectElement;
          if (areaSelect) {
            areaSelect.value = areaData.name;
            area.value = areaData.name;
          }
          document.getElementById("modal-area")?.classList.add("hidden");
        };
      }
    });

    areaList.appendChild(button);

    // Auto-select first area
    if (index === 0) {
      button.click();
    }
  });
};

onMounted(() => {
  fetchFeaturedItems();

  // Auto-fill user info if logged in
  autoFillUserInfo();

  // Start banner slideshow
  startSlideshow();

  // Setup all components after DOM is mounted
  nextTick(() => {
    setupGallery();
    setupModals();
    setupScrollReveal();
  });
});

onUnmounted(() => {
  if (observer) {
    observer.disconnect();
  }
  stopSlideshow();
});

// Watch for user changes
watch(() => auth.user, autoFillUserInfo, { immediate: true });
watch(() => auth.token, autoFillUserInfo);
</script>

<template>
  <div class="min-h-screen bg-white">
    <!-- Hero Banner Carousel - Jollibee Style -->
    <section
      id="trang-chu"
      class="relative h-[600px] overflow-hidden bg-gradient-to-br from-orange-50 to-yellow-50"
    >
      <!-- Banner Slides -->
      <div class="banner-container relative h-full">
        <!-- Slide 1: Thịt Trâu Gác Bếp -->
        <div
          :class="[
            'banner-slide',
            'absolute',
            'inset-0',
            'transition-opacity',
            'duration-1000',
            currentSlide === 0 ? 'opacity-100 z-10' : 'opacity-0 z-0',
          ]"
        >
          <div class="grid md:grid-cols-2 h-full items-center max-w-7xl mx-auto px-8">
            <!-- Left Content -->
            <div class="flex flex-col justify-center space-y-6 z-20">
              <div class="inline-block">
                <span
                  class="bg-gradient-to-r from-red-600 to-orange-500 text-white px-6 py-2 rounded-full text-sm font-bold uppercase tracking-wider shadow-lg"
                >
                  Đặc Sản Tây Bắc
                </span>
              </div>
              <h1 class="text-5xl md:text-7xl font-black text-gray-900 leading-tight">
                Thịt Trâu<br />
                <span
                  class="text-transparent bg-clip-text bg-gradient-to-r from-red-600 to-orange-500"
                >
                  Gác Bếp
                </span>
              </h1>
              <p class="text-xl text-gray-700 font-semibold max-w-md">
                Hương vị đậm đà, thơm ngon của núi rừng Tây Bắc
              </p>
              <div class="flex items-baseline space-x-4">
                <span class="text-5xl font-black text-red-600">250.000đ</span>
                <span class="text-2xl text-gray-500 line-through">300.000đ</span>
              </div>
              <button
                @click="router.push('/menu')"
                class="bg-gradient-to-r from-red-600 to-orange-500 hover:from-red-700 hover:to-orange-600 text-white px-10 py-4 rounded-full text-lg font-bold shadow-xl hover:shadow-2xl transform hover:scale-105 transition-all duration-300 w-fit"
              >
                <span class="flex items-center space-x-2">
                  <span>Đặt Món Ngay</span>
                  <svg class="w-6 h-6" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                    <path
                      stroke-linecap="round"
                      stroke-linejoin="round"
                      stroke-width="2"
                      d="M14 5l7 7m0 0l-7 7m7-7H3"
                    ></path>
                  </svg>
                </span>
              </button>
            </div>
            <!-- Right Image -->
            <div class="relative h-full flex items-center justify-center">
              <div
                class="absolute inset-0 bg-gradient-to-br from-yellow-200/30 to-orange-200/30 rounded-full blur-3xl"
              ></div>
              <img
                src="https://images.unsplash.com/photo-1529692236671-f1f6cf9683ba?w=800&h=800&fit=crop"
                alt="Thịt Trâu Gác Bếp"
                class="relative z-10 w-[500px] h-[500px] object-cover rounded-3xl shadow-2xl transform hover:scale-105 transition-transform duration-500"
                loading="lazy"
              />
            </div>
          </div>
        </div>

        <!-- Slide 2: Cá Hồi Sapa -->
        <div
          :class="[
            'banner-slide',
            'absolute',
            'inset-0',
            'transition-opacity',
            'duration-1000',
            currentSlide === 1 ? 'opacity-100 z-10' : 'opacity-0 z-0',
          ]"
        >
          <div class="grid md:grid-cols-2 h-full items-center max-w-7xl mx-auto px-8">
            <div class="flex flex-col justify-center space-y-6 z-20">
              <div class="inline-block">
                <span
                  class="bg-gradient-to-r from-blue-600 to-cyan-500 text-white px-6 py-2 rounded-full text-sm font-bold uppercase tracking-wider shadow-lg"
                >
                  Tươi Ngon Mỗi Ngày
                </span>
              </div>
              <h1 class="text-5xl md:text-7xl font-black text-gray-900 leading-tight">
                Cá Hồi<br />
                <span
                  class="text-transparent bg-clip-text bg-gradient-to-r from-blue-600 to-cyan-500"
                >
                  Sapa
                </span>
              </h1>
              <p class="text-xl text-gray-700 font-semibold max-w-md">
                Tươi sống từ vùng cao, thịt ngọt thơm béo ngậy
              </p>
              <div class="flex items-baseline space-x-4">
                <span class="text-5xl font-black text-blue-600">180.000đ</span>
                <span class="text-lg text-gray-600">/phần</span>
              </div>
              <button
                @click="router.push('/menu')"
                class="bg-gradient-to-r from-blue-600 to-cyan-500 hover:from-blue-700 hover:to-cyan-600 text-white px-10 py-4 rounded-full text-lg font-bold shadow-xl hover:shadow-2xl transform hover:scale-105 transition-all duration-300 w-fit"
              >
                <span class="flex items-center space-x-2">
                  <span>Xem Thực Đơn</span>
                  <svg class="w-6 h-6" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                    <path
                      stroke-linecap="round"
                      stroke-linejoin="round"
                      stroke-width="2"
                      d="M14 5l7 7m0 0l-7 7m7-7H3"
                    ></path>
                  </svg>
                </span>
              </button>
            </div>
            <div class="relative h-full flex items-center justify-center">
              <div
                class="absolute inset-0 bg-gradient-to-br from-blue-200/30 to-cyan-200/30 rounded-full blur-3xl"
              ></div>
              <img
                src="https://images.unsplash.com/photo-1519708227418-c8fd9a32b7a2?w=800&h=800&fit=crop"
                alt="Cá Hồi Sapa"
                class="relative z-10 w-[500px] h-[500px] object-cover rounded-3xl shadow-2xl transform hover:scale-105 transition-transform duration-500"
                loading="lazy"
              />
            </div>
          </div>
        </div>

        <!-- Slide 3: Combo Tây Bắc -->
        <div
          :class="[
            'banner-slide',
            'absolute',
            'inset-0',
            'transition-opacity',
            'duration-1000',
            currentSlide === 2 ? 'opacity-100 z-10' : 'opacity-0 z-0',
          ]"
        >
          <div class="grid md:grid-cols-2 h-full items-center max-w-7xl mx-auto px-8">
            <div class="flex flex-col justify-center space-y-6 z-20">
              <div class="inline-block">
                <span
                  class="bg-gradient-to-r from-amber-600 to-yellow-500 text-white px-6 py-2 rounded-full text-sm font-bold uppercase tracking-wider shadow-lg"
                >
                  🔥 Combo Siêu Hot
                </span>
              </div>
              <h1 class="text-5xl md:text-7xl font-black text-gray-900 leading-tight">
                Combo<br />
                <span
                  class="text-transparent bg-clip-text bg-gradient-to-r from-amber-600 to-yellow-500"
                >
                  Tây Bắc
                </span>
              </h1>
              <p class="text-xl text-gray-700 font-semibold max-w-md">
                Trọn vẹn 4 món đặc sản + nước uống chỉ với
              </p>
              <div class="flex items-baseline space-x-4">
                <span class="text-5xl font-black text-amber-600">399.000đ</span>
                <span class="text-2xl text-gray-500 line-through">550.000đ</span>
              </div>
              <div
                class="bg-gradient-to-r from-amber-100 to-yellow-100 border-2 border-amber-400 rounded-2xl p-4 max-w-md"
              >
                <p class="text-amber-800 font-bold">🎁 Tặng kèm rau rừng + nước chấm đặc biệt</p>
              </div>
              <button
                @click="router.push('/menu')"
                class="bg-gradient-to-r from-amber-600 to-yellow-500 hover:from-amber-700 hover:to-yellow-600 text-white px-10 py-4 rounded-full text-lg font-bold shadow-xl hover:shadow-2xl transform hover:scale-105 transition-all duration-300 w-fit"
              >
                <span class="flex items-center space-x-2">
                  <span>Đặt Combo Ngay</span>
                  <svg class="w-6 h-6" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                    <path
                      stroke-linecap="round"
                      stroke-linejoin="round"
                      stroke-width="2"
                      d="M14 5l7 7m0 0l-7 7m7-7H3"
                    ></path>
                  </svg>
                </span>
              </button>
            </div>
            <div class="relative h-full flex items-center justify-center">
              <div
                class="absolute inset-0 bg-gradient-to-br from-amber-200/30 to-yellow-200/30 rounded-full blur-3xl"
              ></div>
              <img
                src="https://images.unsplash.com/photo-1504674900247-0877df9cc836?w=800&h=800&fit=crop"
                alt="Combo Tây Bắc"
                class="relative z-10 w-[500px] h-[500px] object-cover rounded-3xl shadow-2xl transform hover:scale-105 transition-transform duration-500"
                loading="lazy"
              />
            </div>
          </div>
        </div>

        <!-- Slide 4: Lợn Cắp Nách -->
        <div
          :class="[
            'banner-slide',
            'absolute',
            'inset-0',
            'transition-opacity',
            'duration-1000',
            currentSlide === 3 ? 'opacity-100 z-10' : 'opacity-0 z-0',
          ]"
        >
          <div class="grid md:grid-cols-2 h-full items-center max-w-7xl mx-auto px-8">
            <div class="flex flex-col justify-center space-y-6 z-20">
              <h1 class="text-5xl md:text-7xl font-black text-gray-900 leading-tight">
                Sườn Nướng Tảng<br />
                <span
                  class="text-transparent bg-clip-text bg-gradient-to-r from-rose-600 to-pink-500"
                >
                  Hoa Ban
                </span>
              </h1>
              <p class="text-xl text-gray-700 font-semibold max-w-md">
                Đặc sản núi rừng - Thịt mềm thơm, đẫm vị
              </p>
              <div class="flex items-baseline space-x-4">
                <span class="text-5xl font-black text-rose-600">280.000đ</span>
                <span class="text-lg text-gray-600">/kg</span>
              </div>
              <div
                class="bg-gradient-to-r from-rose-100 to-pink-100 border-2 border-rose-400 rounded-2xl p-4 max-w-md"
              >
                <p class="text-rose-800 font-bold">
                  ✨ Ướp gia vị bí truyền theo công thức dân tộc
                </p>
              </div>
              <button
                @click="router.push('/menu')"
                class="bg-gradient-to-r from-rose-600 to-pink-500 hover:from-rose-700 hover:to-pink-600 text-white px-10 py-4 rounded-full text-lg font-bold shadow-xl hover:shadow-2xl transform hover:scale-105 transition-all duration-300 w-fit"
              >
                <span class="flex items-center space-x-2">
                  <span>Đặt Món Ngay</span>
                  <svg class="w-6 h-6" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                    <path
                      stroke-linecap="round"
                      stroke-linejoin="round"
                      stroke-width="2"
                      d="M14 5l7 7m0 0l-7 7m7-7H3"
                    ></path>
                  </svg>
                </span>
              </button>
            </div>
            <div class="relative h-full flex items-center justify-center">
              <div
                class="absolute inset-0 bg-gradient-to-br from-rose-200/30 to-pink-200/30 rounded-full blur-3xl"
              ></div>
              <img
                src="https://images.unsplash.com/photo-1544025162-d76694265947?w=800&h=800&fit=crop"
                alt="Lợn Cắp Nách"
                class="relative z-10 w-[500px] h-[500px] object-cover rounded-3xl shadow-2xl transform hover:scale-105 transition-transform duration-500"
                loading="lazy"
              />
            </div>
          </div>
        </div>

        <!-- Slide 5: Xôi Ngũ Sắc -->
        <div
          :class="[
            'banner-slide',
            'absolute',
            'inset-0',
            'transition-opacity',
            'duration-1000',
            currentSlide === 4 ? 'opacity-100 z-10' : 'opacity-0 z-0',
          ]"
        >
          <div class="grid md:grid-cols-2 h-full items-center max-w-7xl mx-auto px-8">
            <div class="flex flex-col justify-center space-y-6 z-20">
              <div class="inline-block">
                <span
                  class="bg-gradient-to-r from-purple-600 to-indigo-500 text-white px-6 py-2 rounded-full text-sm font-bold uppercase tracking-wider shadow-lg"
                >
                  🌈 Đặc Sản Truyền Thống
                </span>
              </div>
              <h1 class="text-5xl md:text-7xl font-black text-gray-900 leading-tight">
                Xôi<br />
                <span
                  class="text-transparent bg-clip-text bg-gradient-to-r from-purple-600 via-pink-500 to-indigo-500"
                >
                  Ngũ Sắc
                </span>
              </h1>
              <p class="text-xl text-gray-700 font-semibold max-w-md">
                5 màu sắc thiên nhiên - Tinh hoa ẩm thực người Thái
              </p>
              <div class="flex items-baseline space-x-4">
                <span class="text-5xl font-black text-purple-600">45.000đ</span>
                <span class="text-lg text-gray-600">/phần</span>
              </div>
              <div
                class="bg-gradient-to-r from-purple-100 to-indigo-100 border-2 border-purple-400 rounded-2xl p-4 max-w-md"
              >
                <p class="text-purple-800 font-bold">🌿 Nhuộm từ lá rừng thiên nhiên 100%</p>
              </div>
              <button
                @click="router.push('/menu')"
                class="bg-gradient-to-r from-purple-600 to-indigo-500 hover:from-purple-700 hover:to-indigo-600 text-white px-10 py-4 rounded-full text-lg font-bold shadow-xl hover:shadow-2xl transform hover:scale-105 transition-all duration-300 w-fit"
              >
                <span class="flex items-center space-x-2">
                  <span>Thử Ngay</span>
                  <svg class="w-6 h-6" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                    <path
                      stroke-linecap="round"
                      stroke-linejoin="round"
                      stroke-width="2"
                      d="M14 5l7 7m0 0l-7 7m7-7H3"
                    ></path>
                  </svg>
                </span>
              </button>
            </div>
            <div class="relative h-full flex items-center justify-center">
              <div
                class="absolute inset-0 bg-gradient-to-br from-purple-200/30 to-indigo-200/30 rounded-full blur-3xl"
              ></div>
              <img
                src="https://i.ytimg.com/vi/11xNlOfZ9S0/maxresdefault.jpg"
                alt="Xôi Ngũ Sắc"
                class="relative z-10 w-[400px] h-[400px] object-cover rounded-3xl shadow-2xl transform hover:scale-105 transition-transform duration-500"
                loading="lazy"
              />
            </div>
          </div>
        </div>

        <!-- Navigation Buttons -->
        <button
          @click="prevSlide"
          class="absolute left-8 top-1/2 -translate-y-1/2 z-30 bg-white/90 hover:bg-white p-4 rounded-full shadow-xl hover:shadow-2xl transform hover:scale-110 transition-all duration-300"
        >
          <svg class="w-8 h-8 text-gray-800" fill="none" stroke="currentColor" viewBox="0 0 24 24">
            <path
              stroke-linecap="round"
              stroke-linejoin="round"
              stroke-width="3"
              d="M15 19l-7-7 7-7"
            ></path>
          </svg>
        </button>
        <button
          @click="nextSlide"
          class="absolute right-8 top-1/2 -translate-y-1/2 z-30 bg-white/90 hover:bg-white p-4 rounded-full shadow-xl hover:shadow-2xl transform hover:scale-110 transition-all duration-300"
        >
          <svg class="w-8 h-8 text-gray-800" fill="none" stroke="currentColor" viewBox="0 0 24 24">
            <path
              stroke-linecap="round"
              stroke-linejoin="round"
              stroke-width="3"
              d="M9 5l7 7-7 7"
            ></path>
          </svg>
        </button>

        <!-- Slide Indicators -->
        <div class="absolute bottom-8 left-1/2 -translate-x-1/2 z-30 flex space-x-3">
          <button
            v-for="(_, index) in 5"
            :key="index"
            @click="currentSlide = index"
            :class="[
              'h-3 rounded-full transition-all duration-300',
              currentSlide === index ? 'w-12 bg-orange-600' : 'w-3 bg-white/50 hover:bg-white/80',
            ]"
          ></button>
        </div>
      </div>
    </section>

    <!-- Quick Info Section -->
    <section class="py-4 bg-gradient-to-r from-orange-600 to-red-600 text-white">
      <div class="max-w-7xl mx-auto px-4">
        <div class="grid grid-cols-1 md:grid-cols-3 gap-4 text-center">
          <div class="flex items-center justify-center space-x-3">
            <svg class="w-8 h-8" fill="currentColor" viewBox="0 0 20 20">
              <path d="M9 2a1 1 0 000 2h2a1 1 0 100-2H9z"></path>
              <path
                fill-rule="evenodd"
                d="M4 5a2 2 0 012-2 3 3 0 003 3h2a3 3 0 003-3 2 2 0 012 2v11a2 2 0 01-2 2H6a2 2 0 01-2-2V5zm3 4a1 1 0 000 2h.01a1 1 0 100-2H7zm3 0a1 1 0 000 2h3a1 1 0 100-2h-3zm-3 4a1 1 0 100 2h.01a1 1 0 100-2H7zm3 0a1 1 0 100 2h3a1 1 0 100-2h-3z"
                clip-rule="evenodd"
              ></path>
            </svg>
            <p class="font-bold text-lg">Hương vị thiên nhiên - Sức khỏe trọn vẹn</p>
          </div>
          <div class="flex items-center justify-center space-x-3">
            <svg class="w-8 h-8" fill="currentColor" viewBox="0 0 20 20">
              <path
                fill-rule="evenodd"
                d="M5 2a1 1 0 011 1v1h1a1 1 0 010 2H6v1a1 1 0 01-2 0V6H3a1 1 0 010-2h1V3a1 1 0 011-1zm0 10a1 1 0 011 1v1h1a1 1 0 110 2H6v1a1 1 0 11-2 0v-1H3a1 1 0 110-2h1v-1a1 1 0 011-1zM12 2a1 1 0 01.967.744L14.146 7.2 17.5 9.134a1 1 0 010 1.732l-3.354 1.935-1.18 4.455a1 1 0 01-1.933 0L9.854 12.8 6.5 10.866a1 1 0 010-1.732l3.354-1.935 1.18-4.455A1 1 0 0112 2z"
                clip-rule="evenodd"
              ></path>
            </svg>
            <p class="font-bold text-lg">🚀 Giao hàng nhanh 30 phút</p>
          </div>
          <div class="flex items-center justify-center space-x-3">
            <svg class="w-8 h-8" fill="currentColor" viewBox="0 0 20 20">
              <path
                d="M2 6a2 2 0 012-2h6a2 2 0 012 2v8a2 2 0 01-2 2H4a2 2 0 01-2-2V6zM14.553 7.106A1 1 0 0014 8v4a1 1 0 00.553.894l2 1A1 1 0 0018 13V7a1 1 0 00-1.447-.894l-2 1z"
              ></path>
            </svg>
            <p class="font-bold text-lg">💯 Cam kết 100% nguyên liệu sạch</p>
          </div>
        </div>
      </div>
    </section>

    <!-- Featured Dishes -->
    <section id="thuc-don" class="py-20 bg-gradient-to-b from-orange-50 via-white to-gray-50">
      <div class="max-w-7xl mx-auto px-4">
        <div class="text-center mb-16 scroll-reveal">
          <h2 class="text-5xl md:text-6xl font-black text-gray-900 mb-6">Món Ăn Nổi Bật</h2>
          <p class="text-xl md:text-2xl text-gray-600 font-medium">
            Thưởng thức ẩm thực Tây Bắc tại Đà Nẵng
          </p>
          <div
            class="mt-6 h-1.5 w-40 mx-auto bg-gradient-to-r from-orange-400 via-red-500 to-orange-400 rounded-full shadow-lg"
          ></div>
        </div>

        <div v-if="loading" class="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-4 gap-8">
          <div v-for="i in 8" :key="i" class="shimmer rounded-2xl h-96"></div>
        </div>

        <div v-else class="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-4 gap-8 mb-12">
          <div
            v-for="(dish, index) in featuredDishes"
            :key="dish.id"
            class="bg-white rounded-3xl overflow-hidden hover:shadow-2xl transition-all shadow-lg border-2 border-gray-100 hover:border-orange-200 hover-lift group animate-fade-in-up"
            :style="`animation-delay: ${index * 0.1}s`"
          >
            <div class="h-52 rounded-3xl overflow-hidden m-4 mb-0 relative">
              <img
                :src="dish.imageUrl || resolveImage('/src/assets/img/logo.png')"
                :alt="`Món ${dish.name} - ${dish.categoryName || 'Ẩm thực Hoa Ban'}`"
                loading="lazy"
                class="w-full h-full object-cover transition-transform duration-500 group-hover:scale-110"
              />
              <div
                class="absolute inset-0 bg-gradient-to-t from-black/50 to-transparent opacity-0 group-hover:opacity-100 transition-opacity duration-300"
              ></div>
              <!-- Best Seller badge -->
              <div
                v-if="dish.signature"
                class="absolute top-3 left-3 bg-gradient-to-r from-yellow-400 to-orange-500 text-white px-3 py-1.5 rounded-full text-xs font-bold shadow-lg flex items-center gap-1"
              >
                <span>⭐</span>
                <span>Best Seller</span>
              </div>
              <!-- Price badge -->
              <div
                class="absolute top-3 right-3 bg-[#9f0909] text-white px-3 py-1.5 rounded-full text-sm font-bold shadow-lg"
              >
                {{ new Intl.NumberFormat("vi-VN").format(dish.price) }}đ
              </div>
            </div>

            <div class="p-6">
              <h3
                class="text-lg font-bold text-gray-900 mb-2 group-hover:text-[#9f0909] transition-colors"
              >
                {{ dish.name }}
              </h3>
              <p class="text-gray-600 mb-4 line-clamp-2 text-sm">
                {{ dish.description || "Món ăn ngon từ thiên nhiên" }}
              </p>

              <button
                @click="ui.openDish(dish.id)"
                class="w-full bg-gradient-to-r from-orange-600 to-red-600 hover:from-orange-700 hover:to-red-700 text-white py-3.5 rounded-full font-bold text-base transition-all transform hover:scale-105 active:scale-95 shadow-lg hover:shadow-xl flex items-center justify-center gap-2"
              >
                <span>Xem chi tiết</span>
                <svg class="w-5 h-5" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                  <path
                    stroke-linecap="round"
                    stroke-linejoin="round"
                    stroke-width="2.5"
                    d="M13 7l5 5m0 0l-5 5m5-5H6"
                  ></path>
                </svg>
              </button>
            </div>
          </div>
        </div>

        <div class="text-center animate-fade-in-up" style="animation-delay: 0.8s">
          <router-link
            to="/menu"
            class="inline-flex items-center gap-3 bg-gradient-to-r from-orange-600 to-red-600 hover:from-orange-700 hover:to-red-700 text-white px-12 py-5 rounded-full font-bold text-lg transition-all transform hover:scale-105 active:scale-95 shadow-xl hover:shadow-2xl"
          >
            <span>Xem thực đơn đầy đủ</span>
            <svg
              class="w-6 h-6 group-hover:translate-x-1 transition-transform"
              fill="none"
              stroke="currentColor"
              viewBox="0 0 24 24"
            >
              <path
                stroke-linecap="round"
                stroke-linejoin="round"
                stroke-width="2.5"
                d="M17 8l4 4m0 0l-4 4m4-4H3"
              />
            </svg>
          </router-link>
        </div>
      </div>
    </section>

    <!-- Featured Combos -->
    <section id="combo" class="py-20 bg-gradient-to-b from-amber-50 via-white to-orange-50">
      <div class="max-w-7xl mx-auto px-4">
        <div class="text-center mb-16 scroll-reveal">
          <h2 class="text-5xl md:text-6xl font-black text-gray-900 mb-6">Combo Đặc Biệt</h2>
          <p class="text-xl md:text-2xl text-gray-600 font-medium">
            Tiết kiệm hơn khi đặt combo - Trải nghiệm trọn vẹn
          </p>
          <div
            class="mt-6 h-1.5 w-40 mx-auto bg-gradient-to-r from-amber-400 via-orange-500 to-amber-400 rounded-full shadow-lg"
          ></div>
        </div>

        <div v-if="loading" class="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-4 gap-8">
          <div v-for="i in 4" :key="i" class="shimmer rounded-2xl h-96"></div>
        </div>

        <div
          v-else-if="featuredCombos.length > 0"
          class="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-4 gap-8 mb-12"
        >
          <div
            v-for="(combo, index) in featuredCombos"
            :key="combo.id"
            class="animate-fade-in-up"
            :style="`animation-delay: ${index * 0.1}s`"
          >
            <ComboCard
              :id="combo.id"
              :name="combo.name"
              :description="combo.description"
              :price="combo.price"
              :imageUrl="combo.imageUrl"
              :status="combo.status"
              :items="combo.items"
              :suggestedSum="combo.suggestedSum"
            />
          </div>
        </div>

        <div v-else class="text-center py-16 scroll-reveal">
          <div class="text-6xl mb-4">🍱</div>
          <p class="text-gray-500 text-xl font-medium">Chưa có combo nào</p>
          <p class="text-gray-400 text-sm mt-2">Hãy quay lại sau nhé!</p>
        </div>

        <div class="text-center scroll-reveal">
          <router-link
            to="/combos"
            class="inline-flex items-center gap-3 bg-gradient-to-r from-amber-600 to-orange-600 hover:from-amber-700 hover:to-orange-700 text-white px-12 py-5 rounded-full font-bold text-lg transition-all transform hover:scale-105 active:scale-95 shadow-xl hover:shadow-2xl"
          >
            <span>Xem tất cả combo</span>
            <svg
              class="w-6 h-6 group-hover:translate-x-1 transition-transform"
              fill="none"
              stroke="currentColor"
              viewBox="0 0 24 24"
            >
              <path
                stroke-linecap="round"
                stroke-linejoin="round"
                stroke-width="2.5"
                d="M17 8l4 4m0 0l-4 4m4-4H3"
              />
            </svg>
          </router-link>
        </div>
      </div>
    </section>
    <!-- Section: Về chúng tôi -->
    <section
      class="mx-auto max-w-7xl px-4 py-20 bg-gradient-to-b from-blue-50 via-white to-gray-50"
    >
      <!-- Title -->
      <div class="text-center mb-16 scroll-reveal">
        <h2 class="text-5xl md:text-6xl font-black text-gray-900 mb-6">Về Chúng Tôi</h2>
        <p class="text-xl md:text-2xl text-gray-600 font-medium">
          Ẩm thực thực dưỡng – cân bằng & tinh tế
        </p>
        <div
          class="mt-6 h-1.5 w-40 mx-auto bg-gradient-to-r from-blue-400 via-cyan-500 to-blue-400 rounded-full shadow-lg"
        ></div>
      </div>

      <!-- Grid -->
      <div class="grid grid-cols-1 md:grid-cols-2 gap-8">
        <!-- Card 1 -->
        <article
          class="scroll-reveal-left group bg-white rounded-3xl shadow-md ring-1 ring-gray-100 overflow-hidden hover:shadow-2xl transition-all hover-lift"
        >
          <div class="aspect-[16/9] overflow-hidden relative">
            <img
              class="h-full w-full object-cover transition-transform duration-700 group-hover:scale-110"
              src="https://i.ytimg.com/vi/11xNlOfZ9S0/maxresdefault.jpg"
              alt="Xôi ngũ sắc - Ẩm thực thực dưỡng"
              loading="lazy"
            />
            <div
              class="absolute inset-0 bg-gradient-to-t from-black/50 to-transparent opacity-0 group-hover:opacity-100 transition-opacity duration-300"
            ></div>
          </div>
          <div class="p-8">
            <h3
              class="text-2xl font-bold text-gray-900 mb-3 group-hover:text-[#9f0909] transition-colors"
            >
              Xôi Ngũ Sắc
            </h3>
            <p class="text-gray-600 leading-relaxed">
              Tại Hoa Ban, mỗi món đều được chế biến từ nguyên liệu tươi sạch, giữ trọn hương vị tự
              nhiên. Các món đậm chất núi rừng như xôi, cơm lam, nậm pịa… mang đến cân bằng dinh
              dưỡng cho cơ thể.
            </p>
          </div>
        </article>

        <!-- Card 2 -->
        <article
          class="scroll-reveal-right group bg-white rounded-3xl shadow-md ring-1 ring-gray-100 overflow-hidden hover:shadow-2xl transition-all hover-lift"
        >
          <div class="aspect-[16/9] overflow-hidden relative">
            <img
              class="h-full w-full object-cover transition-transform duration-700 group-hover:scale-110"
              src="https://images.unsplash.com/photo-1517248135467-4c7edcad34c4?q=100&w=2000&auto=format&fit=crop&ixlib=rb-4.0.3"
              alt="Toàn cảnh nhà hàng Hoa Ban về đêm"
              loading="lazy"
            />
            <div
              class="absolute inset-0 bg-gradient-to-t from-black/50 to-transparent opacity-0 group-hover:opacity-100 transition-opacity duration-300"
            ></div>
          </div>
          <div class="p-8">
            <h3
              class="text-2xl font-bold text-gray-900 mb-3 group-hover:text-[#9f0909] transition-colors"
            >
              Toàn cảnh nhà hàng
            </h3>
            <p class="text-gray-600 leading-relaxed">
              Không gian xanh – gần gũi và ấm áp. Ánh sáng ấm, khoảng mở và cây xanh giúp bạn tận
              hưởng bữa tối thư giãn.
            </p>
          </div>
        </article>

        <!-- Card 3 -->
        <article
          class="scroll-reveal-left group bg-white rounded-3xl shadow-md ring-1 ring-gray-100 overflow-hidden hover:shadow-2xl transition-all hover-lift"
        >
          <div class="aspect-[16/9] overflow-hidden relative">
            <img
              class="h-full w-full object-cover transition-transform duration-700 group-hover:scale-110"
              src="https://images.unsplash.com/photo-1524231757912-21f4fe3a7200?q=80&w=1200&auto=format&fit=crop"
              alt="Phòng ăn riêng tư"
              loading="lazy"
            />
            <div
              class="absolute inset-0 bg-gradient-to-t from-black/50 to-transparent opacity-0 group-hover:opacity-100 transition-opacity duration-300"
            ></div>
          </div>
          <div class="p-8">
            <h3
              class="text-2xl font-bold text-gray-900 mb-3 group-hover:text-[#9f0909] transition-colors"
            >
              Phòng ăn riêng tư
            </h3>
            <p class="text-gray-600 leading-relaxed">
              Không gian kín đáo cho dịp đặc biệt: họp mặt gia đình, sinh nhật, tiếp khách. Hệ thống
              âm thanh – ánh sáng dịu.
            </p>
          </div>
        </article>

        <!-- Card 4 -->
        <article
          class="scroll-reveal-right group bg-white rounded-3xl shadow-md ring-1 ring-gray-100 overflow-hidden hover:shadow-2xl transition-all hover-lift"
        >
          <div class="aspect-[16/9] overflow-hidden relative">
            <img
              class="h-full w-full object-cover transition-transform duration-700 group-hover:scale-110"
              src="https://images.unsplash.com/photo-1559339352-11d035aa65de?q=80&w=1200&auto=format&fit=crop"
              alt="Nơi gắn kết"
              loading="lazy"
            />
            <div
              class="absolute inset-0 bg-gradient-to-t from-black/50 to-transparent opacity-0 group-hover:opacity-100 transition-opacity duration-300"
            ></div>
          </div>
          <div class="p-8">
            <h3
              class="text-2xl font-bold text-gray-900 mb-3 group-hover:text-[#9f0909] transition-colors"
            >
              Nơi gắn kết
            </h3>
            <p class="text-gray-600 leading-relaxed">
              Sự kiện nhỏ – ấm cúng. Đội ngũ phục vụ tinh tế, thực đơn thực dưỡng thiết kế riêng
              theo nhu cầu của bạn.
            </p>
          </div>
        </article>
      </div>
    </section>

    <section id="dat-ban" class="mx-auto max-w-6xl px-4 py-16">
      <!-- Section Title với animation -->
      <div class="text-center mb-12 scroll-reveal">
        <h2 class="text-4xl md:text-5xl font-bold text-gray-900 mb-4">Đặt Bàn Ngay</h2>
        <p class="text-xl text-gray-600">Trải nghiệm ẩm thực tuyệt vời tại Hoa Ban Restaurant</p>
        <div class="mt-6 flex justify-center gap-4 flex-wrap">
          <div class="flex items-center gap-2 bg-green-50 text-green-700 px-4 py-2 rounded-full">
            <svg class="w-5 h-5" fill="currentColor" viewBox="0 0 20 20">
              <path
                fill-rule="evenodd"
                d="M10 18a8 8 0 100-16 8 8 0 000 16zm3.707-9.293a1 1 0 00-1.414-1.414L9 10.586 7.707 9.293a1 1 0 00-1.414 1.414l2 2a1 1 0 001.414 0l4-4z"
                clip-rule="evenodd"
              ></path>
            </svg>
            <span class="font-medium">Đặt bàn miễn phí</span>
          </div>
          <div class="flex items-center gap-2 bg-blue-50 text-blue-700 px-4 py-2 rounded-full">
            <svg class="w-5 h-5" fill="currentColor" viewBox="0 0 20 20">
              <path
                d="M2 3a1 1 0 011-1h2.153a1 1 0 01.986.836l.74 4.435a1 1 0 01-.54 1.06l-1.548.773a11.037 11.037 0 006.105 6.105l.774-1.548a1 1 0 011.059-.54l4.435.74a1 1 0 01.836.986V17a1 1 0 01-1 1h-2C7.82 18 2 12.18 2 5V3z"
              ></path>
            </svg>
            <span class="font-medium">Xác nhận nhanh</span>
          </div>
          <div class="flex items-center gap-2 bg-amber-50 text-amber-700 px-4 py-2 rounded-full">
            <svg class="w-5 h-5" fill="currentColor" viewBox="0 0 20 20">
              <path
                d="M9 6a3 3 0 11-6 0 3 3 0 016 0zM17 6a3 3 0 11-6 0 3 3 0 016 0zM12.93 17c.046-.327.07-.66.07-1a6.97 6.97 0 00-1.5-4.33A5 5 0 0119 16v1h-6.07zM6 11a5 5 0 015 5v1H1v-1a5 5 0 015-5z"
              ></path>
            </svg>
            <span class="font-medium">Phục vụ tận tâm</span>
          </div>
        </div>
      </div>

      <div class="grid lg:grid-cols-2 gap-8">
        <!-- GALLERY LEFT với hiệu ứng -->
        <div
          class="scroll-reveal-left bg-white rounded-2xl shadow-xl ring-1 ring-gray-200 p-6 order-2 lg:order-1 hover-lift"
        >
          <!-- Gallery Header -->
          <div class="mb-4">
            <h3 class="text-2xl font-bold text-gray-900 mb-2">
              <span class="text-[#9f0909]">📸</span> Không Gian Nhà Hàng
            </h3>
            <p class="text-gray-600">Khám phá không gian sang trọng và ấm cúng của Hoa Ban</p>
          </div>

          <!-- Main Image với overlay -->
          <div class="relative overflow-hidden rounded-xl aspect-[4/3] group shadow-lg">
            <img
              id="gal-main"
              class="w-full h-full object-cover transition-all duration-700 group-hover:scale-110"
              src="https://images.unsplash.com/photo-1559339352-11d035aa65de?q=80&w=1400&auto=format&fit=crop"
              alt="Phòng riêng Hoa Ban"
              loading="lazy"
            />
            <!-- Overlay gradient khi hover -->
            <div
              class="absolute inset-0 bg-gradient-to-t from-black/70 via-black/20 to-transparent opacity-0 group-hover:opacity-100 transition-opacity duration-500"
            >
              <div class="absolute bottom-0 left-0 right-0 p-6 text-white">
                <p class="text-lg font-semibold">Không gian sang trọng</p>
                <p class="text-sm text-white/80">Phù hợp cho mọi dịp đặc biệt</p>
              </div>
            </div>

            <!-- Navigation buttons -->
            <button
              id="gal-prev"
              class="absolute left-3 top-1/2 -translate-y-1/2 grid place-items-center w-12 h-12 rounded-full bg-white/95 shadow-xl hover:bg-white hover:scale-110 transition-all duration-300 opacity-0 group-hover:opacity-100 backdrop-blur-sm"
              aria-label="Ảnh trước"
            >
              <svg
                class="w-6 h-6 text-gray-800"
                fill="none"
                stroke="currentColor"
                viewBox="0 0 24 24"
              >
                <path
                  stroke-linecap="round"
                  stroke-linejoin="round"
                  stroke-width="3"
                  d="M15 19l-7-7 7-7"
                />
              </svg>
            </button>
            <button
              id="gal-next"
              class="absolute right-3 top-1/2 -translate-y-1/2 grid place-items-center w-12 h-12 rounded-full bg-white/95 shadow-xl hover:bg-white hover:scale-110 transition-all duration-300 opacity-0 group-hover:opacity-100 backdrop-blur-sm"
              aria-label="Ảnh sau"
            >
              <svg
                class="w-6 h-6 text-gray-800"
                fill="none"
                stroke="currentColor"
                viewBox="0 0 24 24"
              >
                <path
                  stroke-linecap="round"
                  stroke-linejoin="round"
                  stroke-width="3"
                  d="M9 5l7 7-7 7"
                />
              </svg>
            </button>

            <!-- Image counter với design đẹp hơn -->
            <div
              class="absolute top-3 right-3 bg-black/70 text-white px-4 py-2 rounded-full text-sm font-bold backdrop-blur-sm opacity-0 group-hover:opacity-100 transition-opacity duration-300 flex items-center gap-2"
            >
              <svg class="w-4 h-4" fill="currentColor" viewBox="0 0 20 20">
                <path
                  fill-rule="evenodd"
                  d="M4 3a2 2 0 00-2 2v10a2 2 0 002 2h12a2 2 0 002-2V5a2 2 0 00-2-2H4zm12 12H4l4-8 3 6 2-4 3 6z"
                  clip-rule="evenodd"
                ></path>
              </svg>
              <span id="current-img">1</span> / <span id="total-img">4</span>
            </div>
          </div>

          <!-- Thumbnails với animation -->
          <div
            id="gal-thumbs"
            class="mt-4 grid grid-cols-4 gap-3 animate-fade-in-up"
            style="animation-delay: 0.3s"
          ></div>

          <!-- Gallery features -->
          <div class="mt-6 grid grid-cols-2 gap-4">
            <div class="flex items-center gap-2 text-gray-700">
              <svg class="w-5 h-5 text-[#9f0909]" fill="currentColor" viewBox="0 0 20 20">
                <path
                  d="M10.394 2.08a1 1 0 00-.788 0l-7 3a1 1 0 000 1.84L5.25 8.051a.999.999 0 01.356-.257l4-1.714a1 1 0 11.788 1.838L7.667 9.088l1.94.831a1 1 0 00.787 0l7-3a1 1 0 000-1.838l-7-3zM3.31 9.397L5 10.12v4.102a8.969 8.969 0 00-1.05-.174 1 1 0 01-.89-.89 11.115 11.115 0 01.25-3.762zM9.3 16.573A9.026 9.026 0 007 14.935v-3.957l1.818.78a3 3 0 002.364 0l5.508-2.361a11.026 11.026 0 01.25 3.762 1 1 0 01-.89.89 8.968 8.968 0 00-5.35 2.524 1 1 0 01-1.4 0zM6 18a1 1 0 001-1v-2.065a8.935 8.935 0 00-2-.712V17a1 1 0 001 1z"
                ></path>
              </svg>
              <span class="text-sm font-medium">Phòng VIP</span>
            </div>
            <div class="flex items-center gap-2 text-gray-700">
              <svg class="w-5 h-5 text-[#9f0909]" fill="currentColor" viewBox="0 0 20 20">
                <path
                  fill-rule="evenodd"
                  d="M5.05 4.05a7 7 0 119.9 9.9L10 18.9l-4.95-4.95a7 7 0 010-9.9zM10 11a2 2 0 100-4 2 2 0 000 4z"
                  clip-rule="evenodd"
                ></path>
              </svg>
              <span class="text-sm font-medium">View đẹp</span>
            </div>
            <div class="flex items-center gap-2 text-gray-700">
              <svg class="w-5 h-5 text-[#9f0909]" fill="currentColor" viewBox="0 0 20 20">
                <path
                  d="M10 3.5a1.5 1.5 0 013 0V4a1 1 0 001 1h3a1 1 0 011 1v3a1 1 0 01-1 1h-.5a1.5 1.5 0 000 3h.5a1 1 0 011 1v3a1 1 0 01-1 1h-3a1 1 0 01-1-1v-.5a1.5 1.5 0 00-3 0v.5a1 1 0 01-1 1H6a1 1 0 01-1-1v-3a1 1 0 00-1-1h-.5a1.5 1.5 0 010-3H4a1 1 0 001-1V6a1 1 0 011-1h3a1 1 0 001-1v-.5z"
                ></path>
              </svg>
              <span class="text-sm font-medium">Âm thanh hiện đại</span>
            </div>
            <div class="flex items-center gap-2 text-gray-700">
              <svg class="w-5 h-5 text-[#9f0909]" fill="currentColor" viewBox="0 0 20 20">
                <path
                  fill-rule="evenodd"
                  d="M18 10a8 8 0 11-16 0 8 8 0 0116 0zm-7-4a1 1 0 11-2 0 1 1 0 012 0zM9 9a1 1 0 000 2v3a1 1 0 001 1h1a1 1 0 100-2v-3a1 1 0 00-1-1H9z"
                  clip-rule="evenodd"
                ></path>
              </svg>
              <span class="text-sm font-medium">Tư vấn miễn phí</span>
            </div>
          </div>
        </div>

        <!-- FORM RIGHT với gradient đẹp hơn -->
        <div
          class="scroll-reveal-right relative bg-gradient-to-br from-[#8b1111] via-[#9f0909] to-[#7a0707] text-white rounded-2xl shadow-2xl p-8 order-1 lg:order-2 overflow-hidden"
        >
          <!-- Decorative circles -->
          <div
            class="absolute -top-10 -right-10 w-40 h-40 bg-white/10 rounded-full blur-3xl animate-float"
          ></div>
          <div
            class="absolute -bottom-10 -left-10 w-40 h-40 bg-white/10 rounded-full blur-3xl animate-float"
            style="animation-delay: -3s"
          ></div>

          <div class="relative z-10">
            <!-- Header với icon -->
            <div class="flex items-center gap-4 mb-8">
              <div
                class="w-16 h-16 rounded-2xl bg-white/20 backdrop-blur-sm flex items-center justify-center animate-scale-in"
              >
                <img
                  :src="resolveImage('/src/assets/img/logo.png')"
                  alt="Hoa Ban Restaurant Logo"
                  class="w-12 h-12 rounded-xl"
                  loading="lazy"
                />
              </div>
              <div>
                <h2 class="text-3xl font-['Inknut_Antiqua',serif] font-bold">Đặt bàn ngay</h2>
                <p class="text-white/90 text-sm mt-1 flex items-center gap-2">
                  <span class="inline-block w-2 h-2 bg-green-400 rounded-full animate-pulse"></span>
                  Còn trống nhiều bàn hôm nay
                </p>
              </div>
            </div>

            <form id="reserve-form" class="space-y-5 relative z-10">
              <!-- Login hint for guests với design đẹp hơn -->
              <div
                v-if="!auth.user"
                class="bg-white/15 backdrop-blur-sm rounded-xl p-4 mb-6 border border-white/20 animate-fade-in-down"
              >
                <div class="flex items-start gap-3">
                  <div
                    class="w-10 h-10 rounded-full bg-white/20 flex items-center justify-center flex-shrink-0"
                  >
                    💡
                  </div>
                  <div>
                    <p class="text-sm text-white font-medium mb-1">Mẹo hữu ích!</p>
                    <p class="text-sm text-white/80">
                      <router-link
                        to="/auth/login"
                        class="text-white font-semibold hover:underline inline-flex items-center gap-1"
                      >
                        Đăng nhập
                        <svg class="w-4 h-4" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                          <path
                            stroke-linecap="round"
                            stroke-linejoin="round"
                            stroke-width="2"
                            d="M13 7l5 5m0 0l-5 5m5-5H6"
                          ></path>
                        </svg>
                      </router-link>
                      để tự động điền thông tin và quản lý đặt bàn dễ dàng
                    </p>
                  </div>
                </div>
              </div>

              <!-- Thông tin khách hàng -->
              <div
                class="grid md:grid-cols-2 gap-4 animate-fade-in-up"
                style="animation-delay: 0.1s"
              >
                <div class="relative">
                  <label class="flex items-center gap-2 text-sm font-semibold text-white mb-2">
                    <svg class="w-4 h-4" fill="currentColor" viewBox="0 0 20 20">
                      <path
                        fill-rule="evenodd"
                        d="M10 9a3 3 0 100-6 3 3 0 000 6zm-7 9a7 7 0 1114 0H3z"
                        clip-rule="evenodd"
                      ></path>
                    </svg>
                    Tên khách hàng
                    <span class="text-red-300">*</span>
                    <span
                      v-if="auth.user && customerName"
                      class="text-xs bg-green-400/30 text-green-200 px-2 py-0.5 rounded-full ml-auto"
                    >
                      ✓ Tự động
                    </span>
                  </label>
                  <input
                    v-model="customerName"
                    class="w-full rounded-xl bg-white/95 backdrop-blur text-gray-800 px-4 py-3.5 outline-none ring-2 ring-white/20 focus:ring-white focus:bg-white transition-all placeholder-gray-400 font-medium shadow-lg"
                    :placeholder="auth.user ? 'Tên từ tài khoản của bạn' : 'Nhập tên của bạn'"
                    required
                  />
                </div>
                <div class="relative">
                  <label class="flex items-center gap-2 text-sm font-semibold text-white mb-2">
                    <svg class="w-4 h-4" fill="currentColor" viewBox="0 0 20 20">
                      <path
                        d="M2 3a1 1 0 011-1h2.153a1 1 0 01.986.836l.74 4.435a1 1 0 01-.54 1.06l-1.548.773a11.037 11.037 0 006.105 6.105l.774-1.548a1 1 0 011.059-.54l4.435.74a1 1 0 01.836.986V17a1 1 0 01-1 1h-2C7.82 18 2 12.18 2 5V3z"
                      ></path>
                    </svg>
                    Số điện thoại
                    <span class="text-red-300">*</span>
                  </label>
                  <input
                    v-model="customerPhone"
                    type="tel"
                    class="w-full rounded-xl bg-white/95 backdrop-blur text-gray-800 px-4 py-3.5 outline-none ring-2 ring-white/20 focus:ring-white focus:bg-white transition-all placeholder-gray-400 font-medium shadow-lg"
                    placeholder="0901 234 567"
                    required
                  />
                </div>
              </div>

              <!-- Thời gian -->
              <div
                class="grid md:grid-cols-2 gap-4 animate-fade-in-up"
                style="animation-delay: 0.2s"
              >
                <div class="relative">
                  <label class="flex items-center gap-2 text-sm font-semibold text-white mb-2">
                    <svg class="w-4 h-4" fill="currentColor" viewBox="0 0 20 20">
                      <path
                        fill-rule="evenodd"
                        d="M6 2a1 1 0 00-1 1v1H4a2 2 0 00-2 2v10a2 2 0 002 2h12a2 2 0 002-2V6a2 2 0 00-2-2h-1V3a1 1 0 10-2 0v1H7V3a1 1 0 00-1-1zm0 5a1 1 0 000 2h8a1 1 0 100-2H6z"
                        clip-rule="evenodd"
                      ></path>
                    </svg>
                    Ngày đặt
                    <span class="text-red-300">*</span>
                  </label>
                  <input
                    v-model="date"
                    id="reservation-date"
                    type="date"
                    :min="new Date().toISOString().split('T')[0]"
                    class="w-full rounded-xl bg-white/95 backdrop-blur text-gray-800 px-4 py-3.5 outline-none ring-2 ring-white/20 focus:ring-white focus:bg-white transition-all font-medium shadow-lg"
                    required
                  />
                </div>
                <div class="relative">
                  <label class="flex items-center gap-2 text-sm font-semibold text-white mb-2">
                    <svg class="w-4 h-4" fill="currentColor" viewBox="0 0 20 20">
                      <path
                        fill-rule="evenodd"
                        d="M10 18a8 8 0 100-16 8 8 0 000 16zm1-12a1 1 0 10-2 0v4a1 1 0 00.293.707l2.828 2.829a1 1 0 101.415-1.415L11 9.586V6z"
                        clip-rule="evenodd"
                      ></path>
                    </svg>
                    Giờ đặt
                    <span class="text-red-300">*</span>
                  </label>
                  <select
                    v-model="time"
                    class="w-full rounded-xl bg-white/95 backdrop-blur text-gray-800 px-4 py-3.5 outline-none ring-2 ring-white/20 focus:ring-white focus:bg-white transition-all font-medium shadow-lg appearance-none cursor-pointer"
                    required
                  >
                    <option value="" class="text-gray-500">Chọn giờ</option>
                    <option
                      v-for="slot in timeSlots"
                      :key="slot"
                      :value="slot"
                      class="text-gray-800"
                    >
                      🕐 {{ slot }}
                    </option>
                  </select>
                </div>
              </div>

              <!-- Chi tiết đặt bàn -->
              <div
                class="grid md:grid-cols-2 gap-4 animate-fade-in-up"
                style="animation-delay: 0.3s"
              >
                <div class="relative">
                  <label class="flex items-center gap-2 text-sm font-semibold text-white mb-2">
                    <svg class="w-4 h-4" fill="currentColor" viewBox="0 0 20 20">
                      <path
                        d="M9 6a3 3 0 11-6 0 3 3 0 016 0zM17 6a3 3 0 11-6 0 3 3 0 016 0zM12.93 17c.046-.327.07-.66.07-1a6.97 6.97 0 00-1.5-4.33A5 5 0 0119 16v1h-6.07zM6 11a5 5 0 015 5v1H1v-1a5 5 0 015-5z"
                      ></path>
                    </svg>
                    Số lượng khách
                    <span class="text-red-300">*</span>
                  </label>
                  <input
                    v-model.number="partySize"
                    type="number"
                    min="1"
                    max="20"
                    class="w-full rounded-xl bg-white/95 backdrop-blur text-gray-800 px-4 py-3.5 outline-none ring-2 ring-white/20 focus:ring-white focus:bg-white transition-all placeholder-gray-400 font-medium shadow-lg"
                    placeholder="Nhập số người"
                    required
                  />
                </div>
                <div class="relative">
                  <label class="flex items-center gap-2 text-sm font-semibold text-white mb-2">
                    <svg class="w-4 h-4" fill="currentColor" viewBox="0 0 20 20">
                      <path
                        fill-rule="evenodd"
                        d="M4 4a2 2 0 012-2h8a2 2 0 012 2v12a1 1 0 110 2h-3a1 1 0 01-1-1v-2a1 1 0 00-1-1H9a1 1 0 00-1 1v2a1 1 0 01-1 1H4a1 1 0 110-2V4zm3 1h2v2H7V5zm2 4H7v2h2V9zm2-4h2v2h-2V5zm2 4h-2v2h2V9z"
                        clip-rule="evenodd"
                      ></path>
                    </svg>
                    Khu vực
                    <span class="text-red-300">*</span>
                  </label>
                  <div class="relative">
                    <select
                      v-model="area"
                      id="area-select"
                      class="w-full rounded-xl bg-white/95 backdrop-blur text-gray-800 px-4 py-3.5 outline-none ring-2 ring-white/20 focus:ring-white focus:bg-white transition-all appearance-none font-medium shadow-lg cursor-pointer"
                      required
                    >
                      <option value="" class="text-gray-500">Chọn khu vực</option>
                      <option
                        v-for="areaData in areas.filter((a: AreaDto) => a.status === 'ACTIVE')"
                        :key="areaData.id"
                        :value="areaData.name"
                        class="text-gray-800"
                      >
                        {{ areaData.name }}
                      </option>
                    </select>
                    <div
                      class="absolute inset-y-0 right-0 flex items-center pr-4 pointer-events-none"
                    >
                      <svg
                        class="w-5 h-5 text-gray-400"
                        fill="none"
                        stroke="currentColor"
                        viewBox="0 0 24 24"
                      >
                        <path
                          stroke-linecap="round"
                          stroke-linejoin="round"
                          stroke-width="2"
                          d="M19 9l-7 7-7-7"
                        ></path>
                      </svg>
                    </div>
                  </div>
                </div>
              </div>

              <!-- Ghi chú -->
              <div class="animate-fade-in-up" style="animation-delay: 0.4s">
                <label class="flex items-center gap-2 text-sm font-semibold text-white mb-2">
                  <svg class="w-4 h-4" fill="currentColor" viewBox="0 0 20 20">
                    <path
                      fill-rule="evenodd"
                      d="M18 13V5a2 2 0 00-2-2H4a2 2 0 00-2 2v8a2 2 0 002 2h3l3 3 3-3h3a2 2 0 002-2zM5 7a1 1 0 011-1h8a1 1 0 110 2H6a1 1 0 01-1-1zm1 3a1 1 0 100 2h3a1 1 0 100-2H6z"
                      clip-rule="evenodd"
                    ></path>
                  </svg>
                  Ghi chú đặc biệt
                  <span class="text-white/60 text-xs font-normal">(Tùy chọn)</span>
                </label>
                <textarea
                  v-model="note"
                  rows="3"
                  class="w-full rounded-xl bg-white/95 backdrop-blur text-gray-800 px-4 py-3.5 outline-none ring-2 ring-white/20 focus:ring-white focus:bg-white transition-all placeholder-gray-400 resize-none font-medium shadow-lg"
                  placeholder="💡 Ví dụ: Setup sinh nhật, món chay, vị trí ngồi yên tĩnh..."
                ></textarea>
              </div>

              <!-- Action buttons -->
              <div class="space-y-4 pt-4 animate-fade-in-up" style="animation-delay: 0.5s">
                <!-- Nút liên hệ nhanh -->
                <div class="flex gap-3">
                  <button
                    id="btn-call"
                    type="button"
                    class="flex-1 flex items-center justify-center gap-2 rounded-xl bg-white/15 backdrop-blur-sm px-5 py-3.5 ring-2 ring-white/30 hover:bg-white/25 hover:ring-white/50 transition-all transform hover:scale-105 shadow-lg group"
                  >
                    <svg
                      class="w-5 h-5 group-hover:rotate-12 transition-transform"
                      fill="none"
                      stroke="currentColor"
                      viewBox="0 0 24 24"
                    >
                      <path
                        stroke-linecap="round"
                        stroke-linejoin="round"
                        stroke-width="2"
                        d="M3 5a2 2 0 012-2h3.28a1 1 0 01.948.684l1.498 4.493a1 1 0 01-.502 1.21l-2.257 1.13a11.042 11.042 0 005.516 5.516l1.13-2.257a1 1 0 011.21-.502l4.493 1.498a1 1 0 01.684.949V19a2 2 0 01-2 2h-1C9.716 21 3 14.284 3 6V5z"
                      ></path>
                    </svg>
                    <span class="font-semibold">Gọi ngay</span>
                  </button>
                  <button
                    id="btn-map"
                    type="button"
                    class="flex-1 flex items-center justify-center gap-2 rounded-xl bg-white/15 backdrop-blur-sm px-5 py-3.5 ring-2 ring-white/30 hover:bg-white/25 hover:ring-white/50 transition-all transform hover:scale-105 shadow-lg group"
                  >
                    <svg
                      class="w-5 h-5 group-hover:scale-110 transition-transform"
                      fill="none"
                      stroke="currentColor"
                      viewBox="0 0 24 24"
                    >
                      <path
                        stroke-linecap="round"
                        stroke-linejoin="round"
                        stroke-width="2"
                        d="M17.657 16.657L13.414 20.9a1.998 1.998 0 01-2.827 0l-4.244-4.243a8 8 0 1111.314 0z"
                      ></path>
                      <path
                        stroke-linecap="round"
                        stroke-linejoin="round"
                        stroke-width="2"
                        d="M15 11a3 3 0 11-6 0 3 3 0 016 0z"
                      ></path>
                    </svg>
                    <span class="font-semibold">Xem bản đồ</span>
                  </button>
                </div>

                <!-- Nút đặt bàn chính với gradient animation -->
                <button
                  type="submit"
                  :disabled="isLoading"
                  class="relative w-full bg-gradient-to-r from-white via-gray-50 to-white text-[#9f0909] hover:from-gray-50 hover:via-white hover:to-gray-50 py-4 px-6 rounded-xl font-bold text-lg transition-all transform hover:scale-[1.02] active:scale-[0.98] shadow-2xl disabled:opacity-50 disabled:cursor-not-allowed flex items-center justify-center gap-3 overflow-hidden group"
                >
                  <!-- Animated background -->
                  <div
                    class="absolute inset-0 bg-gradient-to-r from-transparent via-white/50 to-transparent -translate-x-full group-hover:translate-x-full transition-transform duration-1000"
                  ></div>

                  <template v-if="isLoading">
                    <svg class="animate-spin w-6 h-6 relative z-10" fill="none" viewBox="0 0 24 24">
                      <circle
                        class="opacity-25"
                        cx="12"
                        cy="12"
                        r="10"
                        stroke="currentColor"
                        stroke-width="4"
                      ></circle>
                      <path
                        class="opacity-75"
                        fill="currentColor"
                        d="M4 12a8 8 0 018-8V0C5.373 0 0 5.373 0 12h4zm2 5.291A7.962 7.962 0 014 12H0c0 3.042 1.135 5.824 3 7.938l3-2.647z"
                      ></path>
                    </svg>
                    <span class="relative z-10 font-bold">Đang xử lý...</span>
                  </template>
                  <template v-else>
                    <span class="text-2xl group-hover:scale-110 transition-transform relative z-10"
                      >🍽️</span
                    >
                    <span class="relative z-10 font-bold">Đặt bàn ngay</span>
                    <svg
                      class="w-5 h-5 group-hover:translate-x-1 transition-transform relative z-10"
                      fill="none"
                      stroke="currentColor"
                      viewBox="0 0 24 24"
                    >
                      <path
                        stroke-linecap="round"
                        stroke-linejoin="round"
                        stroke-width="2"
                        d="M13 7l5 5m0 0l-5 5m5-5H6"
                      ></path>
                    </svg>
                  </template>
                </button>

                <!-- Trust badges -->
                <div class="flex items-center justify-center gap-6 pt-2 text-white/70 text-xs">
                  <div class="flex items-center gap-1.5">
                    <svg class="w-4 h-4 text-green-300" fill="currentColor" viewBox="0 0 20 20">
                      <path
                        fill-rule="evenodd"
                        d="M2.166 4.999A11.954 11.954 0 0010 1.944 11.954 11.954 0 0017.834 5c.11.65.166 1.32.166 2.001 0 5.225-3.34 9.67-8 11.317C5.34 16.67 2 12.225 2 7c0-.682.057-1.35.166-2.001zm11.541 3.708a1 1 0 00-1.414-1.414L9 10.586 7.707 9.293a1 1 0 00-1.414 1.414l2 2a1 1 0 001.414 0l4-4z"
                        clip-rule="evenodd"
                      ></path>
                    </svg>
                    <span>Bảo mật</span>
                  </div>
                  <div class="flex items-center gap-1.5">
                    <svg class="w-4 h-4 text-blue-300" fill="currentColor" viewBox="0 0 20 20">
                      <path
                        fill-rule="evenodd"
                        d="M6.267 3.455a3.066 3.066 0 001.745-.723 3.066 3.066 0 013.976 0 3.066 3.066 0 001.745.723 3.066 3.066 0 012.812 2.812c.051.643.304 1.254.723 1.745a3.066 3.066 0 010 3.976 3.066 3.066 0 00-.723 1.745 3.066 3.066 0 01-2.812 2.812 3.066 3.066 0 00-1.745.723 3.066 3.066 0 01-3.976 0 3.066 3.066 0 00-1.745-.723 3.066 3.066 0 01-2.812-2.812 3.066 3.066 0 00-.723-1.745 3.066 3.066 0 010-3.976 3.066 3.066 0 00.723-1.745 3.066 3.066 0 012.812-2.812zm7.44 5.252a1 1 0 00-1.414-1.414L9 10.586 7.707 9.293a1 1 0 00-1.414 1.414l2 2a1 1 0 001.414 0l4-4z"
                        clip-rule="evenodd"
                      ></path>
                    </svg>
                    <span>Miễn phí</span>
                  </div>
                  <div class="flex items-center gap-1.5">
                    <svg class="w-4 h-4 text-amber-300" fill="currentColor" viewBox="0 0 20 20">
                      <path
                        d="M9.049 2.927c.3-.921 1.603-.921 1.902 0l1.07 3.292a1 1 0 00.95.69h3.462c.969 0 1.371 1.24.588 1.81l-2.8 2.034a1 1 0 00-.364 1.118l1.07 3.292c.3.921-.755 1.688-1.54 1.118l-2.8-2.034a1 1 0 00-1.175 0l-2.8 2.034c-.784.57-1.838-.197-1.539-1.118l1.07-3.292a1 1 0 00-.364-1.118L2.98 8.72c-.783-.57-.38-1.81.588-1.81h3.461a1 1 0 00.951-.69l1.07-3.292z"
                      ></path>
                    </svg>
                    <span>4.9★ Đánh giá</span>
                  </div>
                </div>
              </div>
            </form>
          </div>
        </div>
      </div>
    </section>
    <!-- Customer Reviews Section -->
    <CustomerReviews />

    <!-- MODAL: CHỌN KHU VỰC -->
    <div id="modal-area" class="fixed inset-0 z-50 hidden">
      <div class="absolute inset-0 bg-black/40"></div>
      <div class="relative mx-auto mt-16 max-w-5xl bg-white rounded-2xl shadow-lg overflow-hidden">
        <div class="flex items-center justify-between px-6 py-4 border-b">
          <h3 class="text-lg font-semibold">Chọn khu vực</h3>
          <button class="px-2 py-1 rounded bg-gray-100 hover:bg-gray-200" data-close>✕</button>
        </div>

        <div class="grid lg:grid-cols-3 gap-6 p-6">
          <!-- LIST -->
          <div class="lg:col-span-1 space-y-3" id="area-list"></div>

          <!-- PREVIEW -->
          <div class="lg:col-span-2">
            <div class="rounded-xl overflow-hidden ring-1 ring-gray-100">
              <div class="relative aspect-[16/10]">
                <img id="area-prev-img" class="w-full h-full object-cover" src="" alt="Preview" />
                <button
                  id="area-prev"
                  class="absolute left-3 top-1/2 -translate-y-1/2 grid place-items-center w-9 h-9 rounded-full bg-white/90 shadow"
                >
                  ‹
                </button>
                <button
                  id="area-next"
                  class="absolute right-3 top-1/2 -translate-y-1/2 grid place-items-center w-9 h-9 rounded-full bg-white/90 shadow"
                >
                  ›
                </button>
              </div>
              <div class="p-4">
                <div class="flex items-center justify-between">
                  <div>
                    <h4 id="area-title" class="text-lg font-semibold"></h4>
                    <p id="area-meta" class="text-sm text-gray-500"></p>
                  </div>
                  <button id="area-choose" class="rounded-lg bg-[#9F0909] text-white px-4 py-2">
                    Chọn khu này
                  </button>
                </div>
              </div>
            </div>
            <div class="mt-4">
              <button id="area-open-map" class="rounded-lg border px-3 py-2 hover:bg-gray-50">
                Mở bản đồ khu này
              </button>
            </div>
          </div>
        </div>
      </div>
    </div>

    <!-- MODAL: BẢN ĐỒ -->
    <div id="modal-map" class="fixed inset-0 z-50 hidden">
      <div class="absolute inset-0 bg-black/40"></div>
      <div class="relative mx-auto mt-16 max-w-4xl bg-white rounded-2xl overflow-hidden shadow-lg">
        <div class="flex items-center justify-between px-6 py-4 border-b">
          <h3 class="text-lg font-semibold">Bản đồ Hoa Ban Restaurant</h3>
          <button class="px-2 py-1 rounded bg-gray-100 hover:bg-gray-200" data-close>✕</button>
        </div>
        <div class="aspect-[16/9]">
          <!-- Thay src bằng Google Maps Embed của bạn -->
          <iframe
            class="w-full h-full"
            loading="lazy"
            src="https://maps.google.com/maps?q=Da%20Nang&t=&z=14&ie=UTF8&iwloc=&output=embed"
          ></iframe>
        </div>
      </div>
    </div>

    <DishModal v-if="ui.showDishModal" />
  </div>
</template>

<style scoped>
.animate-pulse {
  animation: pulse 2s cubic-bezier(0.4, 0, 0.6, 1) infinite;
}

@keyframes pulse {
  0%,
  100% {
    opacity: 1;
  }
  50% {
    opacity: 0.5;
  }
}

/* Custom date input styling for dark theme */
input[type="date"]::-webkit-calendar-picker-indicator {
  filter: invert(1);
  cursor: pointer;
}

/* Responsive sticky button for mobile */
@media (max-width: 768px) {
  #reserve-form {
    padding-bottom: 100px;
  }

  #reserve-form button[type="submit"] {
    position: fixed;
    bottom: 20px;
    left: 20px;
    right: 20px;
    z-index: 50;
    margin: 0;
    border-radius: 12px;
    box-shadow: 0 10px 25px rgba(0, 0, 0, 0.3);
  }
}

/* Enhanced gallery thumbnails */
#gal-thumbs img {
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
}

#gal-thumbs img:hover {
  transform: translateY(-2px);
}

/* Gallery main image hover effect */
#gal-main {
  transition: transform 0.7s ease-out;
}

/* Enhanced button hover effects */
.group:hover #gal-prev,
.group:hover #gal-next {
  transform: translateY(-50%) scale(1.1);
}

/* Enhanced area card hover */
.area-card:hover {
  transform: translateY(-4px);
  box-shadow: 0 20px 25px -5px rgba(0, 0, 0, 0.1), 0 10px 10px -5px rgba(0, 0, 0, 0.04);
}

/* Loading spinner */
@keyframes spin {
  to {
    transform: rotate(360deg);
  }
}

.animate-spin {
  animation: spin 1s linear infinite;
}

/* Custom scrollbar for mobile */
@media (max-width: 768px) {
  .space-y-5 > * {
    scroll-margin-top: 20px;
  }
}

/* Form focus enhancements */
input:focus,
select:focus,
textarea:focus {
  box-shadow: 0 0 0 3px rgba(255, 255, 255, 0.1);
}

/* Gradient button hover effect */
button[type="submit"]:hover {
  background: linear-gradient(135deg, #ffffff 0%, #f8f9fa 100%);
}

/* Text shadow utilities for hero section */
.text-shadow {
  text-shadow: 2px 2px 4px rgba(0, 0, 0, 0.8), 0 0 6px rgba(0, 0, 0, 0.6);
}

.text-shadow-lg {
  text-shadow: 3px 3px 6px rgba(0, 0, 0, 0.9), 0 0 12px rgba(0, 0, 0, 0.7),
    0 0 20px rgba(0, 0, 0, 0.4);
}

/* Backdrop blur fallback for older browsers */
@supports not (backdrop-filter: blur(8px)) {
  .backdrop-blur-sm {
    background-color: rgba(0, 0, 0, 0.7) !important;
  }
}

/* Hero content animation */
@keyframes fadeInUp {
  from {
    opacity: 0;
    transform: translateY(30px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}

/* Gallery animations */
@keyframes fadeInLeft {
  from {
    opacity: 0;
    transform: translateX(-50px);
  }
  to {
    opacity: 1;
    transform: translateX(0);
  }
}

@keyframes fadeInUp {
  from {
    opacity: 0;
    transform: translateY(20px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}

/* Apply animations */
.animate-fade-in-left {
  animation: fadeInLeft 0.8s ease-out;
}

.animate-fade-in-up {
  animation: fadeInUp 0.6s ease-out;
  animation-fill-mode: both;
}

.hero-content {
  animation: fadeInUp 1s ease-out;
}

/* Floating decoration animation */
@keyframes float {
  0%,
  100% {
    transform: translateY(0px);
  }
  50% {
    transform: translateY(-20px);
  }
}

.floating {
  animation: float 6s ease-in-out infinite;
}

/* Gradient text enhancement */
.gradient-text {
  background: linear-gradient(135deg, #fcd34d, #f59e0b, #d97706);
  background-size: 200% 200%;
  animation: gradientShift 3s ease infinite;
}

@keyframes gradientShift {
  0% {
    background-position: 0% 50%;
  }
  50% {
    background-position: 100% 50%;
  }
  100% {
    background-position: 0% 50%;
  }
}

/* Enhanced button effect */
.hero-button {
  position: relative;
  overflow: hidden;
}

.hero-button::before {
  content: "";
  position: absolute;
  top: 0;
  left: -100%;
  width: 100%;
  height: 100%;
  background: linear-gradient(90deg, transparent, rgba(255, 255, 255, 0.2), transparent);
  transition: left 0.5s;
}

.hero-button:hover::before {
  left: 100%;
}
</style>
