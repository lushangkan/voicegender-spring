package cn.cutemc.voicegender.web.restapi.controllers;

import cn.cutemc.voicegender.web.restapi.controllers.returners.ReturnRecord;
import cn.cutemc.voicegender.web.restapi.controllers.returners.StatusRecord;
import cn.cutemc.voicegender.web.restapi.status.RequestStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class StatusRestController {

    @GetMapping("/status")
    public Object status() {
        RequestStatus status = RequestStatus.OK;
        return new ReturnRecord.Request(new StatusRecord.RequestStatus(status.getStatusCode(), status.getMessage()));
    }

}
