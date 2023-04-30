package cn.cutemc.voicegender.io.caches;

import cn.cutemc.voicegender.analyze.Analyze;
import cn.cutemc.voicegender.core.configs.MainConfig;
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
@CacheConfig(cacheNames = "AnalyzerCache")
public class AnalyzeCache {

    HashMap<Analyze, Date> updateTime = new HashMap<>();

    private final MainConfig config;
    private final ScheduledExecutorService executorService;

    @Autowired
    public AnalyzeCache(MainConfig config, @Qualifier("cacheScheduler") ScheduledExecutorService executorService) {
        this.config = config;
        this.executorService = executorService;
    }

    @Cacheable(key = "#uuid", unless = "#result == null")
    public Analyze getByKey(UUID uuid) {
        return null;
    }

    @CachePut(key = "#analyze.getProperty().uuid()")
    public Analyze update(Analyze analyze) {
        if (analyze.getProperty().analyzeStatus() == null) throw new RuntimeException("analyzeStatus is null");
        if (getByKey(analyze.getProperty().uuid()) != null) delete(analyze.getProperty().uuid());

        updateTime.put(analyze, new Date());
        executorService.schedule(() -> {
            if ((updateTime.get(analyze).getTime() - new Date().getTime()) >= config.getCleanInterval() * 60 * 1000) {
                delete(analyze.getProperty().uuid());
                updateTime.remove(analyze);
            }
        }, config.getCleanInterval(), TimeUnit.MINUTES);

        return analyze;
    }

    @CacheEvict(key = "#uuid")
    public void delete(UUID uuid) {

    }

    @CacheEvict(allEntries = true)
    public void deleteAll() {

    }


}
