package uk.gov.hmcts.sptribs.ciccase.model;

import org.junit.jupiter.api.Test;
import uk.gov.hmcts.ccd.sdk.type.DynamicList;
import uk.gov.hmcts.ccd.sdk.type.DynamicListElement;
import uk.gov.hmcts.ccd.sdk.type.ListValue;
import uk.gov.hmcts.sptribs.document.model.DivorceDocument;

import java.util.HashMap;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.entry;
import static uk.gov.hmcts.sptribs.ciccase.model.ServiceMethod.PERSONAL_SERVICE;

class RetiredFieldsTest {

    @Test
    void migrateShouldMigrateSomeFieldsAndLeaveOthersAlone() {
        //Given
        final var data = new HashMap<String, Object>();
        data.put("exampleRetiredField", "This will be first name");
        data.put("applicant1FirstName", "This will be overwritten");
        data.put("applicant1LastName", "This will be left alone");

        //When
        final var result = RetiredFields.migrate(data);

        //Then
        assertThat(result).contains(
            entry("applicant1FirstName", "This will be first name"),
            entry("applicant1LastName", "This will be left alone"),
            entry("exampleRetiredField", null)
        );
    }

    @Test
    void shouldMigrateSolServiceMethodToServiceMethod() {
        //Given
        final var data = new HashMap<String, Object>();
        data.put("solServiceMethod", PERSONAL_SERVICE);

        //When
        final var result = RetiredFields.migrate(data);

        //Then
        assertThat(result).contains(
            entry("serviceMethod", PERSONAL_SERVICE),
            entry("solServiceMethod", null)
        );
    }

    @Test
    void shouldMigrateD11DocumentToAnswerReceivedDocuments() {
        //Given
        DivorceDocument d11Document = DivorceDocument.builder().build();
        final var data = new HashMap<String, Object>();
        data.put("d11Document", d11Document);

        //When
        final var result = RetiredFields.migrate(data);

        //Then
        assertThat(result).contains(
            entry("answerReceivedSupportingDocuments", List.of(ListValue
                .<DivorceDocument>builder()
                .id("1")
                .value(d11Document)
                .build()
            )),
            entry("d11Document", null)
        );
    }

    @Test
    void shouldMigrateGeneralApplicationFeeAccountNumberToPbaNumbers() {
        //Given
        String feeCode = "FEE002";
        final var data = new HashMap<String, Object>();
        data.put("generalApplicationFeeAccountNumber", feeCode);

        //When
        final var result = RetiredFields.migrate(data);

        //Then
        assertThat(result).contains(
            entry("generalApplicationFeePbaNumbers",
                DynamicList
                    .builder()
                    .value(DynamicListElement.builder().label(feeCode).build())
                    .listItems(List.of(DynamicListElement.builder().label(feeCode).build()))
                    .build()
            ),
            entry("generalApplicationFeeAccountNumber", null)
        );
    }
}
