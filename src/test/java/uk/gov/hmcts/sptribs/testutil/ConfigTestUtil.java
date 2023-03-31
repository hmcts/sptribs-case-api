package uk.gov.hmcts.sptribs.testutil;

import com.google.common.collect.ImmutableSet;
import org.junit.experimental.categories.Categories;
import uk.gov.hmcts.ccd.sdk.ConfigBuilderImpl;
import uk.gov.hmcts.ccd.sdk.ResolvedCCDConfig;
import uk.gov.hmcts.ccd.sdk.api.CaseCategory;
import uk.gov.hmcts.ccd.sdk.api.ConfigBuilder;
import uk.gov.hmcts.ccd.sdk.api.Event;
import uk.gov.hmcts.ccd.sdk.api.HasRole;
import uk.gov.hmcts.ccd.sdk.api.Search;
import uk.gov.hmcts.ccd.sdk.api.Search.SearchBuilder;
import uk.gov.hmcts.sptribs.ciccase.model.CaseData;
import uk.gov.hmcts.sptribs.ciccase.model.State;
import uk.gov.hmcts.sptribs.ciccase.model.UserRole;
import uk.gov.hmcts.sptribs.ciccase.model.casetype.CareStandardsData;
import uk.gov.hmcts.sptribs.ciccase.model.casetype.CriminalInjuriesCompensationData;
import uk.gov.hmcts.sptribs.ciccase.model.casetype.DisabilityDiscriminationData;
import uk.gov.hmcts.sptribs.ciccase.model.casetype.MentalHealthData;
import uk.gov.hmcts.sptribs.ciccase.model.casetype.PrimaryHealthListsData;
import uk.gov.hmcts.sptribs.ciccase.model.casetype.SpecialEducationalNeedsData;

import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.codehaus.plexus.util.ReflectionUtils.getValueIncludingSuperclasses;
import static org.junit.platform.commons.util.ReflectionUtils.findMethod;

public final class ConfigTestUtil {

    private ConfigTestUtil() {
    }

    public static ConfigBuilderImpl<CaseData, State, UserRole> createCaseDataConfigBuilder() {
        return new ConfigBuilderImpl<>(new ResolvedCCDConfig<>(
            CaseData.class,
            State.class,
            UserRole.class,
            new HashMap<>(),
            ImmutableSet.copyOf(State.class.getEnumConstants())));
    }

    public static ConfigBuilderImpl<CriminalInjuriesCompensationData, State, UserRole> createCICCaseDataConfigBuilder() {
        return new ConfigBuilderImpl<>(new ResolvedCCDConfig<>(
            CriminalInjuriesCompensationData.class,
            State.class,
            UserRole.class,
            new HashMap<>(),
            ImmutableSet.copyOf(State.class.getEnumConstants())));
    }

    public static ConfigBuilderImpl<CareStandardsData, State, UserRole> createCareStandardsDataConfigBuilder() {
        return new ConfigBuilderImpl<>(new ResolvedCCDConfig<>(
            CareStandardsData.class,
            State.class,
            UserRole.class,
            new HashMap<>(),
            ImmutableSet.copyOf(State.class.getEnumConstants())));
    }

    public static ConfigBuilderImpl<MentalHealthData, State, UserRole> createMentalHealthDataConfigBuilder() {
        return new ConfigBuilderImpl<>(new ResolvedCCDConfig<>(
            MentalHealthData.class,
            State.class,
            UserRole.class,
            new HashMap<>(),
            ImmutableSet.copyOf(State.class.getEnumConstants())));
    }

    public static ConfigBuilderImpl<PrimaryHealthListsData, State, UserRole> createPrimaryHealthListsDataConfigBuilder() {
        return new ConfigBuilderImpl<>(new ResolvedCCDConfig<>(
            PrimaryHealthListsData.class,
            State.class,
            UserRole.class,
            new HashMap<>(),
            ImmutableSet.copyOf(State.class.getEnumConstants())));
    }

    public static ConfigBuilderImpl<DisabilityDiscriminationData, State, UserRole> createDisabilityDiscriminationDataConfigBuilder() {
        return new ConfigBuilderImpl<>(new ResolvedCCDConfig<>(
            DisabilityDiscriminationData.class,
            State.class,
            UserRole.class,
            new HashMap<>(),
            ImmutableSet.copyOf(State.class.getEnumConstants())));
    }

    public static ConfigBuilderImpl<SpecialEducationalNeedsData, State, UserRole> createSpecialEducationalNeedsDataConfigBuilder() {
        return new ConfigBuilderImpl<>(new ResolvedCCDConfig<>(
            SpecialEducationalNeedsData.class,
            State.class,
            UserRole.class,
            new HashMap<>(),
            ImmutableSet.copyOf(State.class.getEnumConstants())));
    }

    @SuppressWarnings({"unchecked"})
    public static <T, S, R extends HasRole> Map<String, Event<T, R, S>> getEventsFrom(
        final ConfigBuilderImpl<T, S, R> configBuilder) {

        return (Map<String, Event<T, R, S>>) findMethod(ConfigBuilderImpl.class, "getEvents")
            .map(method -> {
                try {
                    method.setAccessible(true);
                    return method.invoke(configBuilder);
                } catch (IllegalAccessException | InvocationTargetException e) {
                    throw new AssertionError("Unable to invoke ConfigBuilderImpl.class method getEvents", e);
                }
            })
            .orElseThrow(() -> new AssertionError("Unable to find ConfigBuilderImpl.class method getEvents"));
    }

    public static <T, S, R extends HasRole> List<CaseCategory.CaseCategoryBuilder> getCategories(
        final ConfigBuilder<T, S, R> configBuilder) throws IllegalAccessException {
        return getCategoriesFor("categories", configBuilder);
    }

    public static <T, S, R extends HasRole> Search<T, R> getSearchResultFields(
        final ConfigBuilderImpl<T, S, R> configBuilder) throws IllegalAccessException {
        return getSearchFor("searchResultFields", configBuilder);
    }

    public static <T, S, R extends HasRole> Search<T, R> getWorkBasketInputFields(
        final ConfigBuilderImpl<T, S, R> configBuilder) throws IllegalAccessException {
        return getWorkBasketFor("workBasketInputFields", configBuilder);
    }

    public static <T, S, R extends HasRole> Search<T, R> getWorkBasketResultFields(
        final ConfigBuilderImpl<T, S, R> configBuilder) throws IllegalAccessException {
        return getWorkBasketFor("workBasketResultFields", configBuilder);
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    private static <T, S, R extends HasRole> Search<T, R> getSearchFor(
        final String fieldName,
        final ConfigBuilderImpl<T, S, R> configBuilder) throws IllegalAccessException {
        final List<SearchBuilder> searchInputFields =
            (List<SearchBuilder>) getValueIncludingSuperclasses(fieldName, configBuilder);
        final var searchInputBuilder = searchInputFields.get(0);
        return searchInputBuilder.build();
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    private static <T, S, R extends HasRole> Search<T, R> getWorkBasketFor(
        final String fieldName,
        final ConfigBuilderImpl<T, S, R> configBuilder) throws IllegalAccessException {

        final List<SearchBuilder> workBasketInputFields =
            (List<SearchBuilder>) getValueIncludingSuperclasses(fieldName, configBuilder);
        final var workBasketBuilder = workBasketInputFields.get(0);
        return workBasketBuilder.build();
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    private static <T, S, R extends HasRole> List<CaseCategory.CaseCategoryBuilder> getCategoriesFor(
        final String fieldName,
        final ConfigBuilder<T, S, R> configBuilder) throws IllegalAccessException {

        final List<CaseCategory.CaseCategoryBuilder> categories =
            (List<CaseCategory.CaseCategoryBuilder>) getValueIncludingSuperclasses(fieldName, configBuilder);
        //final var categoriesBuilder = categories.get(0);
        return categories;
    }
}
