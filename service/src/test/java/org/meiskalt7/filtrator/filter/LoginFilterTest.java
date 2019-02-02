package org.meiskalt7.filtrator.filter;


import org.meiskalt7.filtrator.config.ServiceProperties;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.keycloak.KeycloakPrincipal;
import org.mockito.Mock;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PowerMockIgnore;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import java.util.Collections;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@RunWith(PowerMockRunner.class)
@PrepareForTest({LoginFilter.class, KeycloakPrincipal.class, FilterUtils.class})
@PowerMockIgnore("javax.security.auth.Subject")
public class LoginFilterTest {
    private LoginFilter loginFilter;
    private Request request;
    @Mock
    private ServiceProperties serviceProperties;
    private String filteredService = "meiskalt7-service";
    private String login1 = "k-123456";

    @Before
    public void setUp() throws Exception {
        loginFilter = new LoginFilter(serviceProperties);
        request = mock(Request.class, RETURNS_DEEP_STUBS);
        PowerMockito.whenNew(Request.class).withNoArguments().thenReturn(request);

        when(serviceProperties.getMicroservicesForLoginFiltration())
                .thenReturn(Collections.singleton(filteredService));
    }

    private void prepareRequest(String loginFromRequest, String loginFromAuthorizationToken) {
        when(request.isPostRequestType()).thenReturn(false);
        when(request.getPath()).thenReturn("/client/" + loginFromRequest);
        when(request.getLogin()).thenReturn(loginFromRequest);

        KeycloakPrincipal keycloakPrincipal = mock(KeycloakPrincipal.class, RETURNS_DEEP_STUBS);
        when(request.getServletRequest().getUserPrincipal()).thenReturn(keycloakPrincipal);
        when(keycloakPrincipal.getKeycloakSecurityContext().getToken().getPreferredUsername()).thenReturn(loginFromAuthorizationToken);
    }

    @Test
    public void requestWithLoginInRequestPathSameAsKloginInToken() {
        prepareRequest(login1, login1);
        assertNull(loginFilter.run());
    }

    @Test(expected = Exception.class)
    public void requestWithKloginInRequestPathDifferentThanKloginInToken() {
        String login2 = "k-654321";
        prepareRequest(login1, login2);
        assertNull(loginFilter.run());
    }

    @Test
    public void serviceShouldFilter() {
        PowerMockito.mockStatic(FilterUtils.class);
        when(FilterUtils.parseServiceName()).thenReturn(filteredService);
        assertTrue(loginFilter.shouldFilter());
    }

    @Test
    public void serviceShouldNotFilter() {
        PowerMockito.mockStatic(FilterUtils.class);
        String ignoredService = "meiskalt7-transporter";
        when(FilterUtils.parseServiceName()).thenReturn(ignoredService);
        assertFalse(loginFilter.shouldFilter());
    }
}