package uk.gov.hmcts.sptribs.caseworker.util;

import uk.gov.hmcts.ccd.sdk.type.FlagDetail;
import uk.gov.hmcts.ccd.sdk.type.Flags;
import uk.gov.hmcts.ccd.sdk.type.ListValue;
import uk.gov.hmcts.sptribs.ciccase.model.CaseData;
import uk.gov.hmcts.sptribs.ciccase.model.PartiesCIC;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.UUID;

public final class CaseFlagsUtil {
    private CaseFlagsUtil() {
        //Hide utility class constructor
    }

    public static void initialiseFlags(CaseData data) {
        data.setCaseFlags(Flags.builder()
                .details(new ArrayList<>())
                .partyName(null)
                .roleOnCase(null)
                .build());

        if (data.getCicCase().getFullName() != null) {
            data.setSubjectFlags(Flags.builder()
                    .details(new ArrayList<>())
                    .partyName(data.getCicCase().getFullName())
                    .roleOnCase("subject")
                    .build()
            );
        }

        if (data.getCicCase().getApplicantFullName() != null) {
            data.setApplicantFlags(Flags.builder()
                    .details(new ArrayList<>())
                    .partyName(data.getCicCase().getApplicantFullName())
                    .roleOnCase("applicant")
                    .build()
            );
        }

        if (data.getCicCase().getRepresentativeFullName() != null) {
            data.setRepresentativeFlags(Flags.builder()
                    .details(new ArrayList<>())
                    .partyName(data.getCicCase().getRepresentativeFullName())
                    .roleOnCase("Representative")
                    .build()
            );
        }
    }

    public static void updateOrInitialiseFlags(CaseData data) {
        if (data.getCaseFlags() == null) {
            data.setCaseFlags(Flags.builder()
                .details(new ArrayList<>())
                .partyName(null)
                .roleOnCase(null)
                .build());
        } else {
            data.getCaseFlags().setPartyName(data.getCaseFlags().getPartyName());
        }

        if (data.getSubjectFlags() == null) {
            data.setSubjectFlags(Flags.builder()
                .details(new ArrayList<>())
                .partyName(data.getCicCase().getFullName())
                .roleOnCase("subject")
                .build());
        } else {
            data.getSubjectFlags().setPartyName(data.getCicCase().getFullName());
        }

        updateApplicantFlags(data);
        updateRepresentativeFlags(data);
    }

    public static void addFlag(CaseData data, FlagDetail flagDetail) {
        updateOrInitialiseFlags(data);

        Flags flags = data.getCaseFlags();
        if (flags.getDetails().stream().noneMatch(detailValue -> caseFlagDetailsEquals(detailValue.getValue(), flagDetail))) {
            flags.getDetails().add(ListValue.<FlagDetail>builder()
                .id(UUID.randomUUID().toString()).value(flagDetail).build());
            data.setCaseFlags(flags);
        }
    }

    private static void updateRepresentativeFlags(CaseData data) {
        if (data.getRepresentativeFlags() != null && data.getCicCase().getPartiesCIC().contains(PartiesCIC.REPRESENTATIVE)) {
            data.getRepresentativeFlags().setPartyName(data.getCicCase().getRepresentativeFullName());
        } else if (data.getRepresentativeFlags() == null && data.getCicCase().getPartiesCIC().contains(PartiesCIC.REPRESENTATIVE)) {
            data.setRepresentativeFlags(Flags.builder()
                .details(new ArrayList<>())
                .partyName(data.getCicCase().getRepresentativeFullName())
                .roleOnCase("Representative")
                .build());
        } else {
            data.setRepresentativeFlags(null);
        }
    }

    private static void updateApplicantFlags(CaseData data) {
        if (data.getApplicantFlags() != null && data.getCicCase().getPartiesCIC().contains(PartiesCIC.APPLICANT)) {
            data.getApplicantFlags().setPartyName(data.getCicCase().getApplicantFullName());
        } else if (data.getApplicantFlags() == null && data.getCicCase().getPartiesCIC().contains(PartiesCIC.APPLICANT)) {
            data.setApplicantFlags(Flags.builder()
                .details(new ArrayList<>())
                .partyName(data.getCicCase().getApplicantFullName())
                .roleOnCase("applicant")
                .build()
            );
        } else {
            data.setApplicantFlags(null);
        }
    }

    public static boolean caseFlagDetailsEquals(FlagDetail flagDetail1, FlagDetail flagDetail2) {
        CaseFlagDetailsComparator caseFlagDetailsComparator = new CaseFlagDetailsComparator();
        return caseFlagDetailsComparator.compare(flagDetail1, flagDetail2) == 0;
    }

    private static class CaseFlagDetailsComparator implements Comparator<FlagDetail> {
        @Override
        public int compare(FlagDetail flagDetail1, FlagDetail flagDetail2) {
            return Comparator.comparing(FlagDetail::getName)
                    .thenComparing(FlagDetail::getStatus)
                    .thenComparing(FlagDetail::getNameCy)
                    .thenComparing(FlagDetail::getFlagCode)
                    .thenComparing(FlagDetail::getFlagComment)
                    .thenComparing(FlagDetail::getHearingRelevant)
                    .thenComparing(FlagDetail::getAvailableExternally)
                    .compare(flagDetail1, flagDetail2);
        }
    }
}
