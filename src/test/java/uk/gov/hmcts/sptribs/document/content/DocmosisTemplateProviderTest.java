package uk.gov.hmcts.sptribs.document.content;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.sptribs.common.config.DocmosisTemplatesConfig;

import java.util.Map;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.sptribs.ciccase.model.LanguagePreference.ENGLISH;
import static uk.gov.hmcts.sptribs.ciccase.model.LanguagePreference.WELSH;
import static uk.gov.hmcts.sptribs.document.DocumentConstants.SPTRIBS_DRAFT_APPLICATION;
import static uk.gov.hmcts.sptribs.testutil.TestConstants.ENGLISH_TEMPLATE_ID;
import static uk.gov.hmcts.sptribs.testutil.TestConstants.WELSH_TEMPLATE_ID;

@ExtendWith(MockitoExtension.class)
class DocmosisTemplateProviderTest {

    @Mock
    private DocmosisTemplatesConfig docmosisTemplatesConfig;

    @InjectMocks
    private DocmosisTemplateProvider docmosisTemplateProvider;

    @Test
    void shouldReturnTemplateForEnglish() {
        //Given
        mockDocmosisTemplateConfig();

        //When
        final String name = docmosisTemplateProvider.templateNameFor(SPTRIBS_DRAFT_APPLICATION, ENGLISH);

        //Then
        assertThat(name, is(ENGLISH_TEMPLATE_ID));
    }

    @Test
    void shouldReturnTemplateForWelsh() {
        //Given
        mockDocmosisTemplateConfig();

        //When
        final String name = docmosisTemplateProvider.templateNameFor(SPTRIBS_DRAFT_APPLICATION, WELSH);

        //Then
        assertThat(name, is(WELSH_TEMPLATE_ID));
    }

    private void mockDocmosisTemplateConfig() {
        when(docmosisTemplatesConfig.getTemplates()).thenReturn(
            Map.of(
                ENGLISH, Map.of(
                    SPTRIBS_DRAFT_APPLICATION, ENGLISH_TEMPLATE_ID
                ),
                WELSH, Map.of(
                    SPTRIBS_DRAFT_APPLICATION, WELSH_TEMPLATE_ID
                )
            )
        );
    }
}
