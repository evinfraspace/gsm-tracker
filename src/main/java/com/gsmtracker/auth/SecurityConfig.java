package com.gsmtracker.auth;

import com.gsmtracker.device.DeviceRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.ProviderManager;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
public class SecurityConfig {

    // Цепочка 1: приём координат от устройства.
    @Bean
    @Order(1)
    public SecurityFilterChain ingestChain(HttpSecurity http,
                                           DeviceRepository deviceRepository,
                                           DeviceAuthenticationEntryPoint deviceEntryPoint) throws Exception {
        AuthenticationManager authManager =
                new ProviderManager(new DeviceAuthenticationProvider(deviceRepository));
        DeviceTokenAuthenticationFilter deviceFilter =
                new DeviceTokenAuthenticationFilter(authManager, deviceEntryPoint);

        http
                .securityMatcher("/api/v1/positions/**")
                .csrf(csrf -> csrf.disable())
                .sessionManagement(sm -> sm.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth.anyRequest().authenticated())
                .exceptionHandling(ex -> ex.authenticationEntryPoint(deviceEntryPoint))
                .addFilterBefore(deviceFilter, UsernamePasswordAuthenticationFilter.class);
        return http.build();
    }

    // Цепочка 2: всё остальное (чтение, дашборд, actuator) — HTTP Basic.
    @Bean
    @Order(2)
    public SecurityFilterChain apiChain(HttpSecurity http) throws Exception {
        http
                .authorizeHttpRequests(auth -> auth.anyRequest().authenticated())
                .httpBasic(Customizer.withDefaults());
        return http.build();
    }

        @Bean
    public UserDetailsService users(
            @Value("${app.admin.username}") String username,
            @Value("${app.admin.password}") String password) {
        return new InMemoryUserDetailsManager(
                User.withUsername(username)
                        .password(password)
                        .roles("ADMIN")
                        .build()
        );
    }
}