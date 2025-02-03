package io.github.erikstrnad.userapi.filter;

import io.github.erikstrnad.userapi.util.JwtUtil;
import io.github.erikstrnad.userapi.service.CustomUserDetailsService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.NonNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

/**
 * JwtAuthenticationFilter intercepts incoming HTTP requests and checks for a JWT token
 * in the "Authorization" header. If a valid token is found, it loads the user details
 * and sets the authentication in the Spring Security context, so that the user is authenticated.
 */
@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private static final Logger logger = LoggerFactory.getLogger(JwtAuthenticationFilter.class);

    private final JwtUtil jwtUtil;
    private final CustomUserDetailsService customUserDetailsService;

    /**
     * Constructs the JwtAuthenticationFilter with the required dependencies.
     */
    public JwtAuthenticationFilter(JwtUtil jwtUtil, CustomUserDetailsService customUserDetailsService) {
        this.jwtUtil = jwtUtil;
        this.customUserDetailsService = customUserDetailsService;
    }

    /**
     * Filters each request to extract the JWT token, validate it, and set the authentication.
     */
    @Override
    protected void doFilterInternal(@NonNull HttpServletRequest request,
                                    @NonNull HttpServletResponse response,
                                    @NonNull FilterChain filterChain)
            throws ServletException, IOException {
        try {
            // Parse the JWT from the Authorization header
            String jwt = parseJwt(request);
            if (jwt != null) {
                // Extract the username from the JWT
                String username = jwtUtil.extractUsername(jwt);
                // Load user details using the username
                UserDetails userDetails = customUserDetailsService.loadUserByUsername(username);
                // Validate the token
                if (jwtUtil.isTokenValid(jwt, userDetails)) {
                    // Create an authentication token and set it in the SecurityContext
                    UsernamePasswordAuthenticationToken authToken =
                            new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
                    authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                    SecurityContextHolder.getContext().setAuthentication(authToken);
                    logger.debug("JWT validated and authentication set for user: {}", username);
                } else {
                    logger.warn("Invalid or expired JWT for user: {}", username);
                }
            }
        } catch (Exception e) {
            logger.error("Error during JWT validation: {}", e.getMessage(), e);
        }
        // Proceed with the next filter in the chain
        filterChain.doFilter(request, response);
    }

    /**
     * Parses the JWT token from the Authorization header.
     */
    private String parseJwt(HttpServletRequest request) {
        String authHeader = request.getHeader("Authorization");
        if (StringUtils.hasText(authHeader) && authHeader.startsWith("Bearer ")) {
            return authHeader.substring(7); // Remove the "Bearer " prefix
        }
        return null;
    }
}
