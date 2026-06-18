package uk.gov.hmcts.sptribs.testutil;

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
                    .isDraft(rs.getBoolean("is_draft"))
                    .sentToApplicantViaContactParties(rs.getBoolean("sent_to_applicant_via_contact_parties"))
                    .updatedAt(rs.getTimestamp("updated_at").toInstant()
                        .atOffset(ZoneId.systemDefault().getRules().getOffset(LocalDateTime.now())))
                    .build();

                documents.add(documentEntity);
            }
            return documents;
        }
    }

    public void saveTestDocumentEntity(long reference) throws SQLException {
        String sql = "INSERT INTO " + TABLE_CASE_DOCUMENTS + " ("
            + KEY_CASE_DOCUMENTS_REFERENCE
            + ", saved_at"
            + ", document_url"
            + ", document_binary_url"
            + ", document_filename"
            + ", category_id"
            + ", document_type_id"
            + ", is_draft"
            + ", sent_to_applicant_via_contact_parties"
            + ", updated_at"
            + ") VALUES (?, ?, ?, ?, ?, ?, CAST(2 AS bigint), ?, ?, ?)";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setLong(1, reference);
            stmt.setTimestamp(2, Timestamp.valueOf(LocalDateTime.now()));
            stmt.setString(3,
                "http://dm-store.service.core-compute.internal/documents/467d1fd1-75bd-4760-b993-51be259daebe");
            stmt.setString(4,
                "http://dm-store.service.core-compute.internal/documents/467d1fd1-75bd-4760-b993-51be259daebe/binary");
            stmt.setString(5, "mockFile.pdf");
            stmt.setString(6, "C");
            stmt.setBoolean(7, false);
            stmt.setBoolean(8, false);
            stmt.setTimestamp(9, Timestamp.valueOf(LocalDateTime.now()));
            stmt.executeUpdate();
        }
    }
}
