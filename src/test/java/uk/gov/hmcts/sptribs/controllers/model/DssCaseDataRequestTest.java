package uk.gov.hmcts.sptribs.controllers.model;

import org.junit.jupiter.api.Test;
import uk.gov.hmcts.ccd.sdk.type.Document;
import uk.gov.hmcts.ccd.sdk.type.ListValue;
import uk.gov.hmcts.sptribs.ciccase.model.DssCaseData;
import uk.gov.hmcts.sptribs.document.model.CitizenCICDocument;

import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static uk.gov.hmcts.ccd.sdk.type.YesOrNo.YES;
import static uk.gov.hmcts.sptribs.ciccase.model.LanguagePreference.ENGLISH;
import static uk.gov.hmcts.sptribs.controllers.model.DssCaseDataRequest.convertDssCaseDataToRequest;

public class DssCaseDataRequestTest {

    @Test
    public void shouldMapDssCaseDataObjectIntoDssCaseDataRequestObject() {
        DssCaseData dssCaseData = DssCaseData.builder()
            .caseTypeOfApplication("CIC")
            .subjectFullName("Test Name")
            .subjectDateOfBirth(LocalDate.of(1990,1,1))
            .subjectEmailAddress("test@email.com")
            .subjectContactNumber("071231231235")
            .subjectAgreeContact(YES)
            .representation(YES)
            .representationQualified(null)
            .representativeFullName("Test Rep")
            .representativeOrganisationName("Test Rep Co")
            .representativeContactNumber("079879879875")
            .representativeEmailAddress("rep@repco.com")
            .documentRelevance("Test relevance")
            .additionalInformation("Test additional info")
            .pcqId("PCD_ID_1")
            .tribunalFormDocuments(getDssCaseDataDocuments())
            .supportingDocuments(getDssCaseDataDocuments())
            .otherInfoDocuments(null)
            .isRepresentativePresent(YES)
            .languagePreference(ENGLISH)
            .build();

        DssCaseDataRequest request = convertDssCaseDataToRequest(dssCaseData);

        assertEquals(request.getDssCaseDataCaseTypeOfApplication(), dssCaseData.getCaseTypeOfApplication());
        assertEquals(request.getDssCaseDataSubjectFullName(), dssCaseData.getSubjectFullName());
        assertEquals(request.getDssCaseDataSubjectDateOfBirth(), dssCaseData.getSubjectDateOfBirth());
        assertEquals(request.getDssCaseDataSubjectEmailAddress(), dssCaseData.getSubjectEmailAddress());
        assertEquals(request.getDssCaseDataSubjectContactNumber(), dssCaseData.getSubjectContactNumber());
        assertEquals(request.getDssCaseDataSubjectAgreeContact(), dssCaseData.getSubjectAgreeContact());
        assertEquals(request.getDssCaseDataRepresentation(), dssCaseData.getRepresentation());
        assertNull(request.getDssCaseDataRepresentationQualified());
        assertEquals(request.getDssCaseDataRepresentativeFullName(), dssCaseData.getRepresentativeFullName());
        assertEquals(request.getDssCaseDataRepresentativeOrganisationName(), dssCaseData.getRepresentativeOrganisationName());
        assertEquals(request.getDssCaseDataRepresentativeContactNumber(), dssCaseData.getRepresentativeContactNumber());
        assertEquals(request.getDssCaseDataRepresentativeEmailAddress(), dssCaseData.getRepresentativeEmailAddress());
        assertEquals(request.getDssCaseDataDocumentRelevance(), dssCaseData.getDocumentRelevance());
        assertEquals(request.getDssCaseDataAdditionalInformation(), dssCaseData.getAdditionalInformation());
        assertEquals(request.getDssCaseDataPcqId(), dssCaseData.getPcqId());
        assertEquals(request.getDssCaseDataTribunalFormDocuments(), dssCaseData.getTribunalFormDocuments());
        assertEquals(request.getDssCaseDataSupportingDocuments(), dssCaseData.getSupportingDocuments());
        assertNull(request.getDssCaseDataOtherInfoDocuments());
        assertEquals(request.getDssCaseDataIsRepresentativePresent(), dssCaseData.getIsRepresentativePresent());
        assertEquals(request.getDssCaseDataLanguagePreference(), dssCaseData.getLanguagePreference());
    }

    private List<ListValue<CitizenCICDocument>> getDssCaseDataDocuments() {
        CitizenCICDocument doc1 = new CitizenCICDocument();
        doc1.setDocumentLink(
            Document.builder()
                .filename("doc1.pdf")
                .binaryUrl("doc1.pdf/binary")
                .categoryId("test category")
                .build()
        );
        doc1.setComment("this doc is relevant to the case");
        CitizenCICDocument doc2 = new CitizenCICDocument();
        doc2.setDocumentLink(
            Document.builder()
                .filename("doc2.pdf")
                .binaryUrl("doc2.pdf/binary")
                .categoryId("test category")
                .build()
        );
        doc2.setComment("this doc is also relevant to the case");
        return List.of(
            new ListValue<>("1", doc1),
            new ListValue<>("2", doc2)
        );
    }
}
