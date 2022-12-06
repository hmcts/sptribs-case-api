package uk.gov.hmcts.sptribs.caseworker.model;

import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import uk.gov.hmcts.ccd.sdk.api.CCD;
import uk.gov.hmcts.ccd.sdk.type.ListValue;
import uk.gov.hmcts.sptribs.ciccase.model.CaseDocumentsCIC;
import uk.gov.hmcts.sptribs.ciccase.model.GetAmendDateAsCompleted;
import uk.gov.hmcts.sptribs.ciccase.model.access.CaseworkerWithCAAAccess;
import uk.gov.hmcts.sptribs.ciccase.model.access.DefaultAccess;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static uk.gov.hmcts.ccd.sdk.type.FieldType.MultiSelectList;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@JsonNaming(PropertyNamingStrategies.UpperCamelCaseStrategy.class)
public class SendOrder {

    @CCD(
        label = "How would you like to issue an order?"
    )
    private OrderIssuingType orderIssuingType;

    @CCD(
        access = {DefaultAccess.class, CaseworkerWithCAAAccess.class}
    )
    private CaseDocumentsCIC orderFile;

    @CCD(
        access = {DefaultAccess.class, CaseworkerWithCAAAccess.class},
        label = "Due Date"
    )
    private List<ListValue<DateModel>> dueDates;

    @Builder.Default
    @CCD(
        typeOverride = MultiSelectList,
        typeParameterOverride = "GetAmendDateAsCompleted",

        access = {DefaultAccess.class, CaseworkerWithCAAAccess.class}
    )
    private Set<GetAmendDateAsCompleted> markAsCompleted = new HashSet<>();



    @CCD(
        access = {DefaultAccess.class, CaseworkerWithCAAAccess.class}
    )
    private DraftOrderCIC draftOrderCIC;

    @CCD(
        label = "Should a reminder notification be sent? You can only send a reminder for the earliest due date stated on this order",
        access = {DefaultAccess.class, CaseworkerWithCAAAccess.class}
    )
    private YesNo yesOrNo;

    @CCD(
        label = "How many days before the earliest due date should a reminder be sent?",
        access = {DefaultAccess.class, CaseworkerWithCAAAccess.class}
    )
    private ReminderDays reminderDays;
}
