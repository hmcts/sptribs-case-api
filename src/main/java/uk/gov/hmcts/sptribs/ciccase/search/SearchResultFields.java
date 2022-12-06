package uk.gov.hmcts.sptribs.ciccase.search;

import org.springframework.stereotype.Component;
import uk.gov.hmcts.ccd.sdk.api.CCDConfig;
import uk.gov.hmcts.ccd.sdk.api.ConfigBuilder;
import uk.gov.hmcts.ccd.sdk.api.SearchField;
import uk.gov.hmcts.sptribs.ciccase.model.CaseData;
import uk.gov.hmcts.sptribs.ciccase.model.State;
import uk.gov.hmcts.sptribs.ciccase.model.UserRoleCIC;

import java.util.List;

import static java.util.List.of;
import static uk.gov.hmcts.ccd.sdk.api.SortOrder.FIRST;
import static uk.gov.hmcts.sptribs.ciccase.search.CaseFieldsConstants.APPLICANT_NAME;
import static uk.gov.hmcts.sptribs.ciccase.search.CaseFieldsConstants.CASE_STATE;
import static uk.gov.hmcts.sptribs.ciccase.search.CaseFieldsConstants.CCD_REFERENCE;
import static uk.gov.hmcts.sptribs.ciccase.search.CaseFieldsConstants.DUE_DATE;
import static uk.gov.hmcts.sptribs.ciccase.search.CaseFieldsConstants.HEARING_DATE;
import static uk.gov.hmcts.sptribs.ciccase.search.CaseFieldsConstants.LAST_MODIFIED_DATE;
import static uk.gov.hmcts.sptribs.ciccase.search.CaseFieldsConstants.LAST_STATE_MODIFIED_DATE;
import static uk.gov.hmcts.sptribs.ciccase.search.CaseFieldsConstants.SUBJECT_NAME;

@Component
public class SearchResultFields implements CCDConfig<CaseData, State, UserRoleCIC> {

    public static final List<SearchField<UserRoleCIC>> SEARCH_RESULT_FIELD_LIST = of(
        SearchField.<UserRoleCIC>builder().id(CCD_REFERENCE).label("Case Number").build(),
        SearchField.<UserRoleCIC>builder().id(SUBJECT_NAME).label("Subject Name").build(),
        SearchField.<UserRoleCIC>builder().id(CASE_STATE).label("Case Status").build(),
        SearchField.<UserRoleCIC>builder().id(HEARING_DATE).label("Hearing Date").build(),
        SearchField.<UserRoleCIC>builder().id(APPLICANT_NAME).label("Applicant Name").build(),
        SearchField.<UserRoleCIC>builder().id(DUE_DATE).label("Due Date").build(),
        SearchField.<UserRoleCIC>builder().id(LAST_MODIFIED_DATE).label("Last modified date").build(),
        SearchField.<UserRoleCIC>builder().id(LAST_STATE_MODIFIED_DATE).label("Last state modified date").order(FIRST.ASCENDING).build()
    );

    @Override
    public void configure(final ConfigBuilder<CaseData, State, UserRoleCIC> configBuilder) {
        configBuilder
            .searchResultFields()
            .fields(SEARCH_RESULT_FIELD_LIST);
    }
}
