package uk.gov.hmcts.sptribs.common.event.page;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.sptribs.ciccase.model.CaseData;
import uk.gov.hmcts.sptribs.ciccase.model.CicCase;
import uk.gov.hmcts.sptribs.common.ccd.CcdPageConfiguration;
import uk.gov.hmcts.sptribs.common.ccd.PageBuilder;


@Slf4j
@Component
public class EditDraftOrder implements CcdPageConfiguration {


    @Override
    public void addTo(PageBuilder pageBuilder) {
        pageBuilder
            .page("editDraftOrder")
            .pageLabel("Edit order")
            .label("LabelEditDraftOrder", "Draft to be edited")
            .complex(CaseData::getCicCase)
            .optional(CicCase::getOrderTemplateDynamisList)
            .done();
    }




}
