import { createRouter, createWebHistory, type RouteRecordRaw } from "vue-router";
import { useAuthStore } from "../stores/auth";
const routes: RouteRecordRaw[] = [
  { path: "/", redirect: { name: "home" } },
  { path: "/login", redirect: { path: "/auth/login" } },
  {
    path: "/",
    component: () => import("@/layouts/PublicLayout.vue"),
    children: [
      { path: "home", name: "home", component: () => import("@/pages/Home.vue") },
      { path: "menu", name: "menu", component: () => import("@/pages/Menu.vue") },
      { path: "combos", name: "combos", component: () => import("@/views/ComboView.vue") },
      {
        path: "reservation",
        name: "reservation",
        component: () => import("@/pages/Reservation.vue"),
      },
      { path: "cart", name: "cart", component: () => import("@/pages/Cart.vue") },
      {
        path: "checkout",
        name: "checkout",
        meta: { requiresAuth: true },
        component: () => import("@/pages/Checkout.vue"),
      },
      {
        path: "order-success/:id",
        name: "order-success",
        component: () => import("@/pages/OrderSuccess.vue"),
      },
      {
        path: "vnpay-return",
        name: "vnpay-return",
        component: () => import("@/pages/VNPayReturn.vue"),
      },
      {
        path: "profile",
        name: "profile",
        meta: { requiresAuth: true },
        component: () => import("@/pages/Profile.vue"),
      },
      {
        path: "history",
        name: "history",
        meta: { requiresAuth: true },
        component: () => import("@/pages/History.vue"),
      },
      {
        path: "vouchers",
        name: "vouchers",
        meta: { requiresAuth: true },
        component: () => import("@/pages/Vouchers.vue"),
      },
      {
        path: "change-password",
        name: "change-password",
        meta: { requiresAuth: true },
        component: () => import("@/pages/ChangePassword.vue"),
      },
      {
        path: "update-profile",
        name: "update-profile",
        meta: { requiresAuth: true },
        component: () => import("@/pages/UpdateProfile.vue"),
      },
    ],
  },
  // Open order by QR code (no header/footer for better UX)
  {
    path: "/open-order",
    name: "open-order",
    component: () => import("@/pages/OpenByQr.vue"),
  },
  {
    path: "/auth",
    component: () => import("@/layouts/AuthLayout.vue"),
    children: [
      {
        path: "login",
        name: "login",
        meta: { requiresGuest: true },
        component: () => import("@/pages/auth/Login.vue"),
      },
      {
        path: "register",
        name: "register",
        meta: { requiresGuest: true },
        component: () => import("@/pages/auth/Register.vue"),
      },
      {
        path: "forgot-password",
        name: "forgot-password",
        meta: { requiresGuest: true },
        component: () => import("@/pages/auth/ForgotPassword.vue"),
      },
    ],
  },
  // Admin
  {
    path: "/admin",
    component: () => import("@/layouts/AdminLayout.vue"),
    meta: { requiresAuth: true, role: "ADMIN" },
    children: [
      { path: "", name: "admin-dashboard", component: () => import("@/views/admin/Dashboard.vue") },
      { path: "users", name: "admin-users", component: () => import("@/views/admin/Users.vue") },
      { path: "dishes", name: "admin-dishes", component: () => import("@/views/admin/Dishes.vue") },
      { path: "combos", name: "admin-combos", component: () => import("@/views/admin/Combos.vue") },
      {
        path: "categories",
        name: "admin-categories",
        component: () => import("@/views/admin/Categories.vue"),
      },
      { path: "areas", name: "admin-areas", component: () => import("@/views/admin/Areas.vue") },
      { path: "tables", name: "admin-tables", component: () => import("@/views/admin/Tables.vue") },
      {
        path: "vouchers",
        name: "admin-vouchers",
        component: () => import("@/views/admin/Vouchers.vue"),
      },
      { path: "orders", name: "admin-orders", component: () => import("@/views/admin/Orders.vue") },
      {
        path: "reservations",
        name: "admin-reservations",
        component: () => import("@/views/admin/Reservations.vue"),
      },
      {
        path: "reviews",
        name: "admin-reviews",
        component: () => import("@/pages/admin/Reviews.vue"),
      },
    ],
  },
  // 404
  {
    path: "/:pathMatch(.*)*",
    name: "not-found",
    component: {
      template: `<div class='p-8 text-center'><h1 class='text-2xl font-bold mb-4'>404 - Không tìm thấy trang</h1><a href='/' class='text-blue-600 underline'>Về trang chủ</a></div>`,
    },
  },
];

const router = createRouter({
  history: createWebHistory(),
  routes,
  scrollBehavior(to, from, savedPosition) {
    // Nếu có savedPosition (back/forward button), sử dụng vị trí đã lưu
    if (savedPosition) {
      return savedPosition;
    }
    // Nếu có anchor (hash), scroll đến anchor đó
    if (to.hash) {
      return {
        el: to.hash,
        behavior: "smooth",
      };
    }
    // Mặc định: scroll về đầu trang
    return {
      top: 0,
      behavior: "smooth",
    };
  },
});

// Auth guard
router.beforeEach(async (to) => {
  const auth = useAuthStore();

  // Nếu chưa load user mà đã có token -> fetch trước (chỉ thực hiện 1 lần)
  if (auth.token && !auth.loadedUser) {
    try {
      await auth.fetchMe();
    } catch (e) {
      // fetchMe đã tự xử lý token invalid
    }
  }

  const isLogged = !!auth.token;
  if (to.meta.requiresAuth && !isLogged) {
    return { name: "login", query: { redirect: to.fullPath } };
  }
  if (to.meta.role && auth.user && (auth.user as any).role !== to.meta.role) {
    return { name: "home" };
  }
  if (to.meta.requiresGuest && isLogged) {
    return { name: "home" };
  }
  return true;
});

export default router;
