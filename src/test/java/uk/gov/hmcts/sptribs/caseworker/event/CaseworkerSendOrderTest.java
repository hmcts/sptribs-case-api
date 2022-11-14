package uk.gov.hmcts.sptribs.caseworker.event;


import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.ccd.sdk.ConfigBuilderImpl;
import uk.gov.hmcts.ccd.sdk.api.CaseDetails;
import uk.gov.hmcts.ccd.sdk.api.Event;
import uk.gov.hmcts.ccd.sdk.api.callback.AboutToStartOrSubmitResponse;
import uk.gov.hmcts.ccd.sdk.type.ListValue;
import uk.gov.hmcts.reform.ccd.client.model.SubmittedCallbackResponse;
import uk.gov.hmcts.sptribs.caseworker.model.DateModel;
import uk.gov.hmcts.sptribs.caseworker.model.SendOrder;
import uk.gov.hmcts.sptribs.ciccase.model.CaseData;
import uk.gov.hmcts.sptribs.ciccase.model.State;
import uk.gov.hmcts.sptribs.ciccase.model.UserRole;

import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static uk.gov.hmcts.sptribs.caseworker.event.CaseworkerSendOrder.CASEWORKER_SEND_ORDER;
import static uk.gov.hmcts.sptribs.testutil.ConfigTestUtil.createCaseDataConfigBuilder;
import static uk.gov.hmcts.sptribs.testutil.ConfigTestUtil.getEventsFrom;
import static uk.gov.hmcts.sptribs.testutil.TestConstants.TEST_CASE_ID;
import static uk.gov.hmcts.sptribs.testutil.TestDataHelper.LOCAL_DATE_TIME;
import static uk.gov.hmcts.sptribs.testutil.TestDataHelper.caseData;


@ExtendWith(MockitoExtension.class)
class CaseworkerSendOrderTest {
    @InjectMocks
    private CaseworkerSendOrder caseworkerSendOrder;

    @Test
    void shouldAddConfigurationToConfigBuilder() {
        //Given
        final ConfigBuilderImpl<CaseData, State, UserRole> configBuilder = createCaseDataConfigBuilder();

        //When
        caseworkerSendOrder.configure(configBuilder);

        //Then
        assertThat(getEventsFrom(configBuilder).values())
            .extracting(Event::getId)
            .contains(CASEWORKER_SEND_ORDER);
    }

    @Test
    void shouldSuccessfullySendOrder() {
        //Given

        final DateModel dateModel = DateModel.builder().dueDate(LocalDate.now()).build();
        final ListValue<DateModel> dates = new ListValue<>();
        dates.setValue(dateModel);
        final SendOrder sendOrder = new SendOrder();
        sendOrder.setDueDates(List.of(dates));
        final CaseData caseData = caseData();
        caseData.setSendOrder(sendOrder);
        final CaseDetails<CaseData, State> updatedCaseDetails = new CaseDetails<>();
        final CaseDetails<CaseData, State> beforeDetails = new CaseDetails<>();
        updatedCaseDetails.setData(caseData);
        updatedCaseDetails.setId(TEST_CASE_ID);
        updatedCaseDetails.setCreatedDate(LOCAL_DATE_TIME);

        //When
        AboutToStartOrSubmitResponse<CaseData, State> response =
            caseworkerSendOrder.aboutToSubmit(updatedCaseDetails, beforeDetails);
        SubmittedCallbackResponse sent = caseworkerSendOrder.sent(updatedCaseDetails, beforeDetails);

        //Then
        assertThat(sent).isNotNull();
        assertThat(response).isNotNull();
        SendOrder order = response.getData().getSendOrder();
        assertThat(order.getDueDates().get(0).getValue().getDueDate()).isNotNull();
    }

}
