package uk.gov.hmcts.sptribs.ciccase.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;

public record SubjectVerificationRequest(
    @JsonProperty("subject_full_name") @NotBlank String subjectFullName,
    @JsonProperty("subject_date_of_birth") @NotNull LocalDate subjectDateOfBirth
) { }
