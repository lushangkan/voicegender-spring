package cn.cutemc.voicegender.analyze;

import cn.cutemc.voicegender.analyze.beans.AnalyzeProperty;
import cn.cutemc.voicegender.analyze.engine.EngineType;
import cn.cutemc.voicegender.analyze.engine.tensorflow.serving.TfServingService;
import cn.cutemc.voicegender.analyze.engine.xgboost.XGBoostService;
import cn.cutemc.voicegender.analyze.status.AnalyzeStatus;
import cn.cutemc.voicegender.core.configs.MainConfig;
import cn.cutemc.voicegender.io.database.entities.AnalyzeLog;
import cn.cutemc.voicegender.io.database.entities.ErrorLog;
import cn.cutemc.voicegender.io.database.repositories.AnalyzeRepository;
import cn.cutemc.voicegender.io.database.repositories.ErrorRepository;
import cn.cutemc.voicegender.io.storages.StorageService;
import cn.cutemc.voicegender.utils.AnalyzeUtils;
import cn.cutemc.voicegender.utils.FileUtils;
import cn.cutemc.voicegender.utils.TimeUtils;
import cn.cutemc.voicegender.utils.UUIDUtils;
import lombok.extern.apachecommons.CommonsLog;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.springframework.data.domain.Example;
import org.springframework.scheduling.annotation.Async;

import javax.script.ScriptException;
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

    private final AnalyzeRepository analyzeRepository;

    private final MainConfig config;

    private final TfServingService tfServing;

    private final XGBoostService xgboost;

    private final FFmpegService ffmpeg;

    private final StorageService storageService;
    private final ErrorRepository errorRepository;

    public AnalyzeImpl(AnalyzeRepository analyzeRepository, MainConfig config, TfServingService tfServing, XGBoostService xgboost, FFmpegService ffmpeg, StorageService storageService, ErrorRepository errorRepository) {
        this.analyzeRepository = analyzeRepository;
        this.config = config;
        this.tfServing = tfServing;
        this.xgboost = xgboost;
        this.ffmpeg = ffmpeg;
        this.storageService = storageService;
        this.errorRepository = errorRepository;
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
            features = AnalyzeUtils.ComputedFeatures(property.tmpFile());
        } catch (IOException | ScriptException e) {
            update(property.uuid(), property.time(), property.uploadFile(), property.tmpFile(), AnalyzeStatus.FAILED, null, null);
            log.error("Audio feature extraction failed");
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
                throw new RuntimeException(e);
            }
        }

        //XGBoost
        try {
            result.put(ModelType.XGBOOST, xgboost.predict(features));
        } catch (Exception e) {
            update(property.uuid(), property.time(), property.uploadFile(), property.tmpFile(), AnalyzeStatus.FAILED, null, null);
            log.error("Audio feature prediction failed, Model type: XGBoost");
            throw new RuntimeException(e);
        }

        //更新
        update(property.uuid(), property.time(), property.uploadFile(), property.tmpFile(), AnalyzeStatus.FINISHED, features, result);
    }

    public void update(UUID uuid, Date time, Path uploadFile, Path tmpFile, AnalyzeStatus analyzeStatus, Map<Features, Double> analyzeFeatures, Map<ModelType, Map<Labels, Double>> modelResult) {
        property = new AnalyzeProperty(uuid, time, uploadFile, tmpFile, analyzeStatus, analyzeFeatures, modelResult);
        Example<AnalyzeLog> example = Example.of(new AnalyzeLog(null, null, UUIDUtils.uuidToString(uuid), null, null, null));
        analyzeRepository.findOne(example).ifPresent(analyzeLog -> {
            analyzeLog.setStatus(analyzeStatus.toString());
            analyzeRepository.save(analyzeLog);
        });
    }

    @Override
    public AnalyzeProperty getProperty() {
        return property;
    }

    private void error(Exception exception) {
        ErrorLog errorLog = new ErrorLog(TimeUtils.formatTime(new Date()), "null", UUIDUtils.uuidToString(property.uuid()), "analyzeing", "", property.analyzeStatus().toString(), exception.getMessage() + "\n" + ExceptionUtils.getRootCauseMessage(exception));
        if (property.uploadFile() != null) {
            try {
                errorLog.setUploadFileSize(org.apache.commons.io.FileUtils.byteCountToDisplaySize(Files.size(property.uploadFile())));
            } catch (IOException e) {
                log.error("Get upload file size failed, UUID: " + property.uuid(), e);
            }
        }
        errorRepository.save(errorLog);
    }
}
