package uk.gov.hmcts.sptribs.caseworker.model;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import uk.gov.hmcts.ccd.sdk.api.CCD;
import uk.gov.hmcts.ccd.sdk.type.ListValue;
import uk.gov.hmcts.ccd.sdk.type.YesOrNo;
import uk.gov.hmcts.sptribs.ciccase.model.access.CaseworkerWithCAAAccess;
import uk.gov.hmcts.sptribs.ciccase.model.access.DefaultAccess;
import uk.gov.hmcts.sptribs.document.model.CICDocument;

import java.time.LocalDate;
import java.util.List;

import static uk.gov.hmcts.ccd.sdk.type.FieldType.Collection;


@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Order {

    @CCD(
        label = "Due Dates",
        access = {DefaultAccess.class, CaseworkerWithCAAAccess.class}

    )
    private List<ListValue<DateModel>> dueDateList;

    @CCD(
        label = "Uploaded File",
        typeOverride = Collection,
        typeParameterOverride = "CICDocument",
        access = {DefaultAccess.class, CaseworkerWithCAAAccess.class}
    )
    private List<ListValue<CICDocument>> uploadedFile;

    @CCD(
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

    @CCD(
        access = {DefaultAccess.class, CaseworkerWithCAAAccess.class}
    )
    private List<ListValue<DateModel>> orderDueDates;

    @CCD(
        label = "Date sent",
        access = {DefaultAccess.class, CaseworkerWithCAAAccess.class}
    )
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate orderSentDate;

    @CCD(
        access = {DefaultAccess.class, CaseworkerWithCAAAccess.class}
    )
    @JsonIgnore
    private YesOrNo isLastSelectedOrder;

}
