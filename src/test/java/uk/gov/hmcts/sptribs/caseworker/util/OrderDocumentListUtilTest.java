package uk.gov.hmcts.sptribs.caseworker.util;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.ccd.sdk.type.Document;
import uk.gov.hmcts.ccd.sdk.type.ListValue;
import uk.gov.hmcts.sptribs.caseworker.model.DraftOrderCIC;
import uk.gov.hmcts.sptribs.caseworker.model.Order;
import uk.gov.hmcts.sptribs.ciccase.model.CaseData;
import uk.gov.hmcts.sptribs.ciccase.model.CicCase;
import uk.gov.hmcts.sptribs.document.model.CICDocument;
import uk.gov.hmcts.sptribs.document.model.CaseworkerCICDocument;
import uk.gov.hmcts.sptribs.document.model.DocumentType;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static uk.gov.hmcts.sptribs.testutil.TestDataHelper.get2Document;
import static uk.gov.hmcts.sptribs.testutil.TestDataHelper.getDocument;

@ExtendWith(MockitoExtension.class)
public class OrderDocumentListUtilTest {

    @Test
    void shouldGenerateOrderDocUpload() {
        //Given

        CICDocument document = CICDocument.builder().documentLink(Document.builder().filename("name").build()).build();
        ListValue<CICDocument> cicDocumentListValue = new ListValue<>();
        cicDocumentListValue.setValue(document);
        Order order = Order.builder().uploadedFile(List.of(cicDocumentListValue)).build();
        ListValue<Order> orderListValue = new ListValue<>();
        orderListValue.setValue(order);
        CicCase cicCase = CicCase.builder().orderList(List.of(orderListValue)).build();

        //When
        List<CaseworkerCICDocument> result = OrderDocumentListUtil.getOrderDocuments(cicCase);

        //Then
        assertThat(result).isNotNull();
    }

    @Test
    void shouldGenerateOrderDocTemp() {
        //Given

        Order order = Order.builder().draftOrder(DraftOrderCIC.builder()
            .templateGeneratedDocument(Document.builder().filename("name").build()).build()).build();
        ListValue<Order> orderListValue = new ListValue<>();
        orderListValue.setValue(order);
        CicCase cicCase = CicCase.builder().orderList(List.of(orderListValue)).build();

        //When
        List<CaseworkerCICDocument> result = OrderDocumentListUtil.getOrderDocuments(cicCase);

        //Then
        assertThat(result).isNotNull();
    }

    @Test
    void givenOneOrderWith2Docs_whenAddOrderDocsForRemoval_thenAdd1File() {
        //Given
        ListValue<CICDocument> cicDocumentListValue1 = buildCicDocumentListValue("url1", "url1", "name1");
        //delete the one below
        ListValue<CICDocument> cicDocumentListValue2 = buildCicDocumentListValue("url2", "url2", "name2");
        List<ListValue<CICDocument>> cicDocumentListValuesOld = new ArrayList<>();
        cicDocumentListValuesOld.add(cicDocumentListValue1);
        cicDocumentListValuesOld.add(cicDocumentListValue2);
        ListValue<Order> orderListValue = buildOrderListValue(cicDocumentListValuesOld);

        List<ListValue<Order>> orderList = new ArrayList<>();
        orderList.add(orderListValue);

        CicCase cicCaseOld = CicCase.builder()
            .decisionDocumentList(get2Document())
            .orderList(orderList)
            .finalDecisionDocumentList(get2Document())
            .applicantDocumentsUploaded(get2Document())
            .reinstateDocuments(get2Document())
            .build();


        List<ListValue<CaseworkerCICDocument>> cicDocumentListValuesNew = new ArrayList<>();
        ListValue<CaseworkerCICDocument> caseworkerCICDocumentListValue1 =
            buildCaseworkerCicDocumentListValue("url1", "url1", "name1");
        cicDocumentListValuesNew.add(caseworkerCICDocumentListValue1);

        CicCase cicCase = CicCase.builder()
            .orderDocumentList(cicDocumentListValuesNew)
            .orderList(orderList)
            .decisionDocumentList(new ArrayList<>())
            .finalDecisionDocumentList(new ArrayList<>())
            .applicantDocumentsUploaded(getDocument())
            .build();


        //When
        CicCase result = OrderDocumentListUtil.addOrderDocsForRemoval(cicCase, cicCaseOld);

        //Then
        assertThat(result).isNotNull();
        assertThat(result.getRemovedDocumentList().size()).isSameAs(1);
        assertThat(result.getRemovedDocumentList().getFirst().getValue().getDocumentLink().getUrl()).isSameAs("url2");
    }

    @Test
    void givenOrderListWith1Doc_whenRemoveNonDraftOrder_thenRemoveDocAndClearOrder() {
        //Given
        ListValue<CICDocument> cicDocumentListValue = buildCicDocumentListValue("url1", "url1", "name1");
        List<ListValue<CICDocument>> cicDoclist = new ArrayList<>();
        cicDoclist.add(cicDocumentListValue);
        ListValue<Order> orderListValue = buildOrderListValue(cicDoclist);
        List<ListValue<Order>> ordersList = new ArrayList<>();
        ordersList.add(orderListValue);

        CaseData caseData = CaseData.builder().cicCase(CicCase.builder()
                .orderDocumentList(getDocument())
                .orderList(ordersList)
                .decisionDocumentList(new ArrayList<>())
                .finalDecisionDocumentList(new ArrayList<>())
                .applicantDocumentsUploaded(getDocument())
                .build())
            .build();

        //When
        OrderDocumentListUtil.removeNonDraftOrder(caseData,
            buildCaseworkerCicDocumentListValue("url1", "url1", "name1"));

        //then
        assertThat(caseData.getCicCase().getOrderList()).isEmpty();

    }

    @Test
    void givenOrderListWith1Doc_whenRemoveNonDraftOrder_thenDoNotRemoveDoc() {
        //Given
        ListValue<CICDocument> cicDocumentListValue = buildCicDocumentListValue("url1", "url1", "name1");
        List<ListValue<CICDocument>> cicDoclist = new ArrayList<>();
        cicDoclist.add(cicDocumentListValue);
        ListValue<Order> orderListValue = buildOrderListValue(cicDoclist);
        List<ListValue<Order>> ordersList = new ArrayList<>();
        ordersList.add(orderListValue);

        CaseData caseData = CaseData.builder().cicCase(CicCase.builder()
                .orderDocumentList(getDocument())
                .orderList(ordersList)
                .decisionDocumentList(new ArrayList<>())
                .finalDecisionDocumentList(new ArrayList<>())
                .applicantDocumentsUploaded(getDocument())
                .build())
            .build();

        //When
        OrderDocumentListUtil.removeNonDraftOrder(caseData,
            buildCaseworkerCicDocumentListValue("url2", "url2", "name2"));

        //then
        assertThat(caseData.getCicCase().getOrderList().size()).isSameAs(1);

    }

    @Test
    void givenOrderListWith2Docs_whenRemoveNonDraftOrder_thenRemoveDocAndKeepOrder() {
        //Given
        ListValue<CICDocument> cicDocumentListValue1 = buildCicDocumentListValue("url1", "url1", "name1");
        ListValue<CICDocument> cicDocumentListValue2 = buildCicDocumentListValue("url2", "url2", "name2");
        List<ListValue<CICDocument>> cicDoclist = new ArrayList<>();
        cicDoclist.add(cicDocumentListValue1);
        cicDoclist.add(cicDocumentListValue2);
        ListValue<Order> orderListValue = buildOrderListValue(cicDoclist);
        List<ListValue<Order>> ordersList = new ArrayList<>();
        ordersList.add(orderListValue);

        CaseData caseData = CaseData.builder().cicCase(CicCase.builder()
                .orderDocumentList(getDocument())
                .orderList(ordersList)
                .decisionDocumentList(new ArrayList<>())
                .finalDecisionDocumentList(new ArrayList<>())
                .applicantDocumentsUploaded(getDocument())
                .build())
            .build();

        //When
        OrderDocumentListUtil.removeNonDraftOrder(caseData,
            buildCaseworkerCicDocumentListValue("url1", "url1", "name1"));

        //then
        assertThat(caseData.getCicCase().getOrderList().size()).isSameAs(1);
        assertThat(caseData.getCicCase().getOrderList().getFirst().getValue().getUploadedFile().size()).isSameAs(1);

    }

    @Test
    void givenOrderListWith2DocsAndSameFileNme_whenRemoveNonDraftOrder_thenRemoveDocsAndCleanOrder() {
        //Given
        ListValue<CICDocument> cicDocumentListValue1 = buildCicDocumentListValue("url1", "url1", "name1");
        List<ListValue<CICDocument>> cicDoclist = new ArrayList<>();
        cicDoclist.add(cicDocumentListValue1);
        cicDoclist.add(cicDocumentListValue1);
        ListValue<Order> orderListValue = buildOrderListValue(cicDoclist);
        List<ListValue<Order>> ordersList = new ArrayList<>();
        ordersList.add(orderListValue);

        CaseData caseData = CaseData.builder().cicCase(CicCase.builder()
                .orderDocumentList(getDocument())
                .orderList(ordersList)
                .decisionDocumentList(new ArrayList<>())
                .finalDecisionDocumentList(new ArrayList<>())
                .applicantDocumentsUploaded(getDocument())
                .build())
            .build();

        //When
        OrderDocumentListUtil.removeNonDraftOrder(caseData,
            buildCaseworkerCicDocumentListValue("url1", "url1", "name1"));

        //then
        assertThat(caseData.getCicCase().getOrderList()).isEmpty();

    }

    @Test
    void givenNullOrderList_whenRemoveNonDraftOrder_thenDoNothing() {
        //Given

        CaseData caseData = CaseData.builder().cicCase(CicCase.builder()
                .orderDocumentList(getDocument())
                .decisionDocumentList(new ArrayList<>())
                .finalDecisionDocumentList(new ArrayList<>())
                .applicantDocumentsUploaded(getDocument())
                .build())
            .build();

        //When
        OrderDocumentListUtil.removeNonDraftOrder(caseData,
            buildCaseworkerCicDocumentListValue("url1", "url1", "name1"));

        //then
        assertThat(caseData.getCicCase().getOrderList()).isNull();
    }

    @Test
    void givenOrderListWithNoDocs_whenRemoveNonDraftOrder_thenClearOrder() {
        //Given
        ListValue<Order> orderListValue = buildOrderListValue(null);
        List<ListValue<Order>> ordersList = new ArrayList<>();
        ordersList.add(orderListValue);

        CaseData caseData = CaseData.builder().cicCase(CicCase.builder()
                .orderDocumentList(getDocument())
                .orderList(ordersList)
                .decisionDocumentList(new ArrayList<>())
                .finalDecisionDocumentList(new ArrayList<>())
                .applicantDocumentsUploaded(getDocument())
                .build())
            .build();

        //When
        OrderDocumentListUtil.removeNonDraftOrder(caseData,
            buildCaseworkerCicDocumentListValue("url1", "url1", "name1"));

        //then
        assertThat(caseData.getCicCase().getOrderList()).isEmpty();
    }

    private ListValue<CICDocument> buildCicDocumentListValue(String url, String binary, String filename) {
        CICDocument document = CICDocument.builder()
            .documentLink(Document.builder().url(url).binaryUrl(binary).filename(filename).build()).build();
        ListValue<CICDocument> cicDocumentListValue = new ListValue<>();
        cicDocumentListValue.setValue(document);
        return cicDocumentListValue;
    }

    private ListValue<CaseworkerCICDocument> buildCaseworkerCicDocumentListValue(String url, String binary, String filename) {
        CaseworkerCICDocument document = CaseworkerCICDocument.builder()
            .documentCategory(DocumentType.TRIBUNAL_DIRECTION)
            .documentLink(Document.builder().url(url).binaryUrl(binary).filename(filename).build()).build();
        ListValue<CaseworkerCICDocument> caseworkerCICDocumentListValue = new ListValue<>();
        caseworkerCICDocumentListValue.setValue(document);
        return caseworkerCICDocumentListValue;
    }

    private ListValue<Order> buildOrderListValue(List<ListValue<CICDocument>> cicDocumentListValues) {
        Order order = Order.builder().uploadedFile(cicDocumentListValues).build();
        ListValue<Order> orderListValue = new ListValue<>();
        orderListValue.setValue(order);
        return orderListValue;
    }
}
