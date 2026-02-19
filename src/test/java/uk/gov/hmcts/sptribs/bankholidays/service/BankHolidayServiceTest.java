package uk.gov.hmcts.sptribs.bankholidays.service;

import feign.Feign;
import feign.codec.Decoder;
import feign.codec.Encoder;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.sptribs.bankholidays.model.BankHolidayEvent;
import uk.gov.hmcts.sptribs.bankholidays.model.BankHolidayResponse;

import java.time.LocalDate;
import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class BankHolidayServiceTest {

    @Mock
    private Decoder feignDecoder;

    @Mock
    private Encoder feignEncoder;

    @Mock
    private BankHolidaysApi bankHolidaysApi;

    private BankHolidayService bankHolidayService;

    private final String testUrl = "https://www.gov.uk/bank-holidays/scotland.json";

    @BeforeEach
    void setUp() {
        bankHolidayService = new BankHolidayService(feignDecoder, feignEncoder);
    }

    @Test
    void shouldReturnBankHolidaysFromApi() {
        BankHolidayResponse mockResponse = createMockBankHolidayResponse();

        try (MockedStatic<Feign> feignMock = mockStatic(Feign.class)) {
            Feign.Builder mockBuilder = mock(Feign.Builder.class);
            feignMock.when(Feign::builder).thenReturn(mockBuilder);

            when(mockBuilder.decoder(any())).thenReturn(mockBuilder);
            when(mockBuilder.encoder(any())).thenReturn(mockBuilder);
            when(mockBuilder.target(BankHolidaysApi.class, testUrl)).thenReturn(bankHolidaysApi);
            when(bankHolidaysApi.retrieveAll()).thenReturn(mockResponse);

            BankHolidayResponse result = bankHolidayService.getScottishBankHolidays(testUrl);

            assertThat(result).isNotNull();
            assertThat(result.getDivision()).isEqualTo("scotland");
            assertThat(result.getEvents()).hasSize(3);
            verify(bankHolidaysApi, times(1)).retrieveAll();
        }
    }

    @Test
    void shouldReturnDatesSetFromBankHolidayResponse() {
        BankHolidayResponse mockResponse = createMockBankHolidayResponse();

        try (MockedStatic<Feign> feignMock = mockStatic(Feign.class)) {
            Feign.Builder mockBuilder = mock(Feign.Builder.class);
            feignMock.when(Feign::builder).thenReturn(mockBuilder);

            when(mockBuilder.decoder(any())).thenReturn(mockBuilder);
            when(mockBuilder.encoder(any())).thenReturn(mockBuilder);
            when(mockBuilder.target(BankHolidaysApi.class, testUrl)).thenReturn(bankHolidaysApi);
            when(bankHolidaysApi.retrieveAll()).thenReturn(mockResponse);

            BankHolidayResponse result = bankHolidayService.getScottishBankHolidays(testUrl);
            Set<LocalDate> dates = result.getDates();

            assertThat(dates).hasSize(3);
            assertThat(dates).contains(
                LocalDate.of(2026, 1, 1),
                LocalDate.of(2026, 1, 2),
                LocalDate.of(2026, 4, 3)
            );
        }
    }

    @Test
    void shouldBuildFeignClientWithCorrectUrl() {
        BankHolidayResponse mockResponse = createMockBankHolidayResponse();
        String customUrl = "https://custom.url/bank-holidays.json";

        try (MockedStatic<Feign> feignMock = mockStatic(Feign.class)) {
            Feign.Builder mockBuilder = mock(Feign.Builder.class);
            feignMock.when(Feign::builder).thenReturn(mockBuilder);

            when(mockBuilder.decoder(any())).thenReturn(mockBuilder);
            when(mockBuilder.encoder(any())).thenReturn(mockBuilder);
            when(mockBuilder.target(BankHolidaysApi.class, customUrl)).thenReturn(bankHolidaysApi);
            when(bankHolidaysApi.retrieveAll()).thenReturn(mockResponse);

            bankHolidayService.getScottishBankHolidays(customUrl);

            verify(mockBuilder).target(BankHolidaysApi.class, customUrl);
        }
    }

    @Test
    void shouldUseProvidedDecoderAndEncoder() {
        BankHolidayResponse mockResponse = createMockBankHolidayResponse();

        try (MockedStatic<Feign> feignMock = mockStatic(Feign.class)) {
            Feign.Builder mockBuilder = mock(Feign.Builder.class);
            feignMock.when(Feign::builder).thenReturn(mockBuilder);

            when(mockBuilder.decoder(feignDecoder)).thenReturn(mockBuilder);
            when(mockBuilder.encoder(feignEncoder)).thenReturn(mockBuilder);
            when(mockBuilder.target(BankHolidaysApi.class, testUrl)).thenReturn(bankHolidaysApi);
            when(bankHolidaysApi.retrieveAll()).thenReturn(mockResponse);

            bankHolidayService.getScottishBankHolidays(testUrl);

            verify(mockBuilder).decoder(feignDecoder);
            verify(mockBuilder).encoder(feignEncoder);
        }
    }

    private BankHolidayResponse createMockBankHolidayResponse() {
        BankHolidayEvent event1 = new BankHolidayEvent();
        event1.setTitle("New Year's Day");
        event1.setDate(LocalDate.of(2026, 1, 1));

        BankHolidayEvent event2 = new BankHolidayEvent();
        event2.setTitle("2nd January");
        event2.setDate(LocalDate.of(2026, 1, 2));

        BankHolidayEvent event3 = new BankHolidayEvent();
        event3.setTitle("Good Friday");
        event3.setDate(LocalDate.of(2026, 4, 3));

        BankHolidayResponse response = new BankHolidayResponse();
        response.setDivision("scotland");
        response.setEvents(List.of(event1, event2, event3));

        return response;
    }
}