package com.example.jewellery_backend.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.Arrays;

@Configuration
public class SecurityConfig {

    @Bean
    SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .cors(Customizer.withDefaults())    // enable CORS (uses the bean below)
                .csrf().disable()                   // DEV: disable CSRF for token/basic-auth APIs (see notes below)
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/admin/**").authenticated()
                        .anyRequest().permitAll()
                )
                .httpBasic(Customizer.withDefaults()); // simple dev auth; replace with JWT/UserDetails later
        return http.build();
    }

    @Bean
    CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOrigins(Arrays.asList("http://localhost:3000")); // <- your frontend origin
        configuration.setAllowedMethods(Arrays.asList("GET","POST","PUT","DELETE","OPTIONS"));
        configuration.setAllowedHeaders(Arrays.asList("Authorization","Cache-Control","Content-Type","X-XSRF-TOKEN","X-Requested-With"));
        configuration.setAllowCredentials(true); // needed if you send cookies or use httpBasic with browser
        configuration.setMaxAge(3600L);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }
}
