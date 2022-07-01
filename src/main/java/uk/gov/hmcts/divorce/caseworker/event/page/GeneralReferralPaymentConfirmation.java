package uk.gov.hmcts.divorce.caseworker.event.page;

import uk.gov.hmcts.divorce.ciccase.model.CaseData;
import uk.gov.hmcts.divorce.ciccase.model.FeeDetails;
import uk.gov.hmcts.divorce.ciccase.model.GeneralReferral;
import uk.gov.hmcts.divorce.common.ccd.CcdPageConfiguration;
import uk.gov.hmcts.divorce.common.ccd.PageBuilder;

public class GeneralReferralPaymentConfirmation implements CcdPageConfiguration {

    @Override
    public void addTo(PageBuilder pageBuilder) {
        pageBuilder.page("GeneralReferralPayment")
            .pageLabel("Payment - general referral payment")
            .complex(CaseData::getGeneralReferral)
            .complex(GeneralReferral::getGeneralReferralFee)
            .mandatory(FeeDetails::getPaymentMethod)
            .mandatory(FeeDetails::getAccountNumber,
                "generalReferralFeePaymentMethod=\"feePayByAccount\"")
            .optional(FeeDetails::getAccountReferenceNumber,
                "generalReferralFeePaymentMethod=\"feePayByAccount\"")
            .mandatory(FeeDetails::getHelpWithFeesReferenceNumber,
                "generalReferralFeePaymentMethod=\"feePayByHelp\"")
            .done()
            .done();
    }
}
