package uk.gov.hmcts.sptribs.document.print.model;

import lombok.Getter;
import uk.gov.hmcts.sptribs.document.model.DivorceDocument;

@Getter
public class Letter {
    private DivorceDocument divorceDocument;
    private final int count;

    public Letter(DivorceDocument divorceDocument, int count) {
        this.divorceDocument = divorceDocument;
        this.count = count;
    }

}
