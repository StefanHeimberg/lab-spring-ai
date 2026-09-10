package ch.stefanheimberg.lab_spring_ai;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.MessageChatMemoryAdvisor;
import org.springframework.ai.chat.client.advisor.SimpleLoggerAdvisor;
import org.springframework.ai.chat.client.advisor.api.Advisor;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.chat.messages.AssistantMessage;
import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.chat.messages.MessageType;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.tool.ToolCallback;
import org.springframework.ai.tool.definition.ToolDefinition;
import org.springframework.ai.tool.function.FunctionToolCallback;
import org.springframework.ai.tool.method.MethodToolCallback;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.util.ReflectionUtils;

import java.lang.reflect.Method;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest
public class ToolCallingTest {

    @Autowired
    private ChatClient.Builder chatClientBuilder;

    @Autowired
    private ChatMemory chatMemory;

    @BeforeEach
    public void setup() {
        final Advisor loggerAdvisor = new SimpleLoggerAdvisor();
        final Advisor memoryAdvisor = MessageChatMemoryAdvisor.builder(chatMemory).build();

        chatClientBuilder
                .defaultSystem("Du bist ein hilfsbereiter Assistent")
                .defaultAdvisors(List.of(loggerAdvisor, memoryAdvisor));
    }

    @Test
    public void declarative() {
        final ChatClient chatClient = chatClientBuilder.build();

        final String conversationId = UUID.randomUUID().toString();

        final DateTimeTools dateTimeTools = new DateTimeTools();

        assertNull(dateTimeTools.getAlarm());

        chatClient.prompt("Kannst du mir jetzt einen Alarm für in 10 Minuten stellen?")
                .advisors(a -> a.param(ChatMemory.CONVERSATION_ID, conversationId))
                .tools(dateTimeTools)
                .call()
                .content();

        assertNotNull(dateTimeTools.getAlarm());

        final List<Message> messages = chatMemory.get(conversationId);
        assertEquals(2, messages.size());

        final Message message1 = messages.get(0);
        assertEquals(MessageType.USER, message1.getMessageType());
        assertEquals("Kannst du mir jetzt einen Alarm für in 10 Minuten stellen?", message1.getText());
        assertEquals(0, ((UserMessage)message1).getMedia().size());

        final Message message2 = messages.get(1);
        assertEquals(MessageType.ASSISTANT, message2.getMessageType());
        assertEquals(false, ((AssistantMessage)message2).hasToolCalls());
    }

    @Test
    void methodToolCallback() {
        final ChatClient chatClient = chatClientBuilder.build();

        final String conversationId = UUID.randomUUID().toString();

        final WeatherTools weatherTools = new WeatherTools();
        final Method method = ReflectionUtils.findMethod(WeatherTools.class, "getWeather", String.class, String.class);
        assertNotNull(method);

        final ToolCallback weatherCallback = MethodToolCallback.builder()
                .toolDefinition(ToolDefinition.builder()
                        .name("getWeather")
                        .description("Get the weather for a city at a specific time")
                        .inputSchema("""
                            {
                                "type": "object",
                                "properties": {
                                    "city": { "type": "string", "description": "City name" },
                                    "at": { "type": "string", "format": "date-time", "description": "Time in ISO-8601 format" }
                                },
                                "required": ["city", "at"]
                            }
                        """)
                        .build())
                .toolMethod(method)
                .toolObject(weatherTools)
                .build();

        final String content = chatClient.prompt("Wie ist das Wetter in Bern?")
                .advisors(a -> a.param(ChatMemory.CONVERSATION_ID, conversationId))
                .tools(weatherCallback)
                .call()
                .content();

        assertTrue(content.contains("22"), content);
        assertTrue(content.contains("°C"), content);
        assertTrue(content.contains("Bern"), content);

        final List<Message> messages = chatMemory.get(conversationId);
        assertEquals(2, messages.size());

        final Message message1 = messages.get(0);
        assertEquals(MessageType.USER, message1.getMessageType());
        assertEquals("Wie ist das Wetter in Bern?", message1.getText());
        assertEquals(0, ((UserMessage)message1).getMedia().size());

        final Message message2 = messages.get(1);
        assertEquals(MessageType.ASSISTANT, message2.getMessageType());
        assertEquals(false, ((AssistantMessage)message2).hasToolCalls());
    }

    @Test
    @Disabled
    void functionToolCallback() {
        final ChatClient chatClient = chatClientBuilder.build();

        final String conversationId = UUID.randomUUID().toString();

        final WeatherTools weatherTools = new WeatherTools();

        final ToolCallback weatherCallback = FunctionToolCallback.builder("getCurrentWeather", weatherTools::getCurrentWeather)
                .inputType(String.class)
                .build();

        final String content = chatClient.prompt("Wie ist das Wetter in Bern?")
                .advisors(a -> a.param(ChatMemory.CONVERSATION_ID, conversationId))
                .tools(weatherCallback)
                .call()
                .content();

        assertTrue(content.contains("19"), content);
        assertTrue(content.contains("°C"), content);
        assertTrue(content.contains("Bern"), content);

        final List<Message> messages = chatMemory.get(conversationId);
        assertEquals(2, messages.size());

        final Message message1 = messages.get(0);
        assertEquals(MessageType.USER, message1.getMessageType());
        assertEquals("Wie ist das Wetter in Bern?", message1.getText());
        assertEquals(0, ((UserMessage)message1).getMedia().size());

        final Message message2 = messages.get(1);
        assertEquals(MessageType.ASSISTANT, message2.getMessageType());
        assertEquals(false, ((AssistantMessage)message2).hasToolCalls());
    }
}
