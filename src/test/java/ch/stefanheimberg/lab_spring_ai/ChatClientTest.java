package ch.stefanheimberg.lab_spring_ai;

import org.junit.jupiter.api.Test;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.messages.AssistantMessage;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.model.Generation;
import org.springframework.ai.openai.OpenAiChatOptions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;

@SpringBootTest
public class ChatClientTest {

    @Autowired
    private ChatClient.Builder chatClientBuilder;

    @Test
    void simpleCall() {
        final String uuid = UUID.randomUUID().toString();

        final ChatClient chatClient = chatClientBuilder.build();
        final String content = chatClient.prompt()
                .options(OpenAiChatOptions.builder()
                        .model("openai/gpt-oss-20b"))
                .system("Antworte mir bitte nur mit dem Text BANANE_%s".formatted(uuid))
                .user("Hallo")
                .call()
                .content();
        assertEquals(content, "BANANE_%s".formatted(uuid));
    }

    @Test
    void simpleCall_chatResponse() {
        final String uuid = UUID.randomUUID().toString();

        final ChatClient chatClient = chatClientBuilder.build();
        final ChatResponse chatResponse = chatClient.prompt()
                .options(OpenAiChatOptions.builder()
                        .model("openai/gpt-oss-20b"))
                .system("Antworte mir bitte nur mit dem Text BANANE_%s".formatted(uuid))
                .user("Hallo")
                .call()
                .chatResponse();

        final List<Generation> results = chatResponse.getResults();
        assertEquals(results.size(), 1);

        final Generation generation = results.get(0);
        final AssistantMessage output = generation.getOutput();

        assertEquals(output.getToolCalls().size(), 0);
        assertEquals(generation.getOutput().getText(), "BANANE_%s".formatted(uuid));
    }

}
