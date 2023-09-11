package uk.gov.hmcts.sptribs.systemupdate.convert;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.sptribs.ciccase.model.CaseData;
import uk.gov.hmcts.sptribs.ciccase.model.State;

import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;


@Component
public class CaseDetailsConverter {

    @Autowired
    private ObjectMapper objectMapper;

    public CaseDetails convertToReformModelFromCaseDetails(final uk.gov.hmcts.ccd.sdk.api.CaseDetails<CaseData, State> caseDetails) {
        return objectMapper.convertValue(caseDetails, CaseDetails.class);
    }

    public uk.gov.hmcts.ccd.sdk.api.CaseDetails<CaseData, State> convertToCaseDetailsFromReformModel(final CaseDetails caseDetails) {
        return objectMapper.convertValue(caseDetails, new TypeReference<>() {
        });
    }

    public CaseData toCaseData(CaseDetails caseDetails) {
        Map<String, Object> data = new HashMap<>(caseDetails.getData());
        data.put("ccdCaseReference", caseDetails.getId());

        CaseData caseData = objectMapper.convertValue(data, CaseData.class);
        //caseData.getCicCase().getNotifications().getCaseWithdrawnNotification().setName("Notification sent");
        //caseData.getCicCase().getNotifications().getCaseWithdrawnNotification().setReference(Calendar.getInstance().getTime().toString());
        return caseData;
    }
}
