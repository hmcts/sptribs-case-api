package uk.gov.hmcts.sptribs.document;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.ccd.sdk.type.Document;
import uk.gov.hmcts.reform.idam.client.models.User;
import uk.gov.hmcts.sptribs.ciccase.model.LanguagePreference;
import uk.gov.hmcts.sptribs.document.model.DocumentInfo;
import uk.gov.hmcts.sptribs.idam.IdamService;
import uk.gov.hmcts.sptribs.testutil.TestDataHelper;

import java.util.HashMap;
import java.util.Map;
import javax.servlet.http.HttpServletRequest;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.sptribs.document.DocAssemblyServiceTest.expectedCaseData;
import static uk.gov.hmcts.sptribs.testutil.TestConstants.ENGLISH_TEMPLATE_ID;
import static uk.gov.hmcts.sptribs.testutil.TestConstants.FILENAME;
import static uk.gov.hmcts.sptribs.testutil.TestConstants.TEST_CASE_ID;

@ExtendWith(MockitoExtension.class)
public class CaseDataDocumentServiceTest {

    @Mock
    private DocAssemblyService docAssemblyService;

    @Mock
    private IdamService idamService;

    @Mock
    private HttpServletRequest request;


    @InjectMocks
    private CaseDataDocumentService caseDataDocumentService;

    @Test
    public void shouldGenerateAndStoreDraftApplicationAndReturnDocumentUrl() {
        //Given
        final Map<String, Object> templateContent = new HashMap<>();
        Map<String, Object> caseDataMap = expectedCaseData();
        User user = TestDataHelper.getUser();
        DocumentInfo info = new DocumentInfo("", FILENAME, "");
        when(idamService.retrieveUser(any())).thenReturn(user);
        when(docAssemblyService.renderDocument(any(), any(), any(), any(), any(), any())).thenReturn(info);

        //When
        Document result = caseDataDocumentService.renderDocument(templateContent,
            TEST_CASE_ID, ENGLISH_TEMPLATE_ID,
            LanguagePreference.ENGLISH,
            FILENAME, request);

        //Then
        assertThat(result.getFilename()).isEqualTo(FILENAME);
    }
}
