package com.gsmtracker.auth;

import com.gsmtracker.device.Device;
import com.gsmtracker.device.DeviceRepository;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import java.util.List;

public class DeviceAuthenticationProvider implements AuthenticationProvider {

    private final DeviceRepository deviceRepository;

    public DeviceAuthenticationProvider(DeviceRepository deviceRepository) {
        this.deviceRepository = deviceRepository;
    }

    @Override
    public Authentication authenticate(Authentication authentication) throws AuthenticationException {
        String token = (String) authentication.getCredentials();
        Device device = deviceRepository.findByToken(token)
                .orElseThrow(() -> new BadCredentialsException("Invalid device token"));
        return new DeviceAuthenticationToken(device, List.of(new SimpleGrantedAuthority("ROLE_DEVICE")));
    }

    // Security вызовет этот провайдер только для наших DeviceAuthenticationToken
    @Override
    public boolean supports(Class<?> authentication) {
        return DeviceAuthenticationToken.class.isAssignableFrom(authentication);
    }
}