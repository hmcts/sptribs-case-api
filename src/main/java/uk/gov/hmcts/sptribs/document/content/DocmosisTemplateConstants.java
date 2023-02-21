package uk.gov.hmcts.sptribs.document.content;

import java.time.format.DateTimeFormatter;

public final class DocmosisTemplateConstants {
    public static final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd MMM yyyy");

    public static final String COMMA_SPACE = ", ";

    public static final String CIC_CASE_SCHEME = "cicCaseSchemeCic";
    public static final String SUBJECT_FULL_NAME = "SubjectRepFullName";
    public static final String CASE_NUMBER = "cicCaseCaseNumber";
    public static final String DATED = "DateDecisionDocCreated";
    public static final String HEARING_TYPE = "venueOrHearingType";
    public static final String TRIBUNAL_MEMBERS = "tribunalMembers";
    public static final String DECISION_SIGNATURE = "decisionSignature";
    public static final String ORDER_SIGNATURE = "orderSignature";
    public static final String MAIN_CONTENT = "MainBody";
    public static final String REPRESENTATIVE_FULL_NAME = "representativeFullName";
    public static final String HEARING_VENUE_NAME = "recordHearingVenueName";
    public static final String HEARING_DATE = "HearingDate";
    public static final String HEARING_TIME = "HearingTime";

    public static final String ELIGIBILITY_MAIN_CONTENT = "1.     The appeal against the CICA’s Review decision dated…..under paragraph….."
        + " is successful. Mr/Mrs …… is eligible for compensation. The CICA must now consider if any compensation should be awarded.\n"
        + "2.     Mr/ Mrs …….application is now returned to the CICA for further decision making and action.\n"
        + "3.     Summary of reasons for the decision";

    public static final String QUANTUM_MAIN_CONTENT = "1.    The appeal is allowed.\n"
        + "2.     The appeal against the CICA's Review Decision dated........under paragraphs 32&33 is successful.\n"
        + "3.     Mr/Ms B is entitled to a total award of…………  Details of this award are set out in the calculation overleaf.\n"
        + "4.     Summary of reasons for decision";

    public static final String RULE27_MAIN_CONTENT = "1.     Rule 27 (4) provides that the Tribunal may make a decision which disposes of"
        + " proceedings without a hearing.\n"
        + "2.     The Tribunal has decided to make a decision without a hearing because\n"
        + "3.     The appeal is\n"
        + "4.     Decision;\n"
        + "5.     The Tribunal made the following findings of facts\n"
        + "6. Any party may make a written application to the Tribunal for the decision to be reconsider at a hearing. Any application "
        + "must be received within one month after the date on which the Tribunal sent notice of the decision to the party making the "
        + "application.";

    public static final String ME_DMI_MAIN_CONTENT =
        "(Directions for DMI/ Psychological/Psychiatric Report – please specify which expert)\n"
            + "1.     I have reviewed the available evidence and consider that it is necessary for .. to be assessed "
            + "by a Psychiatrist or Clinical Psychologist. \n"
            + "2.     The CICA is directed to commission an assessment by a Psychiatrist or Clinical Psychologist. Up to date GP records "
            + "should be sent to the expert, along with a copy of the questions overleaf. CICA is requested to provide an update on "
            + "progress, if thereport has not been completed within 4 months of issue of this notice.\n"
            + "3.     To .. >: Please could you ask your GP for a copy of all your records up to the present day. You "
            + "are entitled to a copy of these without charge and it is likely to avoid delay if you obtain them yourself. The records can"
            + " be in digital form and emailed to CICA. Please do this as soon as you can. (If you are unable to do this, it may be "
            + "possible for the CICA to obtain the GP records with your consent although the cost of this maybe deducted from any award."
            + " Please write to the Tribunal if you need help with this or do not agree to all your records being disclosed).\n"
            + "4. Clerk: Please refer back to any Judge on receipt of report, the update or after 3 months.\n"
            + "A party is entitled to challenge any direction given by applying within a month for another direction which amends, "
            + "suspends or sets aside the first direction.\n\n"
            + "Expert Questions (detail to be completed by Tribunal)"
            + "1.     Has the appellant suffered a mental injury or illness which is directly attributable to the incident on ....... By "
            + "‘directly attributable’ we mean that the incident must have been a substantial or significant cause of the "
            + "injury/illness.\n"
            + "2.     If no, please give reasons.\n"
            + "3.     If yes, what is the diagnosis? Please refer to the clinical features and recognised diagnostic systems, such as "
            + "ICD-10, DSM-V or other diagnostic method.\n"
            + "4.     What are the symptoms of the mental illness/injury?\n"
            + "5.     What effect have the symptoms of the mental illness/injury had on the Appellant's day to day activities? Please "
            + "consider ability to work, manage social and domestic activities and sexual or other relevant function. \n"
            + "6.     To what extent has the mental illness/injury affected the Appellant’s capacity to undertake paid employment?\n"
            + "7.     If the symptoms due to the incident are no longer present, for how long did they last? \n"
            + "8.     What treatment or rehabilitation has the Appellant had?\n"
            + "9.     What future treatment or rehabilitative measures are recommended? \n"
            + "10.     What is the nature of such treatment, the length of time it will take and the probable outcome?\n"
            + "11.    If the Appellant undergoes treatment, how long are the symptoms likely to last?\n"
            + "12.    Are any symptoms likely to be permanent? If yes, which symptoms and what is their effect on the Appellant’s day to"
            + " day activities?\n"
            + "13.    Has the mental illness/injury exacerbated or accelerated a pre-existing condition? \n"
            + "14.    If yes, give details of the pre-existing condition, and the extent (in percentage terms if possible) to which the "
            + "injury has exacerbated or accelerated the pre-existing condition?\n"
            + "15.    Have any other life events played a part in the reduction in functional capacity either before or since the "
            + "incident? If so, please give details.\n"
            + "16. Identify and comment on any other issues that you consider to be relevant.";

    public static final String ME_JOINT_MAIN_CONTENT = "(Where parties should liaise for medical evidence progression)\n"
        + "This appeal file has been referred to me for directions.\n"
        + "I have read the appeal papers including the available medical evidence and have decided that further expert medical evidence is"
        + " required to enable the tribunal to properly consider the application. In the circumstances it is reasonable for the CICA to "
        + "meet the cost of obtaining a report from a psychiatrist or clinical psychologist.\n"
        + "I remind the parties that they have a duty to cooperate with the tribunal and to avoid any unnecessary delay.\n"
        + "Directions\n"
        + "1. The CICA and Mr/Mrs …..shall liaise together to make any necessary and reasonable adjustments to the respondent’s standard "
        + "letter of instruction.\n"
        + "2. I expect the parties to be able to adjust the letter of instruction and to obtain all of the documents, records and other "
        + "evidence which the instructed expert requires to examine promptly and without specific direction from the tribunal but in the "
        + "event that they are unable to agree either party may return to the tribunal for specific directions.\n"
        + "3.I expect that a promptly instructed expert will be able to prepare a report within 4 months from the date of issue of this "
        + "notice and I direct the respondent to send a copy of the report to the tribunal immediately on receipt.\n"
        + "4. I direct the clerk to re-refer this file to me for further directions immediately on receipt of the expert report and in any "
        + "event after 4 months.";

    public static final String STRIKE_OUT_WARNING_MAIN_CONTENT = "(Delete as appropriate)\n"
        + "Jurisdiction\n"
        + "The CICA has requested / The Tribunal needs to consider if/that the appeal should be/is struck out because the Tribunal has no "
        + "power to deal with it. This is because..\n"
        + "If it is failure to comply\n"
        + "On 7/12/2021 the Tribunal gave directions that... The deadline has now passed and there has been no contact from...\n"
        + "To Mr/s Bloggs: You are now warned that your appeal may be struck out. This means the Tribunal will take no further action and"
        + " the CICA's decision will be final. You must comply with the Directions or write to the Tribunal to say why your appeal should "
        + "not be struck out. Please do this within 2 weeks/1 month of issue of this notice.\n"
        + "If you fail to comply with these Directions the appeal may/will be struck out.\n"
        + "To the Clerk:\n"
        + "Please refer back to a Judge if Mr/s Bloggs contacts the Tribunal or after 2 weeks/1 month.\"\n"
        + "A party is entitled to challenge any direction given by applying within a month for another direction which amends, suspends or "
        + "sets aside the first direction";

    public static final String STRIKE_OUT_NOTICE_MAIN_CONTENT =
        "On 7/12/2021 the Tribunal issued Directions for Mr/s Bloggs and there has been no reply.\n"
            + "There has been a failure to comply with the Directions and it is fair to strike out the appeal under Rule ……..\n"
            + "The appeal is now struck out, which means that the Tribunal will take no further action.\n"
            + "It is possible to apply for this decision to be set aside and the appeal re-instated. This application must be in writing "
            + "and received by the Tribunal within a month of issue of this notice.";

    public static final String PRO_FORMA_MAIN_CONTENT =
        "<b>In accordance with rule 16 of the Tribunal Procedure (First-tier Tribunal) (Social Entitlement Chamber) Rules 2008 and upon"
            + " the application of\n"
            + "*To *(Name and Address)\n"
            + "*You are summoned to attend the hearing of the appeal in these proceedings\n"
            + "*(AND/OR) You are ordered to produce the following documents:-</b>\n\n"
            + "Where the person being summoned is not a party to the proceedings, then that person’s necessary expenses of attending shall"
            + " be paid or tendered to them by the Tribunals Service\n"
            + "The person on whom the requirement stated above is imposed may apply to the Tribunal to vary or set aside the summons or"
            + " order if they have not had an opportunity to object to it.\n"
            + "<b>Failure to comply with this summons or order may lead to the matter being referred to the Upper Tribunal in accordance"
            + " with rule 7(3) of the Rules and section 25 of the Tribunals, Courts and Enforcement Act 2007</b>";

    private DocmosisTemplateConstants() {
    }
}
