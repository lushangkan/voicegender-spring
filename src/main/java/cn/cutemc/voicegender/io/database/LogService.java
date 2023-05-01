package cn.cutemc.voicegender.io.database;

import cn.cutemc.voicegender.analyze.beans.AnalyzeProperty;
import cn.cutemc.voicegender.analyze.status.AnalyzeStatus;
import cn.cutemc.voicegender.io.database.entities.AccessLog;
import cn.cutemc.voicegender.io.database.entities.AnalyzeLog;
import cn.cutemc.voicegender.io.database.entities.ErrorLog;
import cn.cutemc.voicegender.io.database.repositories.AccessRepository;
import cn.cutemc.voicegender.io.database.repositories.AnalyzeRepository;
import cn.cutemc.voicegender.io.database.repositories.ErrorRepository;
import cn.cutemc.voicegender.utils.TimeUtils;
import cn.cutemc.voicegender.utils.UUIDUtils;
import lombok.extern.apachecommons.CommonsLog;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Example;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.Objects;
import java.util.UUID;

@CommonsLog
@Service
public class LogService {

    private final AccessRepository accessRepository;
    private final AnalyzeRepository analyzeRepository;
    private final ErrorRepository errorRepository;

    @Autowired
    public LogService(AccessRepository accessRepository, AnalyzeRepository analyzeRepository, ErrorRepository errorRepository) {
        this.accessRepository = accessRepository;
        this.analyzeRepository = analyzeRepository;
        this.errorRepository = errorRepository;
    }

    /**
     * 记录访问日志
     * @param time 访问时间
     * @param addr 访问地址
     * @param path 访问路径
     * @param UA   访问UA
     */
    public void logAccess(String time, String addr, String path, String UA) {
        accessRepository.save(new AccessLog(time, addr, path, UA));
    }

    /**
     * 记录访问日志
     * @param time 访问时间
     * @param addr 访问地址
     * @param path 访问路径
     * @param UA   访问UA
     */
    public void logAccess(Date time, String addr, String path, String UA) {
        accessRepository.save(new AccessLog(TimeUtils.formatTime(time), addr, path, UA));
    }

    /**
     * 记录分析日志
     * @param time 访问时间
     * @param addr 访问地址
     * @param uuid 访问UUID
     * @param uploadFileSize 上传文件大小
     * @param status 分析状态
     * @param UA  访问UA
     */
    public void logAnalyze(String time, String addr, String uuid, String uploadFileSize, String status, String UA) {
        analyzeRepository.save(new AnalyzeLog(time, addr, uuid, uploadFileSize, status, UA));
    }

    /**
     * 记录分析日志
     * @param time 访问时间
     * @param addr 访问地址
     * @param uuid 访问UUID
     * @param uploadFileSize 上传文件大小
     * @param analyzeStatus 分析状态
     * @param UA  访问UA
     */
    public void logAnalyze(Date time, String addr, UUID uuid, Long uploadFileSize, AnalyzeStatus analyzeStatus, String UA) {
        analyzeRepository.save(new AnalyzeLog(TimeUtils.formatTime(time), addr, UUIDUtils.uuidToString(uuid), FileUtils.byteCountToDisplaySize(uploadFileSize), analyzeStatus.toString(), UA));
    }

    /**
     * 更新分析状态
     * @param uuid 访问UUID
     * @param analyzeStatus 分析状态
     */
    public void updateAnalyzeStatus(UUID uuid, AnalyzeStatus analyzeStatus) {
        Example<AnalyzeLog> example = Example.of(new AnalyzeLog(null, null, UUIDUtils.uuidToString(uuid), null, null, null));
        analyzeRepository.findOne(example).ifPresent(analyzeLog -> {
            analyzeLog.setStatus(analyzeStatus.toString());
            analyzeRepository.save(analyzeLog);
        });
    }

    /**
     * 更新分析状态
     * @param property 分析配置
     */
    public void updateAnalyzeStatus(AnalyzeProperty property) {
        updateAnalyzeStatus(property.uuid(), property.analyzeStatus());
    }

    /**
     * 记录错误日志
     * @param time 访问时间
     * @param addr 访问地址
     * @param uuid 访问UUID
     * @param path 访问路径
     * @param uploadFileSize 上传文件大小
     * @param analyzeStatus 分析状态
     * @param exception 异常信息
     */
    public void logError(String time, String addr, String uuid, String path,  String uploadFileSize, String analyzeStatus, String exception) {
        errorRepository.save(new ErrorLog(time, addr, uuid, path, uploadFileSize, analyzeStatus, exception));
    }

    /**
     * 记录错误日志
     * @param time 访问时间
     * @param addr 访问地址
     * @param uuid 访问UUID
     * @param path 访问路径
     * @param uploadFileSize 上传文件大小
     * @param analyzeStatus 分析状态
     * @param throwable 异常信息
     */
    public void logError(Date time, String addr, UUID uuid, String path, Long uploadFileSize, AnalyzeStatus analyzeStatus, Throwable throwable) {
        errorRepository.save(new ErrorLog(TimeUtils.formatTime(time), addr, UUIDUtils.uuidToString(uuid), path, FileUtils.byteCountToDisplaySize(uploadFileSize), Objects.requireNonNullElse(analyzeStatus, "").toString(), ExceptionUtils.getRootCauseMessage(throwable)));
    }
}
