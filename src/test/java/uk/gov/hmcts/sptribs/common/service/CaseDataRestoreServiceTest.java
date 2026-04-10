package uk.gov.hmcts.sptribs.common.service;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.ccd.sdk.type.Document;
import uk.gov.hmcts.ccd.sdk.type.ListValue;
import uk.gov.hmcts.sptribs.caseworker.model.DraftOrderCIC;
import uk.gov.hmcts.sptribs.caseworker.model.Order;
import uk.gov.hmcts.sptribs.caseworker.util.DocumentRemoveListUtil;
import uk.gov.hmcts.sptribs.caseworker.util.SendOrderUtil;
import uk.gov.hmcts.sptribs.ciccase.model.CaseData;
import uk.gov.hmcts.sptribs.ciccase.model.CicCase;
import uk.gov.hmcts.sptribs.common.dto.RemoveEventWithPrecedingData;
import uk.gov.hmcts.sptribs.common.repositories.CaseEventRepository;
import uk.gov.hmcts.sptribs.document.model.CICDocument;
import uk.gov.hmcts.sptribs.document.model.CaseworkerCICDocument;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.sptribs.testutil.TestEventConstants.CASEWORKER_DOCUMENT_MANAGEMENT_REMOVE;

@ExtendWith(MockitoExtension.class)
class CaseDataRestoreServiceTest {

    private static final Long REFERENCE = 12345L;
    private static final LocalDate START_DATE = LocalDate.of(2026, 2, 24);
    private static final LocalDate END_DATE = LocalDate.of(2026, 3, 5);

    @InjectMocks
    private CaseDataRestoreService ordersListRestoreService;

    @Mock
    private CaseEventRepository caseEventRepository;

    @Nested
    class WhenNoRemoveEventsFound {

        @Test
        void shouldReturnEarlyWhenNoRemoveEventsFound() {
            when(caseEventRepository.getRemoveEventsWithPrecedingData(
                REFERENCE, CASEWORKER_DOCUMENT_MANAGEMENT_REMOVE, START_DATE, END_DATE))
                .thenReturn(List.of());

            CaseData currentData = buildCaseDataWithOrders(List.of());

            try (MockedStatic<SendOrderUtil> sendOrderUtil = mockStatic(SendOrderUtil.class);
                 MockedStatic<DocumentRemoveListUtil> documentRemoveListUtil = mockStatic(DocumentRemoveListUtil.class)) {

                ordersListRestoreService.restoreOrdersList(REFERENCE, currentData, START_DATE, END_DATE);

                sendOrderUtil.verifyNoInteractions();
                documentRemoveListUtil.verifyNoInteractions();
            }
        }
    }

    @Nested
    class WhenRemoveEventsFound {

        @Test
        void shouldRestoreRemovedOrderThatIsNotTheRemovedDocument() {
            ListValue<Order> orderToRestore = buildOrderWithUploadedFile("order-1", "doc-url-1");
            ListValue<Order> remainingOrder = buildOrderWithUploadedFile("order-2", "doc-url-2");

            CaseData precedingData = buildCaseDataWithOrders(List.of(orderToRestore, remainingOrder));
            CaseData currentEventData = buildCaseDataWithOrders(List.of(remainingOrder));

            currentEventData.getCicCase().setRemovedDocumentList(List.of(buildCaseworkerCICDocument("doc-url-other")));

            RemoveEventWithPrecedingData event = RemoveEventWithPrecedingData.builder()
                .currentEvent(CASEWORKER_DOCUMENT_MANAGEMENT_REMOVE)
                .currentEventDate(LocalDateTime.of(2026, 2, 25, 10, 0))
                .currentEventData(currentEventData)
                .precedingEventData(precedingData)
                .build();

            when(caseEventRepository.getRemoveEventsWithPrecedingData(
                REFERENCE, CASEWORKER_DOCUMENT_MANAGEMENT_REMOVE, START_DATE, END_DATE))
                .thenReturn(List.of(event));

            CaseData currentData = buildCaseDataWithOrders(List.of(remainingOrder));

            try (MockedStatic<SendOrderUtil> sendOrderUtil = mockStatic(SendOrderUtil.class);
                 MockedStatic<DocumentRemoveListUtil> documentRemoveListUtil = mockStatic(DocumentRemoveListUtil.class)) {

                ordersListRestoreService.restoreOrdersList(REFERENCE, currentData, START_DATE, END_DATE);

                sendOrderUtil.verify(() ->
                    SendOrderUtil.updateCicCaseOrderList(eq(currentData), eq(orderToRestore.getValue())));
            }
        }

        @Test
        void shouldNotRestoreOrderWhenUploadedFileWasTheRemovedDocument() {
            ListValue<Order> removedOrder = buildOrderWithUploadedFile("order-1", "doc-url-1");
            ListValue<Order> remainingOrder = buildOrderWithUploadedFile("order-2", "doc-url-2");

            CaseData precedingData = buildCaseDataWithOrders(List.of(removedOrder, remainingOrder));
            CaseData currentEventData = buildCaseDataWithOrders(List.of(remainingOrder));

            currentEventData.getCicCase().setRemovedDocumentList(List.of(buildCaseworkerCICDocument("doc-url-1")));

            RemoveEventWithPrecedingData event = RemoveEventWithPrecedingData.builder()
                .currentEvent(CASEWORKER_DOCUMENT_MANAGEMENT_REMOVE)
                .currentEventDate(LocalDateTime.of(2026, 2, 25, 10, 0))
                .currentEventData(currentEventData)
                .precedingEventData(precedingData)
                .build();

            when(caseEventRepository.getRemoveEventsWithPrecedingData(
                    REFERENCE, CASEWORKER_DOCUMENT_MANAGEMENT_REMOVE, START_DATE, END_DATE))
                    .thenReturn(List.of(event));

            CaseData currentData = buildCaseDataWithOrders(List.of(remainingOrder));

            try (MockedStatic<SendOrderUtil> sendOrderUtil = mockStatic(SendOrderUtil.class);
                 MockedStatic<DocumentRemoveListUtil> documentRemoveListUtil = mockStatic(DocumentRemoveListUtil.class)) {

                ordersListRestoreService.restoreOrdersList(REFERENCE, currentData, START_DATE, END_DATE);

                sendOrderUtil.verifyNoInteractions();
            }
        }

        @Test
        void shouldNotRestoreOrderWhenDraftOrderDocumentWasTheRemovedDocument() {
            ListValue<Order> removedOrder = buildOrderWithDraftOrderCIC("order-1", "draft-doc-url-1");
            ListValue<Order> remainingOrder = buildOrderWithUploadedFile("order-2", "doc-url-2");

            CaseData precedingData = buildCaseDataWithOrders(List.of(removedOrder, remainingOrder));
            CaseData currentEventData = buildCaseDataWithOrders(List.of(remainingOrder));

            currentEventData.getCicCase().setRemovedDocumentList(List.of(buildCaseworkerCICDocument("draft-doc-url-1")));

            RemoveEventWithPrecedingData event = RemoveEventWithPrecedingData.builder()
                .currentEvent(CASEWORKER_DOCUMENT_MANAGEMENT_REMOVE)
                .currentEventDate(LocalDateTime.of(2026, 2, 25, 10, 0))
                .currentEventData(currentEventData)
                .precedingEventData(precedingData)
                .build();

            when(caseEventRepository.getRemoveEventsWithPrecedingData(
                    REFERENCE, CASEWORKER_DOCUMENT_MANAGEMENT_REMOVE, START_DATE, END_DATE))
                    .thenReturn(List.of(event));

            CaseData currentData = buildCaseDataWithOrders(List.of(remainingOrder));

            try (MockedStatic<SendOrderUtil> sendOrderUtil = mockStatic(SendOrderUtil.class);
                 MockedStatic<DocumentRemoveListUtil> documentRemoveListUtil = mockStatic(DocumentRemoveListUtil.class)) {

                ordersListRestoreService.restoreOrdersList(REFERENCE, currentData, START_DATE, END_DATE);

                sendOrderUtil.verifyNoInteractions();
            }
        }

        @Test
        void shouldNotRestoreOrderAlreadyPresentInCurrentData() {
            ListValue<Order> existingOrder = buildOrderWithUploadedFile("order-1", "doc-url-1");

            CaseData precedingData = buildCaseDataWithOrders(List.of(existingOrder));
            CaseData currentEventData = buildCaseDataWithOrders(List.of());
            currentEventData.getCicCase().setRemovedDocumentList(List.of());

            RemoveEventWithPrecedingData event = RemoveEventWithPrecedingData.builder()
                .currentEvent(CASEWORKER_DOCUMENT_MANAGEMENT_REMOVE)
                .currentEventDate(LocalDateTime.of(2026, 2, 25, 10, 0))
                .currentEventData(currentEventData)
                .precedingEventData(precedingData)
                .build();

            when(caseEventRepository.getRemoveEventsWithPrecedingData(
                REFERENCE, CASEWORKER_DOCUMENT_MANAGEMENT_REMOVE, START_DATE, END_DATE))
                .thenReturn(List.of(event));

            CaseData currentData = buildCaseDataWithOrders(List.of(existingOrder));

            try (MockedStatic<SendOrderUtil> sendOrderUtil = mockStatic(SendOrderUtil.class);
                 MockedStatic<DocumentRemoveListUtil> documentRemoveListUtil = mockStatic(DocumentRemoveListUtil.class)) {

                ordersListRestoreService.restoreOrdersList(REFERENCE, currentData, START_DATE, END_DATE);

                sendOrderUtil.verifyNoInteractions();
            }
        }

        @Test
        void shouldReturnEarlyWhenPrecedingDataHasNoOrdersList() {
            CaseData precedingData = buildCaseDataWithOrders(List.of());
            CaseData currentEventData = buildCaseDataWithOrders(List.of());
            currentEventData.getCicCase().setRemovedDocumentList(List.of());

            RemoveEventWithPrecedingData event = RemoveEventWithPrecedingData.builder()
                .currentEvent(CASEWORKER_DOCUMENT_MANAGEMENT_REMOVE)
                .currentEventDate(LocalDateTime.of(2026, 2, 25, 10, 0))
                .currentEventData(currentEventData)
                .precedingEventData(precedingData)
                .build();

            when(caseEventRepository.getRemoveEventsWithPrecedingData(
                REFERENCE, CASEWORKER_DOCUMENT_MANAGEMENT_REMOVE, START_DATE, END_DATE))
                .thenReturn(List.of(event));

            CaseData currentData = buildCaseDataWithOrders(List.of());

            try (MockedStatic<SendOrderUtil> sendOrderUtil = mockStatic(SendOrderUtil.class);
                 MockedStatic<DocumentRemoveListUtil> documentRemoveListUtil = mockStatic(DocumentRemoveListUtil.class)) {

                ordersListRestoreService.restoreOrdersList(REFERENCE, currentData, START_DATE, END_DATE);

                sendOrderUtil.verifyNoInteractions();
            }
        }

        @Test
        void shouldHandleMultipleRemoveEventsAndRestoreChronologically() {
            ListValue<Order> firstRemovedOrder = buildOrderWithUploadedFile("order-1", "doc-url-1");
            ListValue<Order> secondRemovedOrder = buildOrderWithUploadedFile("order-2", "doc-url-2");
            ListValue<Order> remainingOrder = buildOrderWithUploadedFile("order-3", "doc-url-3");

            CaseData precedingDataFirst = buildCaseDataWithOrders(List.of(firstRemovedOrder, remainingOrder));
            CaseData currentEventDataFirst = buildCaseDataWithOrders(List.of(remainingOrder));
            currentEventDataFirst.getCicCase().setRemovedDocumentList(List.of(buildCaseworkerCICDocument("doc-url-other")));

            CaseData precedingDataSecond = buildCaseDataWithOrders(List.of(secondRemovedOrder, remainingOrder));
            CaseData currentEventDataSecond = buildCaseDataWithOrders(List.of(remainingOrder));
            currentEventDataSecond.getCicCase().setRemovedDocumentList(List.of(buildCaseworkerCICDocument("doc-url-other")));

            RemoveEventWithPrecedingData firstEvent = RemoveEventWithPrecedingData.builder()
                .currentEvent(CASEWORKER_DOCUMENT_MANAGEMENT_REMOVE)
                .currentEventDate(LocalDateTime.of(2026, 2, 25, 10, 0))
                .currentEventData(currentEventDataFirst)
                .precedingEventData(precedingDataFirst)
                .build();

            RemoveEventWithPrecedingData secondEvent = RemoveEventWithPrecedingData.builder()
                .currentEvent(CASEWORKER_DOCUMENT_MANAGEMENT_REMOVE)
                .currentEventDate(LocalDateTime.of(2026, 2, 28, 14, 0))
                .currentEventData(currentEventDataSecond)
                .precedingEventData(precedingDataSecond)
                .build();

            when(caseEventRepository.getRemoveEventsWithPrecedingData(
                REFERENCE, CASEWORKER_DOCUMENT_MANAGEMENT_REMOVE, START_DATE, END_DATE))
                .thenReturn(List.of(secondEvent, firstEvent)); // intentionally out of order

            CaseData currentData = buildCaseDataWithOrders(List.of(remainingOrder));

            try (MockedStatic<SendOrderUtil> sendOrderUtil = mockStatic(SendOrderUtil.class);
                 MockedStatic<DocumentRemoveListUtil> documentRemoveListUtil = mockStatic(DocumentRemoveListUtil.class)) {

                ordersListRestoreService.restoreOrdersList(REFERENCE, currentData, START_DATE, END_DATE);

                // verify both orders restored
                sendOrderUtil.verify(() ->
                    SendOrderUtil.updateCicCaseOrderList(eq(currentData), eq(firstRemovedOrder.getValue())));
                sendOrderUtil.verify(() ->
                    SendOrderUtil.updateCicCaseOrderList(eq(currentData), eq(secondRemovedOrder.getValue())));
            }
        }
    }

    private CaseData buildCaseDataWithOrders(List<ListValue<Order>> orders) {
        CicCase cicCase = CicCase.builder()
                .orderList(new ArrayList<>(orders))
                .build();
        return CaseData.builder()
                .cicCase(cicCase)
                .build();
    }

    private ListValue<Order> buildOrderWithUploadedFile(String id, String documentUrl) {
        CICDocument cicDocument = CICDocument.builder()
                .documentLink(Document.builder().url(documentUrl).build())
                .build();

        Order order = Order.builder()
                .uploadedFile(List.of(ListValue.<CICDocument>builder()
                        .id(id)
                        .value(cicDocument)
                        .build()))
                .build();

        return ListValue.<Order>builder()
                .id(id)
                .value(order)
                .build();
    }

    private ListValue<Order> buildOrderWithDraftOrderCIC(String id, String documentUrl) {
        DraftOrderCIC draftOrder = DraftOrderCIC.builder()
                .templateGeneratedDocument(Document.builder().url(documentUrl).build())
                .build();

        Order order = Order.builder()
                .draftOrder(draftOrder)
                .build();

        return ListValue.<Order>builder()
                .id(id)
                .value(order)
                .build();
    }

    private ListValue<CaseworkerCICDocument> buildCaseworkerCICDocument(String documentUrl) {
        return ListValue.<CaseworkerCICDocument>builder()
                .value(CaseworkerCICDocument.builder()
                        .documentLink(Document.builder().url(documentUrl).build())
                        .build())
                .build();
    }
}