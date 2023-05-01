package cn.cutemc.voicegender.analyze;

import cn.cutemc.voicegender.analyze.beans.AnalyzeProperty;
import cn.cutemc.voicegender.analyze.engine.EngineType;
import cn.cutemc.voicegender.analyze.engine.tensorflow.serving.TfServingService;
import cn.cutemc.voicegender.analyze.engine.xgboost.XGBoostService;
import cn.cutemc.voicegender.analyze.script.RScriptService;
import cn.cutemc.voicegender.analyze.status.AnalyzeStatus;
import cn.cutemc.voicegender.core.configs.MainConfig;
import cn.cutemc.voicegender.io.database.LogService;
import cn.cutemc.voicegender.io.storages.StorageService;
import cn.cutemc.voicegender.utils.FileUtils;
import lombok.extern.apachecommons.CommonsLog;
import org.springframework.scheduling.annotation.Async;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@CommonsLog
public class AnalyzeImpl implements Analyze {

    private AnalyzeProperty property;

    private final MainConfig config;

    private final TfServingService tfServing;

    private final XGBoostService xgboost;

    private final FFmpegService ffmpeg;

    private final StorageService storageService;
    private final LogService logService;
    private final RScriptService rScriptService;

    public AnalyzeImpl(MainConfig config, TfServingService tfServing, XGBoostService xgboost, FFmpegService ffmpeg, StorageService storageService, LogService logService, RScriptService rScriptService) {
        this.config = config;
        this.tfServing = tfServing;
        this.xgboost = xgboost;
        this.ffmpeg = ffmpeg;
        this.storageService = storageService;
        this.logService = logService;
        this.rScriptService = rScriptService;
    }

    @Override
    public void setProperty(AnalyzeProperty property) {
        this.property = property;
    }

    @Override
    @Async("analyzeTask")
    public void start() {
        try {
            preprocessing();
            analyze();
        } catch (Exception e) {
            error(e);
        }

        //删除文件
        storageService.delete(Path.of(config.getUploadPath()), property.uuid());
        storageService.delete(Path.of(config.getTmpPath()), property.uuid());
    }

    @Override
    public void preprocessing() {

        //更新
        update(property.uuid(), property.time(), property.uploadFile(), property.tmpFile(), AnalyzeStatus.CONVERTING, null, null);

        Path tmpFile = FileUtils.toPath(Path.of(config.getTmpPath()), property.uuid() + ".wav");

        try {
            //音频预处理
            ffmpeg.audioPreprocessing(property.uploadFile(), tmpFile);
        } catch (IOException e) {
            update(property.uuid(), property.time(), property.uploadFile(), null, AnalyzeStatus.FAILED, null, null);
            error(e);
            throw new RuntimeException(e);
        }

        // 更新到Cache
        storageService.record(Path.of(config.getTmpPath()), tmpFile, property.uuid());

        update(property.uuid(), property.time(), property.uploadFile(), tmpFile, property.analyzeStatus(), null, null);

    }

    @Override
    public void analyze() {

        //更新
        update(property.uuid(), property.time(), property.uploadFile(), property.tmpFile(), AnalyzeStatus.ANALYZE_AUDIO_FEATURES, null, null);

        //音频特征提取

        Map<Features, Double> features;

        try {
            features = rScriptService.computedFeatures(property.tmpFile());
        } catch (RuntimeException e) {
            update(property.uuid(), property.time(), property.uploadFile(), property.tmpFile(), AnalyzeStatus.FAILED, null, null);
            log.error("Audio feature extraction failed");
            error(e);
            throw new RuntimeException(e);
        }

        //更新
        update(property.uuid(), property.time(), property.uploadFile(), property.tmpFile(), AnalyzeStatus.PREDICT_AUDIO_FEATURES, features, null);

        //音频特征预测

        //TensorFlow

        Map<ModelType, Map<Labels, Double>> result = new HashMap<>();

        for (ModelType model : ModelType.values()) {
            if (model.getEngineType() != EngineType.TENSORFLOW) continue;

            try {
                result.put(model, tfServing.predict(model, features));
            } catch (Exception e) {
                update(property.uuid(), property.time(), property.uploadFile(), property.tmpFile(), AnalyzeStatus.FAILED, null, null);
                log.error("Audio feature prediction failed, Model type: " + model.getId());
                error(e);
                throw new RuntimeException(e);
            }
        }

        //XGBoost
        try {
            result.put(ModelType.XGBOOST, xgboost.predict(features));
        } catch (Exception e) {
            update(property.uuid(), property.time(), property.uploadFile(), property.tmpFile(), AnalyzeStatus.FAILED, null, null);
            log.error("Audio feature prediction failed, Model type: XGBoost");
            error(e);
            throw new RuntimeException(e);
        }

        //更新
        update(property.uuid(), property.time(), property.uploadFile(), property.tmpFile(), AnalyzeStatus.FINISHED, features, result);
    }

    /**
     * 更新数据库
     *
     * @param uuid            uuid
     * @param time            时间
     * @param uploadFile      上传文件
     * @param tmpFile         临时文件
     * @param analyzeStatus   分析状态
     * @param analyzeFeatures 分析特征
     * @param modelResult     模型结果
     */
    public void update(UUID uuid, Date time, Path uploadFile, Path tmpFile, AnalyzeStatus analyzeStatus, Map<Features, Double> analyzeFeatures, Map<ModelType, Map<Labels, Double>> modelResult) {
        property = new AnalyzeProperty(uuid, time, uploadFile, tmpFile, analyzeStatus, analyzeFeatures, modelResult);
        logService.updateAnalyzeStatus(property);
    }

    @Override
    public AnalyzeProperty getProperty() {
        return property;
    }

    /**
     * 错误处理
     * @param exception 异常
     */
    private void error(Exception exception) {
        long size = 0;
        if (property.uploadFile() != null) {
            try {
                size = Files.size(property.uploadFile());
            } catch (IOException e) {
                log.error("Get file size failed");
            }
        }
        logService.logError(new Date(), "", property.uuid(), "", size, property.analyzeStatus(), exception);
    }
}
