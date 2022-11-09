package uk.gov.hmcts.sptribs.document.content;


import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.ccd.sdk.type.AddressGlobalUK;
import uk.gov.hmcts.ccd.sdk.type.YesOrNo;
import uk.gov.hmcts.sptribs.ciccase.model.Applicant;
import uk.gov.hmcts.sptribs.ciccase.model.Application;
import uk.gov.hmcts.sptribs.ciccase.model.CaseData;
import uk.gov.hmcts.sptribs.ciccase.model.CaseInvite;
import uk.gov.hmcts.sptribs.common.config.DocmosisTemplatesConfig;
import uk.gov.hmcts.sptribs.common.service.HoldingPeriodService;
import uk.gov.hmcts.sptribs.notification.CommonContent;

import java.time.LocalDate;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.entry;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.sptribs.ciccase.model.ApplicationType.SOLE_APPLICATION;
import static uk.gov.hmcts.sptribs.ciccase.model.DivorceOrDissolution.DIVORCE;
import static uk.gov.hmcts.sptribs.document.content.DocmosisTemplateConstants.CASE_REFERENCE;
import static uk.gov.hmcts.sptribs.document.content.DocmosisTemplateConstants.ISSUE_DATE;
import static uk.gov.hmcts.sptribs.document.content.NoticeOfProceedingContent.HAS_CASE_BEEN_REISSUED;
import static uk.gov.hmcts.sptribs.document.content.NoticeOfProceedingContent.REISSUE_DATE;
import static uk.gov.hmcts.sptribs.testutil.TestConstants.FORMATTED_TEST_CASE_ID;
import static uk.gov.hmcts.sptribs.testutil.TestConstants.TEST_CASE_ID;
import static uk.gov.hmcts.sptribs.testutil.TestDataHelper.applicantRepresentedBySolicitor;
import static uk.gov.hmcts.sptribs.testutil.TestDataHelper.organisationPolicy;

@ExtendWith(MockitoExtension.class)
public class NoticeOfProceedingContentTest {

    private static final String ADDRESS = "line 1\ntown\npostcode";
    private static final LocalDate APPLICATION_ISSUE_DATE = LocalDate.of(2022, 3, 30);

    @Mock
    private HoldingPeriodService holdingPeriodService;

    @Mock
    private DocmosisTemplatesConfig config;

    @Mock
    private CommonContent commonContent;

    @InjectMocks
    private NoticeOfProceedingContent applicantNopContent;

    @Test
    public void shouldMapTemplateContentForSoleDivorceApplication() {
        //Given
        Applicant applicant1 = applicantRepresentedBySolicitor();
        applicant1.getSolicitor().setOrganisationPolicy(organisationPolicy());
        applicant1.getSolicitor().setAddress(ADDRESS);
        applicant1.getSolicitor().setReference("12345");
        applicant1.setSolicitorRepresented(YesOrNo.YES);
        Applicant applicant2 = applicantRepresentedBySolicitor();
        applicant2.setSolicitorRepresented(YesOrNo.YES);
        applicant2.setAddress(new AddressGlobalUK());
        applicant1.getSolicitor().setAddress(ADDRESS);
        CaseInvite caseInvite = CaseInvite.builder().accessCode("code").build();
        CaseData caseData = CaseData.builder()
            .applicant1(applicant1)
            .caseInvite(caseInvite)
            .applicant2(applicant2)
            .divorceOrDissolution(DIVORCE)
            .applicationType(SOLE_APPLICATION)
            .application(Application.builder()
                .issueDate(APPLICATION_ISSUE_DATE)
                .build())
            .build();

        when(commonContent.getPartner(any(), any()))
            .thenReturn("");
        when(holdingPeriodService.getDueDateFor(APPLICATION_ISSUE_DATE))
            .thenReturn(APPLICATION_ISSUE_DATE.plusDays(16));
        //When
        final Map<String, Object> templateContent = applicantNopContent.apply(caseData, TEST_CASE_ID, applicant1);

        //Then
        assertThat(templateContent)
            .contains(
                entry(ISSUE_DATE, "30 March 2022"),
                entry(CASE_REFERENCE, FORMATTED_TEST_CASE_ID))
            .doesNotContain(
                entry(HAS_CASE_BEEN_REISSUED, true),
                entry(REISSUE_DATE, "30 April 2022"));
    }
}
