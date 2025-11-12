package uk.gov.hmcts.sptribs.ciccase.model;

import lombok.Builder;
import lombok.Data;
import uk.gov.hmcts.ccd.sdk.api.CCD;
import uk.gov.hmcts.ccd.sdk.type.YesOrNo;
import uk.gov.hmcts.sptribs.ciccase.model.access.CaseworkerWithCAAAccess;
import uk.gov.hmcts.sptribs.ciccase.model.access.DefaultAccess;

@Data
@Builder
public class Anonymisation {
        @CCD(
            label = "Apply anonymity to the case?",
            access = {DefaultAccess.class, CaseworkerWithCAAAccess.class}
        )
        private YesOrNo anonymiseYesOrNo;

        @CCD(
            label = "Anonymised Name",
            access = {DefaultAccess.class, CaseworkerWithCAAAccess.class}
        )
        private String anonymisedAppellantName;
}
