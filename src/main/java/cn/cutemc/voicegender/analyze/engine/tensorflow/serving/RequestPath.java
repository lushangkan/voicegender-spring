package cn.cutemc.voicegender.analyze.engine.tensorflow.serving;

import lombok.Getter;

@Getter
public enum RequestPath {

    MODEL_STATUS("/v1/models/<model>", RequestType.GET),
    PREDICT("/v1/models/<model>:predict", RequestType.POST);

    private final String path;
    private final RequestType type;

    RequestPath(String path, RequestType type) {
        this.path = path;
        this.type = type;
    }

}
