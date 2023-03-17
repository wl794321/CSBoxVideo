package wiki.csbox.contentservice.config.interceptor;

import com.baomidou.mybatisplus.annotation.DbType;
import com.baomidou.mybatisplus.extension.plugins.MybatisPlusInterceptor;
import com.baomidou.mybatisplus.extension.plugins.inner.PaginationInnerInterceptor;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * @author Krian
 * @version 1.0
 * @description: TODO MyBatisPlus 分页插件拦截器
 * @date 2023/3/17 0017 16:41
 */
@Configuration
@MapperScan("wiki.csbox.contentservice.mapper")
public class MyBatisPlusPageHelperInterceptor {

    /**
     * 转换SQL语句，实现分页操作
     *
     * @return MybatisPlusInterceptor 拦截器
     */
    @Bean
    public MybatisPlusInterceptor mybatisPlusInterceptor() {
        // 创建拦截器：
        MybatisPlusInterceptor interceptor = new MybatisPlusInterceptor();
        // 给拦截器设置MySQL方言：
        interceptor.addInnerInterceptor(new PaginationInnerInterceptor(DbType.MYSQL));
        return interceptor;
    }
}
