package uk.gov.hmcts.sptribs.ciccase.model;

import lombok.Builder;
import uk.gov.hmcts.sptribs.ciccase.util.AccessCodeGenerator;

public record CaseInvite(
    String applicant2InviteEmailAddress,
    String accessCode,
    String applicant2UserId) {

    @Builder()
    public CaseInvite {}

    public CaseInvite generateAccessCode() {
        return new CaseInvite(applicant2InviteEmailAddress, AccessCodeGenerator.generateAccessCode(), applicant2UserId);
    }

    public CaseInvite useAccessCode() {
        return new CaseInvite(applicant2InviteEmailAddress, null, applicant2UserId);
    }
}
