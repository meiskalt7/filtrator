package org.meiskalt7.filtrator.filter;

import org.meiskalt7.filtrator.config.ServiceProperties;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import java.util.Collections;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(PowerMockRunner.class)
@PrepareForTest({ClientFilter.class, FilterUtils.class})
public class ClientFilterTest {

    private ClientFilter clientFilter;
    private Request request;
    @Mock
    private ServiceProperties serviceProperties;
    private String filteredService = "meiskalt7-service";
    private String allowedKlogin = "k-123456";

    @Before
    public void setUp() throws Exception {
        clientFilter = new ClientFilter(serviceProperties);
        request = mock(Request.class);
        PowerMockito.whenNew(Request.class).withNoArguments().thenReturn(request);

        when(serviceProperties.getAllowedLogins())
                .thenReturn(Collections.singleton(allowedKlogin));
        when(serviceProperties.getMicroservicesForClientFiltration())
                .thenReturn(Collections.singleton(filteredService));
    }

    @Test
    public void requestGetTypeWithAllowedLogin() {

        when(request.isPostRequestType()).thenReturn(false);
        when(request.getPath()).thenReturn("/client/" + allowedKlogin);
        when(request.getLoginFromPath()).thenReturn(allowedKlogin);

        assertNull(clientFilter.run());
    }

    @Test(expected = Exception.class)
    public void requestGetTypeWithNotAllowedLogin() {
        when(request.isPostRequestType()).thenReturn(false);
        String notAllowedKlogin = "k-654321";
        when(request.getPath()).thenReturn("/client/" + notAllowedKlogin);
        when(request.getLoginFromPath()).thenReturn(notAllowedKlogin);

        clientFilter.run();
    }

    @Test
    public void requestPostTypeIgnored() {
        when(request.isPostRequestType()).thenReturn(true);
        when(request.getBody()).thenReturn("k-123456");

        clientFilter.run();

        verify(serviceProperties, never()).getAllowedLogins();
    }

    @Test
    public void serviceShouldFilter() {
        PowerMockito.mockStatic(FilterUtils.class);
        when(FilterUtils.parseServiceName()).thenReturn(filteredService);
        assertTrue(clientFilter.shouldFilter());
    }

    @Test
    public void serviceShouldNotFilter() {
        PowerMockito.mockStatic(FilterUtils.class);
        String ignoredService = "meiskalt7-transporter";
        when(FilterUtils.parseServiceName()).thenReturn(ignoredService);
        assertFalse(clientFilter.shouldFilter());
    }

}