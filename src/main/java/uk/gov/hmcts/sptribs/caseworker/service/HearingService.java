package uk.gov.hmcts.sptribs.caseworker.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.ccd.sdk.api.CaseDetails;
import uk.gov.hmcts.ccd.sdk.type.DynamicList;
import uk.gov.hmcts.ccd.sdk.type.DynamicListElement;
import uk.gov.hmcts.sptribs.ciccase.model.CaseData;
import uk.gov.hmcts.sptribs.ciccase.model.State;

import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import static java.time.format.DateTimeFormatter.ofPattern;
import static java.util.Locale.UK;
import static uk.gov.hmcts.sptribs.caseworker.util.EventConstants.HYPHEN;
import static uk.gov.hmcts.sptribs.caseworker.util.EventConstants.SPACE;

@Service
@Slf4j
public class HearingService {
    public DynamicList getHearingDateDynamicList(final CaseDetails<CaseData, State> caseDetails) {
        final DateTimeFormatter dateFormatter = ofPattern("dd MMM yyyy", UK);
        CaseData data = caseDetails.getData();
        String hearingDate =
            data.getRecordListing().getHearingType().getLabel()
                + SPACE + HYPHEN + SPACE
                + data.getRecordListing().getHearingDate().format(dateFormatter)
                + SPACE
                + data.getRecordListing().getHearingTime();
        List<String> hearingDateList = new ArrayList<>();
        hearingDateList.add(hearingDate);
        List<DynamicListElement> dynamicListElements = hearingDateList
            .stream()
            .sorted()
            .map(date -> DynamicListElement.builder().label(date).code(UUID.randomUUID()).build())
            .collect(Collectors.toList());

        return DynamicList
            .builder()
            .listItems(dynamicListElements)
            .build();
    }
}
