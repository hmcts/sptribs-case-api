package uk.gov.hmcts.sptribs.document;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.authorisation.generators.AuthTokenGenerator;
import uk.gov.hmcts.sptribs.ciccase.model.LanguagePreference;
import uk.gov.hmcts.sptribs.document.content.DocmosisTemplateProvider;
import uk.gov.hmcts.sptribs.document.model.DocAssemblyRequest;
import uk.gov.hmcts.sptribs.document.model.DocAssemblyResponse;
import uk.gov.hmcts.sptribs.document.model.DocumentInfo;

import java.util.Map;

@Service
@Slf4j
public class DocAssemblyService {

    private static final String CASE_TYPE = "CriminalInjuriesCompensation";
    private static final String JURISDICTION = "ST_CIC";
    private static final String CATEGORY_ID = "TD";

    @Autowired
    private AuthTokenGenerator authTokenGenerator;

    @Autowired
    private DocAssemblyClient docAssemblyClient;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private DocmosisTemplateProvider docmosisTemplateProvider;


    public DocumentInfo renderDocument(final Map<String, Object> templateContent,
                                       final Long caseId,
                                       final String authorisation,
                                       final String templateId,
                                       final LanguagePreference languagePreference,
                                       final String filename) {

        final String templateName = docmosisTemplateProvider.templateNameFor(templateId, languagePreference);

        final DocAssemblyRequest docAssemblyRequest =
            DocAssemblyRequest
                .builder()
                .templateId(templateName)
                .outputType("PDF")
                .secureDocStoreEnabled(true)
                .caseTypeId(CASE_TYPE)
                .jurisdictionId(JURISDICTION)
                .formPayload(objectMapper.valueToTree(templateContent))
                .build();

        final DocAssemblyResponse docAssemblyResponse = docAssemblyClient.generateAndStoreDraftApplication(
            authorisation,
            authTokenGenerator.generate(),
            docAssemblyRequest
        );

        log.debug("Document successfully generated and stored for case Id {} with document location {}",
            caseId,
            docAssemblyResponse.getRenditionOutputLocation()
        );

        return new DocumentInfo(
            docAssemblyResponse.getRenditionOutputLocation(),
            filename + ".pdf",
            docAssemblyResponse.getBinaryFilePath(), CATEGORY_ID
        );
    }
}
