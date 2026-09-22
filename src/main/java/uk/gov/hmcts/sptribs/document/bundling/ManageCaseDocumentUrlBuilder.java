package uk.gov.hmcts.sptribs.document.bundling;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.UUID;

@Component
public class ManageCaseDocumentUrlBuilder {

    private static final String DOCUMENTS = "documents";
    private static final String CASES = "cases";
    private static final String DOCUMENTS_V2 = "documentsv2";
    private static final String BINARY = "binary";

    private final URI manageCaseBaseUri;

    public ManageCaseDocumentUrlBuilder(@Value("${case-api.url}") String manageCaseBaseUrl) {
        this.manageCaseBaseUri = parseBaseUri(manageCaseBaseUrl);
    }

    public String buildPublicBinaryUrl(String storedBinaryUrl) {
        URI sourceUri = parseStoredUri(storedBinaryUrl);
        List<String> segments = pathSegments(sourceUri.getPath());

        String publicRoute;
        String documentId;

        if (isLegacyPath(segments)) {
            publicRoute = DOCUMENTS;
            documentId = segments.get(1);
        } else if (isCdamPath(segments)) {
            publicRoute = DOCUMENTS_V2;
            documentId = segments.get(2);
        } else {
            throw new IllegalArgumentException("Unsupported document binary URL format");
        }

        validateUuid(documentId);
        return buildPath(publicRoute, documentId, BINARY);
    }

    private URI parseBaseUri(String value) {
        URI uri = parseUri(value, "case-api.url");
        String scheme = StringUtils.lowerCase(uri.getScheme(), Locale.ROOT);
        if (!uri.isAbsolute() || StringUtils.isBlank(uri.getHost()) || !Set.of("http", "https").contains(scheme)) {
            throw new IllegalArgumentException("case-api.url must be an absolute HTTP(S) URL");
        }
        return uri;
    }

    private URI parseStoredUri(String value) {
        if (StringUtils.isBlank(value)) {
            throw new IllegalArgumentException("Document binary URL must not be blank");
        }
        return parseUri(value, "stored document binary URL");
    }

    private URI parseUri(String value, String sourceName) {
        try {
            return new URI(value);
        } catch (URISyntaxException exception) {
            throw new IllegalArgumentException("Invalid " + sourceName, exception);
        }
    }

    private boolean isLegacyPath(List<String> segments) {
        return segments.size() == 3
            && DOCUMENTS.equals(segments.get(0))
            && BINARY.equals(segments.get(2));
    }

    private boolean isCdamPath(List<String> segments) {
        return segments.size() == 4
            && CASES.equals(segments.get(0))
            && DOCUMENTS.equals(segments.get(1))
            && BINARY.equals(segments.get(3));
    }

    private List<String> pathSegments(String path) {
        if (StringUtils.isBlank(path)) {
            throw new IllegalArgumentException("Unsupported document binary URL format");
        }
        return List.of(StringUtils.split(path, '/'));
    }

    private void validateUuid(String value) {
        try {
            UUID.fromString(value);
        } catch (IllegalArgumentException exception) {
            throw new IllegalArgumentException("Invalid document identifier", exception);
        }
    }

    private String buildPath(String... segments) {
        URI pathUri = URI.create("/" + String.join("/", segments));
        return manageCaseBaseUri.resolve(pathUri).toString();
    }
}
