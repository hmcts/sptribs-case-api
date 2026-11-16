package uk.gov.hmcts.sptribs.document.bundling;

public class AudioVideoEvidenceBundleException extends RuntimeException {

    public AudioVideoEvidenceBundleException(long caseId, Throwable cause) {
        super("Unable to create audio/video evidence document for case " + caseId, cause);
    }
}
