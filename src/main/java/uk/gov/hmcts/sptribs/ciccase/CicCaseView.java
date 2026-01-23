package uk.gov.hmcts.sptribs.ciccase;

import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.ccd.sdk.CaseView;
import uk.gov.hmcts.ccd.sdk.CaseViewRequest;
import uk.gov.hmcts.ccd.sdk.type.Document;
import uk.gov.hmcts.ccd.sdk.type.ListValue;
import uk.gov.hmcts.sptribs.ciccase.model.State;
import uk.gov.hmcts.sptribs.ciccase.model.casetype.CriminalInjuriesCompensationData;
import uk.gov.hmcts.sptribs.common.repositories.CorrespondenceRepository;
import uk.gov.hmcts.sptribs.common.repositories.StatementRepository;
import uk.gov.hmcts.sptribs.document.model.DocumentType;
import uk.gov.hmcts.sptribs.notification.model.Correspondence;
import uk.gov.hmcts.sptribs.notification.model.Statement;
import uk.gov.hmcts.sptribs.notification.persistence.CorrespondenceEntity;
import uk.gov.hmcts.sptribs.notification.persistence.StatementsEntity;

import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

@Component
@Primary
@Slf4j
public class CicCaseView implements CaseView<CriminalInjuriesCompensationData, State> {

    private final CorrespondenceRepository correspondenceRepository;
    private final StatementRepository statementRepository;

    public CicCaseView(CorrespondenceRepository correspondenceRepository,
                       StatementRepository statementRepository) {
        this.correspondenceRepository = correspondenceRepository;
        this.statementRepository = statementRepository;
    }

    @Override
    public CriminalInjuriesCompensationData getCase(CaseViewRequest<State> request,
                                                   CriminalInjuriesCompensationData blobCase) {
        // Invoked whenever CCD needs to load a case.
        // Load up any additional data or perform transformations as needed.
        List<ListValue<Correspondence>> correspondences = new ArrayList<>();
        AtomicInteger listValueIndex = new AtomicInteger(0);
        long caseReference = request.caseRef();

        try {
            buildStatements(caseReference, blobCase);
        } catch (Exception couldNotGetStatement) {
            log.error("Could not load statements into tab!!", couldNotGetStatement);
        }


        for (CorrespondenceEntity correspondenceEntity :
            correspondenceRepository.findAllByCaseReferenceNumberOrderBySentOnDesc(caseReference)) {
            Document correspondenceDocument = Document.builder()
                .url(correspondenceEntity.getDocumentUrl())
                .filename(correspondenceEntity.getDocumentFilename())
                .binaryUrl(correspondenceEntity.getDocumentBinaryUrl())
                .categoryId(DocumentType.CORRESPONDENCE.getCategory())
                .build();

            Correspondence correspondence = Correspondence.builder()
                .sentOn(correspondenceEntity.getSentOn().format(DateTimeFormatter.ofPattern("d MMM yyyy HH:mm")))
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

        blobCase.setCorrespondence(correspondences);
        return blobCase;
    }


    private void buildStatements(long caseReference,
                                 CriminalInjuriesCompensationData blobCase) {

        List<ListValue<Statement>> statements = new ArrayList<>();
        List<StatementsEntity> statementsEntities = statementRepository.
            findAllByCaseReferenceNumberOrderByCreatedOnDesc(caseReference);
        Integer listValueIndex = 0;

        for (StatementsEntity statementsEntity : statementsEntities) {
            Document correspondenceDocument = Document.builder()
                .url(statementsEntity.getDocumentUrl())
                .filename(statementsEntity.getDocumentFilename())
                .binaryUrl(statementsEntity.getDocumentBinaryUrl())
                .categoryId(DocumentType.CORRESPONDENCE.getCategory())
                .build();

            Statement statement = Statement.builder()
                .savedOn(statementsEntity.getCreatedOn().format(DateTimeFormatter.ofPattern("d MMM yyyy HH:mm")))
                .party(statementsEntity.getPartyType())
                .documentUrl(correspondenceDocument)
                .build();

            ListValue<Statement> statementListValue = new ListValue<>();
            statementListValue.setId(String.valueOf(listValueIndex));
            statementListValue.setValue(statement);
            statements.add(statementListValue);
            listValueIndex++;
        }

        blobCase.setStatement(statements);

    }
}
