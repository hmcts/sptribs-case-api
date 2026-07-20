package uk.gov.hmcts.sptribs.caseworker.event;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.ccd.sdk.ConfigBuilderImpl;
import uk.gov.hmcts.ccd.sdk.api.CaseDetails;
import uk.gov.hmcts.ccd.sdk.api.Event;
import uk.gov.hmcts.ccd.sdk.type.FlagDetail;
import uk.gov.hmcts.ccd.sdk.type.Flags;
import uk.gov.hmcts.ccd.sdk.type.ListValue;
import uk.gov.hmcts.ccd.sdk.type.YesOrNo;
import uk.gov.hmcts.reform.ccd.client.model.SubmittedCallbackResponse;
import uk.gov.hmcts.sptribs.caseworker.util.CaseFlagsUtil;
import uk.gov.hmcts.sptribs.ciccase.model.ApplicantCIC;
import uk.gov.hmcts.sptribs.ciccase.model.CaseData;
import uk.gov.hmcts.sptribs.ciccase.model.CicCase;
import uk.gov.hmcts.sptribs.ciccase.model.State;
import uk.gov.hmcts.sptribs.ciccase.model.UserRole;
import uk.gov.hmcts.sptribs.common.repositories.AnonymisationRepository;
import uk.gov.hmcts.sptribs.common.service.AnonymisationService;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;
import static uk.gov.hmcts.sptribs.ciccase.model.State.AwaitingHearing;
import static uk.gov.hmcts.sptribs.ciccase.model.State.AwaitingOutcome;
import static uk.gov.hmcts.sptribs.ciccase.model.State.CaseManagement;
import static uk.gov.hmcts.sptribs.ciccase.model.State.ReadyToList;
import static uk.gov.hmcts.sptribs.ciccase.model.State.Submitted;
import static uk.gov.hmcts.sptribs.testutil.ConfigTestUtil.createCaseDataConfigBuilder;
import static uk.gov.hmcts.sptribs.testutil.ConfigTestUtil.getEventsFrom;
import static uk.gov.hmcts.sptribs.testutil.TestConstants.TEST_CASE_ID;
import static uk.gov.hmcts.sptribs.testutil.TestDataHelper.LOCAL_DATE_TIME;
import static uk.gov.hmcts.sptribs.testutil.TestDataHelper.caseData;
import static uk.gov.hmcts.sptribs.testutil.TestEventConstants.CASEWORKER_MANAGE_CASE_FLAG;

@ExtendWith(MockitoExtension.class)
class CaseworkerManageCaseFlagTest {

    @Mock
    private AnonymisationRepository anonymisationRepository;

    private AnonymisationService anonymisationService;

    private CaseworkerManageCaseFlag caseworkerManageCaseFlag;

    @BeforeEach
    void setUp() {
        anonymisationService = spy(new AnonymisationService(anonymisationRepository));
        caseworkerManageCaseFlag = new CaseworkerManageCaseFlag(anonymisationService);
    }

    @Test
    void shouldAddConfigurationToConfigBuilder() {
        final ConfigBuilderImpl<CaseData, State, UserRole> configBuilder = createCaseDataConfigBuilder();

        caseworkerManageCaseFlag.configure(configBuilder);

        assertThat(getEventsFrom(configBuilder).values()).extracting(Event::getId)
            .contains(CASEWORKER_MANAGE_CASE_FLAG);

        assertThat(getEventsFrom(configBuilder).values()).extracting(Event::getPreState)
            .contains(Set.of(Submitted, CaseManagement, AwaitingHearing, AwaitingOutcome, ReadyToList));
    }

    @Test
    void shouldSuccessfullyUpdateFlag() {
        final CaseData caseData = caseData();
        final CaseDetails<CaseData, State> updatedCaseDetails = new CaseDetails<>();
        final CaseDetails<CaseData, State> beforeDetails = new CaseDetails<>();
        final CicCase cicCase = CicCase.builder().build();
        final Set<ApplicantCIC> set = new HashSet<>();
        set.add(ApplicantCIC.APPLICANT_CIC);
        cicCase.setApplicantCIC(set);
        cicCase.setApplicantFullName("Jane Doe");
        cicCase.setNotifyPartyApplicant(set);
        caseData.setCicCase(cicCase);
        updatedCaseDetails.setData(caseData);
        updatedCaseDetails.setId(TEST_CASE_ID);
        updatedCaseDetails.setCreatedDate(LOCAL_DATE_TIME);

        SubmittedCallbackResponse response = caseworkerManageCaseFlag.submitted(updatedCaseDetails, beforeDetails);

        assertThat(response).isNotNull();
        assertThat(response.getConfirmationHeader()).isEqualTo("# Flag updated");

    }

    @Test
    void shouldKeepOriginalListItemIdAndCopyLatestAnonymityFlagDetails() {
        CaseData caseData = CaseData.builder().cicCase(CicCase.builder()
            .anonymiseYesOrNo(YesOrNo.YES).anonymityAlreadyApplied(YesOrNo.YES).anonymisedAppellantName("AA").build()).build();

        List<ListValue<FlagDetail>> flagDetails = new ArrayList<>();
        flagDetails.add(buildAnonymityFlag("2", "Active", "Latest comment", YesOrNo.YES));
        flagDetails.add(buildAnonymityFlag("1", "Inactive", "Old comment", YesOrNo.NO));
        caseData.setCaseFlags(Flags.builder().details(flagDetails).build());

        CaseData beforeData = CaseData.builder()
            .caseFlags(Flags.builder()
                .details(List.of(buildAnonymityFlag("1", "Inactive", "Old comment", YesOrNo.NO))).build()).build();

        CaseDetails<CaseData, State> details = CaseDetails.<CaseData, State>builder().data(caseData).build();
        CaseDetails<CaseData, State> beforeDetails = CaseDetails.<CaseData, State>builder().data(beforeData).build();
        var response = caseworkerManageCaseFlag.aboutToSubmit(details, beforeDetails);

        assertThat(response.getData().getCicCase().getAnonymiseYesOrNo())
            .isEqualTo(YesOrNo.YES);
        assertThat(response.getData().getCaseFlags().getDetails()).hasSize(1);
        assertThat(response.getData().getCaseFlags().getDetails().getFirst().getId())
            .isEqualTo("1");
        assertThat(response.getData().getCaseFlags().getDetails().getFirst().getValue().getStatus())
            .isEqualTo("Active");
        assertThat(response.getData().getCaseFlags().getDetails().getFirst().getValue().getFlagComment())
            .isEqualTo("Latest comment");
        assertThat(response.getData().getCaseFlags().getDetails().getFirst().getValue().getHearingRelevant())
            .isEqualTo(YesOrNo.YES);
        assertThat(response.getData().getCaseFlags().getDetails().getFirst().getValue().getDateTimeCreated())
            .isEqualTo(LocalDateTime.of(2024, 1, 1, 10, 0));
        assertThat(response.getData().getCaseFlags().getDetails().getFirst().getValue().getDateTimeModified())
            .isNotNull();
        verify(anonymisationService).processAnonymityFlag(eq(caseData), eq(beforeData), anyList());
    }

    @Test
    void shouldSetAnonymityFieldsToNoWhenOnlyInactiveFlagExists() {
        CaseData caseData = CaseData.builder()
            .cicCase(CicCase.builder()
                .anonymiseYesOrNo(YesOrNo.YES)
                .anonymityAlreadyApplied(YesOrNo.YES)
                .anonymisedAppellantName("AA").build())
            .build();

        caseData.setCaseFlags(Flags.builder()
            .details(List.of(buildAnonymityFlag("1", "Inactive", null, null)))
            .build());

        CaseDetails<CaseData, State> details = CaseDetails.<CaseData, State>builder().data(caseData).build();
        var response = caseworkerManageCaseFlag.aboutToSubmit(details, CaseDetails.<CaseData, State>builder().build());

        assertThat(response.getData().getCicCase().getAnonymiseYesOrNo()).isEqualTo(YesOrNo.NO);
        assertThat(response.getData().getCaseFlags().getDetails()).hasSize(1);
        assertThat(response.getData().getCaseFlags().getDetails().getFirst().getValue().getStatus()).isEqualTo("Inactive");
    }

    private ListValue<FlagDetail> buildAnonymityFlag(String id, String status, String flagComment, YesOrNo hearingRelevant) {
        return ListValue.<FlagDetail>builder().id(id)
            .value(FlagDetail.builder()
                .flagCode(CaseFlagsUtil.ANONYMITY_FLAG_CODE)
                .status(status)
                .flagComment(flagComment)
                .hearingRelevant(hearingRelevant)
                .dateTimeCreated("1".equals(id) ? LocalDateTime.of(2024, 1, 1, 10, 0) : LocalDateTime.of(2025, 1, 1, 10, 0))
                .build())
            .build();
    }

}
