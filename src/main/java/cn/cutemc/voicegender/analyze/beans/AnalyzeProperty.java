package cn.cutemc.voicegender.analyze.beans;

import cn.cutemc.voicegender.analyze.Features;
import cn.cutemc.voicegender.analyze.Labels;
import cn.cutemc.voicegender.analyze.ModelType;
import cn.cutemc.voicegender.analyze.status.AnalyzeStatus;
import lombok.NonNull;

import java.io.Serializable;
import java.nio.file.Path;
import java.util.Date;
import java.util.Map;
import java.util.UUID;

public record AnalyzeProperty(@NonNull UUID uuid, @NonNull Date time, Path uploadFile, Path tmpFile, AnalyzeStatus analyzeStatus, Map<Features, Double> analyzeFeatures, Map<ModelType, Map<Labels, Double>> modelResult) implements Serializable {

}
