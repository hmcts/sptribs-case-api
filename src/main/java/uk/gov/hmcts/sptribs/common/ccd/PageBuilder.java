package uk.gov.hmcts.sptribs.common.ccd;

import uk.gov.hmcts.ccd.sdk.api.Event.EventBuilder;
import uk.gov.hmcts.ccd.sdk.api.FieldCollection.FieldCollectionBuilder;
import uk.gov.hmcts.ccd.sdk.api.callback.MidEvent;
import uk.gov.hmcts.sptribs.ciccase.model.CaseData;
import uk.gov.hmcts.sptribs.ciccase.model.State;
import uk.gov.hmcts.sptribs.ciccase.model.UserRole;

/**
 * Page-building helper, generic over the case-data class.
 *
 * <p>It used to be fixed to {@link CaseData}, which was fine while every event config was
 * declared on the base class. Those configs now declare the case type's own data class, so
 * that the SDK groups them with their case type instead of into a nameless group whose
 * output directory is the definitions parent — see {@code CriminalInjuriesCompensation} for
 * why that mattered. An {@code EventBuilder} typed on the derived class cannot be passed to
 * a helper hard-coded to the base.
 *
 * <p>Generic rather than duplicated per case type: the data classes all extend
 * {@link CaseData} and this helper only passes the type through to the builder, so one
 * bounded parameter covers every case type.
 *
 * @param <T> the case type's own case-data class
 */
public class PageBuilder<T extends CaseData> {

    private final EventBuilder<T, UserRole, State> eventBuilder;

    public PageBuilder(final EventBuilder<T, UserRole, State> eventBuilder) {
        this.eventBuilder = eventBuilder;
    }

    public FieldCollectionBuilder<T, State, EventBuilder<T, UserRole, State>> page(final String id) {
        return eventBuilder.fields().page(id);
    }

    public FieldCollectionBuilder<T, State, EventBuilder<T, UserRole, State>> page(
        final String id,
        final MidEvent<T, State> callback) {

        return eventBuilder.fields().page(id, callback);
    }
}
