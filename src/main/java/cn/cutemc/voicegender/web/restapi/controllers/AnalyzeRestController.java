package cn.cutemc.voicegender.web.restapi.controllers;

import cn.cutemc.voicegender.analyze.Analyze;
import cn.cutemc.voicegender.analyze.AnalyzeController;
import cn.cutemc.voicegender.analyze.FFmpegService;
import cn.cutemc.voicegender.analyze.beans.AnalyzeProperty;
import cn.cutemc.voicegender.analyze.status.AnalyzeStatus;
import cn.cutemc.voicegender.core.configs.MainConfig;
import cn.cutemc.voicegender.io.caches.AnalyzeCache;
import cn.cutemc.voicegender.io.database.entities.AnalyzeLog;
import cn.cutemc.voicegender.io.database.repositories.AnalyzeRepository;
import cn.cutemc.voicegender.io.storages.StorageService;
import cn.cutemc.voicegender.utils.HttpUtils;
import cn.cutemc.voicegender.utils.TimeUtils;
import cn.cutemc.voicegender.utils.UUIDUtils;
import cn.cutemc.voicegender.web.restapi.controllers.returners.ReturnRecord;
import cn.cutemc.voicegender.web.restapi.controllers.returners.StatusRecord;
import cn.cutemc.voicegender.web.restapi.status.RequestStatus;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.apachecommons.CommonsLog;
import org.apache.commons.io.FileUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.nio.file.Path;
import java.util.Date;
import java.util.UUID;

@RestController
@CommonsLog
@ComponentScan(basePackages = "cn.cutemc.voicegender.storages")
public class AnalyzeRestController {

    private final StorageService storageService;
    private final AnalyzeCache analyzeCache;
    private final MainConfig config;
    private final FFmpegService ffmpeg;
    private final AnalyzeController controller;
    private final AnalyzeRepository repository;

    @Autowired
    public AnalyzeRestController(MainConfig config, StorageService storageService, AnalyzeCache analyzeCache, FFmpegService service, AnalyzeController controller, AnalyzeRepository repository) {
        this.config = config;
        this.storageService = storageService;
        this.analyzeCache = analyzeCache;
        this.ffmpeg = service;
        this.controller = controller;
        this.repository = repository;
    }

    @PostMapping("/analyze")
    public Object analyze(@RequestParam("file") MultipartFile file, HttpServletRequest req) {

        String fileName = file.getOriginalFilename();
        String suffix = fileName.split("\\.")[1];

        //判断文件类型是否为.mp3 .wav .wma .aac .flac .ape .ogg .amr .m4a .ra .ram .ac3 .dts .pcm
        if (!suffix.equals("mp3") && !suffix.equals("wav") && !suffix.equals("wma") && !suffix.equals("aac") && !suffix.equals("flac") && !suffix.equals("ape") && !suffix.equals("ogg") && !suffix.equals("amr") && !suffix.equals("m4a") && !suffix.equals("ra") && !suffix.equals("ram") && !suffix.equals("ac3") && !suffix.equals("dts") && !suffix.equals("pcm")) {
            RequestStatus status = RequestStatus.WRONG_FILE_FORMAT;
            return new ReturnRecord.Analyze(new StatusRecord.RequestStatus(status.getStatusCode(), status.getMessage()), "null");
        }

        UUID uuid = UUIDUtils.randomUUID();

        storageService.store(Path.of(config.getUploadPath()), file, uuid);

        try {
            double duration = ffmpeg.getDuration(storageService.load(Path.of(config.getUploadPath()), uuid));

            if (duration < 1.5) {
                //删除文件
                storageService.delete(Path.of(config.getUploadPath()), uuid);
                RequestStatus status = RequestStatus.TOO_SHORT;
                return new ReturnRecord.Analyze(new StatusRecord.RequestStatus(status.getStatusCode(), status.getMessage()), "null");
            } else if (duration > 15) {
                //删除文件
                storageService.delete(Path.of(config.getUploadPath()), uuid);
                RequestStatus status = RequestStatus.TOO_LONG;
                return new ReturnRecord.Analyze(new StatusRecord.RequestStatus(status.getStatusCode(), status.getMessage()), "null");
            }
        } catch (Exception e) {
            log.error("Failed to get audio duration");
            //删除文件
            storageService.delete(Path.of(config.getUploadPath()), uuid);
            throw new RuntimeException(e);
        }

        // 新建分析对象
        AnalyzeProperty properties = new AnalyzeProperty(uuid, new Date(), storageService.load(Path.of(config.getUploadPath()), uuid), null, AnalyzeStatus.WAITING, null, null);

        Analyze analyze = controller.createAnalyze(properties);

        analyze.start();

        // 保存到Log

        AnalyzeLog analyzeLog = new AnalyzeLog(TimeUtils.formatTime(new Date()), HttpUtils.getRemoteAddr(req), UUIDUtils.uuidToString(uuid), FileUtils.byteCountToDisplaySize(file.getSize()), properties.analyzeStatus().toString(), req.getHeader("User-Agent"));

        repository.save(analyzeLog);

        log.info("Start a new analyze, request from: " + HttpUtils.getRemoteAddr(req) + " with uuid: " + uuid + " UA: " + req.getHeader("User-Agent") + " file size: " + file.getSize() / 1024 / 1024 + "MB");

        RequestStatus status = RequestStatus.SUCCESS;

        return new ReturnRecord.Analyze(new StatusRecord.RequestStatus(status.getStatusCode(), status.getMessage()), uuid.toString());
    }

    @GetMapping("/analyze/{uuid}/status")
    public Object status(@PathVariable("uuid") UUID uuid, HttpServletRequest req) {
        Analyze analyze = analyzeCache.getByKey(uuid);

        if (analyze == null) {
            RequestStatus requestStatus = RequestStatus.ANALYZE_NOT_EXIST;
            return new ReturnRecord.Error(new StatusRecord.RequestStatus(requestStatus.getStatusCode(), requestStatus.getMessage()));
        }

        AnalyzeProperty property = analyze.getProperty();

        if (property == null) {
            RequestStatus status = RequestStatus.ANALYSIS_REGISTERING;
            return new ReturnRecord.Error(new StatusRecord.RequestStatus(status.getStatusCode(), status.getMessage()));
        }

        RequestStatus status = RequestStatus.SUCCESS;
        return new ReturnRecord.AnalyzeStatus(new StatusRecord.RequestStatus(status.getStatusCode(), status.getMessage()), new StatusRecord.AnalyzeStatus(property.analyzeStatus().getStatusCode(), property.analyzeStatus().getMessage()));
    }

    @GetMapping("/analyze/{uuid}/result")
    public Object result(@PathVariable("uuid") UUID uuid, HttpServletRequest req) {
        Analyze analyze = analyzeCache.getByKey(uuid);

        if (analyze == null) {
            RequestStatus requestStatus = RequestStatus.ANALYZE_NOT_EXIST;
            return new ReturnRecord.Error(new StatusRecord.RequestStatus(requestStatus.getStatusCode(), requestStatus.getMessage()));
        }

        AnalyzeProperty property = analyze.getProperty();

        if (property == null) {
            RequestStatus status = RequestStatus.ANALYSIS_REGISTERING;
            return new ReturnRecord.Error(new StatusRecord.RequestStatus(status.getStatusCode(), status.getMessage()));
        }

        if (property.analyzeStatus() == AnalyzeStatus.FINISHED) {
            RequestStatus status = RequestStatus.SUCCESS;
            return new ReturnRecord.AnalyzeResult(new StatusRecord.RequestStatus(status.getStatusCode(), status.getMessage()), uuid, property.modelResult());
        }

        RequestStatus status = RequestStatus.ANALYSIS_IN_PROGRESS;
        return new ReturnRecord.AnalyzeResult(new StatusRecord.RequestStatus(status.getStatusCode(), status.getMessage()), uuid, null);
    }

}
