package ch.stefanheimberg.lab_spring_ai.rag.chat;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.MessageChatMemoryAdvisor;
import org.springframework.ai.chat.client.advisor.SimpleLoggerAdvisor;
import org.springframework.ai.chat.client.advisor.api.Advisor;
import org.springframework.ai.chat.client.advisor.api.BaseAdvisor;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.rag.advisor.RetrievalAugmentationAdvisor;
import org.springframework.ai.rag.retrieval.search.VectorStoreDocumentRetriever;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;

@SpringBootTest
public class ProduktklassifizierungChatTest {

    @Autowired
    private ChatClient.Builder chatClientBuilder;

    @Autowired
    private ChatMemory chatMemory;

    @Autowired
    private VectorStore vectorStore;

    @BeforeEach
    public void setup() {
        final Advisor loggerAdvisor = new SimpleLoggerAdvisor();
        final Advisor memoryAdvisor = MessageChatMemoryAdvisor.builder(chatMemory)
                // https://docs.spring.io/spring-ai/reference/api/tools.html#inside-the-loop
                .order(BaseAdvisor.HIGHEST_PRECEDENCE + 400)
                .build();
        final Advisor ragAdvisor = RetrievalAugmentationAdvisor.builder()
                .documentRetriever(VectorStoreDocumentRetriever.builder()
                        .similarityThreshold(0.50)
                        .vectorStore(vectorStore)
                        .build())
                .build();

        chatClientBuilder
                .defaultSystem("Du bist ein hilfsbereiter Assistent")
                .defaultAdvisors(List.of(loggerAdvisor, memoryAdvisor, ragAdvisor));
    }

    @Test
    public void test_K022_Firewall() {
        final ChatClient chatClient = chatClientBuilder.build();

        final String conversationId = UUID.randomUUID().toString();

        final String content = chatClient.prompt("Welche Produktklassifizierung kommt für ein Gerät in Frage welches auf TCP Ebene Netzwerkpackete filtert? Antworte mit \"ID: TITLE\"")
                .advisors(a -> a.param(ChatMemory.CONVERSATION_ID, conversationId))
                .call()
                .content();

        assertEquals("K022: Firewalls", content);
    }

    @Test
    public void test_K018_MeetingRoomSysteme() {
        final ChatClient chatClient = chatClientBuilder.build();

        final String conversationId = UUID.randomUUID().toString();

        final String content = chatClient.prompt("In welcher Kategorie muss ich nach Geräten schauen wenn ich einen Beamer kaufen will? Antworte mit \"ID: TITLE\"")
                .advisors(a -> a.param(ChatMemory.CONVERSATION_ID, conversationId))
                .call()
                .content();

        assertEquals("K015: Business-Projektoren", content);
    }

    @Test
    public void test_K006_BusinessMonitore() {
        final ChatClient chatClient = chatClientBuilder.build();

        final String conversationId = UUID.randomUUID().toString();

        final String prompt = """
                # Aufgabe
                Klassifizieren mir dieses Produkt anhand der Produktbeschreibung. Antworte mit "{ID}: {TITLE}"
                
                # Produktbeschreibung
                Der Desktop-Monitor HP 527pm verfügt über ein 27-Zoll-QHD-Display mit einer nativen Auflösung von
                2560 x 1440 und einem dynamischen Kontrastverhältnis von 10000000:1, das lebendige Farben und scharfe
                Bilder liefert. Mit einer Helligkeit von 350 cd/m2 und einer Bildwiederholfrequenz von 100 Hz sorgt der
                Monitor für ein reibungsloses Seherlebnis bei verschiedenen Anwendungen. Ausgestattet mit Flicker-Free-
                und Low Blue Light-Technologien wurde dieser Monitor entwickelt, um die Belastung der Augen bei längerem
                Gebrauch zu reduzieren und den Komfort und die Produktivität zu erhöhen. Mit einer Vielzahl von
                Anschlussmöglichkeiten, darunter HDMI, DisplayPort und USB-C, unterstützt der HP 527pm vielseitige
                Setups für Arbeit und Unterhaltung. Der integrierte USB 3. 2 Gen 1 Hub ermöglicht den schnellen Zugriff
                auf Peripheriegeräte, während die integrierte Kamera mit IR-Sensor nahtlose Videokonferenzen ermöglicht.
                Die Lebensdauer der Hintergrundbeleuchtung von 30. 000 Stunden macht ihn zu einer zuverlässigen Wahl für
                jede Umgebung, ob zu Hause oder im Büro. 27-Zoll-QHD-Display mit einer Auflösung von 2560 x 1440.
                Flicker-Free- und Low Blue Light-Technologien reduzieren die Belastung der Augen. Integrierter USB 3. 2
                Gen 1 Hub für einfachen Zugriff auf Peripheriegeräte. Vielseitige Anschlussmöglichkeiten einschliesslich
                HDMI und USB-C. Eingebaute Kamera mit IR-Sensor für Videokonferenzen.
                """;

        final String content = chatClient.prompt(prompt)
                .advisors(a -> a.param(ChatMemory.CONVERSATION_ID, conversationId))
                .call()
                .content();

        assertEquals("K006: Business-Monitore", content);
    }

    @Test
    public void test_K008_GamingMonitore() {
        final ChatClient chatClient = chatClientBuilder.build();

        final String conversationId = UUID.randomUUID().toString();

        final String prompt = """
                # Aufgabe
                Klassifizieren mir dieses Produkt anhand der Produktbeschreibung. Antworte mit "{ID}: {TITLE}"
                
                # Produktbeschreibung
                TUF Gaming Serie 5 - VG27AQML5A Gaming Monitor – 27-Zoll QHD schnelles IPS-Panel, 300Hz, 0,3ms,
                G-SYNC-kompatibel, AMD FreeSync Premium, ELMB SYNC, VESA DisplayHDR 400, 95% DCI-P3, DisplayWidget
                Center, Gaming AI.
                
                Der TUF Gaming VG27AQML5A QHD ist ein 27-Zoll-Monitor, der fünf fortschrittliche Upgrades für
                erstklassiges Gaming bietet. Er verfügt über eine blitzschnelle Bildwiederholfrequenz von 300Hz und eine
                Reaktionszeit von 0,3ms sowie über AMD FreeSync Premium und NVIDIA G-SYNC-Kompatibilität für ein
                ultra-flüssiges Spielerlebnis. Mit verbesserter Farbperformance, aktualisierter Gaming AI und müheloser
                Steuerung über das DisplayWidget Center bleibt dieser Monitor Ihnen im Wettbewerb immer einen Schritt
                voraus.
                """;

        final String content = chatClient.prompt(prompt)
                .advisors(a -> a.param(ChatMemory.CONVERSATION_ID, conversationId))
                .call()
                .content();

        assertEquals("K008: Gaming-Monitore", content);
    }
}
