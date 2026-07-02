package uk.gov.hmcts.sptribs.caseworker.event;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.ccd.sdk.ConfigBuilderImpl;
import uk.gov.hmcts.ccd.sdk.api.CaseDetails;
import uk.gov.hmcts.ccd.sdk.api.Event;
import uk.gov.hmcts.ccd.sdk.type.YesOrNo;
import uk.gov.hmcts.sptribs.caseworker.event.page.ApplyAnonymity;
import uk.gov.hmcts.sptribs.ciccase.model.CaseData;
import uk.gov.hmcts.sptribs.ciccase.model.CicCase;
import uk.gov.hmcts.sptribs.ciccase.model.State;
import uk.gov.hmcts.sptribs.ciccase.model.UserRole;
import uk.gov.hmcts.sptribs.notification.dispatcher.AnonymityAppliedNotification;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static uk.gov.hmcts.sptribs.caseworker.util.EventConstants.CASEWORKER_UPDATE_ANONYMITY;
import static uk.gov.hmcts.sptribs.testutil.ConfigTestUtil.createCaseDataConfigBuilder;
import static uk.gov.hmcts.sptribs.testutil.ConfigTestUtil.getEventsFrom;

@ExtendWith(MockitoExtension.class)
class CaseworkerUpdateAnonymityTest {

    @InjectMocks
    private CaseworkerUpdateAnonymity caseworkerUpdateAnonymity;

    @Mock
    private ApplyAnonymity applyAnonymity;

    @Mock
    private AnonymityAppliedNotification anonymityAppliedNotification;

    @Test
    void shouldAddConfigurationToConfigBuilder() {

        final ConfigBuilderImpl<CaseData, State, UserRole> configBuilder = createCaseDataConfigBuilder();
        caseworkerUpdateAnonymity.configure(configBuilder);

        assertThat(getEventsFrom(configBuilder).values())
                .extracting(Event::getId)
                .contains(CASEWORKER_UPDATE_ANONYMITY);
        assertThat(getEventsFrom(configBuilder).values())
                .extracting(Event::getName)
                .contains("Update Anonymity");
    }

    @Test
    void shouldSuccessfullyUpdateAnonymity() {
        CaseDetails<CaseData, State> caseDetails = new CaseDetails<>();
        CaseData caseData = CaseData.builder().build();
        CicCase cicCase = CicCase.builder()
            .anonymiseYesOrNo(YesOrNo.YES)
            .build();
        caseData.setCicCase(cicCase);
        caseDetails.setData(caseData);

        var response = caseworkerUpdateAnonymity.aboutToSubmit(caseDetails,
            CaseDetails.<CaseData, State>builder().build());

        assertThat(response.getData().getCicCase().getAnonymiseYesOrNo()).isEqualTo(YesOrNo.YES);
    }

    @Test
    void shouldSendAnonymityNotificationWhenAnonymityIsNewlyApplied() {
        CaseData caseDataAfter = CaseData.builder()
            .hyphenatedCaseRef("1234-5678-9012-3456")
            .cicCase(CicCase.builder()
                .anonymiseYesOrNo(YesOrNo.YES)
                .anonymisedAppellantName("AAC")
                .build())
            .build();

        CaseData caseDataBefore = CaseData.builder()
            .cicCase(CicCase.builder()
                .anonymiseYesOrNo(YesOrNo.NO)
                .anonymityAlreadyApplied(YesOrNo.NO)
                .anonymisedAppellantName(null)
                .build())
            .build();

        CaseDetails<CaseData, State> details = new CaseDetails<>();
        details.setData(caseDataAfter);
        CaseDetails<CaseData, State> beforeDetails = new CaseDetails<>();
        beforeDetails.setData(caseDataBefore);

        caseworkerUpdateAnonymity.submitted(details, beforeDetails);

        verify(anonymityAppliedNotification, times(1))
            .sendAnonymityNotificationIfNewlyApplied(caseDataAfter, caseDataBefore, null);
    }

    @Test
    void shouldNotSendAnonymityNotificationWhenAlreadyAppliedBefore() {
        CaseData caseDataAfter = CaseData.builder()
            .hyphenatedCaseRef("1234-5678-9012-3456")
            .cicCase(CicCase.builder()
                .anonymiseYesOrNo(YesOrNo.YES)
                .anonymisedAppellantName("AAC")
                .build())
            .build();

        CaseData caseDataBefore = CaseData.builder()
            .cicCase(CicCase.builder()
                .anonymiseYesOrNo(YesOrNo.YES)
                .anonymityAlreadyApplied(YesOrNo.YES)
                .anonymisedAppellantName("AAC")
                .build())
            .build();

        CaseDetails<CaseData, State> details = new CaseDetails<>();
        details.setData(caseDataAfter);
        CaseDetails<CaseData, State> beforeDetails = new CaseDetails<>();
        beforeDetails.setData(caseDataBefore);

        caseworkerUpdateAnonymity.submitted(details, beforeDetails);

        verify(anonymityAppliedNotification, times(1))
            .sendAnonymityNotificationIfNewlyApplied(caseDataAfter, caseDataBefore, null);
    }
}
