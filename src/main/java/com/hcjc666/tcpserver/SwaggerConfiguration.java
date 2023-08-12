package com.hcjc666.tcpserver;

import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import springfox.documentation.builders.PathSelectors;
import springfox.documentation.spi.DocumentationType;
import springfox.documentation.spring.web.plugins.Docket;

@Configuration
public class SwaggerConfiguration {    //注意标黄的地方要求能匹配到你所有的接口路径，比如我的例子中两个接口都是以demo开始的，/demo/mockInvoke和/demo/mockInvoke2
    @Bean
    Docket swaggerConfig() {
        return new Docket(DocumentationType.OAS_30).useDefaultResponseMessages(false)
                .produces(Stream.of("application/xml", "application/json").collect(Collectors.toSet())).select()
                .paths(PathSelectors.regex("/tcp/.*")).build()
                .protocols(Stream.of("http", "https").collect(Collectors.toSet()));
    }
}
