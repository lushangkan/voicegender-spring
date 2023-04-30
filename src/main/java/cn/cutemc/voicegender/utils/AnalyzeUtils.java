package cn.cutemc.voicegender.utils;

import cn.cutemc.voicegender.analyze.Features;
import com.github.rcaller.scriptengine.RCallerScriptEngine;
import lombok.extern.apachecommons.CommonsLog;
import ml.dmlc.xgboost4j.java.DMatrix;
import ml.dmlc.xgboost4j.java.XGBoostError;

import javax.script.ScriptException;
import java.io.IOException;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

@CommonsLog
public class AnalyzeUtils {

    public static Map<Features, Double> ComputedFeatures(Path wavFile) throws IOException, ScriptException {
        RCallerScriptEngine engine = new RCallerScriptEngine();

        String audioFile = wavFile.toAbsolutePath().toString().replace("\\", "/");
        engine.put("path", audioFile);
        String run = "source(\"" + FileUtils.getRScriptPath("computed_features.R") + "\")";
        engine.eval(run.replace("\\", "/"));


        HashMap<Features, Double> map = new HashMap<>();


        map.put(Features.MEAN_FREQ, ((double[]) engine.get(Features.MEAN_FREQ.getName()))[0]);
        map.put(Features.SD, ((double[]) engine.get(Features.SD.getName()))[0]);
        map.put(Features.MEDIAN, ((double[]) engine.get(Features.MEDIAN.getName()))[0]);
        map.put(Features.Q25, ((double[]) engine.get(Features.Q25.getName()))[0]);
        map.put(Features.Q75, ((double[]) engine.get(Features.Q75.getName()))[0]);
        map.put(Features.IQR, ((double[]) engine.get(Features.IQR.getName()))[0]);
        map.put(Features.SKEW, ((double[]) engine.get(Features.SKEW.getName()))[0]);
        map.put(Features.KURT, ((double[]) engine.get(Features.KURT.getName()))[0]);
        map.put(Features.SP_ENT, ((double[]) engine.get(Features.SP_ENT.getName()))[0]);
        map.put(Features.SFM, ((double[]) engine.get(Features.SFM.getName()))[0]);
        map.put(Features.MODE, ((double[]) engine.get(Features.MODE.getName()))[0]);
        map.put(Features.CENTROID, ((double[]) engine.get(Features.CENTROID.getName()))[0]);
        map.put(Features.MEAN_FUN, ((double[]) engine.get(Features.MEAN_FUN.getName()))[0]);
        map.put(Features.MIN_FUN, ((double[]) engine.get(Features.MIN_FUN.getName()))[0]);
        map.put(Features.MAX_FUN, ((double[]) engine.get(Features.MAX_FUN.getName()))[0]);
        map.put(Features.MEAN_DOM, ((double[]) engine.get(Features.MEAN_DOM.getName()))[0]);
        map.put(Features.MIN_DOM, ((double[]) engine.get(Features.MIN_DOM.getName()))[0]);
        map.put(Features.MAX_DOM, ((double[]) engine.get(Features.MAX_DOM.getName()))[0]);
        map.put(Features.DF_RANGE, ((double[]) engine.get(Features.DF_RANGE.getName()))[0]);
        map.put(Features.MODINDX, ((double[]) engine.get(Features.MODINDX.getName()))[0]);

        engine.close();

        return map;
    }

    public static List<Double> featureToList(Map<Features, Double> features) {
        return List.of(features.get(Features.MEAN_FREQ), features.get(Features.SD), features.get(Features.MEDIAN), features.get(Features.Q25), features.get(Features.Q75), features.get(Features.IQR), features.get(Features.SKEW), features.get(Features.KURT), features.get(Features.SP_ENT), features.get(Features.SFM), features.get(Features.MODE), features.get(Features.CENTROID), features.get(Features.MEAN_FUN), features.get(Features.MIN_FUN), features.get(Features.MAX_FUN), features.get(Features.MEAN_DOM), features.get(Features.MIN_DOM), features.get(Features.MAX_DOM), features.get(Features.DF_RANGE), features.get(Features.MODINDX));
    }

    public static DMatrix listToMatrix(List<Double> list) throws XGBoostError {
        float[] floats = new float[20];
        for (int i = 0; i < list.size(); i++) {
            floats[i] = list.get(i).floatValue();
        }

        //疑似为向量
        return new DMatrix(floats, 1, 20, 0f);
    }

    public static DMatrix featureToMatrix(Map<Features, Double> features) throws XGBoostError {
        return listToMatrix(featureToList(features));
    }

    public static Map<Features, Double> toDoubleMap(Map<Features, ?> features) {
        if (features.values().stream().anyMatch(Objects::isNull)) {
            log.error("Data cannot contain null values.");
            throw new IllegalArgumentException("Data cannot contain null values.");
        }

        if (features.values().stream().anyMatch(value -> !(value instanceof Double) && !(value instanceof Float))) {
            log.error("Data type must be Double or Float.");
            throw new IllegalArgumentException("Data type must be Double or Float.");
        }

        //转换为Double

        return features.entrySet().stream().collect(HashMap::new, (m, e) -> m.put(e.getKey(), e.getValue() instanceof Double ? (Double) e.getValue() : (Float) e.getValue()), HashMap::putAll);
    }
}
