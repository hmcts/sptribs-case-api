package uk.gov.hmcts.sptribs.ciccase.search;

import org.springframework.stereotype.Component;
import uk.gov.hmcts.ccd.sdk.api.CCDConfig;
import uk.gov.hmcts.ccd.sdk.api.ConfigBuilder;
import uk.gov.hmcts.ccd.sdk.api.SearchField;
import uk.gov.hmcts.sptribs.ciccase.model.CaseData;
import uk.gov.hmcts.sptribs.ciccase.model.State;
import uk.gov.hmcts.sptribs.ciccase.model.UserRole;

import java.util.List;

import static java.util.List.of;
import static uk.gov.hmcts.sptribs.ciccase.search.CaseFieldsConstants.APPLICANT_NAME;
import static uk.gov.hmcts.sptribs.ciccase.search.CaseFieldsConstants.CASE_CATEGORY;
import static uk.gov.hmcts.sptribs.ciccase.search.CaseFieldsConstants.CASE_REGION;
import static uk.gov.hmcts.sptribs.ciccase.search.CaseFieldsConstants.CASE_STATE;
import static uk.gov.hmcts.sptribs.ciccase.search.CaseFieldsConstants.CASE_SUBCATEGORY;
import static uk.gov.hmcts.sptribs.ciccase.search.CaseFieldsConstants.CCD_REFERENCE;
import static uk.gov.hmcts.sptribs.ciccase.search.CaseFieldsConstants.HEARING_FORMAT;
import static uk.gov.hmcts.sptribs.ciccase.search.CaseFieldsConstants.HEARING_LOCATION;
import static uk.gov.hmcts.sptribs.ciccase.search.CaseFieldsConstants.PANEL_COMPOSITION;
import static uk.gov.hmcts.sptribs.ciccase.search.CaseFieldsConstants.REPRESENTATIVE_REFERENCE;
import static uk.gov.hmcts.sptribs.ciccase.search.CaseFieldsConstants.SCHEME;
import static uk.gov.hmcts.sptribs.ciccase.search.CaseFieldsConstants.SHORT_NOTICE;
import static uk.gov.hmcts.sptribs.ciccase.search.CaseFieldsConstants.SUBJECT_ADDRESS;
import static uk.gov.hmcts.sptribs.ciccase.search.CaseFieldsConstants.SUBJECT_DATE_OF_BIRTH;
import static uk.gov.hmcts.sptribs.ciccase.search.CaseFieldsConstants.SUBJECT_NAME;

@Component
public class SearchInputFields implements CCDConfig<CaseData, State, UserRole> {

    public static final List<SearchField<UserRole>> SEARCH_FIELD_LIST = of(
        SearchField.<UserRole>builder().label("Case Number").id(CCD_REFERENCE).build(),
        SearchField.<UserRole>builder().label("Case status (state)").id(CASE_STATE).build(),
        SearchField.<UserRole>builder().label("Case status (state)").id(CASE_STATE).build(),
        SearchField.<UserRole>builder().label("Case Region").id(CASE_REGION).build(),
        SearchField.<UserRole>builder().label("Case category").id(CASE_CATEGORY).build(),
        SearchField.<UserRole>builder().label("Case sub-category").id(CASE_SUBCATEGORY).build(),
        SearchField.<UserRole>builder().label("Scheme").id(SCHEME).build(),
        SearchField.<UserRole>builder().label("Case Region").id(CASE_REGION).build(),
        SearchField.<UserRole>builder().label("Panel Composition").id(PANEL_COMPOSITION).build(),
        SearchField.<UserRole>builder().label("Hearing Location").id(HEARING_LOCATION).build(),
        SearchField.<UserRole>builder().label("Subject Name").id(SUBJECT_NAME).build(),
        SearchField.<UserRole>builder().label("Subject PostCode").id(SUBJECT_ADDRESS).listElementCode("PostCode").build(),
        SearchField.<UserRole>builder().label("Subject Date of Birth").id(SUBJECT_DATE_OF_BIRTH).build(),
        SearchField.<UserRole>builder().label("Applicant Name").id(APPLICANT_NAME).build(),
        SearchField.<UserRole>builder().label("Representative Reference").id(REPRESENTATIVE_REFERENCE).build(),
        SearchField.<UserRole>builder().label("Hearing Format").id(HEARING_FORMAT).build(),
        SearchField.<UserRole>builder().label("Short Notice").id(SHORT_NOTICE).build()
    );


    @Override
    public void configure(final ConfigBuilder<CaseData, State, UserRole> configBuilder) {
        configBuilder.searchInputFields().fields(SEARCH_FIELD_LIST);
    }
}
