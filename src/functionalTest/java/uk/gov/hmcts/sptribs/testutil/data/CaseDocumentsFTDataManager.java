package uk.gov.hmcts.sptribs.testutil.data;

import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.sptribs.document.model.DocumentEntity;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;

import static uk.gov.hmcts.sptribs.testutil.FunctionalTestConstants.KEY_CASE_DOCUMENTS_REFERENCE;
import static uk.gov.hmcts.sptribs.testutil.FunctionalTestConstants.TABLE_CASE_DOCUMENTS;

@Component
@Profile("functional")
public class CaseDocumentsFTDataManager extends FunctionalTestDataManager {

    public CaseDocumentsFTDataManager() {
        super();
    }

    public List<DocumentEntity> getDocumentEntities(long reference) throws SQLException {
        String sql = "SELECT * FROM " + TABLE_CASE_DOCUMENTS + " WHERE " + KEY_CASE_DOCUMENTS_REFERENCE + " = ?";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setLong(1, reference);
            ResultSet rs = stmt.executeQuery();

            List<DocumentEntity> documents = new ArrayList<>();
            while (rs.next()) {
                Timestamp updatedAt = rs.getTimestamp("updated_at");
                DocumentEntity documentEntity = DocumentEntity.builder()
                    .caseReferenceNumber(rs.getLong(KEY_CASE_DOCUMENTS_REFERENCE))
                    .id(rs.getInt("id"))
                    .savedAt(rs.getTimestamp("saved_at").toInstant()
                        .atOffset(ZoneId.systemDefault().getRules().getOffset(LocalDateTime.now())))
                    .documentUrl(rs.getString("document_url"))
                    .documentBinaryUrl(rs.getString("document_binary_url"))
                    .documentFilename(rs.getString("document_filename"))
                    .documentTypeName(rs.getString("document_type_name"))
                    .caseDocumentTypeId(rs.getLong("case_document_type_id"))
                    .updatedAt(updatedAt == null
                        ? null
                        : updatedAt.toInstant()
                        .atZone(ZoneId.systemDefault())
                        .toOffsetDateTime())
                    .build();

                documents.add(documentEntity);
            }
            return documents;
        }
    }

    public int countCaseDocuments(long caseId) {
        String sql = "SELECT COUNT(*) FROM public.case_documents WHERE case_reference_number = ?";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setLong(1, caseId);
            ResultSet rs = stmt.executeQuery();
            rs.next();
            return rs.getInt(1);
        } catch (SQLException e) {
            throw new RuntimeException("Failed to count case documents for caseId: " + caseId, e);
        }
    }


    public static void saveTestDocumentEntity(long reference, String testDocumentUrl, String testDocumentFilename,
                                              String documentTypeName, String caseDocumentTypeId, Timestamp savedAt) throws SQLException {
        String sql = "INSERT INTO " + TABLE_CASE_DOCUMENTS + " ("
            + KEY_CASE_DOCUMENTS_REFERENCE
            + ", saved_at"
            + ", document_url"
            + ", document_binary_url"
            + ", document_filename"
            + ", document_type_name"
            + ", case_document_type_id"
            + ", updated_at"
            + ") VALUES (?, ?, ?, ?, ?, ?, CAST(" + caseDocumentTypeId + " AS bigint), ?)";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setLong(1, reference);
            stmt.setTimestamp(2, savedAt);
            stmt.setString(3, testDocumentUrl);
            stmt.setString(4, testDocumentUrl + "/binary");
            stmt.setString(5, testDocumentFilename);
            stmt.setString(6, documentTypeName);
            stmt.setTimestamp(7, savedAt);
            stmt.executeUpdate();
        }
    }
}
