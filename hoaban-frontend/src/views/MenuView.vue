<script setup lang="ts">
import { ref, onMounted, computed } from "vue";
import { useRoute } from "vue-router";
import { listDishes, type Dish } from "@/api/dishes.service";
import { listCategories } from "@/api/categories.admin";
import type { Category } from "@/types";
import DishCard from "@/components/DishCard.vue";
import DishModal from "@/components/DishModal.vue";
import { useUiStore } from "@/stores/ui";

const ui = useUiStore();
const route = useRoute();
const dishes = ref<Dish[]>([]);
const categories = ref<Category[]>([]);
const loading = ref(true);
const selectedCategory = ref<string | null>(null);
const searchQuery = ref("");

const filteredItems = computed(() => {
  let allItems: any[] = [...dishes.value];

  // Filter by category
  if (selectedCategory.value) {
    allItems = allItems.filter((dish) => dish.categoryId === selectedCategory.value);
  }

  // Filter by search
  if (searchQuery.value.trim()) {
    const query = searchQuery.value.toLowerCase();
    allItems = allItems.filter(
      (item) =>
        item.name.toLowerCase().includes(query) || item.description?.toLowerCase().includes(query)
    );
  }

  return allItems;
});

// Helper function to get category name by ID
const getCategoryName = (categoryId: string) => {
  const category = categories.value.find((cat) => cat.id === categoryId);
  return category?.name || "";
};

const fetchData = async () => {
  try {
    loading.value = true;
    const [dishesRes, categoriesRes] = await Promise.all([listDishes(), listCategories()]);

    // Handle pagination response structure
    const dishesData = dishesRes?.content || dishesRes?.data?.content || dishesRes?.data || [];
    dishes.value = Array.isArray(dishesData) ? dishesData : [];

    const categoriesData = categoriesRes?.data || categoriesRes || [];
    categories.value = Array.isArray(categoriesData) ? categoriesData : [];

    // Set initial category from query parameter
    const categoryQuery = route.query.category as string;
    if (categoryQuery && categories.value.some((cat) => cat.id === categoryQuery)) {
      selectedCategory.value = categoryQuery;
    }
  } catch (error) {
    console.error("Error fetching menu data:", error);
  } finally {
    loading.value = false;
  }
};

onMounted(() => {
  fetchData();
});
</script>

<template>
  <div class="min-h-screen bg-white">
    <!-- Header Section -->
    <div class="bg-white border-b border-gray-200">
      <div class="max-w-7xl mx-auto px-4 py-8 text-center">
        <h1 class="text-3xl md:text-4xl font-bold text-gray-900 mb-2">Thực Đơn Nhà Hàng</h1>
        <p class="text-lg text-gray-600">Thưởng thức ẩm thực tây bắc tại Đà Nẵng</p>
      </div>
    </div>

    <div class="max-w-7xl mx-auto px-4 py-8">
      <!-- Search Bar -->
      <div class="mb-8">
        <div class="relative max-w-md mx-auto">
          <input
            v-model="searchQuery"
            type="text"
            placeholder="Tìm kiếm món ăn"
            class="w-full pl-4 pr-12 py-3 border border-gray-300 rounded-lg focus:ring-2 focus:ring-[#9f0909] focus:border-[#9f0909] text-lg"
          />
          <button
            class="absolute right-2 top-1/2 transform -translate-y-1/2 bg-gray-100 hover:bg-gray-200 p-2 rounded-lg transition-colors"
          >
            <svg
              class="w-5 h-5 text-gray-600"
              fill="none"
              stroke="currentColor"
              viewBox="0 0 24 24"
            >
              <path
                stroke-linecap="round"
                stroke-linejoin="round"
                stroke-width="2"
                d="M21 21l-6-6m2-5a7 7 0 11-14 0 7 7 0 0114 0z"
              />
            </svg>
          </button>
        </div>
      </div>

      <!-- Filter Tabs -->
      <div class="mb-8">
        <div class="flex flex-wrap justify-center gap-2">
          <!-- Tất cả -->
          <button
            @click="selectedCategory = null"
            :class="
              selectedCategory === null
                ? 'bg-[#9f0909] text-white'
                : 'bg-gray-100 text-gray-700 hover:bg-gray-200'
            "
            class="px-6 py-2 rounded-full font-medium transition-colors"
          >
            Tất cả
          </button>

          <!-- Categories từ API -->
          <button
            v-for="category in categories"
            :key="category.id"
            @click="selectedCategory = category.id"
            :class="
              selectedCategory === category.id
                ? 'bg-[#9f0909] text-white'
                : 'bg-gray-100 text-gray-700 hover:bg-gray-200'
            "
            class="px-6 py-2 rounded-full font-medium transition-colors"
          >
            {{ category.name }}
          </button>
        </div>
      </div>

      <!-- Loading State -->
      <div
        v-if="loading"
        class="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 xl:grid-cols-4 gap-6 mb-8"
      >
        <div v-for="i in 12" :key="i" class="bg-gray-200 rounded-2xl h-80 animate-pulse"></div>
      </div>

      <!-- Items Grid -->
      <div
        v-else-if="filteredItems.length > 0"
        class="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 xl:grid-cols-4 gap-6"
      >
        <DishCard
          v-for="dish in filteredItems"
          :key="dish.id"
          :id="dish.id"
          :categoryId="dish.categoryId"
          :categoryName="getCategoryName(dish.categoryId)"
          :name="dish.name"
          :price="dish.price"
          :imageUrl="dish.imageUrl"
          :description="dish.description"
          :unit="dish.unit"
          :status="dish.status"
          class="transform hover:scale-105 transition-all duration-300"
        />
      </div>

      <!-- Empty State -->
      <div v-else class="text-center py-16">
        <div class="w-24 h-24 mx-auto mb-6 text-gray-300">
          <svg fill="none" stroke="currentColor" viewBox="0 0 24 24" class="w-full h-full">
            <path
              stroke-linecap="round"
              stroke-linejoin="round"
              stroke-width="1"
              d="M19 11H5m14 0a2 2 0 012 2v6a2 2 0 01-2 2H5a2 2 0 01-2-2v-6a2 2 0 012-2m14 0V9a2 2 0 00-2-2M5 11V9a2 2 0 012-2m0 0V5a2 2 0 012-2h6a2 2 0 012 2v2M7 7h10"
            />
          </svg>
        </div>
        <h3 class="text-xl font-medium text-gray-900 mb-2">Không tìm thấy món ăn</h3>
        <p class="text-gray-500 mb-4">Thử thay đổi từ khóa tìm kiếm hoặc chọn danh mục khác</p>
        <button
          @click="
            searchQuery = '';
            selectedCategory = null;
          "
          class="inline-flex items-center px-4 py-2 bg-[#9f0909] text-white rounded-lg hover:bg-[#800808] transition-colors"
        >
          Xem tất cả món ăn
        </button>
      </div>
    </div>

    <!-- Dish Modal -->
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

/* Custom hover effects */
.transform {
  transition: transform 0.3s ease;
}

.transform:hover {
  transform: scale(1.02);
}

/* Focus styles for accessibility */
input:focus,
button:focus {
  outline: 2px solid #9f0909;
  outline-offset: 2px;
}

/* Smooth scrolling */
html {
  scroll-behavior: smooth;
}
</style>
