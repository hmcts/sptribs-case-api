package uk.gov.hmcts.sptribs.caseworker.event;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.ccd.sdk.ConfigBuilderImpl;
import uk.gov.hmcts.ccd.sdk.api.CaseDetails;
import uk.gov.hmcts.ccd.sdk.api.Event;
import uk.gov.hmcts.ccd.sdk.type.FlagDetail;
import uk.gov.hmcts.ccd.sdk.type.Flags;
import uk.gov.hmcts.ccd.sdk.type.ListValue;
import uk.gov.hmcts.ccd.sdk.type.YesOrNo;
import uk.gov.hmcts.sptribs.caseworker.event.page.ApplyAnonymity;
import uk.gov.hmcts.sptribs.caseworker.util.CaseFlagsUtil;
import uk.gov.hmcts.sptribs.ciccase.model.CaseData;
import uk.gov.hmcts.sptribs.ciccase.model.CicCase;
import uk.gov.hmcts.sptribs.ciccase.model.State;
import uk.gov.hmcts.sptribs.ciccase.model.UserRole;
import uk.gov.hmcts.sptribs.common.repositories.AnonymisationRepository;
import uk.gov.hmcts.sptribs.common.service.AnonymisationService;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.doReturn;
import static uk.gov.hmcts.sptribs.caseworker.util.EventConstants.CASEWORKER_UPDATE_ANONYMITY;
import static uk.gov.hmcts.sptribs.testutil.ConfigTestUtil.createCaseDataConfigBuilder;
import static uk.gov.hmcts.sptribs.testutil.ConfigTestUtil.getEventsFrom;

@ExtendWith(MockitoExtension.class)
class CaseworkerUpdateAnonymityTest {

    private CaseworkerUpdateAnonymity caseworkerUpdateAnonymity;

    @Mock
    private ApplyAnonymity applyAnonymity;

    @Mock
    private AnonymisationRepository anonymisationRepository;

    private AnonymisationService anonymisationService;

    @BeforeEach
    void setUp() {
        anonymisationService = Mockito.spy(new AnonymisationService(anonymisationRepository));
        caseworkerUpdateAnonymity = new CaseworkerUpdateAnonymity(applyAnonymity, anonymisationService);
    }

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
    void shouldSuccessfullyUpdateAnonymityWithYesAndNoExistingFlag() {
        CaseDetails<CaseData, State> caseDetails = new CaseDetails<>();
        CaseData caseData = CaseData.builder().build();
        CicCase cicCase = CicCase.builder()
            .anonymiseYesOrNo(YesOrNo.YES)
            .partiesCIC(Collections.emptySet())
            .build();
        caseData.setCicCase(cicCase);
        caseDetails.setData(caseData);

        doReturn(1L).when(anonymisationRepository).getNextSequenceValue();

        var response = caseworkerUpdateAnonymity.aboutToSubmit(caseDetails,
            CaseDetails.<CaseData, State>builder().build());

        assertThat(response.getData().getCicCase().getAnonymiseYesOrNo()).isEqualTo(YesOrNo.YES);
        assertThat(response.getData().getCicCase().getAnonymityAlreadyApplied()).isEqualTo(YesOrNo.YES);
        assertThat(response.getData().getCicCase().getAnonymisedAppellantName()).isEqualTo("AA");
        assertThat(response.getData().getCicCase().getAnonymisationDate()).isNotNull();

        assertThat(response.getData().getCaseFlags()).isNotNull();
        assertThat(response.getData().getCaseFlags().getDetails()).hasSize(1);
        assertThat(response.getData().getCaseFlags().getDetails().get(0).getValue().getStatus())
            .isEqualTo("Active");
        assertThat(response.getData().getCaseFlags().getDetails().get(0).getValue().getFlagCode())
            .isEqualTo(CaseFlagsUtil.ANONYMITY_FLAG_CODE);
    }

    @Test
    void shouldSuccessfullyUpdateAnonymityWithYesAndExistingFlag() {
        CaseDetails<CaseData, State> caseDetails = new CaseDetails<>();
        CaseData caseData = CaseData.builder().build();
        CicCase cicCase = CicCase.builder()
            .anonymiseYesOrNo(YesOrNo.YES)
            .partiesCIC(Collections.emptySet())
            .build();
        caseData.setCicCase(cicCase);

        ListValue<FlagDetail> existingFlag = ListValue.<FlagDetail>builder()
            .id("existing-id")
            .value(FlagDetail.builder()
                .flagCode(CaseFlagsUtil.ANONYMITY_FLAG_CODE)
                .status("Inactive")
                .dateTimeCreated(LocalDateTime.now().minusDays(1))
                .build())
            .build();
        caseData.setCaseFlags(Flags.builder().details(new ArrayList<>(List.of(existingFlag))).build());
        caseDetails.setData(caseData);

        doReturn(1L).when(anonymisationRepository).getNextSequenceValue();

        var response = caseworkerUpdateAnonymity.aboutToSubmit(caseDetails,
            CaseDetails.<CaseData, State>builder().data(caseData).build());

        assertThat(response.getData().getCicCase().getAnonymiseYesOrNo()).isEqualTo(YesOrNo.YES);
        assertThat(response.getData().getCaseFlags().getDetails()).hasSize(1);
        assertThat(response.getData().getCaseFlags().getDetails().get(0).getId()).isEqualTo("existing-id");
        assertThat(response.getData().getCaseFlags().getDetails().get(0).getValue().getStatus()).isEqualTo("Active");
    }

    @Test
    void shouldSuccessfullyUpdateAnonymityWithNoAndExistingFlag() {
        CaseDetails<CaseData, State> caseDetails = new CaseDetails<>();
        CaseData caseData = CaseData.builder().build();
        CicCase cicCase = CicCase.builder()
            .anonymiseYesOrNo(YesOrNo.NO)
            .partiesCIC(Collections.emptySet())
            .build();
        caseData.setCicCase(cicCase);

        ListValue<FlagDetail> existingFlag = ListValue.<FlagDetail>builder()
            .id("existing-id")
            .value(FlagDetail.builder()
                .flagCode(CaseFlagsUtil.ANONYMITY_FLAG_CODE)
                .status("Active")
                .dateTimeCreated(LocalDateTime.now().minusDays(1))
                .build())
            .build();
        caseData.setCaseFlags(Flags.builder().details(new ArrayList<>(List.of(existingFlag))).build());
        caseDetails.setData(caseData);

        var response = caseworkerUpdateAnonymity.aboutToSubmit(caseDetails,
            CaseDetails.<CaseData, State>builder().data(caseData).build());

        assertThat(response.getData().getCicCase().getAnonymiseYesOrNo()).isEqualTo(YesOrNo.NO);
        assertThat(response.getData().getCicCase().getAnonymityAlreadyApplied()).isEqualTo(YesOrNo.NO);
        assertThat(response.getData().getCicCase().getAnonymisationDate()).isNull();

        assertThat(response.getData().getCaseFlags().getDetails()).hasSize(1);
        assertThat(response.getData().getCaseFlags().getDetails().get(0).getId()).isEqualTo("existing-id");
        assertThat(response.getData().getCaseFlags().getDetails().get(0).getValue().getStatus()).isEqualTo("Inactive");
    }
}
