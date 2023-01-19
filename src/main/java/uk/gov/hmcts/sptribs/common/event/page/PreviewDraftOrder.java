package uk.gov.hmcts.sptribs.common.event.page;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.ccd.sdk.api.CaseDetails;
import uk.gov.hmcts.ccd.sdk.api.callback.AboutToStartOrSubmitResponse;
import uk.gov.hmcts.ccd.sdk.type.Document;
import uk.gov.hmcts.sptribs.ciccase.model.CaseData;
import uk.gov.hmcts.sptribs.ciccase.model.CicCase;
import uk.gov.hmcts.sptribs.ciccase.model.LanguagePreference;
import uk.gov.hmcts.sptribs.ciccase.model.State;
import uk.gov.hmcts.sptribs.common.ccd.CcdPageConfiguration;
import uk.gov.hmcts.sptribs.common.ccd.PageBuilder;
import uk.gov.hmcts.sptribs.document.CaseDataDocumentService;
import uk.gov.hmcts.sptribs.document.DocAssemblyService;
import uk.gov.hmcts.sptribs.document.content.FinalDecisionTemplateContent;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Map;

import static uk.gov.hmcts.sptribs.document.DocumentConstants.FINAL_DECISION_FILE;

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
            .complex(CaseData::getCicCase)
            .mandatory(CicCase::getAnOrderTemplates)
            .done();

    }


}

