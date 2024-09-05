package uk.gov.hmcts.sptribs.systemupdate.convert;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.ccd.sdk.api.CaseDetails;
import uk.gov.hmcts.sptribs.ciccase.model.CaseData;
import uk.gov.hmcts.sptribs.ciccase.model.State;

import java.util.List;
import java.util.Objects;

@Component
@Slf4j
public class CaseDetailsListConverter {

    private final CaseDetailsConverter caseDetailsConverter;

    @Autowired
    public CaseDetailsListConverter(CaseDetailsConverter caseDetailsConverter) {
        this.caseDetailsConverter = caseDetailsConverter;
    }

    public List<CaseDetails<CaseData, State>> convertToListOfValidCaseDetails(
        final List<uk.gov.hmcts.reform.ccd.client.model.CaseDetails> caseDetailsList) {

        return caseDetailsList.stream()
            .map(caseDetails -> {
                try {
                    return caseDetailsConverter.convertToCaseDetailsFromReformModel(caseDetails);
                } catch (final IllegalArgumentException e) {
                    log.error(
                        "Case failed to deserialize, removing from search results. Case ID: {}",
                        caseDetails.getId());
                    return null;
                }
            })
            .filter(Objects::nonNull)
            .toList();
    }
}
