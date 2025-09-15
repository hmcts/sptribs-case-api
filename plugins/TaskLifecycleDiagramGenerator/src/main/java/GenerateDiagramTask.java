import lombok.extern.slf4j.Slf4j;
import org.gradle.api.DefaultTask;
import org.gradle.api.file.Directory;
import org.gradle.api.file.DirectoryProperty;
import org.gradle.api.file.RegularFileProperty;
import org.gradle.api.tasks.InputDirectory;
import org.gradle.api.tasks.OutputFile;
import org.gradle.api.tasks.TaskAction;
import java.io.File;

@Slf4j
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
            log.error("DMN directory does not exist: {}", dmnDirectory.getAsFile().getAbsolutePath());
            return;
        }

        getLogger().lifecycle("Processing DMNs from: " + dmnDirectory.getAsFile().getAbsolutePath());
        getLogger().lifecycle("Generating output to: " + outputFile.getAbsolutePath());

        DiagramGenerator generator = new DiagramGenerator();
        generator.generate(dmnDirectory.getAsFile(), getOutputFile().getAsFile().get());

        log.info("DMN lifecycle diagram generated at {}", getOutputFile().getAsFile().get().getAbsolutePath());
    }
}
