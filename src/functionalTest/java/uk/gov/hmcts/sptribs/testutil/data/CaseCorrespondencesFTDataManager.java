package uk.gov.hmcts.sptribs.testutil.data;

import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.sptribs.notification.model.Party;
import uk.gov.hmcts.sptribs.notification.persistence.CorrespondenceEntity;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static uk.gov.hmcts.sptribs.testutil.FunctionalTestConstants.KEY_CASE_CORRESPONDENCES_REFERENCE;
import static uk.gov.hmcts.sptribs.testutil.FunctionalTestConstants.TABLE_CASE_CORRESPONDENCES;

@Component
@Profile("functional")
public class CaseCorrespondencesFTDataManager extends FunctionalTestDataManager {


    public List<CorrespondenceEntity> getCorrespondenceEntities(long reference) throws SQLException {
        String sql = "SELECT * FROM " + TABLE_CASE_CORRESPONDENCES + " WHERE " + KEY_CASE_CORRESPONDENCES_REFERENCE + " = ?";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setLong(1, reference);
            ResultSet rs = stmt.executeQuery();

            List<CorrespondenceEntity> correspondences = new ArrayList<>();
            while (rs.next()) {
                CorrespondenceEntity correspondenceEntity = CorrespondenceEntity.builder()
                    .caseReferenceNumber(rs.getLong(KEY_CASE_CORRESPONDENCES_REFERENCE))
                    .id(UUID.fromString(rs.getString("id")))
                    .eventType(rs.getString("event_type"))
                    .sentOn(rs.getTimestamp("sent_on").toInstant()
                        .atOffset(ZoneId.systemDefault().getRules().getOffset(LocalDateTime.now())))
                    .sentFrom(rs.getString("sent_from"))
                    .sentTo(rs.getString("sent_to"))
                    .documentUrl(rs.getString("document_url"))
                    .documentBinaryUrl(rs.getString("document_binary_url"))
                    .documentFilename(rs.getString("document_filename"))
                    .correspondenceType(rs.getString("correspondence_type"))
                    .receivingParty(Party.valueOf(rs.getString("receiving_party")))
                    .build();

                correspondences.add(correspondenceEntity);
            }
            return correspondences;
        }
    }
}
