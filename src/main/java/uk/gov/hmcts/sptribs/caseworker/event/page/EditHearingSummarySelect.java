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
import uk.gov.hmcts.sptribs.document.model.CaseworkerCICDocument;
import uk.gov.hmcts.sptribs.document.model.CaseworkerCICDocumentUpload;

import java.util.List;

import static uk.gov.hmcts.sptribs.caseworker.service.HearingService.isMatchingHearing;
import static uk.gov.hmcts.sptribs.document.DocumentUtil.convertToCaseworkerCICDocument;

@Slf4j
@Component
public class EditHearingSummarySelect implements CcdPageConfiguration {

    @Override
    public void addTo(PageBuilder pageBuilder) {
        pageBuilder
            .page("editHearingSummarySelect", this::midEvent)
            .pageLabel("Select hearing summary")
            .complex(CaseData::getCicCase)
            .mandatoryWithLabel(CicCase::getHearingSummaryList,"Choose a hearing summary to edit")
            .done();
    }

    public AboutToStartOrSubmitResponse<CaseData, State> midEvent(CaseDetails<CaseData, State> details,
                                                                  CaseDetails<CaseData, State> detailsBefore) {

        final CaseData caseData = details.getData();
        String hearingName = caseData.getCicCase().getHearingSummaryList().getValue().getLabel();

        for (ListValue<Listing> listingListValue : caseData.getHearingList()) {
            if (isMatchingHearing(listingListValue, hearingName)) {
                List<ListValue<CaseworkerCICDocument>> documents = listingListValue.getValue().getSummary().getRecFile();
                List<ListValue<CaseworkerCICDocumentUpload>> uploadedDocuments = convertToCaseworkerCICDocument(documents);
                caseData.getListing().getSummary().setRecFileUpload(uploadedDocuments);
                break;
            }
        }

        return AboutToStartOrSubmitResponse.<CaseData, State>builder()
            .data(caseData)
            .build();
    }
}
