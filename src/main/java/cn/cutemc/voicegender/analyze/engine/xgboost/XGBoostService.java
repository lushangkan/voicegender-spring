package cn.cutemc.voicegender.analyze.engine.xgboost;

import cn.cutemc.voicegender.analyze.Features;
import cn.cutemc.voicegender.analyze.Labels;
import cn.cutemc.voicegender.core.configs.MainConfig;
import cn.cutemc.voicegender.utils.AnalyzeUtils;
import cn.cutemc.voicegender.utils.FileUtils;
import lombok.extern.apachecommons.CommonsLog;
import ml.dmlc.xgboost4j.java.Booster;
import ml.dmlc.xgboost4j.java.DMatrix;
import ml.dmlc.xgboost4j.java.XGBoost;
import ml.dmlc.xgboost4j.java.XGBoostError;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.Map;

@Service
@CommonsLog
public class XGBoostService {

    private final Booster booster;

    private final MainConfig config;

    @Autowired
    public XGBoostService(MainConfig config) throws IOException, XGBoostError {
        this.config = config;

        log.info("Initializing XGBoost service...");

        try {
            booster = XGBoost.loadModel(FileUtils.getXGBoostModelPath(config).toAbsolutePath().toString());
        } catch (Exception e) {
            log.error("Failed to load XGBoost model. Please check if the model file exists.");
            throw e;
        }
    }

    public Booster getBooster() {
        return booster;
    }

    public Map<Labels, Double> predict(Map<Features, ?> features) throws XGBoostError {

        Map<Features, Double> doubleFeatures = AnalyzeUtils.toDoubleMap(features);

        DMatrix matrix = AnalyzeUtils.featureToMatrix(doubleFeatures);

        float[][] predict = booster.predict(matrix);

        double masculine = predict[0][0];

        return Map.of(
                Labels.MASCULINE, masculine,
                Labels.FEMININE, 1 - masculine
        );
    }
}
