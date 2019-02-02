package org.meiskalt7.filtrator.config;

import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.validation.annotation.Validated;

import javax.validation.constraints.NotEmpty;
import java.util.Set;

@ConfigurationProperties("app")
@Slf4j
@Validated
@Getter
@Setter
@RefreshScope
public class ServiceProperties {
    @NotEmpty
    private Set<String> allowedLogins;
    @NotEmpty
    private Set<String> microservicesForLoginFiltration;
    @NotEmpty
    private Set<String> microservicesForClientFiltration;
}
