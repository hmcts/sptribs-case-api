package uk.gov.hmcts.sptribs.controllers.model;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import uk.gov.hmcts.ccd.sdk.type.ListValue;
import uk.gov.hmcts.ccd.sdk.type.YesOrNo;
import uk.gov.hmcts.sptribs.ciccase.model.DssCaseData;
import uk.gov.hmcts.sptribs.ciccase.model.LanguagePreference;
import uk.gov.hmcts.sptribs.document.model.DSSCICDocument;

import java.time.LocalDate;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder(toBuilder = true)
public class DssCaseDataRequest {

    private String dssCaseDataCaseTypeOfApplication;

    private String dssCaseDataSubjectFullName;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
    private LocalDate dssCaseDataSubjectDateOfBirth;

    private String dssCaseDataSubjectEmailAddress;

    private String dssCaseDataSubjectContactNumber;

    private YesOrNo dssCaseDataSubjectAgreeContact;

    private YesOrNo dssCaseDataRepresentation;

    private YesOrNo dssCaseDataRepresentationQualified;

    private String dssCaseDataRepresentativeFullName;

    private String dssCaseDataRepresentativeOrganisationName;

    private String dssCaseDataRepresentativeContactNumber;

    private String dssCaseDataRepresentativeEmailAddress;

    private String dssCaseDataDocumentRelevance;

    private String dssCaseDataAdditionalInformation;

    private String dssCaseDataPcqId;

    private List<ListValue<DSSCICDocument>> dssCaseDataTribunalFormDocuments;

    private List<ListValue<DSSCICDocument>> dssCaseDataSupportingDocuments;

    private List<ListValue<DSSCICDocument>> dssCaseDataOtherInfoDocuments;

    private YesOrNo dssCaseDataIsRepresentativePresent;

    private LanguagePreference dssCaseDataLanguagePreference;

    public static DssCaseDataRequest convertDssCaseDataToRequest(DssCaseData dssCaseData) {
        return DssCaseDataRequest.builder()
            .dssCaseDataCaseTypeOfApplication(dssCaseData.getCaseTypeOfApplication())
            .dssCaseDataSubjectFullName(dssCaseData.getSubjectFullName())
            .dssCaseDataSubjectDateOfBirth(dssCaseData.getSubjectDateOfBirth())
            .dssCaseDataSubjectEmailAddress(dssCaseData.getSubjectEmailAddress())
            .dssCaseDataSubjectContactNumber(dssCaseData.getSubjectContactNumber())
            .dssCaseDataSubjectAgreeContact(dssCaseData.getSubjectAgreeContact())
            .dssCaseDataRepresentation(dssCaseData.getRepresentation())
            .dssCaseDataRepresentationQualified(dssCaseData.getRepresentationQualified())
            .dssCaseDataRepresentativeFullName(dssCaseData.getRepresentativeFullName())
            .dssCaseDataRepresentativeOrganisationName(dssCaseData.getRepresentativeOrganisationName())
            .dssCaseDataRepresentativeContactNumber(dssCaseData.getRepresentativeContactNumber())
            .dssCaseDataRepresentativeEmailAddress(dssCaseData.getRepresentativeEmailAddress())
            .dssCaseDataDocumentRelevance(dssCaseData.getDocumentRelevance())
            .dssCaseDataAdditionalInformation(dssCaseData.getAdditionalInformation())
            .dssCaseDataPcqId(dssCaseData.getPcqId())
            .dssCaseDataTribunalFormDocuments(dssCaseData.getTribunalFormDocuments())
            .dssCaseDataSupportingDocuments(dssCaseData.getSupportingDocuments())
            .dssCaseDataOtherInfoDocuments(dssCaseData.getOtherInfoDocuments())
            .dssCaseDataIsRepresentativePresent(dssCaseData.getIsRepresentativePresent())
            .dssCaseDataLanguagePreference(dssCaseData.getLanguagePreference())
            .build();
    }
}
