import { defineStore } from "pinia";
import * as cartApi from "@/api/cart.service";
import { openOrderSelf } from "@/api/orders.service";
import { listDishes } from "@/api/dishes.service";
import { listCombos } from "@/api/combos.service";
import { useAuthStore } from "./auth";
import { toast } from "vue3-toastify";

export type CartItem = {
  id: string;
  name: string;
  price: number;
  qty: number;
  type?: "DISH" | "COMBO";
};

type ServerOrderItem = {
  id: string; // item id in order (line id)
  itemType: string;
  itemId: string;
  name: string;
  unitPrice: number;
  quantity: number;
  lineTotal: number;
};

export const useCartStore = defineStore("cart", {
  state: () => ({
    items: [] as CartItem[],
    serverOrderId: "" as string,
    _dishesCache: [] as any[],
    _combosCache: [] as any[],
    _justCleared: false, // Flag to prevent loading old cart after payment
  }),
  getters: {
    total: (s) => s.items.reduce((sum, i) => sum + i.price * i.qty, 0),
  },
  actions: {
    async _loadCaches() {
      try {
        if (this._dishesCache.length === 0) {
          const response = await listDishes();
          const dishesData = response?.content || response?.data?.content || response?.data || [];
          this._dishesCache = Array.isArray(dishesData) ? dishesData : [];
        }
        if (this._combosCache.length === 0) {
          const response = await listCombos();
          const combosData = response?.data || response || [];
          this._combosCache = Array.isArray(combosData) ? combosData : [];
        }
      } catch (error) {
        console.error("Error loading caches:", error);
      }
    },
    // Load from server if logged in, otherwise keep local
    async load() {
      const auth = useAuthStore();
      if (!auth.token) return;

      // Don't load if we just cleared cart after payment
      if (this._justCleared) {
        console.log("🛒 Skipping cart load - cart was just cleared after payment");
        return;
      }

      try {
        const { data: order } = await cartApi.getCart();
        this.serverOrderId = order.id;

        // Load caches to get image URLs
        await this._loadCaches();

        const mapped: CartItem[] = (order.items as ServerOrderItem[]).map((it) => {
          let imageUrl: string | undefined = undefined;

          // Find image URL from cache
          if (it.itemType === "DISH") {
            const dish = this._dishesCache.find((d) => d.id === it.itemId);
            imageUrl = dish?.imageUrl;
          } else if (it.itemType === "COMBO") {
            const combo = this._combosCache.find((c) => c.id === it.itemId);
            imageUrl = combo?.imageUrl;
          }

          return {
            id: it.itemId,
            name: it.name,
            price: it.unitPrice,
            qty: it.quantity,
            type: it.itemType as "DISH" | "COMBO",
            ...(imageUrl ? { imageUrl } : {}),
          };
        });

        // Consolidate duplicate items (same id and type)
        const consolidated: CartItem[] = [];
        mapped.forEach((item) => {
          const existing = consolidated.find((c) => c.id === item.id && c.type === item.type);
          if (existing) {
            existing.qty += item.qty;
            console.log(`🛒 Consolidated duplicate item ${item.id}, total qty: ${existing.qty}`);
          } else {
            consolidated.push(item);
          }
        });

        this.items = consolidated;
      } catch (error) {
        console.error("Error loading cart:", error);
      }
    },
    async add(item: CartItem) {
      this.resetClearFlag(); // Reset flag when adding items
      const auth = useAuthStore();
      if (auth.token) {
        // Check if item already exists in current cart
        const existingItem = this.items.find((i) => i.id === item.id && i.type === item.type);

        if (existingItem) {
          // Update existing item quantity instead of adding new
          await this.setQty(item.id, existingItem.qty + item.qty);
        } else {
          // Add new item
          await cartApi.addItem({
            itemType: item.type || "DISH",
            itemId: item.id,
            quantity: item.qty,
          });
          await this.load();
        }
        toast.success(`Đã thêm ${item.name} vào giỏ hàng`);
        return;
      }
      const found = this.items.find((i) => i.id === item.id);
      if (found) found.qty += item.qty;
      else this.items.push({ ...item });
      toast.success(`Đã thêm ${item.name} vào giỏ hàng`);
    },
    async addItem(type: "DISH" | "COMBO", itemId: string, quantity: number = 1) {
      this.resetClearFlag(); // Reset flag when adding items
      const auth = useAuthStore();
      if (auth.token) {
        // Check if item already exists in current cart
        const existingItem = this.items.find((item) => item.id === itemId && item.type === type);

        if (existingItem) {
          // Update existing item quantity instead of adding new
          await this.setQty(itemId, existingItem.qty + quantity);
          toast.success(`Đã thêm ${existingItem.name} vào giỏ hàng`);
        } else {
          // Add new item
          await cartApi.addItem({ itemType: type, itemId, quantity });
          await this.load();
          // Find the added item to get its name
          const addedItem = this.items.find((item) => item.id === itemId && item.type === type);
          if (addedItem) {
            toast.success(`Đã thêm ${addedItem.name} vào giỏ hàng`);
          }
        }
        return;
      }

      // For local cart, fetch item details
      await this._loadCaches();

      let itemName = "";
      let itemPrice = 0;
      let imageUrl: string | undefined = undefined;

      if (type === "DISH") {
        const dish = this._dishesCache.find((d) => d.id === itemId);
        if (dish) {
          itemName = dish.name;
          itemPrice = dish.price;
          imageUrl = dish.imageUrl;
        }
      } else {
        const combo = this._combosCache.find((c) => c.id === itemId);
        if (combo) {
          itemName = combo.name;
          itemPrice = combo.price;
          imageUrl = combo.imageUrl;
        }
      }

      const found = this.items.find((i) => i.id === itemId);
      if (found) {
        found.qty += quantity;
      } else {
        this.items.push({
          id: itemId,
          name: itemName || `${type} Item`,
          price: itemPrice,
          qty: quantity,
          type,
          ...(imageUrl ? { imageUrl } : {}),
        });
      }
      toast.success(`Đã thêm ${itemName || `${type} Item`} vào giỏ hàng`);
    },
    async remove(id: string) {
      const auth = useAuthStore();
      if (auth.token) {
        // Find existing item in current items to get type
        const currentItem = this.items.find((i) => i.id === id);
        if (!currentItem) return;

        // Get latest cart to find all matching lines
        const { data: order } = await cartApi.getCart();

        // Find ALL lines with this itemId and same type
        const matchingLines = (order.items as ServerOrderItem[]).filter(
          (it) => it.itemId === id && it.itemType === currentItem.type
        );

        // Remove all matching lines (handles duplicates)
        for (const line of matchingLines) {
          await cartApi.removeItem(line.id);
        }

        await this.load();
        return;
      }
      this.items = this.items.filter((i) => i.id !== id);
    },
    async setQty(id: string, qty: number) {
      const auth = useAuthStore();
      if (qty <= 0) return this.remove(id);
      if (auth.token) {
        // Find existing line in current items first
        const currentItem = this.items.find((i) => i.id === id);
        if (!currentItem) return;

        // Get latest cart to find the correct line id
        const { data: order } = await cartApi.getCart();

        // Find ALL lines with this itemId and same type
        const matchingLines = (order.items as ServerOrderItem[]).filter(
          (it) => it.itemId === id && it.itemType === currentItem.type
        );

        if (matchingLines.length > 0) {
          // If there are multiple lines (duplicates), consolidate them
          if (matchingLines.length > 1) {
            console.log(
              `🛒 Found ${matchingLines.length} duplicate lines for item ${id}, consolidating...`
            );

            // Remove all duplicate lines
            for (const line of matchingLines) {
              await cartApi.removeItem(line.id);
            }

            // Add back as single item with total quantity
            await cartApi.addItem({
              itemType: currentItem.type || "DISH",
              itemId: id,
              quantity: qty,
            });
          } else {
            // Single line, just update quantity
            const line = matchingLines[0];
            if (line) {
              await cartApi.updateItem(line.id, qty);
            }
          }
        }

        await this.load();
        return;
      }
      const found = this.items.find((i) => i.id === id);
      if (found) found.qty = qty;
    },
    async clear() {
      // Clear local cart only (no server API)
      this.items = [];
      this.serverOrderId = "";
      console.log("🛒 Local cart cleared");
    },

    // Clear cart after successful payment
    clearAfterPayment(newOrderId: string) {
      this._justCleared = true;
      this.items = [];
      this.serverOrderId = newOrderId;
      console.log("🛒 Cart cleared after payment, new order:", newOrderId);
    },

    // Reset flag when adding new items
    resetClearFlag() {
      this._justCleared = false;
    },

    // Clean up duplicate items on server
    async cleanupDuplicates() {
      const auth = useAuthStore();
      if (!auth.token) return;

      try {
        const { data: order } = await cartApi.getCart();
        const items = order.items as ServerOrderItem[];

        // Group items by itemId and itemType
        const groups: { [key: string]: ServerOrderItem[] } = {};
        items.forEach((item) => {
          const key = `${item.itemId}_${item.itemType}`;
          if (!groups[key]) groups[key] = [];
          groups[key].push(item);
        });

        // Process groups with duplicates
        for (const [key, group] of Object.entries(groups)) {
          if (group.length > 1) {
            console.log(`🛒 Cleaning up ${group.length} duplicates for ${key}`);

            // Calculate total quantity
            const totalQty = group.reduce((sum, item) => sum + item.quantity, 0);

            // Remove all duplicates
            for (const item of group) {
              await cartApi.removeItem(item.id);
            }

            // Add back as single item
            const firstItem = group[0];
            if (firstItem) {
              await cartApi.addItem({
                itemType: firstItem.itemType as "DISH" | "COMBO",
                itemId: firstItem.itemId,
                quantity: totalQty,
              });
            }
          }
        }

        // Reload cart after cleanup
        await this.load();
      } catch (error) {
        console.error("Error cleaning up duplicates:", error);
      }
    },
  },
});
