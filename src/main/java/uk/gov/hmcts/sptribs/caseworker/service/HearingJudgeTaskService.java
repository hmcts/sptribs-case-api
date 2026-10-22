package uk.gov.hmcts.sptribs.caseworker.service;

import org.springframework.stereotype.Service;
import uk.gov.hmcts.sptribs.caseworker.model.Listing;
import uk.gov.hmcts.sptribs.ciccase.model.CaseData;

import java.time.LocalDate;
import java.util.UUID;

import static uk.gov.hmcts.sptribs.caseworker.model.YesNo.YES;
import static uk.gov.hmcts.sptribs.ciccase.model.HearingState.Listed;

@Service
public class HearingJudgeTaskService {

    private final HearingJudgeTaskDateService dates;

    public HearingJudgeTaskService(HearingJudgeTaskDateService dates) {
        this.dates = dates;
    }

    public void updateTask(CaseData caseData, Listing listing) {
        if (listing.getHearingJudgeTaskId() == null) {
            listing.setHearingJudgeTaskId(UUID.randomUUID().toString());
        }

        LocalDate releaseDate = listing.getHearingStatus() == Listed ? dates.releaseDate(listing.getDate()) : null;
        listing.setHearingJudgeTaskReleaseDate(releaseDate);
        caseData.setAssignHearingJudgeTaskTrigger(null);

        if (dates.isDue(listing.getDate(), releaseDate) && listing.getHearingJudgeTaskRequested() != YES) {
            listing.setHearingJudgeTaskRequested(YES);
            caseData.setAssignHearingJudgeTaskTrigger(listing.getHearingJudgeTaskId());
        }
    }
}
