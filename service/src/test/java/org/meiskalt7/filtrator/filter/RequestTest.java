package org.meiskalt7.filtrator.filter;

import com.netflix.zuul.context.RequestContext;
import org.apache.commons.lang3.StringUtils;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import org.springframework.web.servlet.HandlerMapping;

import javax.servlet.http.HttpServletRequest;
import java.io.IOException;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@RunWith(PowerMockRunner.class)
@PrepareForTest({RequestContext.class, Request.class})
public class RequestTest {

    private static final String requestPath = "/meiskalt7-service/client/k-123456";
    private RequestContext requestContext;
    private HttpServletRequest httpServletRequest;

    @Before
    public void setUp() {
        requestContext = mock(RequestContext.class);
        PowerMockito.mockStatic(RequestContext.class);
        PowerMockito.when(RequestContext.getCurrentContext()).thenReturn(requestContext);

        httpServletRequest = mock(HttpServletRequest.class);
        when(requestContext.getRequest()).thenReturn(httpServletRequest);
        when(httpServletRequest.getMethod()).thenReturn("GET");
        when(httpServletRequest.getRequestURI()).thenReturn(requestPath);
        when(httpServletRequest.getAttribute(HandlerMapping.PATH_WITHIN_HANDLER_MAPPING_ATTRIBUTE)).thenReturn(requestPath);
        when(httpServletRequest.getContentLength()).thenReturn(0);
    }

    @Test
    public void correctInitOfRequestFromContext() throws IOException {
        Request request = new Request();

        assertNotNull(request.getCtx());
        assertNotNull(request.getServletRequest());
        assertEquals("meiskalt7-service", request.getServiceName());
        assertFalse(request.isPostRequestType());
        assertEquals(requestPath, request.getPath());
        assertEquals(StringUtils.EMPTY, request.getBody());
    }

    @Test
    public void postRequest() throws IOException {
        when(httpServletRequest.getMethod()).thenReturn("POST");
        Request request = new Request();

        assertTrue("Request type incorrectly identified", request.isPostRequestType());
    }

    @Test
    public void getLoginFromGetRequest() throws IOException {
        Request request = new Request();

        assertEquals("k-123456", request.getLoginFromPath());
    }

    @Test
    public void getLoginFromPostRequest() throws IOException {
        when(httpServletRequest.getMethod()).thenReturn("POST");
        Request request = new Request();

        assertNull(request.getLogin());
    }
}