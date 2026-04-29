package com.example.admissionsystem.config;

import com.example.admissionsystem.auth.security.JwtAuthenticationFilter;
import com.example.admissionsystem.auth.security.PasswordChangeEnforcementFilter;
import com.example.admissionsystem.auth.security.RestAccessDeniedHandler;
import com.example.admissionsystem.auth.security.RestAuthenticationEntryPoint;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthenticationFilter;
    private final PasswordChangeEnforcementFilter passwordChangeEnforcementFilter;
    private final RestAuthenticationEntryPoint authenticationEntryPoint;
    private final RestAccessDeniedHandler accessDeniedHandler;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .csrf(AbstractHttpConfigurer::disable)
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .exceptionHandling(ex -> ex
                        .authenticationEntryPoint(authenticationEntryPoint)
                        .accessDeniedHandler(accessDeniedHandler)
                )
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/api/health", "/api/auth/login").permitAll()
                        .requestMatchers("/api/auth/me", "/api/auth/change-password").authenticated()
                        .requestMatchers(HttpMethod.GET, "/api/dashboard/**").authenticated()
                        .requestMatchers("/api/batches/**", "/api/schools/**", "/api/majors/**", "/api/quotas/**", "/api/users/**", "/api/audit-logs/**").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.POST, "/api/files/**").hasAnyRole("AGENT", "ADMIN")
                        .requestMatchers(HttpMethod.GET, "/api/files/**").authenticated()
                        .requestMatchers(HttpMethod.GET, "/api/waitlists/*").hasAnyRole("AGENT", "SCHOOL_REVIEWER", "ADMIN")
                        .requestMatchers(HttpMethod.POST, "/api/waitlists/*/promote", "/api/waitlists/*/invalidate").hasAnyRole("SCHOOL_REVIEWER", "ADMIN")
                        .requestMatchers(HttpMethod.GET, "/api/domesticreviews/**").hasAnyRole("DOMESTIC_REVIEWER", "ADMIN")
                        .requestMatchers(HttpMethod.POST, "/api/domesticreviews/**").hasRole("DOMESTIC_REVIEWER")
                        .requestMatchers(HttpMethod.GET, "/api/school-reviews/**").hasAnyRole("SCHOOL_REVIEWER", "ADMIN")
                        .requestMatchers(HttpMethod.POST, "/api/school-reviews/**").hasRole("SCHOOL_REVIEWER")
                        .requestMatchers(HttpMethod.POST, "/api/applications/*/close").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.GET, "/api/applications/my").hasAnyRole("AGENT", "ADMIN")
                        .requestMatchers(HttpMethod.GET, "/api/applications/*").hasAnyRole("AGENT", "DOMESTIC_REVIEWER", "SCHOOL_REVIEWER", "ADMIN")
                        .requestMatchers(HttpMethod.POST, "/api/applications").hasAnyRole("AGENT", "ADMIN")
                        .requestMatchers(HttpMethod.PUT, "/api/applications/*").hasAnyRole("AGENT", "ADMIN")
                        .requestMatchers(HttpMethod.POST, "/api/applications/*/supplement").hasAnyRole("AGENT", "ADMIN")
                        .requestMatchers("/api/applications/**").hasAnyRole("AGENT", "ADMIN")
                        .anyRequest().authenticated());
        http.addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);
        http.addFilterAfter(passwordChangeEnforcementFilter, JwtAuthenticationFilter.class);
        return http.build();
    }
}
