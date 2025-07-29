package uk.gov.hmcts.sptribs.services.model.wa;

import com.fasterxml.jackson.annotation.JsonCreator;
import lombok.Getter;
import uk.gov.hmcts.sptribs.services.wa.TaskManagementClient;

import java.util.List;

@Getter
public class WarningValues {
    private final List<Warning> values;
    @JsonCreator
    public WarningValues(List<Warning> values) { this.values = values; }
}
