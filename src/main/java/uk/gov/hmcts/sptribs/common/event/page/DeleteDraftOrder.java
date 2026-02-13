package uk.gov.hmcts.sptribs.common.event.page;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.sptribs.ciccase.model.CaseData;
import uk.gov.hmcts.sptribs.ciccase.model.CicCase;
import uk.gov.hmcts.sptribs.common.ccd.CcdPageConfiguration;
import uk.gov.hmcts.sptribs.common.ccd.PageBuilder;


@Slf4j
@Component
public class DeleteDraftOrder implements CcdPageConfiguration {

    @Override
    public void addTo(PageBuilder pageBuilder) {
        pageBuilder
            .page("deleteDraftOrder")
            .pageLabel("Delete draft order")
            .label("LabelDeleteDraftOrder", "Draft to be deleted")
            .complex(CaseData::getCicCase)
                .mandatoryWithLabel(CicCase::getDraftOrderDynamicList, "Draft order to be deleted")
                .done()
            .done();
    }
}
