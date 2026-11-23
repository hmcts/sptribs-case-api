package uk.gov.hmcts.sptribs.ciccase.service;

import feign.FeignException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.ccd.sdk.type.Document;
import uk.gov.hmcts.ccd.sdk.type.ListValue;
import uk.gov.hmcts.reform.authorisation.generators.AuthTokenGenerator;
import uk.gov.hmcts.reform.ccd.client.CoreCaseDataApi;
import uk.gov.hmcts.reform.ccd.client.model.CaseDataContent;
import uk.gov.hmcts.reform.ccd.client.model.Event;
import uk.gov.hmcts.sptribs.caseworker.service.ExtendedCaseDataApi;
import uk.gov.hmcts.sptribs.controllers.model.CicaCaseUpdate;
import uk.gov.hmcts.sptribs.document.model.CitizenCICDocument;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import static uk.gov.hmcts.sptribs.caseworker.util.EventConstants.CITIZEN_DSS_UPDATE_CASE_SUBMISSION;

@Service
@RequiredArgsConstructor
public class CicaCaseUpdateService {

    private final CicaCaseService cicaCaseService;
    private final CicaRoleService cicaRoleService;
    private final CoreCaseDataApi coreCaseDataApi;
    private final ExtendedCaseDataApi extendedCaseDataApi;
    private final AuthTokenGenerator authTokenGenerator;

    public void updateCase(String ccdReference, String authorisation, CicaCaseUpdate update) {
        // This lookup includes Draft cases and verifies membership independently of CCD case roles.
        cicaCaseService.checkIfUserHasAccess(ccdReference, authorisation);
        String serviceToken = authTokenGenerator.generate();
        Map<String, Object> data = new HashMap<>();
        data.put("dssCaseDataCaseTypeOfApplication", "CIC");
        data.put("dssCaseDataAdditionalInformation", update.additionalInformation());
        data.put("dssCaseDataOtherInfoDocuments", update.documents().stream().map(upload -> {
            CitizenCICDocument document = new CitizenCICDocument();
            document.setDocumentLink(Document.builder()
                .url(upload.documentUrl())
                .binaryUrl(upload.documentBinaryUrl())
                .filename(upload.documentFilename())
                .build());
            document.setComment(upload.comment());
            return new ListValue<>(UUID.randomUUID().toString(), document);
        }).toList());

        try {
            submitUpdate(ccdReference, authorisation, serviceToken, data);
        } catch (FeignException exception) {
            if (exception.status() != 404) {
                throw exception;
            }
            cicaRoleService.ensureCreatorRole(ccdReference, authorisation);
            // Retry once with a fresh event token, never with the system user's identity.
            submitUpdate(ccdReference, authorisation, serviceToken, data);
        }
    }

    private void submitUpdate(String ccdReference, String authorisation, String serviceToken, Map<String, Object> data) {
        var started = coreCaseDataApi.startEvent(
            authorisation, serviceToken, ccdReference, CITIZEN_DSS_UPDATE_CASE_SUBMISSION);
        extendedCaseDataApi.createEvent(authorisation, serviceToken, ccdReference, CaseDataContent.builder()
            .event(Event.builder().id(CITIZEN_DSS_UPDATE_CASE_SUBMISSION).build())
            .eventToken(started.getToken())
            .data(data)
            .build());
    }
}
