import org.gradle.api.DefaultTask;
import org.gradle.api.file.Directory;
import org.gradle.api.file.DirectoryProperty;
import org.gradle.api.file.RegularFileProperty;
import org.gradle.api.tasks.InputDirectory;
import org.gradle.api.tasks.OutputFile;
import org.gradle.api.tasks.TaskAction;
import java.io.File;

public abstract class GenerateDiagramTask extends DefaultTask {

    @InputDirectory
    public abstract DirectoryProperty getDmnDir();

    @OutputFile
    public abstract RegularFileProperty getOutputFile();

    @TaskAction
    public void generate() throws Exception {
        Directory dmnDirectory = getDmnDir().get();
        File outputFile = getOutputFile().get().getAsFile();

        if (!dmnDirectory.getAsFile().exists()) {
            getLogger().error("DMN directory does not exist: {}", dmnDirectory.getAsFile().getAbsolutePath());
            return;
        }

        getLogger().lifecycle("Processing DMNs from: " + dmnDirectory.getAsFile().getAbsolutePath());
        getLogger().lifecycle("Generating output to: " + outputFile.getAbsolutePath());

        DiagramGenerator generator = new DiagramGenerator(getLogger());
        generator.generate(dmnDirectory.getAsFile(), getOutputFile().getAsFile().get());

        getLogger().lifecycle("DMN lifecycle diagram generated at {}", getOutputFile().getAsFile().get().getAbsolutePath());
    }
}
