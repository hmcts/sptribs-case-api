package uk.gov.hmcts.sptribs.common.links;

import feign.FeignException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import uk.gov.hmcts.ccd.sdk.type.CaseLink;
import uk.gov.hmcts.ccd.sdk.type.DynamicListElement;
import uk.gov.hmcts.ccd.sdk.type.DynamicMultiSelectList;
import uk.gov.hmcts.ccd.sdk.type.LinkReason;
import uk.gov.hmcts.ccd.sdk.type.ListValue;
import uk.gov.hmcts.reform.authorisation.generators.AuthTokenGenerator;
import uk.gov.hmcts.reform.idam.client.models.User;
import uk.gov.hmcts.sptribs.ciccase.model.CaseData;
import uk.gov.hmcts.sptribs.idam.IdamService;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;
import javax.servlet.http.HttpServletRequest;

import static org.springframework.http.HttpHeaders.AUTHORIZATION;
import static uk.gov.hmcts.sptribs.common.CommonConstants.CATEGORY_ID_LINK_REASON;
import static uk.gov.hmcts.sptribs.common.config.ControllerConstants.BEARER_PREFIX;

@Service
@Slf4j
public class LinkService {

    @Autowired
    private IdamService idamService;

    @Autowired
    private HttpServletRequest httpServletRequest;

    @Autowired
    private AuthTokenGenerator authTokenGenerator;

    @Autowired
    private LinkReasonClient linkReasonClient;

    public List<ListValue<LinkReason>> getLinkReasons() {
        List<LinkReason> list = getReasons();
        return getListValueLinkReason(list);
    }

    private List<LinkReason> getReasons() {

        try {
            final User user = idamService.retrieveUser(httpServletRequest.getHeader(AUTHORIZATION));
            final String authorisation = user.getAuthToken().startsWith(BEARER_PREFIX)
                ? user.getAuthToken() : BEARER_PREFIX + user.getAuthToken();
            String serviceAuthorization = authTokenGenerator.generate();
            String serviceAuthorizationLatest = serviceAuthorization.startsWith(BEARER_PREFIX)
                ? serviceAuthorization.substring(7) : serviceAuthorization;

            return linkReasonClient.getLinkReasons(
                serviceAuthorizationLatest,
                authorisation,
                CATEGORY_ID_LINK_REASON);

        } catch (FeignException exception) {
            log.error("Unable to get reason data from reference data with exception {}",
                exception.getMessage());
        }

        return new ArrayList<>();
    }

    private List<ListValue<LinkReason>> getListValueLinkReason(List<LinkReason> list) {
        List<ListValue<LinkReason>> listValuesList = new ArrayList<>();
        for (LinkReason reason : list) {
            if (CollectionUtils.isEmpty(listValuesList)) {
                List<ListValue<LinkReason>> listValues = new ArrayList<>();

                var listValue = ListValue
                    .<LinkReason>builder()
                    .id("1")
                    .value(reason)
                    .build();

                listValues.add(listValue);

                listValuesList = listValues;
            } else {
                AtomicInteger listValueIndex = new AtomicInteger(0);
                var listValue = ListValue
                    .<LinkReason>builder()
                    .value(reason)
                    .build();

                listValuesList.add(0, listValue); // always add new note as first element so that it is displayed on top

                listValuesList.forEach(
                    reasonListItem -> reasonListItem.setId(String.valueOf(listValueIndex.incrementAndGet())));

            }
        }
        return listValuesList;
    }

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
