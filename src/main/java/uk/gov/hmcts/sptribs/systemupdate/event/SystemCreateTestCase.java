package uk.gov.hmcts.sptribs.systemupdate.event;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.IOUtils;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.DefaultResourceLoader;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;
import uk.gov.hmcts.ccd.sdk.api.CCDConfig;
import uk.gov.hmcts.ccd.sdk.api.CaseDetails;
import uk.gov.hmcts.ccd.sdk.api.ConfigBuilder;
import uk.gov.hmcts.ccd.sdk.api.callback.AboutToStartOrSubmitResponse;
import uk.gov.hmcts.ccd.sdk.type.Document;
import uk.gov.hmcts.ccd.sdk.type.DynamicList;
import uk.gov.hmcts.ccd.sdk.type.DynamicListElement;
import uk.gov.hmcts.ccd.sdk.type.ListValue;
import uk.gov.hmcts.reform.authorisation.generators.AuthTokenGenerator;
import uk.gov.hmcts.reform.ccd.client.model.SubmittedCallbackResponse;
import uk.gov.hmcts.reform.ccd.document.am.model.Classification;
import uk.gov.hmcts.reform.ccd.document.am.model.DocumentUploadRequest;
import uk.gov.hmcts.reform.ccd.document.am.util.InMemoryMultipartFile;
import uk.gov.hmcts.sptribs.caseworker.model.CaseManagementLocation;
import uk.gov.hmcts.sptribs.caseworker.model.DraftOrderCIC;
import uk.gov.hmcts.sptribs.caseworker.model.Order;
import uk.gov.hmcts.sptribs.caseworker.model.YesNo;
import uk.gov.hmcts.sptribs.cdam.model.UploadResponse;
import uk.gov.hmcts.sptribs.ciccase.model.CaseData;
import uk.gov.hmcts.sptribs.ciccase.model.LanguagePreference;
import uk.gov.hmcts.sptribs.ciccase.model.State;
import uk.gov.hmcts.sptribs.ciccase.model.UserRole;
import uk.gov.hmcts.sptribs.common.config.AppsConfig;
import uk.gov.hmcts.sptribs.common.service.CcdSupplementaryDataService;
import uk.gov.hmcts.sptribs.document.CaseDataDocumentService;
import uk.gov.hmcts.sptribs.document.content.PreviewDraftOrderTemplateContent;
import uk.gov.hmcts.sptribs.document.model.CICDocument;
import uk.gov.hmcts.sptribs.document.model.CaseDocumentType;
import uk.gov.hmcts.sptribs.document.model.CaseworkerCICDocument;
import uk.gov.hmcts.sptribs.document.model.DocumentType;
import uk.gov.hmcts.sptribs.document.service.DocumentsService;
import uk.gov.hmcts.sptribs.services.cdam.CaseDocumentClientApi;

import java.io.IOException;
import java.nio.charset.Charset;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import static java.lang.String.format;
import static org.springframework.http.HttpHeaders.AUTHORIZATION;
import static uk.gov.hmcts.sptribs.caseworker.util.EventConstants.DOUBLE_HYPHEN;
import static uk.gov.hmcts.sptribs.caseworker.util.MessageUtil.handleDocumentException;
import static uk.gov.hmcts.sptribs.ciccase.model.OrderTemplate.CIC3_RULE_27;
import static uk.gov.hmcts.sptribs.ciccase.model.State.Draft;
import static uk.gov.hmcts.sptribs.ciccase.model.State.Submitted;
import static uk.gov.hmcts.sptribs.ciccase.model.UserRole.SUPER_USER;
import static uk.gov.hmcts.sptribs.ciccase.model.UserRole.SYSTEM_UPDATE;
import static uk.gov.hmcts.sptribs.ciccase.model.access.Permissions.CREATE_READ_UPDATE_DELETE;
import static uk.gov.hmcts.sptribs.constants.CommonConstants.ST_CIC_WA_CASE_BASE_LOCATION;
import static uk.gov.hmcts.sptribs.constants.CommonConstants.ST_CIC_WA_CASE_MANAGEMENT_CATEGORY;
import static uk.gov.hmcts.sptribs.constants.CommonConstants.ST_CIC_WA_CASE_REGION;

@RequiredArgsConstructor
@Component
@Slf4j
public class SystemCreateTestCase implements CCDConfig<CaseData, State, UserRole> {
    public static final String SYSTEM_CREATE_TEST_CASE = "system-create-test-case";
    private final DocumentsService documentsService;
    private static final String TEST_CASE_DATA_FILE = "classpath:data/st_cic_test_case.json";
    private static final ClassPathResource SAMPLE_PDF_FILE_RESOURCE =  new ClassPathResource("data/sample_file.pdf");
    private static final String TEST_DOCUMENT_ERROR = "Unable to create the test case documents. Please try again.";
    private static final String APPLICANT_DOCUMENT_FILENAME = "applicant-document.pdf";
    private static final String ORDER_DOCUMENT_FILENAME = "order-document.pdf";
    private static final String DECISION_DOCUMENT_FILENAME = "decision-document.pdf";
    private static final String FINAL_DECISION_DOCUMENT_FILENAME = "final-decision-document.pdf";
    private static final String DOCUMENT_MANAGEMENT_FILENAME = "document-management-document.pdf";
    private static final List<String> TEST_DOCUMENT_FILENAMES = List.of(
        APPLICANT_DOCUMENT_FILENAME,
        ORDER_DOCUMENT_FILENAME,
        DECISION_DOCUMENT_FILENAME,
        FINAL_DECISION_DOCUMENT_FILENAME,
        DOCUMENT_MANAGEMENT_FILENAME
    );
    private final SimpleDateFormat simpleDateFormat = new SimpleDateFormat("dd-MM-yyyy HH:mm:ss", Locale.ENGLISH);


    private final ObjectMapper objectMapper;
    private final CcdSupplementaryDataService ccdSupplementaryDataService;
    private final AppsConfig appsConfig;
    private final AuthTokenGenerator authTokenGenerator;
    private final HttpServletRequest httpServletRequest;
    private final CaseDocumentClientApi caseDocumentClientApi;
    private final CaseDataDocumentService caseDataDocumentService;
    private final PreviewDraftOrderTemplateContent previewDraftOrderTemplateContent;

    private long retryDelayMs = 2000L;

    void setRetryDelayMs(long retryDelayMs) {
        this.retryDelayMs = retryDelayMs;
    }

    @Override
    public void configure(ConfigBuilder<CaseData, State, UserRole> configBuilder) {
        configBuilder
            .event(SYSTEM_CREATE_TEST_CASE)
            .initialState(Draft)
            .name("System: Create Test Case")
            .description("Create Test Case")
            .aboutToSubmitCallback(this::aboutToSubmit)
            .submittedCallback(this::submitted)
            .grant(CREATE_READ_UPDATE_DELETE, SYSTEM_UPDATE, SUPER_USER);
    }


    @SneakyThrows
    public AboutToStartOrSubmitResponse<CaseData, State> aboutToSubmit(CaseDetails<CaseData, State> details,
                                                                       CaseDetails<CaseData, State> beforeDetails) {

        final DefaultResourceLoader resourceLoader = new DefaultResourceLoader();
        final String json = IOUtils.toString(
            resourceLoader.getResource(TEST_CASE_DATA_FILE).getInputStream(),
            Charset.defaultCharset()
        );
        final CaseData caseData = objectMapper.readValue(json, CaseData.class);
        List<String> errors = new ArrayList<>();
        Long caseId = details.getId();
        int maxAttempts = 3;
        for (int attempt = 1; attempt <= maxAttempts; attempt++) {
            try {
                List<uk.gov.hmcts.sptribs.cdam.model.Document> uploadedDocuments = uploadTestDocuments();
                addApplicantDocument(caseData, findDocument(uploadedDocuments, APPLICANT_DOCUMENT_FILENAME));
                addOrderDocument(caseData, findDocument(uploadedDocuments, ORDER_DOCUMENT_FILENAME));
                addDraftOrderDocument(caseData, caseId);
                addDecisionDocument(caseData, findDocument(uploadedDocuments, DECISION_DOCUMENT_FILENAME));
                addFinalDecisionDocument(caseData, findDocument(uploadedDocuments, FINAL_DECISION_DOCUMENT_FILENAME));
                addDocumentManagementDocument(caseData, findDocument(uploadedDocuments, DOCUMENT_MANAGEMENT_FILENAME));
                break;
            } catch (RuntimeException exception) {
                if (attempt == maxAttempts) {
                    log.error("Failed to create system test case documents after {} attempts: {}",
                        maxAttempts, exception.getMessage(), exception);
                    errors.add(TEST_DOCUMENT_ERROR);
                } else {
                    log.warn("Attempt {} to create system test case documents failed: {}. Retrying...",
                        attempt, exception.getMessage());
                    try {
                        TimeUnit.MILLISECONDS.sleep(retryDelayMs * attempt);
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                        log.error("Thread interrupted during retry wait", e);
                        errors.add(TEST_DOCUMENT_ERROR);
                        break;
                    }
                }
            }
        }

        caseData.setHyphenatedCaseRef(caseData.formatCaseRef(caseId));
        setDefaultCaseDetails(caseData);

        return AboutToStartOrSubmitResponse.<CaseData, State>builder()
            .data(caseData)
            .state(Submitted)
            .errors(errors)
            .build();
    }

    public SubmittedCallbackResponse submitted(CaseDetails<CaseData, State> details,
                                               CaseDetails<CaseData, State> beforeDetails) {

        final CaseData caseData = details.getData();
        final String caseReference = caseData.getHyphenatedCaseRef();

        List<String> errors = new ArrayList<>();
        saveTestCaseDocuments(caseData, details.getId(), errors);
        setSupplementaryData(details.getId());

        if (!errors.isEmpty()) {
            return SubmittedCallbackResponse.builder()
                .confirmationHeader(format("# Case created%n## Some document metadata could not be saved"))
                .build();
        }

        return SubmittedCallbackResponse.builder()
            .confirmationHeader(format("# Case Created %n## Case reference number: %n## %s", caseReference))
            .build();
    }

    private void setDefaultCaseDetails(CaseData data) {
        CaseManagementLocation caseManagementLocation = new CaseManagementLocation(ST_CIC_WA_CASE_BASE_LOCATION, ST_CIC_WA_CASE_REGION);
        log.info("Case Management base location {}, region {}",
            caseManagementLocation.getBaseLocation(), caseManagementLocation.getRegion());

        CaseManagementLocation caseManagementLocation1 = CaseManagementLocation
            .builder()
            .baseLocation(ST_CIC_WA_CASE_BASE_LOCATION)
            .region(ST_CIC_WA_CASE_REGION)
            .build();
        log.info("Case Management (builder) base location {}, region {}",
            caseManagementLocation1.getBaseLocation(), caseManagementLocation1.getRegion());

        data.setNewBundleOrderEnabled(YesNo.YES);
        log.info("New Bundle Order Enabled {}", data.getNewBundleOrderEnabled());

        data.setCaseManagementLocation(
            caseManagementLocation
        );
        log.info("Case Management (data) base location {}, region {}",
            data.getCaseManagementLocation().getBaseLocation(), data.getCaseManagementLocation().getRegion());

        DynamicListElement caseManagementCategory = new DynamicListElement(
            UUID.randomUUID(), ST_CIC_WA_CASE_MANAGEMENT_CATEGORY);
        data.setCaseManagementCategory(
            DynamicList
                .builder()
                .listItems(List.of(caseManagementCategory))
                .value(caseManagementCategory)
                .build()
        );
    }

    private void addApplicantDocument(CaseData caseData, uk.gov.hmcts.sptribs.cdam.model.Document cdamDocument) {
        CaseworkerCICDocument caseworkerCICDocument = convertCdamDocumentToCaseworkerCICDocument(cdamDocument);
        final ListValue<CaseworkerCICDocument> testDocumentListValue = new ListValue<>();
        testDocumentListValue.setId(UUID.randomUUID().toString());
        testDocumentListValue.setValue(caseworkerCICDocument);

        caseData.getCicCase().setApplicantDocumentsUploaded(List.of(testDocumentListValue));
    }

    private List<uk.gov.hmcts.sptribs.cdam.model.Document> uploadTestDocuments() {
        final List<AppsConfig.AppsDetails> appDetails = appsConfig.getApps();
        if (appDetails == null || appDetails.isEmpty() || appDetails.getFirst() == null) {
            throw new IllegalStateException("No application configuration is available for document upload");
        }

        final String caseType = appDetails.getFirst().getCaseType();
        final String jurisdiction = appDetails.getFirst().getJurisdiction();
        final List<MultipartFile> files = new ArrayList<>();
        try {
            byte[] fileContent = SAMPLE_PDF_FILE_RESOURCE.getContentAsByteArray();
            for (String filename : TEST_DOCUMENT_FILENAMES) {
                files.add(new InMemoryMultipartFile(filename, fileContent));
            }
        } catch (IOException exception) {
            throw new IllegalStateException("Unable to read the sample test document", exception);
        }

        final DocumentUploadRequest documentUploadRequest = new DocumentUploadRequest(
            Classification.RESTRICTED.toString(), caseType, jurisdiction, files);
        final String serviceToken = authTokenGenerator.generate();
        final String authorizationHeader = httpServletRequest.getHeader(AUTHORIZATION);
        UploadResponse uploadResponse = caseDocumentClientApi.uploadDocuments(
            authorizationHeader, serviceToken, documentUploadRequest);

        if (uploadResponse == null || uploadResponse.getDocuments() == null) {
            throw new IllegalStateException("Document upload returned no documents");
        }
        uploadResponse.getDocuments().forEach(this::validateDocument);
        return uploadResponse.getDocuments();
    }

    private uk.gov.hmcts.sptribs.cdam.model.Document findDocument(
        List<uk.gov.hmcts.sptribs.cdam.model.Document> documents, String filename) {

        return documents.stream()
            .filter(document -> filename.equals(document.originalDocumentName))
            .findFirst()
            .orElseThrow(() -> new IllegalStateException("Document upload did not return " + filename));
    }

    private void validateDocument(uk.gov.hmcts.sptribs.cdam.model.Document document) {
        if (document == null
            || document.originalDocumentName == null
            || document.originalDocumentName.isBlank()
            || document.links == null
            || document.links.self == null
            || document.links.self.href == null
            || document.links.self.href.isBlank()
            || document.links.binary == null
            || document.links.binary.href == null
            || document.links.binary.href.isBlank()) {
            throw new IllegalStateException("Document upload returned an invalid document");
        }
    }

    private void addOrderDocument(CaseData caseData, uk.gov.hmcts.sptribs.cdam.model.Document cdamDocument) {
        final Document document = convertCdamDocument(cdamDocument, "TD");

        final DraftOrderCIC draftOrderCIC = DraftOrderCIC.builder()
            .templateGeneratedDocument(document)
            .build();

        final Order order = Order.builder()
            .draftOrder(draftOrderCIC)
            .orderSentDate(LocalDate.now())
            .build();

        final ListValue<Order> orderListValue = new ListValue<>();
        orderListValue.setId(UUID.randomUUID().toString());
        orderListValue.setValue(order);

        caseData.getCicCase().setOrderList(List.of(orderListValue));
    }

    private void addDraftOrderDocument(CaseData caseData, Long caseId) {

        Calendar cal = Calendar.getInstance();
        String date = simpleDateFormat.format(cal.getTime());

        final String filename = "Order" + DOUBLE_HYPHEN + "[" + "TestSubject" + "]" + DOUBLE_HYPHEN + date;

        Document generalOrderDocument = caseDataDocumentService.renderDocument(
            previewDraftOrderTemplateContent.apply(caseData, caseId),
            caseId,
            CIC3_RULE_27.getId(),
            LanguagePreference.ENGLISH,
            filename,
            httpServletRequest
        );
        validateAssembledDocument(generalOrderDocument);

        DraftOrderCIC draftOrderCIC = DraftOrderCIC.builder()
            .draftOrderContentCIC(caseData.getDraftOrderContentCIC())
            .templateGeneratedDocument(generalOrderDocument)
            .build();

        final List<ListValue<DraftOrderCIC>> listValues = new ArrayList<>();

        final ListValue<DraftOrderCIC> listValue = ListValue
            .<DraftOrderCIC>builder()
            .id("1")
            .value(draftOrderCIC)
            .build();

        listValues.add(listValue);

        caseData.getCicCase().setDraftOrderCICList(listValues);
    }

    private void addDecisionDocument(CaseData caseData, uk.gov.hmcts.sptribs.cdam.model.Document cdamDocument) {
        final Document document = convertCdamDocument(cdamDocument, "TD");

        CICDocument doc = CICDocument.builder()
            .documentEmailContent("Test Decision")
            .documentLink(document)
            .build();

        caseData.getCaseIssueDecision().setDecisionDocument(doc);
    }

    private void addFinalDecisionDocument(CaseData caseData, uk.gov.hmcts.sptribs.cdam.model.Document cdamDocument) {
        caseData.getCaseIssueFinalDecision().setFinalDecisionDraft(convertCdamDocument(cdamDocument, "TD"));
    }

    private void addDocumentManagementDocument(CaseData caseData, uk.gov.hmcts.sptribs.cdam.model.Document cdamDocument) {
        final Document document = convertCdamDocument(cdamDocument, "TD");

        final CaseworkerCICDocument caseworkerCICDocument = CaseworkerCICDocument.builder()
            .documentLink(document)
            .documentCategory(DocumentType.LINKED_DOCS)
            .documentEmailContent("some email content")
            .build();

        List<ListValue<CaseworkerCICDocument>> documentList = new ArrayList<>();
        ListValue<CaseworkerCICDocument> caseworkerCICDocumentListValue = new ListValue<>();
        caseworkerCICDocumentListValue.setValue(caseworkerCICDocument);
        documentList.add(caseworkerCICDocumentListValue);

        caseData.getAllDocManagement().setCaseworkerCICDocument(documentList);
    }

    private void saveTestCaseDocuments(CaseData caseData, Long caseId, List<String> errors) {
        if (caseData.getCicCase().getApplicantDocumentsUploaded() != null) {
            caseData.getCicCase().getApplicantDocumentsUploaded().forEach(listValue -> {
                CaseworkerCICDocument document = listValue.getValue();
                saveDocumentToDocumentsTable(document.getDocumentLink(), caseId, document.getDocumentCategory(),
                    CaseDocumentType.APPLICATION, errors);
            });
        }
        if (caseData.getCicCase().getOrderList() != null) {
            caseData.getCicCase().getOrderList().forEach(listValue -> saveDocumentToDocumentsTable(
                listValue.getValue().getDraftOrder().getTemplateGeneratedDocument(), caseId,
                DocumentType.TRIBUNAL_DIRECTION, CaseDocumentType.ORDER, errors));
        }
        if (caseData.getCicCase().getDraftOrderCICList() != null) {
            caseData.getCicCase().getDraftOrderCICList().forEach(listValue -> saveDocumentToDocumentsTable(
                listValue.getValue().getTemplateGeneratedDocument(), caseId,
                DocumentType.TRIBUNAL_DIRECTION, CaseDocumentType.DRAFT_ORDER, errors));
        }
        if (caseData.getCaseIssueDecision().getDecisionDocument() != null) {
            saveDocumentToDocumentsTable(caseData.getCaseIssueDecision().getDecisionDocument().getDocumentLink(), caseId,
                DocumentType.TRIBUNAL_DIRECTION, CaseDocumentType.DECISION, errors);
        }
        saveDocumentToDocumentsTable(caseData.getCaseIssueFinalDecision().getFinalDecisionDraft(), caseId,
            DocumentType.TRIBUNAL_DIRECTION, CaseDocumentType.FINAL_DECISION, errors);
        if (caseData.getAllDocManagement().getCaseworkerCICDocument() != null) {
            caseData.getAllDocManagement().getCaseworkerCICDocument().forEach(listValue -> {
                CaseworkerCICDocument document = listValue.getValue();
                saveDocumentToDocumentsTable(document.getDocumentLink(), caseId, document.getDocumentCategory(),
                    CaseDocumentType.DOCUMENT_MANAGEMENT, errors);
            });
        }
    }

    private void validateAssembledDocument(Document document) {
        if (document == null
            || document.getFilename() == null
            || document.getFilename().isBlank()
            || document.getUrl() == null
            || document.getUrl().isBlank()
            || document.getBinaryUrl() == null
            || document.getBinaryUrl().isBlank()) {
            throw new IllegalStateException("Document assembly returned an invalid document");
        }
    }

    private void saveDocumentToDocumentsTable(Document document, Long caseId, DocumentType documentType,
                                              CaseDocumentType caseDocumentType, List<String> errors) {
        if (document == null) {
            return;
        }

        try {
            documentsService.buildAndSaveNewDocumentEntity(document, caseId, documentType, caseDocumentType);
        } catch (RuntimeException e) {
            errors.add(handleDocumentException(document, e.getMessage()));
        }
    }


    private CaseworkerCICDocument convertCdamDocumentToCaseworkerCICDocument(uk.gov.hmcts.sptribs.cdam.model.Document cdamDocument) {
        final Document uploadedDocument = convertCdamDocument(cdamDocument, "A");
        return  CaseworkerCICDocument.builder()
            .documentLink(uploadedDocument)
            .documentCategory(DocumentType.APPLICATION_FORM)
            .documentEmailContent("This is a test document uploaded during create case journey")
            .build();
    }

    private Document convertCdamDocument(uk.gov.hmcts.sptribs.cdam.model.Document cdamDocument, String categoryId) {
        return Document.builder()
            .url(cdamDocument.links.self.href)
            .filename(cdamDocument.originalDocumentName)
            .categoryId(categoryId)
            .binaryUrl(cdamDocument.links.binary.href)
            .build();
    }


    private void setSupplementaryData(Long caseId) {
        try {
            ccdSupplementaryDataService.submitSupplementaryDataToCcd(caseId.toString());
        } catch (Exception exception) {
            log.error("Unable to set Supplementary data with exception : {}", exception.getMessage());
        }
    }
}
