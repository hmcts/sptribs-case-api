package uk.gov.hmcts.divorce.ciccase;

import lombok.Builder;
import lombok.Getter;
import uk.gov.hmcts.divorce.ciccase.model.CaseData;
import uk.gov.hmcts.divorce.ciccase.model.State;

import java.util.List;

@Builder
@Getter
public class CaseInfo {

    private final CaseData caseData;
    private final State state;
    private final List<String> errors;
}
