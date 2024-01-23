package uk.gov.hmcts.sptribs.document.content;

import org.springframework.util.CollectionUtils;
import uk.gov.hmcts.ccd.sdk.type.ListValue;
import uk.gov.hmcts.sptribs.ciccase.model.CaseData;
import uk.gov.hmcts.sptribs.ciccase.model.PanelMember;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static uk.gov.hmcts.sptribs.document.content.DocmosisTemplateConstants.CASE_NUMBER;
import static uk.gov.hmcts.sptribs.document.content.DocmosisTemplateConstants.CIC_CASE_SCHEME;
import static uk.gov.hmcts.sptribs.document.content.DocmosisTemplateConstants.COMMA_SPACE;
import static uk.gov.hmcts.sptribs.document.content.DocmosisTemplateConstants.DECISION_DATED;
import static uk.gov.hmcts.sptribs.document.content.DocmosisTemplateConstants.DIRECTION_DATED;
import static uk.gov.hmcts.sptribs.notification.FormatUtil.FILE_NAME_DATE_FORMATTER;

public final class DocmosisTemplateHelper {

    private static final String EMPTY = "";

    private DocmosisTemplateHelper() {
    }

    public static String getMembers(List<ListValue<PanelMember>> memberList) {
        if (!CollectionUtils.isEmpty(memberList)) {
            StringBuilder members = new StringBuilder(100);

            for (ListValue<PanelMember> panelMember : memberList) {
                if (panelMember != null && panelMember.getValue() != null) {
                    members.append(panelMember.getValue().getName().getValue().getLabel()).append(COMMA_SPACE);
                }
            }

            return (members.length() > 0) ? members.substring(0, members.length() - 2) : EMPTY;
        }

        return EMPTY;
    }

    public static Map<String, Object> getCommonFields(final CaseData caseData, final Long ccdCaseReference) {

        Map<String, Object> templateContent = new HashMap<>();

        templateContent.put(DECISION_DATED, LocalDate.now().format(FILE_NAME_DATE_FORMATTER));
        templateContent.put(DIRECTION_DATED, LocalDate.now().format(FILE_NAME_DATE_FORMATTER));
        templateContent.put(CASE_NUMBER, ccdCaseReference);

        if (caseData.getCicCase() != null && caseData.getCicCase().getSchemeCic() != null) {
            templateContent.put(CIC_CASE_SCHEME, caseData.getCicCase().getSchemeCic().getLabel());
        }

        return templateContent;
    }
}
