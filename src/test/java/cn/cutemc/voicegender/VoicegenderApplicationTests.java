package cn.cutemc.voicegender;

import cn.cutemc.voicegender.analyze.Analyze;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ConfigurableApplicationContext;

@SpringBootTest
class VoicegenderApplicationTests {

    @Autowired
    Analyze analyze;

    @Autowired
    ConfigurableApplicationContext context;


    @Test
    void contextLoads() {
    }
}
