package uk.gov.hmcts.sptribs.caseworker.service;

import org.springframework.stereotype.Service;
import uk.gov.hmcts.sptribs.caseworker.model.Listing;
import uk.gov.hmcts.sptribs.ciccase.model.CaseData;

import java.time.LocalDate;
import java.util.UUID;

import static uk.gov.hmcts.sptribs.caseworker.model.YesNo.NO;
import static uk.gov.hmcts.sptribs.caseworker.model.YesNo.YES;
import static uk.gov.hmcts.sptribs.ciccase.model.HearingState.Listed;

@Service
public class HearingJudgeTaskService {

    private final HearingJudgeTaskDateService dates;

    public HearingJudgeTaskService(HearingJudgeTaskDateService dates) {
        this.dates = dates;
    }

    public void updateTask(CaseData caseData, Listing listing) {
        if (listing.getHearingJudgeHearingId() == null) {
            listing.setHearingJudgeHearingId(UUID.randomUUID().toString());
        }
        LocalDate releaseDate = listing.getHearingStatus() == Listed ? dates.releaseDate(listing.getDate()) : null;
        listing.setHearingJudgeTaskReleaseDate(releaseDate);
        caseData.setAssignHearingJudgeTaskTrigger(null);
        caseData.setCancelHearingJudgeTaskTrigger(null);

        boolean isDue = dates.isDue(listing.getDate(), releaseDate);
        if (!isDue && listing.getHearingJudgeTaskRequested() == YES) {
            caseData.setCancelHearingJudgeTaskTrigger(listing.getHearingJudgeTaskId());
            listing.setHearingJudgeTaskRequested(NO);
            listing.setHearingJudgeTaskId(null);
        }

        if (isDue && listing.getHearingJudgeTaskRequested() != YES) {
            listing.setHearingJudgeTaskId(UUID.randomUUID().toString());
            listing.setHearingJudgeTaskRequested(YES);
            caseData.setAssignHearingJudgeTaskTrigger(listing.getHearingJudgeTaskId());
        }
    }
}
