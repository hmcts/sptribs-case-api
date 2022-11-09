package uk.gov.hmcts.sptribs.endpoint.model.input;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(MockitoExtension.class)
class OcrDataFieldTest {

    private final ObjectMapper mapper = new ObjectMapper();

    @Test
    void shouldOCRDataFieldJsonMapping() throws JsonProcessingException {
        //Given
        String json = "{\"name\":\"NAME\", \"value\":\"VALUE\"}";
        OcrDataField constructedOcrDataField = new OcrDataField("NAME", "VALUE");

        //When
        OcrDataField ocrDataField = mapper.readValue(json, OcrDataField.class);

        //Then
        assertThat(ocrDataField.getName()).isEqualTo(constructedOcrDataField.getName());
        assertThat(ocrDataField.getValue()).isEqualTo(constructedOcrDataField.getValue());
    }


}
