package uk.gov.hmcts.sptribs.document.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;
import uk.gov.hmcts.sptribs.common.repositories.CaseDocumentTypesRepository;
import uk.gov.hmcts.sptribs.document.model.CaseDocumentType;
import uk.gov.hmcts.sptribs.document.model.CaseDocumentTypesEntity;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CaseDocumentTypesCacheTest {

    @Mock
    private CaseDocumentTypesRepository repository;

    @InjectMocks
    private CaseDocumentTypesCache caseDocumentTypesCache;

    @Test
    void shouldLoadAndReturnDocumentTypeIds() {
        //given
        long id = 1L;
        List<CaseDocumentTypesEntity> entities = new ArrayList<>();

        for (CaseDocumentType type : CaseDocumentType.values()) {
            entities.add(buildDocumentTypeEntity(id++, type));
        }

        when(repository.findAll()).thenReturn(entities);
        caseDocumentTypesCache.reload();

        // when
        Long documentTypeId = caseDocumentTypesCache.getId(CaseDocumentType.values()[0]);

        // then
        assertThat(documentTypeId).isEqualTo(1L);
        assertThat(caseDocumentTypesCache.getId(CaseDocumentType.values()[0]))
            .isEqualTo(1L);
    }

    @Test
    void shouldThrowExceptionWhenDocumentTypeMissing() {
        //given
        CaseDocumentType firstType = CaseDocumentType.values()[0];
        CaseDocumentTypesEntity entity = new CaseDocumentTypesEntity();
        entity.setId(1L);
        entity.setCode(firstType.name());

        //when
        when(repository.findAll()).thenReturn(List.of(entity));

        //then
        assertThatThrownBy(() -> caseDocumentTypesCache.reload())
            .isInstanceOf(IllegalStateException.class)
            .hasMessageContaining("Missing document type in database");
    }

    @Test
    void shouldThrowExceptionWhenDocumentTypeIsUnknown() {
        CaseDocumentType knownType = CaseDocumentType.values()[0];
        CaseDocumentType unknownType = CaseDocumentType.values()[1];

        ReflectionTestUtils.setField(
            caseDocumentTypesCache,
            "caseDocumentTypeIdMap",
            Map.of(knownType, 1L)
        );

        assertThatThrownBy(() -> caseDocumentTypesCache.getId(unknownType))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessage("Unknown document type code: " + unknownType);
    }

    @Test
    void shouldLoadCacheOnlyOnFirstAccess() {
        // given
        List<CaseDocumentTypesEntity> entities = new ArrayList<>();

        long id = 1L;
        for (CaseDocumentType type : CaseDocumentType.values()) {
            entities.add(buildDocumentTypeEntity(id++, type));
        }

        when(repository.findAll()).thenReturn(entities);

        // when
        caseDocumentTypesCache.getId(CaseDocumentType.values()[0]);
        caseDocumentTypesCache.getId(CaseDocumentType.values()[1]);

        // then
        verify(repository, times(1)).findAll();
    }

    private CaseDocumentTypesEntity buildDocumentTypeEntity(long id, CaseDocumentType type) {
        CaseDocumentTypesEntity entity = new CaseDocumentTypesEntity();
        entity.setId(id);
        entity.setCode(type.name());
        return entity;
    }
}
