package cn.cutemc.voicegender.analyze;

import cn.cutemc.voicegender.analyze.beans.AnalyzeProperty;
import cn.cutemc.voicegender.io.caches.AnalyzeCache;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.stereotype.Component;

@Component
public class AnalyzeController {

    private final ConfigurableApplicationContext context;
    private final AnalyzeCache cache;

    @Autowired
    public AnalyzeController(AnalyzeCache cache, ConfigurableApplicationContext context) {
        this.cache = cache;
        this.context = context;
    }

    public Analyze createAnalyze(AnalyzeProperty property) {
        Analyze analyze = context.getBean(Analyze.class);
        analyze.setProperty(property);

        cache.update(analyze);

        return analyze;
    }

}
