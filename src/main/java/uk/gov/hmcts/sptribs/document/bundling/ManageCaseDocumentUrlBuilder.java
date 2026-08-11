package uk.gov.hmcts.sptribs.document.bundling;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;
import java.util.UUID;

@Component
public class ManageCaseDocumentUrlBuilder {

    private static final String DOCUMENTS = "documents";
    private static final String CASES = "cases";
    private static final String DOCUMENTS_V2 = "documentsv2";
    private static final String BINARY = "binary";

    private final URI manageCaseBaseUri;

    public ManageCaseDocumentUrlBuilder(@Value("${case-api.url}") String manageCaseBaseUrl) {
        this.manageCaseBaseUri = parseUri(manageCaseBaseUrl, "case-api.url");
    }

    public String buildPublicBinaryUrl(String storedBinaryUrl) {
        if (StringUtils.isBlank(storedBinaryUrl)) {
            throw new IllegalArgumentException("Document binary URL must not be blank");
        }

        URI sourceUri = parseUri(storedBinaryUrl, "stored document binary URL");
        List<String> segments = pathSegments(sourceUri.getPath());
        if (segments.size() < 3) {
            throw new IllegalArgumentException("Unsupported document binary URL format");
        }

        if (!BINARY.equals(segments.getLast())) {
            throw new IllegalArgumentException("Unsupported document binary URL format");
        }

        String uuidValue = segments.get(segments.size() - 2);
        validateUuid(uuidValue);

        if (segments.size() == 3 && DOCUMENTS.equals(segments.getFirst())) {
            return buildPath(DOCUMENTS, uuidValue, BINARY);
        }

        if (segments.size() == 4
            && CASES.equals(segments.getFirst())
            && DOCUMENTS.equals(segments.get(1))) {
            return buildPath(DOCUMENTS_V2, uuidValue, BINARY);
        }

        throw new IllegalArgumentException("Unsupported document binary URL format");
    }

    private URI parseUri(String value, String sourceName) {
        try {
            URI uri = new URI(value);
            if (!uri.isAbsolute() && StringUtils.isBlank(uri.getPath())) {
                throw new IllegalArgumentException("Invalid " + sourceName);
            }
            return uri;
        } catch (URISyntaxException exception) {
            throw new IllegalArgumentException("Invalid " + sourceName, exception);
        }
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
