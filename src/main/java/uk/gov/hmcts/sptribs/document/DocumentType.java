package uk.gov.hmcts.sptribs.document;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Getter;
import uk.gov.hmcts.ccd.sdk.api.HasLabel;

@Getter
@AllArgsConstructor
public enum DocumentType implements HasLabel {
    @JsonProperty("birthOrAdoptionCertificate")
    BIRTH_OR_ADOPTION_CERTIFICATE("birthOrAdoptionCertificate"),

    @JsonProperty("deathCertificate")
    DEATH_CERTIFICATE("deathCertificate"),

    @JsonProperty("applicationSummary")
    APPLICATION_SUMMARY("applicationSummary"),

    @JsonProperty("application")
    @JsonAlias("adoptionApplication")
    APPLICATION("Application"),

    @JsonProperty("email")
    EMAIL("Email");

    private final String label;
}
