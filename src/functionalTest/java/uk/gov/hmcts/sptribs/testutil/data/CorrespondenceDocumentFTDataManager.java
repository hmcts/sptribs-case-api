package uk.gov.hmcts.sptribs.testutil.data;

import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.sptribs.common.repositories.model.CorrespondenceDocumentEntity;
import uk.gov.hmcts.sptribs.common.repositories.model.CorrespondenceDocumentId;

import java.sql.Array;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static uk.gov.hmcts.sptribs.testutil.FunctionalTestConstants.KEY_CASE_DOCUMENTS_REFERENCE;
import static uk.gov.hmcts.sptribs.testutil.FunctionalTestConstants.KEY_CORRESPONDENCE_ID;
import static uk.gov.hmcts.sptribs.testutil.FunctionalTestConstants.TABLE_CASE_CORRESPONDENCES;
import static uk.gov.hmcts.sptribs.testutil.FunctionalTestConstants.TABLE_CORRESPONDENCE_DOCUMENT;

@Component
@Profile("functional")
@Slf4j
public class CorrespondenceDocumentFTDataManager extends FunctionalTestDataManager {

    public List<CorrespondenceDocumentEntity> getCorrespondenceDocuments(UUID correspondenceId) {
        String sql = "SELECT * FROM " + TABLE_CORRESPONDENCE_DOCUMENT + " WHERE " + KEY_CORRESPONDENCE_ID + " = ?";

        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setObject(1, correspondenceId);
            ResultSet rs = stmt.executeQuery();

            List<CorrespondenceDocumentEntity> correspondenceDocumentEntities = new ArrayList<>();

            while (rs.next()) {

                CorrespondenceDocumentId correspondenceDocumentId = CorrespondenceDocumentId.builder()
                        .documentId(rs.getLong("document_id"))
                        .correspondenceId(rs.getObject("correspondence_id", UUID.class)).build();

                CorrespondenceDocumentEntity correspondenceDocumentEntity = CorrespondenceDocumentEntity.builder()
                        .id(correspondenceDocumentId)
                        .build();

                correspondenceDocumentEntities.add(correspondenceDocumentEntity);
            }

            return correspondenceDocumentEntities;

        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public void deleteCorrespondenceDocuments(List<Long> caseReferences) {
        String deleteSql =
            "DELETE FROM " + TABLE_CORRESPONDENCE_DOCUMENT +
                " WHERE " + KEY_CORRESPONDENCE_ID + " IN (" +
                "SELECT id FROM " + TABLE_CASE_CORRESPONDENCES +
                " WHERE " + KEY_CASE_DOCUMENTS_REFERENCE + " = ANY (?)" +
                ")";

        try (PreparedStatement stmt = connection.prepareStatement(deleteSql)) {
            Array referenceArray = connection.createArrayOf("bigint", caseReferences.toArray(Long[]::new));
            stmt.setArray(1, referenceArray);
            int deletedRows = stmt.executeUpdate();

            log.info(
                "Deleted {} correspondence document link(s) for case reference(s): {}",
                deletedRows,
                caseReferences
            );

        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }
}
