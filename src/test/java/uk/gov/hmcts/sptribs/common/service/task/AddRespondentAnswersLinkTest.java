package uk.gov.hmcts.sptribs.common.service.task;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.ccd.sdk.api.CaseDetails;
import uk.gov.hmcts.ccd.sdk.type.Document;
import uk.gov.hmcts.ccd.sdk.type.ListValue;
import uk.gov.hmcts.sptribs.ciccase.model.CaseData;
import uk.gov.hmcts.sptribs.ciccase.model.State;
import uk.gov.hmcts.sptribs.document.model.DivorceDocument;

import static java.util.Collections.singletonList;
import static org.assertj.core.api.Assertions.assertThat;
import static uk.gov.hmcts.sptribs.document.model.DocumentType.RESPONDENT_ANSWERS;
import static uk.gov.hmcts.sptribs.testutil.TestDataHelper.caseData;

@ExtendWith(MockitoExtension.class)
class AddRespondentAnswersLinkTest {

    @InjectMocks
    private AddRespondentAnswersLink addRespondentAnswersLink;

    @Test
    void shouldSetRespondentAnswersLinkIfGenerateAnswersDocumentPresent() {
        //Given
        final Document documentLink = new Document("url", "filename", "binary url");
        final ListValue<DivorceDocument> respondentAnswersListValue = ListValue.<DivorceDocument>builder()
            .value(DivorceDocument.builder()
                .documentType(RESPONDENT_ANSWERS)
                .documentLink(documentLink)
                .build())
            .build();

        final CaseData caseData = caseData();
        caseData.getDocuments().setDocumentsGenerated(singletonList(respondentAnswersListValue));

        final CaseDetails<CaseData, State> caseDetails = new CaseDetails<>();
        caseDetails.setData(caseData);

        //When
        final CaseDetails<CaseData, State> result = addRespondentAnswersLink.apply(caseDetails);

        //Then
        assertThat(result.getData().getConditionalOrder().getRespondentAnswersLink()).isSameAs(documentLink);
    }

    @Test
    void shouldNotSetMiniApplicationLinkIfNoDivorceApplicationDocumentPresent() {
        //Given
        final CaseData caseData = caseData();
        final CaseDetails<CaseData, State> caseDetails = new CaseDetails<>();
        caseDetails.setData(caseData);

        //When
        final CaseDetails<CaseData, State> result = addRespondentAnswersLink.apply(caseDetails);

        //Then
        assertThat(result.getData().getConditionalOrder().getRespondentAnswersLink()).isNull();
    }
}
