package uk.gov.hmcts.sptribs.caseworker.event.page;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.ccd.sdk.api.CaseDetails;
import uk.gov.hmcts.ccd.sdk.api.callback.AboutToStartOrSubmitResponse;
import uk.gov.hmcts.ccd.sdk.type.ListValue;
import uk.gov.hmcts.sptribs.caseworker.model.Listing;
import uk.gov.hmcts.sptribs.ciccase.model.CaseData;
import uk.gov.hmcts.sptribs.ciccase.model.CicCase;
import uk.gov.hmcts.sptribs.ciccase.model.State;
import uk.gov.hmcts.sptribs.common.ccd.CcdPageConfiguration;
import uk.gov.hmcts.sptribs.common.ccd.PageBuilder;

import java.util.ArrayList;
import java.util.List;

import static uk.gov.hmcts.sptribs.caseworker.util.EventConstants.CASEWORKER_CANCEL_HEARING;
import static uk.gov.hmcts.sptribs.caseworker.util.EventConstants.CASEWORKER_CREATE_HEARING_SUMMARY;
import static uk.gov.hmcts.sptribs.caseworker.util.EventConstants.CASEWORKER_EDIT_HEARING_SUMMARY;
import static uk.gov.hmcts.sptribs.caseworker.util.EventConstants.CASEWORKER_EDIT_RECORD_LISTING;
import static uk.gov.hmcts.sptribs.caseworker.util.EventConstants.CASEWORKER_POSTPONE_HEARING;
import static uk.gov.hmcts.sptribs.caseworker.util.EventUtil.getId;

@Slf4j
@Component
public class SelectHearing implements CcdPageConfiguration {


    @Override
    public void addTo(PageBuilder pageBuilder) {
        pageBuilder
            .page("selectHearing", this::midEvent)
            .pageLabel("Select hearing")
            .complex(CaseData::getCicCase)
            .mandatoryWithLabel(CicCase::getHearingList, "Choose a hearing to summarise")
            .done();
    }


    public AboutToStartOrSubmitResponse<CaseData, State> midEvent(CaseDetails<CaseData, State> details,
                                                                  CaseDetails<CaseData, State> detailsBefore) {
        final CaseData data = details.getData();
        final List<String> errors = new ArrayList<>();
        String selectedHearing = data.getCicCase().getHearingList().getValue().getLabel();
        String id = getId(selectedHearing);
        var hearingList = data.getHearingList();
        for (ListValue<Listing> listingListValue : hearingList) {
            if (null != id && id.equals(listingListValue.getId())) {
                data.setListing(listingListValue.getValue());
                break;
            }
        }
        if (StringUtils.isBlank(selectedHearing)) {
            if(data.getCurrentEvent().equals(CASEWORKER_CREATE_HEARING_SUMMARY)) {
                errors.add("Please select a hearing to summarize");
            }else if(data.getCurrentEvent().equals(CASEWORKER_EDIT_HEARING_SUMMARY)) {
                errors.add("Please select a hearing summarize to edit");
            }else if(data.getCurrentEvent().equals(CASEWORKER_CANCEL_HEARING)) {
                errors.add("Please select a hearing to cancel");
            }else if(data.getCurrentEvent().equals(CASEWORKER_POSTPONE_HEARING)) {
                errors.add("Please select a hearing to postpone");
            }else if(data.getCurrentEvent().equals(CASEWORKER_EDIT_RECORD_LISTING)) {
                errors.add("Please select a listing to edit");
            }
        }
        return AboutToStartOrSubmitResponse.<CaseData, State>builder()
            .data(data)
            .errors(errors)
            .build();
    }


}
