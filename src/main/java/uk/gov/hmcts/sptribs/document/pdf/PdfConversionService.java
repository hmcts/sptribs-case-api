package uk.gov.hmcts.sptribs.document.pdf;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.ccd.sdk.type.ListValue;
import uk.gov.hmcts.reform.authorisation.generators.AuthTokenGenerator;
import uk.gov.hmcts.sptribs.document.model.CaseworkerCICDocument;
import uk.gov.hmcts.sptribs.document.pdf.converter.ConvertedPdf;
import uk.gov.hmcts.sptribs.document.pdf.converter.FileToPDFConverter;
import uk.gov.hmcts.sptribs.exception.DocumentTaskProcessingException;
import uk.gov.hmcts.sptribs.idam.IdamService;
import uk.gov.hmcts.sptribs.services.cdam.CaseDocumentClientApi;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class PdfConversionService {

    private final List<FileToPDFConverter> fileToPDFConverters;
    private final CaseDocumentClientApi caseDocumentClientApi;
    private final AuthTokenGenerator serviceAuthTokenGenerator;
    private final IdamService idamService;

    public List<ConvertedPdf> convertFilesToPdf(List<ListValue<CaseworkerCICDocument>> documents,
                                                String caseId) {
        List<ConvertedPdf> convertedPdfs = new ArrayList<>();
        for (ListValue<CaseworkerCICDocument> doc : documents) {
            CaseworkerCICDocument document = doc.getValue();
            var convertedPdf = convertToPdf(document, caseId);

            if (convertedPdf == null) {
                log.error("Unable to convert document {} to PDF for case {}", document.getDocumentCategory(), caseId);
                continue;
            }

            convertedPdf.setOriginalDocument(document);
            convertedPdfs.add(convertedPdf);
        }
        return convertedPdfs;
    }

    private ConvertedPdf convertToPdf(CaseworkerCICDocument document, String caseId) {
        String fileExtension = document.getDocumentLink().getFilename()
            .substring(document.getDocumentLink().getFilename().lastIndexOf(".") + 1);
        String docId = document.getDocumentLink().getUrl()
            .substring(document.getDocumentLink().getUrl().lastIndexOf('/') + 1);

        for (var converter : fileToPDFConverters) {
            if (converter.accepts().contains(fileExtension.toLowerCase())) {
                try {
                    log.info("Converting document type {} to PDF for case {}", converter.getClass().getSimpleName(), caseId);
                    return getConvertedPdf(document, converter, docId);
                } catch (Exception e) {
                    throw new DocumentTaskProcessingException(String.format("Error trying to convert file %s to PDF for case %s",
                        document.getDocumentLink().getUrl(), caseId), e);
                }
            }
        }
        throw new RuntimeException("Unexpected file type: " + fileExtension);
    }

    private ConvertedPdf getConvertedPdf(CaseworkerCICDocument document,
                                         FileToPDFConverter converter,
                                         String docId) {
        try {
            final var response = getDocumentBinary(docId);
            if (response != null && response.getStatusCode().is2xxSuccessful()) {
                log.info("Successfully downloaded document with id {} for case {}", docId, docId);
                var byteArrayResource = (ByteArrayResource) response.getBody();
                if (byteArrayResource != null) {
                    byte[] fileContent = byteArrayResource.getByteArray();
                    return converter.convert(fileContent, document.getDocumentLink().getFilename());
                }
            }
        } catch (IOException e) {
            throw new DocumentTaskProcessingException("Could not download the file from CDAM", e);
        } catch (Exception e) {
            throw new DocumentTaskProcessingException(e.getMessage(), e);
        }

        log.error("Could not access the binary for documentId: {}", docId);
        throw new DocumentTaskProcessingException(String.format("Could not access the binary for docId %s.", docId));
    }

    private ResponseEntity<Resource> getDocumentBinary(String docId) {
        var serviceAuthorization = serviceAuthTokenGenerator.generate();
        var authorization = idamService.retrieveSystemUpdateUserDetails().getAuthToken();
        log.info("Service auth {} and authorization {}", serviceAuthorization, authorization);
        UUID docIdAsUUID = UUID.fromString(docId);
        return caseDocumentClientApi.getDocumentBinary(authorization, serviceAuthorization, docIdAsUUID);
    }
}
