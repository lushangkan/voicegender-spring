package cn.cutemc.voicegender.core.listeners;

import cn.cutemc.voicegender.io.storages.StorageService;
import org.springframework.boot.SpringApplicationRunListener;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.stereotype.Component;

@Component
public class BeanLoadedListener implements SpringApplicationRunListener {

    @Override
    public void contextLoaded(ConfigurableApplicationContext context) {
        SpringApplicationRunListener.super.contextLoaded(context);

        //初始化StoreService
        StorageService storageService = context.getBean(StorageService.class);
        storageService.init();

    }
}
