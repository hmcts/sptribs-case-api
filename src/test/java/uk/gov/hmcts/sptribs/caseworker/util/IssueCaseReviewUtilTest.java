package uk.gov.hmcts.sptribs.caseworker.util;

import org.junit.jupiter.api.Test;
import uk.gov.hmcts.ccd.sdk.type.Document;
import uk.gov.hmcts.ccd.sdk.type.DynamicListElement;
import uk.gov.hmcts.ccd.sdk.type.DynamicMultiSelectList;
import uk.gov.hmcts.ccd.sdk.type.ListValue;
import uk.gov.hmcts.sptribs.caseworker.model.CaseIssue;
import uk.gov.hmcts.sptribs.caseworker.model.Slot;
import uk.gov.hmcts.sptribs.ciccase.model.ApplicantCIC;
import uk.gov.hmcts.sptribs.ciccase.model.CaseData;
import uk.gov.hmcts.sptribs.ciccase.model.CicCase;
import uk.gov.hmcts.sptribs.ciccase.model.RepresentativeCIC;
import uk.gov.hmcts.sptribs.ciccase.model.RespondentCIC;
import uk.gov.hmcts.sptribs.ciccase.model.SubjectCIC;
import uk.gov.hmcts.sptribs.document.model.CaseworkerCICDocument;

import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.IntStream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;

class IssueCaseReviewUtilTest {

    @Test
    void clearingMissingReviewDocumentsDoesNothing() {
        assertThatCode(() -> IssueCaseReviewUtil.clearReviewDocuments(null)).doesNotThrowAnyException();
    }

    @Test
    void showsAllTenSelectedDocumentsAndClearsThemWhenSelectionIsEmpty() {
        List<ListValue<CaseworkerCICDocument>> documents = IntStream.rangeClosed(1, 10)
            .mapToObj(index -> {
                ListValue<CaseworkerCICDocument> value = new ListValue<>();
                value.setValue(CaseworkerCICDocument.builder()
                    .documentLink(Document.builder().filename("document" + index + ".pdf")
                        .url("https://example.test/documents/" + UUID.randomUUID()).build()).build());
                return value;
            }).toList();
        List<DynamicListElement> selection = documents.stream().map(value -> {
            Document document = value.getValue().getDocumentLink();
            return DynamicListElement.builder().code(UUID.randomUUID())
                .label("[" + document.getFilename() + "](" + document.getUrl() + "/binary)").build();
        }).toList();
        CaseData data = CaseData.builder()
            .cicCase(CicCase.builder().reinstateDocuments(documents).build())
            .caseIssue(CaseIssue.builder()
                .documentList(DynamicMultiSelectList.builder().value(selection).listItems(selection).build()).build())
            .build();

        assertThat(IssueCaseReviewUtil.setReviewDocuments(data)).isTrue();

        assertThat(data.getCaseIssue().getActiveDocumentSlots()).containsExactlyInAnyOrder(Slot.values());
        assertThat(data.getCaseIssue().getD01()).isEqualTo(documents.getFirst().getValue());
        assertThat(data.getCaseIssue().getD10()).isEqualTo(documents.get(9).getValue());

        data.getCaseIssue().getDocumentList().setValue(List.of());
        assertThat(IssueCaseReviewUtil.setReviewDocuments(data)).isTrue();

        assertThat(data.getCaseIssue().getActiveDocumentSlots()).isEmpty();
        assertThat(data.getCaseIssue().getD01()).isNull();
        assertThat(data.getCaseIssue().getD10()).isNull();

        IssueCaseReviewUtil.clearReviewDocuments(data.getCaseIssue());
        assertThat(data.getCaseIssue().getActiveDocumentSlots()).isNull();
    }

    @Test
    void duplicateFileDoesNotDisplaceTheTenthSelectedDocument() {
        String sharedUrl = "https://example.test/documents/" + UUID.randomUUID();
        List<ListValue<CaseworkerCICDocument>> documents = IntStream.rangeClosed(0, 10)
            .mapToObj(index -> {
                String url = index < 2 ? sharedUrl : "https://example.test/documents/" + UUID.randomUUID();
                CaseworkerCICDocument document = CaseworkerCICDocument.builder()
                    .documentLink(Document.builder().filename("document" + index + ".pdf").url(url).build())
                    .build();
                return new ListValue<>(UUID.randomUUID().toString(), document);
            }).toList();
        List<DynamicListElement> options = IntStream.rangeClosed(0, 10)
            .mapToObj(index -> DynamicListElement.builder().code(UUID.randomUUID()).build())
            .toList();
        DynamicMultiSelectList list = DynamicMultiSelectList.builder()
            .listItems(options)
            .value(options.subList(1, 11))
            .build();
        CaseData data = CaseData.builder()
            .cicCase(CicCase.builder().reinstateDocuments(documents).build())
            .caseIssue(CaseIssue.builder().documentList(list).build())
            .build();

        assertThat(IssueCaseReviewUtil.setReviewDocuments(data)).isTrue();

        assertThat(data.getCaseIssue().getActiveDocumentSlots()).containsExactlyInAnyOrder(Slot.values());
        assertThat(data.getCaseIssue().getD01()).isEqualTo(documents.get(1).getValue());
        assertThat(data.getCaseIssue().getD10()).isEqualTo(documents.get(10).getValue());
    }

    @Test
    void shouldFormatSelectedParties() {
        CicCase cicCase = CicCase.builder()
            .fullName("Subject Name")
            .notifyPartySubject(Set.of(SubjectCIC.SUBJECT))
            .applicantFullName("Applicant Name")
            .notifyPartyApplicant(Set.of(ApplicantCIC.APPLICANT_CIC))
            .representativeFullName("Rep Name")
            .notifyPartyRepresentative(Set.of(RepresentativeCIC.REPRESENTATIVE))
            .respondentName("Resp Name")
            .notifyPartyRespondent(Set.of(RespondentCIC.RESPONDENT))
            .build();

        String result = IssueCaseReviewUtil.issueCaseSelectedParties(cicCase);
        assertThat(result).isEqualTo("Subject: Subject Name\nApplicant: Applicant Name\nRepresentative: Rep Name\nRespondent: Resp Name");
    }

    @Test
    void setReviewDocumentsReturnsFalseWhenCaseIssueIsNull() {
        CaseData data = CaseData.builder().caseIssue(null).build();
        assertThat(IssueCaseReviewUtil.setReviewDocuments(data)).isFalse();
    }
}
