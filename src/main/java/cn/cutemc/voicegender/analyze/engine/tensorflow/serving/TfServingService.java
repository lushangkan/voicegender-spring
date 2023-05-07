package cn.cutemc.voicegender.analyze.engine.tensorflow.serving;

import cn.cutemc.voicegender.analyze.Features;
import cn.cutemc.voicegender.analyze.Labels;
import cn.cutemc.voicegender.analyze.ModelType;
import cn.cutemc.voicegender.analyze.engine.EngineType;
import cn.cutemc.voicegender.core.configs.MainConfig;
import cn.cutemc.voicegender.utils.AnalyzeUtils;
import cn.cutemc.voicegender.utils.HttpUtils;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import lombok.extern.apachecommons.CommonsLog;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@EnableScheduling
@CommonsLog
public class TfServingService {

    private final String servingUrl;
    private final MainConfig config;

    @Autowired
    public TfServingService(MainConfig config) {
        log.info("Initializing TensorFlow Serving service...");

        this.config = config;

        if (config.isServingHttps()) {
            servingUrl = "https://" + config.getServingAddress() + ":" + config.getServingPort();
        } else {
            servingUrl = "http://" + config.getServingAddress() + ":" + config.getServingPort();
        }

        //Check server status
        try {
            getStatus().forEach((key, value) -> {
                if (!value) {
                    log.error("TensorFlow Serving is not ready! Model " + key.getId() + " is not available.");
                }
            });
        } catch (Exception e) {
            log.error("TensorFlow Serving is not ready!");
            throw new RuntimeException(e);
        }
    }

    @Scheduled(cron = "0 0/${analyze.tf-serving.check-interval} * * * ? ")
    public void watchdog() {
        getStatus().forEach((key, value) -> {
            if (!value) {
                log.error("TensorFlow Serving is not ready! Model " + key.getId() + " is not available.");
            }
        });
    }

    public Map<ModelType, Boolean> getStatus() {
        log.info("Checking TensorFlow Serving status...");

        Map<ModelType, Boolean> status = new HashMap<>();

        ModelType[] models = ModelType.values();

        for (ModelType model : models) {

            if (model.getEngineType() != EngineType.TENSORFLOW) continue;

            String response;

            String requestUri = servingUrl + RequestPath.MODEL_STATUS.getPath().replaceAll("<model>", model.getId());

            try {
                response = HttpUtils.get(requestUri, config.getServingReconnectionTimes());
            } catch (RestClientException e) {
                log.error("Could not connect to TensorFlow Serving!");
                throw new RuntimeException();
            }

            //解析到JSON
            Gson gson = new Gson();
            JsonObject jsonObject = gson.fromJson(response, JsonObject.class);

            String statusString = jsonObject.get("model_version_status").getAsJsonArray().get(0).getAsJsonObject().get("state").getAsString();

            status.put(model, statusString.equals("AVAILABLE"));
        }

        return status;
    }

    public Map<Labels, Double> predict(ModelType model, Map<Features, ?> features) {

        if (model.getEngineType() != EngineType.TENSORFLOW) {
            log.error("Model " + model.getId() + " is not a TensorFlow model!");
            throw new RuntimeException();
        }

        Map<Features, Double> doubleFeatures = AnalyzeUtils.toDoubleMap(features);

        String requestUri = servingUrl + RequestPath.PREDICT.getPath().replaceAll("<model>", model.getId());

        //构造请求
        Gson gson = new Gson();

        String body = gson.toJson(Map.of("instances", List.of(AnalyzeUtils.featureToList(doubleFeatures))));

        String response;

        //发送请求
        try {
            response = HttpUtils.post(requestUri, body, config.getServingReconnectionTimes());
        } catch (RestClientException e) {
            log.error("Could not connect to TensorFlow Serving!");
            throw new RuntimeException(e);
        }

        //解析到JSON
        JsonObject jsonObject = gson.fromJson(response, JsonObject.class);

        double masculine = jsonObject.get("predictions").getAsJsonArray().get(0).getAsJsonArray().get(0).getAsDouble();

        return Map.of(
                Labels.MASCULINE, masculine,
                Labels.FEMININE, (1 - masculine));

    }

}
