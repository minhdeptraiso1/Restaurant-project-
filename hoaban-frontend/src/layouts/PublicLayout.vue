<script setup lang="ts">
import DishModal from "@/components/DishModal.vue";
import ComboModal from "@/components/ComboModal.vue";
import AppHeader from "@/components/AppHeader.vue";
import AppFooter from "@/components/AppFooter.vue";
import ChatBot from "@/components/ChatBot.vue";
import { useUiStore } from "@/stores/ui";

const ui = useUiStore();
</script>

<template>
  <div class="min-h-screen flex flex-col bg-emerald-50/40">
    <AppHeader />
    <main class="flex-1 container mx-auto px-4 py-8">
      <RouterView />
    </main>
    <DishModal />
    <ComboModal v-if="ui.showComboModal" />
    <ChatBot />
    <AppFooter />
  </div>
</template>
