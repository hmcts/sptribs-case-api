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

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

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

}
