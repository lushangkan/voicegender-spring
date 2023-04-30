package cn.cutemc.voicegender.web.restapi.controllers;

import cn.cutemc.voicegender.analyze.Analyze;
import cn.cutemc.voicegender.analyze.beans.AnalyzeProperty;
import cn.cutemc.voicegender.io.caches.AnalyzeCache;
import cn.cutemc.voicegender.io.database.entities.ErrorLog;
import cn.cutemc.voicegender.io.database.repositories.ErrorRepository;
import cn.cutemc.voicegender.utils.TimeUtils;
import cn.cutemc.voicegender.utils.UUIDUtils;
import cn.cutemc.voicegender.web.restapi.controllers.returners.ReturnRecord;
import cn.cutemc.voicegender.web.restapi.controllers.returners.StatusRecord;
import cn.cutemc.voicegender.web.restapi.status.RequestStatus;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.apachecommons.CommonsLog;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.exception.ExceptionUtils;
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

    private final ErrorRepository errorRepository;
    private final AnalyzeCache analyzeCache;

    @Autowired
    public AppExceptionHandler(ErrorRepository errorRepository, AnalyzeCache analyzeCache) {
        this.errorRepository = errorRepository;
        this.analyzeCache = analyzeCache;
    }

    @ExceptionHandler(Exception.class)
    public Object exceptionHandler(Exception e, HttpServletResponse response, HttpServletRequest request) {

        UUID uuid = UUIDUtils.getFormPath(request.getRequestURI());

        String filesize = "";
        String analyzeStatus = "";

        if (uuid != null) {
            Analyze analyze = analyzeCache.getByKey(uuid);
            if (analyze != null) {
                AnalyzeProperty property = analyze.getProperty();
                analyzeStatus = property.analyzeStatus().toString();
                try {
                    filesize = FileUtils.byteCountToDisplaySize(Files.size(property.uploadFile()));
                } catch (IOException ex) {
                    log.error("Unable to get file size", ex);
                }
            }
        }

        ErrorLog errorLog = new ErrorLog(TimeUtils.formatTime(new Date()), request.getRemoteAddr(), UUIDUtils.uuidToString(uuid), request.getRequestURI(), filesize, analyzeStatus, "");

        StringBuilder stackTrace = new StringBuilder();

        stackTrace.append(e.toString()).append("\n");

        for (Throwable throwable : ExceptionUtils.getThrowables(e)) {
            stackTrace.append(throwable.toString()).append("\n");
        }

        if (stackTrace.length() > 1500) {
            stackTrace = new StringBuilder(stackTrace.substring(0, 1499));
        }

        errorLog.setException(stackTrace.toString());

        errorRepository.save(errorLog);

        log.error("Exception occurred, Path: " + request.getRequestURI() + ", UUID: " + uuid + ", Addr: " + request.getRemoteAddr(), e);

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
