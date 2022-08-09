package uk.gov.hmcts.divorce.ciccase.model;

import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import uk.gov.hmcts.ccd.sdk.api.CCD;
import uk.gov.hmcts.divorce.ciccase.model.access.CaseworkerWithCAAAccess;
import uk.gov.hmcts.divorce.ciccase.model.access.DefaultAccess;

import static uk.gov.hmcts.ccd.sdk.type.FieldType.MultiSelectList;

@Data
@AllArgsConstructor
@NoArgsConstructor
@JsonNaming(PropertyNamingStrategies.UpperCamelCaseStrategy.class)
@Builder
public class ContactPreferencesDetailsForApplicationCIC {

    @CCD(
            //label = "Add a Subject",
            typeOverride = MultiSelectList,
            typeParameterOverride = "SubjectCIC",
            access = {DefaultAccess.class, CaseworkerWithCAAAccess.class}
     )
        private SubjectCIC subjectCIC;

    @CCD(
            //label = "Add an Applicant",
            typeOverride = MultiSelectList,
            typeParameterOverride = "ApplicantCIC",
            access = {DefaultAccess.class, CaseworkerWithCAAAccess.class}

     )
        private ApplicantCIC applicantCIC;

    @CCD(
            typeOverride = MultiSelectList,
            typeParameterOverride = "RepresentativeCIC",
            access = {DefaultAccess.class, CaseworkerWithCAAAccess.class}
     )
        private RepresentativeCIC representativeCic;

}


