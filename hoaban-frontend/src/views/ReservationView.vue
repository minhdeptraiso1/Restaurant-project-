<script setup lang="ts">
import { ref, onMounted, nextTick, watch } from "vue";
import { createReservation } from "@/api/reservations.service";
import { listAreas, type AreaDto } from "@/api/areas.admin";
import { toast } from "vue3-toastify";
import GoogleMap from "@/components/GoogleMap.vue";
import { useAuthStore } from "@/stores/auth";
import LoadingOverlay from "@/components/LoadingOverlay.vue";

const auth = useAuthStore();

// Form data
const customerName = ref("");
const customerPhone = ref("");
const date = ref("");
const time = ref("");
const partySize = ref(2);
const area = ref("");
const note = ref("");
const selectedTable = ref("");
const isLoading = ref(false);
const minDate = ref("");

// UI state
const currentImageIndex = ref(0);
const showAreaModal = ref(false);
const showTimeSlots = ref(false);
const showMap = ref(false);

// Image gallery
const galleryImages = [
  "https://images.unsplash.com/photo-1517248135467-4c7edcad34c4?q=100&w=2000&auto=format&fit=crop&ixlib=rb-4.0.3", // Restaurant interior
  "https://images.unsplash.com/photo-1555396273-367ea4eb4db5?q=100&w=2000&auto=format&fit=crop&ixlib=rb-4.0.3", // Restaurant dining area
  "https://images.unsplash.com/photo-1549497538-303791108f95?q=100&w=2000&auto=format&fit=crop&ixlib=rb-4.0.3", // Private dining room
  "https://images.unsplash.com/photo-1571997478779-2adcbbe9ab2f?q=100&w=2000&auto=format&fit=crop&ixlib=rb-4.0.3", // Restaurant atmosphere
  "https://images.unsplash.com/photo-1414235077428-338989a2e8c0?q=100&w=2000&auto=format&fit=crop&ixlib=rb-4.0.3", // Fine dining setup
];

// Time slots - Generate from 10:00 to 22:00 with 30-minute intervals
const generateTimeSlots = () => {
  const slots = [];
  const popularTimes = ["11:30", "12:00", "12:30", "18:00", "18:30", "19:00", "19:30"];

  for (let hour = 10; hour <= 22; hour++) {
    for (let minute of [0, 30]) {
      if (hour === 22 && minute === 30) break; // Stop at 22:00
      const timeStr = `${String(hour).padStart(2, "0")}:${String(minute).padStart(2, "0")}`;
      slots.push({
        time: timeStr,
        available: true,
        popular: popularTimes.includes(timeStr),
      });
    }
  }
  return slots;
};

const timeSlots = generateTimeSlots();

// Areas - will be loaded from API
const areas = ref<Array<AreaDto & { images: string[] }>>([]);

// Default images cho các khu vực
const areaDefaultImages: Record<string, string> = {
  outdoor:
    "https://images.unsplash.com/photo-1517248135467-4c7edcad34c4?q=100&w=2000&auto=format&fit=crop&ixlib=rb-4.0.3",
  indoor:
    "https://images.unsplash.com/photo-1555396273-367ea4eb4db5?q=100&w=2000&auto=format&fit=crop&ixlib=rb-4.0.3",
  private:
    "https://images.unsplash.com/photo-1559339352-11d035aa65de?q=100&w=2000&auto=format&fit=crop&ixlib=rb-4.0.3",
  vip: "https://images.unsplash.com/photo-1559339352-11d035aa65de?q=100&w=2000&auto=format&fit=crop&ixlib=rb-4.0.3",
  upstairs:
    "https://images.unsplash.com/photo-1571997478779-2adcbbe9ab2f?q=100&w=2000&auto=format&fit=crop&ixlib=rb-4.0.3",
  default:
    "https://images.unsplash.com/photo-1414235077428-338989a2e8c0?q=100&w=2000&auto=format&fit=crop&ixlib=rb-4.0.3",
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

// Load areas from API
const loadAreas = async () => {
  try {
    const response = await listAreas();
    const areasData = response?.data || [];
    console.log("Areas loaded:", areasData);
    areas.value = areasData.map((area: AreaDto) => {
      const imageUrl = getAreaImage(area.name);
      console.log(`Mapping area "${area.name}" to image:`, imageUrl);
      return {
        ...area,
        images: [imageUrl],
      };
    });
  } catch (error) {
    console.error("Error loading areas:", error);
    toast.error("Không thể tải danh sách khu vực");
  }
};

const resolveImage = (imagePath: string) => {
  try {
    return new URL(imagePath, import.meta.url).href;
  } catch (e) {
    return new URL("/src/assets/img/logo.png", import.meta.url).href;
  }
};

// Gallery navigation
const nextImage = () => {
  currentImageIndex.value = (currentImageIndex.value + 1) % galleryImages.length;
};

const prevImage = () => {
  currentImageIndex.value =
    currentImageIndex.value === 0 ? galleryImages.length - 1 : currentImageIndex.value - 1;
};

const selectImage = (index: number) => {
  currentImageIndex.value = index;
};

// Time slot selection
const selectTimeSlot = (timeSlot: string) => {
  if (timeSlots.find((slot) => slot.time === timeSlot)?.available) {
    time.value = timeSlot;
    showTimeSlots.value = false;
  }
};

// Area selection
const selectArea = (areaData: any) => {
  area.value = areaData.name;
  showAreaModal.value = false;
};

// Form utilities
function toIsoLocal(d: string, t: string) {
  if (!d || !t) return "";
  const dt = new Date(`${d}T${t}:00`);
  return dt.toISOString();
}

// Form submission
async function submit() {
  try {
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

    if (partySize.value < 1) {
      toast.error("Số lượng khách không hợp lệ");
      return;
    }

    isLoading.value = true;

    const startTime = toIsoLocal(date.value, time.value);

    // Tính endTime = startTime + 2 tiếng
    const startDate = new Date(startTime);
    const endDate = new Date(startDate.getTime() + 2 * 60 * 60 * 1000); // +2 hours
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
    selectedTable.value = "";

    // Auto-fill user info again after reset
    autoFillUserInfo();
  } catch (error: any) {
    toast.error(error?.response?.data?.message || "Có lỗi xảy ra khi đặt bàn");
  } finally {
    isLoading.value = false;
  }
}

// Auto-fill user information if logged in
const autoFillUserInfo = () => {
  if (auth.user && auth.token) {
    if (auth.user.fullName && !customerName.value) {
      customerName.value = auth.user.fullName;
    }
    // Since there's no phone field, we'll leave customerPhone empty for now
    // User can manually enter their phone number
  }
};

// Watch for user changes
watch(() => auth.user, autoFillUserInfo, { immediate: true });
watch(() => auth.token, autoFillUserInfo);

// Auto-set minimum date to today
onMounted(async () => {
  const now = new Date();
  minDate.value = now.toISOString().split("T")[0] || "";

  // Load areas from API
  await loadAreas();

  // Auto-fill user info on mount
  autoFillUserInfo();

  await nextTick();
  // Auto-start gallery slideshow
  setInterval(() => {
    nextImage();
  }, 5000);
});
</script>

<template>
  <LoadingOverlay :show="isLoading" message="Đang xử lý đặt bàn..." />

  <div class="min-h-screen bg-gradient-to-br from-gray-50 to-gray-100 py-8">
    <div class="max-w-7xl mx-auto px-4">
      <!-- Header -->
      <div class="text-center mb-12">
        <h1 class="text-4xl md:text-6xl font-bold text-gray-900 mb-4 font-['Inknut_Antiqua',serif]">
          Đặt Bàn Hoa Ban
        </h1>
        <p class="text-xl text-gray-600 max-w-2xl mx-auto">
          Trải nghiệm ẩm thực tây bắc đặc sắc trong không gian sang trọng và ấm cúng
        </p>
      </div>

      <div class="grid lg:grid-cols-2 gap-8">
        <!-- Left: Enhanced Gallery -->
        <div class="space-y-6">
          <!-- Main Image with Auto Slideshow -->
          <div class="relative group">
            <div class="relative h-96 lg:h-[500px] rounded-2xl overflow-hidden shadow-2xl">
              <img
                :src="galleryImages[currentImageIndex]"
                :alt="`Gallery ${currentImageIndex + 1}`"
                class="w-full h-full object-cover transition-transform duration-700 group-hover:scale-105"
              />

              <!-- Navigation arrows -->
              <button
                @click="prevImage"
                class="absolute left-4 top-1/2 -translate-y-1/2 w-12 h-12 bg-white/90 hover:bg-white rounded-full flex items-center justify-center shadow-lg transition-all opacity-0 group-hover:opacity-100"
              >
                <svg
                  class="w-6 h-6 text-gray-700"
                  fill="none"
                  stroke="currentColor"
                  viewBox="0 0 24 24"
                >
                  <path
                    stroke-linecap="round"
                    stroke-linejoin="round"
                    stroke-width="2"
                    d="M15 19l-7-7 7-7"
                  ></path>
                </svg>
              </button>

              <button
                @click="nextImage"
                class="absolute right-4 top-1/2 -translate-y-1/2 w-12 h-12 bg-white/90 hover:bg-white rounded-full flex items-center justify-center shadow-lg transition-all opacity-0 group-hover:opacity-100"
              >
                <svg
                  class="w-6 h-6 text-gray-700"
                  fill="none"
                  stroke="currentColor"
                  viewBox="0 0 24 24"
                >
                  <path
                    stroke-linecap="round"
                    stroke-linejoin="round"
                    stroke-width="2"
                    d="M9 5l7 7-7 7"
                  ></path>
                </svg>
              </button>

              <!-- Image indicators -->
              <div class="absolute bottom-4 left-1/2 -translate-x-1/2 flex gap-2">
                <button
                  v-for="(img, index) in galleryImages"
                  :key="index"
                  @click="selectImage(index)"
                  :class="[
                    'w-3 h-3 rounded-full transition-all',
                    index === currentImageIndex ? 'bg-white' : 'bg-white/50 hover:bg-white/80',
                  ]"
                ></button>
              </div>
            </div>
          </div>

          <!-- Thumbnail Grid -->
          <div class="grid grid-cols-5 gap-3">
            <button
              v-for="(img, index) in galleryImages"
              :key="index"
              @click="selectImage(index)"
              :class="[
                'relative h-20 rounded-lg overflow-hidden transition-all duration-300',
                index === currentImageIndex
                  ? 'ring-4 ring-[#9f0909] scale-105'
                  : 'hover:ring-2 hover:ring-[#9f0909]/50 hover:scale-102',
              ]"
            >
              <img :src="img" :alt="`Thumbnail ${index + 1}`" class="w-full h-full object-cover" />
            </button>
          </div>

          <!-- Restaurant Info & Map -->
          <div class="bg-white rounded-2xl p-6 shadow-lg">
            <h3 class="text-xl font-bold text-gray-900 mb-4">📍 Thông tin nhà hàng</h3>
            <div class="space-y-3 text-gray-600 mb-4">
              <div class="flex items-center gap-3">
                <span class="text-[#9f0909]">📍</span>
                <span>392 Nguyễn Phước Lan, Phường Hòa Xuân, Q. Cẩm Lệ, TP. Đà Nẵng</span>
              </div>
              <div class="flex items-center gap-3">
                <span class="text-[#9f0909]">📞</span>
                <a href="tel:+84868030043" class="hover:text-[#9f0909] transition-colors"
                  >0868030043</a
                >
              </div>
              <div class="flex items-center gap-3">
                <span class="text-[#9f0909]">🕐</span>
                <span>9:00 - 22:00 (Thứ 2 - Chủ nhật)</span>
              </div>
            </div>

            <!-- Google Map -->
            <div v-if="showMap">
              <GoogleMap
                address="392 Nguyễn Phước Lan, Phường Hòa Xuân, Q. Cẩm Lệ, TP. Đà Nẵng"
                :lat="16.0544068"
                :lng="108.2021667"
                :zoom="17"
                :always-show="true"
                :show-info="true"
                :show-toggle="false"
                class="h-64 md:h-72"
              />
            </div>

            <!-- Map Toggle Button -->
            <div class="mt-4">
              <button
                v-if="!showMap"
                @click="showMap = true"
                class="w-full flex items-center justify-center gap-2 py-3 px-4 bg-[#9f0909] text-white rounded-lg hover:bg-[#800808] transition-colors font-medium"
              >
                <svg class="w-5 h-5" fill="none" stroke="currentColor" viewBox="0 0 24 24">
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
                <span>Xem bản đồ đường đi</span>
              </button>

              <button
                v-else
                @click="showMap = false"
                class="w-full flex items-center justify-center gap-2 py-2 px-4 bg-gray-100 text-gray-700 rounded-lg hover:bg-gray-200 transition-colors font-medium"
              >
                <svg class="w-4 h-4" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                  <path
                    stroke-linecap="round"
                    stroke-linejoin="round"
                    stroke-width="2"
                    d="M6 18L18 6M6 6l12 12"
                  />
                </svg>
                <span>Ẩn bản đồ</span>
              </button>
            </div>
          </div>
        </div>

        <!-- Right: Enhanced Form -->
        <div class="relative">
          <div
            class="bg-gradient-to-br from-[#7a1010] to-[#9f0909] rounded-2xl p-8 shadow-2xl text-white sticky top-8"
          >
            <!-- Header with Logo -->
            <div class="flex items-center gap-4 mb-8">
              <img
                :src="resolveImage('/src/assets/img/logo.png')"
                alt="Hoa Ban Logo"
                class="w-16 h-16 rounded-full border-3 border-white/50 shadow-lg"
              />
              <div>
                <h2 class="text-3xl font-bold font-['Inknut_Antiqua',serif]">Đặt Bàn Ngay</h2>
                <p class="text-white/80">Trải nghiệm ẩm thực tuyệt vời</p>
              </div>
            </div>

            <form @submit.prevent="submit" class="space-y-6">
              <!-- Login hint for guests -->
              <div v-if="!auth.user" class="bg-white/10 rounded-xl p-4 mb-6">
                <p class="text-sm text-white/80 text-center">
                  💡
                  <router-link to="/auth/login" class="text-white font-medium hover:underline"
                    >Đăng nhập</router-link
                  >
                  để tự động điền thông tin và theo dõi đơn đặt bàn
                </p>
              </div>

              <!-- Customer Info -->
              <div class="grid md:grid-cols-2 gap-4">
                <div>
                  <label class="flex items-center gap-2 text-sm font-medium text-white/90 mb-2"
                    >Tên khách hàng *
                    <span
                      v-if="auth.user && customerName"
                      class="text-xs bg-white/20 px-2 py-1 rounded-full"
                    >
                      ✓ Tự động điền
                    </span>
                  </label>
                  <input
                    v-model="customerName"
                    type="text"
                    required
                    :placeholder="auth.user ? 'Tên từ tài khoản của bạn' : 'Nhập tên của bạn'"
                    class="w-full h-12 px-4 rounded-xl bg-white text-gray-900 border border-gray-300 placeholder-gray-500 focus:outline-none focus:ring-2 focus:ring-[#9f0909] focus:border-[#9f0909] transition-all"
                  />
                </div>
                <div>
                  <label class="block text-sm font-medium text-white/90 mb-2"
                    >Số điện thoại *</label
                  >
                  <input
                    v-model="customerPhone"
                    type="tel"
                    required
                    placeholder="Nhập số điện thoại của bạn"
                    class="w-full h-12 px-4 rounded-xl bg-white text-gray-900 border border-gray-300 placeholder-gray-500 focus:outline-none focus:ring-2 focus:ring-[#9f0909] focus:border-[#9f0909] transition-all"
                  />
                </div>
              </div>

              <!-- Date & Time -->
              <div class="grid md:grid-cols-2 gap-4">
                <div>
                  <label class="block text-sm font-medium text-white/90 mb-2">Ngày đặt *</label>
                  <input
                    v-model="date"
                    type="date"
                    required
                    :min="new Date().toISOString().split('T')[0]"
                    class="w-full h-12 px-4 rounded-xl bg-white text-gray-900 border border-gray-300 focus:outline-none focus:ring-2 focus:ring-[#9f0909] focus:border-[#9f0909] transition-all"
                  />
                </div>
                <div>
                  <label class="block text-sm font-medium text-white/90 mb-2">Giờ đặt *</label>
                  <div class="relative">
                    <button
                      type="button"
                      @click="showTimeSlots = !showTimeSlots"
                      class="w-full h-12 px-4 rounded-xl bg-white text-gray-900 border border-gray-300 focus:outline-none focus:ring-2 focus:ring-[#9f0909] focus:border-[#9f0909] transition-all flex items-center justify-between"
                    >
                      <span>{{ time || "Chọn giờ" }}</span>
                      <svg class="w-5 h-5" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                        <path
                          stroke-linecap="round"
                          stroke-linejoin="round"
                          stroke-width="2"
                          d="M19 9l-7 7-7-7"
                        ></path>
                      </svg>
                    </button>

                    <!-- Time Slots Dropdown -->
                    <div
                      v-if="showTimeSlots"
                      class="absolute top-full left-0 right-0 mt-2 bg-white rounded-xl shadow-2xl z-50 p-2 max-h-60 overflow-y-auto"
                    >
                      <button
                        v-for="slot in timeSlots"
                        :key="slot.time"
                        type="button"
                        @click="selectTimeSlot(slot.time)"
                        :disabled="!slot.available"
                        :class="[
                          'w-full p-3 rounded-lg text-left transition-all',
                          slot.available
                            ? 'hover:bg-[#9f0909] hover:text-white text-gray-700'
                            : 'text-gray-400 cursor-not-allowed',
                          slot.popular && slot.available
                            ? 'bg-amber-50 border border-amber-200'
                            : '',
                          time === slot.time ? 'bg-[#9f0909] text-white' : '',
                        ]"
                      >
                        <div class="flex items-center justify-between">
                          <span class="font-medium">{{ slot.time }}</span>
                          <div class="flex gap-1">
                            <span
                              v-if="slot.popular && slot.available"
                              class="text-xs bg-amber-500 text-white px-2 py-1 rounded-full"
                            >
                              Phổ biến
                            </span>
                            <span
                              v-if="!slot.available"
                              class="text-xs bg-gray-400 text-white px-2 py-1 rounded-full"
                            >
                              Hết chỗ
                            </span>
                          </div>
                        </div>
                      </button>
                    </div>
                  </div>
                </div>
              </div>

              <!-- Party Size & Area -->
              <div class="grid md:grid-cols-2 gap-4">
                <div>
                  <label class="block text-sm font-medium text-white/90 mb-2"
                    >Số lượng khách *</label
                  >
                  <select
                    v-model="partySize"
                    class="w-full h-12 px-4 rounded-xl bg-white text-gray-900 border border-gray-300 focus:outline-none focus:ring-2 focus:ring-[#9f0909] focus:border-[#9f0909] transition-all"
                  >
                    <option v-for="i in 15" :key="i" :value="i" class="text-gray-800">
                      {{ i }} người
                    </option>
                  </select>
                </div>
                <div>
                  <label class="block text-sm font-medium text-white/90 mb-2">Khu vực</label>
                  <button
                    type="button"
                    @click="showAreaModal = true"
                    class="w-full h-12 px-4 rounded-xl bg-white text-gray-900 border border-gray-300 focus:outline-none focus:ring-2 focus:ring-[#9f0909] focus:border-[#9f0909] transition-all flex items-center justify-between"
                  >
                    <span>{{ areas.find((a) => a.name === area)?.name || "Chọn khu vực" }}</span>
                    <svg class="w-5 h-5" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                      <path
                        stroke-linecap="round"
                        stroke-linejoin="round"
                        stroke-width="2"
                        d="M19 9l-7 7-7-7"
                      ></path>
                    </svg>
                  </button>
                </div>
              </div>

              <!-- Special Requests -->
              <div>
                <label class="block text-sm font-medium text-white/90 mb-2">Yêu cầu đặc biệt</label>
                <textarea
                  v-model="note"
                  rows="3"
                  placeholder="Sinh nhật, kỷ niệm, món ăn chay, dị ứng thực phẩm..."
                  class="w-full px-4 py-3 rounded-xl bg-white text-gray-900 border border-gray-300 placeholder-gray-500 focus:outline-none focus:ring-2 focus:ring-[#9f0909] focus:border-[#9f0909] transition-all resize-none"
                ></textarea>
              </div>

              <!-- Action Buttons -->
              <div class="space-y-4">
                <!-- Main Submit Button -->
                <button
                  type="submit"
                  :disabled="isLoading"
                  class="w-full h-14 bg-white text-[#7a1010] hover:bg-gray-100 rounded-xl font-bold text-lg transition-all transform hover:scale-[1.02] disabled:opacity-50 disabled:cursor-not-allowed shadow-lg flex items-center justify-center gap-2"
                >
                  <template v-if="isLoading">
                    <svg class="animate-spin w-5 h-5" fill="none" viewBox="0 0 24 24">
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
                    <span>Đang xử lý...</span>
                  </template>
                  <template v-else>
                    <span>🍽️</span>
                    <span>Đặt bàn ngay</span>
                  </template>
                </button>
              </div>
            </form>
          </div>
        </div>
      </div>
    </div>

    <!-- Area Selection Modal -->
    <div
      v-if="showAreaModal"
      class="fixed inset-0 z-50 flex items-center justify-center p-4 bg-black/50"
    >
      <div class="bg-white rounded-2xl shadow-2xl max-w-3xl w-full max-h-[85vh] overflow-hidden">
        <div class="flex items-center justify-between p-4 border-b">
          <h3 class="text-xl font-bold text-gray-900">Chọn Khu Vực</h3>
          <button
            @click="showAreaModal = false"
            class="w-8 h-8 rounded-full bg-gray-100 hover:bg-gray-200 flex items-center justify-center transition-colors"
          >
            <svg class="w-5 h-5" fill="none" stroke="currentColor" viewBox="0 0 24 24">
              <path
                stroke-linecap="round"
                stroke-linejoin="round"
                stroke-width="2"
                d="M6 18L18 6M6 6l12 12"
              ></path>
            </svg>
          </button>
        </div>

        <div class="p-4 overflow-y-auto max-h-[calc(85vh-80px)]">
          <div v-if="areas.length === 0" class="text-center py-8 text-gray-500">
            <p>Đang tải danh sách khu vực...</p>
          </div>
          <div v-else class="grid md:grid-cols-2 gap-4">
            <button
              v-for="areaData in areas.filter((a) => a.status === 'ACTIVE')"
              :key="areaData.id"
              @click="selectArea(areaData)"
              class="text-left p-4 rounded-xl border-2 border-gray-200 hover:border-[#9f0909] hover:shadow-lg transition-all group"
            >
              <div class="aspect-[16/10] rounded-lg overflow-hidden mb-3">
                <img
                  :src="areaData.images[0]"
                  :alt="areaData.name"
                  class="w-full h-full object-cover group-hover:scale-105 transition-transform duration-300"
                />
              </div>
              <h4 class="text-lg font-bold text-gray-900 mb-2">{{ areaData.name }}</h4>
              <p class="text-gray-600 mb-2 text-sm">
                {{ areaData.description || "Không gian thoải mái và sang trọng" }}
              </p>
            </button>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<style scoped>
/* Custom animations */
@keyframes slideIn {
  from {
    opacity: 0;
    transform: translateY(20px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}

.animate-slide-in {
  animation: slideIn 0.6s ease-out;
}

/* Custom scrollbar */
::-webkit-scrollbar {
  width: 6px;
}

::-webkit-scrollbar-track {
  background: #f1f1f1;
  border-radius: 3px;
}

::-webkit-scrollbar-thumb {
  background: #9f0909;
  border-radius: 3px;
}

::-webkit-scrollbar-thumb:hover {
  background: #7a1010;
}

/* Date input custom styling */
input[type="date"]::-webkit-calendar-picker-indicator {
  filter: brightness(0.5);
  cursor: pointer;
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

/* Modal backdrop animation */
.modal-backdrop {
  backdrop-filter: blur(4px);
}

/* Form focus enhancements */
input:focus,
select:focus,
textarea:focus,
button:focus {
  box-shadow: 0 0 0 3px rgba(255, 255, 255, 0.1);
}

/* Responsive adjustments */
@media (max-width: 768px) {
  .sticky-form {
    max-height: calc(100vh - 100px);
    overflow-y: auto;
  }

  .gallery-container {
    order: 2;
  }

  .form-container {
    order: 1;
    position: relative !important;
  }
}

/* Enhanced hover effects */
.area-card:hover {
  transform: translateY(-4px);
  box-shadow: 0 20px 25px -5px rgba(0, 0, 0, 0.1), 0 10px 10px -5px rgba(0, 0, 0, 0.04);
}

/* Time slot styling */
.time-slot {
  transition: all 0.2s ease;
}

.time-slot:hover:not(:disabled) {
  transform: translateX(4px);
}

/* Gradient text effect */
.gradient-text {
  background: linear-gradient(135deg, #9f0909 0%, #7a1010 100%);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  background-clip: text;
}
</style>
