package ch.stefanheimberg.lab_spring_ai.chatclient;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.ChatClientResponse;
import org.springframework.ai.chat.client.advisor.MessageChatMemoryAdvisor;
import org.springframework.ai.chat.client.advisor.SimpleLoggerAdvisor;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.chat.memory.ChatMemoryRepository;
import org.springframework.ai.chat.memory.InMemoryChatMemoryRepository;
import org.springframework.ai.chat.memory.MessageWindowChatMemory;
import org.springframework.ai.chat.messages.AssistantMessage;
import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.chat.messages.MessageType;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.model.Generation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest
public class ChatClientAPITest {

    @Autowired
    private ChatClient.Builder chatClientBuilder;

    @BeforeEach
    public void setup() {
        chatClientBuilder.defaultAdvisors(new SimpleLoggerAdvisor());
    }

    @Test
    void simpleCall() {
        final String uuid = UUID.randomUUID().toString();

        final ChatClient chatClient = chatClientBuilder.build();
        final String content = chatClient
                .prompt()
                .system("Du bist ein hilfsbereiter Assistent")
                .user("Antworte mir bitte nur mit dem Text BANANE_%s".formatted(uuid))
                .call()
                .content();
        assertEquals("BANANE_%s".formatted(uuid), content);
    }

    @Test
    void chatResponse() {
        final String uuid = UUID.randomUUID().toString();

        final ChatClient chatClient = chatClientBuilder.build();
        final ChatResponse chatResponse = chatClient
                .prompt()
                .system("Du bist ein hilfsbereiter Assistent")
                .user("Antworte mir bitte nur mit dem Text BANANE_%s".formatted(uuid))
                .call()
                .chatResponse();

        final List<Generation> results = chatResponse.getResults();
        assertEquals(1, results.size());

        final Generation generation = results.get(0);
        final AssistantMessage output = generation.getOutput();

        assertEquals(0, output.getToolCalls().size());
        assertEquals("BANANE_%s".formatted(uuid), generation.getOutput().getText());
    }

    @Test
    void promptTemplate() {
        final String uuid = UUID.randomUUID().toString();

        final ChatClient chatClient = chatClientBuilder.build();
        final String content = chatClient
                .prompt()
                .system("Du bist ein hilfsbereiter Assistent")
                .user(u -> u.text("Antworte mir bitte nur mit dem Text BANANE_{bananeUuid}")
                        .param("bananeUuid", uuid))
                .call()
                .content();
        assertEquals("BANANE_%s".formatted(uuid), content);
    }

    @Test
    void messageMetadata() {
        final String uuid = UUID.randomUUID().toString();

        final ChatClient chatClient = chatClientBuilder.build();
        final String content = chatClient
                .prompt()
                .system(s -> s.text("Du bist ein hilfsbereiter Assistent")
                        .metadata("type", "system-message")
                        .metadata("class", getClass().getSimpleName())
                        .metadata("method", "mssageMetadata")
                        .metadata("bananaId", uuid))
                .user(u -> u.text("Antworte mir bitte nur mit dem Text BANANE_{bananeUuid}")
                        .param("bananeUuid", uuid)
                        .metadata("type", "system-message")
                        .metadata("class", getClass().getSimpleName())
                        .metadata("method", "mssageMetadata")
                        .metadata("bananaId", uuid))
                .call()
                .content();
        assertEquals("BANANE_%s".formatted(uuid), content);
    }

    @Test
    void mutate() {
        final String uuid = UUID.randomUUID().toString();

        final ChatClient chatClient = chatClientBuilder.build();

        final ChatClient.ChatClientRequestSpec bananaRequestSpec = chatClient
                .prompt()
                .system("Du bist ein hilfsbereiter Assistent")
                .user(u -> u.text("Antworte mir bitte nur mit dem Text BANANE_{bananeUuid}")
                        .param("bananeUuid", uuid));

        final ChatClient.ChatClientRequestSpec appleRequestSpec = bananaRequestSpec.mutate().build().prompt()
                .user(u -> u.text("Antworte mir bitte nur mit dem Text APPLE_{appleUuid}")
                        .param("appleUuid", uuid));

        final String bananaContent = bananaRequestSpec.call().content();
        assertEquals("BANANE_%s".formatted(uuid), bananaContent);

        final String appleContent = appleRequestSpec.call().content();
        assertEquals("APPLE_%s".formatted(uuid), appleContent);
    }

    @Test
    void advise() {
        final String uuid = UUID.randomUUID().toString();

        final ChatClient chatClient = chatClientBuilder.build();

        // INFO: SimpleLoggerAdvisor wird schon per default auf chatClientBuilder gesetzt.

        final ChatClientResponse chatClientResponse = chatClient
                .prompt()
                .advisors(a -> a.param("bananaAdvised", true))
                .system("Du bist ein hilfsbereiter Assistent")
                .user(u -> u.text("Antworte mir bitte nur mit dem Text BANANE_{bananeUuid}")
                        .param("bananeUuid", uuid))
                .call()
                .chatClientResponse();
        assertTrue(chatClientResponse.context().containsKey("bananaAdvised"));

        final ChatResponse chatResponse = chatClientResponse.chatResponse();
        assertNotNull(chatResponse);
        assertEquals(1, chatResponse.getResults().size());

        final Generation firstGeneration = chatResponse.getResults().get(0);

        final String content = firstGeneration.getOutput().getText();
        assertEquals("BANANE_%s".formatted(uuid), content);
    }

    @Test
    void chatMemory() {
        final String uuid = UUID.randomUUID().toString();

        final String conversationId = UUID.randomUUID().toString();

        final ChatMemoryRepository chatMemoryRepository = new InMemoryChatMemoryRepository();
        chatMemoryRepository.deleteByConversationId(conversationId);

        final ChatMemory chatMemory = MessageWindowChatMemory.builder()
                .maxMessages(50)
                .chatMemoryRepository(chatMemoryRepository)
                .build();

        final ChatClient chatClient = chatClientBuilder.clone()
                .defaultSystem("Du bist ein hilfsbereiter Assistent")
                .defaultAdvisors(MessageChatMemoryAdvisor.builder(chatMemory).build())
                .build();

        assertEquals(0, chatMemoryRepository.findConversationIds().size());

        chatClient
            .prompt()
            .advisors(a -> a.param(ChatMemory.CONVERSATION_ID, conversationId))
            .user(u -> u.text("Antworte mir bitte nur mit dem Text BANANE_{bananeUuid}")
                    .param("bananeUuid", uuid))
            .call()
            .content();

        assertEquals(1, chatMemoryRepository.findConversationIds().size());
        assertEquals(conversationId, chatMemoryRepository.findConversationIds().get(0));

        chatClient
                .prompt()
                .advisors(a -> a.param(ChatMemory.CONVERSATION_ID, conversationId))
                .user(u -> u.text("Antworte mir bitte nur mit dem Text APPLE_{appleUuid}")
                        .param("appleUuid", uuid))
                .call()
                .content();

        assertEquals(1, chatMemoryRepository.findConversationIds().size());
        assertEquals(conversationId, chatMemoryRepository.findConversationIds().get(0));

        final List<Message> messages = chatMemory.get(conversationId);
        assertEquals(4, messages.size());

        final Message message1 = messages.get(0);
        assertEquals(MessageType.USER, message1.getMessageType());
        assertEquals("Antworte mir bitte nur mit dem Text BANANE_%s".formatted(uuid), message1.getText());

        final Message message2 = messages.get(1);
        assertEquals(MessageType.ASSISTANT, message2.getMessageType());
        assertEquals("BANANE_%s".formatted(uuid), message2.getText());

        final Message message3 = messages.get(2);
        assertEquals(MessageType.USER, message3.getMessageType());
        assertEquals("Antworte mir bitte nur mit dem Text APPLE_%s".formatted(uuid), message3.getText());

        final Message message4 = messages.get(3);
        assertEquals(MessageType.ASSISTANT, message4.getMessageType());
        assertEquals("APPLE_%s".formatted(uuid), message4.getText());
    }

}
