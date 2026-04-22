package com.example.admissionsystem.auth.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtTokenUtil jwtTokenUtil;
    private final DatabaseUserDetailsService userDetailsService;
    private final RestAuthenticationEntryPoint authenticationEntryPoint;

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain
    ) throws ServletException, IOException {
        String authorization = request.getHeader(HttpHeaders.AUTHORIZATION);
        if (authorization == null || !authorization.startsWith("Bearer ")) {
            filterChain.doFilter(request, response);
            return;
        }

        String token = authorization.substring(7).trim();
        if (token.isBlank() || SecurityContextHolder.getContext().getAuthentication() != null) {
            filterChain.doFilter(request, response);
            return;
        }

        try {
            JwtTokenUtil.TokenPayload payload = jwtTokenUtil.parseAndValidate(token);
            UserDetails userDetails = userDetailsService.loadUserByUsername(payload.username());
            if (!userDetails.isEnabled()) {
                throw new BadCredentialsException("账号已停用");
            }
            if (!userDetails.isAccountNonLocked()) {
                throw new BadCredentialsException("账号已被锁定");
            }
            UsernamePasswordAuthenticationToken authenticationToken = new UsernamePasswordAuthenticationToken(
                    userDetails,
                    null,
                    userDetails.getAuthorities()
            );
            authenticationToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
            SecurityContextHolder.getContext().setAuthentication(authenticationToken);
            filterChain.doFilter(request, response);
        } catch (Exception ex) {
            SecurityContextHolder.clearContext();
            authenticationEntryPoint.commence(request, response, new BadCredentialsException("Invalid token", ex));
        }
    }
}
