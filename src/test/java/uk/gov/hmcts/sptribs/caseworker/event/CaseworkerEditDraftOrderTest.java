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
import uk.gov.hmcts.sptribs.ciccase.model.OrderTemplate;
import uk.gov.hmcts.sptribs.ciccase.model.State;
import uk.gov.hmcts.sptribs.ciccase.model.UserRole;
import uk.gov.hmcts.sptribs.ciccase.model.access.Permissions;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static uk.gov.hmcts.sptribs.ciccase.model.UserRole.ST_CIC_WA_CONFIG_USER;
import static uk.gov.hmcts.sptribs.testutil.ConfigTestUtil.createCaseDataConfigBuilder;
import static uk.gov.hmcts.sptribs.testutil.ConfigTestUtil.getEventsFrom;
import static uk.gov.hmcts.sptribs.testutil.TestConstants.TEST_CASE_ID;
import static uk.gov.hmcts.sptribs.testutil.TestDataHelper.LOCAL_DATE_TIME;
import static uk.gov.hmcts.sptribs.testutil.TestDataHelper.caseData;
import static uk.gov.hmcts.sptribs.testutil.TestEventConstants.CASEWORKER_EDIT_DRAFT_ORDER;


@ExtendWith(MockitoExtension.class)
class CaseworkerEditDraftOrderTest {
    @InjectMocks
    private CaseworkerEditDraftOrder caseworkerEditDraftOrder;

    @Mock
    private OrderService orderService;

    @Test
    void shouldAddPublishToCamundaWhenWAIsEnabled() {

        final ConfigBuilderImpl<CaseData, State, UserRole> configBuilder = createCaseDataConfigBuilder();

        caseworkerEditDraftOrder.configure(configBuilder);

        assertThat(getEventsFrom(configBuilder).values())
            .extracting(Event::getId)
            .contains(CASEWORKER_EDIT_DRAFT_ORDER);

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
        AboutToStartOrSubmitResponse<CaseData, State> response = caseworkerEditDraftOrder.aboutToStart(caseDetails);

        assertThat(response).isNotNull();
        assertThat(response.getData().getCurrentEvent()).isEqualTo(CASEWORKER_EDIT_DRAFT_ORDER);
    }

    @Test
    void shouldSuccessfullySaveDraftOrder() {
        //Given
        final CaseData caseData = caseData();
        final CaseDetails<CaseData, State> updatedCaseDetails = new CaseDetails<>();
        final CaseDetails<CaseData, State> beforeDetails = new CaseDetails<>();

        updatedCaseDetails.setData(caseData);
        updatedCaseDetails.setId(TEST_CASE_ID);
        updatedCaseDetails.setCreatedDate(LOCAL_DATE_TIME);
        caseData.setDraftOrderContentCIC(DraftOrderContentCIC.builder().orderTemplate(OrderTemplate.CIC6_GENERAL_DIRECTIONS).build());
        DynamicListElement element = DynamicListElement.builder().code(UUID.randomUUID())
            .label(OrderTemplate.CIC6_GENERAL_DIRECTIONS.getLabel() + "--01-01-2023 11:11:11").build();
        List<DynamicListElement> elements = new ArrayList<>();
        elements.add(element);
        caseData.getCicCase().setDraftOrderDynamicList(DynamicList.builder().value(element).listItems(elements).build());
        List<ListValue<DraftOrderCIC>> cicList = new ArrayList<>();
        cicList.add(ListValue.<DraftOrderCIC>builder().value(
            DraftOrderCIC.builder()
                .templateGeneratedDocument(Document.builder().filename("draft--user--01-01-2023 11:11:11.pdf")
                    .build())
                .draftOrderContentCIC(DraftOrderContentCIC.builder().orderTemplate(OrderTemplate.CIC6_GENERAL_DIRECTIONS).build()).build()

        ).build());
        caseData.getCicCase().setDraftOrderCICList(cicList);
        caseData.getCicCase().setOrderTemplateIssued(Document.builder().filename("draft--user--01-01-2023 11:11:11.pdf").build());

        //When
        AboutToStartOrSubmitResponse<CaseData, State> response =
            caseworkerEditDraftOrder.aboutToSubmit(updatedCaseDetails, beforeDetails);
        assertThat(response).isNotNull();
        DraftOrderCIC updatedDraftOrder = response.getData()
            .getCicCase().getDraftOrderCICList().getFirst().getValue();
        assertThat(updatedDraftOrder.getDraftOrderContentCIC().getOrderTemplate()).isEqualTo(OrderTemplate.CIC6_GENERAL_DIRECTIONS);
        assertThat(updatedDraftOrder.getTemplateGeneratedDocument()).isNotNull();

        SubmittedCallbackResponse draftCreatedResponse = caseworkerEditDraftOrder.submitted(updatedCaseDetails, beforeDetails);
        //  Then
        assertThat(draftCreatedResponse).isNotNull();
    }

}

