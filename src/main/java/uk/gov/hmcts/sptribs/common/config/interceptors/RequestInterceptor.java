package uk.gov.hmcts.sptribs.common.config.interceptors;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;
import uk.gov.hmcts.reform.authorisation.validators.AuthTokenValidator;

import java.util.List;

import static uk.gov.hmcts.sptribs.common.config.ControllerConstants.BEARER_PREFIX;
import static uk.gov.hmcts.sptribs.common.config.ControllerConstants.SERVICE_AUTHORIZATION;

@Slf4j
@Component
public class RequestInterceptor implements HandlerInterceptor {

    @Autowired
    @Lazy
    private AuthTokenValidator tokenValidator;

    @Value("#{'${s2s-authorised.services}'.split(',')}")
    private List<String> authorisedServices;

    private static final String NO_AUTH_SERVICE_LIST = "List of authorised services is not yet configured";
    private static final String TOKEN_MISSING = "Service authorization token is missing";
    private static final String CCD_DATA_SERVICE = "ccd_data";
    private static final String CCD_PERSISTENCE_ENDPOINT_PREFIX = "/ccd-persistence";
    private static final String CCD_PERSISTENCE_UNAUTHORISED =
        "Service not authorised to access ccd-persistence endpoints";

    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        if (authorisedServices == null || authorisedServices.isEmpty()) {
            log.error(NO_AUTH_SERVICE_LIST);
            throw new UnAuthorisedServiceException(NO_AUTH_SERVICE_LIST);
        }

        String serviceAuthToken = request.getHeader(SERVICE_AUTHORIZATION);
        if (serviceAuthToken == null) {
            log.error(TOKEN_MISSING);
            throw new UnAuthorisedServiceException(TOKEN_MISSING);
        }

        String serviceName;
        if (!serviceAuthToken.startsWith(BEARER_PREFIX)) {
            serviceName = tokenValidator.getServiceName(BEARER_PREFIX + serviceAuthToken);
        } else {
            serviceName = tokenValidator.getServiceName(serviceAuthToken);
        }

        if (!authorisedServices.contains(serviceName)) {
            log.error("Service {} not allowed to trigger save and sign out callback ", serviceName);
            throw new UnAuthorisedServiceException("Service " + serviceName + " not in configured list for accessing callback");
        }

        if (request.getRequestURI().startsWith(CCD_PERSISTENCE_ENDPOINT_PREFIX) && !CCD_DATA_SERVICE.equals(serviceName)) {
            log.error(CCD_PERSISTENCE_UNAUTHORISED);
            throw new UnAuthorisedServiceException(CCD_PERSISTENCE_UNAUTHORISED);
        }

        return true;
    }
}
