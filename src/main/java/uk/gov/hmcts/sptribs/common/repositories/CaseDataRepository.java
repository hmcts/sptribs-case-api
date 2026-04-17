package uk.gov.hmcts.sptribs.common.repositories;

import uk.gov.hmcts.sptribs.common.repositories.model.CicaCaseEntity;

import java.util.Optional;

public interface CaseDataRepository {

    boolean checkCaseExists(String ccdReference);

    Optional<CicaCaseEntity> findCase(String ccdReference, String userEmail);

}
