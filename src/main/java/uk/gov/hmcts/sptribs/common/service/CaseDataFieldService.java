package uk.gov.hmcts.sptribs.common.service;

import com.fasterxml.jackson.annotation.JsonUnwrapped;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.sptribs.ciccase.model.CaseData;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@Service
public class CaseDataFieldService {

    private final Map<String, Field> prefixToFieldMap;

    public CaseDataFieldService() {
        this.prefixToFieldMap = buildPrefixToFieldMap();
    }

    /**
     * Builds a map of JSON prefixes to their corresponding fields in CaseData by scanning all fields with a @JsonUnwrapped annotation.
     *
     * @return map of prefix to Field
     */
    private Map<String, Field> buildPrefixToFieldMap() {
        Map<String, Field> map = new HashMap<>();

        for (Field field : CaseData.class.getDeclaredFields()) {
            JsonUnwrapped jsonUnwrapped = field.getAnnotation(JsonUnwrapped.class);
            if (jsonUnwrapped != null) {
                String prefix = jsonUnwrapped.prefix();
                if (prefix != null && !prefix.isEmpty()) {
                    map.put(prefix, field);
                    log.debug("Registered prefix '{}' for field '{}'", prefix, field.getName());
                }
            }
        }

        log.info("Built prefix map with {} entries: {}", map.size(), map.keySet());
        return map;
    }

    /**
     * Checks if a field exists in CaseData (either directly or as a nested field).
     *
     * @param fieldName the field name to check
     * @param caseData  the case data object
     * @return true if the field exists, false otherwise
     */
    public boolean fieldExists(String fieldName, CaseData caseData) {
        Field field = findField(CaseData.class, fieldName);
        if (field != null) {
            return true;
        }

        return findNestedField(fieldName, caseData) != null;
    }

    public boolean deleteField(String fieldName, CaseData caseData) throws IllegalAccessException {
        Field field = findField(CaseData.class, fieldName);
        if (field != null) {
            field.setAccessible(true);
            field.set(caseData, null);
            return true;
        }

        return deleteNestedField(fieldName, caseData);
    }

    private Field findField(Class<?> clazz, String fieldName) {
        try {
            return clazz.getDeclaredField(fieldName);
        } catch (NoSuchFieldException e) {
            if (clazz.getSuperclass() != null) {
                return findField(clazz.getSuperclass(), fieldName);
            }
            return null;
        }
    }

    /**
     * Finds a nested field in CaseData based on JSON prefix patterns
     * E.g., "cicCaseFullName" would map to caseData.getCicCase().getFullName()
     *
     * @param fieldName the JSON field name (potentially with prefix)
     * @param caseData  the case data object
     * @return the Field object if found, null otherwise
     */
    private Field findNestedField(String fieldName, CaseData caseData) {
        for (Map.Entry<String, Field> entry : prefixToFieldMap.entrySet()) {
            String prefix = entry.getKey();
            Field nestedObjectField = entry.getValue();

            if (fieldName.startsWith(prefix) && fieldName.length() > prefix.length()) {
                String nestedFieldName = fieldName.substring(prefix.length());
                nestedFieldName = Character.toLowerCase(nestedFieldName.charAt(0)) + nestedFieldName.substring(1);

                nestedObjectField.setAccessible(true);
                try {
                    Object nestedObject = nestedObjectField.get(caseData);
                    if (nestedObject != null) {
                        Field innerField = findField(nestedObject.getClass(), nestedFieldName);
                        if (innerField != null) {
                            return innerField;
                        }
                    }
                } catch (IllegalAccessException e) {
                    log.warn("Failed to access nested object for prefix '{}': {}", prefix, e.getMessage());
                }
            }
        }
        return null;
    }

    private boolean deleteNestedField(String fieldName, CaseData caseData) throws IllegalAccessException {
        for (Map.Entry<String, Field> entry : prefixToFieldMap.entrySet()) {
            String prefix = entry.getKey();
            Field nestedObjectField = entry.getValue();

            if (fieldName.startsWith(prefix) && fieldName.length() > prefix.length()) {
                String nestedFieldName = fieldName.substring(prefix.length());
                nestedFieldName = Character.toLowerCase(nestedFieldName.charAt(0)) + nestedFieldName.substring(1);

                nestedObjectField.setAccessible(true);
                Object nestedObject = nestedObjectField.get(caseData);
                if (nestedObject != null) {
                    Field innerField = findField(nestedObject.getClass(), nestedFieldName);
                    if (innerField != null) {
                        innerField.setAccessible(true);
                        innerField.set(nestedObject, null);
                        return true;
                    }
                }
            }
        }
        return false;
    }

    public Map<String, Field> getPrefixToFieldMap() {
        return Map.copyOf(prefixToFieldMap);
    }
}
