package uk.gov.hmcts.sptribs.caseworker.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import uk.gov.hmcts.ccd.sdk.type.CaseLink;
import uk.gov.hmcts.ccd.sdk.type.DynamicListElement;
import uk.gov.hmcts.ccd.sdk.type.DynamicMultiSelectList;
import uk.gov.hmcts.ccd.sdk.type.ListValue;
import uk.gov.hmcts.sptribs.ciccase.model.CaseData;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;

@Service
@Slf4j
public class LinkService {

    public DynamicMultiSelectList prepareLinkList(final CaseData data) {
        List<DynamicListElement> dynamicListElements = new ArrayList<>();
        for (ListValue<CaseLink> caseLinkListValue : data.getCaseLinks()) {
            DynamicListElement element = DynamicListElement.builder()
                .label(caseLinkListValue.getValue().getCaseReference())
                .code(UUID.randomUUID())
                .build();
            dynamicListElements.add(element);
        }

        return DynamicMultiSelectList
            .builder()
            .listItems(dynamicListElements)
            .value(new ArrayList<>())
            .build();
    }

    public List<ListValue<CaseLink>> removeLinks(CaseData data) {
        List<CaseLink> caseLinkList = new ArrayList<>();
        for (ListValue<CaseLink> caseLinksListValue : data.getCaseLinks()) {
            boolean found = false;
            for (DynamicListElement element : data.getCicCase().getLinkDynamicList().getValue()) {
                if (caseLinksListValue.getValue().getCaseReference().equals(element.getLabel())) {
                    found = true;
                    break;
                }
            }
            if (!found) {
                caseLinkList.add(caseLinksListValue.getValue());
            }
        }
        List<ListValue<CaseLink>> caseLinks = new ArrayList<>();
        List<ListValue<CaseLink>> listValues = new ArrayList<>();
        AtomicInteger listValueIndex = new AtomicInteger(0);
        for (CaseLink links : caseLinkList) {
            if (CollectionUtils.isEmpty(caseLinks)) {

                var listValue = ListValue
                    .<CaseLink>builder()
                    .id("1")
                    .value(links)
                    .build();

                listValues.add(listValue);

                caseLinks = listValues;
            } else {
                var listValue = ListValue
                    .<CaseLink>builder()
                    .value(links)
                    .build();

                caseLinks.add(0, listValue); // always add new note as first element so that it is displayed on top

                caseLinks.forEach(
                    caseLinkListValue -> caseLinkListValue.setId(String.valueOf(listValueIndex.incrementAndGet())));
            }
        }
        return caseLinks;
    }
}
