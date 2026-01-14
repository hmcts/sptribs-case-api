package uk.gov.hmcts.sptribs.ciccase.repository;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;
import uk.gov.hmcts.sptribs.controllers.model.CicaCaseResponse;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Slf4j
@Repository
@RequiredArgsConstructor
public class CicaCaseRepository {

    private static final TypeReference<Map<String, JsonNode>> JSON_NODE_MAP = new TypeReference<>() { };

    private static final String CASE_TYPE = "CriminalInjuriesCompensation";
    private static final String JURISDICTION = "ST_CIC";
    private static final String CICA_REFERENCE_JSON_PATH = "{editCicaCaseDetails,cicaReferenceNumber}";

    private final NamedParameterJdbcTemplate namedParameterJdbcTemplate;
    private final ObjectMapper objectMapper;

    /**
     * Finds a case by CICA reference number.
     * Searches in the data JSONB column for the editCicaCaseDetails.cicaReferenceNumber field.
     * Returns the most recently modified case if multiple matches are found.
     *
     * @param cicaReference the CICA reference number to search for (case-insensitive)
     * @return Optional containing the case if found, empty otherwise
     */
    public Optional<CicaCaseResponse> findByCicaReference(String cicaReference) {
        log.info("Searching for case with CICA reference: {}", cicaReference);

        var params = Map.of(
            "cicaReference", cicaReference.toUpperCase(),
            "caseType", CASE_TYPE,
            "jurisdiction", JURISDICTION
        );

        String sql = """
            SELECT
                c.id,
                c.reference,
                c.state,
                c.data::text AS case_data,
                c.last_modified
            FROM ccd.case_data c
            WHERE c.case_type_id = :caseType
              AND c.jurisdiction = :jurisdiction
              AND UPPER(c.data #>> '%s') = :cicaReference
            ORDER BY c.last_modified DESC
            LIMIT 1
            """.formatted(CICA_REFERENCE_JSON_PATH);

        List<CicaCaseResponse> results = namedParameterJdbcTemplate.query(
            sql,
            params,
            (rs, rowNum) -> mapToCicaCaseResponse(rs)
        );

        if (results.isEmpty()) {
            log.info("No case found with CICA reference: {}", cicaReference);
            return Optional.empty();
        }

        log.info("Found case with CICA reference: {}", cicaReference);
        return Optional.of(results.get(0));
    }

    @SneakyThrows
    private CicaCaseResponse mapToCicaCaseResponse(ResultSet rs) throws SQLException {
        Long reference = rs.getObject("reference", Long.class);
        String state = rs.getString("state");
        String caseDataJson = rs.getString("case_data");

        Map<String, JsonNode> caseData = objectMapper.readValue(caseDataJson, JSON_NODE_MAP);

        return CicaCaseResponse.builder()
            .id(String.valueOf(reference))
            .state(state)
            .data(caseData)
            .build();
    }
}


