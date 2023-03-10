package uk.gov.hmcts.sptribs.caseworker.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.ccd.sdk.api.CaseDetails;
import uk.gov.hmcts.ccd.sdk.type.DynamicList;
import uk.gov.hmcts.sptribs.ciccase.model.CaseData;
import uk.gov.hmcts.sptribs.ciccase.model.State;

import java.time.format.DateTimeFormatter;

import static java.time.format.DateTimeFormatter.ofPattern;
import static java.util.Locale.UK;
import static uk.gov.hmcts.sptribs.caseworker.util.DynamicListUtil.createDynamicListWithOneElement;
import static uk.gov.hmcts.sptribs.caseworker.util.EventConstants.HYPHEN;
import static uk.gov.hmcts.sptribs.caseworker.util.EventConstants.SPACE;

@Service
@Slf4j
public class HearingService {

    final DateTimeFormatter dateFormatter = ofPattern("dd MMM yyyy", UK);

    public DynamicList getHearingDateDynamicList(final CaseDetails<CaseData, State> caseDetails) {
        CaseData data = caseDetails.getData();
        String hearingDate =
            data.getListing().getHearingType().getLabel()
                + SPACE + HYPHEN + SPACE
                + data.getListing().getHearingDate().format(dateFormatter)
                + SPACE
                + data.getListing().getHearingTime();

        return createDynamicListWithOneElement(hearingDate);
    }

    public DynamicList getHearingSummaryDynamicList(final CaseDetails<CaseData, State> caseDetails) {
        CaseData data = caseDetails.getData();
        String hearingSummary =
            data.getListing().getHearingType().getLabel()
                + SPACE + HYPHEN + SPACE
                + data.getListing().getHearingDate().format(dateFormatter)
                + SPACE
                + data.getListing().getHearingTime();

        return createDynamicListWithOneElement(hearingSummary);
    }
}
