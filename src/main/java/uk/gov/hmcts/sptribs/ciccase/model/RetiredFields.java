package uk.gov.hmcts.sptribs.ciccase.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.elasticsearch.common.TriConsumer;
import uk.gov.hmcts.ccd.sdk.api.CCD;
import uk.gov.hmcts.ccd.sdk.type.DynamicList;
import uk.gov.hmcts.sptribs.caseworker.model.HearingCancellationReason;
import uk.gov.hmcts.sptribs.caseworker.model.PostponeReason;
import uk.gov.hmcts.sptribs.ciccase.model.access.CaseworkerWithCAAAccess;
import uk.gov.hmcts.sptribs.ciccase.model.access.DefaultAccess;

import java.util.Map;

import static uk.gov.hmcts.ccd.sdk.type.FieldType.FixedRadioList;
import static uk.gov.hmcts.ccd.sdk.type.FieldType.TextArea;

@Data
@NoArgsConstructor
public class RetiredFields {
    @CCD(label = "Case data version")
    private int dataVersion;

    @CCD(
            label = "Enter any other important information about this cancellation",
            typeOverride = TextArea
    )
    private String cancelHearingAdditionalDetail;

    @CCD(
            label = "Why was the hearing cancelled?",
            access = {DefaultAccess.class, CaseworkerWithCAAAccess.class},
            typeOverride = FixedRadioList,
            typeParameterOverride = "HearingCancellationReason"
    )
    private HearingCancellationReason hearingCancellationReason;


    @CCD(
            label = "Template",
            access = {DefaultAccess.class, CaseworkerWithCAAAccess.class}
    )
    private DynamicList orderDynamicList;

    @CCD(
            label = "Enter any other important information about this postponement",
            access = {DefaultAccess.class, CaseworkerWithCAAAccess.class},
            typeOverride = TextArea
    )
    private String postponeAdditionalInformation;

    @CCD(
            label = "Postpone Reason",
            typeOverride = FixedRadioList,
            typeParameterOverride = "PostponeReason",
            access = {DefaultAccess.class, CaseworkerWithCAAAccess.class}
    )
    private PostponeReason postponeReason;


    @JsonIgnore
    private static final TriConsumer<Map<String, Object>, String, Object> DO_NOTHING = (data, key, val) -> {
    };

    @JsonIgnore
    private static final Map<String, TriConsumer<Map<String, Object>, String, Object>> migrations = Map.of(
            "orderDynamicList", moveTo("flagDynamicList")
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
