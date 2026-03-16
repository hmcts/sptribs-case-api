package uk.gov.hmcts.sptribs.bankholidays.service;

import feign.Feign;
import feign.codec.Decoder;
import feign.codec.Encoder;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.sptribs.bankholidays.model.BankHolidayResponse;


@Service
@Slf4j
public class BankHolidayService {

    private final Decoder feignDecoder;
    private final Encoder feignEncoder;

    public BankHolidayService(Decoder feignDecoder, Encoder feignEncoder) {
        this.feignDecoder = feignDecoder;
        this.feignEncoder = feignEncoder;
    }

    @Cacheable(value = "scottish_bank_holiday_cache", key = "#uri", sync = true, cacheManager = "bankHolidayCacheManager")
    public BankHolidayResponse getScottishBankHolidays(String uri) {
        log.info("Getting Scottish bank holidays from {}", uri);
        return bankHolidaysApi(uri).retrieveAll();
    }

    private BankHolidaysApi bankHolidaysApi(String uri) {
        return Feign.builder()
            .decoder(feignDecoder)
            .encoder(feignEncoder)
            .target(BankHolidaysApi.class, uri);
    }
}