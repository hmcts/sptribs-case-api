package uk.gov.hmcts.sptribs.ciccase.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import uk.gov.hmcts.ccd.sdk.api.CCD;
import uk.gov.hmcts.ccd.sdk.type.DynamicList;
import uk.gov.hmcts.sptribs.ciccase.model.access.CaseworkerWithCAAAccess;
import uk.gov.hmcts.sptribs.ciccase.model.access.DefaultAccess;

@Data
@NoArgsConstructor
@Builder
public class PanelMember {

    @CCD(
        label = "Name of the panel member",
        access = {DefaultAccess.class, CaseworkerWithCAAAccess.class}
    )
    private DynamicList name;


    @CCD(
        label = "What was their role on panel?",
        access = {DefaultAccess.class, CaseworkerWithCAAAccess.class}
    )
    private PanelMembersRole role;

    @JsonCreator
    public PanelMember(@JsonProperty("name") DynamicList name,
                       @JsonProperty("role") PanelMembersRole role) {
        this.name = name;
        this.role = role;
    }
}
