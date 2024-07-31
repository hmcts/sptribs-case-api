package uk.gov.hmcts.sptribs.dmn.domain.entities.task;

public class Task {

    private final String template;
    private final String type;
    private final String name;
    private final String roleCategory;
    private final String description;
    private final Integer minorPriority;
    private final Integer majorPriority;
    private final String metaDataKey;
    private final String metaDataValue;

    public Task(
            String template,
            String type,
            String name,
            String roleCategory,
            String description,
            Integer minorPriority,
            Integer majorPriority,
            String metaDataKey,
            String metaDataValue
    ) {
        this.template = template;
        this.type = type;
        this.name = name;
        this.roleCategory = roleCategory;
        this.description = description;
        this.minorPriority = minorPriority;
        this.majorPriority = majorPriority;
        this.metaDataKey = metaDataKey;
        this.metaDataValue = metaDataValue;
    }
}
