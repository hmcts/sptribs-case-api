package uk.gov.hmcts.sptribs.ciccase.workbasket;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import uk.gov.hmcts.ccd.sdk.ConfigBuilderImpl;
import uk.gov.hmcts.sptribs.ciccase.model.CaseData;
import uk.gov.hmcts.sptribs.ciccase.model.State;
import uk.gov.hmcts.sptribs.ciccase.model.UserRole;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.tuple;
import static uk.gov.hmcts.sptribs.testutil.ConfigTestUtil.createCaseDataConfigBuilder;
import static uk.gov.hmcts.sptribs.testutil.ConfigTestUtil.getWorkBasketInputFields;

class WorkBasketInputFieldsTest {
    private WorkBasketInputFields workBasketInputFields;

    @BeforeEach
    void setUp() {
        workBasketInputFields = new WorkBasketInputFields();
    }

    @Test
    void shouldSetWorkBasketResultFields() throws Exception {
        //Given
        final ConfigBuilderImpl<CaseData, State, UserRole> configBuilder = createCaseDataConfigBuilder();

        //When
        workBasketInputFields.configure(configBuilder);

        //Then
        assertThat(getWorkBasketInputFields(configBuilder).getFields())
            .extracting("id",
                "label",
                "listElementCode",
                "showCondition")
            .contains(
                tuple("[CASE_REFERENCE]",
                    "Case Number",
                    null,
                    null),
                tuple("hearingVenueName",
                    "Hearing Location",
                    null,
                    null),
                tuple("cicCaseRegionCIC",
                    "Case Region",
                    null,
                    null),
                tuple("cicCaseFullName",
                    "Subject Name",
                    null,
                    null),
                tuple("cicCaseAddress",
                    "Subject PostCode",
                    "PostCode",
                    null),
                tuple("cicCaseDateOfBirth",
                    "Subject Date of Birth",
                    null,
                    null),
                tuple("cicCaseApplicantFullName",
                    "Applicant Name",
                    null,
                    null),
                tuple("cicCaseRepresentativeReference",
                    "Representative Reference",
                    null,
                    null),
                tuple("hearingFormat",
                    "Hearing Format",
                    null,
                    null),
                tuple("shortNotice",
                    "Short Notice",
                    null,
                    null)
            );
    }
}
