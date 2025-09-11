package uk.gov.hmcts.sptribs.document.pdf;

import org.slf4j.Logger;

import java.io.IOException;
import java.io.InputStream;
import java.util.function.Supplier;

/**
 * Wraps an InputStream as an AutoCloseable who's close method logs and swallows the exception instead of propagating it.
 */
public class InputStreamWrapper implements AutoCloseable, Supplier<InputStream> {

    private final Logger log;
    private final InputStream inputStream;

    public InputStreamWrapper(Logger log, InputStream inputStream) {
        this.log = log;
        this.inputStream = inputStream;
    }

    @Override
    public void close() {
        if (inputStream != null) {
            try {
                inputStream.close();
            } catch (IOException e) {
                log.error(e.getMessage());
            }
        }
    }

    @Override
    public InputStream get() {
        return inputStream;
    }
}
