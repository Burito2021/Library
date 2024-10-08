package net.library.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.method.HandlerTypePredicate;
import org.springframework.web.servlet.config.annotation.PathMatchConfigurer;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import static net.library.util.HttpUtil.GLOBAL_BASE_URI;

@Configuration
public class LibraryConfig implements WebMvcConfigurer {

    @Override
    public void configurePathMatch(PathMatchConfigurer configurer) {
        configurer.addPathPrefix(GLOBAL_BASE_URI, HandlerTypePredicate.forAnnotation(RestController.class));
    }
}
