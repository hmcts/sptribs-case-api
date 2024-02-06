package uk.gov.hmcts.sptribs.caseworker.event.page;

import uk.gov.hmcts.ccd.sdk.api.CaseDetails;
import uk.gov.hmcts.ccd.sdk.api.callback.AboutToStartOrSubmitResponse;
import uk.gov.hmcts.sptribs.caseworker.model.ReferToJudge;
import uk.gov.hmcts.sptribs.caseworker.model.ReferralReason;
import uk.gov.hmcts.sptribs.ciccase.model.CaseData;
import uk.gov.hmcts.sptribs.ciccase.model.State;
import uk.gov.hmcts.sptribs.common.ccd.CcdPageConfiguration;
import uk.gov.hmcts.sptribs.common.ccd.PageBuilder;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static java.util.AbstractMap.SimpleEntry;
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


public class ReferToJudgeReason implements CcdPageConfiguration {

    private final Map<ReferralReason, Set<State>> permittedStatesByReason =
        Map.ofEntries(
            new SimpleEntry<>(CORRECTIONS, of(CaseClosed)),
            new SimpleEntry<>(LISTED_CASE, of(AwaitingHearing)),
            new SimpleEntry<>(LISTED_CASE_WITHIN_5_DAYS, of(AwaitingHearing)),
            new SimpleEntry<>(LISTING_DIRECTIONS, complementOf(of(AwaitingHearing))),
            new SimpleEntry<>(NEW_CASE, of(Submitted, CaseManagement)),
            new SimpleEntry<>(POSTPONEMENT_REQUEST, of(AwaitingHearing)),
            new SimpleEntry<>(REINSTATEMENT_REQUEST, of(CaseClosed)),
            new SimpleEntry<>(RULE_27_REQUEST, complementOf(of(CaseClosed))),
            new SimpleEntry<>(SET_ASIDE_REQUEST, of(CaseClosed)),
            new SimpleEntry<>(STAY_REQUEST, complementOf(of(CaseClosed))),
            new SimpleEntry<>(STRIKE_OUT_REQUEST, complementOf(of(CaseClosed))),
            new SimpleEntry<>(TIME_EXTENSION_REQUEST, complementOf(of(CaseClosed))),
            new SimpleEntry<>(WITHDRAWAL_REQUEST, complementOf(of(CaseClosed))),
            new SimpleEntry<>(WRITTEN_REASONS_REQUEST, of(CaseClosed)),
            new SimpleEntry<>(OTHER, allOf(State.class))
        );

    @Override
    public void addTo(PageBuilder pageBuilder) {
        pageBuilder.page("referToJudgeReason", this::midEvent)
            .pageLabel("Referral reasons")
            .complex(CaseData::getReferToJudge)
            .mandatory(ReferToJudge::getReferralReason)
            .mandatory(ReferToJudge::getReasonForReferral, "referToJudgeReferralReason = \"other\"")
            .done();
    }

    public AboutToStartOrSubmitResponse<CaseData, State> midEvent(CaseDetails<CaseData, State> details,
                                                                  CaseDetails<CaseData, State> detailsBefore) {
        final State caseState = details.getState();
        final CaseData data = details.getData();
        final List<String> errors = new ArrayList<>();

        if (!emptyIfNull(permittedStatesByReason.get(data.getReferToJudge().getReferralReason())).contains(caseState)) {
            errors.add(INCOMPATIBLE_REFERRAL_REASON);
        }
        return AboutToStartOrSubmitResponse.<CaseData, State>builder()
            .data(data)
            .errors(errors)
            .build();
    }
}
