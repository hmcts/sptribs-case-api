package uk.gov.hmcts.sptribs.ciccase.model.casetype;

import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;
import uk.gov.hmcts.sptribs.ciccase.model.CaseData;

/**
 * The case-data class for the Criminal Injuries Compensation case type.
 *
 * <p>Adds no fields: every field is on {@link CaseData}. It exists so that this case type's
 * configs can be declared on a type of their own — {@code CCDDefinitionGenerator} groups
 * configs by their declared case-data class and writes one definition directory per group,
 * so configs declared on the shared base class formed a nameless group whose output
 * directory was the definitions parent itself. See {@code CriminalInjuriesCompensation}.
 *
 * <p>{@code @SuperBuilder} rather than {@code @Builder}, and it has to be on both classes.
 * Lombok's plain {@code @Builder} is not inherited: with it only on the parent,
 * {@code CriminalInjuriesCompensationData.builder()} resolves to the parent's builder and
 * returns a {@link CaseData}, so every caller assigning the result to this type fails to
 * compile. {@code @SuperBuilder} generates a builder per class in the hierarchy that
 * returns the concrete type.
 *
 * <p>No {@code @Data}: it would generate an {@code equals}/{@code hashCode} pair that
 * ignores the superclass, and since every field is on the superclass that means two
 * instances holding different case data would compare equal. The inherited
 * implementations are the correct ones.
 */
@NoArgsConstructor
@SuperBuilder(toBuilder = true)
public class CriminalInjuriesCompensationData extends CaseData {

}
