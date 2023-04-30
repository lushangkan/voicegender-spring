package cn.cutemc.voicegender.analyze.status;

import lombok.Getter;

@Getter
public enum AnalyzeStatus {
    WAITING(110, "waiting"),
    CONVERTING(111, "converting file type"),
    PREDICT_AUDIO_FEATURES(112, "predict audio features"),
    ANALYZE_AUDIO_FEATURES(113, "analyzing audio features"),
    FINISHED(120, "finished"),
    FAILED(-1, "failed");

    private final int statusCode;
    private final String message;

    AnalyzeStatus(int statusCode, String message) {
        this.statusCode = statusCode;
        this.message = message;
    }

    @Override
    public String toString() {
        return "{" +
                "\"statusCode\":" + statusCode +
                ", \"message\":\"" + message + '\"' +
                '}';
    }
}
