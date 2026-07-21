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

import static java.lang.String.format;
import static org.springframework.http.HttpHeaders.AUTHORIZATION;
import static uk.gov.hmcts.sptribs.caseworker.util.EventConstants.DOUBLE_HYPHEN;
import static uk.gov.hmcts.sptribs.ciccase.model.OrderTemplate.CIC3_RULE_27;
import static uk.gov.hmcts.sptribs.ciccase.model.State.Draft;
import static uk.gov.hmcts.sptribs.ciccase.model.UserRole.SUPER_USER;
import static uk.gov.hmcts.sptribs.ciccase.model.UserRole.SYSTEM_UPDATE;
import static uk.gov.hmcts.sptribs.ciccase.model.access.Permissions.CREATE_READ_UPDATE_DELETE;
import static uk.gov.hmcts.sptribs.constants.CommonConstants.ST_CIC_WA_CASE_BASE_LOCATION;
import static uk.gov.hmcts.sptribs.constants.CommonConstants.ST_CIC_WA_CASE_MANAGEMENT_CATEGORY;
import static uk.gov.hmcts.sptribs.constants.CommonConstants.ST_CIC_WA_CASE_REGION;

@RequiredArgsConstructor
@Component
@Slf4j
public class SystemCreateTestCases implements CCDConfig<CaseData, State, UserRole> {
    public static final String SYSTEM_CREATE_TEST_CASES = "create-test-cases";
    private final DocumentsService documentsService;
    private static final String TEST_CASE_DATA_FILE = "classpath:data/st_cic_test_case.json";
    private static final ClassPathResource SAMPLE_PDF_FILE_RESOURCE =  new ClassPathResource("data/sample_file.pdf");
    private final SimpleDateFormat simpleDateFormat = new SimpleDateFormat("dd-MM-yyyy HH:mm:ss", Locale.ENGLISH);


    private final ObjectMapper objectMapper;
    private final CcdSupplementaryDataService ccdSupplementaryDataService;
    private final AppsConfig appsConfig;
    private final AuthTokenGenerator authTokenGenerator;
    private final HttpServletRequest httpServletRequest;
    private final CaseDocumentClientApi caseDocumentClientApi;
    private final CaseDataDocumentService caseDataDocumentService;
    private final PreviewDraftOrderTemplateContent previewDraftOrderTemplateContent;
    private final HttpServletRequest request;

    @Override
    public void configure(ConfigBuilder<CaseData, State, UserRole> configBuilder) {
        configBuilder
            .event(SYSTEM_CREATE_TEST_CASES)
            .initialState(Draft)
            .name("System: Create Test Cases")
            .description("Create Test Cases")
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
        uploadTestDocumentAndUpdateCaseData(caseData);
        addOrderDocument(caseData);
        addDraftOrderDocument(caseData, details.getId());
        addDecisionDocument(caseData);
        addFinalDecisionDocument(caseData);
        addDocumentManagementDocument(caseData);

        caseData.setHyphenatedCaseRef(caseData.formatCaseRef(details.getId()));
        setDefaultCaseDetails(caseData);

        return AboutToStartOrSubmitResponse.<CaseData, State>builder()
            .data(caseData)
            .state(Draft)
            .build();
    }

    public SubmittedCallbackResponse submitted(CaseDetails<CaseData, State> details,
                                               CaseDetails<CaseData, State> beforeDetails) {

        final CaseData caseData = details.getData();
        final String caseReference = caseData.getHyphenatedCaseRef();

        setSupplementaryData(details.getId());

        if (caseData.getCicCase().getApplicantDocumentsUploaded() != null) {
            for (ListValue<CaseworkerCICDocument> document : caseData.getCicCase().getApplicantDocumentsUploaded()) {
                try {
                //                    documentsService.buildAndSaveNewDocumentEntity(
                //                        document.getValue().getDocumentLink(),
                //                        details.getId(),
                //                        document.getValue().getDocumentCategory(),
                //                        CaseDocumentType.DOCUMENT_MANAGEMENT
                //                    );
                } catch (RuntimeException e) {
                    log.error("Saving applicant documents failed with exception : {}", e.getMessage());
                    return SubmittedCallbackResponse.builder()
                        .confirmationHeader(format("# Create case notification failed %n## Please resend the notification"))
                        .build();
                }
            }
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

    private void uploadTestDocumentAndUpdateCaseData(CaseData caseData) {
        final UploadResponse uploadResponse = uploadApplicantDocument();

        if (uploadResponse != null) {
            final uk.gov.hmcts.sptribs.cdam.model.Document cdamUploadedDocument = uploadResponse.getDocuments().getFirst();
            log.info("Document uploaded successfully. href: {}", cdamUploadedDocument.links.self.href);

            CaseworkerCICDocument caseworkerCICDocument = convertCdamDocumentToCaseworkerCICDocument(cdamUploadedDocument);
            final ListValue<CaseworkerCICDocument> testDocumentListValue = new ListValue<>();
            testDocumentListValue.setId(UUID.randomUUID().toString());
            testDocumentListValue.setValue(caseworkerCICDocument);

            caseData.getCicCase().setApplicantDocumentsUploaded(List.of(testDocumentListValue));
        }
    }

    private UploadResponse uploadApplicantDocument() {
        final List<AppsConfig.AppsDetails> appDetails = appsConfig.getApps();
        if (!appDetails.isEmpty() && appDetails.getFirst() != null) {
            final String caseType = appsConfig.getApps().getFirst().getCaseType();
            final String jurisdiction = appsConfig.getApps().getFirst().getJurisdiction();
            try {
                final InMemoryMultipartFile inMemoryMultipartFile =
                    new InMemoryMultipartFile("sample_file.pdf", SAMPLE_PDF_FILE_RESOURCE.getContentAsByteArray());

                final DocumentUploadRequest documentUploadRequest =
                    new DocumentUploadRequest(Classification.RESTRICTED.toString(),
                        caseType,
                        jurisdiction,
                        List.of(inMemoryMultipartFile));

                final String serviceToken = authTokenGenerator.generate();
                final String authorizationHeader = httpServletRequest.getHeader(AUTHORIZATION);

                return this.caseDocumentClientApi.uploadDocuments(authorizationHeader, serviceToken, documentUploadRequest);
            } catch (IOException ioException) {
                log.error("Failed to upload test document due to {}", ioException.toString());
            }
        }
        return null;
    }

    private void addOrderDocument(CaseData caseData) {
        final UploadResponse uploadResponse = uploadApplicantDocument();

        if (uploadResponse != null) {
            final uk.gov.hmcts.sptribs.cdam.model.Document cdamDocument = uploadResponse.getDocuments().getFirst();

            final Document document = Document.builder()
                .url(cdamDocument.links.self.href)
                .filename(cdamDocument.originalDocumentName)
                .categoryId("TD")
                .binaryUrl(cdamDocument.links.binary.href)
                .build();

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
    }

    private void addDraftOrderDocument(CaseData caseData,Long caseId) {

        Calendar cal = Calendar.getInstance();
        String date = simpleDateFormat.format(cal.getTime());

        final String filename = "Order" + DOUBLE_HYPHEN + "[" + "TestSubject" + "]" + DOUBLE_HYPHEN + date;

        Document generalOrderDocument = caseDataDocumentService.renderDocument(
            previewDraftOrderTemplateContent.apply(caseData, caseId),
            caseId,
            CIC3_RULE_27.getId(),
            LanguagePreference.ENGLISH,
            filename,
            request
        );

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

    private void addDecisionDocument(CaseData caseData) {
        final UploadResponse uploadResponse = uploadApplicantDocument();


        if (uploadResponse != null) {
            final uk.gov.hmcts.sptribs.cdam.model.Document cdamDocument = uploadResponse.getDocuments().getFirst();

            final Document document = Document.builder()
                .url(cdamDocument.links.self.href)
                .filename(cdamDocument.originalDocumentName)
                .categoryId("TD")
                .binaryUrl(cdamDocument.links.binary.href)
                .build();

            CICDocument doc = CICDocument.builder()
                .documentEmailContent("Test Decision")
                .documentLink(document)
                .build();

            caseData.getCaseIssueDecision().setDecisionDocument(doc);
        }
    }

    private void addFinalDecisionDocument(CaseData caseData) {
        final UploadResponse uploadResponse = uploadApplicantDocument();

        if (uploadResponse != null) {
            final uk.gov.hmcts.sptribs.cdam.model.Document cdamDocument = uploadResponse.getDocuments().getFirst();

            final Document document = Document.builder()
                .url(cdamDocument.links.self.href)
                .filename(cdamDocument.originalDocumentName)
                .categoryId("TD")
                .binaryUrl(cdamDocument.links.binary.href)
                .build();

            caseData.getCaseIssueFinalDecision().setFinalDecisionDraft(document);
        }
    }

    private void addDocumentManagementDocument(CaseData caseData) {
        final UploadResponse uploadResponse = uploadApplicantDocument();

        if (uploadResponse != null) {
            final uk.gov.hmcts.sptribs.cdam.model.Document cdamDocument = uploadResponse.getDocuments().getFirst();

            final Document document = Document.builder()
                .url(cdamDocument.links.self.href)
                .filename(cdamDocument.originalDocumentName)
                .categoryId("TD")
                .binaryUrl(cdamDocument.links.binary.href)
                .build();

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
    }


    private CaseworkerCICDocument convertCdamDocumentToCaseworkerCICDocument(uk.gov.hmcts.sptribs.cdam.model.Document cdamDocument) {
        final Document uploadedDocument = Document.builder()
            .url(cdamDocument.links.self.href)
            .filename(cdamDocument.originalDocumentName)
            .categoryId("A")
            .binaryUrl(cdamDocument.links.binary.href)
            .build();
        return  CaseworkerCICDocument.builder()
            .documentLink(uploadedDocument)
            .documentCategory(DocumentType.APPLICATION_FORM)
            .documentEmailContent("This is a test document uploaded during create case journey")
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
