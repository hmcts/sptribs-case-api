package uk.gov.hmcts.sptribs.common.event.page;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.sptribs.common.ccd.CcdPageConfiguration;
import uk.gov.hmcts.sptribs.common.ccd.PageBuilder;

@Slf4j
@Component
public class PreviewDraftOrder implements CcdPageConfiguration {


    @Override
    public void addTo(PageBuilder pageBuilder) {
        pageBuilder
            .page("previewOrder")
            .pageLabel("Preview order")
            .label("previewDraft", " Order preview")
            .label("make Changes", "To make changes, choose 'Edit order'\n\n"
                + "If you are happy , continue to the next screen.")
            .done();

    }


}

