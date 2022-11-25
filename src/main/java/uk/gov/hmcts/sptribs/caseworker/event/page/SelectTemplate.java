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
public class SelectTemplate implements CcdPageConfiguration {
    @Override
    public void addTo(PageBuilder pageBuilder) {
        final String selectTemplateObj = "selectTemplateObj";
        Map<String, String> map = new HashMap<>();
        map.put(selectTemplateObj,"recordHearingNotice = \"Create from a template\"");
        map.put("uploadHearingNoticeObj1","recordHearingNotice = \"Upload from your computer\"");

        pageBuilder.page(selectTemplateObj)
            .label(selectTemplateObj, "<h1>Select a template</h1>")
            .pageShowConditions(map)
            .complex(CaseData::getRecordListing)
            .mandatory(RecordListing::getTemplate)
            .done();
    }
}
