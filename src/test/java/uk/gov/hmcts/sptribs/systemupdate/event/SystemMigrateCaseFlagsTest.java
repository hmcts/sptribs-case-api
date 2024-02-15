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
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static uk.gov.hmcts.sptribs.systemupdate.event.SystemMigrateCaseFlags.SYSTEM_MIGRATE_CASE_FLAGS;
import static uk.gov.hmcts.sptribs.testutil.ConfigTestUtil.createCaseDataConfigBuilder;
import static uk.gov.hmcts.sptribs.testutil.ConfigTestUtil.getEventsFrom;
import static uk.gov.hmcts.sptribs.testutil.TestConstants.TEST_CASE_ID;
import static uk.gov.hmcts.sptribs.testutil.TestConstants.TEST_FIRST_NAME;
import static uk.gov.hmcts.sptribs.testutil.TestConstants.TEST_SOLICITOR_NAME;
import static uk.gov.hmcts.sptribs.testutil.TestDataHelper.LOCAL_DATE_TIME;
import static uk.gov.hmcts.sptribs.testutil.TestDataHelper.caseData;

@ExtendWith(SpringExtension.class)
class SystemMigrateCaseFlagsTest {
    @InjectMocks
    private SystemMigrateCaseFlags systemMigrateCaseFlags;


    @Test
    void shouldAddConfigurationToConfigBuilderWithToggleOn() {
        final ConfigBuilderImpl<CaseData, State, UserRole> configBuilder = createCaseDataConfigBuilder();

        systemMigrateCaseFlags.setMigrationFlagEnabled(true);
        systemMigrateCaseFlags.configure(configBuilder);

        assertThat(getEventsFrom(configBuilder).values())
            .extracting(Event::getId)
            .contains(SYSTEM_MIGRATE_CASE_FLAGS);
    }

    @Test
    void shouldNotAddConfigurationToConfigBuilderWithToggleOff() {
        final ConfigBuilderImpl<CaseData, State, UserRole> configBuilder = createCaseDataConfigBuilder();

        systemMigrateCaseFlags.setMigrationFlagEnabled(false);
        systemMigrateCaseFlags.configure(configBuilder);

        assertThat(getEventsFrom(configBuilder).values())
            .extracting(Event::getId)
            .doesNotContain(SYSTEM_MIGRATE_CASE_FLAGS);
    }

    @Test
    void shouldSuccessfullyUpdateCaseFlags() {
        //Given
        final CaseData beforeCaseData = caseData();
        final CaseData updatedCaseData = caseData();
        final CicCase cicCase = CicCase.builder()
            .fullName(TEST_FIRST_NAME)
            .applicantFullName(TEST_FIRST_NAME)
            .representativeFullName(TEST_SOLICITOR_NAME)
            .build();
        updatedCaseData.setCicCase(cicCase);
        final CaseDetails<CaseData, State> updatedCaseDetails = new CaseDetails<>();
        final CaseDetails<CaseData, State> beforeDetails = new CaseDetails<>();
        beforeDetails.setData(beforeCaseData);
        updatedCaseDetails.setData(updatedCaseData);
        updatedCaseDetails.setId(TEST_CASE_ID);
        updatedCaseDetails.setCreatedDate(LOCAL_DATE_TIME);
        systemMigrateCaseFlags.setMigrationFlagEnabled(true);

        //When
        final AboutToStartOrSubmitResponse<CaseData, State> response =
            systemMigrateCaseFlags.aboutToSubmit(updatedCaseDetails, beforeDetails);

        //Then
        assertNotEquals(response.getData().getCaseFlags(), beforeDetails.getData().getCaseFlags());
        assertTrue(response.getData().getCaseFlags().getDetails().isEmpty());
        assertEquals(response.getData().getSubjectFlags().getPartyName(),TEST_FIRST_NAME);
        assertEquals(response.getData().getSubjectFlags().getRoleOnCase(),"subject");
        assertEquals(response.getData().getApplicantFlags().getRoleOnCase(), "applicant");
        assertEquals(response.getData().getApplicantFlags().getPartyName(), TEST_FIRST_NAME);
        assertEquals(response.getData().getRepresentativeFlags().getRoleOnCase(), "Representative");
        assertEquals(response.getData().getRepresentativeFlags().getPartyName(), TEST_SOLICITOR_NAME);
    }

    @Test
    void shouldNotUpdateCaseFlags() {
        //Given
        final CaseData beforeCaseData = caseData();
        final CaseData updatedCaseData = caseData();
        final CicCase updatedCicCase = CicCase.builder()
            .fullName(TEST_FIRST_NAME)
            .applicantFullName(TEST_FIRST_NAME)
            .representativeFullName(TEST_SOLICITOR_NAME)
            .build();

        updatedCaseData.setCicCase(updatedCicCase);
        final CaseDetails<CaseData, State> updatedCaseDetails = new CaseDetails<>();
        final CaseDetails<CaseData, State> beforeDetails = new CaseDetails<>();
        beforeDetails.setData(beforeCaseData);
        updatedCaseDetails.setData(updatedCaseData);
        updatedCaseDetails.setId(TEST_CASE_ID);
        updatedCaseDetails.setCreatedDate(LOCAL_DATE_TIME);
        systemMigrateCaseFlags.setMigrationFlagEnabled(false);

        //When
        final AboutToStartOrSubmitResponse<CaseData, State> response =
            systemMigrateCaseFlags.aboutToSubmit(updatedCaseDetails, beforeDetails);

        //Then
        assertEquals(response.getData().getCaseFlags(), beforeDetails.getData().getCaseFlags());
        assertNull(response.getData().getSubjectFlags());
        assertNull(response.getData().getApplicantFlags());
        assertNull(response.getData().getRepresentativeFlags());
    }

    @Test
    void shouldSuccessfullyUpdateCaseFlagsWithNullValues() {
        //Given
        final CaseData beforeCaseData = caseData();
        final CaseData updatedCaseData = caseData();
        final CicCase cicCase = CicCase.builder()
            .fullName(null)
            .applicantFullName(null)
            .representativeFullName(null)
            .build();
        updatedCaseData.setCicCase(cicCase);
        final CaseDetails<CaseData, State> updatedCaseDetails = new CaseDetails<>();
        final CaseDetails<CaseData, State> beforeDetails = new CaseDetails<>();
        beforeDetails.setData(beforeCaseData);
        updatedCaseDetails.setData(updatedCaseData);
        updatedCaseDetails.setId(TEST_CASE_ID);
        updatedCaseDetails.setCreatedDate(LOCAL_DATE_TIME);
        systemMigrateCaseFlags.setMigrationFlagEnabled(true);

        //When
        final AboutToStartOrSubmitResponse<CaseData, State> response =
            systemMigrateCaseFlags.aboutToSubmit(updatedCaseDetails, beforeDetails);

        //Then
        assertNotEquals(response.getData().getCaseFlags(), beforeCaseData.getCaseFlags());
        assertNotNull(response.getData().getCaseFlags());
        assertNull(response.getData().getSubjectFlags());
        assertNull(response.getData().getApplicantFlags());
        assertNull(response.getData().getRepresentativeFlags());
    }
}
