package org.meiskalt7.filtrator.filter;

import com.netflix.zuul.context.RequestContext;
import lombok.Data;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.web.servlet.HandlerMapping;

import javax.servlet.http.HttpServletRequest;
import java.io.IOException;

import static org.meiskalt7.filtrator.filter.FilterUtils.parseServiceName;

@Data
class Request {
    private RequestContext ctx;
    private HttpServletRequest servletRequest;
    private String serviceName;
    private String type;
    private String path;
    private String body;

    Request() throws IOException {
        ctx = RequestContext.getCurrentContext();
        servletRequest = ctx.getRequest();
        serviceName = parseServiceName(servletRequest);
        type = servletRequest.getMethod();
        path = (String) servletRequest.getAttribute(HandlerMapping.PATH_WITHIN_HANDLER_MAPPING_ATTRIBUTE);
        body = isPostRequestType() && servletRequest.getContentLength() != 0 ? IOUtils.toString(servletRequest.getReader()) : StringUtils.EMPTY;
    }

    boolean isPostRequestType() {
        return "POST".equalsIgnoreCase(type);
    }

    String getLogin() {
        return isPostRequestType() ? null : getLoginFromPath();
    }

    String getLoginFromPath() {
        return "k-" + StringUtils.substringAfter(path, "k-").substring(0, 6);
    }
}
