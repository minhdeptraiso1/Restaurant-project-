<template>
  <!-- Google Maps Component -->
  <div class="relative">
    <!-- Map Container -->
    <div
      v-if="showMap"
      :class="[
        'w-full rounded-xl overflow-hidden shadow-lg border border-white/20',
        $attrs.class || 'h-64 md:h-80',
      ]"
    >
      <iframe
        :src="mapSrc"
        class="w-full h-full"
        style="border: 0"
        allowfullscreen
        loading="lazy"
        referrerpolicy="no-referrer-when-downgrade"
      ></iframe>
    </div>

    <!-- Map Toggle Button (for footer) -->
    <button
      v-if="showToggle && !showMap"
      @click="toggleMap"
      class="inline-flex items-center gap-2 px-4 py-2 bg-white/20 hover:bg-white/30 rounded-lg transition-colors text-sm font-medium"
    >
      <svg class="w-4 h-4" fill="none" stroke="currentColor" viewBox="0 0 24 24">
        <path
          stroke-linecap="round"
          stroke-linejoin="round"
          stroke-width="2"
          d="M9 20l-5.447-2.724A1 1 0 013 16.382V5.618a1 1 0 011.447-.894L9 7m0 13l6-3m-6 3V7m6 10l4.553 2.276A1 1 0 0021 18.382V7.618a1 1 0 00-1.447-.894L15 9m0 8V9m0 0V7"
        />
      </svg>
      <span>Xem bản đồ</span>
    </button>

    <!-- Map overlay with restaurant info (for reservation page) -->
    <div
      v-if="showMap && showInfo"
      class="absolute top-4 left-4 bg-white/95 backdrop-blur rounded-lg p-4 shadow-lg max-w-xs"
    >
      <h4 class="font-bold text-gray-900 mb-2">📍 Hoa Ban Restaurant</h4>
      <p class="text-sm text-gray-600 mb-2">{{ address }}</p>
      <div class="flex gap-2">
        <a
          :href="directionsUrl"
          target="_blank"
          class="text-xs bg-[#9f0909] text-white px-3 py-1 rounded-full hover:bg-[#800808] transition-colors"
        >
          Chỉ đường
        </a>
        <button
          @click="copyAddress"
          class="text-xs bg-gray-100 text-gray-700 px-3 py-1 rounded-full hover:bg-gray-200 transition-colors"
        >
          Sao chép
        </button>
      </div>
    </div>

    <!-- Close button for footer maps -->
    <button
      v-if="showMap && showToggle"
      @click="closeMap"
      class="absolute top-3 right-3 w-8 h-8 bg-white/90 hover:bg-white rounded-full flex items-center justify-center shadow-lg transition-colors"
    >
      <svg class="w-4 h-4 text-gray-700" fill="none" stroke="currentColor" viewBox="0 0 24 24">
        <path
          stroke-linecap="round"
          stroke-linejoin="round"
          stroke-width="2"
          d="M6 18L18 6M6 6l12 12"
        />
      </svg>
    </button>
  </div>
</template>

<script setup lang="ts">
import { ref, computed } from "vue";
import { toast } from "vue3-toastify";

// Disable automatic attribute inheritance
defineOptions({
  inheritAttrs: false,
});

const props = defineProps<{
  address?: string;
  lat?: number;
  lng?: number;
  zoom?: number;
  showToggle?: boolean; // For footer - show toggle button
  showInfo?: boolean; // For reservation - show restaurant info overlay
  alwaysShow?: boolean; // For reservation - always show map
}>();

const showMap = ref(props.alwaysShow || false);

// Default coordinates for Hoa Ban Restaurant in Da Nang
const defaultAddress = "392 Nguyễn Phước Lan, Phường Hòa Xuân, Q. Cẩm Lệ, TP. Đà Nẵng";
const defaultLat = 16.0544068;
const defaultLng = 108.2021667;

const address = computed(() => props.address || defaultAddress);
const lat = computed(() => props.lat || defaultLat);
const lng = computed(() => props.lng || defaultLng);
const zoom = computed(() => props.zoom || 17);

// Google Maps embed URL
const mapSrc = computed(() => {
  const baseUrl = "https://www.google.com/maps/embed/v1/place";
  const apiKey = "AIzaSyBFw0Qbyq9zTFTd-tUY6dronpxnlwSudco"; // Replace with your actual API key

  // Alternative: Use address-based embed (no API key required)
  const addressQuery = encodeURIComponent(address.value);
  return `https://www.google.com/maps/embed?pb=!1m18!1m12!1m3!1d3834.172!2d${lng.value}!3d${lat.value}!2m3!1f0!2f0!3f0!3m2!1i1024!2i768!4f13.1!3m3!1m2!1s0x0%3A0x0!2z${addressQuery}!5e0!3m2!1svi!2s!4v1234567890!5m2!1svi!2s&zoom=${zoom.value}`;
});

// Google Maps directions URL
const directionsUrl = computed(() => {
  const query = encodeURIComponent(address.value);
  return `https://www.google.com/maps/dir/?api=1&destination=${query}`;
});

const toggleMap = () => {
  showMap.value = !showMap.value;
};

const closeMap = () => {
  showMap.value = false;
};

const copyAddress = async () => {
  try {
    await navigator.clipboard.writeText(address.value);
    toast.success("Đã sao chép địa chỉ!");
  } catch (error) {
    toast.error("Không thể sao chép địa chỉ");
  }
};
</script>

<style scoped>
/* Custom animations */
.map-enter-active,
.map-leave-active {
  transition: all 0.3s ease;
}

.map-enter-from,
.map-leave-to {
  opacity: 0;
  transform: translateY(-10px);
}

/* Loading state */
iframe {
  transition: opacity 0.3s ease;
}

/* Responsive adjustments */
@media (max-width: 768px) {
  .map-info {
    position: relative;
    top: auto;
    left: auto;
    margin-top: 1rem;
    max-width: none;
  }
}
</style>
