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
                tuple("cicCaseFullName",
                    "Subject Name",
                    null,
                    null),
                tuple("[STATE]",
                    "Case Status",
                    null,
                    null),
                tuple("cicCaseRegionCIC",
                    "Case Region",
                    null,
                    null),
                tuple("firstHearingDate",
                    "Hearing Date",
                    null,
                    null),
                tuple("cicCaseApplicantFullName",
                    "Applicant Name",
                    null,
                    null),
                tuple("cicCaseFirstOrderDueDate",
                    "Due Date",
                    null,
                    null)
            );
    }
}
