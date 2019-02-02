package org.meiskalt7.filtrator.filter;

import org.meiskalt7.filtrator.config.ServiceProperties;
import com.netflix.zuul.ZuulFilter;
import lombok.extern.slf4j.Slf4j;
import org.keycloak.KeycloakPrincipal;
import org.keycloak.KeycloakSecurityContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.netflix.zuul.util.ZuulRuntimeException;

import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.security.Principal;
import java.util.Objects;

import static org.meiskalt7.filtrator.filter.FilterUtils.createZuulForbiddenException;
import static org.meiskalt7.filtrator.filter.FilterUtils.isContainsLogin;
import static org.springframework.cloud.netflix.zuul.filters.support.FilterConstants.PRE_TYPE;

/**
 * check that login in gwt token same as login in request
 */
@Slf4j
public class LoginFilter extends ZuulFilter {

    private static final String ERROR_MESSAGE = "Неверный login, доступ запрещён";
    private static final String ERROR_MESSAGE_NO_KLOGIN = "Неавторизированный запрос";

    private ServiceProperties serviceProperties;

    @Autowired
    public LoginFilter(ServiceProperties serviceProperties) {
        this.serviceProperties = serviceProperties;
    }

    @Override
    public String filterType() {
        return PRE_TYPE;
    }

    @Override
    public int filterOrder() {
        return 0;
    }

    @Override
    public boolean shouldFilter() {
        return serviceProperties.getMicroservicesForLoginFiltration().contains(FilterUtils.parseServiceName());
    }

    @Override
    public Object run() {
        try {
            Request clientFilterRequest = new Request();
            if (isContainsLogin(clientFilterRequest)) {
                filter(clientFilterRequest);
            }
        } catch (IOException e) {
            log.error(e.getMessage(), e);
            throw new ZuulRuntimeException(e);
        }
        return null;
    }

    private void filter(Request clientFilterRequest) {
        String loginFromRequest = clientFilterRequest.getLogin();
        String loginFromAuthorizationToken = getLoginFromAuthorizationToken(clientFilterRequest.getServletRequest());

        if (!Objects.equals(loginFromAuthorizationToken, loginFromRequest)) {
            log.error("Login in request is not equals login in token.");
            throw new ZuulRuntimeException(
                    createZuulForbiddenException("Login in request is not equals login in token.", ERROR_MESSAGE)
            );
        }
    }

    private String getLoginFromAuthorizationToken(HttpServletRequest request) {
        Principal userPrincipal = request.getUserPrincipal();
        if (userPrincipal == null) {
            throw new ZuulRuntimeException(
                    createZuulForbiddenException("Token is not specified.", ERROR_MESSAGE_NO_KLOGIN)
            );
        }
        String loginFromToken = null;
        if (userPrincipal instanceof KeycloakPrincipal) {
            KeycloakPrincipal keycloakPrincipal = (KeycloakPrincipal) userPrincipal;
            KeycloakSecurityContext context = keycloakPrincipal.getKeycloakSecurityContext();
            if (Objects.isNull(context)) {
                log.error("Token has no information about login.");
                throw new ZuulRuntimeException(
                        createZuulForbiddenException("Token has no information about login.", ERROR_MESSAGE_NO_KLOGIN)
                );
            }
            loginFromToken = context.getToken().getPreferredUsername();
        }
        return loginFromToken;
    }
}