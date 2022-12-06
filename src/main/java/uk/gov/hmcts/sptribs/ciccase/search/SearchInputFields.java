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
import static uk.gov.hmcts.sptribs.ciccase.search.CaseFieldsConstants.APPLICANT_NAME;
import static uk.gov.hmcts.sptribs.ciccase.search.CaseFieldsConstants.CASE_STATE;
import static uk.gov.hmcts.sptribs.ciccase.search.CaseFieldsConstants.CCD_REFERENCE;
import static uk.gov.hmcts.sptribs.ciccase.search.CaseFieldsConstants.HEARING_LOCATION;
import static uk.gov.hmcts.sptribs.ciccase.search.CaseFieldsConstants.REPRESENTATIVE_REFERENCE;
import static uk.gov.hmcts.sptribs.ciccase.search.CaseFieldsConstants.SUBJECT_ADDRESS;
import static uk.gov.hmcts.sptribs.ciccase.search.CaseFieldsConstants.SUBJECT_DATE_OF_BIRTH;
import static uk.gov.hmcts.sptribs.ciccase.search.CaseFieldsConstants.SUBJECT_NAME;

@Component
public class SearchInputFields implements CCDConfig<CaseData, State, UserRoleCIC> {

    public static final List<SearchField<UserRoleCIC>> SEARCH_FIELD_LIST = of(
        SearchField.<UserRoleCIC>builder().label("Case Number").id(CCD_REFERENCE).build(),
        SearchField.<UserRoleCIC>builder().label("Case status (state)").id(CASE_STATE).build(),
        SearchField.<UserRoleCIC>builder().label("Case status (state)").id(CASE_STATE).build(),
        SearchField.<UserRoleCIC>builder().label("Hearing Location").id(HEARING_LOCATION).build(),
        SearchField.<UserRoleCIC>builder().label("Subject Name").id(SUBJECT_NAME).build(),
        SearchField.<UserRoleCIC>builder().label("Subject PostCode").id(SUBJECT_ADDRESS).listElementCode("PostCode").build(),
        SearchField.<UserRoleCIC>builder().label("Subject Date of Birth").id(SUBJECT_DATE_OF_BIRTH).build(),
        SearchField.<UserRoleCIC>builder().label("Applicant Name").id(APPLICANT_NAME).build(),
        SearchField.<UserRoleCIC>builder().label("Representative Reference").id(REPRESENTATIVE_REFERENCE).build()
    );

    @Override
    public void configure(final ConfigBuilder<CaseData, State, UserRoleCIC> configBuilder) {
        configBuilder.searchInputFields().fields(SEARCH_FIELD_LIST);
    }
}
