import model.ProcessModel;
import model.FullTaskDefinition;
import model.rules.CancellationRule;
import model.rules.CompletionRule;
import model.rules.ConfigurationRule;
import model.rules.InitiationRule;
import model.rules.PermissionRule;
import model.rules.TaskTypes;
import org.camunda.bpm.model.dmn.Dmn;
import org.camunda.bpm.model.dmn.DmnModelInstance;
import org.camunda.bpm.model.dmn.instance.Decision;
import org.camunda.bpm.model.dmn.instance.DecisionTable;
import org.camunda.bpm.model.dmn.instance.DmnElement;
import org.camunda.bpm.model.dmn.instance.Input;
import org.camunda.bpm.model.dmn.instance.InputEntry;
import org.camunda.bpm.model.dmn.instance.OutputEntry;
import org.camunda.bpm.model.dmn.instance.Rule;
import org.gradle.api.tasks.TaskAction;
import org.slf4j.Logger;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.Set;
import java.util.function.BiFunction;

public class DiagramGenerator {
    private static final String CAMUNDA_NAMESPACE = "http://camunda.org/schema/1.0/dmn";
    private final Logger taskLogger;

    public  DiagramGenerator(Logger logger) {
        this.taskLogger = logger;
    }

    @TaskAction
    public void generate(File dmnDir, File outputFile) throws IOException {
        if (dmnDir == null || !dmnDir.exists() || !dmnDir.isDirectory()) {
            throw new IllegalArgumentException("DMN directory is invalid or not found: " + (dmnDir != null ? dmnDir.getAbsolutePath() : "null"));
        }

        taskLogger.info("Starting DMN documentation generation from directory: {}", dmnDir.getAbsolutePath());

        List<InitiationRule> allInitiationRules = new ArrayList<>();
        List<CompletionRule> allCompletionRules = new ArrayList<>();
        List<CancellationRule> allCancellationRules = new ArrayList<>();
        List<PermissionRule> allPermissionRules = new ArrayList<>();
        List<ConfigurationRule> allConfigurationRules = new ArrayList<>();
        List<TaskTypes> allTaskTypes = new ArrayList<>();

        for (File dmnFile : Objects.requireNonNull(dmnDir.listFiles((dir, name) -> name.toLowerCase().endsWith(".dmn")))) {
            String fileName = dmnFile.getName().toLowerCase();
            taskLogger.info("Dispatching parser for file: {}", dmnFile.getName());

            if (fileName.contains("initiation")) {
                allInitiationRules.addAll(parseInitiationDmn(dmnFile));
            } else if (fileName.contains("completion")) {
                allCompletionRules.addAll(parseCompletionDmn(dmnFile));
            } else if (fileName.contains("cancellation")) {
                allCancellationRules.addAll(parseCancellationDmn(dmnFile));
            } else if (fileName.contains("permission")) {
                allPermissionRules.addAll(parsePermissionDmn(dmnFile));
            } else if (fileName.contains("config")) {
                allConfigurationRules.addAll(parseConfigurationDmn(dmnFile));
            } else if (fileName.contains("types")) {
                allTaskTypes.addAll(parseTaskTypesDmn(dmnFile));
            } else {
                taskLogger.warn("Skipping DMN file '{}' as its type could not be determined from the filename.", dmnFile.getName());
            }
        }

        taskLogger.info("--- Parsing Complete ---");
        taskLogger.info("Total Initiation Rules: {}", allInitiationRules.size());
        taskLogger.info("Total Completion Rules: {}", allCompletionRules.size());
        taskLogger.info("Total Cancellation Rules: {}", allCancellationRules.size());
        taskLogger.info("Total Permission Rules: {}", allPermissionRules.size());
        taskLogger.info("Total Configuration Rules: {}", allConfigurationRules.size());
        taskLogger.info("Total Task Types: {}", allTaskTypes.size());

        ProcessModel processModel = aggregateAllRules(
                allTaskTypes,
                allInitiationRules,
                allCompletionRules,
                allCancellationRules,
                allPermissionRules,
                allConfigurationRules
        );
        taskLogger.info("Process model entries");
        processModel.tasks().entrySet().forEach(entry -> taskLogger.info("Entry: {}, Value: {}", entry.getKey(), entry.getValue()));
//        processModel.transitions().forEach(transition -> taskLogger.info("Transition: {}", transition));
        processModel.events().forEach(e -> taskLogger.info("events: {}", e));
        processModel.states().forEach(e -> taskLogger.info("states: {}", e));
        String mermaidDiagram = generateStateMachineDiagram(processModel);

        try (FileWriter writer = new FileWriter(outputFile)) {
            writer.write("# Process Flow Diagram\n\n");
            writer.write("```mermaid\n");
            writer.write(mermaidDiagram);
            writer.write("```\n");
        }
        taskLogger.info("Successfully generated process flow diagram at: {}", outputFile.getAbsolutePath());
    }

    private <T> List<T> parseDmnToRules(
            File dmnFile,
            Map<String, List<String>> inputColumnAliases,
            Map<String, List<String>> outputColumnAliases,
            BiFunction<Map<String, String>, Map<String, String>, T> ruleFactory
    ) {
        DmnModelInstance modelInstance = Dmn.readModelFromFile(dmnFile);

        List<T> parsedRules = new ArrayList<>();

        Collection<Decision> decisions = modelInstance.getModelElementsByType(Decision.class);
        taskLogger.info("Decisions: {}", decisions);
        taskLogger.info("Decisions size: {}", decisions.size());

        for (Decision decision : decisions) {
            Collection<DecisionTable> decisionTables = decision.getChildElementsByType(DecisionTable.class);
            if (decisionTables.isEmpty()) {
                taskLogger.warn("No decision table found in DMN file: {}. Skipping.", dmnFile.getName());
                return parsedRules;
            }
            for (DecisionTable decisionTable : decisionTables) {
                taskLogger.info("Parsing decision table in '{}' from file '{}'", decision.getName(), dmnFile.getName());
                Map<String, Integer> inputIndexMap = findColumnIndexes(decisionTable.getInputs());
//                inputIndexMap.entrySet().forEach(entry -> taskLogger.info("input map entry: {}", entry));
                Map<String, Integer> outputIndexMap = findColumnIndexes(decisionTable.getOutputs());

                for (Rule rule : decisionTable.getRules()) {
                    List<InputEntry> inputs = new ArrayList<>(rule.getInputEntries());
                    List<OutputEntry> outputs = new ArrayList<>(rule.getOutputEntries());

                    Map<String, String> inputValues = extractColumnAlias(inputColumnAliases, inputIndexMap, inputs);
                    Map<String, String> outputValues = extractColumnAlias(outputColumnAliases, outputIndexMap, outputs);

                    String eventIdCsv = inputValues.getOrDefault("eventId", "");

                    if (eventIdCsv.contains(",")) {
                        taskLogger.info("Found multi-valued eventId rule: '{}'. Splitting into multiple rules.", eventIdCsv);

                        for (String eventId : eventIdCsv.split(",")) {
                            String trimmedEventId = eventId.trim();
                            if (trimmedEventId.isEmpty()) continue;

                            Map<String, String> singleEventInputValues = new HashMap<>(inputValues);
                            singleEventInputValues.put("eventId", trimmedEventId);

                            T parsedRule = ruleFactory.apply(singleEventInputValues, outputValues);
                            parsedRules.add(parsedRule);
                            taskLogger.info("Parsed rule: {}", parsedRule.toString());
                        }
                    } else {
                        T parsedRule = ruleFactory.apply(inputValues, outputValues);
                        parsedRules.add(parsedRule);
                        taskLogger.info("Parsed rule: {}", parsedRule.toString());
                    }
                }
                // Assume one relevant table per file and break
                break;
            }
        }
        taskLogger.info("Parsed {} rules from {}.", parsedRules.size(), dmnFile.getName());
        return parsedRules;

    }

    private Map<String, String> extractColumnAlias(Map<String, List<String>> inputColumnAliases, Map<String, Integer> inputIndexMap, List<? extends DmnElement> cells) {
        Map<String, String> inputValues = new HashMap<>();
        for (Entry<String, List<String>> aliasEntry : inputColumnAliases.entrySet()) {
            String semanticName = aliasEntry.getKey();
            // ...try all its possible names (e.g., "eventId", "event").
            for (String alias : aliasEntry.getValue()) {
                if (inputIndexMap.containsKey(alias)) {
                    inputValues.put(semanticName, getCellText(cells, inputIndexMap, alias));
                    break; // Found it, move to the next semantic name
                }
            }
        }
        return inputValues;
    }

    private Map<String, Integer> findColumnIndexes(Collection<? extends DmnElement> columns) {
        Map<String, Integer> indexMap = new HashMap<>();
        int index = 0;
        for (DmnElement column : columns) {
            String inputText = null;
            if (column instanceof Input inputColumn) {
                inputText = inputColumn.getInputExpression().getText().getTextContent().trim();
//                taskLogger.info("Set input text as {}", inputText);
            }
            String name = column.getAttributeValue("name");
            String inputVariable = column.getAttributeValueNs(CAMUNDA_NAMESPACE, "inputVariable");
            String id = column.getAttributeValue("id");
            String label = column.getAttributeValue("label");

            String key = null;

            if (name != null && !name.isEmpty()) {
                key = name;
            }
            else if (inputVariable != null && !inputVariable.isEmpty()) {
                key = inputVariable;
            }
            else if (inputText != null && !inputText.isEmpty() && inputText.matches("^[a-zA-Z0-9_]+$")) {
                taskLogger.info("set Key as input text: {}", inputText);
                key = inputText;
            }
            else if (id != null && !id.isEmpty() && id.matches("^[a-zA-Z0-9_]+$")) {
                key = id;
            }
            else if (label != null && !label.isEmpty()) {
                key = label;
            }

            if (key != null) {
                String normalizedKey = normalizeKey(key);
                if (normalizedKey != null) {
                    indexMap.put(normalizedKey, index);
                    taskLogger.info("Mapped column '{}' to normalized key '{}' at index {}.", key, normalizedKey, index);
                }
            } else {
                taskLogger.warn("Could not determine a usable identifier (name, inputVariable, or label) for column at index {}.", index);
            }
            index++;
        }
        return indexMap;
    }

    private String normalizeKey(String rawKey) {
        if (rawKey == null || rawKey.trim().isEmpty()) {
            return null;
        }
        String noSpaces = rawKey.replaceAll("\\s+", "");
        if (noSpaces.isEmpty()) {
            return null;
        }
        return Character.toLowerCase(noSpaces.charAt(0)) + noSpaces.substring(1);
    }

    private ProcessModel aggregateAllRules(
            List<TaskTypes> allTaskTypes,
            List<InitiationRule> allInitiationRules,
            List<CompletionRule> allCompletionRules,
            List<CancellationRule> allCancellationRules,
            List<PermissionRule> allPermissionRules,
            List<ConfigurationRule> allConfigurationRules
    ) {
        taskLogger.info("Starting full aggregation of all parsed DMN rules...");

        Map<String, FullTaskDefinition> tasksById = new HashMap<>();
        Set<String> events = new HashSet<>();
        Set<String> states = new HashSet<>();

        for (TaskTypes taskType : allTaskTypes) {
            tasksById.put(taskType.taskTypeId(), new FullTaskDefinition(taskType.taskTypeId(), taskType.taskTypeName()));
        }
        taskLogger.info("Finished aggregating {} task types", tasksById.size());

        allInitiationRules.forEach(rule -> {
            if (tasksById.containsKey(rule.taskId())) {
                tasksById.get(rule.taskId()).initiationRules().add(rule);
            }
            if(!rule.postEventState().isEmpty()) {
                states.add(rule.postEventState());
            }
            events.add(rule.eventId());
        });

        taskLogger.debug("Finished aggregating {} initiation rules", allInitiationRules.size());

        allCompletionRules.forEach(rule -> {
            if (tasksById.containsKey(rule.taskType())) {
                tasksById.get(rule.taskType()).completionRules().add(rule);
            }
            events.add(rule.eventId());
        });
        taskLogger.debug("Finished aggregating {} completion rules", allCompletionRules.size());

        allPermissionRules.forEach(rule -> {
            if (tasksById.containsKey(rule.taskType())) {
                tasksById.get(rule.taskType()).permissionRules().add(rule);
            }
        });
        taskLogger.debug("Finished aggregating {} permission rules", allPermissionRules.size());

        allConfigurationRules.forEach(rule -> {
            if (tasksById.containsKey(rule.taskType())) {
                tasksById.get(rule.taskType()).configurationRules().add(rule);
            }
        });
        taskLogger.debug("Finished aggregating {} configuration rules", allConfigurationRules.size());

        for (CancellationRule rule : allCancellationRules) {
            taskLogger.info("Aggregating rule {}", rule);
            //for now only deal with cancellation
            if (!rule.action().equalsIgnoreCase("Cancel")) {
                taskLogger.info("skipping rule: {}", rule);
                continue;
            }

            //note we treat empty from/to state as wildcard
            //and empty process categories is also a wildcard
            String categoriesToCancel = rule.processCategories();
            if (categoriesToCancel == null || categoriesToCancel.isEmpty()) {
                continue;
            }

            // Find every task that belongs to the categories specified in the cancellation rule.
            for (FullTaskDefinition task : tasksById.values()) {
                // To know a task's category, we must look at its initiation rules.
                boolean matchesCategory = task.initiationRules().stream()
                        .anyMatch(initiationRule -> categoriesToCancel.contains(initiationRule.processCategory()));

                if (matchesCategory) {
                    // This cancellation rule applies to this task. Record it.
                    task.cancellationRules().add(rule);
                }
            }
        }

        taskLogger.info("Aggregated rules into {} rich task objects.", tasksById.size());

        return new ProcessModel(tasksById, states.stream().toList(), events.stream().toList());
    }

    private String getCellText(List<? extends DmnElement> cells, Map<String, Integer> indexMap, String columnName) {
        if (indexMap.containsKey(columnName)) {
            int index = indexMap.get(columnName);
            if (index < cells.size()) {
                DmnElement cell = cells.get(index);
                if (cell instanceof InputEntry) {
                    return stripQuotes(((InputEntry) cell).getText().getTextContent());
                } else if (cell instanceof OutputEntry) {
                    return stripQuotes(((OutputEntry) cell).getText().getTextContent());
                }
            }
        }
        return "";
    }

    public List<InitiationRule> parseInitiationDmn(File dmnFile) {
        return parseDmnToRules(
                dmnFile,
                Map.of(
                    "eventId", List.of("eventId"),
                    "postEventState", List.of("postEventState"),
                    "referralType", List.of("referralType")
                ),
                Map.of(
                    "taskId", List.of("taskId"),
                    "taskName", List.of("taskName"),
                    "delayDuration", List.of("delayDuration"),
                    "workingDaysAllowed", List.of("workingDaysAllowed"),
                    "processCategory", List.of("processCategory"),
                    "workType", List.of("workType"),
                    "roleCategory", List.of("roleCategory")),
                (inputs, outputs) -> new InitiationRule(
                    inputs.get("eventId"),
                    inputs.get("postEventState"),
                    inputs.get("referralType"),
                    outputs.get("taskId"),
                    outputs.get("taskName"),
                    outputs.get("delayDuration"),
                    outputs.get("workingDaysAllowed"), // Note: I corrected the typo from your record
                    outputs.get("processCategory"),
                    outputs.get("workType"),
                    outputs.get("roleCategory")
                )
        );
    }

    public List<CompletionRule> parseCompletionDmn(File dmnFile) {
        return parseDmnToRules(
                dmnFile,
                Map.of("eventId", List.of("eventId")),
                Map.of(
                    "taskType", List.of("taskType"),
                    "completionMode", List.of("completionMode")),
                (inputs, outputs) -> new CompletionRule(
                    inputs.get("eventId"),
                    outputs.get("taskType"),
                    outputs.get("completionMode")
                )
        );
    }

    public List<CancellationRule> parseCancellationDmn(File dmnFile) {
        return parseDmnToRules(
                dmnFile,
                Map.of(
                    "fromState", List.of("fromState"),
                    "eventId", List.of("eventId", "event"),
                    "toState", List.of("toState", "state")),
                Map.of(
                    "action", List.of("action"),
                    "warningCode", List.of("warningCode"),
                    "warningText", List.of("warningText"),
                    "processCategories", List.of("processCategories")),
                (inputs, outputs) -> new CancellationRule(
                    inputs.get("fromState"),
                    inputs.get("eventId"),
                    inputs.get("toState"),
                    outputs.get("action"),
                    outputs.get("warningCode"),
                    outputs.get("warningText"),
                    outputs.get("processCategories")
                )
        );
    }

    public List<PermissionRule> parsePermissionDmn(File dmnFile) {
        return parseDmnToRules(
                dmnFile,
                Map.of(
                    "taskType", List.of("taskType"),
                    "caseData", List.of("caseData")),
                Map.of(
                    "caseAccessCategory", List.of("caseAccessCategory"),
                    "name", List.of("name"),
                    "value",List.of("value"),
                    "roleCategory", List.of("roleCategory"),
                    "authorisations", List.of("authorisations"),
                    "assignmentPriority", List.of("assignmentPriority"),
                    "autoAssignable", List.of("autoAssignable")),
                (inputs, outputs) -> new PermissionRule(
                    inputs.get("taskType"),
                    inputs.get("caseData"),
                    outputs.get("caseAccessCategory"),
                    outputs.get("name"),
                    outputs.get("value"),
                    outputs.get("roleCategory"),
                    outputs.get("authorisations"),
                    outputs.get("assignmentPriority"),
                    outputs.get("autoAssignable")
                )
        );
    }

    public List<ConfigurationRule> parseConfigurationDmn(File dmnFile) {
        return parseDmnToRules(
                dmnFile,
                Map.of(
                    "caseData", List.of("caseData"),
                    "taskType", List.of("taskType")),
                Map.of(
                    "name", List.of("name"),
                    "value", List.of("value"),
                    "canReconfigure", List.of("canReconfigure")),
                (inputs, outputs) -> new ConfigurationRule(
                    inputs.get("caseData"),
                    inputs.get("taskType"),
                    outputs.get("name"),
                    outputs.get("value"),
                    outputs.get("canReconfigure")
                )
        );
    }

    public List<TaskTypes> parseTaskTypesDmn(File dmnFile) {
        return parseDmnToRules(
                dmnFile,
                new HashMap<>(), // No cells
                Map.of(
                    "taskTypeId", List.of("taskTypeId"),
                    "taskTypeName", List.of("taskTypeName")),
                (inputs, outputs) -> new TaskTypes(
                    outputs.get("taskTypeId"),
                    outputs.get("taskTypeName")
                )
        );
    }

    @FunctionalInterface
    interface RuleProcessor {
        void process(String eventId, String taskId);
    }

    private String generateStateMachineDiagram(ProcessModel processModel) {
        taskLogger.info("Generating Mermaid syntax for the state machine diagram...");
        StringBuilder sb = new StringBuilder();
        sb.append("flowchart TD\n\n");

        Map<String, String> nodeMap = createNodeMap(processModel);

        // Create map of State nodes
        sb.append("%% States: \n");
        processModel.states().forEach(state -> sb.append(nodeMap.get(state)));
        // Create map of Task nodes
        sb.append("%% Tasks:\n");
        processModel.tasks().forEach((id, task) -> sb.append(nodeMap.get(id)));
        // Create map of Event nodes
        sb.append("%% Events:\n");
        processModel.events().forEach(event -> sb.append(nodeMap.get(event)));

        // --- Iterate through all transitions to define nodes and links ---
//        for (RichTransition t : transitions) {
//            // FIX 1: Sanitize state names to create valid Mermaid node IDs.
//            String fromNodeId = toNodeId(t.fromState());
//            String toNodeId = toNodeId(t.toState());
//
//            // Define the 'from' and 'to' nodes if they haven't been defined yet.
//            // The original state name is used as the display label.
//            defineNode(sb, definedNodeIds, fromNodeId, t.fromState());
//            defineNode(sb, definedNodeIds, toNodeId, t.toState());
//
//            // Build the complex label for the arrow
//            StringBuilder label = new StringBuilder();
//            label.append(String.format("`<strong>%s</strong><br/>---<br/>", t.eventId()));
//            if (!t.tasksCreated().isEmpty()) {
//                label.append("<strong>Creates:</strong><br/>");
//                // Using taskName for better readability
//                t.tasksCreated().forEach(task -> label.append(String.format("- %s<br/>", task.taskName())));
//            }
//            if (!t.tasksCompleted().isEmpty()) {
//                label.append("<strong>Completes:</strong><br/>");
//                // Using taskType as the identifier
//                t.tasksCompleted().forEach(task -> label.append(String.format("- %s<br/>", task.taskType())));
//            }
//            if (!t.tasksCancelled().isEmpty()) {
//                label.append("<strong>Cancels:</strong><br/>");
//                t.tasksCancelled().forEach(task -> label.append(String.format("- Action: %s<br/>", task.action())));
//            }
//            label.append("`");
//
//            // FIX 2: Only draw a link if both the start and end nodes are valid.
//            if (fromNodeId != null && toNodeId != null) {
//                sb.append(String.format("    %s -->|%s| %s\n", fromNodeId, label, toNodeId));
//            } else {
//                taskLogger.warn("Skipping link for event '{}' due to a missing fromState or toState.", t.eventId());
//            }
//        }

        return sb.toString();
    }

    private Map<String, String> createNodeMap(ProcessModel processModel) {
        Map<String, String> nodeMap = new HashMap<>();
        processModel.states().forEach(state -> nodeMap.put(state, createStateNode(state)));
        processModel.tasks().forEach((id, task) -> nodeMap.put(id, createTaskNode(task)));
        processModel.events().forEach(event -> nodeMap.put(event, createEventNode(event)));
        return nodeMap;
    }

    private String createStateNode(String state) {
        return String.format("%s@{ shape: stadium, label: '%s' }\n", state, state);
    }

    private String createTaskNode(FullTaskDefinition taskDefinition) {
        return String.format("%s@{ shape subproc, label: '%s' }\n", taskDefinition.id(), taskDefinition.name());
    }

    private String createEventNode(String event) {
        return String.format("%s@{ shape: rounded, label: '%s' }\n", event, event);
    }

    private String stripQuotes(String text) {
        return text.replace("\"", "");
    }
}