package model.rules;

public record ConfigurationRule(
        //inputs
        String caseData,
        String taskType,

        //outputs
        String name,
        String value,
        String canReconfigure
) {
}
