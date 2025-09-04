package uk.gov.hmcts.sptribs.judicialrefdata.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Builder
@Data
@NoArgsConstructor
@AllArgsConstructor
@JsonNaming(value = PropertyNamingStrategies.SnakeCaseStrategy.class)
@JsonIgnoreProperties(ignoreUnknown = true)
public class UserProfileRefreshResponse {

    private String sidamId;

    private String objectId;

    private String knownAs;

    private String surname;

    private String fullName;

    private String title;

    private String postNominals;

    private String emailId;

    private String personalCode;

    private List<AppointmentRefreshResponse> appointments;

    private List<AuthorisationRefreshResponse> authorisations;
}
