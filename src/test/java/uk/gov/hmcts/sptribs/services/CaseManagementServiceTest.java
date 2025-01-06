package uk.gov.hmcts.sptribs.services;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import uk.gov.hmcts.reform.authorisation.generators.AuthTokenGenerator;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.sptribs.ciccase.model.CaseData;
import uk.gov.hmcts.sptribs.ciccase.model.DssCaseData;
import uk.gov.hmcts.sptribs.common.config.AppsConfig;
import uk.gov.hmcts.sptribs.constants.CommonConstants;
import uk.gov.hmcts.sptribs.exception.CaseCreateOrUpdateException;
import uk.gov.hmcts.sptribs.model.CaseResponse;
import uk.gov.hmcts.sptribs.services.ccd.CaseApiService;
import uk.gov.hmcts.sptribs.services.model.Event;
import uk.gov.hmcts.sptribs.util.AppsUtil;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.sptribs.testutil.TestConstants.CASE_CREATE_FAILURE_MSG;
import static uk.gov.hmcts.sptribs.testutil.TestConstants.CASE_DATA_CIC_ID;
import static uk.gov.hmcts.sptribs.testutil.TestConstants.CASE_DATA_FILE_CIC;
import static uk.gov.hmcts.sptribs.testutil.TestConstants.CASE_FETCH_FAILURE_MSG;
import static uk.gov.hmcts.sptribs.testutil.TestConstants.CASE_TEST_AUTHORIZATION;
import static uk.gov.hmcts.sptribs.testutil.TestConstants.CASE_UPDATE_FAILURE_MSG;
import static uk.gov.hmcts.sptribs.testutil.TestConstants.RESPONSE_STATUS_SUCCESS;
import static uk.gov.hmcts.sptribs.testutil.TestConstants.TEST_CASE_ID;
import static uk.gov.hmcts.sptribs.testutil.TestConstants.TEST_UPDATE_CASE_EMAIL_ADDRESS;
import static uk.gov.hmcts.sptribs.testutil.TestConstants.TEST_USER;
import static uk.gov.hmcts.sptribs.testutil.TestFileUtil.loadJson;

@ExtendWith(SpringExtension.class)
class CaseManagementServiceTest {
    private final ObjectMapper mapper = new ObjectMapper().registerModule(new JavaTimeModule());

    @InjectMocks
    private CaseManagementService caseManagementService;

    @Mock
    private AppsConfig appsConfig;

    @Mock
    private AuthTokenGenerator authTokenGenerator;

    @Mock
    private AppsConfig.AppsDetails cicAppDetail;

    @Mock
    CaseApiService caseApiService;

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);

        cicAppDetail = new AppsConfig.AppsDetails();
        cicAppDetail.setCaseType(CommonConstants.ST_CIC_CASE_TYPE);
        cicAppDetail.setJurisdiction(CommonConstants.ST_CIC_JURISDICTION);
        cicAppDetail.setCaseTypeOfApplication(List.of(CASE_DATA_CIC_ID));
    }

    @Test
    void shouldCreateCicCaseData() throws Exception {
        final String caseDataJson = loadJson(CASE_DATA_FILE_CIC);
        final CaseData caseData = mapper.readValue(caseDataJson, CaseData.class);

        final Map<String, Object> caseDataMap = new ConcurrentHashMap<>();
        caseDataMap.put(CASE_DATA_CIC_ID, caseData);

        final AppsConfig.EventsConfig eventsConfig = new AppsConfig.EventsConfig();
        eventsConfig.setCreateEvent("citizen-cic-create-dss-application");

        cicAppDetail.setEventIds(eventsConfig);
        when(appsConfig.getApps()).thenReturn(List.of(cicAppDetail));


        when(authTokenGenerator.generate()).thenReturn(TEST_USER);

        final CaseDetails caseDetail = CaseDetails.builder().caseTypeId(CASE_DATA_CIC_ID)
            .id(TEST_CASE_ID)
            .jurisdiction(CommonConstants.ST_CIC_JURISDICTION)
            .data(caseDataMap)
            .build();

        when(caseApiService.createCase(CASE_TEST_AUTHORIZATION, caseData, cicAppDetail)).thenReturn(caseDetail);

        final CaseResponse caseResponse = CaseResponse.builder().caseData(caseDataMap).build();
        final DssCaseData dssCaseData =  mapper.readValue(caseDataJson, DssCaseData.class);
        caseData.setDssCaseData(dssCaseData);
        final CaseResponse createCaseResponse = caseManagementService.createCase(CASE_TEST_AUTHORIZATION, caseData);
        final CaseData caseResponseData = (CaseData) createCaseResponse.getCaseData().get(CASE_DATA_CIC_ID);

        assertNotNull(cicAppDetail);
        assertNotNull(createCaseResponse);
        assertEquals(createCaseResponse.getCaseData(), caseResponse.getCaseData());
        assertEquals(createCaseResponse.getId(), caseDetail.getId());
        assertTrue(createCaseResponse.getCaseData().containsKey(CASE_DATA_CIC_ID));
        assertEquals(createCaseResponse.getCaseData().get(CASE_DATA_CIC_ID), caseDetail.getData().get(CASE_DATA_CIC_ID));
        assertEquals(caseResponseData.getDssCaseData().getSubjectFullName(), caseData.getDssCaseData().getSubjectFullName());
        assertEquals(caseResponseData.getDssCaseData().getCaseTypeOfApplication(), caseData.getDssCaseData().getCaseTypeOfApplication());
        assertEquals(RESPONSE_STATUS_SUCCESS, createCaseResponse.getStatus());
    }

    @Test
    void shouldCreateCaseCicFailsWhenCaseTypeIsInvalid() throws Exception {

        final String caseDataJson = loadJson(CASE_DATA_FILE_CIC);
        final CaseData caseData = mapper.readValue(caseDataJson, CaseData.class);
        final DssCaseData dssCaseData = DssCaseData.builder().caseTypeOfApplication(CASE_DATA_CIC_ID).build();

        when(AppsUtil.isValidCaseTypeOfApplication(appsConfig,dssCaseData))
            .thenThrow(new CaseCreateOrUpdateException("Invalid Case type application. Please check the request."));

        final CaseCreateOrUpdateException caseCreateOrUpdateException =
            assertThrows(CaseCreateOrUpdateException.class, () ->
            caseManagementService.createCase(CASE_TEST_AUTHORIZATION, caseData));

        assertNotNull(caseCreateOrUpdateException.getCause());
        assertTrue(caseCreateOrUpdateException.getCause() instanceof RuntimeException);
        assertTrue(caseCreateOrUpdateException.getMessage().contains("Invalid Case type application. Please check the request."));
    }

    @Test
    void shouldCreateCaseCicFailedWithCaseCreateUpdateException() throws Exception {

        final AppsConfig.EventsConfig eventsConfig = new AppsConfig.EventsConfig();
        eventsConfig.setCreateEvent("citizen-cic-create-dss-application");

        cicAppDetail.setEventIds(eventsConfig);
        when(appsConfig.getApps()).thenReturn(List.of(cicAppDetail));


        when(authTokenGenerator.generate()).thenReturn(TEST_USER);

        final String caseDataJson = loadJson(CASE_DATA_FILE_CIC);

        final CaseData caseData = mapper.readValue(caseDataJson, CaseData.class);

        when(caseApiService.createCase(CASE_TEST_AUTHORIZATION, caseData, cicAppDetail)).thenThrow(
            new CaseCreateOrUpdateException(CASE_CREATE_FAILURE_MSG, new RuntimeException()
            ));

        final CaseCreateOrUpdateException caseCreateOrUpdateException =
            assertThrows(CaseCreateOrUpdateException.class, () ->
            caseManagementService.createCase(CASE_TEST_AUTHORIZATION, caseData));

        assertNotNull(cicAppDetail);
        assertNotNull(caseCreateOrUpdateException.getCause());
        assertTrue(caseCreateOrUpdateException.getCause() instanceof RuntimeException);
        assertTrue(caseCreateOrUpdateException.getMessage().contains(CASE_CREATE_FAILURE_MSG));
    }

    @Test
    void shouldUpdateCicCaseData() throws Exception {
        final String caseDataJson = loadJson(CASE_DATA_FILE_CIC);
        final CaseData caseData = mapper.readValue(caseDataJson, CaseData.class);
        final DssCaseData dssCaseData = mapper.readValue(caseDataJson, DssCaseData.class);

        final AppsConfig.EventsConfig eventsConfig = new AppsConfig.EventsConfig();
        eventsConfig.setUpdateEvent("citizen-cic-update-dss-application");

        cicAppDetail.setEventIds(eventsConfig);

        final String origEmailAddress = caseData.getDssCaseData().getSubjectEmailAddress();
        dssCaseData.setSubjectEmailAddress(TEST_UPDATE_CASE_EMAIL_ADDRESS);
        assertNotEquals(dssCaseData.getSubjectEmailAddress(), origEmailAddress);

        when(appsConfig.getApps()).thenReturn(List.of(cicAppDetail));

        assertNotNull(cicAppDetail);

        when(authTokenGenerator.generate()).thenReturn(TEST_USER);

        final Map<String, Object> caseDataMap = new ConcurrentHashMap<>();
        caseDataMap.put(CASE_DATA_CIC_ID, caseData);

        final CaseDetails caseDetail = CaseDetails.builder().caseTypeId(CASE_DATA_CIC_ID)
            .id(TEST_CASE_ID)
            .data(caseDataMap)
            .build();

        when(caseApiService.updateCase(
            CASE_TEST_AUTHORIZATION,
            Event.UPDATE,
            TEST_CASE_ID,
            caseData,
            cicAppDetail
        )).thenReturn(caseDetail);

        caseData.setDssCaseData(dssCaseData);

        final CaseResponse updateCaseResponse = caseManagementService.updateCase(
            CASE_TEST_AUTHORIZATION,
            Event.UPDATE,caseData,
            TEST_CASE_ID);
        final CaseData caseResponseData = (CaseData) updateCaseResponse.getCaseData().get(CASE_DATA_CIC_ID);

        assertEquals(updateCaseResponse.getId(), caseDetail.getId());
        assertTrue(updateCaseResponse.getCaseData().containsKey(CASE_DATA_CIC_ID));
        assertNotEquals(caseResponseData.getDssCaseData().getSubjectEmailAddress(), origEmailAddress);
        assertNotNull(updateCaseResponse);
        assertEquals(updateCaseResponse.getCaseData().get(CASE_DATA_CIC_ID), caseDetail.getData().get(CASE_DATA_CIC_ID));
        assertEquals(caseResponseData.getDssCaseData().getSubjectFullName(), caseData.getDssCaseData().getSubjectFullName());
        assertEquals(caseResponseData.getDssCaseData().getCaseTypeOfApplication(), caseData.getDssCaseData().getCaseTypeOfApplication());
        assertEquals(RESPONSE_STATUS_SUCCESS, updateCaseResponse.getStatus());
    }

    @Test
    void shouldUpdateCaseCicFailedWithCaseCreateUpdateException() throws Exception {
        final AppsConfig.EventsConfig eventsConfig = new AppsConfig.EventsConfig();
        eventsConfig.setUpdateEvent("citizen-cic-update-dss-application");

        cicAppDetail.setEventIds(eventsConfig);
        when(appsConfig.getApps()).thenReturn(List.of(cicAppDetail));

        assertNotNull(cicAppDetail);

        final String caseDataJson = loadJson(CASE_DATA_FILE_CIC);

        final CaseData caseData = mapper.readValue(caseDataJson, CaseData.class);
        final DssCaseData dssCaseData = mapper.readValue(caseDataJson, DssCaseData.class);

        when(authTokenGenerator.generate()).thenReturn(TEST_USER);
        when(caseApiService.updateCase(
            CASE_TEST_AUTHORIZATION,
            Event.UPDATE,
            TEST_CASE_ID,
            caseData,
            cicAppDetail
        )).thenThrow(
            new CaseCreateOrUpdateException(
                CASE_UPDATE_FAILURE_MSG,
                new RuntimeException()
            ));

        caseData.setDssCaseData(dssCaseData);

        final CaseCreateOrUpdateException caseCreateOrUpdateException =
            assertThrows(CaseCreateOrUpdateException.class, () ->
            caseManagementService.updateCase(CASE_TEST_AUTHORIZATION, Event.UPDATE, caseData, TEST_CASE_ID));

        assertNotNull(caseCreateOrUpdateException.getCause());
        assertTrue(caseCreateOrUpdateException.getCause() instanceof RuntimeException);
        assertTrue(caseCreateOrUpdateException.getMessage().contains(CASE_UPDATE_FAILURE_MSG));
    }

    @Test
    void shouldUpdateCaseCicFailsWhenCaseTypeIsInvalid() throws Exception {

        final String caseDataJson = loadJson(CASE_DATA_FILE_CIC);
        final CaseData caseData = mapper.readValue(caseDataJson, CaseData.class);
        final DssCaseData dssCaseData = DssCaseData.builder().caseTypeOfApplication(CASE_DATA_CIC_ID).build();

        when(AppsUtil.isValidCaseTypeOfApplication(appsConfig,dssCaseData))
            .thenThrow(new CaseCreateOrUpdateException("Invalid Case type application. Please check the request."));

        final CaseCreateOrUpdateException caseCreateOrUpdateException =
            assertThrows(CaseCreateOrUpdateException.class, () ->
                caseManagementService.updateCase(CASE_TEST_AUTHORIZATION,Event.UPDATE, caseData,TEST_CASE_ID));

        assertNotNull(caseCreateOrUpdateException.getCause());
        assertTrue(caseCreateOrUpdateException.getCause() instanceof RuntimeException);
        assertTrue(caseCreateOrUpdateException.getMessage().contains("Invalid Case type application. Please check the request."));
    }

    @Test
    void shouldFetchCaseDetail() throws Exception {
        final String caseDataJson = loadJson(CASE_DATA_FILE_CIC);
        final CaseData caseData = mapper.readValue(caseDataJson, CaseData.class);
        final DssCaseData dssCaseData = mapper.readValue(caseDataJson, DssCaseData.class);

        final String origEmailAddress = caseData.getDssCaseData().getSubjectEmailAddress();
        dssCaseData.setSubjectEmailAddress(TEST_UPDATE_CASE_EMAIL_ADDRESS);
        assertNotEquals(dssCaseData.getSubjectEmailAddress(), origEmailAddress);

        when(authTokenGenerator.generate()).thenReturn(TEST_USER);

        final Map<String, Object> caseDataMap = new ConcurrentHashMap<>();
        caseDataMap.put(CASE_DATA_CIC_ID, caseData);

        final CaseDetails caseDetail = CaseDetails.builder()
            .id(TEST_CASE_ID)
            .data(caseDataMap)
            .build();

        when(caseApiService.getCaseDetails(CASE_TEST_AUTHORIZATION,TEST_CASE_ID))
            .thenReturn(caseDetail);

        final CaseResponse fetchCaseResponse = caseManagementService.fetchCaseDetails(
            CASE_TEST_AUTHORIZATION,
            TEST_CASE_ID);

        assertNotNull(fetchCaseResponse);
        assertEquals(fetchCaseResponse.getId(), caseDetail.getId());
        assertTrue(fetchCaseResponse.getCaseData().containsKey(CASE_DATA_CIC_ID));
        assertEquals(fetchCaseResponse.getCaseData().get(CASE_DATA_CIC_ID), caseDetail.getData().get(CASE_DATA_CIC_ID));
        assertEquals(RESPONSE_STATUS_SUCCESS, fetchCaseResponse.getStatus());
    }

    @Test
    void shouldFetchCaseDetailsWithException() {

        when(caseApiService.getCaseDetails(CASE_TEST_AUTHORIZATION,TEST_CASE_ID))
            .thenThrow(new CaseCreateOrUpdateException(
                CASE_FETCH_FAILURE_MSG, new RuntimeException()
            ));

        final CaseCreateOrUpdateException caseCreateOrUpdateException =
            assertThrows(CaseCreateOrUpdateException.class, () ->
            caseManagementService.fetchCaseDetails(CASE_TEST_AUTHORIZATION,TEST_CASE_ID));

        assertNotNull(caseCreateOrUpdateException.getCause());
        assertTrue(caseCreateOrUpdateException.getCause() instanceof RuntimeException);
        assertTrue(caseCreateOrUpdateException.getMessage().contains(CASE_FETCH_FAILURE_MSG));
    }
}
