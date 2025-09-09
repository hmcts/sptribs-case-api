package uk.gov.hmcts.sptribs.document.pdf.converter;

import java.io.IOException;
import java.util.List;

public interface FileToPDFConverter {

    List<String> accepts();

    ConvertedPdf convert(byte[] fileContent, String fileName) throws IOException;
}
