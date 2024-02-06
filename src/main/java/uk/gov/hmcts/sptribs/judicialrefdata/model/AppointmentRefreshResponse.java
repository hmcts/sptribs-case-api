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
public class AppointmentRefreshResponse {

    private String baseLocationId;

    private String epimmsId;

    private String courtName;

    private String cftRegionID;

    private String cftRegion;

    private String locationId;

    private String location;

    private String isPrincipalAppointment;

    private String appointment;

    private String appointmentType;

    private String serviceCode;

    private List<String> roles;

    private String startDate;

    private String endDate;
}
