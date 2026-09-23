package uk.gov.hmcts.sptribs.common.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import uk.gov.hmcts.ccd.sdk.jackson.UnwrappedPrefixModule;
import uk.gov.hmcts.ccd.sdk.type.Document;
import uk.gov.hmcts.ccd.sdk.type.DynamicList;
import uk.gov.hmcts.ccd.sdk.type.DynamicListElement;
import uk.gov.hmcts.sptribs.ciccase.model.CaseData;
import uk.gov.hmcts.sptribs.ciccase.model.CicCase;
import uk.gov.hmcts.sptribs.ciccase.model.casetype.CriminalInjuriesCompensationData;
import uk.gov.hmcts.sptribs.common.config.jackson.JacksonConfiguration;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class JacksonCompatibilityTest {

    @Test
    void deserializesUnwrappedCaseData() throws Exception {
        ObjectMapper mapper = mapper();
        CaseData caseData = mapper.readValue(
            "{\"cicCaseSelectedDocumentType\":\"CASE\"}",
            CaseData.class
        );

        assertThat(caseData.getCicCase().getSelectedDocumentType()).isEqualTo("CASE");
    }

    @Test
    void deserializesUnwrappedSdkTypes() throws Exception {
        ObjectMapper mapper = mapper();
        DynamicListElement selected = DynamicListElement.builder()
            .code(UUID.randomUUID())
            .label("selected")
            .build();
        CaseData source = CaseData.builder()
            .cicCase(CicCase.builder()
                .orderDynamicList(DynamicList.builder()
                    .value(selected)
                    .listItems(List.of(selected))
                    .build())
                .selectedDocumentLink(Document.builder()
                    .url("http://url/")
                    .filename("test.pdf")
                    .binaryUrl("http://url/binary")
                    .build())
                .build())
            .build();

        CriminalInjuriesCompensationData caseData = mapper.readValue(
            mapper.writeValueAsString(source),
            CriminalInjuriesCompensationData.class
        );

        assertThat(caseData.getCicCase().getOrderDynamicList().getValue()).isEqualTo(selected);
        assertThat(caseData.getCicCase().getSelectedDocumentLink().getUrl()).isEqualTo("http://url/");
    }

    private static ObjectMapper mapper() {
        // The SDK registers this module on every ObjectMapper bean; this test builds the mapper outside Spring.
        return new JacksonConfiguration().getMapper().registerModule(new UnwrappedPrefixModule());
    }
}
