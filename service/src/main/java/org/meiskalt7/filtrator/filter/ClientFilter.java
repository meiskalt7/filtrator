package org.meiskalt7.filtrator.filter;

import org.meiskalt7.filtrator.config.ServiceProperties;
import com.netflix.zuul.ZuulFilter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.netflix.zuul.util.ZuulRuntimeException;

import java.io.IOException;

import static org.meiskalt7.filtrator.filter.FilterUtils.createZuulForbiddenException;
import static org.meiskalt7.filtrator.filter.FilterUtils.isContainsLogin;
import static org.springframework.cloud.netflix.zuul.filters.support.FilterConstants.PRE_TYPE;

/**
 * check that login/clientId in request is allowed
 */
@Slf4j
public class ClientFilter extends ZuulFilter {

    private static final String ERROR_MESSAGE = "Сервис в режиме бета-тестирования недоступен по заданным параметрам";

    private ServiceProperties serviceProperties;

    @Autowired
    public ClientFilter(ServiceProperties serviceProperties) {
        this.serviceProperties = serviceProperties;
    }

    @Override
    public String filterType() {
        return PRE_TYPE;
    }

    @Override
    public int filterOrder() {
        return 1;
    }

    @Override
    public boolean shouldFilter() {
        return serviceProperties.getMicroservicesForClientFiltration().contains(FilterUtils.parseServiceName());
    }

    @Override
    public Object run() {
        try {
            Request request = new Request();
            log.debug("Request filtration to '{}' started", request.getServiceName());
            if (isContainsLogin(request)) {
                filter(request);
            }
        } catch (IOException e) {
            log.error(e.getMessage(), e);
        }
        return null;
    }

    private void filter(Request request) {
        if (!request.isPostRequestType() && request.getPath().contains("/client/")) {
            String login = request.getLoginFromPath();
            if (!serviceProperties.getAllowedLogins().contains(login)) {
                throw new ZuulRuntimeException(createZuulForbiddenException("login " + login + " is not allowed", ERROR_MESSAGE));
            }
        }
    }
}