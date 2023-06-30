package uk.gov.hmcts.sptribs.ciccase.model;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import uk.gov.hmcts.ccd.sdk.api.CCD;
import uk.gov.hmcts.sptribs.ciccase.model.access.CitizenAccess;
import uk.gov.hmcts.sptribs.ciccase.model.access.DefaultAccess;
import uk.gov.hmcts.sptribs.common.MappableObject;
import uk.gov.hmcts.sptribs.document.model.CaseworkerCICDocument;

import java.time.LocalDate;

@JsonIgnoreProperties(ignoreUnknown = true)
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder(toBuilder = true)
@JsonNaming(PropertyNamingStrategies.UpperCamelCaseStrategy.class)
public class DssMessage implements MappableObject {

    @CCD(
        label = "Date Received",
        access = {DefaultAccess.class, CitizenAccess.class}
    )
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
    private LocalDate dateReceived;

    @CCD(
        label = "Received From",
        access = {DefaultAccess.class, CitizenAccess.class}
    )
    private String receivedFrom;

    @CCD(
        label = "Message",
        access = {DefaultAccess.class, CitizenAccess.class}
    )
    private String message;

    @CCD(
        label = "Document Relevance",
        access = {DefaultAccess.class, CitizenAccess.class}
    )
    private String documentRelevance;

    @CCD(
        label = "Other information uploaded documents",
        access = {DefaultAccess.class, CitizenAccess.class}
    )
    private CaseworkerCICDocument otherInfoDocument;

}
