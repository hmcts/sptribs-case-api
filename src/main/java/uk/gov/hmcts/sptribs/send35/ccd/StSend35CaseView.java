package uk.gov.hmcts.sptribs.send35.ccd;

import org.springframework.stereotype.Component;
import uk.gov.hmcts.ccd.sdk.CaseView;
import uk.gov.hmcts.ccd.sdk.CaseViewRequest;
import uk.gov.hmcts.sptribs.send35.model.StSend35CaseData;
import uk.gov.hmcts.sptribs.send35.model.StSend35State;

/**
 * How CCD reads a SEND35 case back.
 *
 * <p>This case type is decentralised, so CCD holds only a pointer and asks the service for
 * the case data. Every question on the form is a field on the case data, so there is
 * nothing to load from elsewhere and the stored case is returned as it is.
 */
@Component
public class StSend35CaseView implements CaseView<StSend35CaseData, StSend35State> {

    @Override
    public StSend35CaseData getCase(final CaseViewRequest<StSend35State> request,
                                    final StSend35CaseData blobCase) {
        return blobCase;
    }
}
