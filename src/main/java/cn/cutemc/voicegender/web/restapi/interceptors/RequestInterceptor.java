package cn.cutemc.voicegender.web.restapi.interceptors;

import cn.cutemc.voicegender.io.database.entities.AccessLog;
import cn.cutemc.voicegender.io.database.repositories.AccessRepository;
import cn.cutemc.voicegender.utils.TimeUtils;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.NonNull;
import lombok.extern.apachecommons.CommonsLog;
import org.springframework.web.servlet.HandlerInterceptor;

import java.util.Date;

@CommonsLog
public class RequestInterceptor implements HandlerInterceptor {

    private final AccessRepository repository;

    public RequestInterceptor(AccessRepository repository) {
        this.repository = repository;
    }

    @Override
    public boolean preHandle(HttpServletRequest request, @NonNull HttpServletResponse response, @NonNull Object handler) throws Exception {

        AccessLog accessLog = new AccessLog(TimeUtils.formatTime(new Date()), request.getRemoteAddr(), request.getRequestURI(), request.getHeader("User-Agent"));

        repository.save(accessLog);

        log.info("Request from " + request.getRemoteAddr() + " to " + request.getRequestURI() + " with UA: " + request.getHeader("User-Agent"));

        return true;
    }
}
