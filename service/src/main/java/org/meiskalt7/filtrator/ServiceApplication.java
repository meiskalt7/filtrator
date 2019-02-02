package org.meiskalt7.filtrator;

import org.meiskalt7.filtrator.config.ServiceProperties;
import org.meiskalt7.filtrator.filter.ClientFilter;
import org.meiskalt7.filtrator.filter.LoginFilter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.context.TypeExcludeFilter;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cloud.client.SpringCloudApplication;
import org.springframework.cloud.netflix.eureka.EnableEurekaClient;
import org.springframework.cloud.netflix.hystrix.EnableHystrix;
import org.springframework.cloud.netflix.zuul.EnableZuulProxy;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.ComponentScan.Filter;
import org.springframework.context.annotation.FilterType;

@SpringCloudApplication
@ComponentScan(basePackages = {"org.meiskalt7"}, excludeFilters = @Filter(type = FilterType.CUSTOM, classes = TypeExcludeFilter.class))
@EnableEurekaClient
@EnableHystrix
@EnableZuulProxy
@EnableConfigurationProperties(ServiceProperties.class)
public class ServiceApplication {

    public static void main(String[] args) {
        new SpringApplicationBuilder(ServiceApplication.class).build().run(args);
    }

    @Bean
    @Autowired
    public ClientFilter clientFilter(ServiceProperties serviceProperties) {
        return new ClientFilter(serviceProperties);
    }

    @Bean
    @Autowired
    public LoginFilter loginFilter(ServiceProperties serviceProperties) {
        return new LoginFilter(serviceProperties);
    }
}