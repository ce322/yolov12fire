package com.zhuanghd.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * 配置静态资源映射
 */
@Configuration
public class WebConfig implements WebMvcConfigurer {
    // 通过读取配置项获取的文件上传路径
    @Value("${vd.upload.uploadFolder}")
    private String basePath;

    /**
     * 配置静态资源映射
     * @param registry 资源注册器
     */
    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {

        /*
         * 资源映射路径
         * addResourceHandler:访问映射路径
         * addResourceLocations:资源绝对路径
         */
        registry.addResourceHandler("/upload/**").addResourceLocations("file:" + basePath);

        // 添加缩略图访问映射
        registry.addResourceHandler("/thumbnails/**")
                .addResourceLocations("file:E:/vddt/thumbnails/");

        // 添加视频文件访问映射
        registry.addResourceHandler("/videos/**")
                .addResourceLocations("file:E:/vddt/");
    }
}

