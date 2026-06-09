package uk.gov.hmcts.sptribs.document.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.sptribs.common.repositories.CaseDocumentTypesRepository;
import uk.gov.hmcts.sptribs.document.model.CaseDocumentType;
import uk.gov.hmcts.sptribs.document.model.CaseDocumentTypesEntity;

import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class CaseDocumentTypesCache {

    private final CaseDocumentTypesRepository caseDocumentTypesRepository;

    private volatile Map<CaseDocumentType, Long> caseDocumentTypeIdMap = Map.of();

    public void reload() {
        Map<CaseDocumentType, Long> loadedTypes = caseDocumentTypesRepository.findAll()
            .stream()
            .collect(Collectors.toMap(
                entity -> CaseDocumentType.valueOf(entity.getCode()),
                CaseDocumentTypesEntity::getId
            ));

        validate(loadedTypes);

        caseDocumentTypeIdMap = loadedTypes;

        log.info("Loaded {} document types", caseDocumentTypeIdMap.size());
    }

    public Long getId(CaseDocumentType code) {

        if (caseDocumentTypeIdMap.isEmpty()) {
            reload();
        }

        Long id = caseDocumentTypeIdMap.get(code);

        if (id == null) {
            throw new IllegalArgumentException(
                "Unknown document type code: " + code
            );
        }

        return id;
    }

    private void validate(Map<CaseDocumentType, Long> loadedTypes) {
        for (CaseDocumentType type : CaseDocumentType.values()) {
            if (!loadedTypes.containsKey(type)) {
                throw new IllegalStateException(
                    "Missing document type in database: " + type
                );
            }
        }
    }
}
