package ch.stefanheimberg.lab_spring_ai;

import ch.stefanheimberg.lab_spring_ai.tools.DateTimeTools;
import ch.stefanheimberg.lab_spring_ai.tools.WeatherTools;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.MessageChatMemoryAdvisor;
import org.springframework.ai.chat.client.advisor.SimpleLoggerAdvisor;
import org.springframework.ai.chat.client.advisor.api.Advisor;
import org.springframework.ai.chat.client.advisor.api.BaseAdvisor;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.chat.messages.AssistantMessage;
import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.chat.messages.MessageType;
import org.springframework.ai.chat.messages.ToolResponseMessage;
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
        final Advisor memoryAdvisor = MessageChatMemoryAdvisor.builder(chatMemory)
                // https://docs.spring.io/spring-ai/reference/api/tools.html#inside-the-loop
                .order(BaseAdvisor.HIGHEST_PRECEDENCE + 400)
                .build();

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

        chatClient.prompt("Kannst du mir jetzt einen Alarm für in 10 Minuten stellen? Antworte mir zwingen mit dem Text: 'Alarm ist eingerichtet um HIER_UHRZEIT_EINFUEGEN'")
                .advisors(a -> a.param(ChatMemory.CONVERSATION_ID, conversationId))
                .tools(dateTimeTools)
                .call()
                .content();

        assertNotNull(dateTimeTools.getAlarm());

        final List<Message> messages = chatMemory.get(conversationId);
        assertEquals(6, messages.size());

        final Message message1 = messages.get(0);
        assertEquals(MessageType.USER, message1.getMessageType());
        assertEquals("Kannst du mir jetzt einen Alarm für in 10 Minuten stellen? Antworte mir zwingen mit dem Text: 'Alarm ist eingerichtet um HIER_UHRZEIT_EINFUEGEN'", message1.getText());
        assertEquals(0, ((UserMessage)message1).getMedia().size());

        final Message message2 = messages.get(1);
        assertEquals(MessageType.ASSISTANT, message2.getMessageType());
        assertEquals("", message2.getText());
        assertEquals(true, ((AssistantMessage)message2).hasToolCalls());
        final List<AssistantMessage.ToolCall> toolCalls2 = ((AssistantMessage) message2).getToolCalls();
        assertEquals(1, toolCalls2.size());
        final AssistantMessage.ToolCall toolCalls2_1 = toolCalls2.get(0);
        assertEquals("function", toolCalls2_1.type());
        assertEquals("getCurrentDateTime", toolCalls2_1.name());
        assertEquals("{}", toolCalls2_1.arguments());

        final Message message3 = messages.get(2);
        assertEquals(MessageType.TOOL, message3.getMessageType());
        assertEquals("", message3.getText());
        assertTrue(message3 instanceof ToolResponseMessage);

        final Message message4 = messages.get(3);
        assertEquals(MessageType.ASSISTANT, message4.getMessageType());
        assertEquals("", message4.getText());
        assertEquals(true, ((AssistantMessage)message4).hasToolCalls());
        final List<AssistantMessage.ToolCall> toolCalls4 = ((AssistantMessage) message4).getToolCalls();
        assertEquals(1, toolCalls4.size());
        final AssistantMessage.ToolCall toolCalls4_1 = toolCalls4.get(0);
        assertEquals("function", toolCalls4_1.type());
        assertEquals("setAlarm", toolCalls4_1.name());
        assertTrue(toolCalls4_1.arguments().contains("\"time\":\""), toolCalls4_1.arguments());

        final Message message5 = messages.get(4);
        assertEquals(MessageType.TOOL, message5.getMessageType());
        assertEquals("", message3.getText());
        assertTrue(message5 instanceof ToolResponseMessage);

        final Message message6 = messages.get(5);
        assertEquals(MessageType.ASSISTANT, message6.getMessageType());
        assertTrue(message6.getText().startsWith("Alarm ist eingerichtet um "), message6.getText());
        assertEquals(false, ((AssistantMessage)message6).hasToolCalls());
        assertEquals("STOP", message6.getMetadata().get("finishReason"));
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
        assertEquals(4, messages.size());

        final Message message1 = messages.get(0);
        assertEquals(MessageType.USER, message1.getMessageType());
        assertEquals("Wie ist das Wetter in Bern?", message1.getText());
        assertEquals(0, ((UserMessage)message1).getMedia().size());

        final Message message2 = messages.get(1);
        assertEquals(MessageType.ASSISTANT, message2.getMessageType());
        assertEquals("", message2.getText());
        assertEquals(true, ((AssistantMessage)message2).hasToolCalls());
        final List<AssistantMessage.ToolCall> toolCalls2 = ((AssistantMessage) message2).getToolCalls();
        assertEquals(1, toolCalls2.size());
        final AssistantMessage.ToolCall toolCalls2_1 = toolCalls2.get(0);
        assertEquals("function", toolCalls2_1.type());
        assertEquals("getWeather", toolCalls2_1.name());
        assertTrue(toolCalls2_1.arguments().contains("\"city\":\"Bern\""), toolCalls2_1.arguments());
        assertTrue(toolCalls2_1.arguments().contains("\"at\":\""), toolCalls2_1.arguments());

        final Message message3 = messages.get(2);
        assertEquals(MessageType.TOOL, message3.getMessageType());
        assertEquals("", message3.getText());
        assertTrue(message3 instanceof ToolResponseMessage);

        final Message message4 = messages.get(3);
        assertEquals(MessageType.ASSISTANT, message4.getMessageType());
        assertTrue(message4.getText().contains("Bern"), message4.getText());
        assertTrue(message4.getText().contains("22"), message4.getText());
        assertTrue(message4.getText().contains("°C"), message4.getText());
        assertEquals(false, ((AssistantMessage)message4).hasToolCalls());
        assertEquals("STOP", message4.getMetadata().get("finishReason"));
    }

    @Test
    void functionToolCallback() {
        final ChatClient chatClient = chatClientBuilder.build();

        final String conversationId = UUID.randomUUID().toString();

        final WeatherTools weatherTools = new WeatherTools();

        final ToolCallback weatherCallback = FunctionToolCallback.builder("getCurrentWeather", weatherTools::getCurrentWeather)
                .inputType(WeatherTools.CurrentWeatherRequest.class)
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
        assertEquals(4, messages.size());

        final Message message1 = messages.get(0);
        assertEquals(MessageType.USER, message1.getMessageType());
        assertEquals("Wie ist das Wetter in Bern?", message1.getText());
        assertEquals(0, ((UserMessage)message1).getMedia().size());

        final Message message2 = messages.get(1);
        assertEquals(MessageType.ASSISTANT, message2.getMessageType());
        assertEquals("", message2.getText());
        assertEquals(true, ((AssistantMessage)message2).hasToolCalls());
        final List<AssistantMessage.ToolCall> toolCalls2 = ((AssistantMessage) message2).getToolCalls();
        assertEquals(1, toolCalls2.size());
        final AssistantMessage.ToolCall toolCalls2_1 = toolCalls2.get(0);
        assertEquals("function", toolCalls2_1.type());
        assertEquals("getCurrentWeather", toolCalls2_1.name());
        assertEquals("{\"city\":\"Bern\"}", toolCalls2_1.arguments());

        final Message message3 = messages.get(2);
        assertEquals(MessageType.TOOL, message3.getMessageType());
        assertEquals("", message3.getText());
        assertTrue(message3 instanceof ToolResponseMessage);

        final Message message4 = messages.get(3);
        assertEquals(MessageType.ASSISTANT, message4.getMessageType());
        assertTrue(message4.getText().contains("Bern"), message4.getText());
        assertTrue(message4.getText().contains("19"), message4.getText());
        assertTrue(message4.getText().contains("°C"), message4.getText());
        assertEquals(false, ((AssistantMessage)message4).hasToolCalls());
        assertEquals("STOP", message4.getMetadata().get("finishReason"));
    }
}
