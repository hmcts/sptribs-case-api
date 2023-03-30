package uk.gov.hmcts.sptribs.caseworker.event.page;

import lombok.extern.slf4j.Slf4j;
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

import java.util.List;

import static uk.gov.hmcts.sptribs.document.DocumentUtil.validateCaseworkerCICDocumentFormat;

@Slf4j
@Component
public class HearingRecordingUploadPage implements CcdPageConfiguration {

    @Override
    public void addTo(PageBuilder pageBuilder) {
        pageBuilder.page("hearingRecordingUploadPage", this::midEvent)
            .pageLabel("Upload hearing recording")
            .label("LabelHearingRecordingUploadPage","")
            .pageShowConditions(PageShowConditionsUtil.editSummaryShowConditions())
            .label("theHearingRecordingUpload",
                "\n<h2>Upload the recording of the hearing (Optional)</h2>\n"
                    + "\n\nAdvice on uploads\n\n"
                    + "\n- File must be no larger than 500 MB\n"
                    + "\n- You can only upload mp3 files\n"
                    + "\n- Give the files a meaningful name. for example, bail-hearing-John-Smith.mp3\n")
            .complex(CaseData::getListing)
            .complex(Listing::getSummary)
            .optionalWithLabel(HearingSummary::getRecFile, "Upload file")
            .label("theHearingRecordDescription", "<h3>If you can't upload a recording of the hearing, "
                + "please describe where it can be found. You can also enter a link to the recording</h3>")
            .optional(HearingSummary::getRecDesc)
            .done();
    }

    private AboutToStartOrSubmitResponse<CaseData, State> midEvent(CaseDetails<CaseData, State> details,
                                                                   CaseDetails<CaseData, State> detailsBefore) {
        final CaseData data = details.getData();
        List<ListValue<CaseworkerCICDocument>> uploadedDocuments = data.getListing().getSummary().getRecFile();
        final List<String> errors = validateCaseworkerCICDocumentFormat(uploadedDocuments);

        return AboutToStartOrSubmitResponse.<CaseData, State>builder()
            .data(data)
            .errors(errors)
            .build();
    }

}
