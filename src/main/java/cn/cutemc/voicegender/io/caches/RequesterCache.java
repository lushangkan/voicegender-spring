package cn.cutemc.voicegender.io.caches;

import cn.cutemc.voicegender.core.configs.MainConfig;
import cn.cutemc.voicegender.web.restapi.beans.Requester;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.cache.annotation.CacheConfig;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.HashMap;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

@Service
@CacheConfig(cacheNames = "RequesterCache")
public class RequesterCache {

    HashMap<Requester, Date> updateTime = new HashMap<>();

    private final MainConfig config;
    private final ScheduledExecutorService executorService;

    @Autowired
    public RequesterCache(MainConfig config, @Qualifier("cacheScheduler") ScheduledExecutorService executorService) {
        this.config = config;
        this.executorService = executorService;
    }

    @CachePut(key = "#requester.addr()")
    public Requester update(Requester requester) {
        if (getByKey(requester.addr()) != null) delete(requester.addr());
        updateTime.put(requester, new Date());
        executorService.schedule(() -> {
            if ((updateTime.get(requester).getTime() - new Date().getTime()) >= 60 * 60 * 1000) {
                delete(requester.addr());
                updateTime.remove(requester);
            }
        }, 60, TimeUnit.MINUTES);
        return requester;
    }

    @Cacheable(key = "#addr", unless = "#result == null")
    public Requester getByKey(String addr) {
        return null;
    }

    @CacheEvict(key = "#addr")
    public void delete(String addr) {

    }

    @CacheEvict(allEntries = true)
    public void deleteAll() {

    }

}
