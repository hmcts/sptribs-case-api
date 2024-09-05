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
            if (!AppsUtil.isValidCaseTypeOfApplication(appsConfig, caseData.getDssCaseData())) {
                throw new CaseCreateOrUpdateException("Invalid Case type application. Please check the request.");
            }

            final CaseDetails caseDetails = caseApiService.createCase(authorization, caseData,
                                                                AppsUtil.getExactAppsDetails(appsConfig, caseData.getDssCaseData()));
            return CaseResponse.builder().caseData(caseDetails.getData())
                .id(caseDetails.getId()).status(SUCCESS).build();

        } catch (Exception e) {
            log.error("Error while creating case. " + e);
            throw new CaseCreateOrUpdateException("Failing while creating the case " + e.getMessage(), e);
        }
    }

    public CaseResponse updateCase(String authorization, Event event, CaseData caseData, Long caseId) {

        try {
            if (!AppsUtil.isValidCaseTypeOfApplication(appsConfig, caseData.getDssCaseData())) {
                throw new CaseCreateOrUpdateException("Invalid Case type application. Please check the request.");
            }

            final CaseDetails caseDetails = caseApiService.updateCase(authorization, event, caseId, caseData,
                                                                AppsUtil.getExactAppsDetails(appsConfig, caseData.getDssCaseData()));
            return CaseResponse.builder().caseData(caseDetails.getData())
                .id(caseDetails.getId()).status(SUCCESS).build();
        } catch (Exception e) {
            log.error("Error while updating case. " + e);
            throw new CaseCreateOrUpdateException("Failing while updating the case " + e.getMessage(), e);
        }
    }

    public CaseResponse fetchCaseDetails(String authorization,Long caseId) {

        try {
            final CaseDetails caseDetails = caseApiService.getCaseDetails(authorization,
                                                                    caseId);
            return CaseResponse.builder().caseData(caseDetails.getData())
                .id(caseDetails.getId()).status(SUCCESS).build();
        } catch (Exception e) {
            log.error("Error while fetching Case Details. " + e);
            throw new CaseCreateOrUpdateException("Failing while fetching the case details " + e.getMessage(), e);
        }
    }
}
