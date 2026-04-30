package uk.gov.hmcts.sptribs.mapper;

import org.junit.jupiter.api.Test;
import uk.gov.hmcts.sptribs.common.repositories.model.CicaCaseEntity;
import uk.gov.hmcts.sptribs.controllers.model.CicaCaseResponse;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static uk.gov.hmcts.sptribs.testutil.CicaCaseTestHelper.createCicaCaseEntity;

class CicaCaseMapperTest {

    private final CicaCaseMapper mapper = new CicaCaseMapper();

    @Test
    void shouldReturnNullWhenEntityIsNull() {
        // When
        CicaCaseResponse result = mapper.toResponse(null);

        // Then
        assertNull(result);
    }

    @Test
    void shouldMapEntityToResponse() {
        // Given
        String ccdRef = "1234567891234567";
        CicaCaseEntity entity = createCicaCaseEntity(ccdRef);

        // When
        CicaCaseResponse result = mapper.toResponse(entity);

        // Then
        assertNotNull(result);
        assertEquals(ccdRef, result.getId());
        assertEquals(entity.getState(), result.getState());
        assertEquals(entity.getData(), result.getData());
    }
}
