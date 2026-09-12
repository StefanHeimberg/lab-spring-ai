package ch.stefanheimberg.lab_spring_ai.mcpclient;

import io.modelcontextprotocol.spec.McpSchema;
import org.springframework.ai.mcp.annotation.McpLogging;
import org.springframework.ai.mcp.annotation.McpProgress;
import org.springframework.ai.mcp.annotation.McpSampling;
import org.springframework.ai.mcp.annotation.McpToolListChanged;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class MyMcpClientHandlers {

    @McpLogging(clients = "local_mcp_streamable")
    public void handleLoggingMessage(final McpSchema.LoggingMessageNotification notification) {
        System.out.println("Received log: " + notification.level() +
                " - " + notification.data());
    }

    @McpSampling(clients = "local_mcp_streamable")
    public McpSchema.CreateMessageResult handleSamplingRequest(final McpSchema.CreateMessageRequest request) {
        // Process the request and generate a response
        String response = "generateLLMResponse(request)";

        return McpSchema.CreateMessageResult.builder(McpSchema.Role.ASSISTANT, response, "gpt-4")
                .build();
    }

    @McpProgress(clients = "local_mcp_streamable")
    public void handleProgressNotification(final McpSchema.ProgressNotification notification) {
        double percentage = notification.progress() * 100;
        System.out.printf("Progress: %.2f%% - %s%n", percentage, notification.message());
    }

    @McpToolListChanged(clients = "local_mcp_streamable")
    public void handleToolListChanged(List<McpSchema.Tool> updatedTools) {
        System.out.println("Tool list updated: " + updatedTools.size() + " tools available");
        // Update local tool registry
    }

}
