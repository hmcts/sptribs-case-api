package uk.gov.hmcts.sptribs.caseworker.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import uk.gov.hmcts.ccd.sdk.api.CCD;
import uk.gov.hmcts.ccd.sdk.type.Flags;
import uk.gov.hmcts.ccd.sdk.type.ListValue;
import uk.gov.hmcts.sptribs.ciccase.model.access.CaseworkerWithCAAAccess;
import uk.gov.hmcts.sptribs.ciccase.model.access.DefaultAccess;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

import static uk.gov.hmcts.ccd.sdk.type.FieldType.TextArea;

@Data
@NoArgsConstructor
@AllArgsConstructor
@JsonNaming(PropertyNamingStrategies.UpperCamelCaseStrategy.class)
@Builder
@JsonIgnoreProperties(ignoreUnknown = true)
public class CaseFlag {
    @CCD(
        label = "Explain why you are creating this flag.\n"
            + "Do not include any sensitive information such as personal details.",
        regex = "^.{0,200}$",
        hint = "You can enter up to 200 characters",
        typeOverride = TextArea
    )
    private String additionalDetail;

    @CCD(
        access = {DefaultAccess.class, CaseworkerWithCAAAccess.class},
        label = "Enter a flag type"
    )
    private String otherDescription;


    @CCD(
        label = "Why is a stay being added to this case?",
        access = {DefaultAccess.class, CaseworkerWithCAAAccess.class}
    )
    private FlagLevel flagLevel;


    @CCD(
        label = "Why is a stay being added to this case?",
        access = {DefaultAccess.class, CaseworkerWithCAAAccess.class}
    )
    private FlagParty partyLevel;

    //TODO:Flag types will be in CCD config generator after ref data team is ready
    @CCD(
        access = {DefaultAccess.class, CaseworkerWithCAAAccess.class}
    )
    private FlagType flagType;

    @CCD(
        access = {DefaultAccess.class, CaseworkerWithCAAAccess.class}
    )
    private List<ListValue<Flags>> caseFlags;

    @CCD(
        access = {DefaultAccess.class, CaseworkerWithCAAAccess.class}
    )
    private List<ListValue<CaseFlagDisplay>> partyLevelFlags;

    @CCD(
        access = {DefaultAccess.class, CaseworkerWithCAAAccess.class}
    )
    private List<ListValue<CaseFlagDisplay>> caseLevelFlags;

    public List<ListValue<CaseFlagDisplay>> getPartyLevelFlags() {
        List<ListValue<CaseFlagDisplay>> responseList = new ArrayList<>();
        if (this.caseFlags != null) {
            for (ListValue<Flags> flag : this.caseFlags) {
                if (null != flag.getValue().getPartyName() && !flag.getValue().getPartyName().isEmpty()) {
                    responseList = addToList(flag, responseList);
                }
            }
        }
        return responseList.stream().sorted(
            Comparator.comparing(o -> o.getValue().getName())
        ).collect(Collectors.toList());
    }

    public List<ListValue<CaseFlagDisplay>> getCaseLevelFlags() {
        List<ListValue<CaseFlagDisplay>> responseList = new ArrayList<>();
        if (this.caseFlags != null) {
            for (ListValue<Flags> flag : this.caseFlags) {
                if (null == flag.getValue().getPartyName() || flag.getValue().getPartyName().isEmpty()) {
                    responseList = addToList(flag, responseList);
                }
            }
        }
        return responseList.stream().sorted(
            Comparator.comparing(o -> o.getValue().getName())
        ).collect(Collectors.toList());
    }

    private List<ListValue<CaseFlagDisplay>> addToList(ListValue<Flags> flag, List<ListValue<CaseFlagDisplay>> responseList) {

        CaseFlagDisplay display = new CaseFlagDisplay(flag.getValue().getPartyName(),
            flag.getValue().getDetails().get(0).getValue().getName(),
            flag.getValue().getDetails().get(0).getValue().getFlagComment(),
            flag.getValue().getDetails().get(0).getValue().getDateTimeCreated(),
            flag.getValue().getDetails().get(0).getValue().getDateTimeModified(),
            flag.getValue().getDetails().get(0).getValue().getStatus()
        );

        if (responseList.isEmpty()) {
            ListValue<CaseFlagDisplay> listValue = ListValue
                .<CaseFlagDisplay>builder()
                .id("1")
                .value(display)
                .build();
            List<ListValue<CaseFlagDisplay>> listValues = new ArrayList<>();
            listValues.add(listValue);

            responseList = listValues;
        } else {
            AtomicInteger listValueIndex = new AtomicInteger(0);
            ListValue<CaseFlagDisplay> listValue = ListValue
                .<CaseFlagDisplay>builder()
                .id(String.valueOf(listValueIndex.incrementAndGet()))
                .value(display)
                .build();
            responseList.add(0, listValue);
            responseList.forEach(item -> item.setId(String.valueOf(listValueIndex.incrementAndGet())));
        }
        return responseList;
    }
}
