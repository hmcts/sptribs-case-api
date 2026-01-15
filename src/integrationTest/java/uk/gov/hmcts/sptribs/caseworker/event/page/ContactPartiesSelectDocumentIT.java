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
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;
import uk.gov.hmcts.ccd.sdk.type.DynamicListElement;
import uk.gov.hmcts.ccd.sdk.type.DynamicMultiSelectList;
import uk.gov.hmcts.reform.authorisation.generators.AuthTokenGenerator;
import uk.gov.hmcts.reform.idam.client.models.User;
import uk.gov.hmcts.sptribs.caseworker.model.ContactPartiesDocuments;
import uk.gov.hmcts.sptribs.cdam.model.Document;
import uk.gov.hmcts.sptribs.ciccase.model.CaseData;
import uk.gov.hmcts.sptribs.common.config.WebMvcConfig;
import uk.gov.hmcts.sptribs.idam.IdamService;
import uk.gov.hmcts.sptribs.services.cdam.CaseDocumentClientApi;
import uk.gov.hmcts.sptribs.testutil.IdamWireMock;

import java.util.Arrays;
import java.util.List;
import java.util.UUID;

import static net.javacrumbs.jsonunit.assertj.JsonAssertions.assertThatJson;
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

    private User systemUser;

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
        systemUser = mock(User.class);
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
    void shouldReturnErrorWhenDocumentSizeExceedsLimit() throws Exception {
        UUID documentId = UUID.randomUUID();
        String label = "[Oversized Document](http://manage-case.demo.platform.hmcts.net/documents/" + documentId + ")";

        DynamicListElement element = DynamicListElement.builder()
            .label(label)
            .code(UUID.randomUUID())
            .build();

        ContactPartiesDocuments contactPartiesDocuments = new ContactPartiesDocuments();
        contactPartiesDocuments.setDocumentList(DynamicMultiSelectList.builder()
            .value(List.of(element))
            .listItems(List.of(element))
            .build());

        Document oversizedDocument = new Document();
        oversizedDocument.size = 3_000_000;
        when(caseDocumentClientApi.getDocument(eq(SYSTEM_AUTH), eq(SERVICE_AUTH), eq(documentId)))
            .thenReturn(org.springframework.http.ResponseEntity.ok(oversizedDocument));

        mockMvc.perform(post(CONTACT_PARTIES_SELECT_DOCUMENT_MID_EVENT_URL)
            .contentType(APPLICATION_JSON)
            .header(SERVICE_AUTHORIZATION, TEST_AUTHORIZATION_TOKEN)
            .header(AUTHORIZATION, TEST_AUTHORIZATION_TOKEN)
            .content(objectMapper.writeValueAsString(
                callbackRequest(CaseData.builder()
                    .contactPartiesDocuments(contactPartiesDocuments)
                    .build(),
                    CASEWORKER_CONTACT_PARTIES)))
            .accept(APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.errors[0]").value("Unable to proceed because "
                + label.substring(label.indexOf('[') + 1, label.indexOf(']'))
                + " is larger than 2MB"));
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
