package uk.gov.hmcts.sptribs.systemupdate.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class CcdCreateService {

    /*private static final String DIVORCE_CASE_SUBMISSION_EVENT_SUMMARY = "No Fault Divorce case submission event";
    private static final String DIVORCE_CASE_SUBMISSION_EVENT_DESCRIPTION = "Submitting No Fault Divorce Case Event";

    @Autowired
    private CoreCaseDataApi coreCaseDataApi;

    @Autowired
    private CcdCaseDataContentProvider ccdCaseDataContentProvider;

    @Autowired
    private CaseDetailsConverter caseDetailsConverter;*/

    /*public uk.gov.hmcts.ccd.sdk.api.CaseDetails<BulkActionCaseData, BulkActionState> createBulkCase(
        final uk.gov.hmcts.ccd.sdk.api.CaseDetails<BulkActionCaseData, BulkActionState> caseDetails,
        final User user,
        final String serviceAuth) {

        final String userId = user.getUserDetails().getId();
        final String authorization = user.getAuthToken();

        try {

            final StartEventResponse startEventResponse = coreCaseDataApi.startForCaseworker(
                authorization,
                serviceAuth,
                userId,
                JURISDICTION,
                CASE_TYPE,
                CREATE_BULK_LIST
            );

            final CaseDataContent caseDataContent = ccdCaseDataContentProvider.createCaseDataContent(
                startEventResponse,
                DIVORCE_CASE_SUBMISSION_EVENT_SUMMARY,
                DIVORCE_CASE_SUBMISSION_EVENT_DESCRIPTION,
                caseDetails.getData());

            final CaseDetails bulkCaseDetails = coreCaseDataApi.submitForCaseworker(
                authorization,
                serviceAuth,
                userId,
                JURISDICTION,
                CASE_TYPE,
                true,
                caseDataContent);

            return caseDetailsConverter.convertToBulkActionCaseDetailsFromReformModel(bulkCaseDetails);

        } catch (final FeignException e) {
            throw new CcdManagementException(e.status(), "Bulk case creation failed", e);
        }
    }*/
}
