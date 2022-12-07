package uk.gov.hmcts.sptribs.caseworker.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import uk.gov.hmcts.ccd.sdk.api.CaseDetails;
import uk.gov.hmcts.ccd.sdk.type.DynamicList;
import uk.gov.hmcts.ccd.sdk.type.DynamicListElement;
import uk.gov.hmcts.ccd.sdk.type.ListValue;
import uk.gov.hmcts.sptribs.caseworker.model.DraftOrderCIC;
import uk.gov.hmcts.sptribs.ciccase.model.CaseData;
import uk.gov.hmcts.sptribs.ciccase.model.State;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@Slf4j
public class OrderService {

    public DynamicList getDraftOrderDynamicList(final CaseDetails<CaseData, State> caseDetails) {
        CaseData data = caseDetails.getData();
        List<ListValue<DraftOrderCIC>> draftList = data.getCicCase().getDraftOrderCICList();
        List<String> draftOrderList = new ArrayList<>();
        if (!CollectionUtils.isEmpty(draftList)) {
            for (int i = 0; i < draftList.size(); i++) {
                DraftOrderCIC draftOrderCIC = draftList.get(i).getValue();
                String draft = i + "-" + draftOrderCIC.getAnOrderTemplate().getLabel();
                draftOrderList.add(draft);
            }

            List<DynamicListElement> dynamicListElements = draftOrderList
                .stream()
                .sorted()
                .map(draft -> DynamicListElement.builder().label(draft).code(UUID.randomUUID()).build())
                .collect(Collectors.toList());

            return DynamicList
                .builder()
                .value(DynamicListElement.builder().label("draft").code(UUID.randomUUID()).build())
                .listItems(dynamicListElements)
                .build();
        }
        return null;
    }
}
