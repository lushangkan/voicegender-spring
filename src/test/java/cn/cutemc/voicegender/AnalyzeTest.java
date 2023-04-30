package cn.cutemc.voicegender;

import cn.cutemc.voicegender.analyze.Features;
import cn.cutemc.voicegender.analyze.ModelType;
import cn.cutemc.voicegender.analyze.engine.tensorflow.serving.TfServingService;
import cn.cutemc.voicegender.core.configs.MainConfig;
import cn.cutemc.voicegender.utils.AnalyzeUtils;
import cn.cutemc.voicegender.utils.DataSetUtils;
import cn.cutemc.voicegender.utils.FileUtils;
import com.github.rcaller.scriptengine.RCallerScriptEngine;
import com.opencsv.exceptions.CsvException;
import ml.dmlc.xgboost4j.java.Booster;
import ml.dmlc.xgboost4j.java.DMatrix;
import ml.dmlc.xgboost4j.java.XGBoost;
import ml.dmlc.xgboost4j.java.XGBoostError;
import org.junit.jupiter.api.Test;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;

import javax.script.ScriptException;
import java.io.IOException;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class AnalyzeTest {

    @Test
    public void computedFeatures() throws ScriptException, IOException {
        Map<Features, Double> features = AnalyzeUtils.ComputedFeatures(Path.of("/mnt/f/voice-gender/1.wav"));
        features.forEach((k, v) -> System.out.println(k + " : " + v));
    }

    @Test
    public void console() throws IOException, ScriptException {
        RCallerScriptEngine engine = new RCallerScriptEngine();
        String audioFile = Path.of("/mnt/f/voice-gender/1.wav").toString();
        engine.put("path", audioFile);
        String run = "source(\"" + FileUtils.getRScriptFile("computed_features.R") + "\")";
        engine.eval(run);

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

        map.forEach((k, v) -> System.out.println(k + " : " + v));
    }

    @Test
    public void sampleTest() throws IOException, CsvException {
        Path dataSetPath = Path.of("F:\\voice_dataset\\Mozilla Common Voice\\cv-corpus-9.0-2022-04-27\\zh-CN");
        Map<String, String> csvMap = DataSetUtils.getDataSet(dataSetPath, "validated.tsv");
        csvMap = DataSetUtils.sample(csvMap, 100);
        int male = 0;
        int female = 0;

        for (Map.Entry<String, String> entry : csvMap.entrySet()) {
            String v = entry.getValue();
            if (v.equals("male")) {
                male++;
            } else if(v.equals("female")) {
                female++;
            }
        }

        System.out.println("Male: " + male + ", Female: " + female);
    }

    @Test
    public void tensorflowTest() throws ScriptException, IOException {
        MainConfig config = new MainConfig();
        config.setServingAddress("127.0.0.1");
        config.setServingPort(8081);

        TfServingService tfServingService = new TfServingService(config);

        Map<Features, Double> features = AnalyzeUtils.ComputedFeatures(Path.of("F:/voice-gender/1.wav"));

        tfServingService.getStatus().forEach((tfModelType, aBoolean) -> System.out.println(tfModelType + " : " + aBoolean));

        System.out.println("Random Forest");
        tfServingService.predict(ModelType.RANDOM_FOREST, features).forEach((labels, aDouble) -> System.out.println(labels.name() + " : " + aDouble));
        System.out.println("GBT");
        tfServingService.predict(ModelType.GBT, features).forEach((labels, aDouble) -> System.out.println(labels.name() + " : " + aDouble));
        System.out.println("CART");
        tfServingService.predict(ModelType.CART, features).forEach((labels, aDouble) -> System.out.println(labels.name() + " : " + aDouble));
    }

    @Test
    public void xgboostTest() throws XGBoostError, IOException, ScriptException {
        Resource resource = new ClassPathResource("models/xgboost/model.json");
        Booster booster = XGBoost.loadModel(resource.getFile().getAbsolutePath());

        Map<Features, Double> features = AnalyzeUtils.ComputedFeatures(Path.of("F:\\voice_dataset\\ST-CMDS-20170001_1-OS\\20170001P00001A0007.wav"));
        DMatrix matrix = AnalyzeUtils.featureToMatrix(features);

        float[][] predict = booster.predict(matrix);

        List.of(predict).forEach(floats -> System.out.println(Arrays.toString(floats)));
    }


//    Python call tensorflow


//    @Test
//    public void predictFeatures() throws ScriptException, IOException {
//        ModelLoader modelLoader = new ModelLoader();
//
//        Map<Features, Double> features = AnalyzeUtils.ComputedFeatures(Path.of("/mnt/f/voice-gender/1.wav"));
//        features.forEach((k, v) -> System.out.println(k + " : " + v));
//
//        Map<Labels, Double> predict = AnalyzeUtils.predictFeatures(features, modelLoader.getModel(ModelType.RANDOM_FOREST));
//
//        System.out.println(predict);
//    }

//    @Test
//    public void generateTFRecord() {
//        List<Float> featureList = List.of(0.212474116F,0.038163178F,0.216774194F,0.199120235F,0.236480938F,0.037360704F,2.147005741F,7.439587714F,0.865698284F,0.254843906F,0.203225806F,0.212474116F,0.156494605F,0.01793722F,0.23880597F,0.209472656F,0.171875F,0.2421875F,0.0703125F, 0.385185185F);
//
//        FloatNdArray ndArray = NdArrays.ofFloats(Shape.of(20));
//
//        for (int i = 0; i < featureList.size(); i++) {
//            ndArray.set(NdArrays.scalarOf(featureList.get(i)), i);
//        }
//
//        try (EagerSession session = EagerSession.create()) {
//            Ops tf = Ops.create(session);
//            Scope scope = tf.scope();
//            try (TFloat32 tensor = TFloat32.tensorOf(ndArray)) {
//                ListDataset dataset = ListDataset.create(scope, List.of(tf.constant(tensor)), List.of(TFloat32.class), List.of(Shape.of(20)));
//                DatasetToTfRecord record = DatasetToTfRecord.create(scope, dataset, tf.constant("./record.rd"), tf.constant(""));
//            }
//        }
//
//    }

}

