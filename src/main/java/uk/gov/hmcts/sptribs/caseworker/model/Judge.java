package uk.gov.hmcts.sptribs.caseworker.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import uk.gov.hmcts.ccd.sdk.api.CCD;
import uk.gov.hmcts.sptribs.ciccase.model.access.CaseworkerWithCAAAccess;
import uk.gov.hmcts.sptribs.ciccase.model.access.DefaultAccess;

import java.util.Comparator;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Judge {

    @CCD(
        label = "Judge UUID",
        access = {DefaultAccess.class, CaseworkerWithCAAAccess.class}
    )
    private String uuid;

    @CCD(
        label = "Judge Full Name",
        access = {DefaultAccess.class, CaseworkerWithCAAAccess.class}
    )
    private String judgeFullName;

    private String title;

    @CCD(
        label = "Judge Personal Code",
        access = {DefaultAccess.class, CaseworkerWithCAAAccess.class}
    )
    private String personalCode;

    public static Comparator<Judge> byFullNameIgnoringTitle() {
        return Comparator.comparing(
                judge -> stripTitle(judge.getJudgeFullName(), judge.getTitle()),
                String.CASE_INSENSITIVE_ORDER
        );
    }

    private static String stripTitle(String fullName, String title) {
        if (fullName == null) {
            return "";
        }
        if (title != null && fullName.startsWith(title)) {
            return fullName.substring(title.length()).trim();
        }
        return fullName;
    }
}
