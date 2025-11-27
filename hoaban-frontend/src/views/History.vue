<template>
  <div class="min-h-screen bg-gray-50 py-8">
    <div class="container mx-auto px-4 max-w-6xl">
      <h1 class="text-3xl font-bold text-gray-800 mb-8">Lịch sử của tôi</h1>

      <!-- Tabs -->
      <div class="mb-6 border-b border-gray-200">
        <nav class="-mb-px flex space-x-8">
          <button
            @click="activeTab = 'reservations'"
            :class="[
              'py-4 px-1 border-b-2 font-medium text-sm transition-colors',
              activeTab === 'reservations'
                ? 'border-emerald-500 text-emerald-600'
                : 'border-transparent text-gray-500 hover:text-gray-700 hover:border-gray-300',
            ]"
          >
            <span class="flex items-center gap-2">
              <svg class="w-5 h-5" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                <path
                  stroke-linecap="round"
                  stroke-linejoin="round"
                  stroke-width="2"
                  d="M8 7V3m8 4V3m-9 8h10M5 21h14a2 2 0 002-2V7a2 2 0 00-2-2H5a2 2 0 00-2 2v12a2 2 0 002 2z"
                />
              </svg>
              Đơn đặt bàn
            </span>
          </button>
          <button
            @click="activeTab = 'orders'"
            :class="[
              'py-4 px-1 border-b-2 font-medium text-sm transition-colors',
              activeTab === 'orders'
                ? 'border-emerald-500 text-emerald-600'
                : 'border-transparent text-gray-500 hover:text-gray-700 hover:border-gray-300',
            ]"
          >
            <span class="flex items-center gap-2">
              <svg class="w-5 h-5" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                <path
                  stroke-linecap="round"
                  stroke-linejoin="round"
                  stroke-width="2"
                  d="M16 11V7a4 4 0 00-8 0v4M5 9h14l1 12H4L5 9z"
                />
              </svg>
              Đơn hàng
            </span>
          </button>
        </nav>
      </div>

      <!-- Reservations Tab -->
      <div v-if="activeTab === 'reservations'">
        <div v-if="loadingReservations" class="text-center py-12">
          <div
            class="inline-block animate-spin rounded-full h-12 w-12 border-b-2 border-emerald-500"
          ></div>
          <p class="mt-4 text-gray-600">Đang tải...</p>
        </div>

        <div v-else-if="reservations.length === 0" class="text-center py-12">
          <svg
            class="mx-auto h-24 w-24 text-gray-400"
            fill="none"
            stroke="currentColor"
            viewBox="0 0 24 24"
          >
            <path
              stroke-linecap="round"
              stroke-linejoin="round"
              stroke-width="2"
              d="M8 7V3m8 4V3m-9 8h10M5 21h14a2 2 0 002-2V7a2 2 0 00-2-2H5a2 2 0 00-2 2v12a2 2 0 002 2z"
            />
          </svg>
          <h3 class="mt-4 text-lg font-medium text-gray-900">Chưa có đơn đặt bàn</h3>
          <p class="mt-2 text-gray-500">Bạn chưa có đơn đặt bàn nào. Hãy đặt bàn ngay!</p>
          <div class="mt-6">
            <router-link
              to="/reservation"
              class="inline-flex items-center px-6 py-3 bg-emerald-600 text-white rounded-lg hover:bg-emerald-700 transition-colors"
            >
              Đặt bàn ngay
            </router-link>
          </div>
        </div>

        <div v-else class="space-y-4">
          <div
            v-for="reservation in reservations"
            :key="reservation.id"
            class="bg-white rounded-lg shadow-md p-6 hover:shadow-lg transition-shadow"
          >
            <div class="flex items-start justify-between">
              <div class="flex-1">
                <div class="flex items-center gap-3 mb-3">
                  <span
                    :class="[
                      'px-3 py-1 rounded-full text-sm font-medium',
                      getStatusClass(reservation.status),
                    ]"
                  >
                    {{ getStatusText(reservation.status) }}
                  </span>
                  <span class="text-gray-500 text-sm"> Mã: {{ reservation.id.slice(0, 8) }} </span>
                </div>

                <div class="grid grid-cols-1 md:grid-cols-2 gap-4">
                  <div>
                    <div class="flex items-center gap-2 text-gray-700 mb-2">
                      <svg
                        class="w-5 h-5 text-emerald-600"
                        fill="none"
                        stroke="currentColor"
                        viewBox="0 0 24 24"
                      >
                        <path
                          stroke-linecap="round"
                          stroke-linejoin="round"
                          stroke-width="2"
                          d="M8 7V3m8 4V3m-9 8h10M5 21h14a2 2 0 002-2V7a2 2 0 00-2-2H5a2 2 0 00-2 2v12a2 2 0 002 2z"
                        />
                      </svg>
                      <span class="font-medium">{{ formatDate(reservation.startTime) }}</span>
                    </div>
                    <div class="flex items-center gap-2 text-gray-700">
                      <svg
                        class="w-5 h-5 text-emerald-600"
                        fill="none"
                        stroke="currentColor"
                        viewBox="0 0 24 24"
                      >
                        <path
                          stroke-linecap="round"
                          stroke-linejoin="round"
                          stroke-width="2"
                          d="M12 8v4l3 3m6-3a9 9 0 11-18 0 9 9 0 0118 0z"
                        />
                      </svg>
                      <span
                        >{{ formatTime(reservation.startTime) }} -
                        {{ formatTime(reservation.endTime) }}</span
                      >
                    </div>
                  </div>

                  <div>
                    <div class="flex items-center gap-2 text-gray-700 mb-2">
                      <svg
                        class="w-5 h-5 text-emerald-600"
                        fill="none"
                        stroke="currentColor"
                        viewBox="0 0 24 24"
                      >
                        <path
                          stroke-linecap="round"
                          stroke-linejoin="round"
                          stroke-width="2"
                          d="M17 20h5v-2a3 3 0 00-5.356-1.857M17 20H7m10 0v-2c0-.656-.126-1.283-.356-1.857M7 20H2v-2a3 3 0 015.356-1.857M7 20v-2c0-.656.126-1.283.356-1.857m0 0a5.002 5.002 0 019.288 0M15 7a3 3 0 11-6 0 3 3 0 016 0zm6 3a2 2 0 11-4 0 2 2 0 014 0zM7 10a2 2 0 11-4 0 2 2 0 014 0z"
                        />
                      </svg>
                      <span>{{ reservation.partySize }} người</span>
                    </div>
                    <div
                      v-if="reservation.note"
                      class="flex items-start gap-2 text-gray-600 text-sm"
                    >
                      <svg
                        class="w-5 h-5 text-emerald-600 flex-shrink-0"
                        fill="none"
                        stroke="currentColor"
                        viewBox="0 0 24 24"
                      >
                        <path
                          stroke-linecap="round"
                          stroke-linejoin="round"
                          stroke-width="2"
                          d="M7 8h10M7 12h4m1 8l-4-4H5a2 2 0 01-2-2V6a2 2 0 012-2h14a2 2 0 012 2v8a2 2 0 01-2 2h-3l-4 4z"
                        />
                      </svg>
                      <span>{{ reservation.note }}</span>
                    </div>
                  </div>
                </div>

                <div
                  v-if="reservation.tables && reservation.tables.length > 0"
                  class="mt-4 pt-4 border-t border-gray-200"
                >
                  <p class="text-sm text-gray-600 mb-2">Bàn đã được xếp:</p>
                  <span class="font-medium">{{ getTableNames(reservation) }}</span>
                </div>
              </div>

              <div v-if="canCancelReservation(reservation)" class="ml-4">
                <button
                  @click="showCancelModal(reservation)"
                  :disabled="cancellingId === reservation.id"
                  class="px-4 py-2 bg-red-500 text-white rounded-lg hover:bg-red-600 transition-colors disabled:opacity-50 disabled:cursor-not-allowed flex items-center gap-2"
                >
                  <svg
                    v-if="cancellingId === reservation.id"
                    class="animate-spin h-4 w-4"
                    fill="none"
                    viewBox="0 0 24 24"
                  >
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
                  <span v-else>Hủy đơn</span>
                </button>
              </div>
            </div>
          </div>
        </div>
      </div>

      <!-- Orders Tab -->
      <div v-if="activeTab === 'orders'">
        <div v-if="loadingOrders" class="text-center py-12">
          <div
            class="inline-block animate-spin rounded-full h-12 w-12 border-b-2 border-emerald-500"
          ></div>
          <p class="mt-4 text-gray-600">Đang tải...</p>
        </div>

        <div v-else-if="orders.length === 0" class="text-center py-12">
          <svg
            class="mx-auto h-24 w-24 text-gray-400"
            fill="none"
            stroke="currentColor"
            viewBox="0 0 24 24"
          >
            <path
              stroke-linecap="round"
              stroke-linejoin="round"
              stroke-width="2"
              d="M16 11V7a4 4 0 00-8 0v4M5 9h14l1 12H4L5 9z"
            />
          </svg>
          <h3 class="mt-4 text-lg font-medium text-gray-900">Chưa có đơn hàng</h3>
          <p class="mt-2 text-gray-500">Bạn chưa có đơn hàng nào. Hãy đặt món ngay!</p>
          <div class="mt-6">
            <router-link
              to="/menu"
              class="inline-flex items-center px-6 py-3 bg-emerald-600 text-white rounded-lg hover:bg-emerald-700 transition-colors"
            >
              Xem thực đơn
            </router-link>
          </div>
        </div>

        <div v-else class="space-y-4">
          <div
            v-for="order in orders"
            :key="order.id"
            class="bg-white rounded-lg shadow-md overflow-hidden hover:shadow-lg transition-shadow"
          >
            <!-- Order Header - Basic Info -->
            <div class="p-6 pb-4">
              <div class="flex items-start justify-between mb-4">
                <div class="flex-1">
                  <div class="flex items-center gap-3 mb-3">
                    <span
                      :class="[
                        'px-3 py-1 rounded-full text-sm font-medium',
                        getOrderStatusClass(order.status),
                      ]"
                    >
                      {{ getOrderStatusText(order.status) }}
                    </span>
                    <span class="text-gray-500 text-sm"> Mã: {{ order.id.slice(0, 8) }} </span>
                  </div>

                  <!-- Basic Order Info -->
                  <div class="grid grid-cols-1 md:grid-cols-3 gap-4">
                    <div>
                      <div v-if="order.note" class="flex items-start gap-2 text-gray-700 mb-2">
                        <svg
                          class="w-5 h-5 text-emerald-600 flex-shrink-0"
                          fill="none"
                          stroke="currentColor"
                          viewBox="0 0 24 24"
                        >
                          <path
                            stroke-linecap="round"
                            stroke-linejoin="round"
                            stroke-width="2"
                            d="M7 8h10M7 12h4m1 8l-4-4H5a2 2 0 01-2-2V6a2 2 0 012-2h14a2 2 0 012 2v8a2 2 0 01-2 2h-3l-4 4z"
                          />
                        </svg>
                        <span class="font-medium">{{ order.note }}</span>
                      </div>
                      <div v-else class="flex items-center gap-2 text-gray-500 mb-2">
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
                            d="M7 8h10M7 12h4m1 8l-4-4H5a2 2 0 01-2-2V6a2 2 0 012-2h14a2 2 0 012 2v8a2 2 0 01-2 2h-3l-4 4z"
                          />
                        </svg>
                        <span class="font-medium italic">Không có ghi chú</span>
                      </div>
                      <div
                        v-if="order.reservationId"
                        class="flex items-center gap-2 text-gray-700 text-sm"
                      >
                        <svg
                          class="w-4 h-4 text-emerald-600"
                          fill="none"
                          stroke="currentColor"
                          viewBox="0 0 24 24"
                        >
                          <path
                            stroke-linecap="round"
                            stroke-linejoin="round"
                            stroke-width="2"
                            d="M8 7V3m8 4V3m-9 8h10M5 21h14a2 2 0 002-2V7a2 2 0 00-2-2H5a2 2 0 00-2 2v12a2 2 0 002 2z"
                          />
                        </svg>
                        <span>Đặt bàn: {{ order.reservationId.slice(0, 8) }}</span>
                      </div>
                    </div>

                    <div>
                      <div class="flex items-center gap-2 text-gray-700 mb-2">
                        <svg
                          class="w-5 h-5 text-emerald-600"
                          fill="none"
                          stroke="currentColor"
                          viewBox="0 0 24 24"
                        >
                          <path
                            stroke-linecap="round"
                            stroke-linejoin="round"
                            stroke-width="2"
                            d="M12 8c-1.657 0-3 .895-3 2s1.343 2 3 2 3 .895 3 2-1.343 2-3 2m0-8c1.11 0 2.08.402 2.599 1M12 8V7m0 1v8m0 0v1m0-1c-1.11 0-2.08-.402-2.599-1"
                          />
                        </svg>
                        <span class="font-bold text-emerald-600 text-lg">{{
                          formatCurrency(order.total || 0)
                        }}</span>
                      </div>
                      <div
                        v-if="order.appliedVoucherCode"
                        class="flex items-center gap-2 text-green-600 text-sm"
                      >
                        <svg class="w-4 h-4" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                          <path
                            stroke-linecap="round"
                            stroke-linejoin="round"
                            stroke-width="2"
                            d="M7 7h.01M7 3h5c.512 0 1.024.195 1.414.586l7 7a2 2 0 010 2.828l-7 7a2 2 0 01-2.828 0l-7-7A1.99 1.99 0 013 12V7a4 4 0 014-4z"
                          />
                        </svg>
                        <span>Voucher: {{ order.appliedVoucherCode }}</span>
                      </div>
                    </div>

                    <div>
                      <div
                        v-if="order.items && order.items.length > 0"
                        class="flex items-center gap-2 text-gray-700 mb-2"
                      >
                        <svg
                          class="w-5 h-5 text-emerald-600"
                          fill="none"
                          stroke="currentColor"
                          viewBox="0 0 24 24"
                        >
                          <path
                            stroke-linecap="round"
                            stroke-linejoin="round"
                            stroke-width="2"
                            d="M19 11H5m14 0a2 2 0 012 2v6a2 2 0 01-2 2H5a2 2 0 01-2-2v-6a2 2 0 012-2m14 0V9a2 2 0 00-2-2M5 11V9a2 2 0 012-2m0 0V5a2 2 0 012-2h6a2 2 0 012 2v2M7 7h10"
                          />
                        </svg>
                        <span>{{ order.items.length }} món</span>
                      </div>
                      <div
                        v-if="order.tableId"
                        class="flex items-center gap-2 text-gray-700 text-sm"
                      >
                        <svg
                          class="w-4 h-4 text-emerald-600"
                          fill="none"
                          stroke="currentColor"
                          viewBox="0 0 24 24"
                        >
                          <path
                            stroke-linecap="round"
                            stroke-linejoin="round"
                            stroke-width="2"
                            d="M19 21V5a2 2 0 00-2-2H7a2 2 0 00-2 2v16m14 0h2m-2 0h-5m-9 0H3m2 0h5M9 7h1m-1 4h1m4-4h1m-1 4h1m-5 10v-5a1 1 0 011-1h2a1 1 0 011 1v5m-4 0h4"
                          />
                        </svg>
                        <span>Bàn: {{ order.tableId.slice(0, 8) }}</span>
                      </div>
                    </div>
                  </div>
                </div>

                <div class="ml-4 flex flex-col gap-2">
                  <button
                    @click="toggleOrderDetails(order.id)"
                    class="px-4 py-2 bg-emerald-500 text-white rounded-lg hover:bg-emerald-600 transition-colors flex items-center gap-2"
                  >
                    <svg class="w-4 h-4" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                      <path
                        stroke-linecap="round"
                        stroke-linejoin="round"
                        stroke-width="2"
                        d="M15 12a3 3 0 11-6 0 3 3 0 016 0z"
                      />
                      <path
                        stroke-linecap="round"
                        stroke-linejoin="round"
                        stroke-width="2"
                        d="M2.458 12C3.732 7.943 7.523 5 12 5c4.478 0 8.268 2.943 9.542 7-1.274 4.057-5.064 7-9.542 7-4.477 0-8.268-2.943-9.542-7z"
                      />
                    </svg>
                    {{ expandedOrders.includes(order.id) ? "Ẩn chi tiết" : "Chi tiết" }}
                  </button>

                  <!-- Review Button for PAID/COMPLETED orders -->
                  <button
                    v-if="canReviewOrder(order)"
                    @click="openReviewModal(order.id)"
                    class="px-4 py-2 bg-yellow-500 text-white rounded-lg hover:bg-yellow-600 transition-colors flex items-center gap-2"
                  >
                    <svg class="w-4 h-4" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                      <path
                        stroke-linecap="round"
                        stroke-linejoin="round"
                        stroke-width="2"
                        d="M11.049 2.927c.3-.921 1.603-.921 1.902 0l1.519 4.674a1 1 0 00.95.69h4.915c.969 0 1.371 1.24.588 1.81l-3.976 2.888a1 1 0 00-.363 1.118l1.518 4.674c.3.922-.755 1.688-1.538 1.118l-3.976-2.888a1 1 0 00-1.176 0l-3.976 2.888c-.783.57-1.838-.197-1.538-1.118l1.518-4.674a1 1 0 00-.363-1.118l-3.976-2.888c-.784-.57-.38-1.81.588-1.81h4.914a1 1 0 00.951-.69l1.519-4.674z"
                      />
                    </svg>
                    Đánh giá
                  </button>
                </div>
              </div>
            </div>

            <!-- Order Details - Expandable -->
            <div
              v-if="expandedOrders.includes(order.id)"
              class="border-t border-gray-200 bg-gray-50 p-6"
            >
              <!-- Order Items -->
              <div v-if="order.items && order.items.length > 0" class="mb-6">
                <h4 class="font-semibold text-gray-900 mb-3 flex items-center gap-2">
                  <svg
                    class="w-5 h-5 text-emerald-600"
                    fill="none"
                    stroke="currentColor"
                    viewBox="0 0 24 24"
                  >
                    <path
                      stroke-linecap="round"
                      stroke-linejoin="round"
                      stroke-width="2"
                      d="M9 5H7a2 2 0 00-2 2v12a2 2 0 002 2h10a2 2 0 002-2V7a2 2 0 00-2-2h-2M9 5a2 2 0 002 2h2a2 2 0 002-2M9 5a2 2 0 012-2h2a2 2 0 012 2"
                    />
                  </svg>
                  Chi tiết đơn hàng
                </h4>
                <div class="space-y-3">
                  <div
                    v-for="item in order.items"
                    :key="item.id"
                    class="flex justify-between items-center p-3 bg-white rounded-lg border"
                  >
                    <div class="flex-1">
                      <div class="font-medium text-gray-900">{{ item.name }}</div>
                      <div class="text-sm text-gray-500">
                        {{ formatCurrency(item.unitPrice) }} × {{ item.quantity }}
                      </div>
                    </div>
                    <div class="text-right">
                      <div class="font-semibold text-gray-900">
                        {{ formatCurrency(item.lineTotal || item.unitPrice * item.quantity) }}
                      </div>
                    </div>
                  </div>
                </div>
              </div>

              <!-- Order Summary -->
              <div class="bg-white rounded-lg p-4 border">
                <h4 class="font-semibold text-gray-900 mb-3">Tổng kết đơn hàng</h4>
                <div class="space-y-2">
                  <div class="flex justify-between text-gray-600">
                    <span>Tạm tính:</span>
                    <span>{{ formatCurrency(order.subtotal || 0) }}</span>
                  </div>
                  <div
                    v-if="order.discount && order.discount > 0"
                    class="flex justify-between text-green-600"
                  >
                    <span>Giảm giá:</span>
                    <span>-{{ formatCurrency(order.discount) }}</span>
                  </div>
                  <div v-if="order.tax && order.tax > 0" class="flex justify-between text-gray-600">
                    <span>Thuế:</span>
                    <span>{{ formatCurrency(order.tax) }}</span>
                  </div>
                  <hr class="my-2" />
                  <div class="flex justify-between font-bold text-lg">
                    <span>Tổng cộng:</span>
                    <span class="text-emerald-600">{{ formatCurrency(order.total || 0) }}</span>
                  </div>
                </div>
              </div>

              <!-- Order Note -->
              <div v-if="order.note" class="mt-4 p-4 bg-blue-50 rounded-lg border border-blue-200">
                <h4 class="font-semibold text-blue-900 mb-2 flex items-center gap-2">
                  <svg class="w-5 h-5" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                    <path
                      stroke-linecap="round"
                      stroke-linejoin="round"
                      stroke-width="2"
                      d="M7 8h10M7 12h4m1 8l-4-4H5a2 2 0 01-2-2V6a2 2 0 012-2h14a2 2 0 012 2v8a2 2 0 01-2 2h-3l-4 4z"
                    />
                  </svg>
                  Ghi chú
                </h4>
                <p class="text-blue-800">{{ order.note }}</p>
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>

    <!-- Review Modal -->
    <ReviewModal
      :show="showReviewModal"
      :order-id="selectedOrderForReview"
      @close="closeReviewModal"
      @success="onReviewSuccess"
    />

    <!-- Cancel Confirmation Modal -->
    <div
      v-if="showCancelConfirm"
      class="fixed inset-0 bg-black bg-opacity-50 flex items-center justify-center z-50 p-4"
      @click.self="closeCancelModal"
    >
      <div class="bg-white rounded-lg shadow-xl max-w-md w-full p-6">
        <h3 class="text-xl font-bold text-gray-900 mb-4">Xác nhận hủy đơn đặt bàn</h3>
        <p class="text-gray-600 mb-4">Bạn có chắc chắn muốn hủy đơn đặt bàn này không?</p>

        <div class="mb-4">
          <label class="block text-sm font-medium text-gray-700 mb-2"> Lý do hủy (tùy chọn) </label>
          <textarea
            v-model="cancelReason"
            rows="3"
            class="w-full px-3 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-emerald-500 focus:border-transparent"
            placeholder="Nhập lý do hủy đơn..."
          ></textarea>
        </div>

        <div class="flex gap-3 justify-end">
          <button
            @click="closeCancelModal"
            class="px-4 py-2 border border-gray-300 text-gray-700 rounded-lg hover:bg-gray-50 transition-colors"
          >
            Đóng
          </button>
          <button
            @click="confirmCancel"
            :disabled="cancellingId !== null"
            class="px-4 py-2 bg-red-500 text-white rounded-lg hover:bg-red-600 transition-colors disabled:opacity-50 disabled:cursor-not-allowed flex items-center gap-2"
          >
            <svg v-if="cancellingId" class="animate-spin h-4 w-4" fill="none" viewBox="0 0 24 24">
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
            Xác nhận hủy
          </button>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, watch } from "vue";
import { listMyReservations, cancelMyReservation } from "@/api/reservations.service";
import { detailOrder, listOrders } from "@/api/orders.service";
import { toast } from "vue3-toastify";
import ReviewModal from "@/components/ReviewModal.vue";

type ReservationStatus = "PENDING" | "CONFIRMED" | "SEATED" | "COMPLETED" | "CANCELLED" | "NO_SHOW";

interface Table {
  id: string;
  name: string;
  area?: {
    name: string;
  };
}

interface Reservation {
  id: string;
  startTime: string;
  endTime: string;
  partySize: number;
  status: ReservationStatus;
  note?: string;
  tables?: Table[];
  createdAt: string;
}

interface OrderItem {
  id: string;
  name: string;
  quantity: number;
  unitPrice: number;
  lineTotal?: number;
}

interface Order {
  id: string;
  userId: string;
  reservationId?: string;
  tableId?: string;
  status: string;
  subtotal?: number;
  discount?: number;
  tax?: number;
  total?: number;
  note?: string;
  appliedVoucherCode?: string;
  createdAt: string;
  items?: OrderItem[];
}

const activeTab = ref<"reservations" | "orders">("reservations");
const loadingReservations = ref(false);
const loadingOrders = ref(false);
const reservations = ref<Reservation[]>([]);
const orders = ref<Order[]>([]);
const expandedOrders = ref<string[]>([]);
const cancellingId = ref<string | null>(null);
const showCancelConfirm = ref(false);
const selectedReservation = ref<Reservation | null>(null);
const cancelReason = ref("");

// Review modal
const showReviewModal = ref(false);
const selectedOrderForReview = ref<string>("");

// Fetch reservations
const fetchReservations = async () => {
  loadingReservations.value = true;
  try {
    const { data } = await listMyReservations();
    reservations.value = data;
  } catch (error: any) {
    console.error("Error fetching reservations:", error);
    toast.error(error?.response?.data?.message || "Không thể tải lịch sử đặt bàn");
  } finally {
    loadingReservations.value = false;
  }
};

// Fetch orders
const fetchOrders = async () => {
  loadingOrders.value = true;
  try {
    const { data } = await listOrders();
    orders.value = data;
  } catch (error: any) {
    console.error("Error fetching orders:", error);
    toast.error(error?.response?.data?.message || "Không thể tải lịch sử đơn hàng");
  } finally {
    loadingOrders.value = false;
  }
};

// Status helpers
const getStatusClass = (status: ReservationStatus) => {
  const classes: Record<ReservationStatus, string> = {
    PENDING: "bg-yellow-100 text-yellow-800",
    CONFIRMED: "bg-blue-100 text-blue-800",
    SEATED: "bg-purple-100 text-purple-800",
    COMPLETED: "bg-green-100 text-green-800",
    CANCELLED: "bg-red-100 text-red-800",
    NO_SHOW: "bg-gray-100 text-gray-800",
  };
  return classes[status] || "bg-gray-100 text-gray-800";
};

const getStatusText = (status: ReservationStatus) => {
  const texts: Record<ReservationStatus, string> = {
    PENDING: "Chờ xác nhận",
    CONFIRMED: "Đã xác nhận",
    SEATED: "Đã nhận bàn",
    COMPLETED: "Hoàn thành",
    CANCELLED: "Đã hủy",
    NO_SHOW: "Không đến",
  };
  return texts[status] || status;
};

// Order status helpers
const getOrderStatusClass = (status: string) => {
  const classes: Record<string, string> = {
    OPEN: "bg-blue-100 text-blue-800",
    PENDING: "bg-yellow-100 text-yellow-800",
    CONFIRMED: "bg-blue-100 text-blue-800",
    PREPARING: "bg-purple-100 text-purple-800",
    READY: "bg-orange-100 text-orange-800",
    COMPLETED: "bg-green-100 text-green-800",
    CANCELLED: "bg-red-100 text-red-800",
    PAID: "bg-green-100 text-green-800",
  };
  return classes[status] || "bg-gray-100 text-gray-800";
};

const getOrderStatusText = (status: string) => {
  const texts: Record<string, string> = {
    OPEN: "Đang mở",
    PENDING: "Chờ xử lý",
    CONFIRMED: "Đã xác nhận",
    PREPARING: "Đang chuẩn bị",
    READY: "Sẵn sàng",
    COMPLETED: "Hoàn thành",
    CANCELLED: "Đã hủy",
    PAID: "Đã thanh toán",
  };
  return texts[status] || status;
};

const canCancelReservation = (reservation: Reservation) => {
  return reservation.status === "PENDING" || reservation.status === "CONFIRMED";
};

// Format helpers
const formatDate = (dateString: string) => {
  const date = new Date(dateString);
  return date.toLocaleDateString("vi-VN", {
    weekday: "long",
    year: "numeric",
    month: "long",
    day: "numeric",
  });
};

const formatTime = (dateString: string) => {
  const date = new Date(dateString);
  return date.toLocaleTimeString("vi-VN", {
    hour: "2-digit",
    minute: "2-digit",
  });
};

// Format helpers for orders
const formatOrderDate = (dateString: string) => {
  const date = new Date(dateString);
  return date.toLocaleDateString("vi-VN", {
    year: "numeric",
    month: "2-digit",
    day: "2-digit",
    hour: "2-digit",
    minute: "2-digit",
  });
};

const formatCurrency = (amount: number) => {
  return new Intl.NumberFormat("vi-VN", {
    style: "currency",
    currency: "VND",
  }).format(amount);
};

// View order details
const toggleOrderDetails = (orderId: string) => {
  const index = expandedOrders.value.indexOf(orderId);
  if (index > -1) {
    expandedOrders.value.splice(index, 1);
  } else {
    expandedOrders.value.push(orderId);
  }
};

// Cancel reservation
const showCancelModal = (reservation: Reservation) => {
  selectedReservation.value = reservation;
  showCancelConfirm.value = true;
  cancelReason.value = "";
};

const closeCancelModal = () => {
  showCancelConfirm.value = false;
  selectedReservation.value = null;
  cancelReason.value = "";
};

const confirmCancel = async () => {
  if (!selectedReservation.value) return;

  cancellingId.value = selectedReservation.value.id;
  try {
    await cancelMyReservation(selectedReservation.value.id, cancelReason.value || undefined);
    toast.success("Hủy đơn đặt bàn thành công");
    closeCancelModal();
    // Refresh list
    await fetchReservations();
  } catch (error: any) {
    console.error("Error cancelling reservation:", error);
    toast.error(error?.response?.data?.message || "Không thể hủy đơn đặt bàn");
  } finally {
    cancellingId.value = null;
  }
};
function getTableNames(reservation: any) {
  if (reservation.tables && Array.isArray(reservation.tables) && reservation.tables.length > 0) {
    return reservation.tables.map((t: any) => t.code || t.name || t.id.slice(0, 8)).join(", ");
  }
  return "Chưa gán";
}

// Review functions
const openReviewModal = (orderId: string) => {
  selectedOrderForReview.value = orderId;
  showReviewModal.value = true;
};

const closeReviewModal = () => {
  showReviewModal.value = false;
  selectedOrderForReview.value = "";
};

const onReviewSuccess = () => {
  // Optionally refresh orders or show success message
};

// Check if order can be reviewed (only PAID/COMPLETED orders)
const canReviewOrder = (order: Order) => {
  return order.status === "PAID" || order.status === "COMPLETED";
};
onMounted(() => {
  fetchReservations();
});

// Watch for tab changes to load orders when needed
watch(activeTab, (newTab) => {
  if (newTab === "orders" && orders.value.length === 0) {
    fetchOrders();
  }
});
</script>
