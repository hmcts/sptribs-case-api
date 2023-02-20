package uk.gov.hmcts.sptribs.caseworker.util;

import org.springframework.util.CollectionUtils;
import uk.gov.hmcts.sptribs.ciccase.model.CaseData;

public final class CheckRequiredUtil {

    private CheckRequiredUtil() {
    }

    public static boolean checkNullSubjectRepresentativeRespondent(CaseData data) {
        return null != data.getCicCase()
            && CollectionUtils.isEmpty(data.getCicCase().getNotifyPartySubject())
            && CollectionUtils.isEmpty(data.getCicCase().getNotifyPartyRepresentative())
            && CollectionUtils.isEmpty(data.getCicCase().getNotifyPartyRespondent());

    }

    public static boolean checkNullSubjectRepresentativeApplicant(CaseData data) {
        return null != data.getCicCase()
            && CollectionUtils.isEmpty(data.getCicCase().getNotifyPartySubject())
            && CollectionUtils.isEmpty(data.getCicCase().getNotifyPartyRepresentative())
            && CollectionUtils.isEmpty(data.getCicCase().getNotifyPartyApplicant());

    }

    public static boolean checkMultiSubjectRepresentativeApplicant(CaseData data) {
        return !CollectionUtils.isEmpty(data.getCicCase().getNotifyPartySubject())
            && (!CollectionUtils.isEmpty(data.getCicCase().getNotifyPartyApplicant())
            || !CollectionUtils.isEmpty(data.getCicCase().getNotifyPartyRepresentative()))
            || !CollectionUtils.isEmpty(data.getCicCase().getNotifyPartyApplicant())
            && (!CollectionUtils.isEmpty(data.getCicCase().getNotifyPartySubject())
            || !CollectionUtils.isEmpty(data.getCicCase().getNotifyPartyRepresentative()))
            || !CollectionUtils.isEmpty(data.getCicCase().getNotifyPartyRepresentative())
            && (!CollectionUtils.isEmpty(data.getCicCase().getNotifyPartySubject())
            || !CollectionUtils.isEmpty(data.getCicCase().getNotifyPartyApplicant()));
    }

}
