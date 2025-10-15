package uk.gov.hmcts.sptribs.notification.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import java.time.LocalDateTime;

import jakarta.persistence.Id;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import uk.gov.hmcts.ccd.sdk.api.CCD;
import uk.gov.hmcts.ccd.sdk.type.Document;
import uk.gov.hmcts.sptribs.ciccase.model.access.CaseworkerWithCAAAccess;
import uk.gov.hmcts.sptribs.ciccase.model.access.DefaultAccess;

import static uk.gov.hmcts.ccd.sdk.type.FieldType.TextArea;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
public class Correspondence {

    @Column(name = "case_reference")
    private Long caseReference;

    @Column(name = "id")
    @Id
    private Long id;

    @CCD(
        label = "Sent on",
        access = {DefaultAccess.class, CaseworkerWithCAAAccess.class}
    )
    @JsonFormat(pattern = "d MMM y HH:mm")
    @Column(name = "sent_on")
    private LocalDateTime sentOn;

    @CCD(
        label = "From",
        typeOverride = TextArea,
        access = {DefaultAccess.class, CaseworkerWithCAAAccess.class}
    )
    @Column(name = "sent_from")
    private String from;

    @CCD(
        label = "To",
        typeOverride = TextArea,
        access = {DefaultAccess.class, CaseworkerWithCAAAccess.class}
    )
    @Column(name = "sent_to")
    private String to;

    @CCD(
        label = "Document url",
        access = {DefaultAccess.class, CaseworkerWithCAAAccess.class}
    )
    @Column(name = "document_url")
    private String documentUrl;

    @CCD(
        label = "Correspondence type",
        typeOverride = TextArea,
        access = {DefaultAccess.class, CaseworkerWithCAAAccess.class}
    )
    @Column(name = "correspondence_type")
    private String correspondenceType;

    //Add handwritten constructor as a workaround for @JsonUnwrapped prefix issue
    @JsonCreator
    public Correspondence(@JsonProperty("sentOn") LocalDateTime sentOn,
                          @JsonProperty("from") String from,
                          @JsonProperty("to") String to,
                          @JsonProperty("documentUrl") String documentUrl,
                          @JsonProperty("correspondenceType") String correspondenceType) {
        this.sentOn = sentOn;
        this.from = from;
        this.to = to;
        this.documentUrl = documentUrl;
        this.correspondenceType = correspondenceType;
    }

}
