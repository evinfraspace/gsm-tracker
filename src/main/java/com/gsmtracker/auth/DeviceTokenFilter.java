package com.gsmtracker.auth;

import com.gsmtracker.device.Device;
import com.gsmtracker.device.DeviceRepository;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Optional;

/**
 * Minimal device authentication for ingest endpoints: reads a
 * {@code Authorization: Bearer <token>} header, resolves the device and exposes
 * it as the {@value #DEVICE_ATTRIBUTE} request attribute.
 *
 * <p>Deliberately lightweight for a learning project. A production setup would
 * use Spring Security and store only hashed tokens.
 */
@Component
public class DeviceTokenFilter extends OncePerRequestFilter {

    public static final String DEVICE_ATTRIBUTE = "device";
    private static final String INGEST_PATH_PREFIX = "/api/v1/positions";
    private static final String BEARER_PREFIX = "Bearer ";

    private final DeviceRepository deviceRepository;

    public DeviceTokenFilter(DeviceRepository deviceRepository) {
        this.deviceRepository = deviceRepository;
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        return !request.getRequestURI().startsWith(INGEST_PATH_PREFIX);
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        String header = request.getHeader(HttpHeaders.AUTHORIZATION);
        if (header == null || !header.startsWith(BEARER_PREFIX)) {
            unauthorized(response, "Missing or malformed Authorization header");
            return;
        }

        String token = header.substring(BEARER_PREFIX.length()).trim();
        Optional<Device> device = deviceRepository.findByToken(token);
        if (device.isEmpty()) {
            unauthorized(response, "Invalid device token");
            return;
        }

        request.setAttribute(DEVICE_ATTRIBUTE, device.get());
        filterChain.doFilter(request, response);
    }

    private void unauthorized(HttpServletResponse response, String message) throws IOException {
        response.setStatus(HttpStatus.UNAUTHORIZED.value());
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.getWriter().write("{\"status\":401,\"error\":\"Unauthorized\",\"message\":\"" + message + "\"}");
    }
}
