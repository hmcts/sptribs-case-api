package uk.gov.hmcts.sptribs.caseworker.util;

import uk.gov.hmcts.ccd.sdk.type.FlagDetail;
import uk.gov.hmcts.ccd.sdk.type.Flags;
import uk.gov.hmcts.ccd.sdk.type.ListValue;
import uk.gov.hmcts.sptribs.ciccase.model.CaseData;
import uk.gov.hmcts.sptribs.ciccase.model.PartiesCIC;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

public final class CaseFlagsUtil {
    public static final String ANONYMITY_FLAG_CODE = "CF0012";
    public static final String ACTIVE_STATUS = "Active";
    private static final String SUBJECT_ROLE = "subject";
    private static final String APPLICANT_ROLE = "applicant";
    private static final String REPRESENTATIVE_ROLE = "Representative";

    private CaseFlagsUtil() {
        //Hide utility class constructor
    }

    public static void initialiseFlags(CaseData data) {
        data.setCaseFlags(Flags.builder().details(new ArrayList<>()).partyName(null).roleOnCase(null).build());

        if (data.getCicCase().getFullName() != null) {
            data.setSubjectFlags(createFlags(data.getCicCase().getFullName(), SUBJECT_ROLE));
        }

        if (data.getCicCase().getApplicantFullName() != null) {
            data.setApplicantFlags(createFlags(data.getCicCase().getApplicantFullName(), APPLICANT_ROLE));
        }

        if (data.getCicCase().getRepresentativeFullName() != null) {
            data.setRepresentativeFlags(createFlags(data.getCicCase().getRepresentativeFullName(), REPRESENTATIVE_ROLE));
        }
    }

    public static void updateOrInitialiseFlags(CaseData data) {
        if (data.getCaseFlags() == null) {
            data.setCaseFlags(Flags.builder().details(new ArrayList<>()).partyName(null).roleOnCase(null).build());
        }

        if (data.getSubjectFlags() == null) {
            data.setSubjectFlags(createFlags(data.getCicCase().getFullName(), SUBJECT_ROLE));
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
            flags.getDetails().add(ListValue.<FlagDetail>builder().id(UUID.randomUUID().toString()).value(flagDetail).build());
            data.setCaseFlags(flags);
        }
    }

    public static ListValue<FlagDetail> mergeAnonymityFlagsPreserveOriginalId(CaseData data) {
        return mergeAnonymityFlagsPreserveOriginalId(data, null);
    }

    public static ListValue<FlagDetail> mergeAnonymityFlagsPreserveOriginalId(CaseData data, CaseData beforeData) {
        if (data == null || data.getCaseFlags() == null || data.getCaseFlags().getDetails() == null) {
            return null;
        }

        List<ListValue<FlagDetail>> flagDetails = data.getCaseFlags().getDetails().stream()
            .filter(Objects::nonNull)
            .toList();

        if (flagDetails.isEmpty()) {
            return null;
        }

        List<ListValue<FlagDetail>> anonymityFlags = flagDetails.stream().filter(CaseFlagsUtil::isAnonymityFlag).toList();

        if (anonymityFlags.isEmpty()) {
            return null;
        }

        Set<String> beforeAnonymityFlagIds = getAnonymityFlagIds(beforeData);
        ListValue<FlagDetail> originalFlag = selectTargetFlagToKeep(anonymityFlags, beforeAnonymityFlagIds);
        ListValue<FlagDetail> latestFlag = selectSourceFlag(anonymityFlags, beforeAnonymityFlagIds);

        if (originalFlag == null || latestFlag == null) {
            return null;
        }

        if (!originalFlag.equals(latestFlag)) {
            LocalDateTime originalCreatedAt = originalFlag.getValue() == null ? null : originalFlag.getValue().getDateTimeCreated();
            originalFlag.setValue(copyFlagDetail(latestFlag.getValue()));
            if (originalFlag.getValue() != null) {
                originalFlag.getValue().setDateTimeCreated(originalCreatedAt);
                originalFlag.getValue().setDateTimeModified(LocalDateTime.now());
            }
        }

        List<ListValue<FlagDetail>> updatedFlagDetails = flagDetails.stream()
            .filter(flag -> !isAnonymityFlag(flag) || originalFlag.equals(flag)).toList();

        data.getCaseFlags().setDetails(new ArrayList<>(updatedFlagDetails));
        return originalFlag;
    }

    private static Set<String> getAnonymityFlagIds(CaseData data) {
        if (data == null || data.getCaseFlags() == null || data.getCaseFlags().getDetails() == null) {
            return new HashSet<>();
        }

        return data.getCaseFlags().getDetails().stream()
            .filter(Objects::nonNull)
            .filter(CaseFlagsUtil::isAnonymityFlag)
            .map(ListValue::getId)
            .filter(id -> id != null && !id.isBlank()).collect(Collectors.toSet());
    }

    private static ListValue<FlagDetail> selectTargetFlagToKeep(List<ListValue<FlagDetail>> anonymityFlags,
                                                                 Set<String> beforeAnonymityFlagIds) {
        if (anonymityFlags.isEmpty()) {
            return null;
        }

        return anonymityFlags.stream()
            .filter(flag -> flag.getId() != null
                && beforeAnonymityFlagIds.contains(flag.getId())).findFirst().orElse(anonymityFlags.getFirst());
    }

    private static ListValue<FlagDetail> selectSourceFlag(List<ListValue<FlagDetail>> anonymityFlags,
                                                          Set<String> beforeAnonymityFlagIds) {
        if (anonymityFlags.isEmpty()) {
            return null;
        }

        List<ListValue<FlagDetail>> newAnonymityFlags = anonymityFlags.stream()
            .filter(flag -> flag.getId() == null || !beforeAnonymityFlagIds.contains(flag.getId())).toList();

        if (!newAnonymityFlags.isEmpty()) {
            return latestByCreatedDateOrLast(newAnonymityFlags);
        }

        return latestByCreatedDateOrLast(anonymityFlags);
    }

    private static ListValue<FlagDetail> latestByCreatedDateOrLast(List<ListValue<FlagDetail>> flags) {
        if (flags.isEmpty()) {
            return null;
        }

        return flags.stream()
            .filter(flag -> flag.getValue() != null && flag.getValue().getDateTimeCreated() != null)
            .max(Comparator.comparing(flag -> flag.getValue().getDateTimeCreated())).orElse(flags.getLast());
    }

    public static boolean isAnonymityFlag(ListValue<FlagDetail> flag) {
        return flag != null && flag.getValue() != null && ANONYMITY_FLAG_CODE.equals(flag.getValue().getFlagCode());
    }

    public static boolean isActiveFlag(ListValue<FlagDetail> flag) {
        return flag != null && flag.getValue() != null && ACTIVE_STATUS.equalsIgnoreCase(flag.getValue().getStatus());
    }

    private static void updateRepresentativeFlags(CaseData data) {
        boolean hasRepresentative = data.getCicCase().getPartiesCIC().contains(PartiesCIC.REPRESENTATIVE);
        data.setRepresentativeFlags(updateOrCreatePartyFlags(data.getRepresentativeFlags(),
            hasRepresentative, data.getCicCase().getRepresentativeFullName(), REPRESENTATIVE_ROLE));
    }

    private static void updateApplicantFlags(CaseData data) {
        boolean hasApplicant = data.getCicCase().getPartiesCIC().contains(PartiesCIC.APPLICANT);
        data.setApplicantFlags(updateOrCreatePartyFlags(data.getApplicantFlags(),
            hasApplicant, data.getCicCase().getApplicantFullName(), APPLICANT_ROLE));
    }

    private static Flags updateOrCreatePartyFlags(Flags existingFlags, boolean hasParty, String partyName, String roleOnCase) {
        if (!hasParty) {
            return null;
        }

        if (existingFlags == null) {
            return createFlags(partyName, roleOnCase);
        }

        existingFlags.setPartyName(partyName);
        return existingFlags;
    }

    private static Flags createFlags(String partyName, String roleOnCase) {
        return Flags.builder().details(new ArrayList<>()).partyName(partyName).roleOnCase(roleOnCase).build();
    }

    private static FlagDetail copyFlagDetail(FlagDetail sourceFlag) {
        if (sourceFlag == null) {
            return null;
        }

        FlagDetail copiedFlag = FlagDetail.builder()
            .name(sourceFlag.getName())
            .status(sourceFlag.getStatus())
            .nameCy(sourceFlag.getNameCy())
            .flagCode(sourceFlag.getFlagCode())
            .flagComment(sourceFlag.getFlagComment())
            .hearingRelevant(sourceFlag.getHearingRelevant())
            .availableExternally(sourceFlag.getAvailableExternally())
            .dateTimeCreated(sourceFlag.getDateTimeCreated())
            .dateTimeModified(sourceFlag.getDateTimeModified()).build();

        copiedFlag.setPath(sourceFlag.getPath());
        return copiedFlag;
    }

    public static boolean caseFlagDetailsEquals(FlagDetail flagDetail1, FlagDetail flagDetail2) {
        CaseFlagDetailsComparator caseFlagDetailsComparator = new CaseFlagDetailsComparator();
        return caseFlagDetailsComparator.compare(flagDetail1, flagDetail2) == 0;
    }

    private static class CaseFlagDetailsComparator implements Comparator<FlagDetail> {
        @Override
        public int compare(FlagDetail flagDetail1, FlagDetail flagDetail2) {
            return Comparator.comparing(FlagDetail::getName, Comparator.nullsFirst(String::compareTo))
                .thenComparing(FlagDetail::getStatus, Comparator.nullsFirst(String::compareTo))
                .thenComparing(FlagDetail::getNameCy, Comparator.nullsFirst(String::compareTo))
                .thenComparing(FlagDetail::getFlagCode, Comparator.nullsFirst(String::compareTo))
                .thenComparing(FlagDetail::getFlagComment, Comparator.nullsFirst(String::compareTo))
                .thenComparing(FlagDetail::getHearingRelevant).thenComparing(FlagDetail::getAvailableExternally)
                .compare(flagDetail1, flagDetail2);
        }
    }
}
