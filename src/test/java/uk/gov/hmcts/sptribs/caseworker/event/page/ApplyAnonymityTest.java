package uk.gov.hmcts.sptribs.caseworker.event.page;


import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.ccd.sdk.api.CaseDetails;
import uk.gov.hmcts.ccd.sdk.type.YesOrNo;
import uk.gov.hmcts.sptribs.caseworker.model.DraftOrderContentCIC;
import uk.gov.hmcts.sptribs.caseworker.model.OrderIssuingType;
import uk.gov.hmcts.sptribs.ciccase.model.CaseData;
import uk.gov.hmcts.sptribs.ciccase.model.CicCase;
import uk.gov.hmcts.sptribs.ciccase.model.OrderTemplate;
import uk.gov.hmcts.sptribs.ciccase.model.State;
import uk.gov.hmcts.sptribs.common.service.AnonymisationService;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ApplyAnonymityTest {

    @InjectMocks
    private ApplyAnonymity applyAnonymity;

    @Mock
    private AnonymisationService anonymisationService;

    @Test
    void shouldSuccessfullyApplyAnonymity() {
        final CaseDetails<CaseData, State> caseDetails = new CaseDetails<>();
        caseDetails.setId(123L);
        final CicCase cicCase = CicCase.builder()
            .anonymiseYesOrNo(YesOrNo.YES)
            .anonymisedAppellantName(null)
            .build();
        final CaseData caseData = CaseData.builder()
            .cicCase(cicCase)
            .build();
        caseDetails.setData(caseData);

        when(anonymisationService.getOrCreateAnonymisation()).thenReturn("AC");

        var response = applyAnonymity.midEvent(caseDetails, caseDetails);

        verify(anonymisationService).getOrCreateAnonymisation();
        assertThat(response).isNotNull();
        assertThat(response.getData().getCicCase().getAnonymisedAppellantName()).isEqualTo("AC");
        assertThat(response.getData().getCicCase().getAnonymityAlreadyApplied()).isNull();
    }

    @Test
    void shouldNotReapplyAnonymity() {
        final CaseDetails<CaseData, State> caseDetails = new CaseDetails<>();
        caseDetails.setId(123L);
        final CicCase cicCase = CicCase.builder()
                .anonymiseYesOrNo(YesOrNo.YES)
                .anonymisedAppellantName("AC")
                .build();
        final CaseData caseData = CaseData.builder()
                .cicCase(cicCase)
                .build();
        caseDetails.setData(caseData);

        var response = applyAnonymity.midEvent(caseDetails, caseDetails);
        verifyNoInteractions(anonymisationService);

        assertThat(response).isNotNull();
        assertThat(response.getData().getCicCase().getAnonymisedAppellantName()).isEqualTo("AC");
    }

    @Test
    void shouldNotApplyAnonymityWhenNoSelected() {
        final CaseDetails<CaseData, State> caseDetails = new CaseDetails<>();
        final CicCase cicCase = CicCase.builder()
            .anonymiseYesOrNo(YesOrNo.NO)
            .build();
        final CaseData caseData = CaseData.builder()
            .cicCase(cicCase)
            .build();
        caseDetails.setData(caseData);

        var response = applyAnonymity.midEvent(caseDetails, caseDetails);
        verifyNoInteractions(anonymisationService);

        assertThat(response).isNotNull();
        assertThat(response.getData().getCicCase().getAnonymisedAppellantName()).isNull();
    }

    @Test
    void shouldKeepAnonymisedNameWhenAnonymityRemoved() {
        final CaseDetails<CaseData, State> caseDetails = new CaseDetails<>();
        final CicCase cicCase = CicCase.builder()
            .anonymiseYesOrNo(YesOrNo.NO)
            .anonymisedAppellantName("AC")
            .anonymityAlreadyApplied(YesOrNo.YES)
            .anonymisationDate(LocalDate.now())
            .build();
        final CaseData caseData = CaseData.builder()
            .cicCase(cicCase)
            .build();
        caseDetails.setData(caseData);

        var response = applyAnonymity.midEvent(caseDetails, caseDetails);

        verifyNoInteractions(anonymisationService);
        assertThat(response).isNotNull();
        assertThat(response.getData().getCicCase().getAnonymisedAppellantName()).isEqualTo("AC");
        assertThat(response.getData().getCicCase().getAnonymisationDate()).isNull();
        assertThat(response.getData().getCicCase().getAnonymityAlreadyApplied()).isEqualTo(YesOrNo.NO);
    }

    @Test
    void shouldRestrictOrderIssuingAndTemplateOptionsForFirstTimeAnonymisationJourney() {
        final CaseDetails<CaseData, State> caseDetails = new CaseDetails<>();
        final CicCase cicCase = CicCase.builder()
            .anonymiseYesOrNo(YesOrNo.YES)
            .anonymityAlreadyApplied(YesOrNo.NO)
            .build();
        final CaseData caseData = CaseData.builder()
            .cicCase(cicCase)
            .draftOrderContentCIC(new DraftOrderContentCIC())
            .build();
        caseDetails.setData(caseData);

        when(anonymisationService.getOrCreateAnonymisation()).thenReturn("AC");

        var response = applyAnonymity.midEvent(caseDetails, caseDetails);

        assertThat(response.getData().getCicCase().getOrderIssuingDynamicRadioList().getListItems())
            .extracting(item -> item.getLabel())
            .containsExactly(OrderIssuingType.CREATE_AND_SEND_NEW_ORDER.getLabel());
        assertThat(response.getData().getCicCase().getTemplateDynamicList().getListItems())
            .extracting(item -> item.getLabel())
            .containsExactly(OrderTemplate.CIC6_GENERAL_DIRECTIONS.getLabel());
        assertThat(response.getData().getCicCase().getAnonymityAlreadyApplied()).isEqualTo(YesOrNo.NO);
    }
}
