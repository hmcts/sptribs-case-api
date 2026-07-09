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
import uk.gov.hmcts.sptribs.ciccase.model.CaseData;
import uk.gov.hmcts.sptribs.ciccase.model.CicCase;
import uk.gov.hmcts.sptribs.ciccase.model.State;
import uk.gov.hmcts.sptribs.ciccase.model.UserRole;
import uk.gov.hmcts.sptribs.common.repositories.AnonymisationRepository;
import uk.gov.hmcts.sptribs.common.service.AnonymisationService;
import uk.gov.hmcts.sptribs.common.service.CcdSupplementaryDataService;
import uk.gov.hmcts.sptribs.notification.dispatcher.AnonymityAppliedNotification;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;
import static uk.gov.hmcts.sptribs.testutil.ConfigTestUtil.createCaseDataConfigBuilder;
import static uk.gov.hmcts.sptribs.testutil.ConfigTestUtil.getEventsFrom;
import static uk.gov.hmcts.sptribs.testutil.TestConstants.TEST_CASE_ID;
import static uk.gov.hmcts.sptribs.testutil.TestEventConstants.CASEWORKER_CASE_FLAG;

@ExtendWith(MockitoExtension.class)
class CaseworkerCaseFlagTest {

    @Mock
    private CcdSupplementaryDataService coreCaseApiService;

    @Mock
    private AnonymityAppliedNotification anonymityAppliedNotification;

    @Mock
    private AnonymisationRepository anonymisationRepository;

    private AnonymisationService anonymisationService;

    private CaseworkerCaseFlag caseworkerCaseFlag;

    @BeforeEach
    void setUp() {
        anonymisationService = spy(new AnonymisationService(anonymisationRepository));
        caseworkerCaseFlag = new CaseworkerCaseFlag(coreCaseApiService, anonymisationService, anonymityAppliedNotification);
    }

    @Test
    void shouldAddConfigurationToConfigBuilder() {
        final ConfigBuilderImpl<CaseData, State, UserRole> configBuilder = createCaseDataConfigBuilder();

        caseworkerCaseFlag.configure(configBuilder);

        assertThat(getEventsFrom(configBuilder).values())
            .extracting(Event::getId)
            .contains(CASEWORKER_CASE_FLAG);
    }

    @Test
    void shouldSuccessfullyAddFlagSubject() {
        final CaseDetails<CaseData, State> details = new CaseDetails<>();
        details.setId(TEST_CASE_ID);

        SubmittedCallbackResponse submittedCallbackResponse = caseworkerCaseFlag.submitted(details, details);

        assertThat(submittedCallbackResponse.getConfirmationHeader()).contains("Flag created");
        verify(coreCaseApiService).submitSupplementaryDataToCcd(TEST_CASE_ID.toString());
    }

    @Test
    void shouldKeepOriginalListItemIdAndCopyLatestAnonymityFlagDetailsOnCreate() {
        CaseData caseData = CaseData.builder().cicCase(new CicCase()).build();
        ArrayList<ListValue<FlagDetail>> flags = new ArrayList<>();
        flags.add(buildAnonymityFlag("2", "Active", "Latest comment"));
        flags.add(buildAnonymityFlag("1", "Inactive", "Old comment"));
        caseData.setCaseFlags(Flags.builder().details(flags).build());

        CaseData beforeData = CaseData.builder()
            .caseFlags(Flags.builder()
                .details(new ArrayList<>(List.of(buildAnonymityFlag("1", "Inactive", "Old comment"))))
                .build())
            .build();

        CaseDetails<CaseData, State> details = CaseDetails.<CaseData, State>builder().data(caseData).build();
        CaseDetails<CaseData, State> beforeDetails = CaseDetails.<CaseData, State>builder().data(beforeData).build();

        doReturn("AC").when(anonymisationService).getOrCreateAnonymisation();

        var response = caseworkerCaseFlag.aboutToSubmit(details, beforeDetails);

        assertThat(response.getData().getCaseFlags().getDetails()).hasSize(1);
        assertThat(response.getData().getCaseFlags().getDetails().getFirst().getId()).isEqualTo("1");
        assertThat(response.getData().getCaseFlags().getDetails().getFirst().getValue().getStatus()).isEqualTo("Active");
        assertThat(response.getData().getCaseFlags().getDetails().getFirst().getValue().getFlagComment())
            .isEqualTo("Latest comment");
        assertThat(response.getData().getCaseFlags().getDetails().getFirst().getValue().getDateTimeCreated())
            .isEqualTo(LocalDateTime.of(2024, 1, 1, 10, 0));
        assertThat(response.getData().getCaseFlags().getDetails().getFirst().getValue().getDateTimeModified())
            .isNotNull();
        verify(anonymisationService).processAnonymityFlag(eq(caseData), eq(beforeData), anyList());
    }

    private ListValue<FlagDetail> buildAnonymityFlag(String id, String status, String flagComment) {
        return ListValue.<FlagDetail>builder()
            .id(id)
            .value(FlagDetail.builder()
                .flagCode(CaseFlagsUtil.ANONYMITY_FLAG_CODE)
                .status(status)
                .flagComment(flagComment)
                .dateTimeCreated("1".equals(id) ? LocalDateTime.of(2024, 1, 1, 10, 0) : LocalDateTime.of(2025, 1, 1, 10, 0))
                .build())
            .build();
    }

    @Test
    void shouldSendAnonymityNotificationWhenAnonymityIsNewlyApplied() {
        final CaseData caseData = CaseData.builder()
            .hyphenatedCaseRef("1234-5678-9012-3456")
            .cicCase(CicCase.builder()
                .anonymiseYesOrNo(YesOrNo.YES)
                .anonymisedAppellantName("AAC")
                .build())
            .build();

        final CaseData beforeCaseData = CaseData.builder()
            .cicCase(CicCase.builder()
                .anonymiseYesOrNo(YesOrNo.NO)
                .anonymityAlreadyApplied(YesOrNo.NO)
                .anonymisedAppellantName(null)
                .build())
            .build();

        final CaseDetails<CaseData, State> caseDetails = new CaseDetails<>();
        caseDetails.setId(TEST_CASE_ID);
        caseDetails.setData(caseData);
        final CaseDetails<CaseData, State> beforeDetails = new CaseDetails<>();
        beforeDetails.setData(beforeCaseData);

        SubmittedCallbackResponse submittedResponse = caseworkerCaseFlag.submitted(caseDetails, beforeDetails);

        assertThat(submittedResponse.getConfirmationHeader()).contains("Flag created");
        verify(anonymityAppliedNotification).sendAnonymityNotificationIfNewlyApplied(caseData, beforeCaseData, TEST_CASE_ID.toString());
    }
}
