package cn.cutemc.voicegender.web.restapi.controllers.returners;

import cn.cutemc.voicegender.analyze.Labels;
import cn.cutemc.voicegender.analyze.ModelType;

import java.util.Map;
import java.util.UUID;

public class ReturnRecord  {

    public record Request(StatusRecord.RequestStatus status) {}

    public record Analyze(StatusRecord.RequestStatus status, String analyzeUUID) {}

    public record Error(StatusRecord.RequestStatus status) {}

    public record AnalyzeStatus(StatusRecord.RequestStatus status, StatusRecord.AnalyzeStatus analyzeStatus) {}

    public record AnalyzeResult(StatusRecord.RequestStatus status, UUID uuid, Map<ModelType, Map<Labels, Double>> modelResult) {}

}
