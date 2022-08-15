package uk.gov.hmcts.sptribs.caseworker.service.task;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.ccd.sdk.api.CaseDetails;
import uk.gov.hmcts.sptribs.caseworker.service.print.GeneralLetterPrinter;
import uk.gov.hmcts.sptribs.ciccase.model.CaseData;
import uk.gov.hmcts.sptribs.ciccase.model.State;
import uk.gov.hmcts.sptribs.ciccase.task.CaseTask;

@Component
@Slf4j
public class SendGeneralLetter implements CaseTask {

    @Autowired
    private GeneralLetterPrinter printer;

    @Override
    public CaseDetails<CaseData, State> apply(final CaseDetails<CaseData, State> caseDetails) {

        log.info("Sending general letter for case id: {}", caseDetails.getId());

        printer.sendLetterWithAttachments(caseDetails.getData(), caseDetails.getId());

        return caseDetails;
    }
}
