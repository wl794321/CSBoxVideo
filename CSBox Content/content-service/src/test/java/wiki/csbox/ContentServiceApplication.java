package wiki.csbox;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * @author Administrator
 * @version 1.0
 * @description: TODO
 * @date 2023/3/17 0017 16:38
 */
@SpringBootApplication
@MapperScan("wiki.csbox.contentservice.mapper")
public class ContentServiceApplication {
    public static void main(String[] args) {
        SpringApplication.run(ContentServiceApplication.class, args);
    }
}
