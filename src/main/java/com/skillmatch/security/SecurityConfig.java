package com.skillmatch.security;

import jakarta.servlet.http.HttpServletResponse;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.HeadersConfigurer;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.www.BasicAuthenticationFilter;
import org.springframework.security.web.csrf.CookieCsrfTokenRepository;
import org.springframework.security.web.csrf.CsrfTokenRequestAttributeHandler;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class SecurityConfig {

    private final CustomUserDetailsService userDetailsService;

    public SecurityConfig(CustomUserDetailsService userDetailsService) {
        this.userDetailsService = userDetailsService;
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public DaoAuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider authProvider = new DaoAuthenticationProvider();
        authProvider.setUserDetailsService(userDetailsService);
        authProvider.setPasswordEncoder(passwordEncoder());
        return authProvider;
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration authConfig) throws Exception {
        return authConfig.getAuthenticationManager();
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        CsrfTokenRequestAttributeHandler requestHandler = new CsrfTokenRequestAttributeHandler();
        // set the name of the attribute the CsrfToken will be populated on
        requestHandler.setCsrfRequestAttributeName(null);

        http
            .authenticationProvider(authenticationProvider())
            .csrf(csrf -> csrf
                .csrfTokenRepository(CookieCsrfTokenRepository.withHttpOnlyFalse())
                .csrfTokenRequestHandler(requestHandler)
                .ignoringRequestMatchers("/h2-console/**", "/api/auth/register/student", "/api/auth/login")
            )
            .addFilterAfter(new CsrfCookieFilter(), BasicAuthenticationFilter.class)
            .headers(headers -> headers
                .frameOptions(HeadersConfigurer.FrameOptionsConfig::sameOrigin) // for H2 console if used
            )
            .authorizeHttpRequests(auth -> auth
                // Static resources & frontend public pages
                .requestMatchers(
                    "/",
                    "/index.html",
                    "/login.html",
                    "/register.html",
                    "/opportunities.html",
                    "/opportunity-details.html",
                    "/access-denied.html",
                    "/error.html",
                    "/error",
                    "/css/**",
                    "/js/**",
                    "/images/**",
                    "/favicon.ico",
                    "/h2-console/**"
                ).permitAll()

                // Public REST APIs
                .requestMatchers(
                    "/api/auth/register/student",
                    "/api/auth/login",
                    "/api/auth/logout",
                    "/api/auth/me",
                    "/api/auth/csrf"
                ).permitAll()

                // Public Opportunity browsing
                .requestMatchers(HttpMethod.GET, "/api/opportunities", "/api/opportunities/*").permitAll()

                // Student protected endpoints
                .requestMatchers("/student-*.html").hasRole("STUDENT")
                .requestMatchers("/api/students/**").hasRole("STUDENT")
                .requestMatchers(HttpMethod.POST, "/api/opportunities/*/applications").hasRole("STUDENT")

                // Company protected endpoints
                .requestMatchers("/company-*.html").hasRole("COMPANY")
                .requestMatchers("/api/companies/**").hasRole("COMPANY")

                // Administrator protected endpoints
                .requestMatchers("/admin-*.html").hasRole("ADMIN")
                .requestMatchers("/api/admin/**").hasRole("ADMIN")

                // Shared authenticated endpoints (e.g. applications, resume view)
                .requestMatchers("/api/applications/**", "/api/resumes/**").authenticated()

                // Any other request requires authentication
                .anyRequest().authenticated()
            )
            .exceptionHandling(exceptions -> exceptions
                .authenticationEntryPoint((request, response, authException) -> {
                    String uri = request.getRequestURI();
                    if (uri.startsWith("/api/")) {
                        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                        response.setContentType("application/json");
                        response.getWriter().write("{\"status\":401,\"error\":\"Unauthorized\",\"message\":\"Authentication is required to access this resource.\"}");
                    } else {
                        response.sendRedirect("/login.html?session=expired");
                    }
                })
                .accessDeniedHandler((request, response, accessDeniedException) -> {
                    String uri = request.getRequestURI();
                    if (uri.startsWith("/api/")) {
                        response.setStatus(HttpServletResponse.SC_FORBIDDEN);
                        response.setContentType("application/json");
                        response.getWriter().write("{\"status\":403,\"error\":\"Forbidden\",\"message\":\"You do not have permission to access this resource.\"}");
                    } else {
                        response.sendRedirect("/access-denied.html");
                    }
                })
            );

        return http.build();
    }
}
