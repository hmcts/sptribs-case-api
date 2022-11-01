package uk.gov.hmcts.sptribs.document;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.ccd.sdk.type.ListValue;
import uk.gov.hmcts.reform.authorisation.generators.AuthTokenGenerator;
import uk.gov.hmcts.reform.idam.client.models.User;
import uk.gov.hmcts.reform.idam.client.models.UserDetails;
import uk.gov.hmcts.sptribs.document.model.DivorceDocument;
import uk.gov.hmcts.sptribs.idam.IdamService;

import java.util.List;

import static java.util.Collections.singletonList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static uk.gov.hmcts.sptribs.document.model.DocumentType.OTHER;
import static uk.gov.hmcts.sptribs.testutil.TestConstants.SYSTEM_USER_USER_ID;
import static uk.gov.hmcts.sptribs.testutil.TestConstants.TEST_CASE_ID;
import static uk.gov.hmcts.sptribs.testutil.TestDataHelper.documentWithType;

@ExtendWith(MockitoExtension.class)
public class DraftApplicationRemovalServiceTest {

    @Mock
    private DocumentManagementClient documentManagementClient;

    @Mock
    private AuthTokenGenerator authTokenGenerator;

    @Mock
    private IdamService idamService;

    @InjectMocks
    private DraftApplicationRemovalService draftApplicationRemovalService;

    @Test
    public void shouldNotInvokeDocManagementWhenApplicationDocumentDoesNotExistInGenerateDocuments() {
        //Given
        final ListValue<DivorceDocument> divorceDocumentListValue = documentWithType(OTHER);

        //When
        final List<ListValue<DivorceDocument>> actualDocumentsList = draftApplicationRemovalService.removeDraftApplicationDocument(
            singletonList(divorceDocumentListValue),
            TEST_CASE_ID
        );

        //Then
        assertThat(actualDocumentsList).containsExactlyInAnyOrder(divorceDocumentListValue);
        assertThat(actualDocumentsList.get(0).getValue().getDocumentDateAdded()).isNull();
        assertThat(actualDocumentsList.get(0).getValue().getDocumentEmailContent()).isNull();
        assertThat(actualDocumentsList.get(0).getValue().getDocumentComment()).isNull();
        verify(idamService).retrieveSystemUpdateUserDetails();
        verifyNoInteractions(authTokenGenerator, documentManagementClient);
    }

    private User systemUser(final List<String> solicitorRoles, final String userId) {
        final UserDetails userDetails = UserDetails
            .builder()
            .roles(solicitorRoles)
            .id(userId)
            .build();

        return new User(SYSTEM_USER_USER_ID, userDetails);
    }
}
