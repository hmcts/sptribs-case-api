package uk.gov.hmcts.sptribs.document.content;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.ccd.sdk.type.ListValue;
import uk.gov.hmcts.sptribs.caseworker.model.HearingSummary;
import uk.gov.hmcts.sptribs.caseworker.model.Listing;
import uk.gov.hmcts.sptribs.ciccase.model.CaseData;
import uk.gov.hmcts.sptribs.ciccase.model.CaseSubcategory;
import uk.gov.hmcts.sptribs.ciccase.model.CicCase;
import uk.gov.hmcts.sptribs.ciccase.model.HearingState;
import uk.gov.hmcts.sptribs.ciccase.model.SchemeCic;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.entry;
import static uk.gov.hmcts.sptribs.document.content.DocmosisTemplateConstants.SUBJECT_FULL_NAME;
import static uk.gov.hmcts.sptribs.testutil.TestConstants.TEST_CASE_ID;
import static uk.gov.hmcts.sptribs.testutil.TestDataHelper.getMembers;


@ExtendWith(MockitoExtension.class)
public class FinalDecisionTemplateContentTest {


    @InjectMocks
    private FinalDecisionTemplateContent templateContent;

    private static final LocalDate ISSUE_DATE = LocalDate.of(2022, 2, 2);

    @Test
    public void shouldSuccessfullyApplyFinalDecisionContent() {
        //Given
        CaseData caseData = buildCaseData();
        HearingSummary summary = HearingSummary.builder()
            .memberList(getMembers())
            .build();

        Listing listing = Listing.builder().date(LocalDate.now()).hearingTime("11::00")
            .hearingStatus(HearingState.Complete)
            .summary(summary)
            .build();
        ListValue<Listing> listingListValue = new ListValue<>();
        listingListValue.setValue(listing);
        caseData.setHearingList(List.of(listingListValue));
        //When
        Map<String, Object> result = templateContent.apply(caseData, TEST_CASE_ID);

        //Then
        assertThat(result).contains(
            entry("cicCaseSchemeCic", SchemeCic.Year1996.getLabel())
        );
    }

    @Test
    public void shouldSuccessfullyApplyFinalDecisionContentNoMembers() {
        //Given
        CaseData caseData = buildCaseData();
        HearingSummary summary = HearingSummary.builder()
            .build();
        Listing listing = Listing.builder().date(LocalDate.now())
            .summary(summary)
            .hearingStatus(HearingState.Complete)
            .hearingTime("11::00").build();
        ListValue<Listing> listingListValue = new ListValue<>();
        listingListValue.setValue(listing);
        caseData.setHearingList(List.of(listingListValue));
        //When
        Map<String, Object> result = templateContent.apply(caseData, TEST_CASE_ID);

        //Then
        assertThat(result).contains(
            entry("cicCaseSchemeCic", SchemeCic.Year1996.getLabel())
        );
    }

    @Test
    public void shouldSuccessfullyApplyFinalDecisionContentWithFatalSubcategory() {
        //Given
        CaseData caseData = buildCaseDataWithSubcategory(CaseSubcategory.FATAL);
        HearingSummary summary = HearingSummary.builder()
            .memberList(getMembers())
            .build();

        Listing listing = Listing.builder().date(LocalDate.now()).hearingTime("11::00")
            .hearingStatus(HearingState.Complete)
            .summary(summary)
            .build();
        ListValue<Listing> listingListValue = new ListValue<>();
        listingListValue.setValue(listing);
        caseData.setHearingList(List.of(listingListValue));
        //When
        Map<String, Object> result = templateContent.apply(caseData, TEST_CASE_ID);

        //Then
        assertThat(result)
            .contains(entry("cicCaseSchemeCic", SchemeCic.Year1996.getLabel()))
            .contains(entry(SUBJECT_FULL_NAME, "Jane Doe"));;
    }

    @Test
    public void shouldSuccessfullyApplyFinalDecisionContentWithMinorSubcategory() {
        //Given
        CaseData caseData = buildCaseDataWithSubcategory(CaseSubcategory.MINOR);
        HearingSummary summary = HearingSummary.builder()
            .memberList(getMembers())
            .build();

        Listing listing = Listing.builder().date(LocalDate.now()).hearingTime("11::00")
            .hearingStatus(HearingState.Complete)
            .summary(summary)
            .build();
        ListValue<Listing> listingListValue = new ListValue<>();
        listingListValue.setValue(listing);
        caseData.setHearingList(List.of(listingListValue));
        //When
        Map<String, Object> result = templateContent.apply(caseData, TEST_CASE_ID);

        //Then
        assertThat(result)
            .contains(entry("cicCaseSchemeCic", SchemeCic.Year1996.getLabel()))
            .contains(entry(SUBJECT_FULL_NAME, "Jane Doe"));;
    }

    private CaseData buildCaseData() {
        final CicCase cicCase = CicCase.builder().schemeCic(SchemeCic.Year1996).build();

        return CaseData.builder()
            .cicCase(cicCase)
            .build();
    }

    private CaseData buildCaseDataWithSubcategory(CaseSubcategory caseSubcategory) {
        final CicCase cicCase = CicCase.builder()
            .fullName("John Smith")
            .applicantFullName("Jane Doe")
            .caseSubcategory(caseSubcategory)
            .schemeCic(SchemeCic.Year1996).build();

        return CaseData.builder()
            .cicCase(cicCase)
            .build();
    }
}
