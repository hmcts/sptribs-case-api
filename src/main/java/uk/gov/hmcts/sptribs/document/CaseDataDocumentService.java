package uk.gov.hmcts.sptribs.document;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.ccd.sdk.type.Document;
import uk.gov.hmcts.reform.idam.client.models.User;
import uk.gov.hmcts.sptribs.ciccase.model.LanguagePreference;
import uk.gov.hmcts.sptribs.idam.IdamService;

import java.util.Map;
import javax.servlet.http.HttpServletRequest;

import static org.springframework.http.HttpHeaders.AUTHORIZATION;
import static uk.gov.hmcts.sptribs.document.DocumentUtil.documentFrom;

@Service
@Slf4j
public class CaseDataDocumentService {

    @Autowired
    private DocAssemblyService docAssemblyService;

    @Autowired
    private IdamService idamService;

    public Document renderDocument(final Map<String, Object> templateContent,
                                   final Long caseId,
                                   final String templateId,
                                   final LanguagePreference languagePreference,
                                   final String filename,
                                   final HttpServletRequest request) {

        log.info("Rendering document request for templateId : {} ", templateId);

        final User caseworkerUser = idamService.retrieveUser(request.getHeader(AUTHORIZATION));
        final String authorisation = caseworkerUser.getAuthToken();

        final var documentInfo = docAssemblyService.renderDocument(
            templateContent,
            caseId,
            authorisation,
            templateId,
            languagePreference,
            filename
        );

        return documentFrom(documentInfo);
    }
}
