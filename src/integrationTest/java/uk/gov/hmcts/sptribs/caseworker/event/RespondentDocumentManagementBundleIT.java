package uk.gov.hmcts.sptribs.caseworker.event;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;
import uk.gov.hmcts.ccd.sdk.type.Document;
import uk.gov.hmcts.ccd.sdk.type.ListValue;
import uk.gov.hmcts.reform.authorisation.generators.AuthTokenGenerator;
import uk.gov.hmcts.sptribs.caseworker.model.DocumentManagement;
import uk.gov.hmcts.sptribs.caseworker.model.YesNo;
import uk.gov.hmcts.sptribs.ciccase.model.CaseData;
import uk.gov.hmcts.sptribs.common.config.WebMvcConfig;
import uk.gov.hmcts.sptribs.common.service.AuditEventService;
import uk.gov.hmcts.sptribs.document.bundling.client.BundleResponse;
import uk.gov.hmcts.sptribs.document.bundling.client.BundlingClient;
import uk.gov.hmcts.sptribs.document.bundling.model.Bundle;
import uk.gov.hmcts.sptribs.document.bundling.model.BundleCallback;
import uk.gov.hmcts.sptribs.document.bundling.model.BundleDocument;
import uk.gov.hmcts.sptribs.document.bundling.model.BundleFolder;
import uk.gov.hmcts.sptribs.document.bundling.model.BundlePaginationStyle;
import uk.gov.hmcts.sptribs.document.model.AbstractCaseworkerCICDocument;
import uk.gov.hmcts.sptribs.document.model.CaseworkerCICDocument;
import uk.gov.hmcts.sptribs.document.model.CaseworkerCICDocumentUpload;
import uk.gov.hmcts.sptribs.document.model.DocumentType;
import uk.gov.hmcts.sptribs.document.model.PageNumberFormat;
import uk.gov.hmcts.sptribs.testutil.IdamWireMock;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static uk.gov.hmcts.sptribs.caseworker.util.EventConstants.CASEWORKER_DOCUMENT_MANAGEMENT;
import static uk.gov.hmcts.sptribs.caseworker.util.EventConstants.CREATE_BUNDLE;
import static uk.gov.hmcts.sptribs.caseworker.util.EventConstants.RESPONDENT_DOCUMENT_MANAGEMENT;
import static uk.gov.hmcts.sptribs.document.bundling.BundlingConstants.CASE_BUNDLES;
import static uk.gov.hmcts.sptribs.document.bundling.BundlingConstants.DESCRIPTION;
import static uk.gov.hmcts.sptribs.document.bundling.BundlingConstants.DOCUMENTS;
import static uk.gov.hmcts.sptribs.document.bundling.BundlingConstants.FOLDERS;
import static uk.gov.hmcts.sptribs.document.bundling.BundlingConstants.ID;
import static uk.gov.hmcts.sptribs.document.bundling.BundlingConstants.NAME;
import static uk.gov.hmcts.sptribs.document.bundling.BundlingConstants.NEW;
import static uk.gov.hmcts.sptribs.document.bundling.BundlingConstants.PAGE_NUMBER_FORMAT;
import static uk.gov.hmcts.sptribs.document.bundling.BundlingConstants.PAGINATION_STYLE;
import static uk.gov.hmcts.sptribs.document.bundling.BundlingConstants.SORT_INDEX;
import static uk.gov.hmcts.sptribs.document.bundling.BundlingConstants.STITCHING_STATUS;
import static uk.gov.hmcts.sptribs.document.bundling.BundlingConstants.TITLE;
import static uk.gov.hmcts.sptribs.document.bundling.BundlingConstants.VALUE;
import static uk.gov.hmcts.sptribs.testutil.TestConstants.ABOUT_TO_SUBMIT_URL;
import static uk.gov.hmcts.sptribs.testutil.TestConstants.AUTHORIZATION;
import static uk.gov.hmcts.sptribs.testutil.TestConstants.SERVICE_AUTHORIZATION;
import static uk.gov.hmcts.sptribs.testutil.TestConstants.TEST_AUTHORIZATION_TOKEN;
import static uk.gov.hmcts.sptribs.testutil.TestDataHelper.callbackRequest;
import static uk.gov.hmcts.sptribs.testutil.TestDataHelper.caseData;

@ExtendWith(SpringExtension.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
@ContextConfiguration(initializers = {IdamWireMock.PropertiesInitializer.class})
class RespondentDocumentManagementBundleIT {

    private static final TypeReference<Map<String, Object>> RESPONSE_TYPE = new TypeReference<>() {};
    private static final Map<String, String> CATEGORY_TO_FOLDER = Map.ofEntries(
        Map.entry("A", "Application Documents"),
        Map.entry("B", "Police evidence"),
        Map.entry("C", "Medical records"),
        Map.entry("D", "Financial records"),
        Map.entry("E", "Other evidence"),
        Map.entry("L", "Linked documents"),
        Map.entry("S", "Witness Statements"),
        Map.entry("TD", "Direction - decision notices"),
        Map.entry("TG", "General evidence")
    );
    private static final String FURTHER_DOCUMENTS_FOLDER = "Further Documents";
    private static final List<String> FOLDER_ORDER = List.of(
        "Application Documents",
        "Police evidence",
        "Medical records",
        "Financial records",
        "Other evidence",
        "Linked documents",
        "Witness Statements",
        "Direction - decision notices",
        "General evidence",
        FURTHER_DOCUMENTS_FOLDER
    );

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private WebMvcConfig webMvcConfig;

    @MockitoBean
    private AuditEventService auditEventService;

    @MockitoBean
    private BundlingClient bundlingClient;

    @MockitoBean
    private AuthTokenGenerator authTokenGenerator;

    @BeforeAll
    static void setUp() {
        IdamWireMock.start();
    }

    @AfterAll
    static void tearDown() {
        IdamWireMock.stopAndReset();
    }

    @Test
    void shouldSeparateInitialAndFurtherDocumentsWhenCreatingBundle() throws Exception {
        when(auditEventService.hasCaseEvent(anyString(), eq(RESPONDENT_DOCUMENT_MANAGEMENT)))
            .thenReturn(false, true);

        CaseData caseData = prepareInitialCaseData();

        caseData.getNewDocManagement().setCaseworkerCICDocumentUpload(
            createUploads(
                new DocumentUploadSpec("initial-application.pdf", DocumentType.APPLICATION_FORM),
                new DocumentUploadSpec("initial-linked.pdf", DocumentType.LINKED_DOCS)
            )
        );

        CaseData afterFirstRun = triggerAboutToSubmit(RESPONDENT_DOCUMENT_MANAGEMENT, caseData);
        afterFirstRun.setNewBundleOrderEnabled(YesNo.YES);
        assertThat(afterFirstRun.getInitialCicaDocuments()).hasSize(2);

        afterFirstRun.getNewDocManagement().setCaseworkerCICDocumentUpload(
            createUploads(
                new DocumentUploadSpec("second-application.pdf", DocumentType.APPLICATION_FORM),
                new DocumentUploadSpec("second-linked.pdf", DocumentType.LINKED_DOCS)
            )
        );

        CaseData afterSecondRun = triggerAboutToSubmit(RESPONDENT_DOCUMENT_MANAGEMENT, afterFirstRun);
        afterSecondRun.setNewBundleOrderEnabled(YesNo.YES);
        assertThat(afterSecondRun.getFurtherUploadedDocuments()).hasSize(2);

        afterSecondRun.getNewDocManagement().setCaseworkerCICDocumentUpload(
            createUploads(new DocumentUploadSpec("caseworker-direction.pdf", DocumentType.TRIBUNAL_DIRECTION))
        );

        CaseData afterCaseworkerUpload = triggerAboutToSubmit(CASEWORKER_DOCUMENT_MANAGEMENT, afterSecondRun);
        afterCaseworkerUpload.setNewBundleOrderEnabled(YesNo.YES);
        assertThat(afterCaseworkerUpload.getFurtherUploadedDocuments()).hasSize(3);

        AtomicReference<List<String>> caseDocumentNames = new AtomicReference<>(List.of());
        AtomicReference<List<String>> furtherDocumentNames = new AtomicReference<>(List.of());

        when(authTokenGenerator.generate()).thenReturn(SERVICE_AUTHORIZATION);
        when(bundlingClient.createBundle(
            eq(SERVICE_AUTHORIZATION),
            eq(TEST_AUTHORIZATION_TOKEN),
            any(BundleCallback.class)
        )).thenAnswer(invocation -> {
            BundleCallback bundleCallback = invocation.getArgument(2);
            CaseData callbackCaseData = bundleCallback.getCaseDetails().getData();
            caseDocumentNames.set(extractFilenames(callbackCaseData.getCaseDocuments()));
            furtherDocumentNames.set(extractFilenames(callbackCaseData.getFurtherCaseDocuments()));
            return buildBundleResponse(callbackCaseData);
        });

        CaseData afterBundleCreation = triggerAboutToSubmit(CREATE_BUNDLE, afterCaseworkerUpload);

        assertThat(caseDocumentNames.get())
            .containsExactlyInAnyOrder("initial-application.pdf", "initial-linked.pdf");

        assertThat(furtherDocumentNames.get())
            .containsExactlyInAnyOrder("second-application.pdf", "second-linked.pdf", "caseworker-direction.pdf");

        verify(bundlingClient).createBundle(
            eq(SERVICE_AUTHORIZATION),
            eq(TEST_AUTHORIZATION_TOKEN),
            any(BundleCallback.class)
        );

        assertThat(afterBundleCreation.getCaseBundles()).isNotNull();
        Map<String, List<String>> folderDocumentNames = extractFolderDocumentNamesFromBundles(afterBundleCreation.getCaseBundles());

        assertThat(folderDocumentNames)
            .containsKeys("Application Documents", "Linked documents", "Further Documents");
        assertThat(folderDocumentNames.get("Application Documents"))
            .containsExactly("initial-application.pdf");
        assertThat(folderDocumentNames.get("Linked documents"))
            .containsExactly("initial-linked.pdf");
        assertThat(folderDocumentNames.get("Further Documents"))
            .containsExactlyInAnyOrder("second-application.pdf", "second-linked.pdf", "caseworker-direction.pdf")
            .doesNotContain("initial-application.pdf", "initial-linked.pdf");
    }

    private CaseData prepareInitialCaseData() {
        CaseData data = caseData();
        data.setNewBundleOrderEnabled(YesNo.YES);
        ensureDocumentManagementCollections(data);
        return data;
    }

    private CaseData triggerAboutToSubmit(String eventId, CaseData data) throws Exception {
        String response = mockMvc.perform(post(ABOUT_TO_SUBMIT_URL)
                .contentType(APPLICATION_JSON)
                .header(SERVICE_AUTHORIZATION, TEST_AUTHORIZATION_TOKEN)
                .header(AUTHORIZATION, TEST_AUTHORIZATION_TOKEN)
                .content(objectMapper.writeValueAsString(callbackRequest(data, eventId)))
                .accept(APPLICATION_JSON))
            .andExpect(status().isOk())
            .andReturn()
            .getResponse()
            .getContentAsString();

        Map<String, Object> responseMap = objectMapper.readValue(response, RESPONSE_TYPE);
        @SuppressWarnings("unchecked")
        Map<String, Object> dataMap = (Map<String, Object>) responseMap.get("data");

        CaseData updatedCaseData = objectMapper.convertValue(dataMap, CaseData.class);
        ensureDocumentManagementCollections(updatedCaseData);
        return updatedCaseData;
    }

    private void ensureDocumentManagementCollections(CaseData data) {
        if (data.getAllDocManagement() == null) {
            data.setAllDocManagement(new DocumentManagement());
        }
        if (data.getAllDocManagement().getCaseworkerCICDocument() == null) {
            data.getAllDocManagement().setCaseworkerCICDocument(new ArrayList<>());
        }
        if (data.getAllDocManagement().getCaseworkerCICDocumentUpload() == null) {
            data.getAllDocManagement().setCaseworkerCICDocumentUpload(new ArrayList<>());
        }
        if (data.getNewDocManagement() == null) {
            data.setNewDocManagement(new DocumentManagement());
        }
        if (data.getNewDocManagement().getCaseworkerCICDocument() == null) {
            data.getNewDocManagement().setCaseworkerCICDocument(new ArrayList<>());
        }
        if (data.getNewDocManagement().getCaseworkerCICDocumentUpload() == null) {
            data.getNewDocManagement().setCaseworkerCICDocumentUpload(new ArrayList<>());
        }
        if (data.getInitialCicaDocuments() == null) {
            data.setInitialCicaDocuments(new ArrayList<>());
        }
        if (data.getFurtherUploadedDocuments() == null) {
            data.setFurtherUploadedDocuments(new ArrayList<>());
        }
    }

    private BundleResponse buildBundleResponse(CaseData caseData) {
        LinkedHashMap<String, Object> data = new LinkedHashMap<>();
        data.put(CASE_BUNDLES, buildCaseBundlesResponse(caseData));
        return BundleResponse.builder()
            .data(data)
            .errors(List.of())
            .warnings(List.of())
            .build();
    }

    private List<LinkedHashMap<String, Object>> buildCaseBundlesResponse(CaseData caseData) {
        LinkedHashMap<String, Object> bundleValue = new LinkedHashMap<>();
        bundleValue.put(ID, "bundle-1");
        bundleValue.put(TITLE, "CIC Bundle");
        bundleValue.put(DESCRIPTION, "");
        bundleValue.put(STITCHING_STATUS, NEW);
        bundleValue.put(PAGINATION_STYLE, BundlePaginationStyle.off.name());
        bundleValue.put(PAGE_NUMBER_FORMAT, PageNumberFormat.numberOfPages.name());
        bundleValue.put(FOLDERS, buildResponseFolders(caseData));
        bundleValue.put(DOCUMENTS, List.of());

        LinkedHashMap<String, Object> bundleEntry = new LinkedHashMap<>();
        bundleEntry.put(VALUE, bundleValue);
        return List.of(bundleEntry);
    }

    private List<LinkedHashMap<String, Object>> buildResponseFolders(CaseData caseData) {
        Map<String, List<Map<String, Object>>> folderDocuments = new LinkedHashMap<>();
        FOLDER_ORDER.forEach(folder -> folderDocuments.put(folder, new ArrayList<>()));

        addDocumentsToFolderMap(folderDocuments, caseData.getCaseDocuments(), true);
        addDocumentsToFolderMap(folderDocuments, caseData.getFurtherCaseDocuments(), false);

        List<LinkedHashMap<String, Object>> folders = new ArrayList<>();
        for (int index = 0; index < FOLDER_ORDER.size(); index++) {
            String folderName = FOLDER_ORDER.get(index);
            List<Map<String, Object>> documents = folderDocuments.get(folderName);
            if (documents == null || documents.isEmpty()) {
                continue;
            }
            LinkedHashMap<String, Object> folderValue = new LinkedHashMap<>();
            folderValue.put(NAME, folderName);
            folderValue.put(SORT_INDEX, index + 1);
            folderValue.put(DOCUMENTS, documents);

            LinkedHashMap<String, Object> folderEntry = new LinkedHashMap<>();
            folderEntry.put(VALUE, folderValue);
            folders.add(folderEntry);
        }
        return folders;
    }

    private void addDocumentsToFolderMap(Map<String, List<Map<String, Object>>> folderDocuments,
                                         List<AbstractCaseworkerCICDocument<CaseworkerCICDocument>> documents,
                                         boolean groupByCategory) {
        if (documents == null) {
            return;
        }
        for (AbstractCaseworkerCICDocument<CaseworkerCICDocument> abstractDocument : documents) {
            if (abstractDocument == null || abstractDocument.getValue() == null) {
                continue;
            }
            CaseworkerCICDocument document = abstractDocument.getValue();
            String folderName = groupByCategory ? resolveFolder(document) : FURTHER_DOCUMENTS_FOLDER;
            List<Map<String, Object>> docs = folderDocuments.computeIfAbsent(folderName, key -> new ArrayList<>());

            LinkedHashMap<String, Object> documentValue = new LinkedHashMap<>();
            documentValue.put(NAME, document.getDocumentLink().getFilename());
            documentValue.put(DESCRIPTION, document.getDocumentEmailContent());
            documentValue.put(SORT_INDEX, docs.size() + 1);

            LinkedHashMap<String, Object> documentEntry = new LinkedHashMap<>();
            documentEntry.put(VALUE, documentValue);
            docs.add(documentEntry);
        }
    }

    private String resolveFolder(CaseworkerCICDocument document) {
        if (document.getDocumentCategory() != null) {
            String folder = CATEGORY_TO_FOLDER.get(document.getDocumentCategory().getCategory());
            if (folder != null) {
                return folder;
            }
        }
        return FURTHER_DOCUMENTS_FOLDER;
    }

    private Map<String, List<String>> extractFolderDocumentNamesFromBundles(List<ListValue<Bundle>> caseBundles) {
        if (caseBundles == null || caseBundles.isEmpty()) {
            return Map.of();
        }
        Map<String, List<String>> folderDocumentNames = new LinkedHashMap<>();
        for (ListValue<Bundle> bundleListValue : caseBundles) {
            Bundle bundle = bundleListValue.getValue();
            if (bundle == null || bundle.getFolders() == null) {
                continue;
            }
            for (ListValue<BundleFolder> folderListValue : bundle.getFolders()) {
                if (folderListValue == null || folderListValue.getValue() == null) {
                    continue;
                }
                BundleFolder folder = folderListValue.getValue();
                List<String> documents = folder.getDocuments() == null ? List.of()
                    : folder.getDocuments().stream()
                        .map(ListValue::getValue)
                        .filter(Objects::nonNull)
                        .map(BundleDocument::getName)
                        .collect(Collectors.toList());
                folderDocumentNames.put(folder.getName(), documents);
            }
        }
        return folderDocumentNames;
    }

    private List<ListValue<CaseworkerCICDocumentUpload>> createUploads(DocumentUploadSpec... specs) {
        List<ListValue<CaseworkerCICDocumentUpload>> uploads = new ArrayList<>();
        for (DocumentUploadSpec spec : specs) {
            CaseworkerCICDocumentUpload upload = CaseworkerCICDocumentUpload.builder()
                .documentCategory(spec.documentType())
                .documentEmailContent("Description for " + spec.fileName())
                .documentLink(Document.builder().filename(spec.fileName()).build())
                .build();
            ListValue<CaseworkerCICDocumentUpload> listValue = new ListValue<>();
            listValue.setId(UUID.randomUUID().toString());
            listValue.setValue(upload);
            uploads.add(listValue);
        }
        return uploads;
    }

    private List<String> extractFilenames(List<AbstractCaseworkerCICDocument<CaseworkerCICDocument>> documents) {
        if (documents == null) {
            return List.of();
        }
        return documents.stream()
            .map(AbstractCaseworkerCICDocument::getValue)
            .filter(value -> value != null && value.getDocumentLink() != null)
            .map(value -> value.getDocumentLink().getFilename())
            .collect(Collectors.toList());
    }

    private record DocumentUploadSpec(String fileName, DocumentType documentType) {
    }
}
