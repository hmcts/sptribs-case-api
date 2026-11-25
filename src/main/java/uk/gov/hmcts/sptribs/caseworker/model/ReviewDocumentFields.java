package uk.gov.hmcts.sptribs.caseworker.model;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;
import uk.gov.hmcts.ccd.sdk.api.CCD;
import uk.gov.hmcts.sptribs.ciccase.model.access.CaseworkerWithCAAAccess;
import uk.gov.hmcts.sptribs.ciccase.model.access.DefaultAccess;
import uk.gov.hmcts.sptribs.document.model.CaseworkerCICDocument;

import java.util.Set;

import static uk.gov.hmcts.ccd.sdk.type.FieldType.MultiSelectList;
import static uk.gov.hmcts.ccd.sdk.type.FieldType.TextArea;

@Data
@NoArgsConstructor
@SuperBuilder(toBuilder = true)
public abstract class ReviewDocumentFields implements ReviewDocumentSlots {

    @CCD(typeOverride = MultiSelectList, typeParameterOverride = "Slot",
        retainHiddenValue = true, access = {DefaultAccess.class, CaseworkerWithCAAAccess.class})
    private Set<Slot> activeDocumentSlots;

    @CCD(label = "Document 1", access = {DefaultAccess.class, CaseworkerWithCAAAccess.class})
    private CaseworkerCICDocument d01;

    @CCD(label = "Document 2", access = {DefaultAccess.class, CaseworkerWithCAAAccess.class})
    private CaseworkerCICDocument d02;

    @CCD(label = "Document 3", access = {DefaultAccess.class, CaseworkerWithCAAAccess.class})
    private CaseworkerCICDocument d03;

    @CCD(label = "Document 4", access = {DefaultAccess.class, CaseworkerWithCAAAccess.class})
    private CaseworkerCICDocument d04;

    @CCD(label = "Document 5", access = {DefaultAccess.class, CaseworkerWithCAAAccess.class})
    private CaseworkerCICDocument d05;

    @CCD(label = "Document 6", access = {DefaultAccess.class, CaseworkerWithCAAAccess.class})
    private CaseworkerCICDocument d06;

    @CCD(label = "Document 7", access = {DefaultAccess.class, CaseworkerWithCAAAccess.class})
    private CaseworkerCICDocument d07;

    @CCD(label = "Document 8", access = {DefaultAccess.class, CaseworkerWithCAAAccess.class})
    private CaseworkerCICDocument d08;

    @CCD(label = "Document 9", access = {DefaultAccess.class, CaseworkerWithCAAAccess.class})
    private CaseworkerCICDocument d09;

    @CCD(label = "Document 10", access = {DefaultAccess.class, CaseworkerWithCAAAccess.class})
    private CaseworkerCICDocument d10;

    @CCD(label = "Selected parties", typeOverride = TextArea,
        access = {DefaultAccess.class, CaseworkerWithCAAAccess.class})
    private String reviewSelectedParties;
}
