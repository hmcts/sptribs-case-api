package uk.gov.hmcts.sptribs.common.event.page;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.ccd.sdk.api.CaseDetails;
import uk.gov.hmcts.ccd.sdk.api.callback.AboutToStartOrSubmitResponse;
import uk.gov.hmcts.ccd.sdk.type.Document;
import uk.gov.hmcts.sptribs.caseworker.model.DraftOrderMainContentCIC;
import uk.gov.hmcts.sptribs.ciccase.model.CaseData;
import uk.gov.hmcts.sptribs.ciccase.model.LanguagePreference;
import uk.gov.hmcts.sptribs.ciccase.model.State;
import uk.gov.hmcts.sptribs.common.ccd.CcdPageConfiguration;
import uk.gov.hmcts.sptribs.common.ccd.PageBuilder;
import uk.gov.hmcts.sptribs.document.CaseDataDocumentService;
import uk.gov.hmcts.sptribs.document.content.PreviewDraftOrderTemplateContent;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Slf4j
@Component
public class DraftOrderMainContentPage implements CcdPageConfiguration {

    private static final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    @Autowired
    private CaseDataDocumentService caseDataDocumentService;

    @Autowired
    private PreviewDraftOrderTemplateContent previewDraftOrderTemplateContent;


    @Override
    public void addTo(PageBuilder pageBuilder) {
        pageBuilder
            .page("mainContent",this::midEvent)
            .pageLabel("Edit order")
            .label("EditDraftOrderMainContent", "<hr>" + "\n<h3>Header</h3>" + "\nThe header will be automatically generated."
                + "You can preview this in pdf document on the next screen.\n\n"
                + "<hr>\n"
                + "<h3>Main content</h3>\n\n"
                + "Enter text in the box below.This will be added into the centre"
                + " of the generated order document.\n")
            .complex(CaseData::getDraftOrderMainContentCIC)
            .optional(DraftOrderMainContentCIC::getMainContent)
            .done()
            .label("footer", "<h2>Footer</h2>\n The footer will be automatically generated.\n "
                + "You can preview this in pdf document on the next screen.\n"
                + "<hr>\n")
            .done();
    }

    public AboutToStartOrSubmitResponse<CaseData, State> midEvent(
        CaseDetails<CaseData, State> details,
        CaseDetails<CaseData, State> detailsBefore
    ) {


        CaseData caseData = details.getData();
        var template = caseData.getCicCase().getAnOrderTemplates();
        String subjectName = caseData.getCicCase().getFullName();
        final Long caseId = details.getId();
        final String filename = "Order-[" +  subjectName + "]-" + LocalDateTime.now().format(formatter);

        Document generalOrderDocument = caseDataDocumentService.renderDocument(
            previewDraftOrderTemplateContent.apply(caseData, caseId),
            caseId,
            caseData.getCicCase().getAnOrderTemplates().getId(),
            LanguagePreference.ENGLISH,
            filename
        );

        caseData.getCicCase().setAnOrderTemplates(template);
        caseData.getCicCase().setOrderTemplateIssued(generalOrderDocument);

        return AboutToStartOrSubmitResponse.<CaseData, State>builder()
            .data(caseData)
            .build();
    }

}
