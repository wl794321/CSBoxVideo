package wiki.csbox.system.config;


import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import springfox.documentation.builders.ApiInfoBuilder;
import springfox.documentation.builders.PathSelectors;
import springfox.documentation.builders.RequestHandlerSelectors;
import springfox.documentation.service.ApiInfo;
import springfox.documentation.service.Contact;
import springfox.documentation.spi.DocumentationType;
import springfox.documentation.spring.web.plugins.Docket;
import springfox.documentation.swagger2.annotations.EnableSwagger2WebMvc;

/**
 * @author Krian
 * @version 1.0
 * @description: TODO 在线API文档配置类
 * @date 2023/3/17 0017 15:35
 */
@Configuration
@EnableSwagger2WebMvc
public class Knife4jConfig {

    @Bean
    public Docket adminApiConfig() {    // Docket 文档对象
        return new Docket(DocumentationType.SWAGGER_2)
                .groupName("接口文档")    // API 分组管理
                .apiInfo(adminApiInfo())
                .select()               // 设置分组过滤器
                //这里指定Controller扫描包路径
                .apis(RequestHandlerSelectors.basePackage("wiki.csbox"))
                .paths(PathSelectors.any())
                .build();
    }

    // API文档信息配置：
    private ApiInfo adminApiInfo() {
        return new ApiInfoBuilder()
                .title("学习社区视频内容管理系统")
                .description("内容系统管理系统对课程相关信息进行业务管理数据")
                .version("1.0")
                .contact(new Contact("爱吃糖的范同学", "http://csbox.wiki", "2793260947@qq.com"))  // 联系人
                .build();
    }
}
