package uk.gov.hmcts.sptribs.caseworker.event.page;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.ccd.sdk.api.CaseDetails;
import uk.gov.hmcts.ccd.sdk.api.callback.AboutToStartOrSubmitResponse;
import uk.gov.hmcts.ccd.sdk.type.ListValue;
import uk.gov.hmcts.sptribs.caseworker.model.DraftOrderCIC;
import uk.gov.hmcts.sptribs.ciccase.model.CicCase;
import uk.gov.hmcts.sptribs.ciccase.model.State;
import uk.gov.hmcts.sptribs.ciccase.model.casetype.CriminalInjuriesCompensationData;

import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(MockitoExtension.class)
public class ShowDraftOrdersTest {

    @InjectMocks
    private ShowDraftOrders showDraftOrders;

    @Test
    void midEventSuccessful_whenAtLeastOneDraftRemoved() {
        DraftOrderCIC removedDraft = DraftOrderCIC.builder().build();

        ListValue<DraftOrderCIC> oldListValue = new ListValue<>();
        oldListValue.setId("1");
        oldListValue.setValue(removedDraft);

        CriminalInjuriesCompensationData oldData = CriminalInjuriesCompensationData.builder()
            .cicCase(CicCase.builder()
                .draftOrderCICList(List.of(oldListValue))
                .build())
            .build();

        CriminalInjuriesCompensationData newData = CriminalInjuriesCompensationData.builder()
            .cicCase(CicCase.builder()
                .draftOrderCICList(Collections.emptyList())
                .build())
            .build();

        CaseDetails<CriminalInjuriesCompensationData, State> details = new CaseDetails<>();
        details.setData(newData);

        CaseDetails<CriminalInjuriesCompensationData, State> detailsBefore = new CaseDetails<>();
        detailsBefore.setData(oldData);

        AboutToStartOrSubmitResponse<CriminalInjuriesCompensationData, State> response =
            showDraftOrders.midEvent(details, detailsBefore);

        assertThat(response.getErrors()).isEmpty();
        assertThat(response.getData().getCicCase().getRemovedDraftList()).isNotEmpty();
    }

    @Test
    void midEventReturnsErrorForNullDraftOrderList() {
        CaseDetails<CriminalInjuriesCompensationData, State> caseDetails = new CaseDetails<>();
        CicCase cicCase = CicCase.builder().build();
        cicCase.setDraftOrderCICList(Collections.emptyList());

        CriminalInjuriesCompensationData caseData = CriminalInjuriesCompensationData.builder()
            .cicCase(cicCase).build();

        caseDetails.setData(caseData);

        AboutToStartOrSubmitResponse<CriminalInjuriesCompensationData, State> response = showDraftOrders.midEvent(caseDetails, caseDetails);

        assertThat(response.getErrors()).hasSize(1);
        assertThat(response.getErrors()).contains("Please remove at least one draft to continue");
    }
}
