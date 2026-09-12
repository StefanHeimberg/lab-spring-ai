package ch.stefanheimberg.lab_spring_ai.mcpclient;

import io.modelcontextprotocol.client.McpSyncClient;
import org.junit.jupiter.api.Test;
import org.springframework.ai.mcp.SyncMcpToolCallbackProvider;
import org.springframework.ai.tool.ToolCallback;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest
@Import({MyMcpToolNamePrefixGenerator.class, MyMcpClientHandlers.class})
public class McpClientTest {

    @Autowired
    private List<McpSyncClient> mcpSyncClients;

    @Autowired
    private SyncMcpToolCallbackProvider toolCallbackProvider;

    @Test
    public void toolPrefixGenerator() {
        final ToolCallback[] toolCallbacks = toolCallbackProvider.getToolCallbacks();
        assertNotNull(toolCallbacks);
        assertEquals(27, toolCallbacks.length);

        final Map<String, ToolCallback> toolCallbacksByName = Arrays.stream(toolCallbacks)
                .collect(Collectors.toMap(
                        (toolCallback) -> toolCallback.getToolDefinition().name(),
                        (toolCallback) -> toolCallback
                ));

        toolCallbacksByName.keySet().forEach(name -> System.out.println("Tool: " + name));

        assertEquals(13, toolCallbacksByName.keySet().stream()
                .filter(name -> name.startsWith("local_mcp_sse")).count());
        assertTrue(toolCallbacksByName.containsKey("local_mcp_sse_echo"));
        assertTrue(toolCallbacksByName.containsKey("local_mcp_sse_trigger-long-running-operation"));

        assertEquals(14, toolCallbacksByName.keySet().stream()
                .filter(name -> name.startsWith("local_mcp_streamable")).count());
        assertTrue(toolCallbacksByName.containsKey("local_mcp_streamable_echo"));
        assertTrue(toolCallbacksByName.containsKey("local_mcp_streamable_trigger-long-running-operation"));

        // Aufgrund @McpSampling in MyMcpClientHandlers
        assertFalse(toolCallbacksByName.containsKey("local_mcp_sse_trigger-sampling-request"));
        assertTrue(toolCallbacksByName.containsKey("local_mcp_streamable_trigger-sampling-request"));
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
        assertEquals(14, client.listTools().tools().size());
        assertEquals(4, client.listPrompts().prompts().size());

        client.listTools().tools().forEach(tool -> {
            System.out.println("Tool: " + tool.name());
            System.out.println("- Description: " + tool.description());
            System.out.println("- Schema: " + tool.inputSchema());
        });
    }
}
