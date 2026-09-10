package ch.stefanheimberg.lab_spring_ai;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.SimpleLoggerAdvisor;
import org.springframework.ai.chat.messages.AssistantMessage;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.model.Generation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;

@SpringBootTest
public class ChatClientAPITest {

    @Autowired
    private ChatClient.Builder chatClientBuilder;

    @BeforeEach
    public void setup() {
        chatClientBuilder
                .defaultSystem("Du bist ein hilfsbereiter Assistent")
                .defaultAdvisors(new SimpleLoggerAdvisor());
    }

    @Test
    void simpleCall() {
        final String uuid = UUID.randomUUID().toString();

        final String content = chatClientBuilder.build()
                .prompt()
                .user("Antworte mir bitte nur mit dem Text BANANE_%s".formatted(uuid))
                .call()
                .content();
        assertEquals(content, "BANANE_%s".formatted(uuid));
    }

    @Test
    void simpleCall_chatResponse() {
        final String uuid = UUID.randomUUID().toString();

        final ChatResponse chatResponse = chatClientBuilder.build()
                .prompt()
                .user("Antworte mir bitte nur mit dem Text BANANE_%s".formatted(uuid))
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
