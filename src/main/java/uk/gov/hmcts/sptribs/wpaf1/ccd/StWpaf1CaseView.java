package uk.gov.hmcts.sptribs.wpaf1.ccd;

import org.springframework.stereotype.Component;
import uk.gov.hmcts.ccd.sdk.CaseView;
import uk.gov.hmcts.ccd.sdk.CaseViewRequest;
import uk.gov.hmcts.sptribs.wpaf1.model.StWpaf1CaseData;
import uk.gov.hmcts.sptribs.wpaf1.model.StWpaf1State;

/**
 * How CCD reads a WPAF1 case back.
 *
 * <p>This case type is decentralised, so CCD holds only a pointer and asks the service for
 * the case data. Every question on the form is a field on the case data, so there is
 * nothing to load from elsewhere and the stored case is returned as it is.
 */
@Component
public class StWpaf1CaseView implements CaseView<StWpaf1CaseData, StWpaf1State> {

    @Override
    public StWpaf1CaseData getCase(final CaseViewRequest<StWpaf1State> request,
                                   final StWpaf1CaseData blobCase) {
        return blobCase;
    }
}
