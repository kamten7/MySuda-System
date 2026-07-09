package com.suda.config;

import com.suda.interceptor.JwtTokenAdminInterceptor;
import com.suda.interceptor.JwtTokenUserInterceptor;
import com.suda.json.JacksonObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurationSupport;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import org.springdoc.core.models.GroupedOpenApi;

import java.util.List;

/**
 * 配置类，注册web层相关组件
 */
@Configuration
@Slf4j
public class WebMvcConfiguration extends WebMvcConfigurationSupport {

    @Autowired
    private JwtTokenAdminInterceptor jwtTokenAdminInterceptor;

    @Autowired
    private JwtTokenUserInterceptor jwtTokenUserInterceptor;

    /**
     * 注册自定义拦截器
     *
     * @param registry
     */

    protected void addInterceptors(InterceptorRegistry registry) {
        log.info("开始注册自定义拦截器...");
        registry.addInterceptor(jwtTokenAdminInterceptor)
                .addPathPatterns("/admin/**")
                .excludePathPatterns("/admin/employee/login");
        //用户拦截器配置
        //前端发送请求（包含 URL + Header + Body）
        //拦截器先执行（preHandle 方法）
        //如果是登录接口 → 直接放行
        //如果是其他接口 → 校验 Token
        //然后才是 Controller 方法执行
        registry.addInterceptor(jwtTokenUserInterceptor)
                .addPathPatterns("/user/**")//拦截所有带/user/的请求
                .excludePathPatterns("/user/user/login")//用户登录接口排除
                .excludePathPatterns("/user/shop/status")//用户退出登录接口排除
                .excludePathPatterns("/user/order/historyOrders");

    }

    @Bean
    public OpenAPI openAPI() {
        log.info("开始创建接口文档...");
        return new OpenAPI()
                .info(new Info()
                        .title("速达外卖项目接口文档")
                        .version("2.0")
                        .description("速达外卖项目接口文档"));
    }

    @Bean
    public GroupedOpenApi adminApi() {
        return GroupedOpenApi.builder()
                .group("管理端接口")
                .pathsToMatch("/admin/**")
                .build();
    }

    @Bean
    public GroupedOpenApi userApi() {
        return GroupedOpenApi.builder()
                .group("用户端接口")
                .pathsToMatch("/user/**")
                .build();
    }

    /**
     * 扩展Spring mvc消息转换器
     * @param converters
     */

    protected void extendMessageConverters(List<HttpMessageConverter<?>> converters){
        log.info("扩展消息转换器...");
        //创建消息转换器对象
        MappingJackson2HttpMessageConverter converter = new MappingJackson2HttpMessageConverter();
        //需要为消息转换器设置一个对象转换器
        //将java对象序列化为json数据
        converter.setObjectMapper(new JacksonObjectMapper());
        //将自己的消息转换器加入容器中,0表示第一个位置
        converters.add(0,converter);
    }
}
