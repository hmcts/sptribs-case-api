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
            String auth = "Bearer eyJ0eXAiOiJKV1QiLCJraWQiOiIxZXIwV1J3Z0lPVEFGb2pFNHJDL2ZiZUt1M0k9IiwiYWxnIjoiUlMyNTYifQ.eyJzdWIiOiJzdC10ZXN0NjZAbWFpbGluYXRvci5jb20iLCJjdHMiOiJPQVVUSDJfU1RBVEVMRVNTX0dSQU5UIiwiYXV0aF9sZXZlbCI6MCwiYXVkaXRUcmFja2luZ0lkIjoiYmE5OWViMTktYmI0Yy00NTE2LWJmZjktN2NmNDA4ZTM0ZDAyLTE3MzUwMTIyMyIsInN1Ym5hbWUiOiJzdC10ZXN0NjZAbWFpbGluYXRvci5jb20iLCJpc3MiOiJodHRwczovL2Zvcmdlcm9jay1hbS5zZXJ2aWNlLmNvcmUtY29tcHV0ZS1pZGFtLWFhdDIuaW50ZXJuYWw6ODQ0My9vcGVuYW0vb2F1dGgyL3JlYWxtcy9yb290L3JlYWxtcy9obWN0cyIsInRva2VuTmFtZSI6ImFjY2Vzc190b2tlbiIsInRva2VuX3R5cGUiOiJCZWFyZXIiLCJhdXRoR3JhbnRJZCI6Ik5XZ1NmTjhqS0ZPNTJzczhLQ2JwVFVIUENzSSIsImF1ZCI6InNwdHJpYnMtY2FzZS1hcGkiLCJuYmYiOjE2OTY0MTM2OTQsImdyYW50X3R5cGUiOiJwYXNzd29yZCIsInNjb3BlIjpbIm9wZW5pZCIsInByb2ZpbGUiLCJyb2xlcyJdLCJhdXRoX3RpbWUiOjE2OTY0MTM2OTQsInJlYWxtIjoiL2htY3RzIiwiZXhwIjoxNjk2NDQyNDk0LCJpYXQiOjE2OTY0MTM2OTQsImV4cGlyZXNfaW4iOjI4ODAwLCJqdGkiOiJRTnhwa1R5ZG0tNHl4Rnd0eERSUmVhYlB2MDgifQ.ow0Ieve73mAWoZyBqtehtJRh7Vapbzq6QIuzm0jN02PSgCIibQN1WTHsTp0K3pdIdt3K9S6hSQLgP0IQSVuhG40IV7WVY4YHflWSaqTaqnxCGNW8YBKaUvQHF_0cpyub1wteCG6A4wnYYHfe6m5wqcmsvz7a1IDVp0cVMK09YePjqKyPx24vdTQWefYHRUZvz_cVC79-ZwMncAYPcx8o_rMmgJ1WBhn3Bm-G-0Bw1w0fwk7k_N8MyXhNLVqZnOA3QpyCPebLaXJmjitFJnrzHT03pr7Mo7ihGMNCZ7J0Wip9HvmBXUZd0Uz3CwLGOswTLXJumdcjxj6J18jbPiyWxQ";
            CaseDetails caseDetails = caseApiService.getCaseDetails(auth,
                caseId);
            CaseDetails caseDetails1 = caseApiService.updateNotificationCaseForCaseworker(auth, caseId, caseDetails,
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
