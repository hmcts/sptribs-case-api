package uk.gov.hmcts.sptribs.controllers.model;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PostcodeRequest {

    @NotBlank(message = "Postcode cannot be blank")
    private String postcode;
}
