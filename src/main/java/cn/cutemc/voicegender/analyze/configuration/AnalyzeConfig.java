package cn.cutemc.voicegender.analyze.configuration;

import cn.cutemc.voicegender.analyze.Analyze;
import cn.cutemc.voicegender.analyze.AnalyzeImpl;
import cn.cutemc.voicegender.analyze.FFmpegService;
import cn.cutemc.voicegender.analyze.engine.tensorflow.serving.TfServingService;
import cn.cutemc.voicegender.analyze.engine.xgboost.XGBoostService;
import cn.cutemc.voicegender.core.configs.MainConfig;
import cn.cutemc.voicegender.io.database.repositories.AnalyzeRepository;
import cn.cutemc.voicegender.io.database.repositories.ErrorRepository;
import cn.cutemc.voicegender.io.storages.StorageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Scope;

@Configuration
public class AnalyzeConfig {

    private final AnalyzeRepository analyzeRepository;
    private final MainConfig config;
    private final TfServingService tfServing;
    private final XGBoostService xgboost;
    private final FFmpegService ffmpeg;
    private final StorageService storageService;
    private final ErrorRepository errorRepository;

    @Autowired
    public AnalyzeConfig(AnalyzeRepository analyzeRepository, MainConfig config, TfServingService tfServing, XGBoostService xgboost, FFmpegService ffmpeg, StorageService storageService, ErrorRepository errorRepository) {
        this.analyzeRepository = analyzeRepository;
        this.config = config;
        this.tfServing = tfServing;
        this.xgboost = xgboost;
        this.ffmpeg = ffmpeg;
        this.storageService = storageService;
        this.errorRepository = errorRepository;
    }

    @Bean
    @Scope(value = ConfigurableBeanFactory.SCOPE_PROTOTYPE)
    public Analyze analyze() {
        return new AnalyzeImpl(analyzeRepository, config, tfServing, xgboost, ffmpeg, storageService, errorRepository);
    }
}
