package uk.gov.hmcts.sptribs.caseworker.event.page;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.sptribs.caseworker.model.RecordListing;
import uk.gov.hmcts.sptribs.ciccase.model.CaseData;
import uk.gov.hmcts.sptribs.common.ccd.CcdPageConfiguration;
import uk.gov.hmcts.sptribs.common.ccd.PageBuilder;

import java.util.HashMap;
import java.util.Map;

@Slf4j
@Component
public class UploadHearingNotice  implements CcdPageConfiguration {

    @Override
    public void addTo(PageBuilder pageBuilder) {
        final String uploadHearingNoticeObj = "uploadHearingNoticeObj1";
        Map<String, String> map = new HashMap<>();
        map.put("selectTemplateObj","recordHearingNotice = \"Create from a template\"");
        map.put(uploadHearingNoticeObj,"recordHearingNotice = \"Upload from your computer\"");

        pageBuilder.page(uploadHearingNoticeObj)
            .label(uploadHearingNoticeObj, "<h1>Upload hearing notice</h1>")
            .pageShowConditions(map)
            .label("uploadObjectLabel1",
                "\nUpload a copy of the hearing notice that you want to add to this case."
                    + " It must be signed by the appropriate parties.\n"
                    + "\nThe hearing notice should be:\n"
                    + "\n- a maximum of 100MB in size (larger files must be split)\n"
                    + "\n- labelled clearly, e.g. applicant-name-hearing-notice.pdf\n")
            .complex(CaseData::getRecordListing)
            .mandatoryWithLabel(RecordListing::getHearingNoticeDocuments, "Add a file")
            .done();
    }
}
