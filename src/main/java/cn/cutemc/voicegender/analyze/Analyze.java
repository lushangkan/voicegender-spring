package cn.cutemc.voicegender.analyze;

import cn.cutemc.voicegender.analyze.beans.AnalyzeProperty;
import org.springframework.scheduling.annotation.Async;

public interface Analyze {

    @Async
    void preprocessing();

    @Async
    void analyze();

    AnalyzeProperty getProperty();

    void setProperty(AnalyzeProperty properties);

    @Async
    void start();

}
