package cn.cutemc.voicegender.web.restapi.status;

import lombok.Getter;

@Getter
public enum RequestStatus {

    SUCCESS(200, "success"),
    OK(200, "OK"),
    WRONG_FILE_FORMAT(100, "Wrong file type, the file type must be .mp3 .wav .wma .aac .flac .ape .ogg .amr .m4a .ra .ram .ac3 .dts .pcm"),
    FILE_SIZE_EXCEEDS_LIMIT(101, "File size exceeds limit"),
    HOURLY_REQUEST_LIMIT_EXCEEDED(102, "Hourly request limit exceeded"),
    TOO_SHORT(103, "Audio duration is too short"),
    TOO_LONG(104, "Audio duration is too long"),
    ANALYSIS_IN_PROGRESS(105, "Analysis is in progress"),
    ANALYZE_NOT_EXIST(106, "Analyze not exist"),
    ANALYSIS_REGISTERING(107, "Analysis registration"),
    REQUEST_PARAMETER_ERROR(400, "Invalid parameter"),
    INVALID_PATH(404, "Invalid path"),
    INTERNAL_SERVER_ERROR(500, "Internal server error");

    private final int statusCode;
    private final String message;

    RequestStatus(int statusCode, String message) {
        this.statusCode = statusCode;
        this.message = message;
    }
}
