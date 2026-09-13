package ch.stefanheimberg.lab_spring_ai.rag.ingestion;

import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.ai.document.Document;
import org.springframework.ai.document.DocumentReader;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Component
public class ProduktklassifizierungReader implements DocumentReader {

    private final Resource resource;

    public ProduktklassifizierungReader(final Resource resource) {
        this.resource = resource;
    }

    @Override
    public List<Document> get() {
        final List<Document> documents = new ArrayList<>();

        try (final InputStream inputStream = resource.getInputStream();
             final Workbook workbook = new XSSFWorkbook(inputStream)) {
            // Excel Blatt 1
            final Sheet kategorienSheet = workbook.getSheetAt(0);

            for (final Row kategorieRow : kategorienSheet) {
                // skip forst row
                if (kategorieRow.getRowNum() == 0) {
                    continue;
                }

                int i=0;
                final String id = kategorieRow.getCell(i++).getStringCellValue();
                final String titel = kategorieRow.getCell(i++).getStringCellValue();
                final String giltWenn = kategorieRow.getCell(i++).getStringCellValue();
                final String giltNichtWenn = kategorieRow.getCell(i++).getStringCellValue();
                final String marktsegment = kategorieRow.getCell(i++).getStringCellValue();
                final String kundensegment = kategorieRow.getCell(i).getStringCellValue();

                final String text = """
                        # Produktklassifizierung: %s
                        
                        ## ID
                        %s
                        
                        ## Gilt wenn
                        %s
                        
                        ## Gilt nicht wenn
                        %s
                        
                        ## Marktsegment
                        %s
                        
                        ## Kundensegment
                        %s
                        """.formatted(
                        titel,
                        id,
                        giltWenn,
                        giltNichtWenn,
                        marktsegment,
                        kundensegment
                );

                final Document document = Document.builder()
                        .id("%s_%s".formatted(kategorienSheet.getSheetName(), id))
                        .text(text)
                        .metadata(Map.of(
                                "source", resource.getFilename(),
                                "klassifizierung_id", id,
                                "titel", titel,
                                "marktsegment", marktsegment,
                                "kundensegment", kundensegment
                        ))
                        .build();

                documents.add(document);
            }

        } catch (final IOException e) {
            throw new RuntimeException(e);
        }

        return documents;
    }

}
