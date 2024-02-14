package uk.gov.hmcts.sptribs.citizen.event;

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
import uk.gov.hmcts.sptribs.ciccase.model.CicCase;
import uk.gov.hmcts.sptribs.ciccase.model.DssCaseData;
import uk.gov.hmcts.sptribs.ciccase.model.State;
import uk.gov.hmcts.sptribs.ciccase.model.UserRole;
import uk.gov.hmcts.sptribs.document.model.CaseworkerCICDocument;
import uk.gov.hmcts.sptribs.document.model.EdgeCaseDocument;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static uk.gov.hmcts.sptribs.caseworker.util.EventConstants.CITIZEN_DSS_UPDATE_CASE_SUBMISSION;
import static uk.gov.hmcts.sptribs.document.model.DocumentType.DSS_TRIBUNAL_FORM;
import static uk.gov.hmcts.sptribs.testutil.ConfigTestUtil.createCaseDataConfigBuilder;
import static uk.gov.hmcts.sptribs.testutil.ConfigTestUtil.getEventsFrom;

@ExtendWith(MockitoExtension.class)
public class CicDssUpdateCaseEventTest {

    @InjectMocks
    private CicDssUpdateCaseEvent cicDssUpdateCaseEvent;

    @Test
    void shouldAddConfigurationToConfigBuilder() {
        final ConfigBuilderImpl<CaseData, State, UserRole> configBuilder = createCaseDataConfigBuilder();

        cicDssUpdateCaseEvent.configure(configBuilder);

        assertThat(getEventsFrom(configBuilder).values())
            .extracting(Event::getId)
            .contains(CITIZEN_DSS_UPDATE_CASE_SUBMISSION);
    }

    @Test
    void shouldAddDocumentsToCaseDataInAboutToSubmitCallback() {
        final CaseworkerCICDocument caseworkerCICDocument =
            CaseworkerCICDocument.builder()
                .documentLink(Document.builder().build())
                .documentCategory(DSS_TRIBUNAL_FORM)
                .build();
        final List<ListValue<CaseworkerCICDocument>> applicantDocumentsUploaded = new ArrayList<>();
        applicantDocumentsUploaded.add(new ListValue<>("3", caseworkerCICDocument));

        final CaseData caseData = CaseData.builder()
            .cicCase(
                CicCase.builder()
                    .applicantDocumentsUploaded(applicantDocumentsUploaded)
                    .build()
            )
            .dssCaseData(getDssCaseData())
            .build();

        final CaseDetails<CaseData, State> details = new CaseDetails<>();
        details.setData(caseData);

        AboutToStartOrSubmitResponse<CaseData, State> response =
            cicDssUpdateCaseEvent.aboutToSubmit(details, details);

        assertThat(response.getData().getCicCase().getApplicantDocumentsUploaded()).isNotEmpty();
        assertThat(response.getData().getCicCase().getApplicantDocumentsUploaded().size()).isEqualTo(3);
    }

    @Test
    void shouldCreateNewApplicantDocumentsUploadedListIfEmptyInAboutToSubmitCallback() {
        final CaseData caseData = CaseData.builder()
            .cicCase(
                CicCase.builder()
                    .applicantDocumentsUploaded(new ArrayList<>())
                    .build()
            )
            .dssCaseData(getDssCaseData())
            .build();

        final CaseDetails<CaseData, State> details = new CaseDetails<>();
        details.setData(caseData);

        AboutToStartOrSubmitResponse<CaseData, State> response =
            cicDssUpdateCaseEvent.aboutToSubmit(details, details);

        assertThat(response.getData().getCicCase().getApplicantDocumentsUploaded()).isNotEmpty();
        assertThat(response.getData().getCicCase().getApplicantDocumentsUploaded().size()).isEqualTo(2);
    }

    private DssCaseData getDssCaseData() {
        EdgeCaseDocument doc1 = new EdgeCaseDocument();
        doc1.setDocumentLink(
            Document.builder()
                .filename("doc1.pdf")
                .binaryUrl("doc1.pdf/binary")
                .categoryId("test category")
                .build()
        );
        EdgeCaseDocument doc2 = new EdgeCaseDocument();
        doc2.setDocumentLink(
            Document.builder()
                .filename("doc2.pdf")
                .binaryUrl("doc2.pdf/binary")
                .categoryId("test category")
                .build()
        );
        final List<ListValue<EdgeCaseDocument>> dssCaseDataOtherInfoDocuments = List.of(
            new ListValue<>("1", doc1),
            new ListValue<>("2", doc2)
        );

        return DssCaseData.builder()
            .otherInfoDocuments(dssCaseDataOtherInfoDocuments)
            .build();
    }
}
