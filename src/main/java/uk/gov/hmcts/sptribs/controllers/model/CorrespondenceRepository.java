package uk.gov.hmcts.sptribs.controllers.model;

import org.springframework.data.repository.CrudRepository;
import uk.gov.hmcts.sptribs.notification.model.Correspondence;

public interface CorrespondenceRepository extends CrudRepository<Correspondence, Integer> {
}
