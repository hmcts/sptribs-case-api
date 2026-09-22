package uk.gov.hmcts.sptribs.document.bundling;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ManageCaseDocumentUrlBuilderTest {

    @Test
    void shouldBuildLegacyDmStoreUrl() {
        ManageCaseDocumentUrlBuilder builder = new ManageCaseDocumentUrlBuilder(
            "https://manage-case.demo.platform.hmcts.net"
        );

        String result = builder.buildPublicBinaryUrl(
            "http://dm-store/documents/11111111-1111-1111-1111-111111111111/binary"
        );

        assertThat(result)
            .isEqualTo("https://manage-case.demo.platform.hmcts.net/documents/11111111-1111-1111-1111-111111111111/binary");
    }

    @Test
    void shouldBuildCdamV2Url() {
        ManageCaseDocumentUrlBuilder builder = new ManageCaseDocumentUrlBuilder(
            "https://manage-case.demo.platform.hmcts.net"
        );

        String result = builder.buildPublicBinaryUrl(
            "http://ccd-case-document-am-api/cases/documents/22222222-2222-2222-2222-222222222222/binary"
        );

        assertThat(result)
            .isEqualTo("https://manage-case.demo.platform.hmcts.net/documentsv2/22222222-2222-2222-2222-222222222222/binary");
    }

    @Test
    void shouldSupportConfiguredBaseUrlWithAndWithoutTrailingSlash() {
        ManageCaseDocumentUrlBuilder noSlashBuilder = new ManageCaseDocumentUrlBuilder(
            "https://manage-case.demo.platform.hmcts.net"
        );
        ManageCaseDocumentUrlBuilder slashBuilder = new ManageCaseDocumentUrlBuilder(
            "https://manage-case.demo.platform.hmcts.net/"
        );

        String source = "http://dm-store/documents/33333333-3333-3333-3333-333333333333/binary";
        String expected = "https://manage-case.demo.platform.hmcts.net/documents/33333333-3333-3333-3333-333333333333/binary";

        assertThat(noSlashBuilder.buildPublicBinaryUrl(source)).isEqualTo(expected);
        assertThat(slashBuilder.buildPublicBinaryUrl(source)).isEqualTo(expected);
    }

    @Test
    void shouldDiscardSourceQueryAndFragment() {
        ManageCaseDocumentUrlBuilder builder = new ManageCaseDocumentUrlBuilder(
            "https://manage-case.demo.platform.hmcts.net"
        );

        String result = builder.buildPublicBinaryUrl(
            "http://dm-store/documents/44444444-4444-4444-4444-444444444444/binary?token=abc#top"
        );

        assertThat(result)
            .isEqualTo("https://manage-case.demo.platform.hmcts.net/documents/44444444-4444-4444-4444-444444444444/binary");
    }

    @Test
    void shouldNeverRetainSourceHostAndShouldAllowAnySourceHost() {
        ManageCaseDocumentUrlBuilder builder = new ManageCaseDocumentUrlBuilder(
            "https://manage-case.demo.platform.hmcts.net"
        );

        String result = builder.buildPublicBinaryUrl(
            "https://malicious.example/documents/55555555-5555-5555-5555-555555555555/binary"
        );

        assertThat(result)
            .isEqualTo("https://manage-case.demo.platform.hmcts.net/documents/55555555-5555-5555-5555-555555555555/binary")
            .doesNotContain("malicious.example");
    }

    @Test
    void shouldRejectBlankUrl() {
        ManageCaseDocumentUrlBuilder builder = new ManageCaseDocumentUrlBuilder(
            "https://manage-case.demo.platform.hmcts.net"
        );

        assertThatThrownBy(() -> builder.buildPublicBinaryUrl("  "))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessage("Document binary URL must not be blank");
    }

    @Test
    void shouldRejectInvalidUri() {
        ManageCaseDocumentUrlBuilder builder = new ManageCaseDocumentUrlBuilder(
            "https://manage-case.demo.platform.hmcts.net"
        );

        assertThatThrownBy(() -> builder.buildPublicBinaryUrl("http://bad host/documents/11111111-1111-1111-1111-111111111111/binary"))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("Invalid stored document binary URL");
    }

    @Test
    void shouldRejectInvalidUuid() {
        ManageCaseDocumentUrlBuilder builder = new ManageCaseDocumentUrlBuilder(
            "https://manage-case.demo.platform.hmcts.net"
        );

        assertThatThrownBy(() -> builder.buildPublicBinaryUrl("http://dm-store/documents/not-a-uuid/binary"))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessage("Invalid document identifier");
    }

    @Test
    void shouldRejectUnsupportedPath() {
        ManageCaseDocumentUrlBuilder builder = new ManageCaseDocumentUrlBuilder(
            "https://manage-case.demo.platform.hmcts.net"
        );

        assertThatThrownBy(() -> builder.buildPublicBinaryUrl("http://dm-store/other/11111111-1111-1111-1111-111111111111/binary"))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessage("Unsupported document binary URL format");
    }

    @Test
    void shouldRejectMissingBinaryPathSegment() {
        ManageCaseDocumentUrlBuilder builder = new ManageCaseDocumentUrlBuilder(
            "https://manage-case.demo.platform.hmcts.net"
        );

        assertThatThrownBy(() -> builder.buildPublicBinaryUrl("http://dm-store/documents/11111111-1111-1111-1111-111111111111"))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessage("Unsupported document binary URL format");
    }

    @Test
    void shouldUseConfiguredHttpsBase() {
        ManageCaseDocumentUrlBuilder builder = new ManageCaseDocumentUrlBuilder(
            "https://manage-case.aat.platform.hmcts.net"
        );

        String result = builder.buildPublicBinaryUrl(
            "http://dm-store/documents/66666666-6666-6666-6666-666666666666/binary"
        );

        assertThat(result).startsWith("https://manage-case.aat.platform.hmcts.net/");
    }

    @Test
    void shouldRejectRelativeCaseApiBaseUrl() {
        assertThatThrownBy(() -> new ManageCaseDocumentUrlBuilder("/manage-case"))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessage("case-api.url must be an absolute HTTP(S) URL");
    }

    @Test
    void shouldRejectNonHttpCaseApiBaseUrl() {
        assertThatThrownBy(() -> new ManageCaseDocumentUrlBuilder("ftp://manage-case.demo.platform.hmcts.net"))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessage("case-api.url must be an absolute HTTP(S) URL");
    }
}
