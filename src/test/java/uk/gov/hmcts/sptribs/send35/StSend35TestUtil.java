package uk.gov.hmcts.sptribs.send35;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonUnwrapped;
import com.google.common.collect.ImmutableSet;
import uk.gov.hmcts.ccd.sdk.ConfigBuilderImpl;
import uk.gov.hmcts.ccd.sdk.ResolvedCCDConfig;
import uk.gov.hmcts.ccd.sdk.api.Tab;
import uk.gov.hmcts.sptribs.ciccase.model.UserRole;
import uk.gov.hmcts.sptribs.send35.model.StSend35CaseData;
import uk.gov.hmcts.sptribs.send35.model.StSend35State;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import static org.codehaus.plexus.util.ReflectionUtils.getValueIncludingSuperclasses;

/**
 * Helpers for the SEND35 case-type tests.
 *
 * <p>{@link #ccdFieldIds()} derives the CCD field ids from the model the same way the SDK
 * does — {@code @JsonUnwrapped(prefix)} concatenated with the nested {@code @JsonProperty} —
 * so tests can check the string-addressed tab and search layouts against reality. The
 * compiler cannot: those layouts address fields by id, not by getter.
 */
public final class StSend35TestUtil {

    private StSend35TestUtil() {
    }

    public static ConfigBuilderImpl<StSend35CaseData, StSend35State, UserRole> configBuilder() {
        return new ConfigBuilderImpl<>(new ResolvedCCDConfig<>(
            StSend35CaseData.class,
            StSend35State.class,
            UserRole.class,
            new HashMap<>(),
            ImmutableSet.copyOf(StSend35State.class.getEnumConstants())));
    }

    /** Every CCD field id the case data produces, in declaration order. */
    public static Set<String> ccdFieldIds() {
        final Set<String> ids = new LinkedHashSet<>();
        for (Field field : StSend35CaseData.class.getDeclaredFields()) {
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
        for (Field field : StSend35CaseData.class.getDeclaredFields()) {
            final JsonUnwrapped unwrapped = field.getAnnotation(JsonUnwrapped.class);
            if (unwrapped != null) {
                prefixes.add(unwrapped.prefix());
            }
        }
        return prefixes;
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    public static List<Tab<StSend35CaseData, UserRole>> tabsFrom(
        final ConfigBuilderImpl<StSend35CaseData, StSend35State, UserRole> builder) throws IllegalAccessException {

        final List<Tab.TabBuilder> tabBuilders =
            (List<Tab.TabBuilder>) getValueIncludingSuperclasses("tabs", builder);
        final List<Tab<StSend35CaseData, UserRole>> tabs = new ArrayList<>();
        for (Tab.TabBuilder tabBuilder : tabBuilders) {
            tabs.add(tabBuilder.build());
        }
        return tabs;
    }
}
