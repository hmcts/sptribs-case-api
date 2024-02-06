package uk.gov.hmcts.sptribs.systemupdate.event;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import uk.gov.hmcts.ccd.sdk.ConfigBuilderImpl;
import uk.gov.hmcts.ccd.sdk.api.CaseDetails;
import uk.gov.hmcts.ccd.sdk.api.Event;
import uk.gov.hmcts.ccd.sdk.api.callback.AboutToStartOrSubmitResponse;
import uk.gov.hmcts.sptribs.ciccase.model.CaseData;
import uk.gov.hmcts.sptribs.ciccase.model.CicCase;
import uk.gov.hmcts.sptribs.ciccase.model.State;
import uk.gov.hmcts.sptribs.ciccase.model.UserRole;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static uk.gov.hmcts.sptribs.systemupdate.event.SystemMigrateCaseLinks.SYSTEM_MIGRATE_CASE_LINKS;
import static uk.gov.hmcts.sptribs.testutil.ConfigTestUtil.createCaseDataConfigBuilder;
import static uk.gov.hmcts.sptribs.testutil.ConfigTestUtil.getEventsFrom;
import static uk.gov.hmcts.sptribs.testutil.TestConstants.TEST_CASE_ID;
import static uk.gov.hmcts.sptribs.testutil.TestConstants.TEST_FIRST_NAME;
import static uk.gov.hmcts.sptribs.testutil.TestConstants.TEST_SOLICITOR_NAME;
import static uk.gov.hmcts.sptribs.testutil.TestDataHelper.LOCAL_DATE_TIME;
import static uk.gov.hmcts.sptribs.testutil.TestDataHelper.caseData;

@ExtendWith(SpringExtension.class)
class SystemMigrateCaseLinksTest {
    @InjectMocks
    private SystemMigrateCaseLinks systemMigrateCaseLinks;

    @Test
    void shouldAddConfigurationToConfigBuilderWithToggleOn() {
        final ConfigBuilderImpl<CaseData, State, UserRole> configBuilder = createCaseDataConfigBuilder();

        systemMigrateCaseLinks.setCaseLinksMigrationEnabled(true);
        systemMigrateCaseLinks.configure(configBuilder);

        assertThat(getEventsFrom(configBuilder).values())
            .extracting(Event::getId)
            .contains(SYSTEM_MIGRATE_CASE_LINKS);
    }

    @Test
    void shouldAddConfigurationToConfigBuilderWithToggleOff() {
        final ConfigBuilderImpl<CaseData, State, UserRole> configBuilder = createCaseDataConfigBuilder();

        systemMigrateCaseLinks.setCaseLinksMigrationEnabled(false);
        systemMigrateCaseLinks.configure(configBuilder);

        assertThat(getEventsFrom(configBuilder).values())
            .extracting(Event::getId)
            .doesNotContain(SYSTEM_MIGRATE_CASE_LINKS);
    }

    @Test
    void shouldSuccessfullyUpdateCaseLinks() {
        //Given
        final CaseData caseData = caseData();
        final CicCase cicCase = CicCase.builder()
            .fullName(TEST_FIRST_NAME)
            .representativeFullName(TEST_SOLICITOR_NAME)
            .build();
        caseData.setCicCase(cicCase);
        final CaseDetails<CaseData, State> updatedCaseDetails = new CaseDetails<>();
        final CaseDetails<CaseData, State> beforeDetails = new CaseDetails<>();
        beforeDetails.setData(caseData);
        updatedCaseDetails.setData(caseData);
        updatedCaseDetails.setId(TEST_CASE_ID);
        updatedCaseDetails.setCreatedDate(LOCAL_DATE_TIME);

        //When
        AboutToStartOrSubmitResponse<CaseData, State> response =
            systemMigrateCaseLinks.aboutToSubmit(updatedCaseDetails, beforeDetails);

        //Then
        assertNotNull(response.getData());
        assertEquals(TEST_FIRST_NAME,response.getData().getCaseNameHmctsInternal());
    }

}
