package uk.gov.hmcts.sptribs.ciccase;

import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Component;
import lombok.extern.slf4j.Slf4j;
import uk.gov.hmcts.ccd.sdk.CaseView;
import uk.gov.hmcts.ccd.sdk.CaseViewRequest;
import uk.gov.hmcts.ccd.sdk.type.Document;
import uk.gov.hmcts.ccd.sdk.type.ListValue;
import uk.gov.hmcts.sptribs.ciccase.model.State;
import uk.gov.hmcts.sptribs.ciccase.model.casetype.CriminalInjuriesCompensationData;
import uk.gov.hmcts.sptribs.common.repositories.CorrespondenceRepository;
import uk.gov.hmcts.sptribs.document.model.DocumentType;
import uk.gov.hmcts.sptribs.notification.model.Correspondence;
import uk.gov.hmcts.sptribs.notification.persistence.CorrespondenceEntity;
import uk.gov.hmcts.sptribs.statement.model.Statement;
import uk.gov.hmcts.sptribs.statement.service.StatementPersistenceException;
import uk.gov.hmcts.sptribs.statement.service.StatementService;

import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

@Component
@Primary
@Slf4j
public class CicCaseView implements CaseView<CriminalInjuriesCompensationData, State> {

    private final CorrespondenceRepository correspondenceRepository;
    private final StatementService statementService;

    public CicCaseView(CorrespondenceRepository correspondenceRepository,
                       StatementService statementService) {
        this.correspondenceRepository = correspondenceRepository;
        this.statementService = statementService;
    }

    @Override
    public CriminalInjuriesCompensationData getCase(CaseViewRequest<State> request,
                                                   CriminalInjuriesCompensationData blobCase) {
        // Invoked whenever CCD needs to load a case.
        // Load up any additional data or perform transformations as needed.
        List<ListValue<Correspondence>> correspondences = new ArrayList<>();
        AtomicInteger listValueIndex = new AtomicInteger(0);
        List<ListValue<Statement>> statements = Collections.emptyList();

        for (CorrespondenceEntity correspondenceEntity :
            correspondenceRepository.findAllByCaseReferenceNumberOrderBySentOnDesc(request.caseRef())) {
            Document correspondenceDocument = Document.builder()
                .url(correspondenceEntity.getDocumentUrl())
                .filename(correspondenceEntity.getDocumentFilename())
                .binaryUrl(correspondenceEntity.getDocumentBinaryUrl())
                .categoryId(DocumentType.CORRESPONDENCE.getCategory())
                .build();

            Correspondence correspondence = Correspondence.builder()
                .sentOn(correspondenceEntity.getSentOn().atZoneSameInstant(ZoneId.systemDefault())
                    .format(DateTimeFormatter.ofPattern("d MMM yyyy HH:mm")))
                .from(correspondenceEntity.getSentFrom())
                .to(correspondenceEntity.getSentTo())
                .documentUrl(correspondenceDocument)
                .correspondenceType(correspondenceEntity.getCorrespondenceType())
                .build();

            ListValue<Correspondence> correspondenceListValue = new ListValue<>();
            correspondenceListValue.setId(String.valueOf(listValueIndex.incrementAndGet()));
            correspondenceListValue.setValue(correspondence);
            correspondences.add(correspondenceListValue);
        }
        try {
            statements = statementService.getStatementsForCase(request.caseRef());
        } catch (StatementPersistenceException statementPersistenceException) {
            log.error("Unable to retrieve statements for case {}", request.caseRef(), statementPersistenceException);
            statements = Collections.emptyList();
        }

        blobCase.setCorrespondence(correspondences);
        blobCase.setStatements(statements);
        return blobCase;
    }
}
