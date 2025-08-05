package uk.gov.hmcts.sptribs.ciccase.model;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import uk.gov.hmcts.ccd.sdk.api.CCD;
import uk.gov.hmcts.ccd.sdk.type.ListValue;
import uk.gov.hmcts.ccd.sdk.type.YesOrNo;
import uk.gov.hmcts.sptribs.ciccase.model.access.CaseworkerWithCAAAccess;
import uk.gov.hmcts.sptribs.ciccase.model.access.CitizenAccess;
import uk.gov.hmcts.sptribs.ciccase.model.access.CitizenDocumentAccess;
import uk.gov.hmcts.sptribs.ciccase.model.access.DefaultAccess;
import uk.gov.hmcts.sptribs.common.MappableObject;
import uk.gov.hmcts.sptribs.document.model.CitizenCICDocument;

import java.time.LocalDate;
import java.util.List;
import java.util.Set;

import static uk.gov.hmcts.ccd.sdk.type.FieldType.Collection;
import static uk.gov.hmcts.ccd.sdk.type.FieldType.TextArea;

@JsonIgnoreProperties(ignoreUnknown = true)
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder(toBuilder = true)
@JsonNaming(PropertyNamingStrategies.UpperCamelCaseStrategy.class)
public class DssCaseData implements MappableObject {

    @CCD(
        label = "caseTypeOfApplication",
        access = {CaseworkerWithCAAAccess.class, CitizenAccess.class}
    )
    private String caseTypeOfApplication;

    @CCD(
        label = "Subject Full Name",
        access = {DefaultAccess.class, CitizenAccess.class}
    )
    private String subjectFullName;

    @CCD(
        label = "Subject Date of Birth",
        access = {DefaultAccess.class, CitizenAccess.class}
    )
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
    private LocalDate subjectDateOfBirth;

    @CCD(
        label = "Subject Email Address",
        access = {DefaultAccess.class, CitizenAccess.class}
    )
    private String subjectEmailAddress;

    @CCD(
        label = "Subject Contact Number",
        access = {DefaultAccess.class, CitizenAccess.class}
    )
    private String subjectContactNumber;

    @CCD(
        label = "Subject Agree To Be Contacted",
        access = {DefaultAccess.class, CitizenAccess.class}
    )
    private YesOrNo subjectAgreeContact;

    @CCD(
        label = "Named Representative",
        access = {DefaultAccess.class, CitizenAccess.class}
    )
    private YesOrNo representation;

    @CCD(
        label = "Named Representative Qualified",
        access = {DefaultAccess.class, CitizenAccess.class}
    )
    private YesOrNo representationQualified;

    @CCD(
        label = "Representative Full Name",
        access = {DefaultAccess.class, CitizenAccess.class}
    )
    private String representativeFullName;

    @CCD(
        label = "Representative Organisation Name",
        access = {DefaultAccess.class, CitizenAccess.class}
    )
    private String representativeOrganisationName;

    @CCD(
        label = "Representative Contact Number",
        access = {DefaultAccess.class, CitizenAccess.class}
    )
    private String representativeContactNumber;

    @CCD(
        label = "Representative Email Address",
        access = {DefaultAccess.class, CitizenAccess.class}
    )
    private String representativeEmailAddress;

    @CCD(
        label = "Document Relevance",
        access = {DefaultAccess.class, CitizenAccess.class}
    )
    private String documentRelevance;

    @CCD(
        label = "Additional Information",
        access = {DefaultAccess.class, CitizenAccess.class}
    )
    private String additionalInformation;

    @CCD(
        access = {DefaultAccess.class, CitizenAccess.class}
    )
    private String pcqId;

    @CCD(
        access = {DefaultAccess.class, CitizenAccess.class}
    )
    private NotificationResponse subjectNotificationResponse;

    @CCD(
        access = {DefaultAccess.class, CitizenAccess.class}
    )
    private NotificationResponse repNotificationResponse;

    @CCD(
        label = "Notified Parties",
        access = {DefaultAccess.class, CitizenAccess.class}
    )
    private Set<NotificationParties> notificationParties;

    @CCD(
        label = "Tribunal form uploaded documents",
        typeOverride = Collection,
        typeParameterOverride = "CitizenCICDocument",
        access = {DefaultAccess.class, CitizenDocumentAccess.class}
    )
    private List<ListValue<CitizenCICDocument>> tribunalFormDocuments;

    @CCD(
        label = "Supporting uploaded documents",
        typeOverride = Collection,
        typeParameterOverride = "CitizenCICDocument",
        access = {DefaultAccess.class, CitizenDocumentAccess.class}
    )
    private List<ListValue<CitizenCICDocument>> supportingDocuments;

    @CCD(
        label = "Other information uploaded documents",
        typeOverride = Collection,
        typeParameterOverride = "CitizenCICDocument",
        access = {DefaultAccess.class, CitizenDocumentAccess.class}
    )
    private List<ListValue<CitizenCICDocument>> otherInfoDocuments;

    @CCD(
        label = "Message",
        typeOverride = TextArea,
        access = {DefaultAccess.class, CitizenAccess.class}
    )
    private String notifyPartyMessage;

    @CCD(
        label = "Is there a representative?",
        access = {DefaultAccess.class, CaseworkerWithCAAAccess.class, CitizenAccess.class}
    )
    private YesOrNo isRepresentativePresent;

    @CCD(
        label = "Language preference",
        access = {DefaultAccess.class, CitizenAccess.class}
    )
    private LanguagePreference languagePreference;
}
