package uk.gov.hmcts.sptribs.document;

import jakarta.persistence.Embeddable;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@NoArgsConstructor
@Builder
@Data
@Embeddable
public class SerializableDocument implements Serializable {
    private String url;

    private String filename;

    private String binaryUrl;

    private String categoryId;

    public SerializableDocument(
        String url,
        String filename,
        String binaryUrl
    ) {
        this.url = url;
        this.filename = filename;
        this.binaryUrl = binaryUrl;
    }

    public SerializableDocument(
        String url,
        String filename,
        String binaryUrl,
        String categoryId
    ) {
        this.url = url;
        this.filename = filename;
        this.binaryUrl = binaryUrl;
        this.categoryId = categoryId;
    }
}
