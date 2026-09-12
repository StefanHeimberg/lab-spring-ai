package ch.stefanheimberg.lab_spring_ai.mcpclient;

import io.modelcontextprotocol.spec.McpSchema;
import org.jspecify.annotations.NonNull;
import org.springframework.ai.mcp.McpConnectionInfo;
import org.springframework.ai.mcp.McpToolNamePrefixGenerator;
import org.springframework.stereotype.Component;

@Component
public class MyMcpToolNamePrefixGenerator implements McpToolNamePrefixGenerator {

    @Override
    public @NonNull String prefixedToolName(final McpConnectionInfo connectionInfo, final McpSchema.Tool tool) {
        final String prefix = connectionInfo.clientInfo().title();
        return "%s_%s".formatted(prefix, tool.name());
    }

}
