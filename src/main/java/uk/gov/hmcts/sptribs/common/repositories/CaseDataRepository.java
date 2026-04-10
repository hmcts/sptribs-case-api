package uk.gov.hmcts.sptribs.common.repositories;

import uk.gov.hmcts.sptribs.common.repositories.model.CicaCaseEntity;
import uk.gov.hmcts.sptribs.controllers.model.CicaCaseResponse;

import java.util.Optional;

public interface CaseDataRepository {

    boolean checkCaseExists(String ccdReference);
    Optional<CicaCaseEntity> findCase(String ccdReference, String userEmail);
}
