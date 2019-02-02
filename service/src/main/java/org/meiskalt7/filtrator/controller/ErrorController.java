package org.meiskalt7.filtrator.controller;

import java.util.Map;
import java.util.Objects;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.lang.StringUtils;
import org.keycloak.adapters.OIDCAuthenticationError;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.web.BasicErrorController;
import org.springframework.boot.autoconfigure.web.ErrorAttributes;
import org.springframework.boot.autoconfigure.web.ServerProperties;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import lombok.extern.slf4j.Slf4j;

@CrossOrigin(maxAge = 3600, methods = {RequestMethod.GET, RequestMethod.POST})
@Validated
@RestController
@Slf4j
public class ErrorController extends BasicErrorController {

    @Autowired
    public ErrorController(ErrorAttributes errorAttributes, ServerProperties serverProperties) {
        super(errorAttributes, serverProperties.getError());
    }

    @RequestMapping
    @ResponseBody
    @Override
    public ResponseEntity<Map<String, Object>> error(HttpServletRequest request) {
        logError(request);
        return super.error(request);
    }

    private void logError(HttpServletRequest request) {
        String reqId = request.getParameter("reqId");
        OIDCAuthenticationError error = (OIDCAuthenticationError) request.getAttribute("org.keycloak.adapters.spi.AuthenticationError");
        String path = (String) request.getAttribute("javax.servlet.error.request_uri");
        if (!Objects.isNull(error)) {
            log.error("Error performing request [reqId = {}]. {}: {}. Path = {}",
                    reqId,
                    error.getReason().name(),
                    StringUtils.isNotBlank(error.getDescription()) ? error.getDescription() : "",
                    path);
        } else {
            String errorCode = request.getAttribute("javax.servlet.error.status_code").toString();
            if (errorCode.equals("403")) {
                log.error("Error performing request [reqId = {}]. {}. Path = {}", reqId, "Not enough rights to role", path);
            } else {
                String servletError = (String) request.getAttribute("javax.servlet.error.message");
                log.error("Error performing request [reqId = {}]. {}. Path = {}", reqId, servletError, path);
            }
        }
    }
}
