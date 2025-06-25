package uk.gov.hmcts.sptribs.caseworker.event.page;

import org.springframework.util.CollectionUtils;
import org.springframework.util.ObjectUtils;
import uk.gov.hmcts.ccd.sdk.api.CaseDetails;
import uk.gov.hmcts.ccd.sdk.api.callback.AboutToStartOrSubmitResponse;
import uk.gov.hmcts.ccd.sdk.type.ListValue;
import uk.gov.hmcts.sptribs.caseworker.model.CloseCase;
import uk.gov.hmcts.sptribs.caseworker.model.DocumentManagement;
import uk.gov.hmcts.sptribs.caseworker.model.HearingSummary;
import uk.gov.hmcts.sptribs.caseworker.model.Listing;
import uk.gov.hmcts.sptribs.ciccase.model.CaseData;
import uk.gov.hmcts.sptribs.ciccase.model.CicCase;
import uk.gov.hmcts.sptribs.ciccase.model.State;
import uk.gov.hmcts.sptribs.common.ccd.CcdPageConfiguration;
import uk.gov.hmcts.sptribs.common.ccd.PageBuilder;
import uk.gov.hmcts.sptribs.document.model.CaseworkerCICDocument;

import java.util.ArrayList;
import java.util.List;

import static uk.gov.hmcts.sptribs.caseworker.util.DocumentRemoveListUtil.setDocumentsListForRemoval;

public class ShowCaseDocuments implements CcdPageConfiguration {


    @Override
    public void addTo(PageBuilder pageBuilder) {
        pageBuilder
            .page("showCaseDocuments", this::midEvent)
            .pageLabel("Show case documents")
            .label("LabelShowCaseDocuments", "")
            .complex(CaseData::getAllDocManagement)
            .readonlyWithLabel(DocumentManagement::getCaseworkerCICDocument, "Document Management Files")
            .done()
            .complex(CaseData::getCloseCase)
            .readonly(CloseCase::getDocuments)
            .done()
            .complex(CaseData::getCicCase)
            .readonly(CicCase::getReinstateDocuments)
            .readonly(CicCase::getApplicantDocumentsUploaded)
            .readonly(CicCase::getOrderDocumentList)
            .readonly(CicCase::getFinalDecisionDocumentList)
            .readonly(CicCase::getDecisionDocumentList)
            .done()
            .complex(CaseData::getLatestCompletedHearing)
            .complex(Listing::getSummary)
            .readonlyWithLabel(HearingSummary::getRecFile, "Hearing Summary Documents")
            .done();
    }

    public AboutToStartOrSubmitResponse<CaseData, State> midEvent(CaseDetails<CaseData, State> details,
                                                                  CaseDetails<CaseData, State> detailsBefore) {
        final CaseData data = details.getData();
        final CaseData oldData = detailsBefore.getData();
        if (ObjectUtils.isEmpty(data.getCicCase().getRemovedDocumentList())) {
            List<ListValue<CaseworkerCICDocument>> removedDocumentList = new ArrayList<>();
            data.getCicCase().setRemovedDocumentList(removedDocumentList);
        }
        final CaseData newCaseData = setDocumentsListForRemoval(data, oldData);
        data.getCicCase().setReadOnlyRemovedDocList(data.getCicCase().getRemovedDocumentList());
        final List<String> errors = new ArrayList<>();
        if (CollectionUtils.isEmpty(newCaseData.getCicCase().getRemovedDocumentList())) {
            errors.add("Please remove at least one document to continue");
        }
        return AboutToStartOrSubmitResponse.<CaseData, State>builder()
            .data(newCaseData)
            .errors(errors)
            .build();
    }


}
