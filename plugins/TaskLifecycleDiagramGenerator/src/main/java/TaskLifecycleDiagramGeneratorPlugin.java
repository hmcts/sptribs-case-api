import org.gradle.api.Plugin;
import org.gradle.api.Project;

import java.io.File;

public class TaskLifecycleDiagramGeneratorPlugin implements Plugin<Project> {

    public void apply(Project project) {
        TaskLifecycleDiagramExtension taskLifecycleDiagramExtension =
                project.getExtensions().create("taskLifecycleDiagram", TaskLifecycleDiagramExtension.class, project);
        project.getTasks().register("generateDiagram", GenerateDiagramTask.class, task -> {
            task.setDescription("Generates Mermaid diagram from DMN files");
            task.setGroup("dmn");
            task.getDmnDir().set(taskLifecycleDiagramExtension.getDmnDir());
            task.getOutputFile().set(taskLifecycleDiagramExtension.getOutputFile());
        });

    }

}