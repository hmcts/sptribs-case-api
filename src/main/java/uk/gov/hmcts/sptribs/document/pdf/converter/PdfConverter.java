package uk.gov.hmcts.sptribs.document.pdf.converter;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.List;

@Component
@RequiredArgsConstructor
public class PdfConverter implements FileToPDFConverter {

    @Override
    public List<String> accepts() {
        return List.of("pdf");
    }

    @Override
    public ConvertedPdf convert(byte[] fileContent, String fileName) throws IOException {
        return ConvertedPdf.builder().fileName(fileName).fileContent(fileContent).build();
    }
}
