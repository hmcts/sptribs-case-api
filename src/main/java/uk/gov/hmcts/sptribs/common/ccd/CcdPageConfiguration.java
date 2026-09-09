package uk.gov.hmcts.sptribs.common.ccd;

import uk.gov.hmcts.sptribs.ciccase.model.CaseData;

/**
 * One page of an event, contributed to whichever event is being built.
 *
 * <p>The page classes implementing this describe fields of {@link CaseData} — the base
 * class — so they are usable by any case type whose data class extends it. The method is
 * generic now that {@link PageBuilder} is, so that these stay shared across case types
 * rather than being rewritten per case type, which is the whole reason they are separate
 * classes.
 *
 * <p>A type parameter rather than a {@code ? extends CaseData} wildcard, deliberately. A
 * wildcard captures to a fresh type on each call, and a page that registers a mid-event
 * callback needs to name the builder's type to do it — a {@code MidEvent<CaseData, State>}
 * cannot be passed where the captured type is expected:
 *
 * <pre>
 *     incompatible types: CaseDetails&lt;CAP#1,State&gt; cannot be converted to
 *     CaseDetails&lt;CaseData,State&gt;
 * </pre>
 *
 * <p>With a named parameter the implementation can declare its callback on {@code T} and the
 * call site infers it from the builder.
 *
 * @param <T> the case-data class of the case type this page is being added to
 */
public interface CcdPageConfiguration {

    <T extends CaseData> void addTo(final PageBuilder<T> pageBuilder);
}
