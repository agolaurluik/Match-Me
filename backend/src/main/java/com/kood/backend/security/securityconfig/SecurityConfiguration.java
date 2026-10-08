package com.kood.backend.security.securityconfig;

import static org.springframework.security.config.http.SessionCreationPolicy.STATELESS;

import java.util.Arrays;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.servlet.handler.HandlerMappingIntrospector;

import com.kood.backend.security.jwtconfig.AuthEntryPointJwt;
import com.kood.backend.security.jwtconfig.AuthTokenFilter;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class SecurityConfiguration {

    @Autowired
    UserDetailsService userDetailsService;
    @Autowired
    private AuthEntryPointJwt unauthorizedHandler;

    private static final String[] WHITE_LIST_URL = { "/api/auth/**", "/api/test/**", "/media/public" };

    // These are the endpoints that are
    // accessible by anyone (** means every endpoint in auth route is accessible)
    // If you want to add any other paths they must be the complete path, otherwise
    // spring will block it

    // The WHITELIST is both here and inside AuthTokenFilter, otherwise they will
    // both try to block regardless

    @Bean
    public AuthTokenFilter authenticationJwtTokenFilter() {
        return new AuthTokenFilter();
    }

    @Bean
    public DaoAuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider authProvider = new DaoAuthenticationProvider();
        // DAO = Data Access Object
        authProvider.setUserDetailsService(userDetailsService);
        authProvider.setPasswordEncoder(passwordEncoder());

        return authProvider;
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration authConfig) throws Exception {
        return authConfig.getAuthenticationManager();
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http, HandlerMappingIntrospector introspector)
            throws Exception {
        http.csrf(abstractHttpConfigurer -> abstractHttpConfigurer.disable())
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))
                // This is a bandaid to allow some form of connection with FRONTEND
                // This is stateless so we don't need CSRF protection
                // (Cross-Site Request Forgery)
                //////////////////////////////////////////////// .cors(AbstractHttpConfigurer::disable)
                // Should be modified not disabled
                .authorizeHttpRequests(req -> req

                        // .requestMatchers("/ws/**", "/app/**", "/topic/**", "/queue/**",
                        // "/actuator/**", "/error").permitAll()
                        .requestMatchers("/ws/**", "/info", "/sockjs-node/**").permitAll()

                        .requestMatchers(WHITE_LIST_URL).permitAll()
                        .anyRequest().authenticated())
                // This means:
                // Any URL matching entries in WHITE_LIST_URL is open to everyone.
                // All other URLs require the user to be authenticated.
                .exceptionHandling(ex -> ex.authenticationEntryPoint(unauthorizedHandler))
                .sessionManagement(session -> session.sessionCreationPolicy(STATELESS))
                // STATeless mode means:
                // Spring Security will not store anything in an HTTP session.
                // Every request must carry its own authentication — via a JWT in the
                // Authorization header.
                // This aligns with RESTful principles.
                .authenticationProvider(authenticationProvider())
                .addFilterBefore(authenticationJwtTokenFilter(), UsernamePasswordAuthenticationFilter.class);
        // Run AuthTokenFilter before the built-in
        // UsernamePasswordAuthenticationFilter
        // Why? Because:
        // Your filter checks for JWTs and sets the authentication context.
        // If a JWT is valid, it effectively logs in the user for the current request.

        return http.build();
    }

    // CORS configuration
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration corsConfig = new CorsConfiguration();
        corsConfig.setAllowedOriginPatterns(List.of("http://localhost:5173"));
        corsConfig.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "PATCH"));
        corsConfig.setAllowedHeaders(Arrays.asList("Authorization", "Content-Type"));
        corsConfig.setAllowCredentials(true);
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", corsConfig);
        return source;
    }
}