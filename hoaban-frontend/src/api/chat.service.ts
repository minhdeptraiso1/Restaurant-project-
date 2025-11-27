import http from "./http";
import type { ChatResponseData } from "@/types";

/**
 * Send a message to chatbot
 */
export async function sendChatMessage(sessionId: string, message: string) {
  console.log("🚀 Sending chat message:", { sessionId, message });
  try {
    const response = await http.post<ChatResponseData>("/chat", { sessionId, message });
    console.log("✅ Chat response:", response.data);
    return response;
  } catch (error: any) {
    console.error("❌ Chat error:", {
      status: error.response?.status,
      data: error.response?.data,
      message: error.message,
    });
    throw error;
  }
}
