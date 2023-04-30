package cn.cutemc.voicegender.web.restapi.controllers.returners;

public class StatusRecord {

    public record RequestStatus(int code, String message) {}

    public record AnalyzeStatus(int code, String message) {}

}
