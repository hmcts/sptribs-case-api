package uk.gov.hmcts.sptribs.systemupdate.schedule.migration.task;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.ccd.sdk.api.CaseDetails;
import uk.gov.hmcts.sptribs.ciccase.model.CaseData;
import uk.gov.hmcts.sptribs.ciccase.model.State;
import uk.gov.hmcts.sptribs.testutil.TestDataHelper;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;


@ExtendWith(MockitoExtension.class)
class MigrateRetiredFieldsTest {

    @Spy
    ObjectMapper objectMapper = new ObjectMapper();

    @InjectMocks
    private MigrateRetiredFields migrateRetiredFields;

    @Test
    void shouldMigrateFields() {
        final CaseData caseData = TestDataHelper.awaitingOutcomeData();

        final CaseDetails<CaseData, State> caseDetails = new CaseDetails<>();
        caseDetails.setData(caseData);

        final CaseDetails<CaseData, State> result = migrateRetiredFields.apply(caseDetails);

        assertNotNull(result);
        assertEquals(State.AwaitingOutcome, result.getData().getCaseStatus());
        assertEquals(caseDetails.getData(), result.getData());

    }
}
