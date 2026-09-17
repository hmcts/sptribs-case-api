package uk.gov.hmcts.sptribs.common.service;

import java.util.Map;

@FunctionalInterface
public interface PdfServiceClient {

    byte[] generateFromHtml(byte[] template, Map<String, Object> placeholders);
}
