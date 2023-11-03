package uk.gov.hmcts.sptribs.ciccase.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.elasticsearch.common.TriConsumer;
import uk.gov.hmcts.ccd.sdk.api.CCD;
import uk.gov.hmcts.ccd.sdk.type.ListValue;
import uk.gov.hmcts.sptribs.ciccase.model.access.CaseworkerWithCAAAccess;
import uk.gov.hmcts.sptribs.ciccase.model.access.DefaultAccess;
import uk.gov.hmcts.sptribs.document.bundling.model.Bundle;

import java.util.List;
import java.util.Map;

@Data
@NoArgsConstructor
public class RetiredFields {
    @CCD(label = "Case data version")
    private int dataVersion;


    @CCD(access = {DefaultAccess.class, CaseworkerWithCAAAccess.class})
    private List<ListValue<Bundle>> cicBundles;

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
