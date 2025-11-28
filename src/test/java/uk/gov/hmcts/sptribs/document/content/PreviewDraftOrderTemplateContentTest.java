package uk.gov.hmcts.sptribs.document.content;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.ccd.sdk.type.ListValue;
import uk.gov.hmcts.ccd.sdk.type.YesOrNo;
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
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.sptribs.document.content.DocmosisTemplateConstants.HEARING_DATE;
import static uk.gov.hmcts.sptribs.document.content.DocmosisTemplateConstants.ORDER_SIGNATURE;
import static uk.gov.hmcts.sptribs.document.content.DocmosisTemplateConstants.SUBJECT_FULL_NAME;
import static uk.gov.hmcts.sptribs.document.content.DocmosisTemplateConstants.formatter;
import static uk.gov.hmcts.sptribs.testutil.TestConstants.TEST_CASE_ID;
import static uk.gov.hmcts.sptribs.testutil.TestDataHelper.getMembers;


@ExtendWith(MockitoExtension.class)
class PreviewDraftOrderTemplateContentTest {

    @InjectMocks
    private PreviewDraftOrderTemplateContent previewDraftOrderTemplateContent;

    @Test
    void shouldSuccessfullyPreviewDraftOrderContent() {
        CaseData caseData = buildCaseData();
        final HearingSummary summary = HearingSummary.builder()
            .memberList(getMembers())
            .subjectName("John Smith")
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

        Map<String, Object> result = previewDraftOrderTemplateContent.apply(caseData, TEST_CASE_ID);

        assertThat(result)
            .contains(entry("cicCaseSchemeCic", SchemeCic.Year1996.getLabel()))
            .contains(entry(HEARING_DATE, LocalDate.now().format(formatter)))
            .contains(entry(SUBJECT_FULL_NAME, "John Smith"));
    }

    @Test
    void shouldSuccessfullyApplyPreviewDraftOrderContentNoMembers() {
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
        final Map<String, Object> result = previewDraftOrderTemplateContent.apply(caseData, TEST_CASE_ID);

        assertThat(result)
            .contains(entry("cicCaseSchemeCic", SchemeCic.Year1996.getLabel()))
            .contains(entry(ORDER_SIGNATURE, null));
    }

    @Test
    void shouldSuccessfullyApplyPreviewDraftOrderContentWithEmptyDate() {
        final CaseData caseData = buildCaseData();
        final HearingSummary summary = HearingSummary.builder()
            .subjectName("John Smith")
            .build();
        final Listing listing = Listing.builder()
            .summary(summary)
            .date(null)
            .hearingTime("15:30")
            .build();

        //Using a spy as getLatestCompletedHearing cannot handle a null date
        final CaseData caseDataMock = spy(caseData);
        when(caseDataMock.getLatestCompletedHearing()).thenReturn(listing);

        final Map<String, Object> result = previewDraftOrderTemplateContent.apply(caseDataMock, TEST_CASE_ID);

        assertThat(result)
            .contains(entry("cicCaseSchemeCic", SchemeCic.Year1996.getLabel()))
            .contains(entry(HEARING_DATE, ""))
            .contains(entry(SUBJECT_FULL_NAME, "John Smith"));
    }

    @Test
    void shouldSuccessfullyPreviewDraftOrderContentWithFatalSubcategory() {
        CaseData caseData = buildCaseDataWithSubcategory(CaseSubcategory.FATAL, true);
        final HearingSummary summary = HearingSummary.builder()
            .memberList(getMembers())
            .subjectName("John Smith")
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

        Map<String, Object> result = previewDraftOrderTemplateContent.apply(caseData, TEST_CASE_ID);

        assertThat(result)
            .contains(entry("cicCaseSchemeCic", SchemeCic.Year1996.getLabel()))
            .contains(entry(SUBJECT_FULL_NAME, "Jane Doe"))
            .contains(entry(HEARING_DATE, LocalDate.now().format(formatter)));
    }

    @Test
    void shouldSuccessfullyPreviewDraftOrderContentWithMinorSubcategory() {
        CaseData caseData = buildCaseDataWithSubcategory(CaseSubcategory.MINOR, true);
        final HearingSummary summary = HearingSummary.builder()
            .memberList(getMembers())
            .subjectName("John Smith")
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

        Map<String, Object> result = previewDraftOrderTemplateContent.apply(caseData, TEST_CASE_ID);

        assertThat(result)
            .contains(entry("cicCaseSchemeCic", SchemeCic.Year1996.getLabel()))
            .contains(entry(SUBJECT_FULL_NAME, "Jane Doe"))
            .contains(entry(HEARING_DATE, LocalDate.now().format(formatter)));
    }

    @Test
    void shouldSuccessfullyPreviewDraftOrderContentWithFatalSubcategoryNoApplicant() {
        CaseData caseData = buildCaseDataWithSubcategory(CaseSubcategory.FATAL, false);
        final HearingSummary summary = HearingSummary.builder()
            .memberList(getMembers())
            .subjectName("John Smith")
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

        Map<String, Object> result = previewDraftOrderTemplateContent.apply(caseData, TEST_CASE_ID);

        assertThat(result)
            .contains(entry("cicCaseSchemeCic", SchemeCic.Year1996.getLabel()))
            .contains(entry(SUBJECT_FULL_NAME, "John Smith"))
            .contains(entry(HEARING_DATE, LocalDate.now().format(formatter)));
    }

    @Test
    void shouldSuccessfullyPreviewDraftOrderContentWithMinorSubcategoryNoApplicant() {
        CaseData caseData = buildCaseDataWithSubcategory(CaseSubcategory.MINOR, false);
        final HearingSummary summary = HearingSummary.builder()
            .memberList(getMembers())
            .subjectName("John Smith")
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

        Map<String, Object> result = previewDraftOrderTemplateContent.apply(caseData, TEST_CASE_ID);

        assertThat(result)
            .contains(entry("cicCaseSchemeCic", SchemeCic.Year1996.getLabel()))
            .contains(entry(SUBJECT_FULL_NAME, "John Smith"))
            .contains(entry(HEARING_DATE, LocalDate.now().format(formatter)));
    }

    @Test
    void shouldSuccessfullyPreviewAnonymisedDraftOrderContent() {
        CaseData caseData = buildCaseData();
        caseData.getCicCase().setAnonymiseYesOrNo(YesOrNo.YES);
        caseData.getCicCase().setAnonymisedAppellantName("Anonymised Name");
        final HearingSummary summary = HearingSummary.builder()
            .memberList(getMembers())
            .subjectName("John Smith")
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

        Map<String, Object> result = previewDraftOrderTemplateContent.apply(caseData, TEST_CASE_ID);

        assertThat(result)
            .contains(entry("cicCaseSchemeCic", SchemeCic.Year1996.getLabel()))
            .contains(entry(HEARING_DATE, LocalDate.now().format(formatter)))
            .contains(entry(SUBJECT_FULL_NAME, "Anonymised Name"));
    }

    @Test
    void shouldNotUseAnonymisedNameDraftOrderContent() {
        CaseData caseData = buildCaseData();
        caseData.getCicCase().setAnonymiseYesOrNo(YesOrNo.NO);
        caseData.getCicCase().setAnonymisedAppellantName("Anonymised Name");
        final HearingSummary summary = HearingSummary.builder()
            .memberList(getMembers())
            .subjectName("John Smith")
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

        Map<String, Object> result = previewDraftOrderTemplateContent.apply(caseData, TEST_CASE_ID);

        assertThat(result)
            .contains(entry("cicCaseSchemeCic", SchemeCic.Year1996.getLabel()))
            .contains(entry(HEARING_DATE, LocalDate.now().format(formatter)))
            .contains(entry(SUBJECT_FULL_NAME, "John Smith"));
    }

    private CaseData buildCaseData() {
        final CicCase cicCase = CicCase.builder()
            .fullName("John Smith")
            .schemeCic(SchemeCic.Year1996)
            .anonymiseYesOrNo(YesOrNo.NO)
            .build();

        return CaseData.builder()
            .cicCase(cicCase)
            .build();
    }

    private CaseData buildCaseDataWithSubcategory(CaseSubcategory caseSubcategory, boolean applicant) {
        if (applicant) {
            final CicCase cicCase = CicCase.builder()
                .fullName("John Smith")
                .applicantFullName("Jane Doe")
                .caseSubcategory(caseSubcategory)
                .schemeCic(SchemeCic.Year1996)
                .anonymiseYesOrNo(YesOrNo.NO)
                .build();

            return CaseData.builder()
                .cicCase(cicCase)
                .build();
        } else {
            final CicCase cicCase = CicCase.builder()
                .fullName("John Smith")
                .caseSubcategory(caseSubcategory)
                .schemeCic(SchemeCic.Year1996)
                .anonymiseYesOrNo(YesOrNo.NO)
                .build();

            return CaseData.builder()
                .cicCase(cicCase)
                .build();
        }
    }

}
