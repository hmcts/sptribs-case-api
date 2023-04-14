package uk.gov.hmcts.sptribs.caseworker.util;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.ccd.sdk.type.Document;
import uk.gov.hmcts.ccd.sdk.type.ListValue;
import uk.gov.hmcts.sptribs.caseworker.model.DraftOrderCIC;
import uk.gov.hmcts.sptribs.caseworker.model.Order;
import uk.gov.hmcts.sptribs.ciccase.model.CicCase;
import uk.gov.hmcts.sptribs.document.model.CICDocument;
import uk.gov.hmcts.sptribs.document.model.CaseworkerCICDocument;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static uk.gov.hmcts.sptribs.testutil.TestDataHelper.get2Document;
import static uk.gov.hmcts.sptribs.testutil.TestDataHelper.get2DocumentCiC;
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
    void shouldRemoveOrderDocs() {
        //Given

        CICDocument document = CICDocument.builder()
            .documentLink(Document.builder().url("url1").binaryUrl("url1").filename("name1").build()).build();
        ListValue<CICDocument> cicDocumentListValue = new ListValue<>();
        cicDocumentListValue.setValue(document);
        Order order = Order.builder().uploadedFile(List.of(cicDocumentListValue)).build();
        ListValue<Order> orderListValue = new ListValue<>();
        orderListValue.setValue(order);
        Order orderOld = Order.builder().uploadedFile(get2DocumentCiC()).build();
        Order orderOld2 = Order.builder().uploadedFile(get2DocumentCiC()).build();
        ListValue<Order> orderListValueOld = new ListValue<>();
        orderListValueOld.setValue(orderOld);
        ListValue<Order> orderListValueOld2 = new ListValue<>();
        orderListValueOld2.setValue(orderOld2);
        List<ListValue<Order>> orderList = new ArrayList<>();
        orderList.add(orderListValueOld);
        orderList.add(orderListValueOld2);
        CicCase cicCase = CicCase.builder()
            .orderDocumentList(getDocument())
            .orderList(List.of(orderListValue))
            .decisionDocumentList(new ArrayList<>())
            .finalDecisionDocumentList(new ArrayList<>())
            .applicantDocumentsUploaded(getDocument())
            .build();
        CicCase cicCaseOld = CicCase.builder()
            .decisionDocumentList(get2Document())
            .orderList(orderList)
            .finalDecisionDocumentList(get2Document())
            .applicantDocumentsUploaded(get2Document())
            .reinstateDocuments(get2Document())
            .build();


        //When
        CicCase result = OrderDocumentListUtil.removeOrderDoc(cicCase, cicCaseOld);

        //Then
        assertThat(result).isNotNull();
    }
}
