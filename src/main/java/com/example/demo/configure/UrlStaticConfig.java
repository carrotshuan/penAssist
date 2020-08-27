package com.example.demo.configure;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.ViewControllerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
@EnableWebMvc
public class UrlStaticConfig implements WebMvcConfigurer{

    // 增加对静态资源的映射，Springboot2不能单纯使用改application.properties的方式，而是通过如下方式
    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        registry.addResourceHandler("/static/*").addResourceLocations("classpath:/static/");
    }
    // 如果需要对view返回则更改如下方法
    @Override
    public void addViewControllers(ViewControllerRegistry registry) {

    }
}
