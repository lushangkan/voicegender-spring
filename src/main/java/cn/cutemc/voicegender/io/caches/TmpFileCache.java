package cn.cutemc.voicegender.io.caches;

import cn.cutemc.voicegender.core.configs.MainConfig;
import cn.cutemc.voicegender.io.storages.bean.StorageFile;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.cache.annotation.CacheConfig;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.HashMap;
import java.util.UUID;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

@Service
@CacheConfig(cacheNames = "TmpFileCache")
public class TmpFileCache {

    HashMap<StorageFile, Date> updateTime = new HashMap<>();

    private final MainConfig config;
    private final ScheduledExecutorService executorService;

    @Autowired
    public TmpFileCache(MainConfig config, @Qualifier("cacheScheduler") ScheduledExecutorService executorService) {
        this.config = config;
        this.executorService = executorService;
    }

    @Cacheable(key = "#uuid", unless = "#result == null")
    public StorageFile getByKey(UUID uuid) {
        return null;
    }

    @CachePut(key = "#storageFile.analyzeUUID()")
    public StorageFile update(StorageFile storageFile) {
        if (getByKey(storageFile.analyzeUUID()) != null) delete(storageFile.analyzeUUID());
        updateTime.put(storageFile, new Date());
        executorService.schedule(() -> {
            if ((updateTime.get(storageFile).getTime() - new Date().getTime()) >= config.getCleanInterval() * 60 * 1000) {
                delete(storageFile.analyzeUUID());
                updateTime.remove(storageFile);
            }
        }, config.getCleanInterval(), TimeUnit.MINUTES);
        return storageFile;
    }

    @CacheEvict(key = "#uuid")
    public void delete(UUID uuid) {

    }

    @CacheEvict(allEntries = true)
    public void deleteAll() {

    }

}
