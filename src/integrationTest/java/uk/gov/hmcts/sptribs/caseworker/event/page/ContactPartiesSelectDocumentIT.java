package uk.gov.hmcts.sptribs.caseworker.event.page;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.context.junit.jupiter.DisabledIf;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;
import uk.gov.hmcts.ccd.sdk.type.DynamicListElement;
import uk.gov.hmcts.ccd.sdk.type.DynamicMultiSelectList;
import uk.gov.hmcts.ccd.sdk.type.ListValue;
import uk.gov.hmcts.reform.authorisation.generators.AuthTokenGenerator;
import uk.gov.hmcts.sptribs.caseworker.model.ContactPartiesDocuments;
import uk.gov.hmcts.sptribs.caseworker.util.DocumentListUtil;
import uk.gov.hmcts.sptribs.cdam.model.Document;
import uk.gov.hmcts.sptribs.ciccase.model.CaseData;
import uk.gov.hmcts.sptribs.ciccase.model.CicCase;
import uk.gov.hmcts.sptribs.common.config.WebMvcConfig;
import uk.gov.hmcts.sptribs.document.model.CaseworkerCICDocument;
import uk.gov.hmcts.sptribs.idam.CICUser;
import uk.gov.hmcts.sptribs.idam.IdamService;
import uk.gov.hmcts.sptribs.services.cdam.CaseDocumentClientApi;
import uk.gov.hmcts.sptribs.testutil.IdamWireMock;

import java.util.Arrays;
import java.util.List;
import java.util.UUID;

import static net.javacrumbs.jsonunit.assertj.JsonAssertions.assertThatJson;
import static org.apache.commons.lang.StringUtils.substringAfterLast;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static uk.gov.hmcts.sptribs.caseworker.util.EventConstants.CASEWORKER_CONTACT_PARTIES;
import static uk.gov.hmcts.sptribs.testutil.TestConstants.AUTHORIZATION;
import static uk.gov.hmcts.sptribs.testutil.TestConstants.CONTACT_PARTIES_SELECT_DOCUMENT_MID_EVENT_URL;
import static uk.gov.hmcts.sptribs.testutil.TestConstants.SERVICE_AUTHORIZATION;
import static uk.gov.hmcts.sptribs.testutil.TestConstants.TEST_AUTHORIZATION_TOKEN;
import static uk.gov.hmcts.sptribs.testutil.TestDataHelper.callbackRequest;
import static uk.gov.hmcts.sptribs.testutil.TestDataHelper.caseData;
import static uk.gov.hmcts.sptribs.testutil.TestDataHelper.getCaseworkerCICDocumentList;

@ExtendWith(SpringExtension.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
@ContextConfiguration(initializers = {IdamWireMock.PropertiesInitializer.class})
public class ContactPartiesSelectDocumentIT {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private WebMvcConfig webMvcConfig;

    @MockitoBean
    private CaseDocumentClientApi caseDocumentClientApi;

    @MockitoBean
    private IdamService idamService;

    @MockitoBean
    private AuthTokenGenerator authTokenGenerator;

    private static final String SYSTEM_AUTH = "system-auth-token";
    private static final String SERVICE_AUTH = "service-auth-token";

    private CICUser systemUser;

    @BeforeAll
    static void setUp() {
        IdamWireMock.start();
    }

    @AfterAll
    static void tearDown() {
        IdamWireMock.stopAndReset();
    }

    @BeforeEach
    void configureMocks() {
        systemUser = mock(CICUser.class);
        when(idamService.retrieveSystemUpdateUserDetails()).thenReturn(systemUser);
        when(systemUser.getAuthToken()).thenReturn(SYSTEM_AUTH);
        when(authTokenGenerator.generate()).thenReturn(SERVICE_AUTH);

        Document document = new Document();
        document.size = 1_000_000;
        when(caseDocumentClientApi.getDocument(any(), any(), any()))
            .thenReturn(org.springframework.http.ResponseEntity.ok(document));
    }

    @Test
    void shouldReturnErrorsOnMidEvent() throws Exception {
        final ContactPartiesDocuments contactPartiesDocuments = new ContactPartiesDocuments();
        contactPartiesDocuments.setDocumentList(DynamicMultiSelectList
            .builder()
            .value(populateContactPartiesDocumentsList())
            .listItems(populateContactPartiesDocumentsList())
            .build());
        final CaseData caseData = CaseData.builder()
            .contactPartiesDocuments(contactPartiesDocuments)
            .build();

        mockMvc.perform(post(CONTACT_PARTIES_SELECT_DOCUMENT_MID_EVENT_URL)
            .contentType(APPLICATION_JSON)
            .header(SERVICE_AUTHORIZATION, TEST_AUTHORIZATION_TOKEN)
            .header(AUTHORIZATION, TEST_AUTHORIZATION_TOKEN)
            .content(objectMapper.writeValueAsString(
                callbackRequest(caseData,
                    CASEWORKER_CONTACT_PARTIES)))
            .accept(APPLICATION_JSON))
            .andExpect(
                status().isOk())
            .andExpect(
                jsonPath("$.errors")
                    .value("Select up to 10 documents")
            );
    }

    @Test
    void shouldNotReturnErrorsOnMidEvent() throws Exception {
        String response = mockMvc.perform(post(CONTACT_PARTIES_SELECT_DOCUMENT_MID_EVENT_URL)
            .contentType(APPLICATION_JSON)
            .header(SERVICE_AUTHORIZATION, TEST_AUTHORIZATION_TOKEN)
            .header(AUTHORIZATION, TEST_AUTHORIZATION_TOKEN)
            .content(objectMapper.writeValueAsString(
                callbackRequest(caseData(),
                    CASEWORKER_CONTACT_PARTIES)))
            .accept(APPLICATION_JSON))
            .andExpect(
                status().isOk())
            .andReturn()
            .getResponse()
            .getContentAsString();

        assertThatJson(response)
            .inPath("$.errors")
            .isArray()
            .isEmpty();
    }

    @Test
    @DisabledIf(expression = "${feature.citizen-dashboard.enabled:true}", loadContext = true)
    void shouldReturnErrorWhenDocumentSizeExceedsLimit() throws Exception {
        List<ListValue<CaseworkerCICDocument>> availableDocuments = getCaseworkerCICDocumentList("Oversized Document.pdf");
        CaseData caseData = CaseData.builder()
            .cicCase(CicCase.builder().applicantDocumentsUploaded(availableDocuments).build())
            .build();
        DynamicMultiSelectList documentList = DocumentListUtil.prepareContactPartiesDocumentList(caseData,
            "http://manage-case.demo.platform.hmcts.net");
        assertThat(documentList.getListItems()).hasSize(1);
        documentList.setValue(List.of(documentList.getListItems().getFirst()));
        caseData.setContactPartiesDocuments(ContactPartiesDocuments.builder().documentList(documentList).build());

        UUID documentId = UUID.fromString(substringAfterLast(
            availableDocuments.getFirst().getValue().getDocumentLink().getUrl(), "/"));

        Document oversizedDocument = new Document();
        oversizedDocument.size = 3_000_000;
        when(caseDocumentClientApi.getDocument(eq(SYSTEM_AUTH), eq(SERVICE_AUTH), eq(documentId)))
            .thenReturn(org.springframework.http.ResponseEntity.ok(oversizedDocument));

        mockMvc.perform(post(CONTACT_PARTIES_SELECT_DOCUMENT_MID_EVENT_URL)
            .contentType(APPLICATION_JSON)
            .header(SERVICE_AUTHORIZATION, TEST_AUTHORIZATION_TOKEN)
            .header(AUTHORIZATION, TEST_AUTHORIZATION_TOKEN)
            .content(objectMapper.writeValueAsString(
                callbackRequest(caseData, CASEWORKER_CONTACT_PARTIES)))
            .accept(APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.errors[0]")
                .value("Unable to proceed because Oversized Document.pdf is larger than 2MB"));
    }

    private List<DynamicListElement> populateContactPartiesDocumentsList() {
        List<DynamicListElement> contactPartiesDocumentsList = Arrays.asList(new DynamicListElement[12]);
        contactPartiesDocumentsList.replaceAll(o -> getListItem());
        return contactPartiesDocumentsList;
    }

    private DynamicListElement getListItem() {
        UUID documentId = UUID.randomUUID();
        return DynamicListElement
            .builder()
            .label("[pdf.pdf A - Application Form](http://manage-case.demo.platform.hmcts.net/documents/" + documentId + ")")
            .code(UUID.randomUUID())
            .build();
    }
}
