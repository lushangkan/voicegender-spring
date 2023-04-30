package cn.cutemc.voicegender.web.restapi.controllers;

import cn.cutemc.voicegender.utils.TimeUtils;
import cn.cutemc.voicegender.web.restapi.controllers.returners.StatusRecord;
import cn.cutemc.voicegender.web.restapi.status.RequestStatus;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.boot.autoconfigure.web.ServerProperties;
import org.springframework.boot.autoconfigure.web.servlet.error.BasicErrorController;
import org.springframework.boot.web.error.ErrorAttributeOptions;
import org.springframework.boot.web.servlet.error.DefaultErrorAttributes;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

@Controller
@RequestMapping("${server.error.path:${error.path:/error}}")
public class AppErrorController extends BasicErrorController {

    public AppErrorController(ServerProperties serverProperties) {
        super(new DefaultErrorAttributes(), serverProperties.getError());
    }

    @Override
    public ResponseEntity<Map<String, Object>> error(HttpServletRequest request) {
        HttpStatus httpStatus = getStatus(request);
        ResponseEntity<Map<String, Object>> defaultResponse = super.error(request);
        Map<String, Object> errorAttributes = defaultResponse.getBody();
        if (errorAttributes == null) {
            errorAttributes = getErrorAttributes(request, ErrorAttributeOptions.of(ErrorAttributeOptions.Include.STACK_TRACE));
        }

        if (errorAttributes.get("trace") == null && httpStatus == HttpStatus.NOT_FOUND) {
            Map<String, Object> response = new HashMap<>();

            RequestStatus status = RequestStatus.INVALID_PATH;

            response.put("status", new StatusRecord.RequestStatus(status.getStatusCode(), status.getMessage()));

            response.put("path", errorAttributes.get("path"));

            return new ResponseEntity<>(response, HttpStatus.NOT_FOUND);
        } else if (errorAttributes.get("trace") != null) {
            Map<String, Object> response = new HashMap<>();
            RequestStatus status = RequestStatus.INTERNAL_SERVER_ERROR;
            response.put("time", TimeUtils.formatTime(new Date()));
            response.put("status", new StatusRecord.RequestStatus(status.getStatusCode(), status.getMessage()));
            response.put("path", errorAttributes.get("path"));
            return new ResponseEntity<>(response, HttpStatus.INTERNAL_SERVER_ERROR);
        }

        return defaultResponse;
    }

    @Override
    public ModelAndView errorHtml(HttpServletRequest request, HttpServletResponse response) {
        return super.errorHtml(request, response);
    }
}
