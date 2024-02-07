package uk.gov.hmcts.sptribs.document.content;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.ccd.sdk.type.ListValue;
import uk.gov.hmcts.sptribs.caseworker.model.HearingSummary;
import uk.gov.hmcts.sptribs.caseworker.model.Listing;
import uk.gov.hmcts.sptribs.ciccase.model.CaseData;
import uk.gov.hmcts.sptribs.ciccase.model.CicCase;
import uk.gov.hmcts.sptribs.ciccase.model.HearingState;
import uk.gov.hmcts.sptribs.ciccase.model.SchemeCic;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.entry;
import static uk.gov.hmcts.sptribs.document.content.DocmosisTemplateConstants.DECISION_SIGNATURE;
import static uk.gov.hmcts.sptribs.document.content.DocmosisTemplateConstants.MAIN_CONTENT;
import static uk.gov.hmcts.sptribs.document.content.DocmosisTemplateConstants.TRIBUNAL_MEMBERS;
import static uk.gov.hmcts.sptribs.testutil.TestConstants.TEST_CASE_ID;
import static uk.gov.hmcts.sptribs.testutil.TestDataHelper.getMembers;

@ExtendWith(MockitoExtension.class)
public class DecisionTemplateContentTest {

    @InjectMocks
    private DecisionTemplateContent templateContent;

    @Test
    public void shouldSuccessfullyApplyDecisionContent() {
        final CaseData caseData = buildCaseData();
        caseData.setDecisionSignature("John Doe");
        caseData.setDecisionMainContent("Case Closed");
        final HearingSummary summary = HearingSummary.builder()
            .memberList(getMembers())
            .build();
        final Listing listing = Listing.builder()
            .date(LocalDate.now())
            .hearingTime("11::00")
            .hearingStatus(HearingState.Complete)
            .summary(summary)
            .build();
        final ListValue<Listing> listingListValue = new ListValue<>();
        listingListValue.setValue(listing);
        caseData.setHearingList(List.of(listingListValue));

        final Map<String, Object> result = templateContent.apply(caseData, TEST_CASE_ID);

        assertThat(result)
            .contains(entry("cicCaseSchemeCic", SchemeCic.Year1996.getLabel()))
            .contains(entry(DECISION_SIGNATURE, "John Doe"))
            .contains(entry(MAIN_CONTENT, "Case Closed"));
    }

    @Test
    public void shouldSuccessfullyApplyDecisionContentNoMembers() {
        final CaseData caseData = buildCaseData();
        final HearingSummary summary = HearingSummary.builder()
            .build();
        final Listing listing = Listing.builder()
            .summary(summary)
            .date(LocalDate.now())
            .hearingTime("11::00")
            .build();
        final ListValue<Listing> listingListValue = new ListValue<>();
        listingListValue.setValue(listing);
        caseData.setHearingList(List.of(listingListValue));
        final Map<String, Object> result = templateContent.apply(caseData, TEST_CASE_ID);

        assertThat(result)
            .contains(entry("cicCaseSchemeCic", SchemeCic.Year1996.getLabel()))
            .contains(entry(DECISION_SIGNATURE, null))
            .contains(entry(TRIBUNAL_MEMBERS, ""));
    }

    private CaseData buildCaseData() {
        final CicCase cicCase = CicCase.builder().schemeCic(SchemeCic.Year1996).build();

        return CaseData.builder()
            .cicCase(cicCase)
            .build();
    }
}
