package uk.gov.hmcts.sptribs.wpaf1.event;

import org.springframework.stereotype.Component;
import uk.gov.hmcts.ccd.sdk.api.CCDConfig;
import uk.gov.hmcts.ccd.sdk.api.ConfigBuilder;
import uk.gov.hmcts.sptribs.ciccase.model.UserRole;
import uk.gov.hmcts.sptribs.wpaf1.model.StWpaf1Appellant;
import uk.gov.hmcts.sptribs.wpaf1.model.StWpaf1Appointee;
import uk.gov.hmcts.sptribs.wpaf1.model.StWpaf1CaseData;
import uk.gov.hmcts.sptribs.wpaf1.model.StWpaf1Decision;
import uk.gov.hmcts.sptribs.wpaf1.model.StWpaf1Declaration;
import uk.gov.hmcts.sptribs.wpaf1.model.StWpaf1Reasons;
import uk.gov.hmcts.sptribs.wpaf1.model.StWpaf1Reconsideration;
import uk.gov.hmcts.sptribs.wpaf1.model.StWpaf1Representative;
import uk.gov.hmcts.sptribs.wpaf1.model.StWpaf1State;
import uk.gov.hmcts.sptribs.wpaf1.model.StWpaf1Support;
import uk.gov.hmcts.sptribs.wpaf1.model.StWpaf1Timeliness;
import uk.gov.hmcts.sptribs.wpaf1.model.StWpaf1Uksf;

import static uk.gov.hmcts.sptribs.ciccase.model.UserRole.CITIZEN;
import static uk.gov.hmcts.sptribs.ciccase.model.UserRole.CREATOR;
import static uk.gov.hmcts.sptribs.ciccase.model.UserRole.ST_CIC_CASEWORKER;
import static uk.gov.hmcts.sptribs.ciccase.model.UserRole.ST_CIC_HEARING_CENTRE_ADMIN;
import static uk.gov.hmcts.sptribs.ciccase.model.UserRole.ST_CIC_SENIOR_CASEWORKER;
import static uk.gov.hmcts.sptribs.ciccase.model.UserRole.SUPER_USER;
import static uk.gov.hmcts.sptribs.ciccase.model.access.Permissions.CREATE_READ_UPDATE;

/**
 * {@code create-case} — creates a WPAF1 appeal in {@code Submitted}.
 *
 * <p>This is the event the citizen prototype calls. It is granted to {@code CITIZEN} because
 * the appeal is submitted by the appellant, not keyed in by a caseworker; the caseworker
 * roles are granted too so the same event is usable from XUI when testing.
 *
 * <p>The pages exist so the event is usable in XUI. They are not the citizen journey — that
 * lives in {@code hmcts/sptribs-wpaf1-prototype}, which posts the whole appeal in one call.
 * Mandatory here matches what the prototype guarantees to send, which is why questions that
 * only apply on some routes are declared optional with a show condition rather than
 * mandatory.
 */
@Component
public class StWpaf1CreateCase implements CCDConfig<StWpaf1CaseData, StWpaf1State, UserRole> {

    private static final String YES = "=\"Yes\"";
    private static final String HAS_REPRESENTATIVE = "repHasRepresentative" + YES;
    private static final String HAS_APPOINTEE = "aptHasAppointee" + YES;

    @Override
    public void configure(final ConfigBuilder<StWpaf1CaseData, StWpaf1State, UserRole> configBuilder) {
        configBuilder.event("create-case")
            .initialState(StWpaf1State.Submitted)
            .name("Appeal: Create appeal")
            .description("Create a WPAF1 appeal from the citizen form")
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
            .page("appellantPage")
                .pageLabel("Section 1 — Appellant's information")
                .complex(StWpaf1CaseData::getAppellant)
                    .mandatory(StWpaf1Appellant::getLivesIn)
                    .optional(StWpaf1Appellant::getTitle)
                    .mandatory(StWpaf1Appellant::getFirstName)
                    .mandatory(StWpaf1Appellant::getLastName)
                    .mandatory(StWpaf1Appellant::getDateOfBirth)
                    .mandatory(StWpaf1Appellant::getNationalInsuranceNumber)
                    .optional(StWpaf1Appellant::getAfcsMemberNumber)
                    .mandatory(StWpaf1Appellant::getAddress)
                    .optional(StWpaf1Appellant::getPhoneNumber)
                    .optional(StWpaf1Appellant::getMobileNumber)
                    .optional(StWpaf1Appellant::getEmailAddress)
                    .mandatory(StWpaf1Appellant::getServiceStatus)
                    .optional(StWpaf1Appellant::getProposedDischargeDate, "appServiceStatus=\"stillServing\"")
                    .done()
            .page("representativePage")
                .pageLabel("Section 2 — Appellant's representative")
                .complex(StWpaf1CaseData::getRepresentative)
                    .mandatory(StWpaf1Representative::getHasRepresentative)
                    .optional(StWpaf1Representative::getFirstName, HAS_REPRESENTATIVE)
                    .optional(StWpaf1Representative::getLastName, HAS_REPRESENTATIVE)
                    .optional(StWpaf1Representative::getOrganisation, HAS_REPRESENTATIVE)
                    .optional(StWpaf1Representative::getAddress, HAS_REPRESENTATIVE)
                    .optional(StWpaf1Representative::getDxNumber, HAS_REPRESENTATIVE)
                    .optional(StWpaf1Representative::getReferenceNumber, HAS_REPRESENTATIVE)
                    .optional(StWpaf1Representative::getPhoneNumber, HAS_REPRESENTATIVE)
                    .optional(StWpaf1Representative::getMobileNumber, HAS_REPRESENTATIVE)
                    .optional(StWpaf1Representative::getEmailAddress, HAS_REPRESENTATIVE)
                    .done()
            .page("appointeePage")
                .pageLabel("Section 3 — Appellant's appointee")
                .complex(StWpaf1CaseData::getAppointee)
                    .mandatory(StWpaf1Appointee::getHasAppointee)
                    .optional(StWpaf1Appointee::getFirstName, HAS_APPOINTEE)
                    .optional(StWpaf1Appointee::getLastName, HAS_APPOINTEE)
                    .optional(StWpaf1Appointee::getOrganisation, HAS_APPOINTEE)
                    .optional(StWpaf1Appointee::getAddress, HAS_APPOINTEE)
                    .optional(StWpaf1Appointee::getPhoneNumber, HAS_APPOINTEE)
                    .optional(StWpaf1Appointee::getMobileNumber, HAS_APPOINTEE)
                    .optional(StWpaf1Appointee::getEmailAddress, HAS_APPOINTEE)
                    .optional(StWpaf1Appointee::getSigningGround, HAS_APPOINTEE)
                    .done()
            .page("decisionPage")
                .pageLabel("Section 4 — Decision appealed against")
                .complex(StWpaf1CaseData::getDecision)
                    .mandatory(StWpaf1Decision::getScheme)
                    .mandatory(StWpaf1Decision::getDecisionLetterDate)
                    .mandatory(StWpaf1Decision::getAppealAbout)
                    .optional(StWpaf1Decision::getAppealAboutOther)
                    .done()
            .page("reasonsPage")
                .pageLabel("Section 5 — Reasons for appealing")
                .complex(StWpaf1CaseData::getReasons)
                    .mandatory(StWpaf1Reasons::getAppealReasons)
                    .done()
            .page("reconsiderationPage")
                .pageLabel("Section 6 — AFCS reconsideration")
                .complex(StWpaf1CaseData::getReconsideration)
                    // War Pensions Scheme appellants are never asked Section 6, so this
                    // cannot be mandatory even though an AFCS appeal always answers it.
                    .optional(StWpaf1Reconsideration::getAskedForReconsideration, "dsnScheme=\"afcs\"")
                    .done()
            .page("timelinessPage")
                .pageLabel("Section 7 — Late appeals")
                .complex(StWpaf1CaseData::getTimeliness)
                    .optional(StWpaf1Timeliness::getLateAppealExplanation)
                    .done()
            .page("supportPage")
                .pageLabel("Section 8 — Reasonable adjustments")
                .complex(StWpaf1CaseData::getSupport)
                    .mandatory(StWpaf1Support::getNeedsSupport)
                    .optional(StWpaf1Support::getSupportDetails, "supNeedsSupport" + YES)
                    .done()
            .page("uksfPage")
                .pageLabel("UK Special Forces confidentiality")
                .complex(StWpaf1CaseData::getUksf)
                    .mandatory(StWpaf1Uksf::getServedWithUksf)
                    .optional(StWpaf1Uksf::getDisclosureCellConfirmed, "uksfServedWithUksf" + YES)
                    .done()
            .page("supportingEvidencePage")
                .pageLabel("Additional evidence")
                .optional(StWpaf1CaseData::getSupportingEvidence)
            .page("declarationPage")
                .pageLabel("Declaration")
                .complex(StWpaf1CaseData::getDeclaration)
                    .mandatory(StWpaf1Declaration::getCapacity)
                    .mandatory(StWpaf1Declaration::getSignatoryRole)
                    .mandatory(StWpaf1Declaration::getFullName)
                    .mandatory(StWpaf1Declaration::getSignature)
                    .mandatory(StWpaf1Declaration::getDateSigned)
                    .done()
            .done();
    }
}
