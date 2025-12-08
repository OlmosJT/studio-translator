package com.platform.studiotranslator;

import com.platform.studiotranslator.util.ApplicationUtils;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.web.config.EnableSpringDataWebSupport;

import static org.springframework.data.web.config.EnableSpringDataWebSupport.PageSerializationMode.VIA_DTO;

@SpringBootApplication
@EnableSpringDataWebSupport(pageSerializationMode = VIA_DTO)
public class StudioTranslatorApplication {

    public static void main(String[] args) {
        var application = new SpringApplication(StudioTranslatorApplication.class);
        var env = application.run(args).getEnvironment();
        ApplicationUtils.logApplicationStartup(env);
    }

}
