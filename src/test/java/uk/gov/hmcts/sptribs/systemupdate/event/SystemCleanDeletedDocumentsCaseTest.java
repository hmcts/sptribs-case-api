package uk.gov.hmcts.sptribs.systemupdate.event;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.ccd.sdk.ConfigBuilderImpl;
import uk.gov.hmcts.ccd.sdk.api.CaseDetails;
import uk.gov.hmcts.ccd.sdk.api.Event;
import uk.gov.hmcts.ccd.sdk.api.callback.AboutToStartOrSubmitResponse;
import uk.gov.hmcts.ccd.sdk.type.Document;
import uk.gov.hmcts.ccd.sdk.type.ListValue;
import uk.gov.hmcts.sptribs.ciccase.model.CaseData;
import uk.gov.hmcts.sptribs.ciccase.model.State;
import uk.gov.hmcts.sptribs.ciccase.model.UserRole;
import uk.gov.hmcts.sptribs.document.model.CaseworkerCICDocument;
import uk.gov.hmcts.sptribs.document.model.DocumentType;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static uk.gov.hmcts.sptribs.systemupdate.event.SystemCleanDeletedDocumentsCase.SYSTEM_CLEAN_DELETED_DOCUMENTS;
import static uk.gov.hmcts.sptribs.testutil.ConfigTestUtil.createCaseDataConfigBuilder;
import static uk.gov.hmcts.sptribs.testutil.ConfigTestUtil.getEventsFrom;

@ExtendWith(MockitoExtension.class)
class SystemCleanDeletedDocumentsCaseTest {

    @InjectMocks
    private SystemCleanDeletedDocumentsCase systemCleanDeletedDocumentsCase;

    @Test
    void whenConfigure_thenShouldAddConfigurationToConfigBuilder() {
        final ConfigBuilderImpl<CaseData, State, UserRole> configBuilder = createCaseDataConfigBuilder();

        systemCleanDeletedDocumentsCase.configure(configBuilder);

        assertThat(getEventsFrom(configBuilder).values())
            .extracting(Event::getId)
            .contains(SYSTEM_CLEAN_DELETED_DOCUMENTS);
    }

    @Test
    void givenCaseData_whenAboutToSubmit_thenRemove2FurtherDocuments() {

        //Given
        ListValue<CaseworkerCICDocument> documentListValue1
            = buildCaseworkerCicDocumentListValue("url1", "1", "111");
        ListValue<CaseworkerCICDocument> documentListValue2
            = buildCaseworkerCicDocumentListValue("url2", "2", "222");
        ListValue<CaseworkerCICDocument> documentListValue3
            = buildCaseworkerCicDocumentListValue("url3", "3", "333");

        List<ListValue<CaseworkerCICDocument>> furtherDocs = new ArrayList<>();
        furtherDocs.add(documentListValue1);
        furtherDocs.add(documentListValue2);
        furtherDocs.add(documentListValue3);

        List<ListValue<CaseworkerCICDocument>> expectedAllDocs = new ArrayList<>();
        expectedAllDocs.add(documentListValue1);

        final CaseData caseData = CaseData.builder()
            .furtherUploadedDocuments(furtherDocs)
            .build();

        caseData.getAllDocManagement().setCaseworkerCICDocument(expectedAllDocs);

        final CaseDetails<CaseData, State> updatedCaseDetails = new CaseDetails<>();
        final CaseDetails<CaseData, State> beforeDetails = new CaseDetails<>();
        beforeDetails.setData(caseData);
        updatedCaseDetails.setData(caseData);

        //when
        final AboutToStartOrSubmitResponse<CaseData, State> response =
            systemCleanDeletedDocumentsCase.aboutToSubmit(updatedCaseDetails, beforeDetails);

        //then
        assertThat(response.getData().getFurtherUploadedDocuments().size()).isEqualTo(response.getData()
            .getAllDocManagement().getCaseworkerCICDocument().size());

    }

    @Test
    void givenCaseDataWithNullFurtherDocs_whenAboutToSubmit_thenNothingToRemove() {

        //Given
        ListValue<CaseworkerCICDocument> documentListValue1
            = buildCaseworkerCicDocumentListValue("url1", "1", "111");

        List<ListValue<CaseworkerCICDocument>> expectedAllDocs = new ArrayList<>();
        expectedAllDocs.add(documentListValue1);

        final CaseData caseData = CaseData.builder()
            .build();

        caseData.getAllDocManagement().setCaseworkerCICDocument(expectedAllDocs);

        final CaseDetails<CaseData, State> updatedCaseDetails = new CaseDetails<>();
        final CaseDetails<CaseData, State> beforeDetails = new CaseDetails<>();
        beforeDetails.setData(caseData);
        updatedCaseDetails.setData(caseData);

        //when
        final AboutToStartOrSubmitResponse<CaseData, State> response =
            systemCleanDeletedDocumentsCase.aboutToSubmit(updatedCaseDetails, beforeDetails);


        //then
        assertThat(response.getData().getFurtherUploadedDocuments()).isNull();

    }

    @Test
    void givenCaseDataWithEmptyFurtherDocs_whenAboutToSubmit_thenNothingToRemove() {

        //Given
        ListValue<CaseworkerCICDocument> documentListValue1
            = buildCaseworkerCicDocumentListValue("url1", "1", "111");

        List<ListValue<CaseworkerCICDocument>> expectedAllDocs = new ArrayList<>();
        expectedAllDocs.add(documentListValue1);

        final CaseData caseData = CaseData.builder()
            .furtherUploadedDocuments(Collections.emptyList())
            .build();

        caseData.getAllDocManagement().setCaseworkerCICDocument(expectedAllDocs);

        final CaseDetails<CaseData, State> updatedCaseDetails = new CaseDetails<>();
        final CaseDetails<CaseData, State> beforeDetails = new CaseDetails<>();
        beforeDetails.setData(caseData);
        updatedCaseDetails.setData(caseData);

        //when
        final AboutToStartOrSubmitResponse<CaseData, State> response =
            systemCleanDeletedDocumentsCase.aboutToSubmit(updatedCaseDetails, beforeDetails);


        //then
        assertThat(response.getData().getFurtherUploadedDocuments()).isEmpty();

    }

    @Test
    void givenCaseDataWithFurtherDocsSameAsExpected_whenAboutToSubmit_thenNothingToRemove() {

        //Given
        ListValue<CaseworkerCICDocument> documentListValue1
            = buildCaseworkerCicDocumentListValue("url1", "1", "111");

        List<ListValue<CaseworkerCICDocument>> expectedAllDocs = new ArrayList<>();
        expectedAllDocs.add(documentListValue1);

        List<ListValue<CaseworkerCICDocument>> furtherDocs = new ArrayList<>();
        furtherDocs.add(documentListValue1);

        final CaseData caseData = CaseData.builder()
            .furtherUploadedDocuments(furtherDocs)
            .build();

        caseData.getAllDocManagement().setCaseworkerCICDocument(expectedAllDocs);

        final CaseDetails<CaseData, State> updatedCaseDetails = new CaseDetails<>();
        final CaseDetails<CaseData, State> beforeDetails = new CaseDetails<>();
        beforeDetails.setData(caseData);
        updatedCaseDetails.setData(caseData);

        //when
        final AboutToStartOrSubmitResponse<CaseData, State> response =
            systemCleanDeletedDocumentsCase.aboutToSubmit(updatedCaseDetails, beforeDetails);


        //then
        assertThat(response.getData().getFurtherUploadedDocuments().size()).isEqualTo(response.getData()
            .getAllDocManagement().getCaseworkerCICDocument().size());

    }

    private ListValue<CaseworkerCICDocument> buildCaseworkerCicDocumentListValue(String url, String binary, String filename) {
        CaseworkerCICDocument document = CaseworkerCICDocument.builder()
            .documentCategory(DocumentType.TRIBUNAL_DIRECTION)
            .documentLink(Document.builder().url(url).binaryUrl(binary).filename(filename).build()).build();
        ListValue<CaseworkerCICDocument> caseworkerCICDocumentListValue = new ListValue<>();
        caseworkerCICDocumentListValue.setValue(document);
        return caseworkerCICDocumentListValue;
    }


}
