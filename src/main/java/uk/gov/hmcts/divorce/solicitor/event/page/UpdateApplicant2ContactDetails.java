package uk.gov.hmcts.divorce.solicitor.event.page;

import org.springframework.stereotype.Component;
import uk.gov.hmcts.divorce.ciccase.model.Applicant;
import uk.gov.hmcts.divorce.ciccase.model.CaseData;
import uk.gov.hmcts.divorce.common.ccd.CcdPageConfiguration;
import uk.gov.hmcts.divorce.common.ccd.PageBuilder;

@Component
public class UpdateApplicant2ContactDetails implements CcdPageConfiguration {

    @Override
    public void addTo(final PageBuilder pageBuilder) {
        pageBuilder
            .page("Applicant2ContactDetails")
            .pageLabel("Update contact details")
            .complex(CaseData::getApplicant2)
                .mandatoryWithLabel(Applicant::getEmail,
                    "${labelContentTheApplicant2UC} email address")
                .mandatoryWithLabel(Applicant::getPhoneNumber,
                    "${labelContentTheApplicant2UC} phone number")
                .mandatoryWithLabel(Applicant::getAddress,
                    "${labelContentTheApplicant2UC} home address")
                .label("LabelHorizontalLine1App2", "<hr>")
                .mandatoryWithLabel(Applicant::getContactDetailsType,
                    "Should ${labelContentTheApplicant2} contact details be kept private?")
                .done();
    }
}
