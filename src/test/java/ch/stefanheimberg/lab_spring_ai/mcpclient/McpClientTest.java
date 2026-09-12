package ch.stefanheimberg.lab_spring_ai.mcpclient;

import io.modelcontextprotocol.client.McpSyncClient;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.ai.mcp.SyncMcpToolCallbackProvider;
import org.springframework.ai.tool.ToolCallback;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest
public class McpClientTest {

    @Autowired
    private List<McpSyncClient> mcpSyncClients;

    @Autowired
    private SyncMcpToolCallbackProvider toolCallbackProvider;

    @BeforeEach
    public void setUp() {
        final ToolCallback[] toolCallbacks = toolCallbackProvider.getToolCallbacks();
        assertNotNull(toolCallbacks);
        assertEquals(26, toolCallbacks.length);
    }

    @Test
    void testMcpSyncClient_SSE() {
        final Optional<McpSyncClient> clientOpt = mcpSyncClients.stream()
                .filter(client -> "local_mcp_sse".equals(client.getClientInfo().title()))
                .findFirst();
        assertTrue(clientOpt.isPresent());

        final McpSyncClient client = clientOpt.get();

        assertEquals(7, client.listResources().resources().size());
        assertEquals(2, client.listResourceTemplates().resourceTemplates().size());
        assertEquals(13, client.listTools().tools().size());
        assertEquals(4, client.listPrompts().prompts().size());

        client.listTools().tools().forEach(tool -> {
            System.out.println("Tool: " + tool.name());
            System.out.println("- Description: " + tool.description());
            System.out.println("- Schema: " + tool.inputSchema());
        });
    }

    @Test
    void testMcpSyncClient_Streamable() {
        final Optional<McpSyncClient> clientOpt = mcpSyncClients.stream()
                .filter(client -> "local_mcp_streamable".equals(client.getClientInfo().title()))
                .findFirst();
        assertTrue(clientOpt.isPresent());

        final McpSyncClient client = clientOpt.get();

        assertEquals(7, client.listResources().resources().size());
        assertEquals(2, client.listResourceTemplates().resourceTemplates().size());
        assertEquals(13, client.listTools().tools().size());
        assertEquals(4, client.listPrompts().prompts().size());

        client.listTools().tools().forEach(tool -> {
            System.out.println("Tool: " + tool.name());
            System.out.println("- Description: " + tool.description());
            System.out.println("- Schema: " + tool.inputSchema());
        });
    }
}
