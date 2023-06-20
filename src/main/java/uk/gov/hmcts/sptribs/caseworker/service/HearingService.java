package uk.gov.hmcts.sptribs.caseworker.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import uk.gov.hmcts.ccd.sdk.api.CaseDetails;
import uk.gov.hmcts.ccd.sdk.type.DynamicList;
import uk.gov.hmcts.ccd.sdk.type.ListValue;
import uk.gov.hmcts.sptribs.caseworker.model.Listing;
import uk.gov.hmcts.sptribs.ciccase.model.CaseData;
import uk.gov.hmcts.sptribs.ciccase.model.HearingState;
import uk.gov.hmcts.sptribs.ciccase.model.State;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import static java.time.format.DateTimeFormatter.ofPattern;
import static java.util.Locale.UK;
import static uk.gov.hmcts.sptribs.caseworker.util.DynamicListUtil.createDynamicList;
import static uk.gov.hmcts.sptribs.caseworker.util.DynamicListUtil.createDynamicListWithOneElement;
import static uk.gov.hmcts.sptribs.caseworker.util.EventConstants.HYPHEN;
import static uk.gov.hmcts.sptribs.caseworker.util.EventConstants.SPACE;

@Service
@Slf4j
public class HearingService {

    final DateTimeFormatter dateFormatter = ofPattern("dd MMM yyyy", UK);


    public DynamicList getListedHearingDynamicList(final CaseData data) {

        List<String> hearingDateList = new ArrayList<>();

        for (ListValue<Listing> listing : data.getHearingList()) {
            if (listing.getValue().getHearingStatus() == HearingState.Listed) {
                String hearingDate =
                    listing.getId()
                        + SPACE + HYPHEN + SPACE
                        + listing.getValue().getHearingType().getLabel()
                        + SPACE + HYPHEN + SPACE
                        + listing.getValue().getDate().format(dateFormatter)
                        + SPACE
                        + listing.getValue().getHearingTime();
                hearingDateList.add(hearingDate);
            }
        }

        return createDynamicList(hearingDateList);
    }

    public DynamicList getCompletedHearingDynamicList(final CaseData data) {

        List<String> hearingDateList = new ArrayList<>();

        for (ListValue<Listing> listing : data.getHearingList()) {
            if (listing.getValue().getHearingStatus() == HearingState.Complete) {
                String hearingDate =
                    listing.getId()
                        + SPACE + HYPHEN + SPACE
                        + listing.getValue().getHearingType().getLabel()
                        + SPACE + HYPHEN + SPACE
                        + listing.getValue().getDate().format(dateFormatter)
                        + SPACE
                        + listing.getValue().getHearingTime();
                hearingDateList.add(hearingDate);
            }
        }

        return createDynamicList(hearingDateList);
    }

    public void addListing(CaseData caseData, Listing listing) {
        if (CollectionUtils.isEmpty(caseData.getHearingList())) {
            List<ListValue<Listing>> listValues = new ArrayList<>();

            var listValue = ListValue
                .<Listing>builder()
                .id("1")
                .value(listing)
                .build();

            listValues.add(listValue);

            caseData.setHearingList(listValues);
        } else {
            AtomicInteger listValueIndex = new AtomicInteger(0);
            var listValue = ListValue
                .<Listing>builder()
                .value(listing)
                .build();

            caseData.getHearingList().add(0, listValue); // always add new note as first element so that it is displayed on top

            caseData.getHearingList().forEach(
                caseNoteListValue -> caseNoteListValue.setId(String.valueOf(listValueIndex.incrementAndGet())));

        }
    }

    public void updateHearingList(CaseData caseData) {
        for (ListValue<Listing> listingListValue : caseData.getHearingList()) {
            String hearingName = caseData.getCicCase().getHearingList().getValue().getLabel();
            if (hearingName.contains(listingListValue.getValue().getHearingTime())
                && hearingName.contains(listingListValue.getValue().getHearingType().getLabel())) {
                listingListValue.setValue(caseData.getSelectedListing());
                break;
            }
        }
    }


    public Listing getCompletedHearing(CaseData data) {

        List<Listing> completedHearingList = getCompletedHearingList(data);
        Listing latestCompletedHearing = new Listing();
        if (null != completedHearingList) {
            LocalDate latest = LocalDate.MIN;
            for (Listing listing : completedHearingList) {
                if (listing.getDate().isAfter(latest)) {
                    latest = listing.getDate();
                    latestCompletedHearing = listing;
                }
            }
        }
        return latestCompletedHearing;
    }

    public List<Listing> getListedHearingList(CaseData caseData) {
        List<Listing> listedHearingList = new ArrayList<>();
        if (null != caseData.getHearingList()) {
            for (ListValue<Listing> listingListValue : caseData.getHearingList()) {
                if (listingListValue.getValue().getHearingStatus() == HearingState.Listed) {
                    listedHearingList.add(listingListValue.getValue());
                }
            }
        }
        return listedHearingList;
    }

    public List<Listing> getCompletedHearingList(CaseData caseData) {
        List<Listing> completedHearingList = new ArrayList<>();
        if (null != caseData.getHearingList()) {
            for (ListValue<Listing> listingListValue : caseData.getHearingList()) {
                if (listingListValue.getValue().getHearingStatus() == HearingState.Complete) {
                    completedHearingList.add(listingListValue.getValue());
                }
            }
        }
        return completedHearingList;
    }
}
