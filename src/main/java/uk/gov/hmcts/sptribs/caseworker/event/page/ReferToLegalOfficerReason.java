package uk.gov.hmcts.sptribs.caseworker.event.page;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
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


@Component
@Slf4j
public class ReferToLegalOfficerReason implements CcdPageConfiguration {


    private final Map<ReferralReason, Set<State>> permittedStatesByReason =
        Map.ofEntries(
            new AbstractMap.SimpleEntry<>(ReferralReason.CORRECTIONS, of(State.CaseClosed)),
            new AbstractMap.SimpleEntry<>(ReferralReason.LISTED_CASE, of(State.AwaitingHearing)),
            new AbstractMap.SimpleEntry<>(ReferralReason.LISTED_CASE_WITHIN_5_DAYS, of(State.AwaitingHearing)),
            new AbstractMap.SimpleEntry<>(ReferralReason.LISTING_DIRECTIONS, complementOf(of(State.AwaitingHearing))),
            new AbstractMap.SimpleEntry<>(ReferralReason.NEW_CASE, of(State.Submitted)),
            new AbstractMap.SimpleEntry<>(ReferralReason.POSTPONEMENT_REQUEST, of(State.AwaitingHearing)),
            new AbstractMap.SimpleEntry<>(ReferralReason.REINSTATEMENT_REQUEST, of(State.CaseClosed)),
            new AbstractMap.SimpleEntry<>(ReferralReason.RULE_27_REQUEST, complementOf(of(State.CaseClosed))),
            new AbstractMap.SimpleEntry<>(ReferralReason.SET_ASIDE_REQUEST, of(State.CaseClosed)),
            new AbstractMap.SimpleEntry<>(ReferralReason.STAY_REQUEST, complementOf(of(State.CaseClosed))),
            new AbstractMap.SimpleEntry<>(ReferralReason.STRIKE_OUT_REQUEST, complementOf(of(State.CaseClosed))),
            new AbstractMap.SimpleEntry<>(ReferralReason.TIME_EXTENSION_REQUEST, complementOf(of(State.CaseClosed))),
            new AbstractMap.SimpleEntry<>(ReferralReason.WITHDRAWAL_REQUEST, complementOf(of(State.CaseClosed))),
            new AbstractMap.SimpleEntry<>(ReferralReason.WRITTEN_REASONS_REQUEST, of(State.CaseClosed)),
            new AbstractMap.SimpleEntry<>(ReferralReason.OTHER, allOf(State.class))
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
            errors.add("The case state is incompatible with the selected referral reason");
        }
        return AboutToStartOrSubmitResponse.<CaseData, State>builder()
            .data(data)
            .errors(errors)
            .build();
    }


}
