package uk.gov.hmcts.sptribs.document.pdf.converter;

import com.google.common.collect.Lists;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;
import uk.gov.hmcts.sptribs.document.model.ByteArrayMultipartFile;
import uk.gov.hmcts.sptribs.services.docmosis.DocmosisApiClient;

import java.io.IOException;
import java.util.List;

/**
 * Converts word doc,docx,excel,power point files to PDF using the Docmosis API.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class DocmosisConverter implements FileToPDFConverter {

    @Value("${docmosis.tornado.key}")
    private String docmosisAccessKey;
    private final DocmosisApiClient docmosisApiClient;

    @Override
    public List<String> accepts() {
        return Lists.newArrayList("csv",
            "txt",
            "rtf",
            "xlsx",
            "docx",
            "doc",
            "xls"
        );
    }

    @Override
    public ConvertedPdf convert(byte[] fileContent, String originalFileName) throws IOException {

        final String convertedFileName = originalFileName + ".pdf";

        MultipartFile multipartFile = ByteArrayMultipartFile.builder()
            .name("file")
            .content(fileContent)
            .originalName(originalFileName)
            .contentType(MediaType.APPLICATION_PDF)
            .build();

        final var response = docmosisApiClient.convert(docmosisAccessKey, convertedFileName, multipartFile);

        if (!response.getStatusCode().is2xxSuccessful()) {
            String responseMsg = String.format(
                "Docmosis error code : (%s) for converting: %s with response msg: %s ",
                response.getStatusCode(),
                originalFileName,
                response.getBody()
            );
            log.error(responseMsg);
            throw new IOException(responseMsg);
        }

        return ConvertedPdf.builder().fileContent(response.getBody()).fileName(convertedFileName).build();
    }
}
