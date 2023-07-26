package uk.gov.hmcts.sptribs.caseworker.model;

import com.fasterxml.jackson.annotation.JsonUnwrapped;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import uk.gov.hmcts.ccd.sdk.api.CCD;
import uk.gov.hmcts.sptribs.ciccase.model.access.CaseworkerWithCAAAccess;
import uk.gov.hmcts.sptribs.ciccase.model.access.DefaultAccess;

@Data
@NoArgsConstructor
@Builder
public class Notification {
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public class DssApplicationReceived{
        @CCD(
            access = {DefaultAccess.class, CaseworkerWithCAAAccess.class}
        )
        private String RepNotificationSent;
    }
}
