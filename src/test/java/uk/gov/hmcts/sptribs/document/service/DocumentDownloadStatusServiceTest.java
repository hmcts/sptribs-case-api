package uk.gov.hmcts.sptribs.document.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.idam.client.models.User;
import uk.gov.hmcts.reform.idam.client.models.UserDetails;
import uk.gov.hmcts.sptribs.ciccase.model.CaseData;
import uk.gov.hmcts.sptribs.ciccase.model.CicCase;
import uk.gov.hmcts.sptribs.common.repositories.CaseDataRepository;
import uk.gov.hmcts.sptribs.common.repositories.DocumentDownloadStatusesRepository;
import uk.gov.hmcts.sptribs.common.repositories.DocumentsRepository;
import uk.gov.hmcts.sptribs.common.repositories.model.CicaCaseEntity;
import uk.gov.hmcts.sptribs.document.model.DocumentDownloadStatusEntity;
import uk.gov.hmcts.sptribs.document.model.DocumentEntity;
import uk.gov.hmcts.sptribs.idam.IdamService;
import uk.gov.hmcts.sptribs.notification.model.Party;

import java.util.HashMap;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class DocumentDownloadStatusServiceTest {

    @InjectMocks
    private DocumentDownloadStatusService documentDownloadStatusService;

    @Mock
    private IdamService idamService;

    @Mock
    private CaseDataRepository caseDataRepository;

    @Mock
    private ObjectMapper objectMapper;

    @Mock
    private DocumentsRepository documentsRepository;

    @Mock
    private DocumentDownloadStatusesRepository documentDownloadStatusesRepository;

    @Test
    public void shouldRecordNewDocumentDownloadForSubject() {
        String auth = "Bearer token";
        String ref = "1234567890123456";
        String postcode = "SW1 1AA";
        String docUuid = "uuid-123";
        String email = "subject@test.com";

        User user = new User(auth, UserDetails.builder().email(email).build());

        CicaCaseEntity cicaCaseEntity = CicaCaseEntity.builder()
            .data(new HashMap<>())
            .build();

        CaseData caseData = CaseData.builder()
            .cicCase(CicCase.builder().email(email).build())
            .build();

        DocumentEntity docEntity = DocumentEntity.builder()
            .id(100L)
            .build();

        when(idamService.retrieveUser(auth)).thenReturn(user);
        when(caseDataRepository.findCase(ref, email, postcode)).thenReturn(Optional.of(cicaCaseEntity));
        when(objectMapper.convertValue(cicaCaseEntity.getData(), CaseData.class)).thenReturn(caseData);
        when(documentsRepository.findByDocumentIdUuid(docUuid)).thenReturn(Optional.of(docEntity));
        when(documentDownloadStatusesRepository.findByDocumentIdAndParty(100L, Party.SUBJECT)).thenReturn(Optional.empty());

        documentDownloadStatusService.recordDocumentDownload(auth, ref, postcode, docUuid);

        verify(documentDownloadStatusesRepository, times(1)).save(org.mockito.ArgumentMatchers.any(DocumentDownloadStatusEntity.class));
    }

    @Test
    public void shouldUpdateExistingDocumentDownloadForSubject() {
        String auth = "Bearer token";
        String ref = "1234567890123456";
        String postcode = "SW1 1AA";
        String docUuid = "uuid-123";
        String email = "subject@test.com";

        User user = new User(auth, UserDetails.builder().email(email).build());

        CicaCaseEntity cicaCaseEntity = CicaCaseEntity.builder()
            .data(new HashMap<>())
            .build();

        CaseData caseData = CaseData.builder()
            .cicCase(CicCase.builder().email(email).build())
            .build();

        DocumentEntity docEntity = DocumentEntity.builder()
            .id(100L)
            .build();

        DocumentDownloadStatusEntity statusEntity = DocumentDownloadStatusEntity.builder()
            .id(1L)
            .caseReferenceNumber(1234567890123456L)
            .documentId(100L)
            .party(Party.SUBJECT)
            .build();

        when(idamService.retrieveUser(auth)).thenReturn(user);
        when(caseDataRepository.findCase(ref, email, postcode)).thenReturn(Optional.of(cicaCaseEntity));
        when(objectMapper.convertValue(cicaCaseEntity.getData(), CaseData.class)).thenReturn(caseData);
        when(documentsRepository.findByDocumentIdUuid(docUuid)).thenReturn(Optional.of(docEntity));
        when(documentDownloadStatusesRepository.findByDocumentIdAndParty(100L, Party.SUBJECT)).thenReturn(Optional.of(statusEntity));

        documentDownloadStatusService.recordDocumentDownload(auth, ref, postcode, docUuid);

        verify(documentDownloadStatusesRepository, times(1)).save(statusEntity);
    }

    @Test
    public void shouldNotRecordDownloadIfUserEmailDoesNotMatchAnyParty() {
        String auth = "Bearer token";
        String ref = "1234567890123456";
        String postcode = "SW1 1AA";
        String email = "stranger@test.com";

        User user = new User(auth, UserDetails.builder().email(email).build());

        CicaCaseEntity cicaCaseEntity = CicaCaseEntity.builder()
            .data(new HashMap<>())
            .build();

        CaseData caseData = CaseData.builder()
            .cicCase(CicCase.builder().email("subject@test.com").build())
            .build();

        when(idamService.retrieveUser(auth)).thenReturn(user);
        when(caseDataRepository.findCase(ref, email, postcode)).thenReturn(Optional.of(cicaCaseEntity));
        when(objectMapper.convertValue(cicaCaseEntity.getData(), CaseData.class)).thenReturn(caseData);

        documentDownloadStatusService.recordDocumentDownload(auth, ref, postcode, "uuid-123");

        verify(documentDownloadStatusesRepository, times(0)).save(org.mockito.ArgumentMatchers.any());
    }

    @Test
    public void shouldNotRecordDownloadIfDocumentNotFound() {
        String auth = "Bearer token";
        String ref = "1234567890123456";
        String postcode = "SW1 1AA";
        String email = "subject@test.com";

        User user = new User(auth, UserDetails.builder().email(email).build());

        CicaCaseEntity cicaCaseEntity = CicaCaseEntity.builder()
            .data(new HashMap<>())
            .build();

        CaseData caseData = CaseData.builder()
            .cicCase(CicCase.builder().email(email).build())
            .build();

        when(idamService.retrieveUser(auth)).thenReturn(user);
        when(caseDataRepository.findCase(ref, email, postcode)).thenReturn(Optional.of(cicaCaseEntity));
        when(objectMapper.convertValue(cicaCaseEntity.getData(), CaseData.class)).thenReturn(caseData);
        when(documentsRepository.findByDocumentIdUuid("uuid-123")).thenReturn(Optional.empty());

        documentDownloadStatusService.recordDocumentDownload(auth, ref, postcode, "uuid-123");

        verify(documentDownloadStatusesRepository, times(0)).save(org.mockito.ArgumentMatchers.any());
    }

    @Test
    public void shouldReturnDownloadedDocumentIds() {
        String auth = "Bearer token";
        String ref = "1234567890123456";
        String postcode = "SW1 1AA";
        String email = "subject@test.com";

        User user = new User(auth, UserDetails.builder().email(email).build());

        CicaCaseEntity cicaCaseEntity = CicaCaseEntity.builder()
            .data(new HashMap<>())
            .build();

        CaseData caseData = CaseData.builder()
            .cicCase(CicCase.builder().email(email).build())
            .build();

        DocumentDownloadStatusEntity status1 = DocumentDownloadStatusEntity.builder()
            .documentId(101L)
            .build();

        DocumentDownloadStatusEntity status2 = DocumentDownloadStatusEntity.builder()
            .documentId(102L)
            .build();

        when(idamService.retrieveUser(auth)).thenReturn(user);
        when(caseDataRepository.findCase(ref, email, postcode)).thenReturn(Optional.of(cicaCaseEntity));
        when(objectMapper.convertValue(cicaCaseEntity.getData(), CaseData.class)).thenReturn(caseData);
        when(documentDownloadStatusesRepository.findAllByCaseReferenceNumberAndParty(1234567890123456L, Party.SUBJECT))
            .thenReturn(List.of(status1, status2));

        Set<Long> result = documentDownloadStatusService.getDownloadedDocumentIds(auth, ref, postcode);

        assertThat(result).containsExactlyInAnyOrder(101L, 102L);
    }

    @Test
    public void shouldReturnEmptySetIfCaseNotFoundForDownloadedDocumentIds() {
        String auth = "Bearer token";
        String ref = "1234567890123456";
        String postcode = "SW1 1AA";
        String email = "subject@test.com";

        User user = new User(auth, UserDetails.builder().email(email).build());

        when(idamService.retrieveUser(auth)).thenReturn(user);
        when(caseDataRepository.findCase(ref, email, postcode)).thenReturn(Optional.empty());

        Set<Long> result = documentDownloadStatusService.getDownloadedDocumentIds(auth, ref, postcode);

        assertThat(result).isEmpty();
    }

    @Test
    public void shouldTrackIndependentDownloadStatusesForAllParties() {
        String auth = "Bearer token";
        String ref = "1234567890123456";
        String postcode = "SW1 1AA";
        String docUuid = "uuid-123";

        List<String> emails = List.of(
            "subject@test.com",
            "applicant@test.com",
            "representative@test.com",
            "respondent@test.com"
        );

        List<Party> parties = List.of(
            Party.SUBJECT,
            Party.APPLICANT,
            Party.REPRESENTATIVE,
            Party.RESPONDENT
        );

        DocumentEntity docEntity = DocumentEntity.builder()
            .id(100L)
            .build();

        for (int i = 0; i < emails.size(); i++) {
            String email = emails.get(i);
            Party party = parties.get(i);

            User user = new User(auth, UserDetails.builder().email(email).build());

            CicaCaseEntity cicaCaseEntity = CicaCaseEntity.builder()
                .data(new HashMap<>())
                .build();

            CaseData caseData = CaseData.builder()
                .cicCase(CicCase.builder()
                    .email("subject@test.com")
                    .applicantEmailAddress("applicant@test.com")
                    .representativeEmailAddress("representative@test.com")
                    .respondentEmail("respondent@test.com")
                    .build())
                .build();

            when(idamService.retrieveUser(auth)).thenReturn(user);
            when(caseDataRepository.findCase(ref, email, postcode)).thenReturn(Optional.of(cicaCaseEntity));
            when(objectMapper.convertValue(cicaCaseEntity.getData(), CaseData.class)).thenReturn(caseData);
            when(documentsRepository.findByDocumentIdUuid(docUuid)).thenReturn(Optional.of(docEntity));
            when(documentDownloadStatusesRepository.findByDocumentIdAndParty(100L, party)).thenReturn(Optional.empty());

            documentDownloadStatusService.recordDocumentDownload(auth, ref, postcode, docUuid);

            verify(documentDownloadStatusesRepository, times(1)).save(org.mockito.ArgumentMatchers.argThat(status ->
                status.getDocumentId() == 100L && status.getParty() == party
            ));
        }
    }
}
