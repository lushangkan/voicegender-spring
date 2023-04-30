package cn.cutemc.voicegender.web.restapi.interceptors;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.NonNull;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.web.servlet.HandlerInterceptor;

import java.util.Objects;

public class BlockingHttpMethodInterceptor implements HandlerInterceptor {

    @Override
    public boolean preHandle(HttpServletRequest request, @NonNull HttpServletResponse response, @NonNull Object handler) throws Exception {

        if ((HttpMethod.GET.matches(request.getMethod()) || HttpMethod.POST.matches(request.getMethod())) && !Objects.requireNonNullElse(request.getHeader("Accept"), "").contains("text/html")) {
            return true;
        }

        response.sendError(HttpStatus.METHOD_NOT_ALLOWED.value());
        return false;
    }
}
