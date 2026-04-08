package uk.gov.hmcts.sptribs.common.repositories;

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
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Repository responsible for querying the case_data table.
 */
@RequiredArgsConstructor
@Repository
@Slf4j
public class CaseDataRepository {

    private static final TypeReference<Map<String, JsonNode>> JSON_NODE_MAP = new TypeReference<>() { };
    private static final String CASE_TYPE = "CriminalInjuriesCompensation";
    private static final String JURISDICTION = "ST_CIC";
    private final NamedParameterJdbcTemplate namedParameterJdbcTemplate;
    private final ObjectMapper objectMapper;

    private static final String SELECT_CASE_DATA_BY_CCD_REF_AND_USER_EMAIL =
        "SELECT c.id, c.reference, c.state, c.data::text AS case_data, c.last_modified " +
            "FROM ccd.case_data c " +
            "WHERE c.case_type_id = :caseType AND c.jurisdiction = :jurisdiction " +
            "AND c.reference = :ccdReference " +
            "AND c.state IN :validStates " +
            "AND ( c.data #>> '{cicCaseEmail}' = :userEmail " +
            "OR jsonb_path_exists(c.data, '$.searchCriteria.SearchParties[*] ? (@.EmailAddress == $email)', " +
            "jsonb_build_object('email', :userEmail))) " +
            "ORDER BY c.last_modified DESC LIMIT 1";

    /**
     * Finds a case by CCD reference number and email.
     * Searches in the data JSONB column for the editCicaCaseDetails.cicaReferenceNumber field.
     * Returns the most recently modified case if multiple matches are found.
     *
     * @param ccdReference the CCD reference number to search for (case-insensitive)
     * @return Optional containing the case if found, empty otherwise
     */
    public Optional<CicaCaseResponse> findByCCDReferenceAndEmail(String ccdReference) {
        log.info("Searching for case with CCD reference: {}", ccdReference);

        var params = Map.of(
            "ccdReference", ccdReference,
            "caseType", CASE_TYPE,
            "jurisdiction", JURISDICTION
        );


        //i think i should query then check the email after for better error response .....
        List<CicaCaseResponse> results = namedParameterJdbcTemplate.query(
            SELECT_CASE_DATA_BY_CCD_REF_AND_USER_EMAIL,
            params,
            (rs, rowNum) -> mapToCicaCaseResponse(rs)
        );

        if (results.isEmpty()) {
            log.info("No case found with CCD reference: {}", ccdReference);
            return Optional.empty();
        }

        log.info("Found case with CCD reference: {}", ccdReference);
        return Optional.of(results.getFirst());
    }

    @SneakyThrows
    private CicaCaseResponse mapToCicaCaseResponse(ResultSet rs) {
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
