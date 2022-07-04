package uk.gov.hmcts.divorce.caseworker.event.page;

import uk.gov.hmcts.divorce.ciccase.model.AcknowledgementOfService;
import uk.gov.hmcts.divorce.ciccase.model.CaseData;
import uk.gov.hmcts.divorce.ciccase.model.FeeDetails;
import uk.gov.hmcts.divorce.common.ccd.CcdPageConfiguration;
import uk.gov.hmcts.divorce.common.ccd.PageBuilder;

public class AnswerReceivedPaymentConfirmation implements CcdPageConfiguration {

    @Override
    public void addTo(final PageBuilder pageBuilder) {
        pageBuilder.page("answerReceivedPayment")
            .pageLabel("Payment - answer application payment")
            .complex(CaseData::getAcknowledgementOfService)
                .complex(AcknowledgementOfService::getDisputingFee)
                .mandatory(FeeDetails::getPaymentMethod)
                .mandatory(FeeDetails::getAccountNumber,
                    "disputingFeePaymentMethod = \"feePayByAccount\"")
                .optional(FeeDetails::getAccountReferenceNumber,
                    "disputingFeePaymentMethod = \"feePayByAccount\"")
                .mandatory(FeeDetails::getHelpWithFeesReferenceNumber,
                    "disputingFeePaymentMethod = \"feePayByHelp\"")
                .done()
            .done();
    }
}
