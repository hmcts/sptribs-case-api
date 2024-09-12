package uk.gov.hmcts.sptribs.systemupdate.service;

import org.apache.commons.collections4.map.HashedMap;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.ccd.client.model.StartEventResponse;
import uk.gov.hmcts.sptribs.ciccase.model.CaseData;
import uk.gov.hmcts.sptribs.ciccase.model.State;
import uk.gov.hmcts.sptribs.ciccase.task.CaseTask;

import java.time.LocalDate;

@ExtendWith(SpringExtension.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
public class CaseDetailsUpdaterIT {

    @Autowired
    private CaseDetailsUpdater caseDetailsUpdater;

    @Test
    void shouldUpdateCaseData() {
        final CaseTask caseTask = caseDetails -> {
            caseDetails.getData().setDueDate(LocalDate.of(2023, 1, 2));
            return caseDetails;
        };

        final StartEventResponse startEventResponse = StartEventResponse.builder()
            .caseDetails(CaseDetails.builder()
                .data(new HashedMap<>())
                .build())
            .build();

        uk.gov.hmcts.ccd.sdk.api.CaseDetails<CaseData, State> caseDetails = caseDetailsUpdater.updateCaseData(caseTask, startEventResponse);

    }
}
