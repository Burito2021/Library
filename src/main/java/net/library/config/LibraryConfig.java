package net.library.config;

import net.library.config.parsers.BookItemStatusTypeParser;
import net.library.config.parsers.ModerationTypeParser;
import net.library.config.parsers.RoleTypeTypeParser;
import net.library.config.parsers.UserStateTypeParser;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.web.PageableHandlerMethodArgumentResolver;
import org.springframework.format.FormatterRegistry;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.method.HandlerTypePredicate;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.servlet.config.annotation.PathMatchConfigurer;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.util.List;

import static net.library.util.HttpUtil.GLOBAL_BASE_URI;

@Configuration
public class LibraryConfig implements WebMvcConfigurer {

    @Override
    public void configurePathMatch(PathMatchConfigurer configurer) {
        configurer.addPathPrefix(GLOBAL_BASE_URI, HandlerTypePredicate.forAnnotation(RestController.class));
    }

    @Override
    public void addFormatters(FormatterRegistry registry) {
        registry.addConverter(new ModerationTypeParser());
        registry.addConverter(new RoleTypeTypeParser());
        registry.addConverter(new UserStateTypeParser());
        registry.addConverter(new BookItemStatusTypeParser());
    }

    @Override
    public void addArgumentResolvers(List<HandlerMethodArgumentResolver> argumentResolver) {
        var resolver = new PageableHandlerMethodArgumentResolver();

        resolver.setMaxPageSize(100);
        argumentResolver.add(resolver);
    }
}
