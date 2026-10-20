package uk.gov.hmcts.sptribs.wpaf1.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import uk.gov.hmcts.ccd.sdk.api.CCD;
import uk.gov.hmcts.ccd.sdk.type.YesOrNo;
import uk.gov.hmcts.sptribs.ciccase.model.access.CaseworkerWithCAAAccess;
import uk.gov.hmcts.sptribs.ciccase.model.access.DefaultAccess;

/**
 * The UK Special Forces confidentiality confirmation.
 *
 * <p>Anyone who signed the UKSF Confidentiality Contract — and anyone who served with UK
 * Special Forces without signing it — must take advice from the UKSF Disclosure Cell before
 * sending documents to the tribunal. The confirmation is what the caseworker needs to see
 * before circulating the papers.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder(toBuilder = true)
public class StWpaf1Uksf {

    @JsonProperty("ServedWithUksf")
    @CCD(
        label = "Has the appellant served with UK Special Forces?",
        access = {DefaultAccess.class, CaseworkerWithCAAAccess.class}
    )
    private YesOrNo servedWithUksf;

    @JsonProperty("DisclosureCellConfirmed")
    @CCD(
        label = "Confirmed that the UKSF Disclosure Cell has been consulted",
        showCondition = "uksfServedWithUksf=\"Yes\"",
        access = {DefaultAccess.class, CaseworkerWithCAAAccess.class}
    )
    private YesOrNo disclosureCellConfirmed;
}
