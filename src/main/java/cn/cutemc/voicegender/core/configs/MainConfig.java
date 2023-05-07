package cn.cutemc.voicegender.core.configs;

import lombok.Data;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.stereotype.Component;

@Data
@Component
@Configuration
public class MainConfig {

    @Value("${server.bind-address}")
    private String bindAddress;

    @Value("${server.bind-port}")
    private int bindPort;

    @Value("${file.upload-path}")
    private String uploadPath;

    @Value("${file.tmp-path}")
    private String tmpPath;

    @Value("${file.upload-max-size}")
    private String uploadMaxSize;

    @Value("${file.clean-interval}")
    private long cleanInterval;

    @Value("${database.mysql.address}")
    private String databaseMysqlAddress;

    @Value("${database.mysql.port}")
    private String databaseMysqlPort;

    @Value("${database.mysql.username}")
    private String databaseMysqlUserName;

    @Value("${database.mysql.password}")
    private String databaseMysqlPassword;

    @Value("${database.mysql.database-name}")
    private String databaseMysqlName;

    @Value("${analyze.hour-maximum}")
    private long hourMaximum;

    @Value("${analyze.tf-serving.address}")
    private String servingAddress;

    @Value("${analyze.tf-serving.rest-api-port}")
    private int servingPort;

    @Value("${analyze.tf-serving.check-interval}")
    private int servingCheckInterval;

    @Value("${analyze.tf-serving.https}")
    private boolean servingHttps;

    @Value("${analyze.tf-serving.reconnection-times}")
    private int servingReconnectionTimes;

    @Value("${analyze.xgboost.model-file}")
    private String xgboostModelFile;

}
