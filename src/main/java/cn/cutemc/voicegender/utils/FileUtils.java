package cn.cutemc.voicegender.utils;

import cn.cutemc.voicegender.core.configs.MainConfig;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public class FileUtils {

    public static String getRScriptFile(String filename) throws IOException {
        return Files.readString(getRScriptPath(filename));
    }

    public static Path getRScriptPath(String filename) throws IOException {
        Resource resource = new ClassPathResource("R/scripts/" + filename);
        File file = resource.getFile();
        return file.toPath();
    }

    public static Path getXGBoostModelPath(MainConfig config) throws IOException {
        Resource resource = new FileSystemResource(config.getXgboostModelFile());
        File file = resource.getFile();
        return file.toPath();
    }

    public static Path toPath(Path path, String... paths) {
        return Path.of(path.toString(), paths);
    }

}
