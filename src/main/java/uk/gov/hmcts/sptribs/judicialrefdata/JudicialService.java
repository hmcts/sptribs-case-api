package uk.gov.hmcts.sptribs.judicialrefdata;

import feign.FeignException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.ccd.sdk.type.DynamicList;
import uk.gov.hmcts.ccd.sdk.type.DynamicListElement;
import uk.gov.hmcts.reform.authorisation.generators.AuthTokenGenerator;
import uk.gov.hmcts.sptribs.judicialrefdata.model.UserProfileRefreshResponse;

import javax.servlet.http.HttpServletRequest;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

import static org.springframework.http.HttpHeaders.AUTHORIZATION;

@Service
@Slf4j
public class JudicialService {

    @Autowired
    private HttpServletRequest httpServletRequest;

    @Autowired
    private AuthTokenGenerator authTokenGenerator;

    @Autowired
    private JudicialClient judicialClient;

    private static final String SERVICE_NAME = "DIVORCE";

    public DynamicList getAllUsers() {
        final var users = getUsers();
        return populateRegionDynamicList(users);
    }

    private UserProfileRefreshResponse[] getUsers() {
        ResponseEntity<UserProfileRefreshResponse[]> regionResponseEntity = null;

        try {
            regionResponseEntity = judicialClient.getUserProfiles(
                authTokenGenerator.generate(),
                httpServletRequest.getHeader(AUTHORIZATION),
                JudicialUsersRequest.builder()
                    .ccdServiceName(SERVICE_NAME)
                    .build());
        } catch (FeignException exception) {
            log.error("Unable to get user profile data from reference data with exception {}",
                exception.getMessage());
        }

        return Optional.ofNullable(regionResponseEntity)
            .map(response ->
                Optional.ofNullable(response.getBody())
                    .orElseGet(() -> null)
            )
            .orElseGet(() -> null);
    }


    private DynamicList populateRegionDynamicList(UserProfileRefreshResponse... userProfiles) {
        List<String> regionList = Objects.nonNull(userProfiles)
            ? Arrays.asList(userProfiles).stream().map(v -> v.getFullName()).collect(Collectors.toList())
            : new ArrayList<>();

        List<DynamicListElement> regionDynamicList = regionList
            .stream()
            .sorted()
            .map(region -> DynamicListElement.builder().label(region).code(UUID.randomUUID()).build())
            .collect(Collectors.toList());

        return DynamicList
            .builder()
            .value(DynamicListElement.builder().label("judicialUser").code(UUID.randomUUID()).build())
            .listItems(regionDynamicList)
            .build();
    }
}
