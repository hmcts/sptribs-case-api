package uk.gov.hmcts.sptribs.ciccase;

import org.junit.jupiter.api.Test;
import uk.gov.hmcts.ccd.sdk.CaseViewRequest;
import uk.gov.hmcts.ccd.sdk.type.Document;
import uk.gov.hmcts.ccd.sdk.type.ListValue;
import uk.gov.hmcts.sptribs.ciccase.model.State;
import uk.gov.hmcts.sptribs.ciccase.model.casetype.CriminalInjuriesCompensationData;
import uk.gov.hmcts.sptribs.common.repositories.CorrespondenceRepository;
import uk.gov.hmcts.sptribs.notification.model.Correspondence;
import uk.gov.hmcts.sptribs.notification.persistence.CorrespondenceEntity;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.UUID;

import static java.util.Collections.singletonList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class CicCaseViewTest {

    private final CorrespondenceRepository correspondenceRepository = mock(CorrespondenceRepository.class);
    private final CicCaseView cicCaseView = new CicCaseView(correspondenceRepository);

    @Test
    void shouldReturnProvidedCaseDataUnchanged() {
        CriminalInjuriesCompensationData caseData = new CriminalInjuriesCompensationData();
        caseData.setCaseNameHmctsInternal("Sample case");
        CaseViewRequest<State> request = new CaseViewRequest<>(1234567890123456L, State.CaseManagement);

        var returnedCaseData = cicCaseView.getCase(request, caseData);

        assertThat(returnedCaseData).isSameAs(caseData);
    }

    @Test
    void shouldReturnProvidedCaseDataWithCorrespondenceObject() {
        Document testCorrespondencePDF = Document.builder()
            .url("http://test-url.com/document.pdf")
            .filename("testDocument.pdf")
            .binaryUrl("http://test-url.com/document.pdf/binary")
            .build();

        String testSentOn = LocalDateTime.now().format(DateTimeFormatter.ofPattern("d MMM yyyy HH:mm"));

        ListValue<Correspondence> testCorrespondenceListValue = new ListValue<>();
        testCorrespondenceListValue.setId("1");
        testCorrespondenceListValue.setValue(Correspondence.builder()
            .sentOn(testSentOn)
            .from("test@test.com")
            .to("testRecipient@test.com")
            .documentUrl(testCorrespondencePDF)
            .correspondenceType("Email")
            .build());

        CriminalInjuriesCompensationData caseData = new CriminalInjuriesCompensationData();
        caseData.setCaseNameHmctsInternal("Sample case");
        caseData.setCorrespondence(singletonList(testCorrespondenceListValue));

        CaseViewRequest<State> request = new CaseViewRequest<>(1234567890123456L, State.CaseManagement);

        CorrespondenceEntity testCorrespondenceEntity = CorrespondenceEntity.builder()
            .id(UUID.randomUUID())
            .eventType("TEST_EVENT")
            .caseReferenceNumber(1234567890123456L)
            .sentOn(LocalDateTime.now().atOffset(ZoneOffset.UTC))
            .sentFrom("test@test.com")
            .sentTo("testRecipient@test.com")
            .documentUrl("http://test-url.com/document.pdf")
            .documentFilename("testDocument.pdf")
            .documentBinaryUrl("http://test-url.com/document.pdf/binary")
            .correspondenceType("Email")
            .build();

        when(correspondenceRepository.findAllByCaseReferenceNumberOrderBySentOnDesc(request.caseRef()))
            .thenReturn(singletonList(testCorrespondenceEntity));

        CriminalInjuriesCompensationData returnedCaseData = cicCaseView.getCase(request, caseData);

        assertThat(returnedCaseData).isSameAs(caseData);
    }
}
