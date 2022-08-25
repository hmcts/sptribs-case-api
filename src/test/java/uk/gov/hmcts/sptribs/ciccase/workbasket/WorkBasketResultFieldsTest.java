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
import static uk.gov.hmcts.sptribs.testutil.ConfigTestUtil.getWorkBasketResultFields;

class WorkBasketResultFieldsTest {

    private WorkBasketResultFields workBasketResultFields;

    @BeforeEach
    void setUp() {
        workBasketResultFields = new WorkBasketResultFields();
    }

    @Test
    void shouldSetWorkBasketResultFields() throws Exception {
        //Given
        final ConfigBuilderImpl<CaseData, State, UserRole> configBuilder = createCaseDataConfigBuilder();

        //When
        workBasketResultFields.configure(configBuilder);

        //Then
        assertThat(getWorkBasketResultFields(configBuilder).getFields())
            .extracting("id",
                "label",
                "listElementCode",
                "showCondition")
            .contains(
                tuple("[CASE_REFERENCE]",
                    "Case Number",
                    null,
                    null),
                tuple("applicationType",
                    "Application Type",
                    null,
                    null),
                tuple("applicant1FirstName",
                    "Applicant's First Name",
                    null,
                    null),
                tuple("applicant1LastName",
                    "Applicant's Last Name",
                    null,
                    null),
                tuple("applicant2FirstName",
                    "Respondent's First Name",
                    null,
                    null),
                tuple("applicant2LastName",
                    "Respondent's Last Name",
                    null,
                    null),
                tuple("dueDate",
                    "Due Date",
                    null,
                    null)
            );
    }
}
