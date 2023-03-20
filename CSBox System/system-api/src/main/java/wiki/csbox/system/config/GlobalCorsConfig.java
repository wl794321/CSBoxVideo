package wiki.csbox.system.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.filter.CorsFilter;

/**
 * @author Krian
 * @version 1.0
 * @description: TODO 解决基于浏览器端请求跨域问题
 * @date 2023/3/18 0018 10:13
 */
@Configuration
public class GlobalCorsConfig {

    @Bean
    public CorsFilter corsFilter() {
        // 创建跨域访问配置类：
        CorsConfiguration corsConfiguration = new CorsConfiguration();
        // 添加允许跨域访问站点：
        corsConfiguration.addAllowedOrigin("*");
        // 允许跨域发送Cookie：
        corsConfiguration.setAllowCredentials(true);
        // 放行所有请求头信息：
        corsConfiguration.addAllowedHeader("*");
        // 允许所有请求跨域调用：
        corsConfiguration.addAllowedMethod("*");

        // 设置跨域请求：
        UrlBasedCorsConfigurationSource urlBasedCorsConfigurationSource = new UrlBasedCorsConfigurationSource();
        urlBasedCorsConfigurationSource.registerCorsConfiguration("/**", corsConfiguration);

        return new CorsFilter(urlBasedCorsConfigurationSource);
    }
}
