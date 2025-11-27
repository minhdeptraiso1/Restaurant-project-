<script setup lang="ts">
import { ref, nextTick, computed } from "vue";
import { useAuthStore } from "@/stores/auth";
import { useRouter } from "vue-router";
import { sendChatMessage } from "@/api/chat.service";
import type { ChatMessage } from "@/types";
import { toast } from "vue3-toastify";
import { useUiStore } from "@/stores/ui";

const auth = useAuthStore();
const router = useRouter();
const ui = useUiStore();

// State
const isOpen = ref(false);
const messages = ref<ChatMessage[]>([]);
const inputMessage = ref("");
const isLoading = ref(false);
const sessionId = ref<string>(`session-${Date.now()}`); // Generate simple sessionId
const chatContainer = ref<HTMLElement | null>(null);
const lastMessageTime = ref<number>(0);
const MESSAGE_COOLDOWN = 3000; // 3 seconds between messages

// Computed
const isLoggedIn = computed(() => !!auth.token && !!auth.user);

// Scroll to bottom
const scrollToBottom = () => {
  nextTick(() => {
    if (chatContainer.value) {
      chatContainer.value.scrollTop = chatContainer.value.scrollHeight;
    }
  });
};

// Toggle chat window
const toggleChat = async () => {
  if (!isLoggedIn.value) {
    toast.info("Vui lòng đăng nhập để sử dụng chatbot");
    router.push("/auth/login");
    return;
  }
  isOpen.value = !isOpen.value;
  if (isOpen.value && messages.value.length === 0) {
    loadInitialMessages();
  }
};

// Load initial welcome message
const loadInitialMessages = () => {
  messages.value = [
    {
      role: "assistant",
      content:
        "Xin chào! Tôi là trợ lý ảo của Hoa Ban Restaurant. Tôi có thể giúp bạn:\n\n• Tư vấn món ăn và combo\n• Đặt bàn\n• Tra cứu đơn hàng\n\nBạn cần tôi giúp gì?",
      at: new Date().toISOString(),
    },
  ];
  scrollToBottom();
};

// Send message
const sendMessage = async () => {
  if (!inputMessage.value.trim() || isLoading.value) return;

  // Check rate limiting
  const now = Date.now();
  if (now - lastMessageTime.value < MESSAGE_COOLDOWN) {
    const remainingTime = Math.ceil((MESSAGE_COOLDOWN - (now - lastMessageTime.value)) / 1000);
    toast.warning(`Vui lòng đợi ${remainingTime} giây trước khi gửi tin nhắn tiếp theo`);
    return;
  }

  const userMessage: ChatMessage = {
    role: "user",
    content: inputMessage.value,
    at: new Date().toISOString(),
  };

  messages.value.push(userMessage);
  const messageText = inputMessage.value;
  inputMessage.value = "";
  scrollToBottom();

  try {
    isLoading.value = true;
    lastMessageTime.value = Date.now();
    const response = await sendChatMessage(sessionId.value, messageText);

    const assistantMessage: ChatMessage = {
      role: "assistant",
      content: response.data.reply,
      at: new Date().toISOString(),
    };

    messages.value.push(assistantMessage);
    scrollToBottom();
  } catch (error: any) {
    console.error("Chat error:", error);
    toast.error("Có lỗi xảy ra khi gửi tin nhắn");
    messages.value.push({
      role: "assistant",
      content: "❌ Xin lỗi, tôi gặp sự cố. Vui lòng thử lại sau.",
      at: new Date().toISOString(),
    });
  } finally {
    isLoading.value = false;
  }
};

// Handle Enter key
const handleKeyPress = (event: KeyboardEvent) => {
  if (event.key === "Enter" && !event.shiftKey) {
    event.preventDefault();
    sendMessage();
  }
};

// Reset conversation
const resetConversation = () => {
  if (!confirm("Bạn có chắc muốn xóa hội thoại này?")) return;
  messages.value = [];
  sessionId.value = `session-${Date.now()}`; // Generate new sessionId
  loadInitialMessages();
  toast.success("Đã reset hội thoại");
};
</script>

<template>
  <!-- Chat Button -->
  <button
    @click="toggleChat"
    class="fixed bottom-6 right-6 z-[9998] w-16 h-16 bg-gradient-to-r from-[#9f0909] to-[#7a1010] text-white rounded-full shadow-2xl hover:scale-110 transition-transform duration-300 flex items-center justify-center group"
  >
    <svg
      v-if="!isOpen"
      class="w-8 h-8 group-hover:scale-110 transition-transform"
      fill="none"
      stroke="currentColor"
      viewBox="0 0 24 24"
    >
      <path
        stroke-linecap="round"
        stroke-linejoin="round"
        stroke-width="2"
        d="M8 10h.01M12 10h.01M16 10h.01M9 16H5a2 2 0 01-2-2V6a2 2 0 012-2h14a2 2 0 012 2v8a2 2 0 01-2 2h-5l-5 5v-5z"
      />
    </svg>
    <svg v-else class="w-8 h-8" fill="none" stroke="currentColor" viewBox="0 0 24 24">
      <path
        stroke-linecap="round"
        stroke-linejoin="round"
        stroke-width="2"
        d="M6 18L18 6M6 6l12 12"
      />
    </svg>
  </button>

  <!-- Chat Window -->
  <transition name="slide-up">
    <div
      v-if="isOpen"
      class="fixed bottom-24 right-6 z-[9999] w-96 h-[600px] bg-white rounded-2xl shadow-2xl flex flex-col overflow-hidden border border-gray-200"
    >
      <!-- Header -->
      <div
        class="bg-gradient-to-r from-[#9f0909] to-[#7a1010] text-white p-4 flex items-center justify-between"
      >
        <div class="flex items-center gap-3">
          <div class="w-10 h-10 bg-white/20 rounded-full flex items-center justify-center">
            <svg class="w-6 h-6" fill="none" stroke="currentColor" viewBox="0 0 24 24">
              <path
                stroke-linecap="round"
                stroke-linejoin="round"
                stroke-width="2"
                d="M9.75 17L9 20l-1 1h8l-1-1-.75-3M3 13h18M5 17h14a2 2 0 002-2V5a2 2 0 00-2-2H5a2 2 0 00-2 2v10a2 2 0 002 2z"
              />
            </svg>
          </div>
          <div>
            <h3 class="font-bold text-lg">Hoa Ban Assistant</h3>
            <p class="text-xs text-white/80">Trợ lý ảo</p>
          </div>
        </div>

        <div class="flex items-center gap-2">
          <!-- Reset Button -->
          <button
            @click="resetConversation"
            class="p-2 hover:bg-white/20 rounded-lg transition-colors"
            title="Reset hội thoại"
          >
            <svg class="w-5 h-5" fill="none" stroke="currentColor" viewBox="0 0 24 24">
              <path
                stroke-linecap="round"
                stroke-linejoin="round"
                stroke-width="2"
                d="M4 4v5h.582m15.356 2A8.001 8.001 0 004.582 9m0 0H9m11 11v-5h-.581m0 0a8.003 8.003 0 01-15.357-2m15.357 2H15"
              />
            </svg>
          </button>
        </div>
      </div>

      <!-- Messages Area -->
      <div class="flex-1 flex overflow-hidden">
        <div ref="chatContainer" class="flex-1 overflow-y-auto p-4 space-y-4 bg-gray-50">
          <div
            v-for="(message, index) in messages"
            :key="index"
            :class="['flex', message.role === 'user' ? 'justify-end' : 'justify-start']"
          >
            <div
              :class="[
                'max-w-[80%] rounded-2xl px-4 py-2 break-words',
                message.role === 'user'
                  ? 'bg-[#9f0909] text-white'
                  : 'bg-white text-gray-900 shadow-sm border border-gray-200',
              ]"
            >
              <div class="whitespace-pre-wrap text-sm">{{ message.content }}</div>
              <div class="text-xs mt-1 opacity-70">
                {{
                  new Date(message.at).toLocaleTimeString("vi-VN", {
                    hour: "2-digit",
                    minute: "2-digit",
                  })
                }}
              </div>
            </div>
          </div>

          <!-- Loading indicator -->
          <div v-if="isLoading" class="flex justify-start">
            <div
              class="bg-white text-gray-900 rounded-2xl px-4 py-3 shadow-sm border border-gray-200"
            >
              <div class="flex gap-1">
                <div class="w-2 h-2 bg-gray-400 rounded-full animate-bounce"></div>
                <div
                  class="w-2 h-2 bg-gray-400 rounded-full animate-bounce"
                  style="animation-delay: 0.1s"
                ></div>
                <div
                  class="w-2 h-2 bg-gray-400 rounded-full animate-bounce"
                  style="animation-delay: 0.2s"
                ></div>
              </div>
            </div>
          </div>
        </div>
      </div>

      <!-- Input Area -->
      <div class="border-t border-gray-200 p-4 bg-white">
        <div class="flex gap-2">
          <input
            v-model="inputMessage"
            @keypress="handleKeyPress"
            type="text"
            placeholder="Nhập tin nhắn..."
            class="flex-1 px-4 py-3 border border-gray-300 rounded-xl focus:outline-none focus:ring-2 focus:ring-[#9f0909] focus:border-transparent"
          />
          <button
            @click="sendMessage"
            :disabled="!inputMessage.trim() || isLoading"
            class="px-6 py-3 bg-[#9f0909] text-white rounded-xl hover:bg-[#800808] transition-colors disabled:opacity-50 disabled:cursor-not-allowed"
          >
            <svg class="w-5 h-5" fill="none" stroke="currentColor" viewBox="0 0 24 24">
              <path
                stroke-linecap="round"
                stroke-linejoin="round"
                stroke-width="2"
                d="M12 19l9 2-9-18-9 18 9-2zm0 0v-8"
              />
            </svg>
          </button>
        </div>
      </div>
    </div>
  </transition>
</template>

<style scoped>
.slide-up-enter-active,
.slide-up-leave-active {
  transition: all 0.3s ease;
}

.slide-up-enter-from {
  opacity: 0;
  transform: translateY(20px);
}

.slide-up-leave-to {
  opacity: 0;
  transform: translateY(20px);
}

.slide-left-enter-active,
.slide-left-leave-active {
  transition: all 0.3s ease;
}

.slide-left-enter-from {
  transform: translateX(-100%);
}

.slide-left-leave-to {
  transform: translateX(-100%);
}

.line-clamp-1 {
  display: -webkit-box;
  -webkit-line-clamp: 1;
  line-clamp: 1;
  -webkit-box-orient: vertical;
  overflow: hidden;
}

/* Custom scrollbar */
::-webkit-scrollbar {
  width: 6px;
}

::-webkit-scrollbar-track {
  background: #f1f1f1;
}

::-webkit-scrollbar-thumb {
  background: #9f0909;
  border-radius: 3px;
}

::-webkit-scrollbar-thumb:hover {
  background: #7a1010;
}
</style>
