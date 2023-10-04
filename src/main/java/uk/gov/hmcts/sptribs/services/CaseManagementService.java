package uk.gov.hmcts.sptribs.services;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.sptribs.ciccase.model.CaseData;
import uk.gov.hmcts.sptribs.common.config.AppsConfig;
import uk.gov.hmcts.sptribs.edgecase.event.Event;
import uk.gov.hmcts.sptribs.exception.CaseCreateOrUpdateException;
import uk.gov.hmcts.sptribs.model.CaseResponse;
import uk.gov.hmcts.sptribs.notification.model.NotificationRequest;
import uk.gov.hmcts.sptribs.notification.model.NotificationResponse;
import uk.gov.hmcts.sptribs.services.ccd.CaseApiService;
import uk.gov.hmcts.sptribs.util.AppsUtil;

@Service
@Slf4j
public class CaseManagementService {

    @Autowired
    CaseApiService caseApiService;

    @Autowired
    AppsConfig appsConfig;

    private static final String SUCCESS = "Success";

    public CaseResponse createCase(String authorization, CaseData caseData) {
        try {
            // Validate Case Data (CHECKING CASE TYPE ALONE)
            log.info("Case data received from UI: " + caseData.toString());
            if (!AppsUtil.isValidCaseTypeOfApplication(appsConfig, caseData.getDssCaseData())) {
                throw new CaseCreateOrUpdateException("Invalid Case type application. Please check the request.");
            }

            // creating case to CCD.
            CaseDetails caseDetails = caseApiService.createCase(authorization, caseData,
                                                                AppsUtil.getExactAppsDetails(appsConfig, caseData.getDssCaseData()));
            log.info("Created case details: " + caseDetails.toString());
            return CaseResponse.builder().caseData(caseDetails.getData())
                .id(caseDetails.getId()).status(SUCCESS).build();


        } catch (Exception e) {
            log.error("Error while creating case." + e);
            throw new CaseCreateOrUpdateException("Failing while creating the case" + e.getMessage(), e);
        }
    }

    public CaseResponse updateCase(String authorization, Event event, CaseData caseData, Long caseId) {
        try {
            // Validate Case Type of application
            if (!AppsUtil.isValidCaseTypeOfApplication(appsConfig, caseData.getDssCaseData())) {
                throw new CaseCreateOrUpdateException("Invalid Case type application. Please check the request.");
            }

            // Updating or Submitting case to CCD..
            CaseDetails caseDetails = caseApiService.updateCaseForCitizen(authorization, event, caseId, caseData,
                                                                AppsUtil.getExactAppsDetails(appsConfig, caseData.getDssCaseData()));
            log.info("Updated case details: " + caseDetails.toString());
            return CaseResponse.builder().caseData(caseDetails.getData())
                .id(caseDetails.getId()).status(SUCCESS).build();
        } catch (Exception e) {
            //This has to be corrected
            log.error("Error while updating case." + e);
            throw new CaseCreateOrUpdateException("Failing while updating the case" + e.getMessage(), e);
        }
    }

    public CaseResponse fetchCaseDetails(String authorization,Long caseId) {

        try {
            CaseDetails caseDetails = caseApiService.getCaseDetails(authorization,
                                                                    caseId);
            log.info("Case Details for CaseID :{} and CaseDetails:{}", caseId, caseDetails);
            return CaseResponse.builder().caseData(caseDetails.getData())
                .id(caseDetails.getId()).status(SUCCESS).build();
        } catch (Exception e) {
            log.error("Error while fetching Case Details" + e);
            throw new CaseCreateOrUpdateException("Failing while fetching the case details" + e.getMessage(), e);
        }

    }

    public CaseResponse updateNotificationDetails(String authorization, Long caseId,
                                                  NotificationRequest notificationResponse) {
        try {
            String caseTypeOfApplication = "CIC";
            String auth = "Bearer eyJ0eXAiOiJKV1QiLCJraWQiOiIxZXIwV1J3Z0lPVEFGb2pFNHJDL2ZiZUt1M0k9IiwiYWxnIjoiUlMyNTYifQ.eyJzdWIiOiJzdC10ZXN0NjZAbWFpbGluYXRvci5jb20iLCJjdHMiOiJPQVVUSDJfU1RBVEVMRVNTX0dSQU5UIiwiYXV0aF9sZXZlbCI6MCwiYXVkaXRUcmFja2luZ0lkIjoiNDcyMGE1M2YtZTQ2MS00Yzc3LTk5NmYtN2M2YThjZDc4ZGI4LTY4Mjk1ODU4Iiwic3VibmFtZSI6InN0LXRlc3Q2NkBtYWlsaW5hdG9yLmNvbSIsImlzcyI6Imh0dHBzOi8vZm9yZ2Vyb2NrLWFtLnNlcnZpY2UuY29yZS1jb21wdXRlLWlkYW0tYWF0Mi5pbnRlcm5hbDo4NDQzL29wZW5hbS9vYXV0aDIvcmVhbG1zL3Jvb3QvcmVhbG1zL2htY3RzIiwidG9rZW5OYW1lIjoiYWNjZXNzX3Rva2VuIiwidG9rZW5fdHlwZSI6IkJlYXJlciIsImF1dGhHcmFudElkIjoiSE9XMEZQUUJudU5URnVKMTFGaU9MejBRd0w0IiwiYXVkIjoic3B0cmlicy1jYXNlLWFwaSIsIm5iZiI6MTY5NTg5OTg3OCwiZ3JhbnRfdHlwZSI6InBhc3N3b3JkIiwic2NvcGUiOlsib3BlbmlkIiwicHJvZmlsZSIsInJvbGVzIl0sImF1dGhfdGltZSI6MTY5NTg5OTg3OCwicmVhbG0iOiIvaG1jdHMiLCJleHAiOjE2OTU5Mjg2NzgsImlhdCI6MTY5NTg5OTg3OCwiZXhwaXJlc19pbiI6Mjg4MDAsImp0aSI6IlhDdHlIUGo4eE4wZUdNQmpJc0ZCcU9LQTRHdyJ9.b3j8VXQQwGCqvU6oqol1AgEVVD9NyADrUB9qvNKMkUK_MoNSON8jpWdvyiQf1JB-38ppnuhxsI0bY87Z_OlPBXvXItUwjOSgGR9a6lNjDyhALdUVVTvhL7fOOlg6K1nERxxKLVC3XOHLKzAivjUnigQUFEPMNtgPbXG2qghaSjsd9D_YbRZaNkvD5Nnp80BIhhVzrVzwotjc2_PJARUlrsQRmYhla8vJAUIhTu_op2u2jqcGbis-ssHwAQ08ZH1FJ0RSuckr1dHU_3ZJmXiWeYZQSDpcWqtiM1S5YmN1sIkv3jxvuhwgc24AdxrvZ0EsACh3jLTEzlZAw3NQuyM6iA";
            CaseDetails caseDetails = caseApiService.getCaseDetails(auth,
                caseId);
            CaseDetails caseDetails1 = caseApiService.updateCaseForCaseworker(auth, caseId, caseDetails,
                AppsUtil.getExactAppsDetails(appsConfig, caseTypeOfApplication), notificationResponse);



            log.info("Case Details for CaseID :{} and CaseDetails:{}", caseId, caseDetails);
            return CaseResponse.builder().caseData(caseDetails.getData())
                .id(caseDetails.getId()).status(SUCCESS).build();
        } catch (Exception e) {
            log.error("Error while fetching Case Details" + e);
            throw new CaseCreateOrUpdateException("Failing while fetching the case details" + e.getMessage(), e);
        }

    }

}
