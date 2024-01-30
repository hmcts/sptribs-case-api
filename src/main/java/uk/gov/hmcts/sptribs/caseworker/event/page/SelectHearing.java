package uk.gov.hmcts.sptribs.caseworker.event.page;

import lombok.extern.slf4j.Slf4j;
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
            .mandatoryWithLabel(CicCase::getHearingList, "Choose a hearing")
            .done();
    }


    public AboutToStartOrSubmitResponse<CaseData, State> midEvent(CaseDetails<CaseData, State> details,
                                                                  CaseDetails<CaseData, State> detailsBefore) {

        final CaseData data = details.getData();
        final List<String> errors = new ArrayList<>();

        final String selectedHearing = data.getCicCase().getHearingList().getValue().getLabel();
        final String id = getId(selectedHearing);
        final List<ListValue<Listing>> hearingList = data.getHearingList();

        for (ListValue<Listing> listingListValue : hearingList) {
            if (null != id && id.equals(listingListValue.getId())) {
                data.setListing(listingListValue.getValue());
                break;
            }
        }

        return AboutToStartOrSubmitResponse.<CaseData, State>builder()
            .data(data)
            .errors(errors)
            .build();
    }

}
