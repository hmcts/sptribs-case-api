import org.gradle.api.Project;
import java.io.File;

public class TaskLifecycleDiagramExtension {
    private File dmnDir;
    private File outputFile;

    public TaskLifecycleDiagramExtension(Project project) {
        this.dmnDir = project.file("src/main/resources/dmn");
        this.outputFile = project.file("build/diagrams/dmn-lifecycle.md");
    }

    public File getDmnDir() {
        return dmnDir;
    }

    public void setDmnDir(File dmnDir) {
        this.dmnDir = dmnDir;
    }

    public File getOutputFile() {
        return outputFile;
    }

    public void setOutputFile(File outputFile) {
        this.outputFile = outputFile;
    }
}
