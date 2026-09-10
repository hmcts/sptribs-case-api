package uk.gov.hmcts.sptribs.send35.event;

import org.springframework.stereotype.Component;
import uk.gov.hmcts.ccd.sdk.api.CCDConfig;
import uk.gov.hmcts.ccd.sdk.api.ConfigBuilder;
import uk.gov.hmcts.sptribs.ciccase.model.UserRole;
import uk.gov.hmcts.sptribs.send35.model.StSend35AdditionalParents;
import uk.gov.hmcts.sptribs.send35.model.StSend35Advocate;
import uk.gov.hmcts.sptribs.send35.model.StSend35Appellant;
import uk.gov.hmcts.sptribs.send35.model.StSend35CaseData;
import uk.gov.hmcts.sptribs.send35.model.StSend35ChildOrYoungPerson;
import uk.gov.hmcts.sptribs.send35.model.StSend35Communication;
import uk.gov.hmcts.sptribs.send35.model.StSend35Declaration;
import uk.gov.hmcts.sptribs.send35.model.StSend35HealthAndSocialCare;
import uk.gov.hmcts.sptribs.send35.model.StSend35HearingPreferences;
import uk.gov.hmcts.sptribs.send35.model.StSend35Mediation;
import uk.gov.hmcts.sptribs.send35.model.StSend35OtherCases;
import uk.gov.hmcts.sptribs.send35.model.StSend35ParentalResponsibility;
import uk.gov.hmcts.sptribs.send35.model.StSend35Reasons;
import uk.gov.hmcts.sptribs.send35.model.StSend35Representative;
import uk.gov.hmcts.sptribs.send35.model.StSend35SchoolOrProvider;
import uk.gov.hmcts.sptribs.send35.model.StSend35State;
import uk.gov.hmcts.sptribs.send35.model.StSend35Support;
import uk.gov.hmcts.sptribs.send35.model.StSend35Timeliness;
import uk.gov.hmcts.sptribs.send35.model.StSend35TypeOfAppeal;

import static uk.gov.hmcts.sptribs.ciccase.model.UserRole.CITIZEN;
import static uk.gov.hmcts.sptribs.ciccase.model.UserRole.CREATOR;
import static uk.gov.hmcts.sptribs.ciccase.model.UserRole.ST_CIC_CASEWORKER;
import static uk.gov.hmcts.sptribs.ciccase.model.UserRole.ST_CIC_HEARING_CENTRE_ADMIN;
import static uk.gov.hmcts.sptribs.ciccase.model.UserRole.ST_CIC_SENIOR_CASEWORKER;
import static uk.gov.hmcts.sptribs.ciccase.model.UserRole.SUPER_USER;
import static uk.gov.hmcts.sptribs.ciccase.model.access.Permissions.CREATE_READ_UPDATE;

/**
 * {@code create-case} — creates a SEND35 appeal in {@code Submitted}.
 *
 * <p>This is the event the citizen prototype calls. It is granted to {@code CITIZEN} because
 * the appeal is submitted by the appellant, not keyed in by a caseworker; the caseworker
 * roles are granted too so the same event is usable from XUI when testing.
 *
 * <p>The pages exist so the event is usable in XUI. They are not the citizen journey — that
 * lives in {@code hmcts/sptribs-send-prototype}, which posts the whole appeal in one call.
 * Mandatory here matches what the prototype guarantees to send, which is why questions that
 * only apply on some routes are declared optional with a show condition rather than
 * mandatory.
 */
@Component
public class StSend35CreateCase implements CCDConfig<StSend35CaseData, StSend35State, UserRole> {

    private static final String YES = "=\"Yes\"";

    @Override
    public void configure(final ConfigBuilder<StSend35CaseData, StSend35State, UserRole> configBuilder) {
        configBuilder.event("create-case")
            .initialState(StSend35State.Submitted)
            .name("Appeal: Create appeal")
            .description("Create a SEND35 appeal from the citizen form")
            .showSummary()
            .grant(
                CREATE_READ_UPDATE,
                SUPER_USER,
                ST_CIC_CASEWORKER,
                ST_CIC_SENIOR_CASEWORKER,
                ST_CIC_HEARING_CENTRE_ADMIN,
                CITIZEN,
                CREATOR)
            .fields()
            .page("childOrYoungPersonPage")
                .pageLabel("Section 1 — Child or young person's information")
                .complex(StSend35CaseData::getChildOrYoungPerson)
                    .mandatory(StSend35ChildOrYoungPerson::getFirstName)
                    .mandatory(StSend35ChildOrYoungPerson::getLastName)
                    .mandatory(StSend35ChildOrYoungPerson::getDateOfBirth)
                    .mandatory(StSend35ChildOrYoungPerson::getGender)
                    .done()
            .page("appellantPage")
                .pageLabel("Section 2 — Details of the person appealing")
                .complex(StSend35CaseData::getAppellant)
                    .mandatory(StSend35Appellant::getWhoIsAppealing)
                    .mandatory(StSend35Appellant::getFirstName)
                    .mandatory(StSend35Appellant::getLastName)
                    .mandatory(StSend35Appellant::getRelationship)
                    .mandatory(StSend35Appellant::getPhoneNumber)
                    .mandatory(StSend35Appellant::getEmailAddress)
                    .mandatory(StSend35Appellant::getAddress)
                    .done()
            .page("additionalParentsPage")
                .pageLabel("Section 3 — Details of another parent or carer")
                .complex(StSend35CaseData::getAdditionalParents)
                    .mandatory(StSend35AdditionalParents::getAddParentOrCarer)
                    .optional(StSend35AdditionalParents::getFirstName, "parentsAddParentOrCarer" + YES)
                    .optional(StSend35AdditionalParents::getLastName, "parentsAddParentOrCarer" + YES)
                    .optional(StSend35AdditionalParents::getPhoneNumber, "parentsAddParentOrCarer" + YES)
                    .optional(StSend35AdditionalParents::getEmailAddress, "parentsAddParentOrCarer" + YES)
                    .optional(StSend35AdditionalParents::getRelationship, "parentsAddParentOrCarer" + YES)
                    .optional(StSend35AdditionalParents::getAddSecondParentOrCarer, "parentsAddParentOrCarer" + YES)
                    .optional(StSend35AdditionalParents::getSecondFirstName, "parentsAddSecondParentOrCarer" + YES)
                    .optional(StSend35AdditionalParents::getSecondLastName, "parentsAddSecondParentOrCarer" + YES)
                    .optional(StSend35AdditionalParents::getSecondPhoneNumber, "parentsAddSecondParentOrCarer" + YES)
                    .optional(StSend35AdditionalParents::getSecondEmailAddress, "parentsAddSecondParentOrCarer" + YES)
                    .optional(StSend35AdditionalParents::getSecondRelationship, "parentsAddSecondParentOrCarer" + YES)
                    .done()
            .page("representativePage")
                .pageLabel("Section 4 — Representative's information")
                .complex(StSend35CaseData::getRepresentative)
                    .mandatory(StSend35Representative::getHasRepresentative)
                    .optional(StSend35Representative::getFirstName, "repHasRepresentative" + YES)
                    .optional(StSend35Representative::getLastName, "repHasRepresentative" + YES)
                    .optional(StSend35Representative::getCompanyName, "repHasRepresentative" + YES)
                    .optional(StSend35Representative::getPhoneNumber, "repHasRepresentative" + YES)
                    .optional(StSend35Representative::getEmailAddress, "repHasRepresentative" + YES)
                    .done()
            .page("advocatePage")
                .pageLabel("Section 5 — Advocate information")
                .complex(StSend35CaseData::getAdvocate)
                    .mandatory(StSend35Advocate::getHasAdvocate)
                    .optional(StSend35Advocate::getFirstName, "advHasAdvocate" + YES)
                    .optional(StSend35Advocate::getLastName, "advHasAdvocate" + YES)
                    .optional(StSend35Advocate::getPhoneNumber, "advHasAdvocate" + YES)
                    .optional(StSend35Advocate::getEmailAddress, "advHasAdvocate" + YES)
                    .done()
            .page("communicationPage")
                .pageLabel("Section 6 — Communication during the appeal")
                .complex(StSend35CaseData::getCommunication)
                    .mandatory(StSend35Communication::getRecipient)
                    .mandatory(StSend35Communication::getFirstName)
                    .mandatory(StSend35Communication::getLastName)
                    .optional(StSend35Communication::getMobileNumber)
                    .mandatory(StSend35Communication::getEmailAddress)
                    .mandatory(StSend35Communication::getAddress)
                    .done()
            .page("parentalResponsibilityPage")
                .pageLabel("Section 7 — Anyone else with parental responsibility")
                .complex(StSend35CaseData::getParentalResponsibility)
                    .mandatory(StSend35ParentalResponsibility::getOtherPersonOrOrganisation)
                    .optional(StSend35ParentalResponsibility::getName, "prOtherPersonOrOrganisation" + YES)
                    .optional(StSend35ParentalResponsibility::getTold, "prOtherPersonOrOrganisation" + YES)
                    .optional(StSend35ParentalResponsibility::getReasonNotTold, "prTold=\"No\"")
                    .done()
            .page("typeOfAppealPage")
                .pageLabel("Section 8 — Type of appeal")
                .complex(StSend35CaseData::getTypeOfAppeal)
                    .mandatory(StSend35TypeOfAppeal::getAppealAbout)
                    .optional(StSend35TypeOfAppeal::getFollowingAnnualReview)
                    .optional(StSend35TypeOfAppeal::getPlanSections)
                    .done()
            .page("schoolOrProviderPage")
                .pageLabel("Section 9 — School, college or education provider")
                .complex(StSend35CaseData::getSchoolOrProvider)
                    .optional(StSend35SchoolOrProvider::getSectionIDisagreement)
                    .optional(StSend35SchoolOrProvider::getAskedForProvider)
                    .optional(StSend35SchoolOrProvider::getTypeOfProvider, "schoolAskedForProvider=\"No\"")
                    .optional(StSend35SchoolOrProvider::getProviderName, "schoolAskedForProvider" + YES)
                    .optional(StSend35SchoolOrProvider::getProviderAddress, "schoolAskedForProvider" + YES)
                    .optional(StSend35SchoolOrProvider::getDateContacted, "schoolAskedForProvider" + YES)
                    .optional(StSend35SchoolOrProvider::getProviderResponse, "schoolAskedForProvider" + YES)
                    .done()
            .page("reasonsPage")
                .pageLabel("Section 10 — Reasons for the appeal")
                .complex(StSend35CaseData::getReasons)
                    .mandatory(StSend35Reasons::getAppealReasons)
                    .done()
            .page("healthAndSocialCarePage")
                .pageLabel("Section 11 — Health and social care")
                .complex(StSend35CaseData::getHealthAndSocialCare)
                    .mandatory(StSend35HealthAndSocialCare::getWantRecommendation)
                    .optional(StSend35HealthAndSocialCare::getRecommendationTypes, "hscWantRecommendation" + YES)
                    .optional(StSend35HealthAndSocialCare::getHealthIssues, "hscWantRecommendation" + YES)
                    .optional(StSend35HealthAndSocialCare::getHealthRecommendations, "hscWantRecommendation" + YES)
                    .optional(StSend35HealthAndSocialCare::getSocialCareIssues, "hscWantRecommendation" + YES)
                    .optional(StSend35HealthAndSocialCare::getSocialCareRecommendations, "hscWantRecommendation" + YES)
                    .done()
            .page("mediationPage")
                .pageLabel("Section 12 — Mediation certificate")
                .complex(StSend35CaseData::getMediation)
                    .mandatory(StSend35Mediation::getHasCertificate)
                    .optional(StSend35Mediation::getNoCertificateReason, "medHasCertificate=\"No\"")
                    .optional(StSend35Mediation::getNoCertificateExplanation, "medNoCertificateReason=\"otherReason\"")
                    .done()
            .page("timelinessPage")
                .pageLabel("Section 13 — Appealing on time")
                .complex(StSend35CaseData::getTimeliness)
                    .mandatory(StSend35Timeliness::getDecisionLetterDate)
                    .optional(StSend35Timeliness::getMediationCertificateDate)
                    .optional(StSend35Timeliness::getLateAppealExplanation)
                    .done()
            .page("otherCasesPage")
                .pageLabel("Sections 14 and 15 — Other cases")
                .complex(StSend35CaseData::getOtherCases)
                    .mandatory(StSend35OtherCases::getOtherSendAppeals)
                    .optional(StSend35OtherCases::getAppealReferenceNumbers, "otherOtherSendAppeals" + YES)
                    .mandatory(StSend35OtherCases::getOtherCourtCases)
                    .optional(StSend35OtherCases::getCourtCaseDetails, "otherOtherCourtCases" + YES)
                    .done()
            .page("hearingPreferencesPage")
                .pageLabel("Section 16 — Hearing preferences")
                .complex(StSend35CaseData::getHearingPreferences)
                    .mandatory(StSend35HearingPreferences::getPreferredType)
                    .mandatory(StSend35HearingPreferences::getWantEarlierHearing)
                    .mandatory(StSend35HearingPreferences::getCanAttendByVideo)
                    .optional(StSend35HearingPreferences::getCannotAttendByVideoReason, "hearingCanAttendByVideo=\"No\"")
                    .done()
            .page("supportPage")
                .pageLabel("Section 17 — Support during your case")
                .complex(StSend35CaseData::getSupport)
                    .mandatory(StSend35Support::getNeedsInterpreter)
                    .optional(StSend35Support::getLanguages, "supportNeedsInterpreter" + YES)
                    .mandatory(StSend35Support::getNeedsAdjustments)
                    .optional(StSend35Support::getAdjustmentsDetail, "supportNeedsAdjustments" + YES)
                    .done()
            .page("supportingEvidencePage")
                .pageLabel("Supporting evidence")
                .optional(StSend35CaseData::getSupportingEvidence)
            .page("declarationPage")
                .pageLabel("Declaration")
                .complex(StSend35CaseData::getDeclaration)
                    .mandatory(StSend35Declaration::getCapacity)
                    .mandatory(StSend35Declaration::getSignatoryRole)
                    .mandatory(StSend35Declaration::getFullName)
                    .mandatory(StSend35Declaration::getSignature)
                    .mandatory(StSend35Declaration::getDateSigned)
                    .done()
            .done();
    }
}
