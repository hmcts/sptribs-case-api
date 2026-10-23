package uk.gov.hmcts.sptribs.caseworker.model;

import uk.gov.hmcts.sptribs.document.model.CaseworkerCICDocument;

import java.util.Set;

public interface ReviewDocumentSlots {

    void setActiveDocumentSlots(Set<Slot> slots);

    void setD01(CaseworkerCICDocument document);

    void setD02(CaseworkerCICDocument document);

    void setD03(CaseworkerCICDocument document);

    void setD04(CaseworkerCICDocument document);

    void setD05(CaseworkerCICDocument document);

    void setD06(CaseworkerCICDocument document);

    void setD07(CaseworkerCICDocument document);

    void setD08(CaseworkerCICDocument document);

    void setD09(CaseworkerCICDocument document);

    void setD10(CaseworkerCICDocument document);
}
