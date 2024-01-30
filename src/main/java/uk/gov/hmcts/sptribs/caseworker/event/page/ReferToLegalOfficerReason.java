package uk.gov.hmcts.sptribs.caseworker.event.page;

import uk.gov.hmcts.ccd.sdk.api.CaseDetails;
import uk.gov.hmcts.ccd.sdk.api.callback.AboutToStartOrSubmitResponse;
import uk.gov.hmcts.sptribs.caseworker.model.ReferToLegalOfficer;
import uk.gov.hmcts.sptribs.caseworker.model.ReferralReason;
import uk.gov.hmcts.sptribs.ciccase.model.CaseData;
import uk.gov.hmcts.sptribs.ciccase.model.State;
import uk.gov.hmcts.sptribs.common.ccd.CcdPageConfiguration;
import uk.gov.hmcts.sptribs.common.ccd.PageBuilder;

import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static java.util.EnumSet.allOf;
import static java.util.EnumSet.complementOf;
import static java.util.EnumSet.of;
import static org.apache.commons.collections4.CollectionUtils.emptyIfNull;
import static uk.gov.hmcts.sptribs.caseworker.model.ReferralReason.CORRECTIONS;
import static uk.gov.hmcts.sptribs.caseworker.model.ReferralReason.LISTED_CASE;
import static uk.gov.hmcts.sptribs.caseworker.model.ReferralReason.LISTED_CASE_WITHIN_5_DAYS;
import static uk.gov.hmcts.sptribs.caseworker.model.ReferralReason.LISTING_DIRECTIONS;
import static uk.gov.hmcts.sptribs.caseworker.model.ReferralReason.NEW_CASE;
import static uk.gov.hmcts.sptribs.caseworker.model.ReferralReason.OTHER;
import static uk.gov.hmcts.sptribs.caseworker.model.ReferralReason.POSTPONEMENT_REQUEST;
import static uk.gov.hmcts.sptribs.caseworker.model.ReferralReason.REINSTATEMENT_REQUEST;
import static uk.gov.hmcts.sptribs.caseworker.model.ReferralReason.RULE_27_REQUEST;
import static uk.gov.hmcts.sptribs.caseworker.model.ReferralReason.SET_ASIDE_REQUEST;
import static uk.gov.hmcts.sptribs.caseworker.model.ReferralReason.STAY_REQUEST;
import static uk.gov.hmcts.sptribs.caseworker.model.ReferralReason.STRIKE_OUT_REQUEST;
import static uk.gov.hmcts.sptribs.caseworker.model.ReferralReason.TIME_EXTENSION_REQUEST;
import static uk.gov.hmcts.sptribs.caseworker.model.ReferralReason.WITHDRAWAL_REQUEST;
import static uk.gov.hmcts.sptribs.caseworker.model.ReferralReason.WRITTEN_REASONS_REQUEST;
import static uk.gov.hmcts.sptribs.caseworker.util.ErrorConstants.INCOMPATIBLE_REFERRAL_REASON;
import static uk.gov.hmcts.sptribs.ciccase.model.State.AwaitingHearing;
import static uk.gov.hmcts.sptribs.ciccase.model.State.CaseClosed;
import static uk.gov.hmcts.sptribs.ciccase.model.State.CaseManagement;
import static uk.gov.hmcts.sptribs.ciccase.model.State.Submitted;



public class ReferToLegalOfficerReason implements CcdPageConfiguration {

    private final Map<ReferralReason, Set<State>> permittedStatesByReason =
        Map.ofEntries(
            new AbstractMap.SimpleEntry<>(CORRECTIONS, of(CaseClosed)),
            new AbstractMap.SimpleEntry<>(LISTED_CASE, of(AwaitingHearing)),
            new AbstractMap.SimpleEntry<>(LISTED_CASE_WITHIN_5_DAYS, of(AwaitingHearing)),
            new AbstractMap.SimpleEntry<>(LISTING_DIRECTIONS, complementOf(of(AwaitingHearing))),
            new AbstractMap.SimpleEntry<>(NEW_CASE, of(Submitted, CaseManagement)),
            new AbstractMap.SimpleEntry<>(POSTPONEMENT_REQUEST, of(AwaitingHearing)),
            new AbstractMap.SimpleEntry<>(REINSTATEMENT_REQUEST, of(CaseClosed)),
            new AbstractMap.SimpleEntry<>(RULE_27_REQUEST, complementOf(of(CaseClosed))),
            new AbstractMap.SimpleEntry<>(SET_ASIDE_REQUEST, of(CaseClosed)),
            new AbstractMap.SimpleEntry<>(STAY_REQUEST, complementOf(of(CaseClosed))),
            new AbstractMap.SimpleEntry<>(STRIKE_OUT_REQUEST, complementOf(of(CaseClosed))),
            new AbstractMap.SimpleEntry<>(TIME_EXTENSION_REQUEST, complementOf(of(CaseClosed))),
            new AbstractMap.SimpleEntry<>(WITHDRAWAL_REQUEST, complementOf(of(CaseClosed))),
            new AbstractMap.SimpleEntry<>(WRITTEN_REASONS_REQUEST, of(CaseClosed)),
            new AbstractMap.SimpleEntry<>(OTHER, allOf(State.class))
        );


    @Override
    public void addTo(PageBuilder pageBuilder) {

        pageBuilder.page("referToLegalOfficer", this::midEvent)
            .pageLabel("Referral reasons")
            .complex(CaseData::getReferToLegalOfficer)
            .mandatory(ReferToLegalOfficer::getReferralReason)
            .mandatory(ReferToLegalOfficer::getReasonForReferral, "referToLegalOfficerReferralReason = \"other\"")
            .done();


    }

    public AboutToStartOrSubmitResponse<CaseData, State> midEvent(CaseDetails<CaseData, State> details,
                                                                  CaseDetails<CaseData, State> detailsBefore) {
        final CaseData data = details.getData();
        final List<String> errors = new ArrayList<>();
        final State caseState = details.getState();


        if (!emptyIfNull(permittedStatesByReason.get(data.getReferToLegalOfficer().getReferralReason())).contains(caseState)) {
            errors.add(INCOMPATIBLE_REFERRAL_REASON);
        }
        return AboutToStartOrSubmitResponse.<CaseData, State>builder()
            .data(data)
            .errors(errors)
            .build();
    }


}
