package cn.cutemc.voicegender;

import cn.cutemc.voicegender.io.storages.StorageProperties;
import lombok.extern.apachecommons.CommonsLog;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.FileSystemResource;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;

import java.nio.file.Files;
import java.nio.file.Path;

@SpringBootApplication
@EnableScheduling
@EnableAsync
@CommonsLog
@EnableConfigurationProperties(StorageProperties.class)
public class VoicegenderApplication {

    public static void main(String[] args) {
        checkConfig();
        ConfigurableApplicationContext app = SpringApplication.run(VoicegenderApplication.class, args);
    }

    public static void checkConfig() {
        FileSystemResource configFile1 = new FileSystemResource(System.getProperty("user.dir") + "/config/application.yml");
        FileSystemResource configFile2 = new FileSystemResource(System.getProperty("user.dir") + "/application.yml");
        if (!configFile1.exists() && !configFile2.exists()) {
            log.fatal("Unable to find config file, config file will be generated.");
            ClassPathResource defaultConfig = new ClassPathResource("application_default.yml");
            try {
                Files.copy(defaultConfig.getInputStream(), Path.of(configFile2.getPath()));
            } catch (Exception e) {
                log.fatal("Unable to generate config file, please check your permission.");
                System.exit(1);
            }
            log.fatal("Config file generated, please edit it and restart the application.");
            System.exit(1);
        }
    }
}
