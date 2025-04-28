package com.epam.training.spring_boot_epam.security;

import jakarta.servlet.http.HttpServletResponse;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import com.epam.training.spring_boot_epam.security.service.impl.JwtAuthFilter;

@Profile("dev")
@Configuration
@EnableMethodSecurity
public class SecurityConfig {

    private final CustomCorsConfiguration customCorsConfiguration;
    private final JwtAuthFilter jwtAuthFilter;
    private final AuthenticationProvider authenticationProvider;

    private static final String[] WHITE_URLS = new String[]{
            "/v1/auth/login",
            "/swagger-ui/**",
            "/v3/api-docs/**",
            "/v2/api-docs/**",
    };

    public SecurityConfig(CustomCorsConfiguration customCorsConfiguration, JwtAuthFilter jwtAuthFilter, AuthenticationProvider authenticationProvider) {
        this.customCorsConfiguration = customCorsConfiguration;
        this.jwtAuthFilter = jwtAuthFilter;
        this.authenticationProvider = authenticationProvider;
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity httpSecurity) throws Exception {
        httpSecurity
                .csrf(AbstractHttpConfigurer::disable)
                .cors(c -> c.configurationSource(customCorsConfiguration))
                .authorizeHttpRequests(request -> request
                        .requestMatchers(WHITE_URLS).permitAll()
                        .requestMatchers(HttpMethod.POST, "/v1/trainers", "/v1/trainees").permitAll()
                        .anyRequest().authenticated())
                .exceptionHandling(configurer ->  {
                    configurer.authenticationEntryPoint((request, response, authException) -> {
                        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                        response.setContentType("application/json");
                        response.getWriter().write("{\"success\": false, \"message\": \"Please authorize first\", \"data\": null}");
                    });
                })
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authenticationProvider(authenticationProvider)
                .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class);

        return httpSecurity.build();
    }


}