package cn.cutemc.voicegender.analyze.script;

import cn.cutemc.voicegender.analyze.Features;
import cn.cutemc.voicegender.utils.FileUtils;
import com.github.rcaller.scriptengine.RCallerScriptEngine;
import lombok.extern.apachecommons.CommonsLog;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;

@Service
@CommonsLog
public class RScriptService {

    private final String script;

    public RScriptService() {
        log.info("Loading R script...");

        try {
            script = Files.readString(FileUtils.getRScriptFile("computed_features.R").toPath(), StandardCharsets.UTF_8);
        } catch (IOException e) {
            log.fatal("Failed to load R script!");
            throw new Error(e);
        }

        try {
            new RCallerScriptEngine();
        } catch (Exception e) {
            log.fatal("Failed to initialize RCallerScriptEngine! Please check your R installation.");
            throw new Error(e);
        }
    }

    /**
     * 获取RCallerScriptEngine
     * @return RCallerScriptEngine
     */
    public RCallerScriptEngine getEngine() {
        return new RCallerScriptEngine();
    }

    /**
     * 计算音频文件的Features
     * @param wavFile 音频文件
     * @return 音频文件的Features
     * @throws RuntimeException 计算异常
     */
    public Map<Features, Double> computedFeatures(Path wavFile) {
        try {
            RCallerScriptEngine engine = getEngine();
            engine.put("path", wavFile.toAbsolutePath().toString());

            engine.eval(script);

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
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
