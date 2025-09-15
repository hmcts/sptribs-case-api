import lombok.extern.slf4j.Slf4j;
import org.camunda.bpm.dmn.engine.DmnDecision;
import org.camunda.bpm.dmn.engine.DmnDecisionLogic;
import org.camunda.bpm.dmn.engine.DmnEngine;
import org.camunda.bpm.dmn.engine.DmnEngineConfiguration;
import org.camunda.bpm.dmn.engine.impl.DmnDecisionTableImpl;
import org.camunda.bpm.dmn.engine.impl.DmnDecisionTableInputImpl;
import org.camunda.bpm.dmn.engine.impl.DmnDecisionTableOutputImpl;
import org.camunda.bpm.dmn.engine.impl.DmnDecisionTableRuleImpl;
import org.camunda.bpm.model.dmn.impl.instance.DecisionTableImpl;
import org.camunda.bpm.model.dmn.instance.DecisionTable;
import org.camunda.bpm.model.dmn.instance.InputEntry;
import org.camunda.bpm.model.dmn.instance.OutputEntry;
import org.camunda.bpm.model.dmn.instance.Rule;
import org.gradle.api.file.Directory;
import org.gradle.api.tasks.TaskAction;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

@Slf4j
public class DiagramGenerator {
    private final DmnEngine dmnEngine = DmnEngineConfiguration.createDefaultDmnEngineConfiguration().buildEngine();

    @TaskAction
    public void generate(File dmnDir, File outputFile) throws IOException {
        if (dmnDir == null || !dmnDir.exists() || !dmnDir.isDirectory()) {
            throw new IllegalArgumentException("DMN directory is invalid or not found: " + (dmnDir != null ? dmnDir.getAbsolutePath() : "null"));
        }

        log.info("Starting DMN documentation generation from directory: {}", dmnDir.getAbsolutePath());

        Map<String, TaskDefinition> tasks = new HashMap<>();
        List<LifecycleAction> actions = new ArrayList<>();

        for (File dmnFile : Objects.requireNonNull(dmnDir.listFiles((dir, name) -> name.toLowerCase().endsWith(".dmn")))) {
            String fileName = dmnFile.getName().toLowerCase();

            log.info("Processing file: {}", dmnFile.getName());

            if (fileName.contains("initiation")) {
                parseDmn(dmnFile, "eventId", "taskId", (String eventId, String taskId) -> actions.add(new LifecycleAction(eventId, taskId, LifecycleActionType.CREATES)));
            } else if (fileName.contains("completion")) {
                parseDmn(dmnFile, "eventId", "taskId", (eventId, taskId) -> actions.add(new LifecycleAction(eventId, taskId, LifecycleActionType.COMPLETES)));
            } else if (fileName.contains("cancellation")) {
                parseDmn(dmnFile, "eventId", "taskId", (eventId, taskId) -> actions.add(new LifecycleAction(eventId, taskId, LifecycleActionType.CANCELS)));
            } else if (fileName.contains("task-types") || fileName.contains("task_types")) {
                // For task-types, the 'name' is the input and 'id' is the output
                parseDmn(dmnFile, "name", "id", (taskName, taskId) -> tasks.put(taskId, new TaskDefinition(taskId, taskName)));
            } else {
                log.warn("Skipping DMN file '{}' as its type could not be determined from the filename.", dmnFile.getName());
            }
        }

        log.info("Parsed {} total task definitions.", tasks.size());
        log.info("Parsed {} total lifecycle actions.", actions.size());

        String mermaidDiagram = generateMermaidDiagram(tasks.values(), actions);

        try (FileWriter writer = new FileWriter(outputFile)) {
            writer.write("# Task Lifecycle Diagram\n\n");
            writer.write(mermaidDiagram);
        }

        log.info("Successfully generated Mermaid diagram at: {}", outputFile.getAbsolutePath());
    }

    private String getFileNameWithoutExtension(File file) {
        String name = file.getName();
        int lastIndexOf = name.lastIndexOf(".");
        if (lastIndexOf == -1) {
            return name;
        }
        return name.substring(0, lastIndexOf);
    }

//    private void parseDmn(File dmnFile, String decisionKey, String eventColumn, String taskColumn, RuleProcessor processor) throws IOException {
//        if (!dmnFile.exists()) {
//            log.warn("DMN file not found, skipping: {}", dmnFile.getName());
//            return;
//        }
//
//        try (InputStream inputStream = new FileInputStream(dmnFile)) {
//            DmnDecision decision = dmnEngine.parseDecision(decisionKey, inputStream);
//
//            DmnDecisionLogic logic = decision.getDecisionLogic();
//            log.info("Logic type: {}", logic.getClass());
//            if (!(logic instanceof DmnDecisionTableImpl)) {
//                log.warn("Decision logic in {} is not a Decision Table. Skipping.", dmnFile);
//                return;
//            }
//            DmnDecisionTableImpl table = (DmnDecisionTableImpl) logic;
//            List<DmnDecisionTableRuleImpl> rules = ((DmnDecisionTableImpl) logic).getRules();
//            List<DmnDecisionTableInputImpl> inputs = table.getInputs();
//            List<DmnDecisionTableOutputImpl> outputs = table.getOutputs();
//
//            for (DmnDecisionTableRuleImpl rule : rules)
//            {
//                log.info(rule.toString());
//            }
//
//            // Find the column indexes for the event and task columns.
////            final int eventInputIndex = findColumnIndex(inputs, eventColumn);
////            final int taskOutputIndex = findColumnIndex(outputs, taskColumn);
////
////            if (eventInputIndex == -1 || taskOutputIndex == -1) {
////                log.error("In DMN '{}', could not find required columns: input '{}' and output '{}'. Check DMN column names/labels.", dmnFile.getName(), eventColumn, taskColumn);
////                return;
////            }
//
//            // Statically iterate over all rules in the model.
////            for (Rule rule : table.getRules()) {
////                List<InputEntry> inputs = new ArrayList<>(rule.getInputEntries());
////                List<OutputEntry> outputs = new ArrayList<>(rule.getOutputEntries());
////
////                String eventId = stripQuotes(inputs.get(eventInputIndex).getText().getTextContent());
////                String taskId = stripQuotes(outputs.get(taskOutputIndex).getText().getTextContent());
////
//                // Process the extracted rule using a lambda.
////                processor.process(eventId, taskId);
////            }
////            log.info("Parsed {} rules from decision '{}' in {}.", table.getRules().size(), decisionKey, dmnFile.getName());
////        } catch (Exception e) {
////            log.error("Failed to parse DMN file: " + dmnFile.getName(), e);
//        }
//    }
    private void parseDmn(File dmnFile, String inputColumnName, String outputColumnName, RuleProcessor processor) throws IOException {
        try (InputStream inputStream = new FileInputStream(dmnFile)) {
            // FIX 1: Parse ALL decisions from the file, not just one by a key.
            // This makes the code independent of the decision ID.
            Collection<DmnDecision> decisions = dmnEngine.parseDecisions(inputStream);

            for (DmnDecision decision : decisions) {
                DmnDecisionLogic logic = decision.getDecisionLogic();
                log.info("Logic for {}, is {}", dmnFile, logic.getClass().toGenericString());

                // We only care about decisions that are implemented as decision tables.
                if (logic instanceof DmnDecisionTableImpl) {
                    DmnDecisionTableImpl table = (DmnDecisionTableImpl) logic;
                    table.getInputs().forEach(i -> log.info("input id: {}, input name {}", i.id, i.name));
                    table.getRules();
                    table.getOutputs();


//                    final int inputIndex = findColumnIndex(table.getInputs(), inputColumnName);
//                    final int outputIndex = findColumnIndex(table.getOutputs(), outputColumnName);

//                    if (inputIndex == -1 || outputIndex == -1) {
//                        log.warn("In DMN '{}', decision '{}', could not find required columns: input '{}' and output '{}'. Skipping this decision.", dmnFile.getName(), decision.getKey(), inputColumnName, outputColumnName);
//                        continue; // Skip to the next decision in the file
//                    }
//
//                    for (Rule rule : table.getRules()) {
//                        List<InputEntry> inputs = new ArrayList<>(rule.getInputEntries());
//                        List<OutputEntry> outputs = new ArrayList<>(rule.getOutputEntries());
//
//                        String rawInputText = stripQuotes(inputs.get(inputIndex).getText().getContent());
//                        String taskValue = stripQuotes(outputs.get(outputIndex).getText().getContent());
//
//                        if (rawInputText == null || rawInputText.trim().isEmpty()) {
//                            continue; // Skip rules with empty inputs
//                        }
//
//                         FIX 2: Handle comma-separated values in the input entry.
//                        String[] eventValues = rawInputText.split(",");
//                        for (String eventValue : eventValues) {
//                            String trimmedEvent = eventValue.trim();
//                            if (!trimmedEvent.isEmpty()) {
//                                processor.process(trimmedEvent, taskValue);
//                            }
//                        }
//                    }
//                    log.info("Parsed {} rules from decision '{}' in {}.", table.getRules().size(), decision.getKey(), dmnFile.getName());
//                     We assume one relevant decision table per file, so we can break after finding it.
//                    break;
//                }
                }
//        } catch (Exception e) {
//            log.error("Failed to parse DMN file: {}.", dmnFile.getName(), e);
            }
        }
    }

    @FunctionalInterface
    interface RuleProcessor {
        void process(String eventId, String taskId);
    }

    private String generateMermaidDiagram(Collection<TaskDefinition> tasks, List<LifecycleAction> actions) {
        StringBuilder sb = new StringBuilder();
        sb.append("```mermaid\n");
        sb.append("flowchart TD\n\n");
        sb.append("    %% --- Task Nodes ---\n");
        // FIX 5: Changed to standard getters (.getId(), .getName()) which are generated by Lombok's @Value
        tasks.forEach(task -> sb.append(String.format("    %s[\"Task: %s\"]\n", task.id(), task.name())));
        sb.append("\n    %% --- Event Nodes ---\n");
        actions.stream().map(LifecycleAction::eventId).distinct()
                .forEach(eventId -> sb.append(String.format("    %s(Event: %s)\n", eventId, eventId)));
        sb.append("\n    %% --- Links ---\n");
        actions.forEach(action -> sb.append(String.format("    %s -->|%s| %s\n", action.eventId(), action.actionType(), action.taskId())));
        sb.append("```\n");
        return sb.toString();
    }

    private int findColumnIndex(Collection<? extends org.camunda.bpm.model.dmn.instance.DmnElement> columns, String name) {
        int index = 0;
        for (org.camunda.bpm.model.dmn.instance.DmnElement column : columns) {
            if (name.equals(column.getAttributeValue("name")) || name.equals(column.getAttributeValue("label"))) {
                return index;
            }
            index++;
        }
        return -1;
    }

    private String stripQuotes(String text) {
        if (text != null && text.startsWith("\"") && text.endsWith("\"")) {
            return text.substring(1, text.length() - 1);
        }
        return text;
    }
}