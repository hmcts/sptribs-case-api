package uk.gov.hmcts.sptribs.common.service;

import com.google.common.collect.Lists;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.ccd.sdk.type.Document;
import uk.gov.hmcts.ccd.sdk.type.ListValue;
import uk.gov.hmcts.sptribs.ciccase.model.Applicant;
import uk.gov.hmcts.sptribs.ciccase.model.CaseData;
import uk.gov.hmcts.sptribs.ciccase.model.CaseDocuments;
import uk.gov.hmcts.sptribs.ciccase.model.ContactDetailsType;
import uk.gov.hmcts.sptribs.document.model.ConfidentialDivorceDocument;
import uk.gov.hmcts.sptribs.document.model.ConfidentialDocumentsReceived;
import uk.gov.hmcts.sptribs.document.model.DivorceDocument;
import uk.gov.hmcts.sptribs.document.model.DocumentType;

import java.util.List;
import java.util.stream.Collectors;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static uk.gov.hmcts.sptribs.testutil.TestConstants.TEST_CASE_ID;

@ExtendWith(MockitoExtension.class)
public class ProcessConfidentialDocumentsServiceTest {

    private final ListValue<DivorceDocument> applicant1Nop = ListValue.<DivorceDocument>builder()
        .id("1111")
        .value(buildDivorceDocument(DocumentType.NOTICE_OF_PROCEEDINGS_APP_1))
        .build();

    private final ListValue<DivorceDocument> applicant2Nop = ListValue.<DivorceDocument>builder()
        .id("2222")
        .value(buildDivorceDocument(DocumentType.NOTICE_OF_PROCEEDINGS_APP_2))
        .build();

    private final ListValue<DivorceDocument> applicationDoc = ListValue.<DivorceDocument>builder()
        .id("3333")
        .value(buildDivorceDocument(DocumentType.APPLICATION))
        .build();

    private final ListValue<ConfidentialDivorceDocument> applicant1NopConfidential
        = ListValue.<ConfidentialDivorceDocument>builder()
        .id("1111")
        .value(buildConfidentialDivorceDocument(ConfidentialDocumentsReceived.NOTICE_OF_PROCEEDINGS_APP_1))
        .build();

    private final ListValue<ConfidentialDivorceDocument> applicant2NopConfidential
        = ListValue.<ConfidentialDivorceDocument>builder()
        .id("2222")
        .value(buildConfidentialDivorceDocument(ConfidentialDocumentsReceived.NOTICE_OF_PROCEEDINGS_APP_2))
        .build();

    @InjectMocks
    private ProcessConfidentialDocumentsService documentsService;

    @Test
    public void processDocumentsShouldMoveNOPDocumentsToConfidentialDocumentsGeneratedWhenContactIsPrivateForBothApplicants() {
        //Given
        CaseData caseData = CaseData.builder()
            .applicant1(Applicant.builder().contactDetailsType(ContactDetailsType.PRIVATE).build())
            .applicant2(Applicant.builder().contactDetailsType(ContactDetailsType.PRIVATE).build())
            .documents(CaseDocuments.builder()
                .documentsGenerated(Lists.newArrayList(applicant1Nop, applicant2Nop, applicationDoc))
                .build())
            .build();

        //When
        documentsService.processDocuments(caseData, TEST_CASE_ID);

        //Then
        List<ListValue<DivorceDocument>> nonConfidentialDocuments = caseData.getDocuments().getDocumentsGenerated();
        List<ListValue<ConfidentialDivorceDocument>> confidentialDocuments = caseData.getDocuments().getConfidentialDocumentsGenerated();

        assertEquals(1, nonConfidentialDocuments.size());
        assertEquals(2, confidentialDocuments.size());
        Assertions.assertNull(confidentialDocuments.get(0).getValue().getDocumentEmailContent());
        org.assertj.core.api.Assertions.assertThat(confidentialDocuments.get(0).getValue().getDocumentLink()).isNotNull();
        Assertions.assertNull(confidentialDocuments.get(0).getValue().getDocumentDateAdded());
        Assertions.assertNull(confidentialDocuments.get(0).getValue().getDocumentComment());
        Assertions.assertNull(confidentialDocuments.get(0).getValue().getDocumentFileName());

        assertEquals(DocumentType.APPLICATION,
            nonConfidentialDocuments.get(0).getValue().getDocumentType());

        assertThat(
            confidentialDocuments.stream()
                .map(doc -> doc.getValue().getConfidentialDocumentsReceived())
                .collect(Collectors.toList()),
            containsInAnyOrder(ConfidentialDocumentsReceived.NOTICE_OF_PROCEEDINGS_APP_1,
                ConfidentialDocumentsReceived.NOTICE_OF_PROCEEDINGS_APP_2)
        );
    }

    @Test
    public void processDocumentsShouldMoveNOPDocumentsToConfidentialDocumentsGeneratedWhenContactIsPrivateForApplicant1() {
        //Given
        CaseData caseData = CaseData.builder()
            .applicant1(Applicant.builder().contactDetailsType(ContactDetailsType.PRIVATE).build())
            .documents(CaseDocuments.builder()
                .documentsGenerated(Lists.newArrayList(applicant1Nop, applicant2Nop, applicationDoc))
                .build())
            .build();

        //When
        documentsService.processDocuments(caseData, caseData.getApplicant1(), true);

        //Then
        List<ListValue<DivorceDocument>> nonConfidentialDocuments = caseData.getDocuments().getDocumentsGenerated();
        List<ListValue<ConfidentialDivorceDocument>> confidentialDocuments = caseData.getDocuments().getConfidentialDocumentsGenerated();

        assertEquals(2, nonConfidentialDocuments.size());
        assertEquals(1, confidentialDocuments.size());

        assertEquals(ConfidentialDocumentsReceived.NOTICE_OF_PROCEEDINGS_APP_1,
            confidentialDocuments.get(0).getValue().getConfidentialDocumentsReceived());

        assertThat(
            nonConfidentialDocuments.stream()
                .map(doc -> doc.getValue().getDocumentType())
                .collect(Collectors.toList()),
            containsInAnyOrder(
                DocumentType.NOTICE_OF_PROCEEDINGS_APP_2,
                DocumentType.APPLICATION)
        );
    }

    @Test
    public void processDocumentsShouldMoveNOPDocumentsToConfidentialDocumentsGeneratedWhenContactIsPrivateForApplicant2() {
        //Given
        CaseData caseData = CaseData.builder()
            .applicant2(Applicant.builder().contactDetailsType(ContactDetailsType.PRIVATE).build())
            .documents(CaseDocuments.builder()
                .documentsGenerated(Lists.newArrayList(applicant1Nop, applicant2Nop, applicationDoc))
                .build())
            .build();

        //When
        documentsService.processDocuments(caseData, caseData.getApplicant2(), false);

        //Then
        List<ListValue<DivorceDocument>> nonConfidentialDocuments = caseData.getDocuments().getDocumentsGenerated();
        List<ListValue<ConfidentialDivorceDocument>> confidentialDocuments = caseData.getDocuments().getConfidentialDocumentsGenerated();

        assertEquals(2, nonConfidentialDocuments.size());
        assertEquals(1, confidentialDocuments.size());

        assertEquals(ConfidentialDocumentsReceived.NOTICE_OF_PROCEEDINGS_APP_2.getLabel(),
            confidentialDocuments.get(0).getValue().getConfidentialDocumentsReceived().getLabel());

        assertThat(
            nonConfidentialDocuments.stream()
                .map(doc -> doc.getValue().getDocumentType())
                .collect(Collectors.toList()),
            containsInAnyOrder(
                DocumentType.NOTICE_OF_PROCEEDINGS_APP_1,
                DocumentType.APPLICATION)
        );
    }

    @Test
    public void processDocumentsShouldNotMoveConfidentialDocumentsToDocumentsGeneratedWhenContactIsNotPrivateForApplicant1() {
        //Given
        CaseData caseData = CaseData.builder()
            .applicant1(Applicant.builder().contactDetailsType(ContactDetailsType.PUBLIC).build())
            .documents(CaseDocuments.builder()
                .confidentialDocumentsGenerated(Lists.newArrayList(applicant1NopConfidential, applicant2NopConfidential))
                .documentsGenerated(Lists.newArrayList(applicationDoc))
                .build())
            .build();

        //When
        documentsService.processDocuments(caseData, caseData.getApplicant1(), true);

        //Then
        List<ListValue<DivorceDocument>> nonConfidentialDocuments = caseData.getDocuments().getDocumentsGenerated();
        List<ListValue<ConfidentialDivorceDocument>> confidentialDocuments = caseData.getDocuments().getConfidentialDocumentsGenerated();

        assertEquals(1, nonConfidentialDocuments.size());
        assertEquals(2, confidentialDocuments.size());

        assertThat(
            confidentialDocuments.stream()
                .map(doc -> doc.getValue().getConfidentialDocumentsReceived())
                .collect(Collectors.toList()),
            containsInAnyOrder(
                ConfidentialDocumentsReceived.NOTICE_OF_PROCEEDINGS_APP_1,
                ConfidentialDocumentsReceived.NOTICE_OF_PROCEEDINGS_APP_2)
        );

        assertEquals(DocumentType.APPLICATION,
            nonConfidentialDocuments.get(0).getValue().getDocumentType());
    }

    @Test
    public void processDocumentsShouldNotMoveConfidentialDocumentsToDocumentsGeneratedWhenContactIsNotPrivateForApplicant2() {
        //Given
        CaseData caseData = CaseData.builder()
            .applicant2(Applicant.builder().contactDetailsType(ContactDetailsType.PUBLIC).build())
            .documents(CaseDocuments.builder()
                .confidentialDocumentsGenerated(Lists.newArrayList(applicant1NopConfidential, applicant2NopConfidential))
                .documentsGenerated(Lists.newArrayList(applicationDoc))
                .build())
            .build();

        //When
        documentsService.processDocuments(caseData, caseData.getApplicant2(), false);

        //Then
        List<ListValue<DivorceDocument>> nonConfidentialDocuments = caseData.getDocuments().getDocumentsGenerated();
        List<ListValue<ConfidentialDivorceDocument>> confidentialDocuments = caseData.getDocuments().getConfidentialDocumentsGenerated();

        assertEquals(1, nonConfidentialDocuments.size());
        assertEquals(2, confidentialDocuments.size());

        assertThat(
            confidentialDocuments.stream()
                .map(doc -> doc.getValue().getConfidentialDocumentsReceived())
                .collect(Collectors.toList()),
            containsInAnyOrder(
                ConfidentialDocumentsReceived.NOTICE_OF_PROCEEDINGS_APP_1,
                ConfidentialDocumentsReceived.NOTICE_OF_PROCEEDINGS_APP_2)
        );

        assertEquals(DocumentType.APPLICATION,
            nonConfidentialDocuments.get(0).getValue().getDocumentType());
    }

    private DivorceDocument buildDivorceDocument(final DocumentType documentType) {
        return DivorceDocument.builder()
            .documentType(documentType)
            .documentLink(Document.builder().build())
            .build();
    }

    private ConfidentialDivorceDocument buildConfidentialDivorceDocument(final ConfidentialDocumentsReceived documentType) {
        return ConfidentialDivorceDocument.builder()
            .confidentialDocumentsReceived(documentType)
            .documentLink(Document.builder().build())
            .build();
    }
}
