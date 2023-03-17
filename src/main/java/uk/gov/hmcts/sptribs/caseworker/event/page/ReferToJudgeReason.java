package uk.gov.hmcts.sptribs.caseworker.event.page;

import org.springframework.stereotype.Component;
import uk.gov.hmcts.ccd.sdk.api.CaseDetails;
import uk.gov.hmcts.ccd.sdk.api.callback.AboutToStartOrSubmitResponse;
import uk.gov.hmcts.sptribs.caseworker.model.ReferToJudge;
import uk.gov.hmcts.sptribs.caseworker.model.ReferralReason;
import uk.gov.hmcts.sptribs.ciccase.model.CaseData;
import uk.gov.hmcts.sptribs.ciccase.model.State;
import uk.gov.hmcts.sptribs.common.ccd.CcdPageConfiguration;
import uk.gov.hmcts.sptribs.common.ccd.PageBuilder;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static java.util.AbstractMap.SimpleEntry;
import static java.util.EnumSet.allOf;
import static java.util.EnumSet.complementOf;
import static java.util.EnumSet.of;
import static org.apache.commons.collections4.CollectionUtils.emptyIfNull;

@Component
public class ReferToJudgeReason implements CcdPageConfiguration {

    private final Map<ReferralReason, Set<State>> permittedStatesByReason =
        Map.ofEntries(
            new SimpleEntry<>(ReferralReason.CORRECTIONS, of(State.CaseClosed)),
            new SimpleEntry<>(ReferralReason.LISTED_CASE, of(State.AwaitingHearing)),
            new SimpleEntry<>(ReferralReason.LISTED_CASE_WITHIN_5_DAYS, of(State.AwaitingHearing)),
            new SimpleEntry<>(ReferralReason.LISTING_DIRECTIONS, complementOf(of(State.AwaitingHearing))),
            new SimpleEntry<>(ReferralReason.NEW_CASE, of(State.Submitted)),
            new SimpleEntry<>(ReferralReason.POSTPONEMENT_REQUEST, of(State.AwaitingHearing)),
            new SimpleEntry<>(ReferralReason.REINSTATEMENT_REQUEST, of(State.CaseClosed)),
            new SimpleEntry<>(ReferralReason.RULE_27_REQUEST, complementOf(of(State.CaseClosed))),
            new SimpleEntry<>(ReferralReason.SET_ASIDE_REQUEST, EnumSet.of(State.CaseClosed)),
            new SimpleEntry<>(ReferralReason.STAY_REQUEST, complementOf(of(State.CaseClosed))),
            new SimpleEntry<>(ReferralReason.STRIKE_OUT_REQUEST, complementOf(of(State.CaseClosed))),
            new SimpleEntry<>(ReferralReason.TIME_EXTENSION_REQUEST, complementOf(of(State.CaseClosed))),
            new SimpleEntry<>(ReferralReason.WITHDRAWAL_REQUEST, complementOf(of(State.CaseClosed))),
            new SimpleEntry<>(ReferralReason.WRITTEN_REASONS_REQUEST, of(State.CaseClosed)),
            new SimpleEntry<>(ReferralReason.OTHER, allOf(State.class))
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
            errors.add("The case state is incompatible with the selected referral reason");
        }
        return AboutToStartOrSubmitResponse.<CaseData, State>builder()
            .data(data)
            .errors(errors)
            .build();
    }
}
