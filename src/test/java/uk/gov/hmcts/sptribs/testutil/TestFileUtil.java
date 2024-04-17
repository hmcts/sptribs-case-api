package uk.gov.hmcts.sptribs.testutil;

import com.fasterxml.jackson.databind.ObjectMapper;
import uk.gov.hmcts.sptribs.exception.InvalidResourceException;

import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;

import static uk.gov.hmcts.sptribs.testutil.TestConstants.TEST_RESOURCE_NOT_FOUND;

public final class TestFileUtil {
    private TestFileUtil() {
    }

    public static String loadJson(final String filePath) throws IOException {
        return new String(loadResource(filePath), StandardCharsets.UTF_8);
    }

    public static byte[] loadResource(final String filePath) {
        try {
            URL url = Thread.currentThread().getContextClassLoader().getResource(filePath);

            if (url == null) {
                throw new IllegalArgumentException(String.format(TEST_RESOURCE_NOT_FOUND + "%s", filePath));
            }
            return Files.readAllBytes(Paths.get(url.toURI()));
        } catch (IOException | URISyntaxException | IllegalArgumentException ioException) {
            throw new InvalidResourceException(TEST_RESOURCE_NOT_FOUND + filePath, ioException);
        }
    }

    public static <T> T loadJsonToObject(String filePath, Class<T> type) {
        try {
            return new ObjectMapper().readValue(loadJson(filePath), type);
        } catch (Exception e) {
            throw new InvalidResourceException(TEST_RESOURCE_NOT_FOUND + filePath, e);
        }
    }

    public static <T> String objectToJson(T object) {
        try {
            return new ObjectMapper().writeValueAsString(object);
        } catch (Exception e) {
            throw new InvalidResourceException("Could not write object to Json ", e);
        }
    }
}
