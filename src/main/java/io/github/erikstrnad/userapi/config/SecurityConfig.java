package io.github.erikstrnad.userapi.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.Arrays;
import java.util.List;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http.csrf(AbstractHttpConfigurer::disable)
                .cors(Customizer.withDefaults())
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/login", "/register", "/swagger-ui/**", "/v3/api-docs/**", "/h2-console/**").permitAll()
                        .anyRequest().authenticated()
                );
        return http.build();
    }

    /**
     * Configures CORS filter to allow cross-domain requests.
     *
     * @return CorsConfigurationSource that defines allowed origins, methods, and headers.
     */
    @Bean
    CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();

        // In a production environment, it is recommended to specify allowed domains explicitly
        // Example: configuration.setAllowedOrigins(List.of("https://www.myapp.com", "http://localhost:4200"));
        configuration.setAllowedOrigins(List.of("*")); // Allow all origins ("*") for testing purposes

        // Define allowed HTTP methods
        configuration.setAllowedMethods(Arrays.asList("GET", "POST"));

        // Specify allowed headers
        configuration.setAllowedHeaders(List.of("Origin", "Content-type", "Accept", "Authorization"));

        // Register this configuration for all API endpoints
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }
}
