package uk.gov.hmcts.sptribs.common.repositories.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentMatchers;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import uk.gov.hmcts.sptribs.common.repositories.model.CicaCaseEntity;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyMap;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.sptribs.ciccase.model.State.DSS_Expired;

@ExtendWith(MockitoExtension.class)
class CaseDataRepositoryImplTest {

    @Mock
    private NamedParameterJdbcTemplate jdbcTemplate;

    @Mock
    private ObjectMapper objectMapper;

    @InjectMocks
    private CaseDataRepositoryImpl repository;

    @Test
    void shouldReturnTrueWhenCountGreaterThanZero() {
        when(jdbcTemplate.queryForObject(anyString(), anyMap(), eq(Integer.class)))
            .thenReturn(1);

        boolean result = repository.checkCaseExists("123");

        assertThat(result).isTrue();
        verify(jdbcTemplate).queryForObject(
            argThat(sql -> sql.contains("c.state <> :expiredState")),
            ArgumentMatchers.<Map<String, ?>>argThat(params -> DSS_Expired.getName().equals(params.get("expiredState"))
                && !params.containsKey("invalidStates")),
            eq(Integer.class));
    }

    @Test
    void shouldReturnFalseWhenCountIsZero() {
        when(jdbcTemplate.queryForObject(anyString(), anyMap(), eq(Integer.class)))
            .thenReturn(0);

        boolean result = repository.checkCaseExists("123");

        assertThat(result).isFalse();
    }

    @Test
    void shouldReturnCaseWhenResultExists() {
        CicaCaseEntity entity = CicaCaseEntity.builder()
            .id("123")
            .state("Submitted")
            .data(Map.of())
            .build();

        when(jdbcTemplate.query(
            anyString(),
            anyMap(),
            org.mockito.ArgumentMatchers.<RowMapper<CicaCaseEntity>>any()
        )).thenReturn(List.of(entity));

        Optional<CicaCaseEntity> result = repository.findCase("123", "test@test.com");

        assertThat(result).isPresent();
        assertThat(result.get().getId()).isEqualTo("123");
    }

    @Test
    void shouldReturnEmptyWhenNoResults() {
        when(jdbcTemplate.query(
            anyString(),
            anyMap(),
            org.mockito.ArgumentMatchers.<org.springframework.jdbc.core.RowMapper<CicaCaseEntity>>any()
        ))
            .thenReturn(List.of());

        Optional<CicaCaseEntity> result = repository.findCase("123", "test@test.com");

        assertThat(result).isEmpty();
    }

    @Test
    void shouldReturnTrueWhenUserHasAccessToCase() {
        CicaCaseEntity entity = CicaCaseEntity.builder()
            .id("123")
            .state("Submitted")
            .data(Map.of())
            .build();

        when(jdbcTemplate.query(
            anyString(),
            anyMap(),
            org.mockito.ArgumentMatchers.<RowMapper<CicaCaseEntity>>any()
        )).thenReturn(List.of(entity));

        boolean result = repository.checkIfUserHasAccessToCase("123", "test@test.com");

        assertThat(result).isTrue();
    }

    @Test
    void shouldReturnFalseWhenUserHasNoAccessToCase() {
        when(jdbcTemplate.query(
            anyString(),
            anyMap(),
            org.mockito.ArgumentMatchers.<RowMapper<CicaCaseEntity>>any()
        )).thenReturn(List.of());

        boolean result = repository.checkIfUserHasAccessToCase("123", "test@test.com");

        assertThat(result).isFalse();
    }
}
