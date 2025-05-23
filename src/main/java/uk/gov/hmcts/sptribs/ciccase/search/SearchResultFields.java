package uk.gov.hmcts.sptribs.ciccase.search;

import org.springframework.stereotype.Component;
import uk.gov.hmcts.ccd.sdk.api.CCDConfig;
import uk.gov.hmcts.ccd.sdk.api.ConfigBuilder;
import uk.gov.hmcts.ccd.sdk.api.SearchField;
import uk.gov.hmcts.ccd.sdk.api.SortOrder;
import uk.gov.hmcts.sptribs.ciccase.model.CaseData;
import uk.gov.hmcts.sptribs.ciccase.model.State;
import uk.gov.hmcts.sptribs.ciccase.model.UserRole;

import java.util.List;

import static java.util.List.of;
import static uk.gov.hmcts.ccd.sdk.api.SortOrder.FIRST;
import static uk.gov.hmcts.sptribs.ciccase.search.CaseFieldsConstants.APPLICANT_NAME;
import static uk.gov.hmcts.sptribs.ciccase.search.CaseFieldsConstants.CASE_REGION;
import static uk.gov.hmcts.sptribs.ciccase.search.CaseFieldsConstants.CASE_STATE;
import static uk.gov.hmcts.sptribs.ciccase.search.CaseFieldsConstants.CCD_REFERENCE;
import static uk.gov.hmcts.sptribs.ciccase.search.CaseFieldsConstants.DUE_DATE;
import static uk.gov.hmcts.sptribs.ciccase.search.CaseFieldsConstants.HEARING_DATE;
import static uk.gov.hmcts.sptribs.ciccase.search.CaseFieldsConstants.LAST_MODIFIED_DATE;
import static uk.gov.hmcts.sptribs.ciccase.search.CaseFieldsConstants.LAST_STATE_MODIFIED_DATE;
import static uk.gov.hmcts.sptribs.ciccase.search.CaseFieldsConstants.SUBJECT_NAME;

@Component
public class SearchResultFields implements CCDConfig<CaseData, State, UserRole> {

    public static final List<SearchField<UserRole>> SEARCH_RESULT_FIELD_LIST = of(
        SearchField.<UserRole>builder().id(CCD_REFERENCE).label("Case Number").build(),
        SearchField.<UserRole>builder().id(SUBJECT_NAME).label("Subject Name").build(),
        SearchField.<UserRole>builder().id(CASE_STATE).label("Case Status").build(),
        SearchField.<UserRole>builder().id(CASE_REGION).label("Case Region").build(),
        SearchField.<UserRole>builder().id(HEARING_DATE).label("Hearing Date").build(),
        SearchField.<UserRole>builder().id(APPLICANT_NAME).label("Applicant Name").build(),
        SearchField.<UserRole>builder().id(DUE_DATE).label("Due Date").order(FIRST.ASCENDING).build(),
        SearchField.<UserRole>builder().id(LAST_MODIFIED_DATE).label("Last modified date").build(),
        SearchField.<UserRole>builder().id(LAST_STATE_MODIFIED_DATE).label("Last state modified date").order(FIRST.ASCENDING).build()
    );

    @Override
    public void configure(final ConfigBuilder<CaseData, State, UserRole> configBuilder) {
        configBuilder
            .searchResultFields()
            .fields(SEARCH_RESULT_FIELD_LIST);
    }
}
