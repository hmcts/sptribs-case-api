package uk.gov.hmcts.sptribs.caseworker.util;

import lombok.extern.slf4j.Slf4j;
import org.springframework.util.CollectionUtils;
import org.springframework.util.ObjectUtils;
import uk.gov.hmcts.ccd.sdk.type.Document;
import uk.gov.hmcts.ccd.sdk.type.ListValue;
import uk.gov.hmcts.sptribs.caseworker.model.DraftOrderCIC;
import uk.gov.hmcts.sptribs.ciccase.model.CicCase;
import uk.gov.hmcts.sptribs.document.model.CaseworkerCICDocument;
import uk.gov.hmcts.sptribs.document.model.DocumentType;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Slf4j
public class DraftOrderDocumentListUtil {

    private DraftOrderDocumentListUtil() {

    }

    public static List<CaseworkerCICDocument> getDraftOrderDocuments(CicCase cicCase) {
        List<CaseworkerCICDocument> draftOrderList = new ArrayList<>();

        try {
            if (!CollectionUtils.isEmpty(cicCase.getDraftOrderCICList())) {
                for (ListValue<DraftOrderCIC> draftOrderListValue : cicCase.getDraftOrderCICList()) {
                    if (draftOrderListValue.getValue().getDraftOrderContentCIC() != null
                        && draftOrderListValue.getValue().getTemplateGeneratedDocument() != null
                        && !ObjectUtils.isEmpty(draftOrderListValue.getValue().getTemplateGeneratedDocument().getFilename())) {
                        Document templateGeneratedDoc = draftOrderListValue.getValue().getTemplateGeneratedDocument();
                        CaseworkerCICDocument doc = CaseworkerCICDocument.builder()
                            .documentLink(templateGeneratedDoc)
                            .documentCategory(DocumentType.TRIBUNAL_DIRECTION)
                            .date(extractLocalDateFromFileName(draftOrderListValue.getValue().getTemplateGeneratedDocument().getFilename()))
                            .build();
                        draftOrderList.add(doc);
                    }
                }
            }
            return draftOrderList;
        } catch (RuntimeException e) {
            log.error("ERROR GETTING DRAFT ORDERS:  ", e);
            throw e;
        }
    }

    private static LocalDate extractLocalDateFromFileName(String fileName) {
        Pattern pattern = Pattern.compile("(\\d{2}-\\d{2}-\\d{4})");
        Matcher matcher = pattern.matcher(fileName);
        if (matcher.find()) {
            return LocalDate.parse(matcher.group(1), DateTimeFormatter.ofPattern("dd-MM-yyyy"));
        }
        throw new IllegalArgumentException("No date pattern found in filename for draft order: " + fileName);
    }
}
