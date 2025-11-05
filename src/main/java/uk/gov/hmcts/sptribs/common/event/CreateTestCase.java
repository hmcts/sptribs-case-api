package uk.gov.hmcts.sptribs.common.event;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.IOUtils;
import org.springframework.beans.factory.annotation.Autowired;
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
import uk.gov.hmcts.sptribs.caseworker.model.YesNo;
import uk.gov.hmcts.sptribs.cdam.model.UploadResponse;
import uk.gov.hmcts.sptribs.ciccase.model.CaseData;
import uk.gov.hmcts.sptribs.ciccase.model.State;
import uk.gov.hmcts.sptribs.ciccase.model.UserRole;
import uk.gov.hmcts.sptribs.common.ccd.PageBuilder;
import uk.gov.hmcts.sptribs.common.config.AppsConfig;
import uk.gov.hmcts.sptribs.common.service.CcdSupplementaryDataService;
import uk.gov.hmcts.sptribs.document.model.CaseworkerCICDocument;
import uk.gov.hmcts.sptribs.document.model.DocumentType;
import uk.gov.hmcts.sptribs.services.cdam.CaseDocumentClientApi;

import java.io.IOException;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static java.lang.String.format;
import static java.lang.System.getenv;
import static org.springframework.http.HttpHeaders.AUTHORIZATION;
import static uk.gov.hmcts.sptribs.ciccase.model.State.Draft;
import static uk.gov.hmcts.sptribs.ciccase.model.UserRole.CASEWORKER;
import static uk.gov.hmcts.sptribs.ciccase.model.UserRole.CITIZEN;
import static uk.gov.hmcts.sptribs.ciccase.model.UserRole.ST_CIC_CASEWORKER;
import static uk.gov.hmcts.sptribs.ciccase.model.UserRole.ST_CIC_SENIOR_CASEWORKER;
import static uk.gov.hmcts.sptribs.ciccase.model.UserRole.SUPER_USER;
import static uk.gov.hmcts.sptribs.ciccase.model.UserRole.SYSTEM_UPDATE;
import static uk.gov.hmcts.sptribs.ciccase.model.access.Permissions.CREATE_READ_UPDATE;
import static uk.gov.hmcts.sptribs.constants.CommonConstants.ST_CIC_WA_CASE_BASE_LOCATION;
import static uk.gov.hmcts.sptribs.constants.CommonConstants.ST_CIC_WA_CASE_MANAGEMENT_CATEGORY;
import static uk.gov.hmcts.sptribs.constants.CommonConstants.ST_CIC_WA_CASE_REGION;

@Slf4j
@Component
public class CreateTestCase implements CCDConfig<CaseData, State, UserRole> {

    private static final String ENVIRONMENT_AAT = "aat";
    private static final String TEST_CREATE = "create-test-case";
    private static final String TEST_CASE_DATA_FILE = "classpath:data/st_cic_test_case.json";
    private static final ClassPathResource SAMPLE_PDF_FILE_RESOURCE =  new ClassPathResource("data/sample_file.pdf");

    private final ObjectMapper objectMapper;
    private final CcdSupplementaryDataService ccdSupplementaryDataService;
    private final AppsConfig appsConfig;
    private final AuthTokenGenerator authTokenGenerator;
    private final HttpServletRequest httpServletRequest;
    private final CaseDocumentClientApi caseDocumentClientApi;

    @Autowired
    public CreateTestCase(ObjectMapper objectMapper,
                          CcdSupplementaryDataService ccdSupplementaryDataService,
                          CaseDocumentClientApi caseDocumentClientApi,
                          AppsConfig appsConfig,
                          AuthTokenGenerator authTokenGenerator,
                          HttpServletRequest httpServletRequest) {
        this.objectMapper = objectMapper;
        this.ccdSupplementaryDataService = ccdSupplementaryDataService;
        this.caseDocumentClientApi = caseDocumentClientApi;
        this.appsConfig = appsConfig;
        this.authTokenGenerator = authTokenGenerator;
        this.httpServletRequest = httpServletRequest;
    }

    @Override
    public void configure(ConfigBuilder<CaseData, State, UserRole> configBuilder) {
        final List<UserRole> roles = new ArrayList<>();
        final String env = getenv().getOrDefault("S2S_URL_BASE", "aat");

        if (env.contains(ENVIRONMENT_AAT)) {
            roles.add(CASEWORKER);
            roles.add(ST_CIC_CASEWORKER);
            roles.add(ST_CIC_SENIOR_CASEWORKER);
        }

        new PageBuilder(configBuilder
            .event(TEST_CREATE)
            .initialState(Draft)
            .name("Create test case")
            .aboutToSubmitCallback(this::aboutToSubmit)
            .submittedCallback(this::submitted)
            .grant(CREATE_READ_UPDATE, roles.toArray(UserRole[]::new))
            .grantHistoryOnly(SUPER_USER, CASEWORKER, SYSTEM_UPDATE, CITIZEN))
            .page("Create test case")
            .mandatoryWithLabel(CaseData::getCaseStatus, "Case state")
            .done();
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
        caseData.setHyphenatedCaseRef(caseData.formatCaseRef(details.getId()));
        setDefaultCaseDetails(caseData);

        return AboutToStartOrSubmitResponse.<CaseData, State>builder()
            .data(caseData)
            .state(details.getData().getCaseStatus())
            .build();
    }

    public SubmittedCallbackResponse submitted(CaseDetails<CaseData, State> details,
                                               CaseDetails<CaseData, State> beforeDetails) {

        final CaseData caseData = details.getData();
        final String caseReference = caseData.getHyphenatedCaseRef();

        ccdSupplementaryDataService.submitSupplementaryDataToCcd(details.getId().toString());

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
}
