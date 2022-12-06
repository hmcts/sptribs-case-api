package uk.gov.hmcts.sptribs.common.ccd;

import uk.gov.hmcts.ccd.sdk.api.Event.EventBuilder;
import uk.gov.hmcts.ccd.sdk.api.FieldCollection.FieldCollectionBuilder;
import uk.gov.hmcts.ccd.sdk.api.callback.MidEvent;
import uk.gov.hmcts.sptribs.ciccase.model.CaseData;
import uk.gov.hmcts.sptribs.ciccase.model.State;
import uk.gov.hmcts.sptribs.ciccase.model.UserRoleCIC;

public class PageBuilder {

    private final EventBuilder<CaseData, UserRoleCIC, State> eventBuilder;

    public PageBuilder(final EventBuilder<CaseData, UserRoleCIC, State> eventBuilder) {
        this.eventBuilder = eventBuilder;
    }

    public FieldCollectionBuilder<CaseData, State, EventBuilder<CaseData, UserRoleCIC, State>> page(final String id) {
        return eventBuilder.fields().page(id);
    }

    public FieldCollectionBuilder<CaseData, State, EventBuilder<CaseData, UserRoleCIC, State>> page(
        final String id,
        final MidEvent<CaseData, State> callback) {

        return eventBuilder.fields().page(id, callback);
    }
}
