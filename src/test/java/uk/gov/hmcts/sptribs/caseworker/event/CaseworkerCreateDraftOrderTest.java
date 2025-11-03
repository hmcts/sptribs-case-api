package uk.gov.hmcts.sptribs.caseworker.event;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.ccd.sdk.ConfigBuilderImpl;
import uk.gov.hmcts.ccd.sdk.api.CaseDetails;
import uk.gov.hmcts.ccd.sdk.api.Event;
import uk.gov.hmcts.ccd.sdk.api.callback.AboutToStartOrSubmitResponse;
import uk.gov.hmcts.ccd.sdk.type.Document;
import uk.gov.hmcts.ccd.sdk.type.DynamicList;
import uk.gov.hmcts.ccd.sdk.type.DynamicListElement;
import uk.gov.hmcts.ccd.sdk.type.ListValue;
import uk.gov.hmcts.reform.ccd.client.model.SubmittedCallbackResponse;
import uk.gov.hmcts.sptribs.caseworker.model.DraftOrderCIC;
import uk.gov.hmcts.sptribs.caseworker.model.DraftOrderContentCIC;
import uk.gov.hmcts.sptribs.caseworker.service.OrderService;
import uk.gov.hmcts.sptribs.ciccase.model.CaseData;
import uk.gov.hmcts.sptribs.ciccase.model.CicCase;
import uk.gov.hmcts.sptribs.ciccase.model.OrderTemplate;
import uk.gov.hmcts.sptribs.ciccase.model.State;
import uk.gov.hmcts.sptribs.ciccase.model.UserRole;
import uk.gov.hmcts.sptribs.ciccase.model.access.Permissions;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static uk.gov.hmcts.sptribs.caseworker.util.EventConstants.DOUBLE_HYPHEN;
import static uk.gov.hmcts.sptribs.ciccase.model.UserRole.ST_CIC_WA_CONFIG_USER;
import static uk.gov.hmcts.sptribs.testutil.ConfigTestUtil.createCaseDataConfigBuilder;
import static uk.gov.hmcts.sptribs.testutil.ConfigTestUtil.getEventsFrom;
import static uk.gov.hmcts.sptribs.testutil.TestConstants.TEST_CASE_ID;
import static uk.gov.hmcts.sptribs.testutil.TestDataHelper.LOCAL_DATE_TIME;
import static uk.gov.hmcts.sptribs.testutil.TestDataHelper.caseData;
import static uk.gov.hmcts.sptribs.testutil.TestEventConstants.CASEWORKER_CREATE_DRAFT_ORDER;

@ExtendWith(MockitoExtension.class)
class CaseworkerCreateDraftOrderTest {

    @InjectMocks
    private CaseworkerCreateDraftOrder caseworkerCreateDraftOrder;

    @Mock
    private OrderService orderService;

    @Test
    void shouldAddPublishToCamundaWhenWAIsEnabled() {

        final ConfigBuilderImpl<CaseData, State, UserRole> configBuilder = createCaseDataConfigBuilder();

        caseworkerCreateDraftOrder.configure(configBuilder);

        assertThat(getEventsFrom(configBuilder).values())
            .extracting(Event::getId)
            .contains(CASEWORKER_CREATE_DRAFT_ORDER);

        assertThat(getEventsFrom(configBuilder).values())
            .extracting(Event::isPublishToCamunda)
            .contains(true);

        assertThat(getEventsFrom(configBuilder).values())
                .extracting(Event::getGrants)
                .extracting(map -> map.containsKey(ST_CIC_WA_CONFIG_USER))
                .contains(true);

        assertThat(getEventsFrom(configBuilder).values())
                .extracting(Event::getGrants)
                .extracting(map -> map.get(ST_CIC_WA_CONFIG_USER))
                .contains(Permissions.CREATE_READ_UPDATE);
    }

    @Test
    void aboutToStartShouldSetCurrentEvent() {
        CaseDetails<CaseData, State> caseDetails = CaseDetails.<CaseData, State>builder()
            .data(CaseData.builder().build())
            .build();
        AboutToStartOrSubmitResponse<CaseData, State> response = caseworkerCreateDraftOrder.aboutToStart(caseDetails);

        assertThat(response).isNotNull();
        assertThat(response.getData().getCurrentEvent()).isEqualTo(CASEWORKER_CREATE_DRAFT_ORDER);
    }

    @Test
    void shouldSuccessfullySaveDraftOrder() {

        //Given
        final CicCase cicCase = CicCase.builder()
            .orderTemplateIssued(Document.builder().filename("a--b--02-02-2002 11:11:11.pdf").build()).build();
        final CaseData caseData = caseData();
        caseData.setCicCase(cicCase);
        final CaseDetails<CaseData, State> updatedCaseDetails = new CaseDetails<>();
        final CaseDetails<CaseData, State> beforeDetails = new CaseDetails<>();

        updatedCaseDetails.setData(caseData);
        updatedCaseDetails.setId(TEST_CASE_ID);
        updatedCaseDetails.setCreatedDate(LOCAL_DATE_TIME);
        caseData.setDraftOrderContentCIC(DraftOrderContentCIC.builder()
            .orderTemplate(OrderTemplate.CIC6_GENERAL_DIRECTIONS).build());

        final DraftOrderCIC expectedDraftOrderCIC = DraftOrderCIC.builder()
            .draftOrderContentCIC(DraftOrderContentCIC.builder()
                .orderTemplate(OrderTemplate.CIC6_GENERAL_DIRECTIONS).build())
            .templateGeneratedDocument(Document.builder().filename("a--b--02-02-2002 11:11:11.pdf").build())
            .build();

        //When
        AboutToStartOrSubmitResponse<CaseData, State> response =
            caseworkerCreateDraftOrder.aboutToSubmit(updatedCaseDetails, beforeDetails);
        SubmittedCallbackResponse draftCreatedResponse = caseworkerCreateDraftOrder.submitted(updatedCaseDetails, beforeDetails);

        //  Then
        assertThat(response).isNotNull();
        CaseData responseData = response.getData();
        assertThat(responseData.getCicCase().getDraftOrderCICList()).hasSize(1);
        assertThat(responseData.getCicCase().getDraftOrderCICList().getFirst().getValue()).isEqualTo(expectedDraftOrderCIC);

        assertThat(draftCreatedResponse).isNotNull();
        assertThat(draftCreatedResponse.getConfirmationHeader()).isEqualTo("# Draft order created.");
    }

    @Test
    void shouldSuccessfullyShowPreviewOrderWithTemplate() {

        //Given
        DraftOrderContentCIC orderContentCIC = DraftOrderContentCIC.builder()
            .orderTemplate(OrderTemplate.CIC6_GENERAL_DIRECTIONS).build();

        final CicCase cicCase = CicCase.builder()
            .orderTemplateIssued(Document.builder().filename("a--b--02-02-2002 11:11:11.pdf").build()).build();
        final CaseData caseData = CaseData.builder()
            .draftOrderContentCIC(orderContentCIC)
            .build();
        caseData.setCicCase(cicCase);
        final CaseDetails<CaseData, State> caseDetails = new CaseDetails<>();
        caseDetails.setId(TEST_CASE_ID);

        caseDetails.setData(caseData);

        //When
        AboutToStartOrSubmitResponse<CaseData, State> response = caseworkerCreateDraftOrder.midEvent(caseDetails, caseDetails);

        //Then
        assertThat(response.getErrors()).isNull();
    }

    @Test
    void shouldSuccessfullySave2DraftOrder() {

        //Given
        final CicCase cicCase = CicCase.builder()
            .orderTemplateIssued(Document.builder().filename("a--b--02-02-2002 11:11:11.pdf").build()).build();
        final CaseData caseData = caseData();
        caseData.setCicCase(cicCase);
        final CaseDetails<CaseData, State> updatedCaseDetails = new CaseDetails<>();
        final CaseDetails<CaseData, State> beforeDetails = new CaseDetails<>();
        final DraftOrderContentCIC orderContentCIC = DraftOrderContentCIC.builder()
            .orderTemplate(OrderTemplate.CIC7_ME_DMI_REPORTS).build();
        caseData.setDraftOrderContentCIC(orderContentCIC);
        updatedCaseDetails.setData(caseData);
        updatedCaseDetails.setId(TEST_CASE_ID);
        updatedCaseDetails.setCreatedDate(LOCAL_DATE_TIME);

        //When
        AboutToStartOrSubmitResponse<CaseData, State> response =
            caseworkerCreateDraftOrder.aboutToSubmit(updatedCaseDetails, beforeDetails);
        assertThat(response).isNotNull();

        //When
        SubmittedCallbackResponse draftCreatedResponse = caseworkerCreateDraftOrder.submitted(updatedCaseDetails, beforeDetails);

        //  Then
        assertThat(draftCreatedResponse).isNotNull();

        final CicCase cicCase2 = CicCase.builder()
            .orderTemplateIssued(Document.builder().filename("a--b--02-02-2002 11:11:11.pdf").build()).build();
        caseData.setCicCase(cicCase2);
        updatedCaseDetails.setData(caseData);
        caseData.setDraftOrderContentCIC(orderContentCIC);
        AboutToStartOrSubmitResponse<CaseData, State> response2 =
            caseworkerCreateDraftOrder.aboutToSubmit(updatedCaseDetails, beforeDetails);
        //  Then
        assertThat(response2).isNotNull();
    }

    @Test
    void shouldHandlePreExistingDraftOrderList() {
        final CicCase cicCase = CicCase.builder()
                .orderTemplateIssued(Document.builder().filename("a--b--02-02-2002 11:11:11.pdf").build()).build();
        final CaseData caseData = caseData();
        caseData.setCicCase(cicCase);
        final CaseDetails<CaseData, State> updatedCaseDetails = new CaseDetails<>();
        final CaseDetails<CaseData, State> beforeDetails = new CaseDetails<>();
        final DraftOrderContentCIC orderContentCIC = DraftOrderContentCIC.builder()
                .orderTemplate(OrderTemplate.CIC7_ME_DMI_REPORTS).build();
        caseData.setDraftOrderContentCIC(orderContentCIC);
        updatedCaseDetails.setData(caseData);
        updatedCaseDetails.setId(TEST_CASE_ID);
        updatedCaseDetails.setCreatedDate(LOCAL_DATE_TIME);

        final DraftOrderCIC existingDraftOrderCIC = DraftOrderCIC.builder()
                .templateGeneratedDocument(Document.builder().filename("aa--bb--02-01-2002 09:10:10.pdf").build())
                .draftOrderContentCIC(DraftOrderContentCIC.builder().orderTemplate(OrderTemplate.CIC6_GENERAL_DIRECTIONS).build())
                .build();
        final ListValue<DraftOrderCIC> existingDraftOrderCICListValue = new ListValue<>();
        existingDraftOrderCICListValue.setValue(existingDraftOrderCIC);
        existingDraftOrderCICListValue.setId("0");
        final List<ListValue<DraftOrderCIC>> existingDraftOrderCICList = new ArrayList<>();
        existingDraftOrderCICList.add(existingDraftOrderCICListValue);
        caseData.getCicCase().setDraftOrderCICList(existingDraftOrderCICList);

        final String existingLabel =
                OrderTemplate.CIC6_GENERAL_DIRECTIONS.getLabel() + DOUBLE_HYPHEN + "02-01-2002 09:10:10.pdf" + DOUBLE_HYPHEN + "draft.pdf";
        final DynamicListElement existingOrder = DynamicListElement.builder().label(existingLabel).code(UUID.randomUUID()).build();
        final List<DynamicListElement> existingOrderDynamicList = new ArrayList<>();
        existingOrderDynamicList.add(existingOrder);
        cicCase.setDraftOrderDynamicList(DynamicList.builder().listItems(existingOrderDynamicList).build());

        final AboutToStartOrSubmitResponse<CaseData, State> response =
                caseworkerCreateDraftOrder.aboutToSubmit(updatedCaseDetails, beforeDetails);

        assertThat(response).isNotNull();
        assertThat(response.getData()).isNotNull();
        assertThat(response.getData().getCicCase().getDraftOrderCICList()).hasSize(2);
        assertThat(response.getData().getCicCase().getDraftOrderDynamicList().getListItems()).contains(existingOrder);
        assertThat(response.getData().getCicCase().getDraftOrderCICList().get(0).getValue().getDraftOrderContentCIC())
                .isEqualTo(orderContentCIC);
        assertThat(response.getData().getCicCase().getDraftOrderCICList().get(1).getValue().getDraftOrderContentCIC())
                .isEqualTo(existingDraftOrderCIC.getDraftOrderContentCIC());
    }
}

