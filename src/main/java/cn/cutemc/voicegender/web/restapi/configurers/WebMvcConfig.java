package cn.cutemc.voicegender.web.restapi.configurers;

import cn.cutemc.voicegender.io.database.repositories.AccessRepository;
import cn.cutemc.voicegender.web.restapi.interceptors.BlockingHttpMethodInterceptor;
import cn.cutemc.voicegender.web.restapi.interceptors.RequestInterceptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebMvcConfig implements WebMvcConfigurer {

    private final AccessRepository accessRepository;

    @Autowired
    public WebMvcConfig(AccessRepository accessRepository) {
        this.accessRepository = accessRepository;
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(new BlockingHttpMethodInterceptor())
                .addPathPatterns("/**");

        registry.addInterceptor(new RequestInterceptor(accessRepository))
                .addPathPatterns("/**");
    }
}
