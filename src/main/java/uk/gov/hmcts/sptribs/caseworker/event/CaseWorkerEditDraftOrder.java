package uk.gov.hmcts.sptribs.caseworker.event;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.ccd.sdk.api.CCDConfig;
import uk.gov.hmcts.ccd.sdk.api.CaseDetails;
import uk.gov.hmcts.ccd.sdk.api.ConfigBuilder;
import uk.gov.hmcts.ccd.sdk.api.callback.AboutToStartOrSubmitResponse;
import uk.gov.hmcts.reform.ccd.client.model.SubmittedCallbackResponse;
import uk.gov.hmcts.sptribs.caseworker.model.DraftOrderCIC;
import uk.gov.hmcts.sptribs.ciccase.model.CaseData;
import uk.gov.hmcts.sptribs.ciccase.model.LanguagePreference;
import uk.gov.hmcts.sptribs.ciccase.model.State;
import uk.gov.hmcts.sptribs.ciccase.model.UserRole;
import uk.gov.hmcts.sptribs.common.ccd.PageBuilder;
import uk.gov.hmcts.sptribs.common.config.DocmosisTemplatesConfig;
import uk.gov.hmcts.sptribs.document.CaseDataDocumentService;
import uk.gov.hmcts.sptribs.document.DocAssemblyService;
import uk.gov.hmcts.sptribs.document.content.DraftEditTemplateContentCIC;
import uk.gov.hmcts.sptribs.idam.IdamService;

import java.util.HashMap;
import java.util.Map;

import static java.lang.String.format;
import static uk.gov.hmcts.sptribs.ciccase.model.State.POST_SUBMISSION_STATES_WITH_WITHDRAWN_AND_REJECTED;
import static uk.gov.hmcts.sptribs.ciccase.model.UserRole.COURT_ADMIN_CIC;
import static uk.gov.hmcts.sptribs.ciccase.model.UserRole.SOLICITOR;
import static uk.gov.hmcts.sptribs.ciccase.model.UserRole.SUPER_USER;
import static uk.gov.hmcts.sptribs.ciccase.model.access.Permissions.CREATE_READ_UPDATE_DELETE;

import static uk.gov.hmcts.sptribs.document.model.DocumentType.GENERAL_LETTER;
import static uk.gov.hmcts.sptribs.document.DocumentConstants.GENERAL_LETTER_TEMPLATE_ID;
import static uk.gov.hmcts.sptribs.document.DocumentConstants.CONDITIONAL_ORDER_PRONOUNCED_DOCUMENT_NAME;

@Component
@Slf4j
public class CaseWorkerEditDraftOrder implements CCDConfig<CaseData, State, UserRole> {

    public static final String CASEWORKER_EDIT_DRAFT_ORDER = "caseworker-edit-draft-order";
    public static final String CIC_CASE_FULL_NAME = "cicCaseFullName";

    @Autowired
    private CaseDataDocumentService caseDataDocumentService;
    @Autowired
    private DraftEditTemplateContentCIC templateContent;
    @Autowired
    private DocAssemblyService docAssemblyService;
    @Autowired
    private IdamService idamService;
    @Autowired
    private DocmosisTemplatesConfig docmosisTemplatesConfig;
    @Override
    public void configure(final ConfigBuilder<CaseData, State, UserRole> configBuilder) {
        PageBuilder pageBuilder = new PageBuilder(
            configBuilder
                .event(CASEWORKER_EDIT_DRAFT_ORDER)
                .forStates(POST_SUBMISSION_STATES_WITH_WITHDRAWN_AND_REJECTED)
                .name("Edit draft order")
                .showSummary()
                .aboutToSubmitCallback(this::aboutToSubmit)
                .submittedCallback(this::draftCreated)
                .showEventNotes()
                .grant(CREATE_READ_UPDATE_DELETE, COURT_ADMIN_CIC, SUPER_USER)
                .grantHistoryOnly(SOLICITOR));
        editDraftOrder(pageBuilder);
        previewOrder(pageBuilder);


    }


    private void editDraftOrder(PageBuilder pageBuilder) {
        pageBuilder
            .page("editDraftOrder")
            .pageLabel("Edit order")
            .label("editableDraft", "Draft to be edited")
            .complex(CaseData::getDraftOrderCIC)
            .mandatory(DraftOrderCIC::getOrderTemplate, "")
            .label("edit", "<hr>" + "\n<h3>Header</h3>" + "\n<h4>First tier tribunal Health lists</h4>\n\n" +
                "<h3>IN THE MATTER OF THE NATIONAL HEALTH SERVICES (PERFORMERS LISTS)(ENGLAND) REGULATIONS 2013</h2>\n\n"
                + "&lt; &lt; CaseNumber &gt; &gt; \n"
                + "\nBETWEEN\n"
                + "\n&lt; &lt; SubjectName &gt; &gt; \n"
                + "\nApplicant\n" + "\n<RepresentativeName>" + "\nRespondent<hr>"
                + "\n<h3>Main content</h3>\n\n ")
            .optional(DraftOrderCIC::getMainContentToBeEdited)
            .label("footer", "<h2>Footer</h2>\n First-tier Tribunal (Health,Education and Social Care)\n\n" +
                "Date Issued &lt; &lt;  SaveDate &gt; &gt;")

            .done();
    }

    private void previewOrder(PageBuilder pageBuilder) {

        pageBuilder
            .page("previewOrder", this::aboutToSubmit)
            .pageLabel("Preview order")
            .label("previewDraft", " Order preview")
            .label("make Changes", "To make changes, choose 'Edit order'\n\n"
                + "If you are happy , continue to the next screen.")
            .done();
    }



    public AboutToStartOrSubmitResponse<CaseData, State> aboutToSubmit(
        final CaseDetails<CaseData, State> details,
        final CaseDetails<CaseData, State> beforeDetails
    ) {
        final String authorisation = idamService.retrieveSystemUpdateUserDetails().getAuthToken();
        final String templateId = "GENERAL_DIRECTIONS";  //ST-CIC-STD-ENG-CIC6_General_Directions.docx
        final String filename = "ST-CIC-STD-ENG-CIC_General_Directions";
        var caseData = details.getData();
        var caseId = details.getId();

        log.info("Sending document request for template : {} case id: {}", filename, caseId);
      final String caseDataMap2 = docmosisTemplatesConfig.getTemplateVars().put("cicCaseFullName", CIC_CASE_FULL_NAME);


        Map<String, Object> caseDataMap = new HashMap<>();

      //  caseDataMap =  templateContent.apply(caseData, caseId);
        caseDataMap.put("cicCaseFullName", CIC_CASE_FULL_NAME);
        caseDataMap.put("cicCaseFullName", CIC_CASE_FULL_NAME);
//        caseDataMap.put("petitionerMiddleName", TEST_MIDDLE_NAME);
//        caseDataMap.put("petitionerLastName", TEST_LAST_NAME);
//        caseDataMap.put("divorceOrDissolution", DivorceOrDissolution.DIVORCE);
//        caseDataMap.put("petitionerEmail", TEST_USER_EMAIL);


        final var documentInfo = docAssemblyService.renderDocument(
            caseDataMap,
            caseId,
            authorisation,
            templateId,
            LanguagePreference.ENGLISH,
            filename
        );
        System.out.println("DDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDOOOOOOOOOOOOOOOOCCCCCCCCCc------------------------++++++++++++++++++++++++++++++++"+ documentInfo);


//         caseDataDocumentService.renderDocumentAndUpdateCaseData(
//            caseData,
//            GENERAL_LETTER,
//            templateContent.apply(caseData, caseId),
//            caseId,
//            GENERAL_LETTER_TEMPLATE_ID,
//            LanguagePreference.ENGLISH,
//            CONDITIONAL_ORDER_PRONOUNCED_DOCUMENT_NAME
//        );
//
//        System.out.println("DDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDOOOOOOOOOOOOOOOOCCCCCCCCCc------------------------++++++++++++++++++++++++++++++++");
        return AboutToStartOrSubmitResponse.<CaseData, State>builder()
            .data(caseData)
            .state(State.NewCaseReceived)
            .build();

    }

    public SubmittedCallbackResponse draftCreated(CaseDetails<CaseData, State> details,
                                                  CaseDetails<CaseData, State> beforeDetails) {
        return SubmittedCallbackResponse.builder()
            .confirmationHeader("When you have finished drafting this order,you can send it to parties in this case.")
            .build();
    }
}
