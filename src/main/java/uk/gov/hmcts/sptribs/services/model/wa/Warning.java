package uk.gov.hmcts.sptribs.services.model.wa;

import com.fasterxml.jackson.annotation.JsonCreator;
import lombok.Getter;

@Getter
public class Warning {
    private final String warningCode;
    private final String warningText;
    @JsonCreator public Warning(String warningCode, String warningText) {
        this.warningCode = warningCode; this.warningText = warningText;
    }
}
