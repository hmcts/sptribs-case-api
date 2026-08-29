package uk.gov.hmcts.sptribs.ciccase.repository;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import uk.gov.hmcts.sptribs.controllers.model.CicaCaseResponse;

import java.sql.ResultSet;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CicaCaseRepositoryTest {

    @Mock
    private NamedParameterJdbcTemplate namedParameterJdbcTemplate;

    @Captor
    private ArgumentCaptor<String> sqlCaptor;

    @Captor
    private ArgumentCaptor<Map<String, Object>> paramsCaptor;

    @Captor
    private ArgumentCaptor<RowMapper<CicaCaseResponse>> rowMapperCaptor;

    private CicaCaseRepository cicaCaseRepository;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @BeforeEach
    void setUp() {
        cicaCaseRepository = new CicaCaseRepository(namedParameterJdbcTemplate, objectMapper);
    }

    @Test
    void shouldReturnCaseWhenFoundByCicaReference() {
        // Given
        String cicaReference = "X12345";
        String caseDataJson = "{\"testKey\":\"testValue\"}";

        when(namedParameterJdbcTemplate.query(
            sqlCaptor.capture(),
            paramsCaptor.capture(),
            rowMapperCaptor.capture()
        )).thenAnswer(invocation -> {
            ResultSet rs = mock(ResultSet.class);
            when(rs.getObject("reference", Long.class)).thenReturn(1624351572550045L);
            when(rs.getString("state")).thenReturn("Submitted");
            when(rs.getString("case_data")).thenReturn(caseDataJson);
            RowMapper<CicaCaseResponse> mapper = invocation.getArgument(2);
            return List.of(mapper.mapRow(rs, 0));
        });

        // When
        Optional<CicaCaseResponse> result = cicaCaseRepository.findByCicaReference(cicaReference);

        // Then
        assertThat(result).isPresent();
        assertThat(result.get().getId()).isEqualTo("1624351572550045");
        assertThat(result.get().getState()).isEqualTo("Submitted");
        assertThat(result.get().getData().get("testKey").asText()).isEqualTo("testValue");

        // Verify SQL contains expected elements
        assertThat(sqlCaptor.getValue()).contains("ccd.case_data");
        assertThat(sqlCaptor.getValue()).contains("{editCicaCaseDetails,cicaReferenceNumber}");
        assertThat(sqlCaptor.getValue()).contains("last_modified");
        assertThat(sqlCaptor.getValue()).contains("LIMIT 1");

        // Verify params
        assertThat(paramsCaptor.getValue()).containsEntry("cicaReference", "X12345");
        assertThat(paramsCaptor.getValue()).containsEntry("caseType", "CriminalInjuriesCompensation");
        assertThat(paramsCaptor.getValue()).containsEntry("jurisdiction", "ST_CIC");
    }

    @Test
    void shouldReturnEmptyOptionalWhenNoCaseFound() {
        // Given
        String cicaReference = "X99999";
        when(namedParameterJdbcTemplate.query(
            anyString(),
            paramsCaptor.capture(),
            rowMapperCaptor.capture()
        )).thenReturn(Collections.emptyList());

        // When
        Optional<CicaCaseResponse> result = cicaCaseRepository.findByCicaReference(cicaReference);

        // Then
        assertThat(result).isEmpty();
    }

    @Test
    void shouldConvertCicaReferenceToUpperCaseForQuery() {
        // Given
        String cicaReference = "x12345";
        when(namedParameterJdbcTemplate.query(
            anyString(),
            paramsCaptor.capture(),
            rowMapperCaptor.capture()
        )).thenReturn(Collections.emptyList());

        // When
        cicaCaseRepository.findByCicaReference(cicaReference);

        // Then - verify that the query is called with the uppercase reference in params
        assertThat(paramsCaptor.getValue().get("cicaReference")).isEqualTo("X12345");
    }

    @Test
    void shouldReturnFirstCaseWhenMultipleFound() {
        // Given
        String cicaReference = "X12345";
        CicaCaseResponse firstCase = CicaCaseResponse.builder()
            .id("1624351572550045")
            .state("Submitted")
            .data(Map.of())
            .build();
        CicaCaseResponse secondCase = CicaCaseResponse.builder()
            .id("1624351572550046")
            .state("Draft")
            .data(Map.of())
            .build();

        when(namedParameterJdbcTemplate.query(
            anyString(),
            paramsCaptor.capture(),
            rowMapperCaptor.capture()
        )).thenReturn(List.of(firstCase, secondCase));

        // When
        Optional<CicaCaseResponse> result = cicaCaseRepository.findByCicaReference(cicaReference);

        // Then - should return the first case (most recently modified due to ORDER BY)
        assertThat(result).isPresent();
        assertThat(result.get().getId()).isEqualTo("1624351572550045");
    }

    @Test
    void shouldQueryWithCorrectCaseTypeAndJurisdiction() {
        // Given
        String cicaReference = "X12345";
        when(namedParameterJdbcTemplate.query(
            anyString(),
            paramsCaptor.capture(),
            rowMapperCaptor.capture()
        )).thenReturn(Collections.emptyList());

        // When
        cicaCaseRepository.findByCicaReference(cicaReference);

        // Then - verify that the query includes the correct case type and jurisdiction
        Map<String, Object> params = paramsCaptor.getValue();
        assertThat(params.get("caseType")).isEqualTo("CriminalInjuriesCompensation");
        assertThat(params.get("jurisdiction")).isEqualTo("ST_CIC");
    }
}
