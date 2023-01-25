package uk.gov.hmcts.sptribs.document;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.ccd.sdk.type.Document;
import uk.gov.hmcts.reform.idam.client.models.User;
import uk.gov.hmcts.sptribs.document.model.DocumentInfo;
import uk.gov.hmcts.sptribs.idam.IdamService;

import java.util.HashMap;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.sptribs.ciccase.model.LanguagePreference.ENGLISH;
import static uk.gov.hmcts.sptribs.document.DocumentConstants.DIVORCE_GENERAL_ORDER;
import static uk.gov.hmcts.sptribs.testutil.TestConstants.TEST_AUTHORIZATION_TOKEN;
import static uk.gov.hmcts.sptribs.testutil.TestConstants.TEST_CASE_ID;

@ExtendWith(MockitoExtension.class)
class CaseDataDocumentServiceTest {

    private static final String DOC_URL = "http://localhost:4200/assets/59a54ccc-979f-11eb-a8b3-0242ac130003";
    private static final String DOC_BINARY_URL = "http://localhost:4200/assets/59a54ccc-979f-11eb-a8b3-0242ac130003/binary";
    private static final String PDF_FILENAME = "draft-divorce-application-1616591401473378.pdf";
    private static final String GENERAL_ORDER_PDF_FILENAME = "draft-divorce-application-1616591401473378.pdf";
    private static final String NOP_PDF_FILENAME = "noticeOfProceedings-1616591401473378.pdf";
    private static final String URL = "url";
    private static final String FILENAME = "filename";
    private static final String BINARY_URL = "binaryUrl";
    private static final String GENERAL_LETTER_PDF = "GeneralLetter-2020-04-20:12:21.pdf";

    @Mock
    private DocAssemblyService docAssemblyService;

    @Mock
    private DocumentIdProvider documentIdProvider;

    @Mock
    private IdamService idamService;

    @InjectMocks
    private CaseDataDocumentService caseDataDocumentService;

    @Test
    void shouldGenerateAndReturnGeneralOrderDocument() {
        //Given
        final Map<String, Object> templateContent = new HashMap<>();
        final User systemUser = mock(User.class);
        final String filename = GENERAL_ORDER_PDF_FILENAME + TEST_CASE_ID;

        when(idamService.retrieveSystemUpdateUserDetails()).thenReturn(systemUser);
        when(systemUser.getAuthToken()).thenReturn(TEST_AUTHORIZATION_TOKEN);
        when(docAssemblyService
            .renderDocument(
                templateContent,
                TEST_CASE_ID,
                TEST_AUTHORIZATION_TOKEN,
                DIVORCE_GENERAL_ORDER,
                ENGLISH,
                filename))
            .thenReturn(new DocumentInfo(DOC_URL, PDF_FILENAME, DOC_BINARY_URL));

        //When
        final Document result = caseDataDocumentService.renderDocument(
            templateContent,
            TEST_CASE_ID,
            DIVORCE_GENERAL_ORDER,
            ENGLISH,
            filename);

        //Then
        assertThat(result.getBinaryUrl()).isEqualTo(DOC_BINARY_URL);
        assertThat(result.getUrl()).isEqualTo(DOC_URL);
        assertThat(result.getFilename()).isEqualTo(GENERAL_ORDER_PDF_FILENAME);
    }
}
