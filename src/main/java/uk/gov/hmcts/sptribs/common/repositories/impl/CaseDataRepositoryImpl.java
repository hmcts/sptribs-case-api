package uk.gov.hmcts.sptribs.common.repositories.impl;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;
import uk.gov.hmcts.sptribs.ciccase.model.State;
import uk.gov.hmcts.sptribs.common.repositories.CaseDataRepository;
import uk.gov.hmcts.sptribs.common.repositories.model.CicaCaseEntity;

import java.sql.ResultSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static uk.gov.hmcts.sptribs.ciccase.model.State.DSS_Draft;
import static uk.gov.hmcts.sptribs.ciccase.model.State.DSS_Expired;
import static uk.gov.hmcts.sptribs.ciccase.model.State.DSS_Submitted;
import static uk.gov.hmcts.sptribs.ciccase.model.State.Draft;
import static uk.gov.hmcts.sptribs.ciccase.model.State.Submitted;

/**
 * Repository responsible for querying the case_data table.
 */
@RequiredArgsConstructor
@Repository
@Slf4j
public class CaseDataRepositoryImpl implements CaseDataRepository {

    private static final TypeReference<Map<String, JsonNode>> JSON_NODE_MAP = new TypeReference<>() { };
    private static final String CASE_TYPE = "CriminalInjuriesCompensation";
    private static final String JURISDICTION = "ST_CIC";
    private static final List<String> INVALID_STATES = List.of(Draft.getName(), DSS_Draft.getName(),
        DSS_Expired.getName());
    private final NamedParameterJdbcTemplate namedParameterJdbcTemplate;
    private final ObjectMapper objectMapper;

    private static final String CHECK_CASE_EXISTS_AND_CORRECT_STATE =
        "SELECT 1 FROM ccd.case_data c " +
            "WHERE c.case_type_id = :caseType AND c.jurisdiction = :jurisdiction " +
            "AND c.reference = :ccdReference " +
            "AND c.state NOT IN (:invalidStates)";

    private static final String SELECT_CASE_DATA_BY_CCD_REF_AND_EMAIL =
        "SELECT c.id, c.reference, c.state, c.data::text AS case_data, c.last_modified " +
            "FROM ccd.case_data c " +
            "WHERE c.case_type_id = :caseType " +
            "AND c.jurisdiction = :jurisdiction " +
            "AND c.reference = :ccdReference " +
            "AND ( " +
            "  c.data #>> '{cicCaseEmail}' = :userEmail " +
            "  OR EXISTS ( " +
            "    SELECT 1 " +
            "    FROM jsonb_array_elements( " +
            "      COALESCE(c.data->'SearchCriteria'->'SearchParties', '[]'::jsonb) " +
            "    ) sp " +
            "    WHERE sp->'value'->>'EmailAddress' = :userEmail " +
            "  ) " +
            ") " +
            "ORDER BY c.last_modified DESC " +
            "LIMIT 1";

    @Override
    public boolean checkCaseExists(String ccdReference) {
        log.info("Searching for case with CCD reference: {}", ccdReference);

        var params = Map.of(
            "ccdReference", Long.valueOf(ccdReference),
            "caseType", CASE_TYPE,
            "jurisdiction", JURISDICTION,
            "invalidStates", INVALID_STATES
        );

        return namedParameterJdbcTemplate.queryForObject(CHECK_CASE_EXISTS_AND_CORRECT_STATE, params, Integer.class) != null;
    }

    @Override
    public Optional<CicaCaseEntity> findCase(String ccdReference, String userEmail) {
        log.info("Checking case has correct email for CCD reference: {}", ccdReference);

        var params = Map.of(
            "ccdReference", Long.valueOf(ccdReference),
            "caseType", CASE_TYPE,
            "jurisdiction", JURISDICTION,
            "userEmail", userEmail
        );

        List<CicaCaseEntity> results = namedParameterJdbcTemplate.query(
            SELECT_CASE_DATA_BY_CCD_REF_AND_EMAIL,
            params,
            (rs, rowNum) -> mapToCicaCaseEntity(rs)
        );

        log.info("Email matched for CCD reference: {}", ccdReference);
        return results.stream().findFirst();
    }

    @SneakyThrows
    private CicaCaseEntity mapToCicaCaseEntity(ResultSet rs) {
        Long reference = rs.getObject("reference", Long.class);
        String state = rs.getString("state");
        String caseDataJson = rs.getString("case_data");

        Map<String, JsonNode> caseData = objectMapper.readValue(caseDataJson, JSON_NODE_MAP);

        return CicaCaseEntity.builder()
            .id(String.valueOf(reference))
            .state(state)
            .data(caseData)
            .build();
    }
}
