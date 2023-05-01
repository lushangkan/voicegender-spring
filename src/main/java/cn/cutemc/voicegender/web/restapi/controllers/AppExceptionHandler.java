package cn.cutemc.voicegender.web.restapi.controllers;

import cn.cutemc.voicegender.analyze.Analyze;
import cn.cutemc.voicegender.analyze.beans.AnalyzeProperty;
import cn.cutemc.voicegender.analyze.status.AnalyzeStatus;
import cn.cutemc.voicegender.io.caches.AnalyzeCache;
import cn.cutemc.voicegender.io.database.LogService;
import cn.cutemc.voicegender.utils.UUIDUtils;
import cn.cutemc.voicegender.web.restapi.controllers.returners.ReturnRecord;
import cn.cutemc.voicegender.web.restapi.controllers.returners.StatusRecord;
import cn.cutemc.voicegender.web.restapi.status.RequestStatus;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.apachecommons.CommonsLog;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.multipart.MaxUploadSizeExceededException;
import org.springframework.web.multipart.support.MissingServletRequestPartException;

import java.io.IOException;
import java.nio.file.Files;
import java.util.Date;
import java.util.UUID;

@RestControllerAdvice
@CommonsLog
public class AppExceptionHandler {

    private final LogService logService;
    private final AnalyzeCache analyzeCache;

    @Autowired
    public AppExceptionHandler(LogService logService, AnalyzeCache analyzeCache) {
        this.logService = logService;
        this.analyzeCache = analyzeCache;
    }

    @ExceptionHandler(Throwable.class)
    public Object exceptionHandler(Throwable throwable, HttpServletResponse response, HttpServletRequest request) {

        UUID uuid = UUIDUtils.getFormPath(request.getRequestURI());

        Long filesize = null;
        AnalyzeStatus analyzeStatus = null;

        if (uuid != null) {
            Analyze analyze = analyzeCache.getByKey(uuid);
            if (analyze != null) {
                AnalyzeProperty property = analyze.getProperty();
                analyzeStatus = property.analyzeStatus();
                try {
                    filesize = Files.size(property.uploadFile());
                } catch (IOException ex) {
                    log.error("Unable to get file size", ex);
                }
            }
        }

        logService.logError(new Date(), request.getRemoteAddr(), uuid, request.getRequestURI(), filesize, analyzeStatus, throwable);

        log.error("Exception occurred, Path: " + request.getRequestURI() + ", UUID: " + uuid + ", Addr: " + request.getRemoteAddr(), throwable);

        return null;
    }

    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ExceptionHandler(MissingServletRequestPartException.class)
    public Object handleMissingServletRequestPartException(MissingServletRequestPartException ex) {
        RequestStatus status = RequestStatus.REQUEST_PARAMETER_ERROR;
        return new ReturnRecord.Error(new StatusRecord.RequestStatus(status.getStatusCode(), status.getMessage()));
    }

    @ExceptionHandler(MaxUploadSizeExceededException.class)
    public Object handleMaxUploadSizeExceededException(MaxUploadSizeExceededException ex, HttpServletResponse response, HttpServletRequest request) {
        return new ReturnRecord.Error(new StatusRecord.RequestStatus(RequestStatus.FILE_SIZE_EXCEEDS_LIMIT.getStatusCode(), RequestStatus.FILE_SIZE_EXCEEDS_LIMIT.getMessage()));
    }

}
