package uk.gov.hmcts.sptribs.caseworker.util;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.ccd.sdk.type.Document;
import uk.gov.hmcts.ccd.sdk.type.ListValue;
import uk.gov.hmcts.sptribs.caseworker.model.DocumentManagement;
import uk.gov.hmcts.sptribs.caseworker.model.Order;
import uk.gov.hmcts.sptribs.ciccase.model.CaseData;
import uk.gov.hmcts.sptribs.ciccase.model.CicCase;
import uk.gov.hmcts.sptribs.document.model.CICDocument;

import java.util.ArrayList;
import java.util.List;

import static uk.gov.hmcts.sptribs.testutil.TestDataHelper.caseData;
import static uk.gov.hmcts.sptribs.testutil.TestDataHelper.get2Document;
import static uk.gov.hmcts.sptribs.testutil.TestDataHelper.getDocument;

@ExtendWith(MockitoExtension.class)
public class DocumentManagementUtilTest {


    @Test
    void shouldAddToRemoveList() {
        //Given

        CICDocument document = CICDocument.builder()
            .documentLink(Document.builder().url("url1").binaryUrl("url1").filename("name1").build()).build();
        ListValue<CICDocument> cicDocumentListValue = new ListValue<>();
        cicDocumentListValue.setValue(document);
        Order order = Order.builder().uploadedFile(List.of(cicDocumentListValue)).build();
        ListValue<Order> orderListValue = new ListValue<>();
        orderListValue.setValue(order);

        CicCase cicCase = CicCase.builder()
            .orderDocumentList(getDocument())
            .orderList(List.of(orderListValue))
            .decisionDocumentList(new ArrayList<>())
            .finalDecisionDocumentList(new ArrayList<>())
            .applicantDocumentsUploaded(getDocument())
            .build();

        //When
        DocumentManagementUtil.addToRemovedDocuments(cicCase, get2Document().get(1).getValue());
        DocumentManagementUtil.addToRemovedDocuments(cicCase, get2Document().get(0).getValue());

    }

    @Test
    void shouldCheckList() {
        //Given

        final CaseData caseData = caseData();

        CicCase cicCase = CicCase.builder()
            .decisionDocumentList(new ArrayList<>())
            .finalDecisionDocumentList(new ArrayList<>())
            .applicantDocumentsUploaded(getDocument())
            .build();
        caseData.setCicCase(cicCase);
        DocumentManagement documentManagement = DocumentManagement.builder().caseworkerCICDocument(getDocument()).build();
        caseData.setDocManagement(documentManagement);

        //When
        DocumentManagementUtil.checkLists(caseData, get2Document(), getDocument());

    }


    @Test
    void shouldCheckListWithEmpty() {
        //Given

        final CaseData caseData = caseData();

        CicCase cicCase = CicCase.builder()
            .decisionDocumentList(new ArrayList<>())
            .finalDecisionDocumentList(new ArrayList<>())
            .applicantDocumentsUploaded(getDocument())
            .build();
        caseData.setCicCase(cicCase);
        DocumentManagement documentManagement = DocumentManagement.builder().caseworkerCICDocument(getDocument()).build();
        caseData.setDocManagement(documentManagement);

        //When
        DocumentManagementUtil.checkLists(caseData, get2Document(), new ArrayList<>());

    }
}
