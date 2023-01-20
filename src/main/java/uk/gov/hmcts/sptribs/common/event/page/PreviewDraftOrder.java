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
import uk.gov.hmcts.sptribs.document.content.FinalDecisionTemplateContent;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Slf4j
@Component
public class PreviewDraftOrder implements CcdPageConfiguration {
    private static final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    @Autowired
    private CaseDataDocumentService caseDataDocumentService;

    @Autowired
    private FinalDecisionTemplateContent finalDecisionTemplateContent;

    @Override
    public void addTo(PageBuilder pageBuilder) {
        pageBuilder
            .page("previewOrder")
            .pageLabel("Preview order")
            .label("previewDraft", " Order preview")
            .label("make Changes", "To make changes, choose 'Edit order'\n\n"
                + "If you are happy , continue to the next screen.")
           // .complex(CaseData::getCicCase)
            //.mandatory(CicCase::getAnOrderTemplates)
             .complex(CaseData::getDraftOrderMainContentCIC)
            .optional(DraftOrderMainContentCIC::getOrderTemplateIssued)
            .done();

    }
    public AboutToStartOrSubmitResponse<CaseData, State> midEvent(
        CaseDetails<CaseData, State> details,
        CaseDetails<CaseData, State> detailsBefore
    ) {

        //  final String authorisation = idamService.retrieveSystemUpdateUserDetails().getAuthToken();
        CaseData caseData = details.getData();
        var finalDecision = caseData.getCaseIssueFinalDecision();
        var finalOrderIssu = caseData.getDraftOrderMainContentCIC();

        var template = caseData.getCicCase().getAnOrderTemplates();
        final String templateId = caseData.getCicCase().getAnOrderTemplates().getId();
        final Long caseId = details.getId();
        //final Long ccdCaseReference= null;
        // final String templateName = docmosisTemplateProvider.templateNameFor(templateId, languagePreference);

        final String filename= "Order" + LocalDateTime.now().format(formatter);
//        Map<String, Object> templateContent = new HashMap<>();
//
//        templateContent.put(DATED, LocalDate.now().format(DATE_TIME_FORMATTER));
//        templateContent.put(CIC_CASE_SCHEME, caseData.getCicCase().getSchemeCic().getLabel());
//        templateContent.put(CASE_NUMBER, ccdCaseReference);
//        templateContent.put(REPRESENTATIVE_FULL_NAME, caseData.getCicCase().getRepresentativeFullName());

//        DocumentInfo generateTemplate = docAssembleyService.renderDocument(templateContent,caseId,
//            null,templateId,LanguagePreference.ENGLISH,filename);



//        public DocumentInfo renderDocument(final Map<String, Object> templateContent,
//        final Long caseId,
//        final String authorisation,
//        final String templateId,
//        final LanguagePreference languagePreference,
//        final String filename)

//        Document generalOrderDocument = caseDataDocumentService.renderDocument(
//            //  finalDecisionTemplateContent.apply(caseData, caseId)
//            templateContent,
//            caseId,templateId,
//            //finalDecision.getFinalDecisionTemplate().getId(),
//            LanguagePreference.ENGLISH,
//            filename
//        );

        Document generalOrderDocument = caseDataDocumentService.renderDocument(
            finalDecisionTemplateContent.apply(caseData, caseId),
            caseId,
            caseData.getCicCase().getAnOrderTemplates().getId(),
            // finalDecision.getFinalDecisionTemplate().getId(),
            LanguagePreference.ENGLISH,
            filename
        );


//        final Map<String, Object> templateContent,
//        final Long caseId,
//        final String authorisation,
//        final String templateId,
//        final LanguagePreference languagePreference,
//        final String filename

        caseData.getCicCase().setAnOrderTemplates(template);
        finalOrderIssu.setOrderTemplateIssued(generalOrderDocument);
        // finalDecision.setFinalDecisionDraft(generateTemplate);
        //caseData.getCicCase().setAnOrderTemplates(template);
        //finalDecision.setFinalDecisionDraft(generateTemplate);
        //caseData.setCaseIssueFinalDecision(finalDecision);

        return AboutToStartOrSubmitResponse.<CaseData, State>builder()
            .data(caseData)
            .build();
    }


}

