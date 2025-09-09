package uk.gov.hmcts.sptribs.services.docmosis;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.cloud.openfeign.FeignClientProperties;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.multipart.MultipartFile;

@FeignClient(name = "docmosis", url = "${docmosis.tornado.url}", configuration =
    FeignClientProperties.FeignClientConfiguration.class)
public interface DocmosisApiClient {

    @PostMapping(value = "/rs/convert", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    ResponseEntity<byte[]> convert(
        @RequestPart("accessKey") String accessKey,
        @RequestPart("outputName") String outputName,
        @RequestPart("file") MultipartFile file
    );
}
