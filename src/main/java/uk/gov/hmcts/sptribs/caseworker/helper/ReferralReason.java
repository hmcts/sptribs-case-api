package uk.gov.hmcts.sptribs.caseworker.helper;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.sptribs.ciccase.model.State;
import uk.gov.hmcts.sptribs.common.ccd.CcdPageConfiguration;

import java.util.AbstractMap;
import java.util.Map;
import java.util.Set;

import static java.util.EnumSet.allOf;
import static java.util.EnumSet.complementOf;
import static java.util.EnumSet.of;

@Component
@Slf4j
public class ReferralReason {

    private final Map<uk.gov.hmcts.sptribs.caseworker.model.ReferralReason, Set<State>> permittedStatesByReason =
        Map.ofEntries(
            new AbstractMap.SimpleEntry<>(uk.gov.hmcts.sptribs.caseworker.model.ReferralReason.CORRECTIONS, of(State.CaseClosed)),
            new AbstractMap.SimpleEntry<>(uk.gov.hmcts.sptribs.caseworker.model.ReferralReason.LISTED_CASE, of(State.AwaitingHearing)),
            new AbstractMap.SimpleEntry<>(uk.gov.hmcts.sptribs.caseworker.model.ReferralReason.LISTED_CASE_WITHIN_5_DAYS, of(State.AwaitingHearing)),
            new AbstractMap.SimpleEntry<>(uk.gov.hmcts.sptribs.caseworker.model.ReferralReason.LISTING_DIRECTIONS, complementOf(of(State.AwaitingHearing))),
            new AbstractMap.SimpleEntry<>(uk.gov.hmcts.sptribs.caseworker.model.ReferralReason.NEW_CASE, of(State.Submitted)),
            new AbstractMap.SimpleEntry<>(uk.gov.hmcts.sptribs.caseworker.model.ReferralReason.POSTPONEMENT_REQUEST, of(State.AwaitingHearing)),
            new AbstractMap.SimpleEntry<>(uk.gov.hmcts.sptribs.caseworker.model.ReferralReason.REINSTATEMENT_REQUEST, of(State.CaseClosed)),
            new AbstractMap.SimpleEntry<>(uk.gov.hmcts.sptribs.caseworker.model.ReferralReason.RULE_27_REQUEST, complementOf(of(State.CaseClosed))),
            new AbstractMap.SimpleEntry<>(uk.gov.hmcts.sptribs.caseworker.model.ReferralReason.SET_ASIDE_REQUEST, of(State.CaseClosed)),
            new AbstractMap.SimpleEntry<>(uk.gov.hmcts.sptribs.caseworker.model.ReferralReason.STAY_REQUEST, complementOf(of(State.CaseClosed))),
            new AbstractMap.SimpleEntry<>(uk.gov.hmcts.sptribs.caseworker.model.ReferralReason.STRIKE_OUT_REQUEST, complementOf(of(State.CaseClosed))),
            new AbstractMap.SimpleEntry<>(uk.gov.hmcts.sptribs.caseworker.model.ReferralReason.TIME_EXTENSION_REQUEST, complementOf(of(State.CaseClosed))),
            new AbstractMap.SimpleEntry<>(uk.gov.hmcts.sptribs.caseworker.model.ReferralReason.WITHDRAWAL_REQUEST, complementOf(of(State.CaseClosed))),
            new AbstractMap.SimpleEntry<>(uk.gov.hmcts.sptribs.caseworker.model.ReferralReason.WRITTEN_REASONS_REQUEST, of(State.CaseClosed)),
            new AbstractMap.SimpleEntry<>(uk.gov.hmcts.sptribs.caseworker.model.ReferralReason.OTHER, allOf(State.class))
        );
}
