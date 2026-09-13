package ch.stefanheimberg.lab_spring_ai.rag;

import org.springframework.ai.document.Document;
import org.springframework.ai.document.DocumentWriter;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class ProduktklassifizierungWriter implements DocumentWriter {

    @Autowired
    private VectorStore vectorStore;

    @Override
    public void accept(final List<Document> documents) {
        vectorStore.accept(documents);
    }

}
