package uk.gov.hmcts.sptribs.dmn.service;

import io.restassured.http.Headers;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.ccd.client.CoreCaseDataApi;
import uk.gov.hmcts.reform.ccd.client.model.CaseDataContent;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.ccd.client.model.Event;
import uk.gov.hmcts.reform.ccd.client.model.StartEventResponse;
import uk.gov.hmcts.sptribs.dmn.domain.entities.idam.UserInfo;

import java.io.IOException;
import java.util.Locale;
import java.util.Map;

import static java.util.Collections.emptyMap;
import static uk.gov.hmcts.sptribs.dmn.service.AuthorizationHeadersProvider.AUTHORIZATION;
import static uk.gov.hmcts.sptribs.dmn.service.AuthorizationHeadersProvider.SERVICE_AUTHORIZATION;

@Service
public class CcdCaseCreator {

    @Autowired
    protected CoreCaseDataApi coreCaseDataApi;

    @Autowired
    protected AuthorizationHeadersProvider authorizationHeadersProvider;

    @Autowired
    private MapValueExpander mapValueExpander;

    public String createCase(Map<String, Object> scenario,
                             String jurisdiction,
                             String caseType,
                             Headers authorizationHeaders) throws IOException {

        Map<String, String> ccdTemplatesByFilename =
            StringResourceLoader.load(
                "/templates/" + jurisdiction.toLowerCase(Locale.ENGLISH) + "/ccd/*.json"
            );

        Map<String, Object> caseData = getCaseData(scenario, ccdTemplatesByFilename);

        String eventId = MapValueExtractor.extractOrThrow(scenario, "eventId");

        String caseId = createInitialStartEventAndSubmit(
            eventId,
            jurisdiction,
            caseType,
            caseData,
            authorizationHeaders
        );

        return caseId;

    }

    private Map<String, Object> getCaseData(
        Map<String, Object> input,
        Map<String, String> templatesByFilename
    ) throws IOException {

        Map<String, Object> caseData = buildCaseData(
            MapValueExtractor.extract(input, "caseData"),
            templatesByFilename
        );

        return caseData;
    }

    private Map<String, Object> buildCaseData(
        Map<String, Object> caseDataInput,
        Map<String, String> templatesByFilename
    ) throws IOException {

        String templateFilename = MapValueExtractor.extract(caseDataInput, "template");

        String template = templatesByFilename.get(templateFilename);

        Map<String, Object> caseData = deserializeWithExpandedValues(template);
        Map<String, Object> caseDataReplacements = MapValueExtractor.extract(caseDataInput, "replacements");
        if (caseDataReplacements != null) {
            MapMerger.merge(caseData, caseDataReplacements);
        }

        return caseData;
    }

    private Map<String, Object> deserializeWithExpandedValues(String source) throws IOException {
        Map<String, Object> data = MapSerializer.deserialize(source);
        mapValueExpander.expandValues(data, emptyMap());
        return data;
    }

    private String createInitialStartEventAndSubmit(String eventId,
                                                    String jurisdiction,
                                                    String caseType,
                                                    Map<String, Object> caseData,
                                                    Headers authorizationHeaders) {

        String userToken = authorizationHeaders.getValue(AUTHORIZATION);
        String serviceToken = authorizationHeaders.getValue(SERVICE_AUTHORIZATION);
        UserInfo userInfo = authorizationHeadersProvider.getUserInfo(userToken);

        //Fire start event
        StartEventResponse startCase = coreCaseDataApi.startForCaseworker(
            userToken,
            serviceToken,
            userInfo.getUid(),
            jurisdiction,
            caseType,
            eventId
        );

        CaseDataContent caseDataContent = CaseDataContent.builder()
            .eventToken(startCase.getToken())
            .event(Event.builder()
                .id(startCase.getEventId())
                .summary("summary")
                .description("description")
                .build())
            .data(caseData)
            .build();

        //Fire submit event
        CaseDetails caseDetails = coreCaseDataApi.submitForCaseworker(
            userToken,
            serviceToken,
            userInfo.getUid(),
            jurisdiction,
            caseType,
            true,
            caseDataContent
        );

        System.out.println("Created case [" + caseDetails.getId() + "]");

        return caseDetails.getId().toString();
    }
}
