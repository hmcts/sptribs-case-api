package uk.gov.hmcts.sptribs.notification;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.sptribs.caseworker.model.EditCicaCaseDetails;
import uk.gov.hmcts.sptribs.ciccase.model.CaseData;
import uk.gov.hmcts.sptribs.ciccase.model.DssCaseData;
import uk.gov.hmcts.sptribs.notification.model.NotificationRequest;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static uk.gov.hmcts.sptribs.common.CommonConstants.CICA_REF_NUMBER;
import static uk.gov.hmcts.sptribs.common.CommonConstants.CIC_CASE_NUMBER;
import static uk.gov.hmcts.sptribs.common.CommonConstants.CIC_CASE_SUBJECT_NAME;
import static uk.gov.hmcts.sptribs.common.CommonConstants.CONTACT_NAME;
import static uk.gov.hmcts.sptribs.common.CommonConstants.HAS_CICA_NUMBER;
import static uk.gov.hmcts.sptribs.common.CommonConstants.TRIBUNAL_NAME;
import static uk.gov.hmcts.sptribs.common.ccd.CcdCaseType.CIC;
import static uk.gov.hmcts.sptribs.testutil.TestConstants.TEST_CASE_ID;

@ExtendWith(MockitoExtension.class)
public class DssNotificationHelperTest {


    private static final String CASE_NUMBER = TEST_CASE_ID.toString();
    private static final String EMAIL_ID = "test@outlook.com";
    private static final String CICA_REFERENCE_NUMBER = "X/12/123456-TM1A";

    private static final EditCicaCaseDetails CICA_CASE_DETAILS = EditCicaCaseDetails.builder()
            .cicaReferenceNumber(CICA_REFERENCE_NUMBER).build();
    private static final EditCicaCaseDetails EMPTY_CICA_CASE_DETAILS = EditCicaCaseDetails.builder()
            .cicaReferenceNumber("").build();

    @InjectMocks
    private DssNotificationHelper dssNotificationHelper;

    @Test
    void shouldSetSubjectCommonVars() {
        final DssCaseData dssCaseData = getMockDssCaseData();
        final CaseData caseData = CaseData.builder().dssCaseData(dssCaseData).build();
        Map<String, Object> templateVars = dssNotificationHelper.getSubjectCommonVars(CASE_NUMBER, caseData);

        assertThat(templateVars)
                .containsEntry(TRIBUNAL_NAME, CIC)
                .containsEntry(CIC_CASE_NUMBER, CASE_NUMBER)
                .containsEntry(CIC_CASE_SUBJECT_NAME, dssCaseData.getSubjectFullName())
                .containsEntry(CONTACT_NAME, dssCaseData.getSubjectFullName())
                .containsEntry(HAS_CICA_NUMBER, false)
                .containsEntry(CICA_REF_NUMBER, "");
    }

    @Test
    void shouldSetRepresentativeCommonVars() {
        final DssCaseData dssCaseData = getMockDssCaseData();
        final CaseData caseData = CaseData.builder().dssCaseData(dssCaseData).build();
        Map<String, Object> templateVars = dssNotificationHelper.getRepresentativeCommonVars(CASE_NUMBER, caseData);

        assertThat(templateVars).containsEntry(TRIBUNAL_NAME, CIC)
                .containsEntry(CIC_CASE_NUMBER, CASE_NUMBER)
                .containsEntry(CIC_CASE_SUBJECT_NAME, dssCaseData.getSubjectFullName())
                .containsEntry(CONTACT_NAME, dssCaseData.getRepresentativeFullName())
                .containsEntry(HAS_CICA_NUMBER, false)
                .containsEntry(CICA_REF_NUMBER, "");
    }

    @Test
    void shouldSetRepresentativeCommonVarsWithCicaReference() {
        final DssCaseData dssCaseData = getMockDssCaseData();
        final CaseData caseData = CaseData.builder().dssCaseData(dssCaseData).editCicaCaseDetails(CICA_CASE_DETAILS).build();
        Map<String, Object> templateVars = dssNotificationHelper.getRepresentativeCommonVars(CASE_NUMBER, caseData);

        assertThat(templateVars).containsEntry(TRIBUNAL_NAME, CIC)
                .containsEntry(CIC_CASE_NUMBER, CASE_NUMBER)
                .containsEntry(CIC_CASE_SUBJECT_NAME, dssCaseData.getSubjectFullName())
                .containsEntry(CONTACT_NAME, dssCaseData.getRepresentativeFullName())
                .containsEntry(HAS_CICA_NUMBER, true)
                .containsEntry(CICA_REF_NUMBER, CICA_REFERENCE_NUMBER);
    }

    @Test
    void shouldSetSubjectCommonVarsWithCicaReference() {
        final DssCaseData dssCaseData = getMockDssCaseData();
        final CaseData caseData = CaseData.builder()
                .dssCaseData(dssCaseData)
                .editCicaCaseDetails(CICA_CASE_DETAILS)
                .build();
        Map<String, Object> templateVars = dssNotificationHelper.getSubjectCommonVars(CASE_NUMBER, caseData);

        assertThat(templateVars).containsEntry(TRIBUNAL_NAME, CIC)
                .containsEntry(CIC_CASE_NUMBER, CASE_NUMBER)
                .containsEntry(CIC_CASE_SUBJECT_NAME, dssCaseData.getSubjectFullName())
                .containsEntry(CONTACT_NAME, dssCaseData.getSubjectFullName())
                .containsEntry(HAS_CICA_NUMBER, true)
                .containsEntry(CICA_REF_NUMBER, CICA_REFERENCE_NUMBER);
    }

    @Test
    void shouldSetRepresentativeCommonVarsWithEmptyCicaReference() {
        final DssCaseData dssCaseData = getMockDssCaseData();
        final CaseData caseData = CaseData.builder().dssCaseData(dssCaseData).editCicaCaseDetails(EMPTY_CICA_CASE_DETAILS).build();
        Map<String, Object> templateVars = dssNotificationHelper.getRepresentativeCommonVars(CASE_NUMBER, caseData);

        assertThat(templateVars).containsEntry(TRIBUNAL_NAME, CIC)
                .containsEntry(CIC_CASE_NUMBER, CASE_NUMBER)
                .containsEntry(CIC_CASE_SUBJECT_NAME, dssCaseData.getSubjectFullName())
                .containsEntry(CONTACT_NAME, dssCaseData.getRepresentativeFullName())
                .containsEntry(HAS_CICA_NUMBER, false)
                .containsEntry(CICA_REF_NUMBER, "");
    }

    @Test
    void shouldSetSubjectCommonVarsWithEmptyCicaReference() {
        final DssCaseData dssCaseData = getMockDssCaseData();
        final CaseData caseData = CaseData.builder().dssCaseData(dssCaseData).editCicaCaseDetails(EMPTY_CICA_CASE_DETAILS).build();
        Map<String, Object> templateVars = dssNotificationHelper.getSubjectCommonVars(CASE_NUMBER, caseData);

        assertThat(templateVars).containsEntry(TRIBUNAL_NAME, CIC)
                .containsEntry(CIC_CASE_NUMBER, CASE_NUMBER)
                .containsEntry(CIC_CASE_SUBJECT_NAME, dssCaseData.getSubjectFullName())
                .containsEntry(CONTACT_NAME, dssCaseData.getSubjectFullName())
                .containsEntry(HAS_CICA_NUMBER, false)
                .containsEntry(CICA_REF_NUMBER, "");
    }

    @Test
    void shouldBuildEmailNotificationRequest() {
        final DssCaseData dssCaseData = getMockDssCaseData();
        final CaseData caseData = CaseData.builder().dssCaseData(dssCaseData).build();
        Map<String, Object> templateVars = dssNotificationHelper.getSubjectCommonVars(CASE_NUMBER, caseData);

        NotificationRequest notificationRequest = dssNotificationHelper.buildEmailNotificationRequest(
            EMAIL_ID,
            templateVars,
            TemplateName.APPLICATION_RECEIVED
        );

        assertThat(notificationRequest.getDestinationAddress()).isEqualTo(EMAIL_ID);
        assertThat(notificationRequest.getTemplate()).isEqualTo(TemplateName.APPLICATION_RECEIVED);
        assertThat(notificationRequest.getTemplateVars()).isEqualTo(templateVars);
    }

    private DssCaseData getMockDssCaseData() {
        return DssCaseData.builder()
            .subjectFullName("Subject Full Name")
            .representativeFullName("Rep Full Name")
            .build();
    }
}
