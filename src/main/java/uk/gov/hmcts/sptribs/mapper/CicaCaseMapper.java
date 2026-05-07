package uk.gov.hmcts.sptribs.mapper;

import org.springframework.stereotype.Component;
import uk.gov.hmcts.sptribs.common.repositories.model.CicaCaseEntity;
import uk.gov.hmcts.sptribs.controllers.model.CicaCaseResponse;

@Component
public class CicaCaseMapper {

    public CicaCaseResponse toResponse(CicaCaseEntity entity) {
        if (entity == null) {
            return null;
        }

        return CicaCaseResponse.builder()
            .id(entity.getId())
            .state(entity.getState())
            .data(entity.getData())
            .build();
    }
}
