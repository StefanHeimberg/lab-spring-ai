package ch.stefanheimberg.lab_spring_ai.rag.ingestion;

import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.TestMethodOrder;
import org.springframework.ai.document.Document;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.core.io.Resource;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@SpringBootTest
@Import(value = { ProduktklassifizierungTransformer.class, ProduktklassifizierungWriter.class })
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class ProduktklassifizierungIngestionTest {

    private List<Document> extractedDocuments;
    private List<Document> transformedDocuments;

    @Value("file:data/b2b_produktklassifizierung_30_kategorien.xlsx")
    private Resource dataResource;

    @Autowired
    private ProduktklassifizierungTransformer produktklassifizierungTransformer;

    @Autowired
    private ProduktklassifizierungWriter produktklassifizierungWriter;

    @Test
    @Order(1)
    void step_1_extract() {
        extractedDocuments = new ProduktklassifizierungReader(dataResource).get();
        assertNotNull(extractedDocuments);
        assertEquals(30, extractedDocuments.size());
    }

    @Test
    @Order(2)
    void step_2_transform() {
        transformedDocuments = produktklassifizierungTransformer.apply(extractedDocuments);
        assertNotNull(transformedDocuments);
    }

    @Test
    @Order(3)
    void step_3_load() {
        produktklassifizierungWriter.write(transformedDocuments);
    }

}
