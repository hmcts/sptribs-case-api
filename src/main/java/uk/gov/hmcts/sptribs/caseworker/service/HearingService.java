package uk.gov.hmcts.sptribs.caseworker.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import uk.gov.hmcts.ccd.sdk.type.DynamicList;
import uk.gov.hmcts.ccd.sdk.type.ListValue;
import uk.gov.hmcts.sptribs.caseworker.model.Listing;
import uk.gov.hmcts.sptribs.ciccase.model.CaseData;
import uk.gov.hmcts.sptribs.ciccase.model.HearingState;

import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import static java.time.format.DateTimeFormatter.ofPattern;
import static java.util.Locale.UK;
import static uk.gov.hmcts.sptribs.caseworker.util.DynamicListUtil.createDynamicList;
import static uk.gov.hmcts.sptribs.caseworker.util.EventConstants.HYPHEN;
import static uk.gov.hmcts.sptribs.caseworker.util.EventConstants.SPACE;

@Service
@Slf4j
public class HearingService {

    @Autowired
    private ObjectMapper objectMapper;

    private static final DateTimeFormatter dateFormatter = ofPattern("dd MMM yyyy", UK);

    public DynamicList getListedHearingDynamicList(final CaseData data) {

        List<String> hearingDateList = new ArrayList<>();

        for (ListValue<Listing> listing : data.getHearingList()) {
            if (listing.getValue().getHearingStatus() == HearingState.Listed) {
                String hearingDate = getHearingDate(listing.getId(), listing.getValue());
                hearingDateList.add(hearingDate);
            }
        }

        return createDynamicList(hearingDateList);
    }

    public void addListingIfExists(CaseData data) {
        if (data.getListing().getHearingType() != null && data.getHearingList().isEmpty()) {
            addListing(data, data.getListing());

            ListValue<Listing> firstListing = data.getHearingList().stream().findFirst().orElse(null);
            if (firstListing != null && data.getRetiredFields() != null) {
                firstListing.getValue().setHearingCancellationReason(data.getRetiredFields().getCicCaseHearingCancellationReason());
                firstListing.getValue().setCancelHearingAdditionalDetail(data.getRetiredFields().getCicCaseCancelHearingAdditionalDetail());
                firstListing.getValue().setPostponeReason(data.getRetiredFields().getCicCasePostponeReason());
                firstListing.getValue().setPostponeAdditionalInformation(data.getRetiredFields().getCicCasePostponeAdditionalInformation());
            }
        }
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

            ListValue<Listing> listValue = ListValue
                .<Listing>builder()
                .id("1")
                .value(listing)
                .build();

            listValues.add(listValue);

            caseData.setHearingList(listValues);
        } else {
            AtomicInteger listValueIndex = new AtomicInteger(0);
            ListValue<Listing> listValue = ListValue
                .<Listing>builder()
                .value(listing)
                .build();

            caseData.getHearingList().add(0, listValue); // always add new note as first element so that it is displayed on top

            caseData.getHearingList().forEach(
                caseNoteListValue -> caseNoteListValue.setId(String.valueOf(listValueIndex.incrementAndGet())));

        }
    }

    @SneakyThrows
    public void updateHearingList(CaseData caseData, String hearingName) {
        for (ListValue<Listing> listingListValue : caseData.getHearingList()) {
            if (isMatchingHearing(listingListValue, hearingName)) {
                // deep copy of listing needed in order to clear rec file
                Listing listingDeepCopy = objectMapper
                    .readValue(objectMapper.writeValueAsString(caseData.getListing()), Listing.class);
                listingListValue.setValue(listingDeepCopy);
                break;
            }
        }
    }

    public static boolean isMatchingHearing(ListValue<Listing> listingListValue, String hearingName) {
        return hearingName.contains(listingListValue.getValue().getHearingTime())
            && hearingName.contains(listingListValue.getValue().getHearingType().getLabel())
            && hearingName.contains(listingListValue.getValue().getDate().format(dateFormatter));
    }

    private String getHearingDate(String id, Listing listing) {
        return id
            + SPACE + HYPHEN + SPACE
            + listing.getHearingType().getLabel()
            + SPACE + HYPHEN + SPACE
            + listing.getDate().format(dateFormatter)
            + SPACE
            + listing.getHearingTime();
    }
}
