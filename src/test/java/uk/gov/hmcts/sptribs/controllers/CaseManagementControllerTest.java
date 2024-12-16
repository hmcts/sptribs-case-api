package uk.gov.hmcts.sptribs.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import uk.gov.hmcts.sptribs.ciccase.model.CaseData;
import uk.gov.hmcts.sptribs.ciccase.model.DssCaseData;
import uk.gov.hmcts.sptribs.exception.CaseCreateOrUpdateException;
import uk.gov.hmcts.sptribs.model.CaseResponse;
import uk.gov.hmcts.sptribs.services.CaseManagementService;
import uk.gov.hmcts.sptribs.services.model.Event;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.sptribs.testutil.TestConstants.CASE_CREATE_FAILURE_MSG;
import static uk.gov.hmcts.sptribs.testutil.TestConstants.CASE_DATA_CIC_ID;
import static uk.gov.hmcts.sptribs.testutil.TestConstants.CASE_DATA_FILE_CIC;
import static uk.gov.hmcts.sptribs.testutil.TestConstants.CASE_FETCH_FAILURE_MSG;
import static uk.gov.hmcts.sptribs.testutil.TestConstants.CASE_TEST_AUTHORIZATION;
import static uk.gov.hmcts.sptribs.testutil.TestConstants.CASE_UPDATE_FAILURE_MSG;
import static uk.gov.hmcts.sptribs.testutil.TestConstants.TEST_CASE_EMAIL_ADDRESS;
import static uk.gov.hmcts.sptribs.testutil.TestConstants.TEST_CASE_ID;
import static uk.gov.hmcts.sptribs.testutil.TestConstants.TEST_UPDATE_CASE_EMAIL_ADDRESS;
import static uk.gov.hmcts.sptribs.testutil.TestFileUtil.loadJson;

@ExtendWith(SpringExtension.class)
class CaseManagementControllerTest {

    private final ObjectMapper mapper = new ObjectMapper().registerModule(new JavaTimeModule());

    @InjectMocks
    private CaseManagementController caseManagementController;

    @Mock
    private CaseManagementService caseManagementService;

    private AutoCloseable closeableMocks;
    private CaseData caseData;

    @BeforeEach
    void setUp() throws Exception {
        closeableMocks = MockitoAnnotations.openMocks(this);

        final String caseDataJson = loadJson(CASE_DATA_FILE_CIC);
        final DssCaseData dssCaseData = mapper.readValue(caseDataJson, DssCaseData.class);
        caseData = CaseData.builder().dssCaseData(dssCaseData).build();
    }

    @AfterEach
    void tearDown() throws Exception {
        closeableMocks.close();
    }

    @Test
    void cicCreateCaseDataIsSuccessful() {
        final Map<String, Object> caseDataMap = new ConcurrentHashMap<>();
        final CaseResponse caseResponse = CaseResponse.builder().caseData(caseDataMap).build();

        when(caseManagementService.createCase(eq(CASE_TEST_AUTHORIZATION), any(CaseData.class))).thenReturn(caseResponse);

        final ResponseEntity<?> createCaseResponse =
            caseManagementController.createCase(CASE_TEST_AUTHORIZATION, caseData.getDssCaseData());
        final CaseResponse testResponse = (CaseResponse) createCaseResponse.getBody();

        assertEquals(HttpStatus.OK, createCaseResponse.getStatusCode());
        assertNotNull(testResponse);
        assertThat(testResponse.getCaseData()).isEqualTo(caseDataMap);
    }

    @Test
    void cicCreateCaseDataThrowsCaseCreateOrUpdateException() {
        when(caseManagementService.createCase(eq(CASE_TEST_AUTHORIZATION), any(CaseData.class)))
            .thenThrow(new CaseCreateOrUpdateException(CASE_CREATE_FAILURE_MSG));
        final DssCaseData dssCaseData = caseData.getDssCaseData();
        final Exception exception = assertThrows(CaseCreateOrUpdateException.class,
            () -> caseManagementController.createCase(CASE_TEST_AUTHORIZATION, dssCaseData));
        assertThat(exception.getMessage()).contains(CASE_CREATE_FAILURE_MSG);
    }

    @Test
    void cicUpdateCaseDataIsSuccessful() {
        final Map<String, Object> caseDataMap = new ConcurrentHashMap<>();
        final CaseResponse caseResponse = CaseResponse.builder().caseData(caseDataMap).build();

        caseDataMap.put(CASE_DATA_CIC_ID, caseData);
        caseResponse.setId(TEST_CASE_ID);
        caseResponse.setStatus(null);

        when(caseManagementService.updateCase(eq(CASE_TEST_AUTHORIZATION), eq(Event.UPDATE),
            any(CaseData.class), eq(TEST_CASE_ID))).thenReturn(caseResponse);

        final ResponseEntity<?> preUpdateCaseResponse = caseManagementController.updateCase(
            TEST_CASE_ID,
            CASE_TEST_AUTHORIZATION,
            Event.UPDATE,
            caseData.getDssCaseData()
        );

        final CaseResponse testPreUpdResponse = (CaseResponse) preUpdateCaseResponse.getBody();
        assertEquals(TEST_CASE_EMAIL_ADDRESS, caseData.getDssCaseData().getSubjectEmailAddress());

        assertThat(testPreUpdResponse).isNotNull();
        final CaseData caseDataUpdate = (CaseData) testPreUpdResponse.getCaseData().get(CASE_DATA_CIC_ID);
        caseDataUpdate.getDssCaseData().setSubjectEmailAddress(TEST_UPDATE_CASE_EMAIL_ADDRESS);

        final ResponseEntity<?> postUpdateCaseResponse = caseManagementController.updateCase(
            TEST_CASE_ID,
            CASE_TEST_AUTHORIZATION,
            Event.UPDATE,
            caseDataUpdate.getDssCaseData()
        );

        final CaseResponse caseDataUpdateResponse = (CaseResponse) (postUpdateCaseResponse.getBody());
        assertThat(caseDataUpdateResponse).isNotNull();

        final CaseData caseDataUpdatedFromResponse = (CaseData) (caseDataUpdateResponse.getCaseData().get(CASE_DATA_CIC_ID));

        assertEquals(
            caseDataUpdatedFromResponse.getDssCaseData().getSubjectEmailAddress(),
            caseDataUpdate.getDssCaseData().getSubjectEmailAddress()
        );
        assertEquals(HttpStatus.OK, postUpdateCaseResponse.getStatusCode());
    }

    @Test
    void cicUpdateCaseDataThrowsCaseCreateOrUpdateException() {
        when(caseManagementService.updateCase(eq(CASE_TEST_AUTHORIZATION), eq(Event.UPDATE), any(CaseData.class), eq(TEST_CASE_ID)))
            .thenThrow(new CaseCreateOrUpdateException(CASE_UPDATE_FAILURE_MSG));
        final DssCaseData dssCaseData = caseData.getDssCaseData();
        final Exception exception = assertThrows(CaseCreateOrUpdateException.class, () ->
            caseManagementController.updateCase(
                TEST_CASE_ID,
                CASE_TEST_AUTHORIZATION,
                Event.UPDATE,
                dssCaseData));
        assertThat(exception.getMessage()).contains(CASE_UPDATE_FAILURE_MSG);
    }

    @Test
    void fetchCaseDetailsIsSuccessful() {
        final Map<String, Object> caseDataMap = new ConcurrentHashMap<>();
        caseDataMap.put(CASE_DATA_CIC_ID, caseData);

        final CaseResponse caseResponse = CaseResponse.builder().caseData(caseDataMap).build();
        caseResponse.setId(TEST_CASE_ID);

        when(caseManagementService.fetchCaseDetails(CASE_TEST_AUTHORIZATION,TEST_CASE_ID)).thenReturn(caseResponse);

        final ResponseEntity<?> postFetchCaseResponse = caseManagementController.fetchCaseDetails(
            TEST_CASE_ID,
            CASE_TEST_AUTHORIZATION
        );

        final CaseResponse caseDataFetchResponse = (CaseResponse) (postFetchCaseResponse.getBody());
        assertThat(caseDataFetchResponse).isNotNull();
        assertEquals(caseDataFetchResponse.getId(),caseResponse.getId());
        assertEquals(postFetchCaseResponse.getStatusCode(),HttpStatus.OK);
    }

    @Test
    void fetchCaseDetailsThrowsCreateCaseOrUpdateException() {
        when(caseManagementService.fetchCaseDetails(CASE_TEST_AUTHORIZATION,TEST_CASE_ID))
            .thenThrow(new CaseCreateOrUpdateException(CASE_FETCH_FAILURE_MSG));

        Exception exception = assertThrows(CaseCreateOrUpdateException.class, () ->
            caseManagementController.fetchCaseDetails(TEST_CASE_ID, CASE_TEST_AUTHORIZATION));
        assertThat(exception.getMessage()).contains(CASE_FETCH_FAILURE_MSG);
    }
}

