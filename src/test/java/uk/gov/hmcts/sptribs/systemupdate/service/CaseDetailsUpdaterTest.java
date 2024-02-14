package uk.gov.hmcts.sptribs.systemupdate.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.collections4.map.HashedMap;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.ccd.client.model.StartEventResponse;
import uk.gov.hmcts.sptribs.ciccase.model.CaseData;
import uk.gov.hmcts.sptribs.ciccase.model.State;
import uk.gov.hmcts.sptribs.ciccase.task.CaseTask;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;

@ExtendWith(MockitoExtension.class)
class CaseDetailsUpdaterTest {

    @Spy
    private ObjectMapper objectMapper = new ObjectMapper();

    @InjectMocks
    private CaseDetailsUpdater caseDetailsUpdater;

    @Test
    void shouldReturnUpdatedCaseDetailsFromStartEventResponse() {
        //Given
        final LocalDate now = LocalDate.now();
        final CaseTask caseTask = caseDetails -> {
            caseDetails.getData().setDueDate(now);
            return caseDetails;
        };
        final StartEventResponse startEventResponse = StartEventResponse.builder()
            .caseDetails(CaseDetails.builder()
                .data(new HashedMap<>())
                .build())
            .build();

        //When
        final uk.gov.hmcts.ccd.sdk.api.CaseDetails<CaseData, State> caseDetails = caseDetailsUpdater.updateCaseData(
            caseTask,
            startEventResponse);

        //When
        assertThat(caseDetails.getData().getDueDate()).isEqualTo(now);
        assertEquals(caseDetails.getId(),startEventResponse.getCaseDetails().getId());
        assertEquals(caseDetails.getCaseTypeId(),startEventResponse.getCaseDetails().getCaseTypeId());
        assertEquals(caseDetails.getJurisdiction(),startEventResponse.getCaseDetails().getJurisdiction());
    }
}
