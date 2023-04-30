package cn.cutemc.voicegender.web.restapi.aspects;

import cn.cutemc.voicegender.core.configs.MainConfig;
import cn.cutemc.voicegender.io.caches.RequesterCache;
import cn.cutemc.voicegender.utils.AspectUtils;
import cn.cutemc.voicegender.web.restapi.beans.Requester;
import cn.cutemc.voicegender.web.restapi.controllers.returners.ReturnRecord;
import cn.cutemc.voicegender.web.restapi.controllers.returners.StatusRecord;
import cn.cutemc.voicegender.web.restapi.status.RequestStatus;
import jakarta.servlet.http.HttpServletRequest;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Component
@Aspect
public class AnalyzeAspect {

    private final RequesterCache requesterCache;

    private final MainConfig config;

    @Autowired
    public AnalyzeAspect(RequesterCache requesterCache, MainConfig config) {
        this.requesterCache = requesterCache;
        this.config = config;
    }

    @Around(value = "execution(public * cn.cutemc.voicegender.web.restapi.controllers.AnalyzeRestController.analyze(..)))")
    public Object beforeAnalyze(ProceedingJoinPoint joinPoint) throws Throwable {

        HttpServletRequest req = AspectUtils.getParamByName(joinPoint, "req", HttpServletRequest.class);

        if (req == null) throw new RuntimeException("Req is null!");

        String remoteAddr = req.getRemoteAddr();

        if (remoteAddr == null) throw new RuntimeException("RemoteAddr is null!");

        Requester requester = requesterCache.getByKey(remoteAddr);

        if (requester != null) {
            List<Date> requestTimes =  requester.requestTimes();
            List<Date> validRequestTimes = new ArrayList<>();
            for (Date requestTime : requestTimes) {
                if (new Date().getTime() - requestTime.getTime() < 1000 * 60 * 30) {
                    validRequestTimes.add(requestTime);
                }
            }

            if (validRequestTimes.size() >= config.getHourMaximum()) {
                //超出限制
                RequestStatus status = RequestStatus.HOURLY_REQUEST_LIMIT_EXCEEDED;

                return new ReturnRecord.Analyze(new StatusRecord.RequestStatus(status.getStatusCode(), status.getMessage()), "");
            } else {
                validRequestTimes.add(new Date());
                requesterCache.update(new Requester(remoteAddr, validRequestTimes));
            }


        } else {
            List<Date> requestTimes = new ArrayList<>();
            requestTimes.add(new Date());
            requesterCache.update(new Requester(remoteAddr, requestTimes));
        }

        return joinPoint.proceed();
    }

}
