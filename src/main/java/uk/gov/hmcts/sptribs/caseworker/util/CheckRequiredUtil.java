package uk.gov.hmcts.sptribs.caseworker.util;

import uk.gov.hmcts.sptribs.ciccase.model.CaseData;

import static org.springframework.util.CollectionUtils.isEmpty;

public final class CheckRequiredUtil {

    private CheckRequiredUtil() {
    }

    public static boolean checkNullSubjectRepresentativeRespondent(CaseData data) {
        return data.getCicCase() != null
            && isEmpty(data.getCicCase().getNotifyPartySubject())
            && isEmpty(data.getCicCase().getNotifyPartyRepresentative())
            && isEmpty(data.getCicCase().getNotifyPartyRespondent())
            && isEmpty(data.getCicCase().getNotifyPartyApplicant());
    }

    public static boolean checkNullSubjectRepresentativeApplicant(CaseData data) {
        return data.getCicCase() != null
            && isEmpty(data.getCicCase().getNotifyPartySubject())
            && isEmpty(data.getCicCase().getNotifyPartyRepresentative())
            && isEmpty(data.getCicCase().getNotifyPartyApplicant())
            && isEmpty(data.getCicCase().getNotifyPartyRespondent());
    }

    public static boolean checkMultiSubjectRepresentativeApplicant(CaseData data) {
        return !isEmpty(data.getCicCase().getNotifyPartySubject())
            && (!isEmpty(data.getCicCase().getNotifyPartyApplicant())
            || !isEmpty(data.getCicCase().getNotifyPartyRepresentative())
            || !isEmpty(data.getCicCase().getNotifyPartyRespondent()))
            || !isEmpty(data.getCicCase().getNotifyPartyApplicant())
            && (!isEmpty(data.getCicCase().getNotifyPartyRepresentative())
            || !isEmpty(data.getCicCase().getNotifyPartyRespondent()))
            || !isEmpty(data.getCicCase().getNotifyPartyRepresentative())
            && (!isEmpty(data.getCicCase().getNotifyPartyRespondent()));
    }

}
