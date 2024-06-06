package uk.gov.hmcts.sptribs.caseworker.event;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.ccd.sdk.ConfigBuilderImpl;
import uk.gov.hmcts.ccd.sdk.api.CaseDetails;
import uk.gov.hmcts.ccd.sdk.api.Event;
import uk.gov.hmcts.ccd.sdk.api.callback.AboutToStartOrSubmitResponse;
import uk.gov.hmcts.ccd.sdk.type.ListValue;
import uk.gov.hmcts.reform.ccd.client.model.SubmittedCallbackResponse;
import uk.gov.hmcts.sptribs.caseworker.helper.RecordListHelper;
import uk.gov.hmcts.sptribs.caseworker.model.HearingSummary;
import uk.gov.hmcts.sptribs.caseworker.model.Listing;
import uk.gov.hmcts.sptribs.caseworker.service.HearingService;
import uk.gov.hmcts.sptribs.ciccase.model.CaseData;
import uk.gov.hmcts.sptribs.ciccase.model.CicCase;
import uk.gov.hmcts.sptribs.ciccase.model.RepresentativeCIC;
import uk.gov.hmcts.sptribs.ciccase.model.RespondentCIC;
import uk.gov.hmcts.sptribs.ciccase.model.State;
import uk.gov.hmcts.sptribs.ciccase.model.SubjectCIC;
import uk.gov.hmcts.sptribs.ciccase.model.UserRole;
import uk.gov.hmcts.sptribs.document.model.CaseworkerCICDocument;
import uk.gov.hmcts.sptribs.document.model.CaseworkerCICDocumentUpload;
import uk.gov.hmcts.sptribs.document.model.DocumentType;
import uk.gov.hmcts.sptribs.judicialrefdata.JudicialService;

import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.sptribs.testutil.ConfigTestUtil.createCaseDataConfigBuilder;
import static uk.gov.hmcts.sptribs.testutil.ConfigTestUtil.getEventsFrom;
import static uk.gov.hmcts.sptribs.testutil.TestDataHelper.getCaseworkerCICDocumentList;
import static uk.gov.hmcts.sptribs.testutil.TestDataHelper.getCaseworkerCICDocumentUploadList;
import static uk.gov.hmcts.sptribs.testutil.TestDataHelper.getRecordListing;
import static uk.gov.hmcts.sptribs.testutil.TestEventConstants.CASEWORKER_EDIT_HEARING_SUMMARY;

@ExtendWith(MockitoExtension.class)
class CaseworkerEditHearingSummaryTest {

    @Mock
    private RecordListHelper recordListHelper;

    @InjectMocks
    private CaseWorkerEditHearingSummary caseWorkerEditHearingSummary;

    @Mock
    private HearingService hearingService;

    @Mock
    private JudicialService judicialService;

    @Test
    void shouldAddConfigurationToConfigBuilder() {
        final ConfigBuilderImpl<CaseData, State, UserRole> configBuilder = createCaseDataConfigBuilder();

        caseWorkerEditHearingSummary.configure(configBuilder);

        assertThat(getEventsFrom(configBuilder).values())
            .extracting(Event::getId)
            .contains(CASEWORKER_EDIT_HEARING_SUMMARY);
    }

    @Test
    void shouldRunAboutToStart() {
        final CaseDetails<CaseData, State> updatedCaseDetails = new CaseDetails<>();
        final CicCase cicCase = CicCase.builder().build();
        final CaseData caseData = CaseData.builder()
            .cicCase(cicCase)
            .build();
        updatedCaseDetails.setData(caseData);
        Listing recordListing = getRecordListing();
        caseData.setListing(recordListing);
        List<ListValue<CaseworkerCICDocument>> documentList = getCaseworkerCICDocumentList("file.pdf");
        HearingSummary hearingSummary = HearingSummary.builder().recFile(documentList).build();
        recordListing.setSummary(hearingSummary);

        AboutToStartOrSubmitResponse<CaseData, State> response = caseWorkerEditHearingSummary.aboutToStart(updatedCaseDetails);

        assertThat(response).isNotNull();
        assertThat(response.getData().getCicCase().getHearingList()).isNull();
        assertThat(response.getData().getCurrentEvent()).isEqualTo(CASEWORKER_EDIT_HEARING_SUMMARY);
        assertThat(response.getData().getListing().getSummary().getRecFileUpload()).hasSize(1);
        assertThat(response.getData().getListing().getSummary().getRecFileUpload().get(0).getValue().getDocumentCategory())
            .isEqualTo(DocumentType.LINKED_DOCS);
        assertThat(response.getData().getListing().getSummary().getRecFileUpload().get(0).getValue().getDocumentEmailContent())
            .isEqualTo("some email content");
        assertThat(response.getData().getListing().getSummary().getRecFileUpload().get(0).getValue().getDocumentLink().getFilename())
            .isEqualTo("file.pdf");
    }

    @Test
    void shouldRunAboutToSubmit() {
        final CaseDetails<CaseData, State> updatedCaseDetails = new CaseDetails<>();
        final CicCase cicCase = CicCase.builder()
            .notifyPartyRepresentative(Set.of(RepresentativeCIC.REPRESENTATIVE))
            .notifyPartyRespondent(Set.of(RespondentCIC.RESPONDENT))
            .notifyPartySubject(Set.of(SubjectCIC.SUBJECT))
            .build();
        final CaseData caseData = CaseData.builder()
            .cicCase(cicCase)
            .build();
        final CaseDetails<CaseData, State> beforeDetails = new CaseDetails<>();

        Listing recordListing = getRecordListing();
        caseData.setListing(recordListing);
        List<ListValue<CaseworkerCICDocumentUpload>> documentList = getCaseworkerCICDocumentUploadList("file.pdf");
        HearingSummary hearingSummary = HearingSummary.builder().recFileUpload(documentList).build();
        recordListing.setSummary(hearingSummary);

        updatedCaseDetails.setData(caseData);

        when(judicialService.populateJudicialId(any())).thenReturn("personal_code");

        AboutToStartOrSubmitResponse<CaseData, State> response =
            caseWorkerEditHearingSummary.aboutToSubmit(updatedCaseDetails, beforeDetails);

        assertThat(response).isNotNull();
        assertThat(response.getData().getJudicialId())
            .isEqualTo("personal_code");
        assertThat(response.getData().getListing().getSummary().getJudgeList())
            .isNull();
        assertThat(response.getData().getListing().getSummary().getRecFileUpload()).hasSize(0);
        assertThat(response.getData().getListing().getSummary().getRecFile()).hasSize(0);
    }

    @Test
    void shouldRunSubmitted() {
        final CaseDetails<CaseData, State> updatedCaseDetails = new CaseDetails<>();
        final CaseDetails<CaseData, State> beforeDetails = new CaseDetails<>();

        SubmittedCallbackResponse response =
            caseWorkerEditHearingSummary.summaryCreated(updatedCaseDetails, beforeDetails);

        assertThat(response).isNotNull();
        assertThat(response.getConfirmationHeader()).contains("Hearing summary edited");
    }

}
