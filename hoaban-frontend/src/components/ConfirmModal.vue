<script setup lang="ts">
defineProps<{
  show: boolean;
  title?: string;
  message: string;
  itemName?: string;
  confirmText?: string;
  cancelText?: string;
  loading?: boolean;
  variant?: "danger" | "warning" | "info";
}>();

const emit = defineEmits<{
  confirm: [];
  cancel: [];
}>();
</script>

<template>
  <div v-if="show" class="modal-overlay">
    <div class="modal-content max-w-md">
      <div class="modal-header">
        <h3 class="text-xl font-bold">{{ title || "Xác nhận" }}</h3>
        <button @click="emit('cancel')" class="text-white/60 hover:text-white">✕</button>
      </div>

      <div class="modal-body">
        <p class="text-white/80">
          {{ message }}
          <strong v-if="itemName">{{ itemName }}</strong>
          {{ message.includes("?") ? "" : " không?" }}
        </p>
        <span class="text-red-400 text-sm mt-2 block"> Hành động này không thể hoàn tác! </span>
      </div>

      <div class="modal-footer">
        <button @click="emit('cancel')" class="btn-admin-secondary">
          {{ cancelText || "Hủy" }}
        </button>
        <button
          @click="emit('confirm')"
          :disabled="loading"
          :class="[
            variant === 'danger'
              ? 'btn-admin-danger'
              : variant === 'warning'
              ? 'btn-admin-warning'
              : 'btn-admin-primary',
          ]"
        >
          <span v-if="loading" class="loading-spinner"></span>
          <span v-else>{{ confirmText || "Xác nhận" }}</span>
        </button>
      </div>
    </div>
  </div>
</template>
