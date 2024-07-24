package uk.gov.hmcts.sptribs.exception;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import uk.gov.hmcts.sptribs.ciccase.model.CaseData;
import uk.gov.hmcts.sptribs.ciccase.model.DssCaseData;
import uk.gov.hmcts.sptribs.controllers.CaseManagementController;
import uk.gov.hmcts.sptribs.edgecase.event.Event;
import uk.gov.hmcts.sptribs.services.CaseManagementService;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.sptribs.testutil.TestConstants.CASE_CREATE_FAILURE_MSG;
import static uk.gov.hmcts.sptribs.testutil.TestConstants.CASE_DATA_FILE_CIC;
import static uk.gov.hmcts.sptribs.testutil.TestConstants.CASE_FETCH_FAILURE_MSG;
import static uk.gov.hmcts.sptribs.testutil.TestConstants.CASE_TEST_AUTHORIZATION;
import static uk.gov.hmcts.sptribs.testutil.TestConstants.CASE_UPDATE_FAILURE_MSG;
import static uk.gov.hmcts.sptribs.testutil.TestConstants.TEST_CASE_ID;
import static uk.gov.hmcts.sptribs.testutil.TestFileUtil.loadJson;

@ExtendWith(SpringExtension.class)
class CaseCreateOrUpdateExceptionTest {

    private final ObjectMapper mapper = new ObjectMapper().registerModule(new JavaTimeModule());

    @InjectMocks
    private CaseManagementController caseManagementController;

    @Mock
    private CaseManagementService caseManagementService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void createCaseDataCaseCreateUpdateException() throws IOException {
        final String createCaseTestAuth = CASE_TEST_AUTHORIZATION;
        final String caseDataJson = loadJson(CASE_DATA_FILE_CIC);
        final DssCaseData dssCaseData = mapper.readValue(caseDataJson, DssCaseData.class);

        when(caseManagementController.createCase(createCaseTestAuth, dssCaseData))
            .thenThrow(new CaseCreateOrUpdateException(CASE_CREATE_FAILURE_MSG, new Throwable()));

        final CaseCreateOrUpdateException caseCreateOrUpdateException =
            assertThrows(CaseCreateOrUpdateException.class, () ->
                caseManagementController.createCase(createCaseTestAuth, dssCaseData));

        assertTrue(caseCreateOrUpdateException.getMessage().contains(CASE_CREATE_FAILURE_MSG));
        assertNotNull(caseCreateOrUpdateException.getCause());
    }

    @Test
    void updateCaseDataCaseCreateUpdateException() throws IOException {
        final String updateCaseTestAuth = CASE_TEST_AUTHORIZATION;
        final String caseDataJson = loadJson(CASE_DATA_FILE_CIC);
        final CaseData caseData = mapper.readValue(caseDataJson, CaseData.class);

        when(caseManagementService.updateCase(updateCaseTestAuth, Event.UPDATE, caseData,TEST_CASE_ID))
            .thenThrow(new CaseCreateOrUpdateException(CASE_UPDATE_FAILURE_MSG, new Throwable()));

        final CaseCreateOrUpdateException caseCreateOrUpdateException =
            assertThrows(CaseCreateOrUpdateException.class, () ->
                caseManagementService.updateCase(updateCaseTestAuth, Event.UPDATE, caseData, TEST_CASE_ID));

        assertTrue(caseCreateOrUpdateException.getMessage().contains(CASE_UPDATE_FAILURE_MSG));
        assertNotNull(caseCreateOrUpdateException.getCause());
    }

    @Test
    void fetchCaseDataCaseCreateUpdateException() {
        final String fetchCaseTestAuth = CASE_TEST_AUTHORIZATION;

        when(caseManagementService.fetchCaseDetails(fetchCaseTestAuth,TEST_CASE_ID))
            .thenThrow(new CaseCreateOrUpdateException(CASE_FETCH_FAILURE_MSG, new Throwable()));

        final CaseCreateOrUpdateException caseCreateOrUpdateException =
            assertThrows(CaseCreateOrUpdateException.class, () ->
                caseManagementController.fetchCaseDetails(TEST_CASE_ID,fetchCaseTestAuth));

        assertTrue(caseCreateOrUpdateException.getMessage().contains(CASE_FETCH_FAILURE_MSG));
        assertNotNull(caseCreateOrUpdateException.getCause());
    }
}
