package uk.gov.hmcts.sptribs.caseworker.event.page;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang3.ObjectUtils;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.ccd.sdk.api.CaseDetails;
import uk.gov.hmcts.ccd.sdk.api.callback.AboutToStartOrSubmitResponse;
import uk.gov.hmcts.ccd.sdk.type.ListValue;
import uk.gov.hmcts.sptribs.caseworker.model.HearingSummary;
import uk.gov.hmcts.sptribs.caseworker.model.Listing;
import uk.gov.hmcts.sptribs.caseworker.util.PageShowConditionsUtil;
import uk.gov.hmcts.sptribs.ciccase.model.CaseData;
import uk.gov.hmcts.sptribs.ciccase.model.State;
import uk.gov.hmcts.sptribs.common.ccd.CcdPageConfiguration;
import uk.gov.hmcts.sptribs.common.ccd.PageBuilder;
import uk.gov.hmcts.sptribs.document.model.CaseworkerCICDocument;

import java.util.ArrayList;
import java.util.List;

@Slf4j
@Component
public class HearingRecordingUploadPage implements CcdPageConfiguration {

    @Override
    public void addTo(PageBuilder pageBuilder) {
        pageBuilder.page("hearingRecordingUploadPage", this::midEvent)
            .pageLabel("Upload hearing recording")
            .label("LabelHearingRecordingUploadPage", "")
            .pageShowConditions(PageShowConditionsUtil.editSummaryShowConditions())
            .label("theHearingRecordingUpload",
                "\n<h2>Upload the recording of the hearing (Optional)</h2>\n"
                    + "\n\nAdvice on uploads\n\n"
                    + "\n- File must be no larger than 500 MB\n"
                    + "\n- You can only upload mp3 files\n"
                    + "\n- Give the files a meaningful name. for example, bail-hearing-John-Smith.mp3\n\n\n\n"
                    + "Note: If the remove button is disabled, please refresh the page to remove attachments")
            .complex(CaseData::getListing)
            .complex(Listing::getSummary)
            .optionalWithLabel(HearingSummary::getRecFile, "Upload file")
            .label("theHearingRecordDescription", "<h3>If you can't upload a recording of the hearing, "
                + "please describe where it can be found. You can also enter a link to the recording</h3>")
            .optional(HearingSummary::getRecDesc)
            .done();
    }

    public AboutToStartOrSubmitResponse<CaseData, State> midEvent(CaseDetails<CaseData, State> details,
                                                                  CaseDetails<CaseData, State> detailsBefore) {
        final CaseData data = details.getData();
        List<ListValue<CaseworkerCICDocument>> uploadedDocuments = data.getListing().getSummary().getRecFile();
        List<String> errors = new ArrayList<>();
        if (!CollectionUtils.isEmpty(uploadedDocuments)) {
            for (ListValue<CaseworkerCICDocument> documentListValue : uploadedDocuments) {
                if (ObjectUtils.isEmpty(documentListValue.getValue())
                    || ObjectUtils.isEmpty(documentListValue.getValue().getDocumentLink())) {
                    errors.add("Please attach the document");
                } else {
                    if (!documentListValue.getValue().isDocumentValid("mp3")) {
                        errors.add("Please attach a mp3 document");
                    }
                    if (StringUtils.isEmpty(documentListValue.getValue().getDocumentEmailContent())) {
                        errors.add("Description is mandatory for each document");
                    }
                    if (ObjectUtils.isEmpty(documentListValue.getValue().getDocumentCategory())) {
                        errors.add("Category is mandatory for each document");
                    }
                }
            }
        }
        return AboutToStartOrSubmitResponse.<CaseData, State>builder()
            .data(data)
            .errors(errors)
            .build();
    }

}
