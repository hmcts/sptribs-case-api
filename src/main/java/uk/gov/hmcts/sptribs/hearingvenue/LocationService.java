package uk.gov.hmcts.sptribs.hearingvenue;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.ccd.sdk.type.DynamicList;
import uk.gov.hmcts.ccd.sdk.type.DynamicListElement;
import uk.gov.hmcts.reform.authorisation.generators.AuthTokenGenerator;
import uk.gov.hmcts.sptribs.hearingvenue.model.HearingVenueResponse;

import javax.servlet.http.HttpServletRequest;

import java.util.List;
import java.util.UUID;

import static org.springframework.http.HttpHeaders.AUTHORIZATION;

@Service
@Slf4j
public class LocationService {

    @Autowired
    private HttpServletRequest httpServletRequest;

    @Autowired
    private AuthTokenGenerator authTokenGenerator;

    @Autowired
    private LocationClient locationClient;

    public DynamicList getHearingVenuesByRegion() {

        final var hearingVenueResponse = getCourtVenues();

        DynamicList hearingVenues = DynamicList
            .builder()
            .value(DynamicListElement.builder().label(String.valueOf(hearingVenueResponse.getAmount())).build())
            .listItems(List.of(DynamicListElement.builder().label(hearingVenueResponse.getDescription()).code(UUID.randomUUID()).build()))
            .build();

        return hearingVenues;
    }

    private HearingVenueResponse getCourtVenues() {
        final var hearingVenueResponse = locationClient.getHearingVenues(
            authTokenGenerator.generate(),
            httpServletRequest.getHeader(AUTHORIZATION),
            "Y",
            "Y",
            "Court",
            "N"
        );

        return hearingVenueResponse;
    }


}
