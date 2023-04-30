package cn.cutemc.voicegender.io.storages;

import cn.cutemc.voicegender.core.configs.MainConfig;
import cn.cutemc.voicegender.io.caches.TmpFileCache;
import cn.cutemc.voicegender.io.caches.UploadFileCache;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.concurrent.ScheduledExecutorService;

@Configuration
public class StorageConfiguration {

    private final StorageProperties properties;


    private final UploadFileCache uploadFileCache;

    private final TmpFileCache tmpFileCache;

    private final ScheduledExecutorService executorService;

    private final MainConfig config;

    @Autowired
    public StorageConfiguration(StorageProperties properties, UploadFileCache uploadFileCache, TmpFileCache tmpFileCache, @Qualifier("storageScheduler") ScheduledExecutorService executorService, MainConfig config) {
        this.properties = properties;
        this.uploadFileCache = uploadFileCache;
        this.tmpFileCache = tmpFileCache;
        this.executorService = executorService;
        this.config = config;
    }

    @Bean
    public StorageService storageService() {
        return new StorageServiceImpl(properties, uploadFileCache, tmpFileCache, executorService, config);
    }
}
