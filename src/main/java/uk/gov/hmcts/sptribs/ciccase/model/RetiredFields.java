package uk.gov.hmcts.sptribs.ciccase.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.elasticsearch.common.TriConsumer;
import uk.gov.hmcts.ccd.sdk.api.CCD;
import uk.gov.hmcts.ccd.sdk.type.CaseLink;
import uk.gov.hmcts.ccd.sdk.type.Document;
import uk.gov.hmcts.ccd.sdk.type.FlagType;
import uk.gov.hmcts.ccd.sdk.type.Flags;
import uk.gov.hmcts.ccd.sdk.type.ListValue;
import uk.gov.hmcts.sptribs.caseworker.model.CaseLinks;
import uk.gov.hmcts.sptribs.caseworker.model.FlagLevel;
import uk.gov.hmcts.sptribs.caseworker.model.HearingCancellationReason;
import uk.gov.hmcts.sptribs.caseworker.model.LinkCaseReason;
import uk.gov.hmcts.sptribs.caseworker.model.PostponeReason;
import uk.gov.hmcts.sptribs.ciccase.model.access.CaseworkerWithCAAAccess;
import uk.gov.hmcts.sptribs.ciccase.model.access.DefaultAccess;
import uk.gov.hmcts.sptribs.document.bundling.model.Bundle;
import uk.gov.hmcts.sptribs.document.model.CaseworkerCICDocument;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;

import static java.time.format.DateTimeFormatter.ofPattern;
import static java.util.Locale.UK;
import static uk.gov.hmcts.ccd.sdk.type.FieldType.Collection;
import static uk.gov.hmcts.ccd.sdk.type.FieldType.FixedRadioList;
import static uk.gov.hmcts.ccd.sdk.type.FieldType.TextArea;

@Data
@NoArgsConstructor
public class RetiredFields {

    @CCD(
        label = "Case data version",
        access = {DefaultAccess.class, CaseworkerWithCAAAccess.class}
    )
    private int dataVersion;

    @CCD(
        access = {DefaultAccess.class, CaseworkerWithCAAAccess.class}
    )
    @JsonIgnore
    private Document cicCaseLastSelectedOrder;

    @CCD(access = {DefaultAccess.class, CaseworkerWithCAAAccess.class})
    private List<ListValue<Bundle>> cicBundles;

    @CCD(
        label = "Enter any other important information about this cancellation",
        access = {DefaultAccess.class, CaseworkerWithCAAAccess.class},
        typeOverride = TextArea
    )
    private String cicCaseCancelHearingAdditionalDetail;

    @CCD(
        label = "Why was the hearing cancelled?",
        access = {DefaultAccess.class, CaseworkerWithCAAAccess.class},
        typeOverride = FixedRadioList,
        typeParameterOverride = "HearingCancellationReason"
    )
    private HearingCancellationReason cicCaseHearingCancellationReason;

    @CCD(
        label = "Postpone Reason",
        typeOverride = FixedRadioList,
        typeParameterOverride = "PostponeReason",
        access = {DefaultAccess.class, CaseworkerWithCAAAccess.class}
    )
    private PostponeReason cicCasePostponeReason;

    @CCD(
        label = "Enter any other important information about this postponement",
        access = {DefaultAccess.class, CaseworkerWithCAAAccess.class},
        typeOverride = TextArea
    )
    private String cicCasePostponeAdditionalInformation;

    @CCD(access = {DefaultAccess.class, CaseworkerWithCAAAccess.class},
        label = "Retired field for Appellant flags",
        typeOverride = Collection,
        typeParameterOverride = "Flags")
    private List<ListValue<Flags>> cicCaseAppellantFlags;

    @CCD(access = {DefaultAccess.class, CaseworkerWithCAAAccess.class},
        label = "Retired field for Case flags",
        typeOverride = Collection,
        typeParameterOverride = "Flags")
    private List<ListValue<Flags>> cicCaseCaseFlags;

    @CCD(access = {DefaultAccess.class, CaseworkerWithCAAAccess.class},
        label = "Retired field for Respondent flags",
        typeOverride = Collection,
        typeParameterOverride = "Flags")
    private List<ListValue<Flags>> cicCaseRespondentFlags;

    @CCD(
        label = "Retired field for Flag Type",
        access = {DefaultAccess.class, CaseworkerWithCAAAccess.class}
    )
    private FlagType cicCaseFlagType;

    @CCD(
        label = "Retired field for flagAdditionalDetail",
        regex = "^.{0,200}$",
        hint = "You can enter up to 200 characters",
        typeOverride = TextArea,
        access = {DefaultAccess.class, CaseworkerWithCAAAccess.class}
    )
    private String cicCaseFlagAdditionalDetail;

    @CCD(
        access = {DefaultAccess.class, CaseworkerWithCAAAccess.class},
        label = "Retired field for flag type"
    )
    private String cicCaseFlagOtherDescription;

    @CCD(
        label = "Retired field for flagLevel",
        access = {DefaultAccess.class, CaseworkerWithCAAAccess.class}
    )
    private FlagLevel cicCaseFlagLevel;

    @CCD(
        label = "Retired field for link case number",
        access = {DefaultAccess.class, CaseworkerWithCAAAccess.class}
    )
    private CaseLink cicCaseLinkCaseNumber;

    @CCD(
        label = "Retired field for linkCaseReason",
        access = {DefaultAccess.class, CaseworkerWithCAAAccess.class}
    )
    private LinkCaseReason cicCaseLinkCaseReason;

    @CCD(
        label = "Retired field for Other Description",
        access = {DefaultAccess.class, CaseworkerWithCAAAccess.class}
    )
    private String cicCaseLinkCaseOtherDescription;

    @CCD(access = {DefaultAccess.class, CaseworkerWithCAAAccess.class},
        label = "Retired field for CaseLinks",
        typeOverride = Collection,
        typeParameterOverride = "CaseLinks")
    private List<ListValue<CaseLinks>> cicCaseCaseLinks;

    @CCD(
        access = {DefaultAccess.class, CaseworkerWithCAAAccess.class},
        label = "Retired field for firstDueDate"
    )
    private String cicCaseFirstDueDate;

    @CCD(
        label = "Case Status",
        typeOverride = FixedRadioList,
        typeParameterOverride = "State",
        access = {DefaultAccess.class, CaseworkerWithCAAAccess.class}
    )
    private State cicCaseTestState;

    @CCD(
        label = "Retired field for cicCaseSelectedDocument",
        typeParameterOverride = "CaseworkerCICDocument",
        access = {DefaultAccess.class}
    )
    private CaseworkerCICDocument cicCaseSelectedDocument = new CaseworkerCICDocument();

    @JsonIgnore
    private static final TriConsumer<Map<String, Object>, String, Object> DO_NOTHING = (data, key, val) -> {
    };

    @JsonIgnore
    private static final Map<String, TriConsumer<Map<String, Object>, String, Object>> migrations = Map.of(
        "cicBundles", moveTo("caseBundles"),
        "cicCaseFirstDueDate", (caseData, key, value) -> {
            try {
                if (!value.toString().isEmpty()) {
                    DateTimeFormatter dateFormatter = ofPattern("dd MMM yyyy", UK);
                    LocalDate newValue = dateFormatter.parse(value.toString(), LocalDate::from);
                    caseData.put("cicCaseFirstOrderDueDate", newValue);
                }
            } catch (Exception e) {
                System.err.println("Could not migrate case " + caseData.get("hyphenatedCaseRef") + ":" + e);
            }
        }
    );

    /**
     * This function will iterate over the properties in the given map and check for a migration. If one is found
     * it will be executed and the map will be mutated by the migration function (usually to migrate data from
     * the old field to the new one).
     * <br>
     * Note that the migrated field will be nulled after the migration has run. Therefore, it is not possible to
     * use a migration to modify data in a field, only modify it while moving it to another.
     */
    public static Map<String, Object> migrate(Map<String, Object> data) {

        for (String key : migrations.keySet()) {
            if (data.containsKey(key) && null != data.get(key)) {
                migrations.get(key).apply(data, key, data.get(key));
                data.put(key, null);
            }
        }

        data.put("dataVersion", getVersion());

        return data;
    }

    public static int getVersion() {
        return migrations.size();
    }

    private static TriConsumer<Map<String, Object>, String, Object> moveTo(String newFieldName) {
        return (data, key, val) -> data.put(newFieldName, val);
    }
}
