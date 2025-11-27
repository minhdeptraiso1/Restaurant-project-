import { defineStore } from "pinia";

export const useUiStore = defineStore("ui", {
  state: () => ({
    showCart: false,
    showDishModal: false,
    selectedDishId: "",
    showComboModal: false,
    selectedComboId: "",
  }),
  actions: {
    openCart() {
      this.showCart = true;
    },
    closeCart() {
      this.showCart = false;
    },
    openDish(dishId: string) {
      this.selectedDishId = dishId;
      this.showDishModal = true;
    },
    closeDish() {
      this.showDishModal = false;
      this.selectedDishId = "";
    },
    openCombo(comboId: string) {
      this.selectedComboId = comboId;
      this.showComboModal = true;
    },
    closeCombo() {
      this.showComboModal = false;
      this.selectedComboId = "";
    },
  },
});
