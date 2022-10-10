package uk.gov.hmcts.sptribs.caseworker.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.ccd.sdk.api.CaseDetails;
import uk.gov.hmcts.sptribs.caseworker.service.task.GenerateGeneralLetter;
import uk.gov.hmcts.sptribs.ciccase.model.CaseData;
import uk.gov.hmcts.sptribs.ciccase.model.State;

import static uk.gov.hmcts.sptribs.ciccase.task.CaseTaskRunner.caseTasks;

@Service
@Slf4j
public class GeneralLetterService {

    @Autowired
    private GenerateGeneralLetter generateGeneralLetter;

    public CaseDetails<CaseData, State> processGeneralLetter(final CaseDetails<CaseData, State> caseDetails) {
        return caseTasks(
            generateGeneralLetter
        ).run(caseDetails);
    }
}
