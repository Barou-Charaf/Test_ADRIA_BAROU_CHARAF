package org.example.chatbotservice.service;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.mcp.SyncMcpToolCallbackProvider;
import org.springframework.stereotype.Service;

@Service
public class ChatService {

    private final ChatClient chatClient;
    private final SyncMcpToolCallbackProvider mcpToolCallbackProvider;

    public ChatService(
            ChatClient.Builder chatClientBuilder,
            SyncMcpToolCallbackProvider mcpToolCallbackProvider) {
        this.mcpToolCallbackProvider = mcpToolCallbackProvider;
        this.chatClient = chatClientBuilder
                .defaultSystem("""
                        You are an ADRIA banking assistant. Use the available MCP tools for account,
                        balance, transaction and transfer requests. Never claim that a banking operation
                        succeeded unless its tool call succeeded. Preserve meaningful validation errors.
                        """)
                .build();
    }

    public String chat(String message) {
        return chatClient.prompt()
                .user(message)
                .tools(mcpToolCallbackProvider)
                .call()
                .content();
    }
}
