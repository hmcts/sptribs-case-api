package uk.gov.hmcts.sptribs.caseworker.model;

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

import java.util.List;
import java.util.Set;

import static uk.gov.hmcts.ccd.sdk.type.FieldType.MultiSelectList;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Order {

    @CCD(
        label = "Uploaded File",
        access = {DefaultAccess.class, CaseworkerWithCAAAccess.class}
    )
    private CaseDocumentsCIC uploadedFile;

    @CCD(
        access = {DefaultAccess.class, CaseworkerWithCAAAccess.class},
        label = "Due Dates"
    )
    private List<ListValue<DateModel>> dueDateList;

    @CCD(
        label = "Completed",
        typeOverride = MultiSelectList,
        typeParameterOverride = "GetAmendDateAsCompleted",
        access = {DefaultAccess.class, CaseworkerWithCAAAccess.class}
    )
    private Set<GetAmendDateAsCompleted> completed;


    @CCD(
        label = "Draft Order",
        access = {DefaultAccess.class, CaseworkerWithCAAAccess.class}
    )
    private DraftOrderCIC draftOrder;

    @CCD(
        label = "How many days before the earliest due date should a reminder be sent?",
        access = {DefaultAccess.class, CaseworkerWithCAAAccess.class}
    )
    private ReminderDays reminderDay;

    @CCD(
        label = "Recipients",
        access = {DefaultAccess.class, CaseworkerWithCAAAccess.class}
    )
    private String parties;

}
