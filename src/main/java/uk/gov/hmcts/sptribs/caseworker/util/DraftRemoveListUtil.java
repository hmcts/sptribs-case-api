package uk.gov.hmcts.sptribs.caseworker.util;

import org.springframework.util.CollectionUtils;
import uk.gov.hmcts.ccd.sdk.type.DynamicList;
import uk.gov.hmcts.ccd.sdk.type.DynamicListElement;
import uk.gov.hmcts.ccd.sdk.type.ListValue;
import uk.gov.hmcts.sptribs.caseworker.model.DraftOrderCIC;
import uk.gov.hmcts.sptribs.ciccase.model.CaseData;
import uk.gov.hmcts.sptribs.ciccase.model.CicCase;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static uk.gov.hmcts.sptribs.caseworker.util.EventConstants.DOUBLE_HYPHEN;


public final class DraftRemoveListUtil {

    private DraftRemoveListUtil() {

    }

    public static CaseData setDraftListForRemoval(CaseData caseData, CaseData oldData) {

        List<ListValue<DraftOrderCIC>> draftOrderList = oldData.getCicCase().getDraftOrderCICList();

        CicCase cicCase = caseData.getCicCase();

        List<DraftOrderCIC> currentDrafts = cicCase.getDraftOrderCICList()
            .stream()
            .map(ListValue::getValue)
            .toList();

        for (ListValue<DraftOrderCIC> listValue : draftOrderList) {
            DraftOrderCIC draftOrder = listValue.getValue();

            if (!currentDrafts.contains(draftOrder)) {
                addToRemovedDraftOrdersList(cicCase, draftOrder);
            }
        }

        return caseData;
    }

    public static void addToRemovedDraftOrdersList(CicCase cicCase, DraftOrderCIC draftOrderCIC) {
        if (CollectionUtils.isEmpty(cicCase.getRemovedDraftList())) {
            List<ListValue<DraftOrderCIC>> listValues = new ArrayList<>();

            ListValue<DraftOrderCIC> listValue = ListValue
                .<DraftOrderCIC>builder()
                .id("1")
                .value(draftOrderCIC)
                .build();

            listValues.add(listValue);

            cicCase.setRemovedDraftList(listValues);
        } else {
            AtomicInteger listValueIndex = new AtomicInteger(0);
            ListValue<DraftOrderCIC> listValue = ListValue
                .<DraftOrderCIC>builder()
                .value(draftOrderCIC)
                .build();

            cicCase.getRemovedDraftList().add(0, listValue); // always add new note as first element so that it is displayed on top

            cicCase.getRemovedDraftList().forEach(
                removedFileListValue -> removedFileListValue.setId(String.valueOf(listValueIndex.incrementAndGet())));

        }
    }

    public static CicCase repopulateDynamicDraftList(CicCase cicCase) {

        DynamicList dynamicDraftList = DynamicList.builder()
            .listItems(new ArrayList<>())
            .build();

        if (cicCase.getDraftOrderCICList().isEmpty()) {
            cicCase.setDraftOrderDynamicList(new DynamicList());
            return cicCase;
        }

        List<ListValue<DraftOrderCIC>> draftOrderList = cicCase.getDraftOrderCICList();

        //DynamicDraftList is in reverse order to DraftOrderCICList
        for (int i = draftOrderList.size() - 1; i >= 0; i--) {
            ListValue<DraftOrderCIC> draftOrderCIC = draftOrderList.get(i);

            DynamicListElement element = createDynamicListElement(draftOrderCIC);

            dynamicDraftList.getListItems().add(element);
        }

        cicCase.setDraftOrderDynamicList(dynamicDraftList);
        return cicCase;
    }

    private static DynamicListElement createDynamicListElement(ListValue<DraftOrderCIC> draftOrderCIC) {
        return DynamicListElement.builder()
            .label(generateDraftLabelName(draftOrderCIC.getValue()))
            .code(generateUuidFromDraftURL(draftOrderCIC.getValue().getTemplateGeneratedDocument().getUrl()))
            .build();
    }

    private static String generateDraftLabelName(DraftOrderCIC draftOrderCIC) {

        String filename = draftOrderCIC.getTemplateGeneratedDocument().getFilename();
        String draftOrderTemplateLabel = draftOrderCIC.getDraftOrderContentCIC().getOrderTemplate().getLabel();

        return draftOrderTemplateLabel + DOUBLE_HYPHEN + extractDateFromFileName(filename) + DOUBLE_HYPHEN + "draft.pdf";
    }


    private static String extractDateFromFileName(String filename) {

        Pattern dateTimePattern = Pattern.compile("--(\\d{2}-\\d{2}-\\d{4} \\d{2}:\\d{2}:\\d{2})\\.pdf$");

        Matcher matcher = dateTimePattern.matcher(filename);

        if (matcher.find()) {
            return matcher.group(1);
        }

        throw new IllegalArgumentException("Filename does not contain valid timestamp: " + filename);
    }

    private static UUID generateUuidFromDraftURL(String url) {

        Pattern urlUuidPattern = Pattern.compile("/documents/([0-9a-fA-F\\-]{36})(?:/|$)");

        Matcher matcher = urlUuidPattern.matcher(url);

        if (matcher.find()) {
            return UUID.fromString(matcher.group(1));
        }

        return UUID.randomUUID();
    }

}
