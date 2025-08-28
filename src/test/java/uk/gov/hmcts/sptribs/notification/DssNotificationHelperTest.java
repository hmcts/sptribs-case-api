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

@ExtendWith(MockitoExtension.class)
public class DssNotificationHelperTest {


    private static final String CASE_NUMBER = "CN1";
    private static final String EMAIL_ID = "test@outlook.com";
    private static final String CICA_REFERENCE_NUMBER = "X/12/123456-TM1A";

    private static final EditCicaCaseDetails CICA_CASE_DETAILS = EditCicaCaseDetails.builder()
            .cicaReferenceNumber(CICA_REFERENCE_NUMBER).build();

    @InjectMocks
    private DssNotificationHelper dssNotificationHelper;

    @Test
    void shouldSetSubjectCommonVars() {
        final DssCaseData dssCaseData = getMockDssCaseData();
        final CaseData caseData = CaseData.builder().dssCaseData(dssCaseData).build();
        Map<String, Object> templateVars = dssNotificationHelper.getSubjectCommonVars(CASE_NUMBER, caseData);

        assertThat(templateVars.get(TRIBUNAL_NAME)).isEqualTo(CIC);
        assertThat(templateVars.get(CIC_CASE_NUMBER)).isEqualTo(CASE_NUMBER);
        assertThat(templateVars.get(CIC_CASE_SUBJECT_NAME)).isEqualTo(dssCaseData.getSubjectFullName());
        assertThat(templateVars.get(CONTACT_NAME)).isEqualTo(dssCaseData.getSubjectFullName());
        assertThat(templateVars.get(HAS_CICA_NUMBER)).isEqualTo(false);
        assertThat(templateVars.get(CICA_REF_NUMBER)).isEqualTo("");
    }

    @Test
    void shouldSetRepresentativeCommonVars() {
        final DssCaseData dssCaseData = getMockDssCaseData();
        final CaseData caseData = CaseData.builder().dssCaseData(dssCaseData).build();
        Map<String, Object> templateVars = dssNotificationHelper.getRepresentativeCommonVars(CASE_NUMBER, caseData);

        assertThat(templateVars.get(TRIBUNAL_NAME)).isEqualTo(CIC);
        assertThat(templateVars.get(CIC_CASE_NUMBER)).isEqualTo(CASE_NUMBER);
        assertThat(templateVars.get(CIC_CASE_SUBJECT_NAME)).isEqualTo(dssCaseData.getSubjectFullName());
        assertThat(templateVars.get(CONTACT_NAME)).isEqualTo(dssCaseData.getRepresentativeFullName());
        assertThat(templateVars.get(HAS_CICA_NUMBER)).isEqualTo(false);
        assertThat(templateVars.get(CICA_REF_NUMBER)).isEqualTo("");
    }

    @Test
    void shouldSetRepresentativeCommonVarsWithCicaReference() {
        final DssCaseData dssCaseData = getMockDssCaseData();
        final CaseData caseData = CaseData.builder().dssCaseData(dssCaseData).editCicaCaseDetails(CICA_CASE_DETAILS).build();
        Map<String, Object> templateVars = dssNotificationHelper.getRepresentativeCommonVars(CASE_NUMBER, caseData);

        assertThat(templateVars.get(TRIBUNAL_NAME)).isEqualTo(CIC);
        assertThat(templateVars.get(CIC_CASE_NUMBER)).isEqualTo(CASE_NUMBER);
        assertThat(templateVars.get(CIC_CASE_SUBJECT_NAME)).isEqualTo(dssCaseData.getSubjectFullName());
        assertThat(templateVars.get(CONTACT_NAME)).isEqualTo(dssCaseData.getRepresentativeFullName());
        assertThat(templateVars.get(HAS_CICA_NUMBER)).isEqualTo(true);
        assertThat(templateVars.get(CICA_REF_NUMBER)).isEqualTo(CICA_REFERENCE_NUMBER);
    }

    @Test
    void shouldSetSubjectCommonVarsWithCicaReference() {
        final DssCaseData dssCaseData = getMockDssCaseData();
        final CaseData caseData = CaseData.builder()
                .dssCaseData(dssCaseData)
                .editCicaCaseDetails(CICA_CASE_DETAILS)
                .build();
        Map<String, Object> templateVars = dssNotificationHelper.getSubjectCommonVars(CASE_NUMBER, caseData);

        assertThat(templateVars.get(TRIBUNAL_NAME)).isEqualTo(CIC);
        assertThat(templateVars.get(CIC_CASE_NUMBER)).isEqualTo(CASE_NUMBER);
        assertThat(templateVars.get(CIC_CASE_SUBJECT_NAME)).isEqualTo(dssCaseData.getSubjectFullName());
        assertThat(templateVars.get(CONTACT_NAME)).isEqualTo(dssCaseData.getSubjectFullName());
        assertThat(templateVars.get(HAS_CICA_NUMBER)).isEqualTo(true);
        assertThat(templateVars.get(CICA_REF_NUMBER)).isEqualTo(CICA_REFERENCE_NUMBER);
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
