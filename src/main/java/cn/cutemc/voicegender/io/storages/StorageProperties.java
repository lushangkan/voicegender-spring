package cn.cutemc.voicegender.io.storages;

import cn.cutemc.voicegender.core.configs.MainConfig;
import lombok.Getter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.Objects;

@ConfigurationProperties("storage")
@Getter
public class StorageProperties {

    private final String location;

    @Autowired
    public StorageProperties(MainConfig config) {
        this.location = Objects.requireNonNullElse(config.getUploadPath(), "./tmp/upload");
    }



}
