package uk.gov.hmcts.sptribs.flag.refdata;

import feign.FeignException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.sptribs.common.service.AuthorisationService;

@Service
@Slf4j
public class FlagTypeService {

    @Autowired
    AuthorisationService authorisationService;

    @Autowired
    private FlagTypeClient flagTypeClient;

    public Object getFlagTypes() {

        try {

            final String authorisation = authorisationService.getAuthorisation();
            String serviceAuthorization = authorisationService.getServiceAuthorization();
            Object list = flagTypeClient.getFlags(
                serviceAuthorization,
                authorisation);
            log.info(list.toString());
            return list;
        } catch (FeignException exception) {
            log.error("Unable to get flag type data from reference data with exception {}",
                exception.getMessage());
        }
        return null;
    }

}
