package org.meiskalt7.filtrator.filter;

import com.netflix.zuul.context.RequestContext;
import com.netflix.zuul.exception.ZuulException;

import javax.servlet.http.HttpServletRequest;
import java.util.regex.Pattern;

final class FilterUtils {
    private static final Pattern LOGIN_PATTERN = Pattern.compile("k-\\d{6}");

    private FilterUtils() {
    }

    static String parseServiceName() {
        RequestContext ctx = RequestContext.getCurrentContext();
        HttpServletRequest request = ctx.getRequest();
        return parseServiceName(request);
    }

    static String parseServiceName(HttpServletRequest request) {
        return request.getRequestURI().substring(1).split("/")[0];
    }

    static ZuulException createZuulForbiddenException(String logMessage, String frontMessage) {
        return new ZuulException(logMessage, 403, frontMessage);
    }

    static boolean isContainsLogin(String verifiablePart) {
        return LOGIN_PATTERN.matcher(verifiablePart).find();
    }

    static boolean isContainsLogin(Request request) {
        String verifiablePart = request.isPostRequestType() ? request.getBody() : request.getPath();
        return isContainsLogin(verifiablePart);
    }
}
