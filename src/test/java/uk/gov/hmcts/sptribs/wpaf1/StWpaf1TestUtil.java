package uk.gov.hmcts.sptribs.wpaf1;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonUnwrapped;
import com.google.common.collect.ImmutableSet;
import uk.gov.hmcts.ccd.sdk.ConfigBuilderImpl;
import uk.gov.hmcts.ccd.sdk.ResolvedCCDConfig;
import uk.gov.hmcts.ccd.sdk.api.Tab;
import uk.gov.hmcts.sptribs.ciccase.model.UserRole;
import uk.gov.hmcts.sptribs.wpaf1.model.StWpaf1CaseData;
import uk.gov.hmcts.sptribs.wpaf1.model.StWpaf1State;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import static org.codehaus.plexus.util.ReflectionUtils.getValueIncludingSuperclasses;

/**
 * Helpers for the WPAF1 case-type tests.
 *
 * <p>{@link #ccdFieldIds()} derives the CCD field ids from the model the same way the SDK
 * does — {@code @JsonUnwrapped(prefix)} concatenated with the nested {@code @JsonProperty} —
 * so tests can check the string-addressed tab and search layouts against reality. The
 * compiler cannot: those layouts address fields by id, not by getter.
 */
public final class StWpaf1TestUtil {

    private StWpaf1TestUtil() {
    }

    public static ConfigBuilderImpl<StWpaf1CaseData, StWpaf1State, UserRole> configBuilder() {
        return new ConfigBuilderImpl<>(new ResolvedCCDConfig<>(
            StWpaf1CaseData.class,
            StWpaf1State.class,
            UserRole.class,
            new HashMap<>(),
            ImmutableSet.copyOf(StWpaf1State.class.getEnumConstants())));
    }

    /** Every CCD field id the case data produces, in declaration order. */
    public static Set<String> ccdFieldIds() {
        final Set<String> ids = new LinkedHashSet<>();
        for (Field field : StWpaf1CaseData.class.getDeclaredFields()) {
            final JsonUnwrapped unwrapped = field.getAnnotation(JsonUnwrapped.class);
            if (unwrapped != null) {
                for (Field nested : field.getType().getDeclaredFields()) {
                    final JsonProperty property = nested.getAnnotation(JsonProperty.class);
                    if (property != null) {
                        ids.add(unwrapped.prefix() + property.value());
                    }
                }
                continue;
            }
            final JsonProperty property = field.getAnnotation(JsonProperty.class);
            if (property != null) {
                ids.add(property.value());
            }
        }
        return ids;
    }

    /**
     * The {@code @JsonUnwrapped} prefixes, in declaration order, including duplicates.
     */
    public static List<String> unwrappedPrefixes() {
        final List<String> prefixes = new ArrayList<>();
        for (Field field : StWpaf1CaseData.class.getDeclaredFields()) {
            final JsonUnwrapped unwrapped = field.getAnnotation(JsonUnwrapped.class);
            if (unwrapped != null) {
                prefixes.add(unwrapped.prefix());
            }
        }
        return prefixes;
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    public static List<Tab<StWpaf1CaseData, UserRole>> tabsFrom(
        final ConfigBuilderImpl<StWpaf1CaseData, StWpaf1State, UserRole> builder) throws IllegalAccessException {

        final List<Tab.TabBuilder> tabBuilders =
            (List<Tab.TabBuilder>) getValueIncludingSuperclasses("tabs", builder);
        final List<Tab<StWpaf1CaseData, UserRole>> tabs = new ArrayList<>();
        for (Tab.TabBuilder tabBuilder : tabBuilders) {
            tabs.add(tabBuilder.build());
        }
        return tabs;
    }
}
