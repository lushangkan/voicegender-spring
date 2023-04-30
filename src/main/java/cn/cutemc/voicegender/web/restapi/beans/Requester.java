package cn.cutemc.voicegender.web.restapi.beans;


import lombok.NonNull;

import java.io.Serializable;
import java.util.Date;
import java.util.List;

public record Requester(@NonNull String addr, @NonNull List<Date> requestTimes) implements Serializable {

}
