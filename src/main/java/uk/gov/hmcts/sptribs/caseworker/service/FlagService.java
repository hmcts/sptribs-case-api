package uk.gov.hmcts.sptribs.caseworker.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import uk.gov.hmcts.ccd.sdk.type.DynamicList;
import uk.gov.hmcts.ccd.sdk.type.DynamicListElement;
import uk.gov.hmcts.ccd.sdk.type.Flags;
import uk.gov.hmcts.ccd.sdk.type.ListValue;
import uk.gov.hmcts.sptribs.ciccase.model.CicCase;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import static uk.gov.hmcts.sptribs.caseworker.util.EventConstants.APPELLANT_FLAG;
import static uk.gov.hmcts.sptribs.caseworker.util.EventConstants.CASE_FLAG;
import static uk.gov.hmcts.sptribs.caseworker.util.EventConstants.HYPHEN;
import static uk.gov.hmcts.sptribs.caseworker.util.EventConstants.RESPONDENT_FLAG;

@Service
@Slf4j
public class FlagService {


    public DynamicList populateFlagList(CicCase cicCase) {
        List<String> flagsList = getFLags(cicCase);


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

    private List<String> getFLags(CicCase cicCase) {
        List<String> flagsList = new ArrayList<>();
        if (!CollectionUtils.isEmpty(cicCase.getCaseFlags())) {
            for (ListValue<Flags> flagsListValue : cicCase.getCaseFlags()) {
                flagsList.add(CASE_FLAG + HYPHEN + flagsListValue.getId() + HYPHEN + " ");
            }
        }
        if (!CollectionUtils.isEmpty(cicCase.getAppellantFlags())) {
            for (ListValue<Flags> flagsListValue : cicCase.getAppellantFlags()) {
                flagsList.add(APPELLANT_FLAG + HYPHEN + flagsListValue.getId() + HYPHEN + flagsListValue.getValue().getPartyName());
            }
        }
        if (!CollectionUtils.isEmpty(cicCase.getRespondentFlags())) {
            for (ListValue<Flags> flagsListValue : cicCase.getRespondentFlags()) {
                flagsList.add(RESPONDENT_FLAG + HYPHEN + flagsListValue.getId() + HYPHEN + flagsListValue.getValue().getPartyName());
            }
        }
        return flagsList;
    }
}
