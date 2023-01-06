package uk.gov.hmcts.sptribs.common.event.page;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.sptribs.caseworker.model.DraftOrderCIC;
import uk.gov.hmcts.sptribs.ciccase.model.CaseData;
import uk.gov.hmcts.sptribs.ciccase.model.CicCase;
import uk.gov.hmcts.sptribs.common.ccd.CcdPageConfiguration;
import uk.gov.hmcts.sptribs.common.ccd.PageBuilder;

@Slf4j
@Component
public class CreateDraftOrder implements CcdPageConfiguration {


    @Override
    public void addTo(PageBuilder pageBuilder) {
        pageBuilder
            .page("createDraftOrder")
            .pageLabel("Create order")
           // .label("createDraftOrder", "Draft to be created")
            .complex(CaseData::getCicCase)

            //.complex(CicCase::getDraftOrderCIC)
            .mandatory(CicCase::getAnOrderTemplates)
            //.mandatory(DraftOrderCIC::getAnOrderTemplate)
//            .label("footer", "<h2>Footer</h2>\n First-tier Tribunal (Health,Education and Social Care)\n\n"
//                + "Date Issued &lt; &lt;  SaveDate &gt; &gt;")
            .done();
    }


}
