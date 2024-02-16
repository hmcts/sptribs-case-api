package uk.gov.hmcts.sptribs.services.ccd;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import uk.gov.hmcts.reform.authorisation.generators.AuthTokenGenerator;
import uk.gov.hmcts.reform.ccd.client.CoreCaseDataApi;
import uk.gov.hmcts.reform.ccd.client.model.CaseDataContent;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.ccd.client.model.Event;
import uk.gov.hmcts.reform.ccd.client.model.StartEventResponse;
import uk.gov.hmcts.reform.idam.client.models.User;
import uk.gov.hmcts.reform.idam.client.models.UserDetails;
import uk.gov.hmcts.sptribs.ciccase.model.CaseData;
import uk.gov.hmcts.sptribs.common.config.AppsConfig;
import uk.gov.hmcts.sptribs.idam.IdamService;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.sptribs.constants.CommonConstants.ST_CIC_CASE_TYPE;
import static uk.gov.hmcts.sptribs.constants.CommonConstants.ST_CIC_JURISDICTION;
import static uk.gov.hmcts.sptribs.edgecase.event.Event.UPDATE_CASE;
import static uk.gov.hmcts.sptribs.testutil.TestConstants.CASE_DATA_CIC_ID;
import static uk.gov.hmcts.sptribs.testutil.TestConstants.CASE_DATA_FILE_CIC;
import static uk.gov.hmcts.sptribs.testutil.TestConstants.CASE_TEST_AUTHORIZATION;
import static uk.gov.hmcts.sptribs.testutil.TestConstants.TEST_AUTHORIZATION_TOKEN;
import static uk.gov.hmcts.sptribs.testutil.TestConstants.TEST_CASE_ID;
import static uk.gov.hmcts.sptribs.testutil.TestConstants.TEST_USER;
import static uk.gov.hmcts.sptribs.testutil.TestFileUtil.loadJson;
import static uk.gov.hmcts.sptribs.util.AppsUtil.getExactAppsDetailsByCaseType;


@ExtendWith(SpringExtension.class)
@SpringBootTest
@TestPropertySource("classpath:application.yaml")
@ActiveProfiles("test")
class CaseApiServiceTest {
    private final ObjectMapper mapper = new ObjectMapper().registerModule(new JavaTimeModule());
    private static final String TEST_CASE_REFERENCE = "123";
    private AppsConfig.AppsDetails cicAppDetails;

    @InjectMocks
    private CaseApiService caseApiService;

    @Mock
    private AuthTokenGenerator authTokenGenerator;

    @Mock
    StartEventResponse eventRes;

    @Mock
    User user;

    @Mock
    UserDetails userDetails;

    @Mock
    IdamService idamService;

    @Autowired
    AppsConfig appsConfig;

    @Mock
    CoreCaseDataApi coreCaseDataApi;

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);
        cicAppDetails = appsConfig.getApps().stream().filter(eachApps -> eachApps.getCaseTypeOfApplication().contains(
            CASE_DATA_CIC_ID)).findAny().orElse(null);
    }

    @Test
    void shouldfgmCreateCaseData() throws Exception {
        final String caseDataJson = loadJson(CASE_DATA_FILE_CIC);
        final CaseData caseData = mapper.readValue(caseDataJson, CaseData.class);

        final Map<String, Object> caseDataMap = new ConcurrentHashMap<>();

        caseDataMap.put(TEST_CASE_REFERENCE, caseData);
        final CaseDetails caseDetail = CaseDetails.builder().caseTypeId(CASE_DATA_CIC_ID)
            .id(TEST_CASE_ID)
            .data(caseDataMap)
            .jurisdiction(ST_CIC_JURISDICTION)
            .build();

        eventRes = StartEventResponse.builder()
            .eventId(getExactAppsDetailsByCaseType(appsConfig, ST_CIC_CASE_TYPE).getEventIds().getSubmitEvent())
            .caseDetails(caseDetail)
            .token(TEST_AUTHORIZATION_TOKEN)
            .build();

        when(idamService.retrieveUser(CASE_TEST_AUTHORIZATION)).thenReturn(user);

        when(user.getUserDetails()).thenReturn(userDetails);

        when(userDetails.getId()).thenReturn(TEST_USER);

        when(authTokenGenerator.generate()).thenReturn(TEST_USER);

        when(coreCaseDataApi.startForCitizen(
            CASE_TEST_AUTHORIZATION,
            authTokenGenerator.generate(),
            TEST_USER,
            ST_CIC_JURISDICTION,
            ST_CIC_CASE_TYPE,
            getExactAppsDetailsByCaseType(appsConfig, ST_CIC_CASE_TYPE).getEventIds().getCreateEvent()
        )).thenReturn(eventRes);

        final CaseDataContent caseDataContent = CaseDataContent.builder()
            .data(caseData)
            .event(Event.builder().id(getExactAppsDetailsByCaseType(
                appsConfig,
                ST_CIC_CASE_TYPE
            ).getEventIds().getCreateEvent()).build())
            .eventToken(TEST_AUTHORIZATION_TOKEN)
            .build();

        when(coreCaseDataApi.submitForCitizen(
            CASE_TEST_AUTHORIZATION,
            authTokenGenerator.generate(),
            TEST_USER,
            ST_CIC_JURISDICTION,
            ST_CIC_CASE_TYPE,
            true,
            caseDataContent
        )).thenReturn(caseDetail);

        final CaseDetails createCaseDetail = caseApiService.createCase(CASE_TEST_AUTHORIZATION, caseData, cicAppDetails);

        assertEquals(CASE_DATA_CIC_ID, createCaseDetail.getCaseTypeId());
        assertEquals(createCaseDetail.getId(), caseDetail.getId());
        assertEquals(createCaseDetail.getCaseTypeId(), caseDetail.getCaseTypeId());
        assertEquals(createCaseDetail.getData(), caseDetail.getData());
        assertEquals(createCaseDetail.getData().get(TEST_CASE_REFERENCE), caseDataMap.get(TEST_CASE_REFERENCE));
    }

    @Test
    void shouldUpdateCaseData() throws IOException {

        final String caseDataJson = loadJson(CASE_DATA_FILE_CIC);
        final CaseData caseData = mapper.readValue(caseDataJson, CaseData.class);

        final Map<String, Object> caseDataMap = new ConcurrentHashMap<>();

        caseDataMap.put(TEST_CASE_REFERENCE, caseData);
        final CaseDetails caseDetail = CaseDetails.builder().caseTypeId(CASE_DATA_CIC_ID)
            .id(TEST_CASE_ID)
            .data(caseDataMap)
            .jurisdiction(ST_CIC_JURISDICTION)
            .build();

        eventRes = StartEventResponse.builder()
            .eventId(getExactAppsDetailsByCaseType(appsConfig, ST_CIC_CASE_TYPE).getEventIds().getUpdateEvent())
            .caseDetails(caseDetail)
            .token(TEST_AUTHORIZATION_TOKEN)
            .build();

        when(idamService.retrieveUser(CASE_TEST_AUTHORIZATION)).thenReturn(user);

        when(user.getUserDetails()).thenReturn(userDetails);

        when(userDetails.getId()).thenReturn(TEST_USER);

        when(authTokenGenerator.generate()).thenReturn(TEST_USER);

        when(coreCaseDataApi.startEventForCitizen(
            CASE_TEST_AUTHORIZATION,
            authTokenGenerator.generate(),
            TEST_USER,
            ST_CIC_JURISDICTION,
            ST_CIC_CASE_TYPE,
            TEST_CASE_ID.toString(),
            getExactAppsDetailsByCaseType(appsConfig, ST_CIC_CASE_TYPE).getEventIds().getUpdateEvent()
        )).thenReturn(eventRes);

        final CaseDataContent caseDataContent = CaseDataContent.builder()
            .data(caseData)
            .event(Event.builder().id(getExactAppsDetailsByCaseType(
                appsConfig,
                ST_CIC_CASE_TYPE
            ).getEventIds().getUpdateEvent()).build())
            .eventToken(TEST_AUTHORIZATION_TOKEN)
            .build();

        when(coreCaseDataApi.submitEventForCitizen(
            CASE_TEST_AUTHORIZATION,
            authTokenGenerator.generate(),
            TEST_USER,
            ST_CIC_JURISDICTION,
            ST_CIC_CASE_TYPE,
            TEST_CASE_ID.toString(),
            true,
            caseDataContent
        )).thenReturn(caseDetail);

        final CaseDetails updateCaseDetails = caseApiService.updateCase(
            CASE_TEST_AUTHORIZATION,
            uk.gov.hmcts.sptribs.edgecase.event.Event.UPDATE,
            TEST_CASE_ID,
            caseData,
            cicAppDetails);

        assertEquals(CASE_DATA_CIC_ID, updateCaseDetails.getCaseTypeId());
        assertEquals(updateCaseDetails.getId(), caseDetail.getId());
        assertEquals(updateCaseDetails.getCaseTypeId(), caseDetail.getCaseTypeId());
        assertEquals(updateCaseDetails.getData(), caseDetail.getData());
        assertEquals(updateCaseDetails.getData().get(TEST_CASE_REFERENCE), caseDataMap.get(TEST_CASE_REFERENCE));
    }

    @Test
    void shouldGetEventToken() throws IOException {

        final String caseDataJson = loadJson(CASE_DATA_FILE_CIC);
        final CaseData caseData = mapper.readValue(caseDataJson, CaseData.class);

        final Map<String, Object> caseDataMap = new ConcurrentHashMap<>();

        caseDataMap.put(TEST_CASE_REFERENCE, caseData);
        final CaseDetails caseDetail = CaseDetails.builder().caseTypeId(CASE_DATA_CIC_ID)
            .id(TEST_CASE_ID)
            .data(caseDataMap)
            .jurisdiction(ST_CIC_JURISDICTION)
            .build();

        eventRes = StartEventResponse.builder()
            .eventId(getExactAppsDetailsByCaseType(appsConfig, ST_CIC_CASE_TYPE).getEventIds().getCreateEvent())
            .caseDetails(caseDetail)
            .token(TEST_AUTHORIZATION_TOKEN)
            .build();

        when(coreCaseDataApi.startForCitizen(
            CASE_TEST_AUTHORIZATION,
            authTokenGenerator.generate(),
            TEST_USER,
            ST_CIC_JURISDICTION,
            ST_CIC_CASE_TYPE,
            getExactAppsDetailsByCaseType(appsConfig, ST_CIC_CASE_TYPE).getEventIds().getCreateEvent()
        )).thenReturn(eventRes);

        final String result = caseApiService.getEventToken(
            CASE_TEST_AUTHORIZATION,
            TEST_USER,
            cicAppDetails.getEventIds().getCreateEvent(),
            cicAppDetails
        );

        assertNotNull(result);
        assertEquals(eventRes.getToken(),result);
        assertEquals(eventRes.getToken(),result);

    }

    @Test
    void shouldGetEventTokenForUpdate() throws IOException {

        final String caseDataJson = loadJson(CASE_DATA_FILE_CIC);
        final CaseData caseData = mapper.readValue(caseDataJson, CaseData.class);

        final Map<String, Object> caseDataMap = new ConcurrentHashMap<>();

        caseDataMap.put(TEST_CASE_REFERENCE, caseData);
        final CaseDetails caseDetail = CaseDetails.builder().caseTypeId(CASE_DATA_CIC_ID)
            .id(TEST_CASE_ID)
            .data(caseDataMap)
            .jurisdiction(ST_CIC_JURISDICTION)
            .build();

        eventRes = StartEventResponse.builder()
            .eventId(getExactAppsDetailsByCaseType(appsConfig, ST_CIC_CASE_TYPE).getEventIds().getUpdateEvent())
            .caseDetails(caseDetail)
            .token(TEST_AUTHORIZATION_TOKEN)
            .build();

        when(coreCaseDataApi.startEventForCitizen(
            CASE_TEST_AUTHORIZATION,
            authTokenGenerator.generate(),
            TEST_USER,
            ST_CIC_JURISDICTION,
            ST_CIC_CASE_TYPE,
            TEST_CASE_ID.toString(),
            getExactAppsDetailsByCaseType(appsConfig, ST_CIC_CASE_TYPE).getEventIds().getUpdateEvent()
        )).thenReturn(eventRes);

        final String result = caseApiService.getEventTokenForUpdate(
            CASE_TEST_AUTHORIZATION,
            TEST_USER,
            cicAppDetails.getEventIds().getUpdateEvent(),
            TEST_CASE_ID.toString(),
            cicAppDetails
        );

        assertNotNull(result);
        assertEquals(eventRes.getToken(),result);
    }

    @Test
    void shouldUpdateCaseDataWithSubmitEvent() throws IOException {

        final String caseDataJson = loadJson(CASE_DATA_FILE_CIC);
        final CaseData caseData = mapper.readValue(caseDataJson, CaseData.class);

        final Map<String, Object> caseDataMap = new ConcurrentHashMap<>();

        caseDataMap.put(TEST_CASE_REFERENCE, caseData);
        final CaseDetails caseDetail = CaseDetails.builder().caseTypeId(CASE_DATA_CIC_ID)
            .id(TEST_CASE_ID)
            .data(caseDataMap)
            .jurisdiction(ST_CIC_JURISDICTION)
            .build();

        eventRes = StartEventResponse.builder()
            .eventId(getExactAppsDetailsByCaseType(appsConfig, ST_CIC_CASE_TYPE).getEventIds().getSubmitEvent())
            .caseDetails(caseDetail)
            .token(TEST_AUTHORIZATION_TOKEN)
            .build();

        when(idamService.retrieveUser(CASE_TEST_AUTHORIZATION)).thenReturn(user);

        when(user.getUserDetails()).thenReturn(userDetails);

        when(userDetails.getId()).thenReturn(TEST_USER);

        when(authTokenGenerator.generate()).thenReturn(TEST_USER);

        when(coreCaseDataApi.startEventForCitizen(
            CASE_TEST_AUTHORIZATION,
            authTokenGenerator.generate(),
            TEST_USER,
            ST_CIC_JURISDICTION,
            ST_CIC_CASE_TYPE,
            TEST_CASE_ID.toString(),
            getExactAppsDetailsByCaseType(appsConfig, ST_CIC_CASE_TYPE).getEventIds().getSubmitEvent()
        )).thenReturn(eventRes);

        final CaseDataContent caseDataContent = CaseDataContent.builder()
            .data(caseData)
            .event(Event.builder().id(getExactAppsDetailsByCaseType(
                appsConfig,
                ST_CIC_CASE_TYPE
            ).getEventIds().getSubmitEvent()).build())
            .eventToken(TEST_AUTHORIZATION_TOKEN)
            .build();

        when(coreCaseDataApi.submitEventForCitizen(
            CASE_TEST_AUTHORIZATION,
            authTokenGenerator.generate(),
            TEST_USER,
            ST_CIC_JURISDICTION,
            ST_CIC_CASE_TYPE,
            TEST_CASE_ID.toString(),
            true,
            caseDataContent
        )).thenReturn(caseDetail);

        final CaseDetails updateCaseDetails = caseApiService.updateCase(
            CASE_TEST_AUTHORIZATION,
            uk.gov.hmcts.sptribs.edgecase.event.Event.SUBMIT,
            TEST_CASE_ID,
            caseData,
            cicAppDetails);

        assertEquals(CASE_DATA_CIC_ID, updateCaseDetails.getCaseTypeId());
        assertEquals(updateCaseDetails.getId(), caseDetail.getId());
        assertEquals(updateCaseDetails.getCaseTypeId(), caseDetail.getCaseTypeId());
        assertEquals(updateCaseDetails.getData(), caseDetail.getData());
        assertEquals(updateCaseDetails.getData().get(TEST_CASE_REFERENCE), caseDataMap.get(TEST_CASE_REFERENCE));
    }

    @Test
    void shouldGetCaseDetails() throws Exception {
        final String caseDatajson = loadJson(CASE_DATA_FILE_CIC);
        final CaseData caseData = mapper.readValue(caseDatajson,CaseData.class);

        final Map<String, Object> caseDataMap = new ConcurrentHashMap<>();
        caseDataMap.put(TEST_CASE_REFERENCE, caseData);

        final CaseDetails caseDetail = CaseDetails.builder().caseTypeId(CASE_DATA_CIC_ID)
            .id(TEST_CASE_ID)
            .data(caseDataMap)
            .jurisdiction(ST_CIC_JURISDICTION)
            .build();

        when(coreCaseDataApi.getCase(
            CASE_TEST_AUTHORIZATION,
            authTokenGenerator.generate(),
            String.valueOf(TEST_CASE_ID)))
            .thenReturn(caseDetail);

        final CaseDetails result = caseApiService.getCaseDetails(
            CASE_TEST_AUTHORIZATION,
            TEST_CASE_ID);

        assertNotNull(result);
        assertEquals(result.getId(),caseDetail.getId());
        assertEquals(result.getCaseTypeId(),caseDetail.getCaseTypeId());
        assertEquals(result.getJurisdiction(),caseDetail.getJurisdiction());
        assertEquals(result.getData(),caseDetail.getData());
    }

    @Test
    void verifyDssUpdateCaseSubmissionEventIsStartedForCitizen() throws IOException {
        String caseDatajson = loadJson(CASE_DATA_FILE_CIC);
        CaseData caseData = mapper.readValue(caseDatajson,CaseData.class);

        Map<String, Object> caseDataMap = new ConcurrentHashMap<>();
        caseDataMap.put(TEST_CASE_REFERENCE, caseData);

        CaseDetails caseDetail = CaseDetails.builder().caseTypeId(CASE_DATA_CIC_ID)
            .id(TEST_CASE_ID)
            .data(caseDataMap)
            .jurisdiction(ST_CIC_JURISDICTION)
            .build();

        StartEventResponse startEventResponse = StartEventResponse.builder()
            .caseDetails(caseDetail)
            .eventId("citizen-cic-dss-update-case")
            .token("event token")
            .build();

        when(idamService.retrieveUser(CASE_TEST_AUTHORIZATION)).thenReturn(user);
        when(user.getUserDetails()).thenReturn(userDetails);
        when(userDetails.getId()).thenReturn(TEST_USER);
        when(coreCaseDataApi.startEventForCitizen(
            "testAuth",
            authTokenGenerator.generate(),
            "TestUser",
            "ST_CIC",
            "CriminalInjuriesCompensation",
            TEST_CASE_ID.toString(),
            "citizen-cic-dss-update-case")
        ).thenReturn(startEventResponse);

        caseApiService.updateCase(
            CASE_TEST_AUTHORIZATION,
            UPDATE_CASE,
            TEST_CASE_ID,
            caseData,
            cicAppDetails
        );

        verify(coreCaseDataApi).startEventForCitizen(
            "testAuth",
            authTokenGenerator.generate(),
            "TestUser",
            "ST_CIC",
            "CriminalInjuriesCompensation",
            TEST_CASE_ID.toString(),
            "citizen-cic-dss-update-case"
        );
    }
}
