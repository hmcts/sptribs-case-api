package uk.gov.hmcts.sptribs.notification.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import uk.gov.hmcts.ccd.sdk.api.CCD;
import uk.gov.hmcts.ccd.sdk.type.Document;
import uk.gov.hmcts.sptribs.ciccase.model.access.CaseworkerWithCAAAccess;
import uk.gov.hmcts.sptribs.ciccase.model.access.DefaultAccess;

import java.time.LocalDateTime;
import java.util.UUID;

import static uk.gov.hmcts.ccd.sdk.type.FieldType.TextArea;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Correspondence {

    @CCD(
        label = "Case Reference Number",
        access = {DefaultAccess.class, CaseworkerWithCAAAccess.class}
    )
    private Long caseReferenceNumber;

    @CCD(
        label = "Notification ID",
        access = {DefaultAccess.class, CaseworkerWithCAAAccess.class}
    )
    private String id;

    @CCD(
        label = "Sent on",
        access = {DefaultAccess.class, CaseworkerWithCAAAccess.class}
    )
    @JsonFormat(pattern = "d MMM y HH:mm")
    private LocalDateTime sentOn;

    @CCD(
        label = "From",
        typeOverride = TextArea,
        access = {DefaultAccess.class, CaseworkerWithCAAAccess.class}
    )
    private String from;

    @CCD(
        label = "To",
        typeOverride = TextArea,
        access = {DefaultAccess.class, CaseworkerWithCAAAccess.class}
    )
    private String to;

    @CCD(
        label = "Document url",
        access = {DefaultAccess.class, CaseworkerWithCAAAccess.class}
    )
    private Document documentUrl;

    @CCD(
        label = "Correspondence type",
        typeOverride = TextArea,
        access = {DefaultAccess.class, CaseworkerWithCAAAccess.class}
    )
    private String correspondenceType;

    //Add handwritten constructor as a workaround for @JsonUnwrapped prefix issue
    @JsonCreator
    public Correspondence(@JsonProperty("sentOn") LocalDateTime sentOn,
                          @JsonProperty("from") String from,
                          @JsonProperty("to") String to,
                          @JsonProperty("documentUrl") Document documentUrl,
                          @JsonProperty("correspondenceType") String correspondenceType,
                          @JsonProperty("id") String id) {
        this.sentOn = sentOn;
        this.from = from;
        this.to = to;
        this.documentUrl = documentUrl;
        this.correspondenceType = correspondenceType;
        this.id = id;
    }

}
