package com.bookmanager.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.util.Arrays;
import java.util.List;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Autowired
    private JwtInterceptor jwtInterceptor;

    /**
     * 无需登录即可访问的公开接口（仅限只读/认证/注册）。
     */
    private static final List<String> EXCLUDE_PATHS = Arrays.asList(
            "/api/user/login",
            "/api/user/register",
            // 图书只读接口
            "/api/book/list",
            "/api/book/page",
            "/api/book/search",
            "/api/book/category/*",
            // 分类只读接口
            "/api/category/list",
            "/api/category/tree",
            "/api/category/enabled"
    );

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/**")
                // 仅允许前端开发服务器跨域（生产环境应替换为前端实际域名）
                .allowedOriginPatterns("http://localhost:5173", "http://127.0.0.1:5173")
                .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS")
                .allowedHeaders("*")
                .allowCredentials(true);
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(jwtInterceptor)
                .addPathPatterns("/api/**")
                .excludePathPatterns(EXCLUDE_PATHS);
    }
}
