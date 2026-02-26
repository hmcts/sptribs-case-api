package uk.gov.hmcts.sptribs.caseworker.util;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.ccd.sdk.type.Document;
import uk.gov.hmcts.ccd.sdk.type.DynamicList;
import uk.gov.hmcts.ccd.sdk.type.ListValue;
import uk.gov.hmcts.sptribs.caseworker.model.CaseIssueDecision;
import uk.gov.hmcts.sptribs.caseworker.model.CaseIssueFinalDecision;
import uk.gov.hmcts.sptribs.caseworker.model.CloseCase;
import uk.gov.hmcts.sptribs.caseworker.model.DocumentManagement;
import uk.gov.hmcts.sptribs.caseworker.model.DraftOrderCIC;
import uk.gov.hmcts.sptribs.caseworker.model.DraftOrderContentCIC;
import uk.gov.hmcts.sptribs.caseworker.model.HearingSummary;
import uk.gov.hmcts.sptribs.caseworker.model.Listing;
import uk.gov.hmcts.sptribs.caseworker.model.Order;
import uk.gov.hmcts.sptribs.ciccase.model.CaseData;
import uk.gov.hmcts.sptribs.ciccase.model.CicCase;
import uk.gov.hmcts.sptribs.ciccase.model.OrderTemplate;
import uk.gov.hmcts.sptribs.document.model.CICDocument;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static uk.gov.hmcts.sptribs.testutil.TestDataHelper.caseData;
import static uk.gov.hmcts.sptribs.testutil.TestDataHelper.get2Document;
import static uk.gov.hmcts.sptribs.testutil.TestDataHelper.getDocument;

@ExtendWith(MockitoExtension.class)
public class DraftRemoveListUtilTest {

    CaseData caseDataNew = new CaseData();
    CaseData caseDataOld = new CaseData();


    @BeforeEach
    void setUp() {

        final Document document = Document.builder()
            .url("test/documents/a57d1138-1f8d-4aeb-b5ad-3681aba68747")
            .filename("Order--[test]--24-02-2026 15:47:25.pdf")
            .binaryUrl("test")
            .categoryId("TD")
            .build();

        final Document document2 = Document.builder()
            .url("test/documents/a57d1138-1f8d-4aeb-b5ad-3681aba68748")
            .filename("Order--[test]--25-02-2026 15:47:25.pdf")
            .binaryUrl("test")
            .categoryId("TD")
            .build();

        final DraftOrderContentCIC content = DraftOrderContentCIC.builder()
            .orderTemplate(OrderTemplate.CIC3_RULE_27)
            .mainContent("test")
            .orderSignature("test")
            .build();

        final DraftOrderCIC draftOrder = DraftOrderCIC.builder()
            .templateGeneratedDocument(document)
            .draftOrderContentCIC(content)
            .build();

        final DraftOrderCIC draftOrder2 = DraftOrderCIC.builder()
            .templateGeneratedDocument(document2)
            .draftOrderContentCIC(content)
            .build();


        final ListValue<DraftOrderCIC> lv = ListValue.<DraftOrderCIC>builder()
            .id("1")
            .value(draftOrder)
            .build();

        final ListValue<DraftOrderCIC> lv2 = ListValue.<DraftOrderCIC>builder()
            .id("2")
            .value(draftOrder)
            .build();

        final ListValue<DraftOrderCIC> lv3 = ListValue.<DraftOrderCIC>builder()
            .id("3")
            .value(draftOrder2)
            .build();

        caseDataOld.setCicCase(CicCase.builder()
            .draftOrderCICList(new ArrayList<>(List.of(lv, lv2, lv3)))
            .draftOrderDynamicList(DynamicList.builder()
                .listItems(new ArrayList<>())
                .build())
            .build());

        caseDataNew.setCicCase(CicCase.builder()
            .draftOrderCICList(new ArrayList<>(List.of(lv, lv3)))
            .draftOrderDynamicList(DynamicList.builder()
                .listItems(new ArrayList<>())
                .build())
            .build());

    }


    @Test
    void shouldCorrectlyPopulateDraftRemovalList() {


        CaseData result = DraftRemoveListUtil.setDraftListForRemoval(caseDataNew, caseDataOld);

        Assertions.assertEquals(1 ,result.getCicCase().getRemovedDraftList().size());
    }

    @Test
    void shouldCorrectlyRepopulateDraftDynamicList() {

        CicCase result = DraftRemoveListUtil.repopulateDynamicDraftList(caseDataNew.getCicCase());


        Assertions.assertEquals(2, result.getDraftOrderDynamicList().getListItems().size());
        //DynamicList Order should be inverse of draftOrderCICList
        Assertions.assertEquals("CIC3 - Rule 27--25-02-2026 15:47:25--draft.pdf", result.getDraftOrderDynamicList().getListItems().getFirst().getLabel());
        Assertions.assertEquals(UUID.fromString("a57d1138-1f8d-4aeb-b5ad-3681aba68748"), result.getDraftOrderDynamicList().getListItems().getFirst().getCode());

    }
}
