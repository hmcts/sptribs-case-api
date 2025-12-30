package uk.gov.hmcts.sptribs.common.event.page;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.ccd.sdk.api.CaseDetails;
import uk.gov.hmcts.ccd.sdk.api.callback.AboutToStartOrSubmitResponse;
import uk.gov.hmcts.ccd.sdk.type.YesOrNo;
import uk.gov.hmcts.sptribs.caseworker.model.DraftOrderContentCIC;
import uk.gov.hmcts.sptribs.ciccase.model.CaseData;
import uk.gov.hmcts.sptribs.ciccase.model.CicCase;
import uk.gov.hmcts.sptribs.ciccase.model.OrderTemplate;
import uk.gov.hmcts.sptribs.ciccase.model.State;
import uk.gov.hmcts.sptribs.document.content.DocmosisTemplateConstants;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;


@ExtendWith(MockitoExtension.class)
class DraftOrderMainContentPageTest {

    @InjectMocks
    private DraftOrderMainContentPage draftOrderMainContentPage;

    @Test
    void shouldAddAnonymityParagraphWhenAnonymityHasBeenApplied() {
        CaseDetails<CaseData, State> caseDetails = new CaseDetails<>();
        CaseDetails<CaseData, State> caseDetailsBefore = new CaseDetails<>();

        DraftOrderContentCIC contentCIC = DraftOrderContentCIC.builder()
            .orderTemplate(OrderTemplate.CIC7_ME_DMI_REPORTS)
            .mainContent("test content")
            .build();

        LocalDate date = LocalDate.of(2025, 12, 12);
        CicCase cicCase = CicCase.builder()
            .anonymiseYesOrNo(YesOrNo.YES)
            .anonymisationDate(date)
            .anonymisedAppellantName("Anonymised Name")
            .build();

        CaseData caseData = CaseData.builder()
            .draftOrderContentCIC(contentCIC)
            .cicCase(cicCase)
            .build();
        caseDetails.setData(caseData);

        String expectedStatement = DocmosisTemplateConstants.generateAnonymisationStatement(date);

        AboutToStartOrSubmitResponse<CaseData, State> response = draftOrderMainContentPage.midEvent(caseDetails, caseDetailsBefore);

        assertThat(response).isNotNull();
        assertThat(response.getData().getDraftOrderContentCIC().getMainContent())
            .contains("test content")
            .contains(expectedStatement);
    }

    @Test
    void shouldNotAddAnonymityParagraphWhenNoAnonymityHasBeenApplied() {
        CaseDetails<CaseData, State> caseDetails = new CaseDetails<>();
        CaseDetails<CaseData, State> caseDetailsBefore = new CaseDetails<>();

        DraftOrderContentCIC contentCIC = DraftOrderContentCIC.builder()
            .orderTemplate(OrderTemplate.CIC7_ME_DMI_REPORTS)
            .mainContent("test content")
            .build();

        LocalDate date = LocalDate.of(2025, 12, 12);
        CicCase cicCase = CicCase.builder()
                .anonymiseYesOrNo(YesOrNo.NO)
                .build();

        CaseData caseData = CaseData.builder()
                .draftOrderContentCIC(contentCIC)
                .cicCase(cicCase)
                .build();
        caseDetails.setData(caseData);

        String expectedStatement = DocmosisTemplateConstants.generateAnonymisationStatement(date);

        AboutToStartOrSubmitResponse<CaseData, State> response = draftOrderMainContentPage.midEvent(caseDetails, caseDetailsBefore);

        assertThat(response).isNotNull();
        assertThat(response.getData().getDraftOrderContentCIC().getMainContent())
            .contains("test content")
            .doesNotContain(expectedStatement);
    }
}