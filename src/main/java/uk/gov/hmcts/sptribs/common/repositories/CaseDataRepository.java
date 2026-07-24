package uk.gov.hmcts.sptribs.common.repositories;

import uk.gov.hmcts.sptribs.common.repositories.model.CicaCaseEntity;

import java.util.Optional;

public interface CaseDataRepository {

    boolean checkCaseExists(String ccdReference);

    boolean checkIfUserHasAccessToCase(String ccdReference, String userEmail);

    boolean checkIfUserHasAccessToCase(String ccdReference, String userEmail, String postcode);

    Optional<CicaCaseEntity> findCase(String ccdReference, String userEmail);

    Optional<CicaCaseEntity> findCase(String ccdReference, String userEmail, String postcode);

}
