package uk.gov.hmcts.sptribs.flag.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.util.ObjectUtils;
import uk.gov.hmcts.ccd.sdk.type.DynamicList;
import uk.gov.hmcts.ccd.sdk.type.DynamicListElement;
import uk.gov.hmcts.ccd.sdk.type.FlagDetail;
import uk.gov.hmcts.ccd.sdk.type.ListValue;
import uk.gov.hmcts.sptribs.ciccase.model.CaseData;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import static uk.gov.hmcts.sptribs.caseworker.util.EventConstants.APPLICANT_FLAG;
import static uk.gov.hmcts.sptribs.caseworker.util.EventConstants.CASE_FLAG;
import static uk.gov.hmcts.sptribs.caseworker.util.EventConstants.HYPHEN;
import static uk.gov.hmcts.sptribs.caseworker.util.EventConstants.REPRESENTATIVE_FLAG;
import static uk.gov.hmcts.sptribs.caseworker.util.EventConstants.SUBJECT_FLAG;

@Service
@Slf4j
public class FlagService {


    public DynamicList populateFlagList(CaseData caseData) {
        List<String> flagsList = getFLags(caseData);


        List<DynamicListElement> dynamicFlagElements = flagsList
            .stream()
            .sorted()
            .map(flags -> DynamicListElement.builder().label(flags).code(UUID.randomUUID()).build())
            .collect(Collectors.toList());

        return DynamicList
            .builder()
            .listItems(dynamicFlagElements)
            .build();
    }

    private List<String> getFLags(CaseData caseData) {
        List<String> flagsList = new ArrayList<>();
        if (!ObjectUtils.isEmpty(caseData.getCaseFlags())) {
            for (ListValue<FlagDetail> flagsListValue : caseData.getCaseFlags().getDetails()) {
                flagsList.add(CASE_FLAG + HYPHEN + flagsListValue.getId() + HYPHEN + " ");
            }
        }
        if (!ObjectUtils.isEmpty(caseData.getSubjectFlags())) {
            for (ListValue<FlagDetail> flagsListValue : caseData.getSubjectFlags().getDetails()) {
                flagsList.add(SUBJECT_FLAG + HYPHEN + flagsListValue.getId() + HYPHEN + caseData.getSubjectFlags().getPartyName());
            }
        }
        if (!ObjectUtils.isEmpty(caseData.getRepresentativeFlags())) {
            for (ListValue<FlagDetail> flagsListValue : caseData.getRepresentativeFlags().getDetails()) {
                flagsList.add(REPRESENTATIVE_FLAG + HYPHEN + flagsListValue.getId() + HYPHEN
                    + caseData.getRepresentativeFlags().getPartyName());
            }
        }
        if (!ObjectUtils.isEmpty(caseData.getApplicantFlags())) {
            for (ListValue<FlagDetail> flagsListValue : caseData.getApplicantFlags().getDetails()) {
                flagsList.add(APPLICANT_FLAG + HYPHEN + flagsListValue.getId() + HYPHEN + caseData.getApplicantFlags().getPartyName());
            }
        }
        return flagsList;
    }
}
