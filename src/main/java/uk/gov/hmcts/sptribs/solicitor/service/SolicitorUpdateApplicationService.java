package uk.gov.hmcts.sptribs.solicitor.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.ccd.sdk.api.CaseDetails;
import uk.gov.hmcts.sptribs.ciccase.model.CaseData;
import uk.gov.hmcts.sptribs.ciccase.model.State;
import uk.gov.hmcts.sptribs.document.task.DivorceApplicationRemover;
import uk.gov.hmcts.sptribs.solicitor.service.task.DivorceApplicationDraft;
import uk.gov.hmcts.sptribs.solicitor.service.task.SetApplicant1SolicitorAddress;
import uk.gov.hmcts.sptribs.solicitor.service.task.SetApplicantGender;

import static uk.gov.hmcts.sptribs.ciccase.task.CaseTaskRunner.caseTasks;

@Service
@Slf4j
public class SolicitorUpdateApplicationService {

    @Autowired
    private DivorceApplicationRemover divorceApplicationRemover;

    @Autowired
    private DivorceApplicationDraft divorceApplicationDraft;

    @Autowired
    private SetApplicant1SolicitorAddress setApplicant1SolicitorAddress;

    @Autowired
    private SetApplicantGender setApplicantGender;

    public CaseDetails<CaseData, State> aboutToSubmit(final CaseDetails<CaseData, State> caseDetails) {

        return caseTasks(
            setApplicant1SolicitorAddress,
            divorceApplicationRemover,
            divorceApplicationDraft,
            setApplicantGender
        ).run(caseDetails);
    }
}
