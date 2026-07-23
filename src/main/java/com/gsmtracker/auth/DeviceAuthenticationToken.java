package com.gsmtracker.auth;

import com.gsmtracker.device.Device;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;

import java.util.Collection;

/**
 * Носитель аутентификации устройства.
 * До проверки: хранит только сырой token (credentials), не аутентифицирован.
 * После проверки: хранит Device (principal) и права, помечен как аутентифицированный.
 */
public class DeviceAuthenticationToken extends AbstractAuthenticationToken {

    private final String token;
    private final Device device;

    // неаутентифицированный — создаётся фильтром из заголовка
    public DeviceAuthenticationToken(String token) {
        super(null);
        this.token = token;
        this.device = null;
        setAuthenticated(false);
    }

    // аутентифицированный — создаётся провайдером после успешной проверки
    public DeviceAuthenticationToken(Device device, Collection<? extends GrantedAuthority> authorities) {
        super(authorities);
        this.token = null;
        this.device = device;
        setAuthenticated(true);
    }

    @Override
    public Object getCredentials() {
        return token;
    }

    @Override
    public Object getPrincipal() {
        return device;
    }
}