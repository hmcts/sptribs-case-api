package uk.gov.hmcts.sptribs.document.model;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Getter;
import uk.gov.hmcts.ccd.sdk.api.HasLabel;

@Getter
@AllArgsConstructor
public enum DocumentType implements HasLabel {

    // New added doc types

    @JsonProperty("caseApplicationForm")
    @JsonAlias({"A Docs"})
    CASE_APPLICATION_FORM("Case Application Form"),

    @JsonProperty("TribunalDirection")
    @JsonAlias({"decisionNotices", "TD"})
    TRIBUNAL_DIRECTION("Tribunal Direction"),

    @JsonProperty("PoliceEvidence")
    @JsonAlias({"B Docs"})
    POLICE_EVIDENCE("Police Evidence"),

    @JsonProperty("MedicalEvidence")
    @JsonAlias({"C Docs"})
    MEDICAL_EVIDENCE("Medical Evidence"),

    @JsonProperty("LossOfEarnings")
    @JsonAlias({"financialInformation", "D Docs"})
    LOSS_OF_EARNINGS("Loss Of Earnings"),

    @JsonProperty("CareDocuments")
    @JsonAlias({"E Docs"})
    CARE_DOCUMENTS("Care Documents"),

    @JsonProperty("LinkedDocumentsFromALinkedPastCase")
    @JsonAlias({"linkedDocuments", "L Docs"})
    LINKED_DOCUMENTS_FROM_A_LINKED_PAST_CASE("Linked Documents From A Linked Past Case"),

    @JsonProperty("Statements")
    @JsonAlias({"S Docs"})
    STATEMENTS("Statements"),

    @JsonProperty("GeneralEvidence")
    @JsonAlias({"TG"})
    GENERAL_EVIDENCE("General Evidence"),


    //old for family divorce ones
    @JsonProperty("application")
    @JsonAlias("divorceApplication")
    APPLICATION("Application"),

    @JsonProperty("certificateOfEntitlement")
    CERTIFICATE_OF_ENTITLEMENT("Certificate of entitlement"),

    @JsonProperty("conditionalOrderAnswers")
    CONDITIONAL_ORDER_ANSWERS("Conditional order answers"),

    @JsonProperty("conditionalOrderGranted")
    CONDITIONAL_ORDER_GRANTED("Conditional Order Granted"),

    @JsonProperty("d10")
    D10("D10"),

    @JsonProperty("email")
    EMAIL("Email"),
    @JsonProperty("generalOrder")
    GENERAL_ORDER("General order"),

    @JsonProperty("marriageCertificate")
    MARRIAGE_CERTIFICATE("Marriage/Civil Partnership Certificate"),

    @JsonProperty("nameChangeEvidence")
    NAME_CHANGE_EVIDENCE("Name change evidence"),

    @JsonProperty("noticeOfProceedings")
    NOTICE_OF_PROCEEDINGS_APP_1("Notice of proceedings for applicant/applicant 1"),

    @JsonProperty("noticeOfProceedingsApp2")
    NOTICE_OF_PROCEEDINGS_APP_2("Notice of proceedings for respondent/applicant 2"),

    @JsonProperty("other")
    @JsonAlias({"aosOfflineInvitationLetterToApplicant2"})
    OTHER("Other"),
    @JsonProperty("respondentAnswers")
    @JsonAlias("applicant2Answers")
    RESPONDENT_ANSWERS("Respondent answers"),

    @JsonProperty("aosResponseLetter")
    AOS_RESPONSE_LETTER("Aos response letter"),

    @JsonProperty("aosOverdueLetter")
    AOS_OVERDUE_LETTER("Aos overdue letter"),

    @JsonProperty("generalLetter")
    GENERAL_LETTER("General letter");


    private final String label;
}
