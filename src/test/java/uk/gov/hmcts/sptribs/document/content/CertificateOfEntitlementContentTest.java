package uk.gov.hmcts.sptribs.document.content;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.sptribs.ciccase.model.Applicant;
import uk.gov.hmcts.sptribs.ciccase.model.Application;
import uk.gov.hmcts.sptribs.ciccase.model.ApplicationType;
import uk.gov.hmcts.sptribs.ciccase.model.CaseData;
import uk.gov.hmcts.sptribs.ciccase.model.ConditionalOrder;
import uk.gov.hmcts.sptribs.common.ConditionalOrderCourtDetails;
import uk.gov.hmcts.sptribs.common.config.ConditionalOrderCourtDetailsConfig;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.entry;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.ccd.sdk.type.YesOrNo.YES;
import static uk.gov.hmcts.sptribs.ciccase.model.ApplicationType.JOINT_APPLICATION;
import static uk.gov.hmcts.sptribs.ciccase.model.ApplicationType.SOLE_APPLICATION;
import static uk.gov.hmcts.sptribs.ciccase.model.ConditionalOrderCourt.BURY_ST_EDMUNDS;
import static uk.gov.hmcts.sptribs.ciccase.model.DivorceOrDissolution.DISSOLUTION;
import static uk.gov.hmcts.sptribs.ciccase.model.DivorceOrDissolution.DIVORCE;
import static uk.gov.hmcts.sptribs.ciccase.model.Gender.FEMALE;
import static uk.gov.hmcts.sptribs.ciccase.model.Gender.MALE;
import static uk.gov.hmcts.sptribs.ciccase.model.WhoDivorcing.HUSBAND;
import static uk.gov.hmcts.sptribs.document.content.DocmosisTemplateConstants.APPLICANT_1_FULL_NAME;
import static uk.gov.hmcts.sptribs.document.content.DocmosisTemplateConstants.APPLICANT_2_FULL_NAME;
import static uk.gov.hmcts.sptribs.document.content.DocmosisTemplateConstants.CCD_CASE_REFERENCE;
import static uk.gov.hmcts.sptribs.document.content.DocmosisTemplateConstants.CIVIL_PARTNERSHIP;
import static uk.gov.hmcts.sptribs.document.content.DocmosisTemplateConstants.DATE_OF_HEARING;
import static uk.gov.hmcts.sptribs.document.content.DocmosisTemplateConstants.HAS_FINANCIAL_ORDERS;
import static uk.gov.hmcts.sptribs.document.content.DocmosisTemplateConstants.MARRIAGE;
import static uk.gov.hmcts.sptribs.document.content.DocmosisTemplateConstants.MARRIAGE_OR_CIVIL_PARTNERSHIP;
import static uk.gov.hmcts.sptribs.document.content.DocmosisTemplateConstants.TIME_OF_HEARING;
import static uk.gov.hmcts.sptribs.testutil.TestConstants.TEST_CASE_ID;

@ExtendWith(MockitoExtension.class)
public class CertificateOfEntitlementContentTest {

    @Mock
    private ConditionalOrderCourtDetailsConfig conditionalOrderCourtDetailsConfig;

    @InjectMocks
    private CertificateOfEntitlementContent certificateOfEntitlementContent;

    @Test
    void shouldReturnTemplateContentForSole() {
        //Given
        final CaseData caseData = getCaseDataFor(SOLE_APPLICATION);
        caseData.setDivorceOrDissolution(DIVORCE);
        final ConditionalOrderCourtDetails expectedDetails = setupConditionalOrderCourtDetailsConfig();

        //When
        final Map<String, Object> contentMap = certificateOfEntitlementContent.apply(caseData, TEST_CASE_ID);

        //Then
        assertThat(contentMap).contains(
            entry(CCD_CASE_REFERENCE, "1616-5914-0147-3378"),
            entry("courtDetails", expectedDetails),
            entry("approvalDate", "8 November 2021"),
            entry(APPLICANT_1_FULL_NAME, "John Smith"),
            entry(APPLICANT_2_FULL_NAME, "Jane Jones"),
            entry(MARRIAGE_OR_CIVIL_PARTNERSHIP, MARRIAGE),
            entry("isSole", true),
            entry("isJoint", false),
            entry(DATE_OF_HEARING, "8 November 2021"),
            entry(TIME_OF_HEARING, "14:56 pm"),
            entry(HAS_FINANCIAL_ORDERS, true)
        );
    }

    @Test
    void shouldReturnTemplateContentForJoint() {
        //Given
        final CaseData caseData = getCaseDataFor(JOINT_APPLICATION);
        caseData.setDivorceOrDissolution(DIVORCE);
        final ConditionalOrderCourtDetails expectedDetails = setupConditionalOrderCourtDetailsConfig();

        //When
        final Map<String, Object> contentMap = certificateOfEntitlementContent.apply(caseData, TEST_CASE_ID);

        //Then
        assertThat(contentMap).contains(
            entry(CCD_CASE_REFERENCE, "1616-5914-0147-3378"),
            entry("courtDetails", expectedDetails),
            entry("approvalDate", "8 November 2021"),
            entry(APPLICANT_1_FULL_NAME, "John Smith"),
            entry(APPLICANT_2_FULL_NAME, "Jane Jones"),
            entry(MARRIAGE_OR_CIVIL_PARTNERSHIP, "marriage"),
            entry("isSole", false),
            entry("isJoint", true),
            entry(DATE_OF_HEARING, "8 November 2021"),
            entry(TIME_OF_HEARING, "14:56 pm"),
            entry(HAS_FINANCIAL_ORDERS, true)
        );
    }

    @Test
    void shouldReturnTemplateContentForCivilPartnership() {
        //Given
        final CaseData caseData = getCaseDataFor(JOINT_APPLICATION);
        caseData.setDivorceOrDissolution(DISSOLUTION);

        //When
        final Map<String, Object> contentMap = certificateOfEntitlementContent.apply(caseData, TEST_CASE_ID);

        //Then
        assertThat(contentMap).contains(
            entry(MARRIAGE_OR_CIVIL_PARTNERSHIP, CIVIL_PARTNERSHIP)
        );
    }

    @Test
    void shouldSetHasFinancialOrdersAndCostsGrantedToFalseIfNotSet() {
        //Given
        final CaseData caseData = getCaseDataFor(SOLE_APPLICATION);
        caseData.setDivorceOrDissolution(DIVORCE);
        caseData.getApplicant1().setFinancialOrder(null);
        caseData.getConditionalOrder().setClaimsGranted(null);

        final ConditionalOrderCourtDetails expectedDetails = setupConditionalOrderCourtDetailsConfig();

        //When
        final Map<String, Object> contentMap = certificateOfEntitlementContent.apply(caseData, TEST_CASE_ID);

        //Then
        assertThat(contentMap).contains(
            entry(CCD_CASE_REFERENCE, "1616-5914-0147-3378"),
            entry("courtDetails", expectedDetails),
            entry("approvalDate", "8 November 2021"),
            entry(APPLICANT_1_FULL_NAME, "John Smith"),
            entry(APPLICANT_2_FULL_NAME, "Jane Jones"),
            entry(MARRIAGE_OR_CIVIL_PARTNERSHIP, MARRIAGE),
            entry("isSole", true),
            entry("isJoint", false),
            entry(DATE_OF_HEARING, "8 November 2021"),
            entry(TIME_OF_HEARING, "14:56 pm"),
            entry(HAS_FINANCIAL_ORDERS, false)
        );
    }

    @Test
    void shouldSetValuesToNullIfNotSetInCaseData() {
        //Given
        final CaseData caseData = getCaseDataFor(SOLE_APPLICATION);
        caseData.setDivorceOrDissolution(DIVORCE);
        caseData.getApplicant1().setFinancialOrder(null);
        caseData.getConditionalOrder().setClaimsGranted(null);
        caseData.getConditionalOrder().setCourt(null);
        caseData.getConditionalOrder().setDecisionDate(null);
        caseData.getConditionalOrder().setDateAndTimeOfHearing(null);

        //When
        final Map<String, Object> contentMap = certificateOfEntitlementContent.apply(caseData, TEST_CASE_ID);

        //Then
        assertThat(contentMap).contains(
            entry(CCD_CASE_REFERENCE, "1616-5914-0147-3378"),
            entry("courtDetails", null),
            entry("approvalDate", null),
            entry(APPLICANT_1_FULL_NAME, "John Smith"),
            entry(APPLICANT_2_FULL_NAME, "Jane Jones"),
            entry(MARRIAGE_OR_CIVIL_PARTNERSHIP, MARRIAGE),
            entry("isSole", true),
            entry("isJoint", false),
            entry(DATE_OF_HEARING, null),
            entry(TIME_OF_HEARING, null),
            entry(HAS_FINANCIAL_ORDERS, false)
        );
    }

    private CaseData getCaseDataFor(final ApplicationType soleApplication) {
        final LocalDateTime localDateTime = LocalDateTime.of(2021, 11, 8, 14, 56);
        final LocalDate localDate = LocalDate.of(2021, 11, 8);
        return CaseData.builder()
            .applicationType(soleApplication)
            .applicant1(Applicant.builder()
                .firstName("John")
                .lastName("Smith")
                .financialOrder(YES)
                .gender(FEMALE)
                .build())
            .applicant2(Applicant.builder()
                .firstName("Jane")
                .lastName("Jones")
                .gender(MALE)
                .build())
            .conditionalOrder(ConditionalOrder.builder()
                .court(BURY_ST_EDMUNDS)
                .dateAndTimeOfHearing(localDateTime)
                .decisionDate(localDate)
                .claimsGranted(YES)
                .claimsCostsOrderInformation("info")
                .build())
            .application(Application.builder()
                .divorceWho(HUSBAND)
                .build())
            .build();
    }

    private ConditionalOrderCourtDetails setupConditionalOrderCourtDetailsConfig() {
        final ConditionalOrderCourtDetails expectedDetails = new ConditionalOrderCourtDetails();
        expectedDetails.setName("Bury St. Edmunds Regional Divorce Centre");
        expectedDetails.setAddress("2nd Floor\nTriton House\nSt. Andrews Street North\nBury St. Edmunds\nIP33 1TR");
        expectedDetails.setEmail("divorcecase@justice.gov.uk");
        expectedDetails.setPhone("0300 303 0642");

        when(conditionalOrderCourtDetailsConfig.get(BURY_ST_EDMUNDS.getCourtId())).thenReturn(expectedDetails);
        return expectedDetails;
    }
}
