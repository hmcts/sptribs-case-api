package uk.gov.hmcts.sptribs.caseworker.event.page;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.ccd.sdk.api.CaseDetails;
import uk.gov.hmcts.ccd.sdk.api.callback.AboutToStartOrSubmitResponse;
import uk.gov.hmcts.ccd.sdk.type.DynamicListElement;
import uk.gov.hmcts.ccd.sdk.type.DynamicMultiSelectList;
import uk.gov.hmcts.sptribs.caseworker.model.ContactPartiesDocuments;
import uk.gov.hmcts.sptribs.ciccase.model.CaseData;
import uk.gov.hmcts.sptribs.ciccase.model.State;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ContactPartiesSelectDocumentTest {

    @InjectMocks
    private ContactPartiesSelectDocument contactPartiesSelectDocument;

    @Mock
    private ContactPartiesDocuments contactPartiesDocuments;

    @Mock
    private DynamicMultiSelectList dynamicMultiSelectList;

    @Mock
    private List<DynamicListElement> dynamicList;

    @Test
    void midEventIsSuccessful() {
        final CaseDetails<CaseData, State> caseDetails = new CaseDetails<>();
        final CaseData caseData = CaseData.builder()
            .contactPartiesDocuments(contactPartiesDocuments)
            .build();
        caseDetails.setData(caseData);
        when(contactPartiesDocuments.getDocumentList()).thenReturn(dynamicMultiSelectList);
        when(dynamicMultiSelectList.getValue()).thenReturn(dynamicList);
        when(dynamicList.size()).thenReturn(10);

        final AboutToStartOrSubmitResponse<CaseData, State> response = contactPartiesSelectDocument.midEvent(caseDetails, caseDetails);
        assertTrue(response.getErrors().isEmpty());
    }

    @Test
    void midEventReturnsErrorWhenMaxDocumentsExceeded() {
        final CaseDetails<CaseData, State> caseDetails = new CaseDetails<>();
        final CaseData caseData = CaseData.builder()
            .contactPartiesDocuments(contactPartiesDocuments)
            .build();
        caseDetails.setData(caseData);
        when(contactPartiesDocuments.getDocumentList()).thenReturn(dynamicMultiSelectList);
        when(dynamicMultiSelectList.getValue()).thenReturn(dynamicList);
        when(dynamicList.size()).thenReturn(11);

        final AboutToStartOrSubmitResponse<CaseData, State> response = contactPartiesSelectDocument.midEvent(caseDetails, caseDetails);
        assertThat(response.getErrors()).hasSize(1);
        assertThat(response.getErrors()).contains("Select up to 10 documents");
    }

    @Test
    void midEventReturnsWithNoDocumentsSelected() {
        final CaseDetails<CaseData, State> caseDetails = new CaseDetails<>();
        final CaseData caseData = CaseData.builder().build();
        caseDetails.setData(caseData);

        final AboutToStartOrSubmitResponse<CaseData, State> response = contactPartiesSelectDocument.midEvent(caseDetails, caseDetails);
        assertThat(response.getErrors()).isEmpty();
    }
}
