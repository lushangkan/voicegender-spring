package cn.cutemc.voicegender.core.configuration;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

@Configuration
@EnableAsync
public class TaskExecutorConfig {

    @Bean(name="analyzeTask")
    public ThreadPoolTaskExecutor taskExecutor() {
        ThreadPoolTaskExecutor pool = new ThreadPoolTaskExecutor();
        pool.setCorePoolSize(5);//核心线程数
        pool.setMaxPoolSize(8);//最大线程数
        pool.setQueueCapacity(50);//线程队列
        pool.setThreadNamePrefix("analyze-task-");//线程名称前缀
        pool.initialize();//线程初始化
        return pool;
    }

    @Bean(name="cacheScheduler")
    public ScheduledExecutorService cacheScheduled(){
        return Executors.newScheduledThreadPool(1);
    }

    @Bean(name="storageScheduler")
    public ScheduledExecutorService storageScheduled(){
        return Executors.newScheduledThreadPool(1);
    }

    @Bean(name="taskScheduler")
    public ScheduledExecutorService taskScheduled(){
        return Executors.newScheduledThreadPool(1);
    }

}
