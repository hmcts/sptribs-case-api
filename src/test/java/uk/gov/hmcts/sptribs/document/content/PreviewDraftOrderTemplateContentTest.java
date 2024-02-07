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
import static uk.gov.hmcts.sptribs.document.content.DocmosisTemplateConstants.HEARING_DATE;
import static uk.gov.hmcts.sptribs.document.content.DocmosisTemplateConstants.SUBJECT_FULL_NAME;
import static uk.gov.hmcts.sptribs.document.content.DocmosisTemplateConstants.formatter;
import static uk.gov.hmcts.sptribs.testutil.TestConstants.TEST_CASE_ID;
import static uk.gov.hmcts.sptribs.testutil.TestDataHelper.getMembers;


@ExtendWith(MockitoExtension.class)
public class PreviewDraftOrderTemplateContentTest {

    @InjectMocks
    private PreviewDraftOrderTemplateContent previewDraftOrderTemplateContent;

    @Test
    public void shouldSuccessfullyPreviewDraftOrderContent() {
        CaseData caseData = buildCaseData();
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

        Map<String, Object> result = previewDraftOrderTemplateContent.apply(caseData, TEST_CASE_ID);

        assertThat(result)
            .contains(entry("cicCaseSchemeCic", SchemeCic.Year1996.getLabel()))
            .contains(entry(SUBJECT_FULL_NAME, "John Smith"))
            .contains(entry(HEARING_DATE, LocalDate.now().format(formatter)));
    }

    private CaseData buildCaseData() {
        final CicCase cicCase = CicCase.builder().fullName("John Smith").schemeCic(SchemeCic.Year1996).build();

        return CaseData.builder()
            .cicCase(cicCase)
            .build();
    }

}
