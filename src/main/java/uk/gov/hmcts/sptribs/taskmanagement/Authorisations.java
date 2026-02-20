package uk.gov.hmcts.sptribs.taskmanagement;

import lombok.Getter;

@Getter
public enum Authorisations {
    JUDICIAL("328"),
    NONE("");

    private final String authorisation;

    Authorisations(String authorisation) {
        this.authorisation = authorisation;
    }
}




