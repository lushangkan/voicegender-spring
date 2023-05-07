package cn.cutemc.voicegender.web.restapi.interceptors;

import cn.cutemc.voicegender.io.database.LogService;
import cn.cutemc.voicegender.utils.HttpUtils;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.NonNull;
import lombok.extern.apachecommons.CommonsLog;
import org.springframework.web.servlet.HandlerInterceptor;

import java.util.Date;
import java.util.Enumeration;

@CommonsLog
public class RequestInterceptor implements HandlerInterceptor {

    private final LogService logService;

    public RequestInterceptor(LogService logService) {
        this.logService = logService;
    }

    @Override
    public boolean preHandle(HttpServletRequest request, @NonNull HttpServletResponse response, @NonNull Object handler) throws Exception {

        logService.logAccess(new Date(), HttpUtils.getRemoteAddr(request), request.getRequestURI(), request.getHeader("User-Agent"));

        log.info("Request from " + HttpUtils.getRemoteAddr(request) + " to " + request.getRequestURI() + " with UA: " + request.getHeader("User-Agent"));

        return true;
    }
}
