package com.example.jewellery_backend.config;

import com.example.jewellery_backend.config.AdminUserDetailsService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.Arrays;

@Configuration
@EnableMethodSecurity(prePostEnabled = true)
public class SecurityConfig {

    private final AdminUserDetailsService adminUserDetailsService;

    public SecurityConfig(AdminUserDetailsService adminUserDetailsService) {
        this.adminUserDetailsService = adminUserDetailsService;
    }

    // -------------------- Security Filter Chain --------------------
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .cors(Customizer.withDefaults())
                .csrf(csrf -> csrf.disable())
                .authorizeHttpRequests(auth -> auth
                        // --- Allow Public Access FIRST ---
                        .requestMatchers(
                                "/", // Allow access to root for "API is running" message
                                "/api/public/**", // Public products, categories
                                "/api/products/filter", // Product filtering
                                "/api/cart/**", // Cart operations
                                "/products/{productId}/reviews", // Public reviews
                                "/api/admin-users/login" // Allow admin login attempts
                                // Add any other public endpoints here (e.g., /api/gold-rates/** ?)
                        ).permitAll()

                        // --- Secure Admin Endpoints ---
                        .requestMatchers(
                                "/admin/**", // Secure admin UI if  add one
                                "/api/admin/**", // Secure admin API
                                "/api/categories/**", // Only ADMIN can POST/PUT/DELETE categories
                                "/orders/**" // Only ADMIN should manage orders/slips via these top-level paths
                        ).hasRole("ADMIN") // Requires ROLE_ADMIN

                        // --- Secure Any Other /api Endpoints ---
                        //  catches things like maybe a future /api/user/profile endpoint
                        .requestMatchers("/api/**").authenticated()

                        // --- Allow All Other Requests (e.g., static files if served) ---
                        .anyRequest().permitAll()
                )
                .formLogin(form -> form.disable())
                .logout(logout -> logout.disable())
                .httpBasic(basic -> basic.disable());

        return http.build();
    }


    // -------------------- CORS Configuration --------------------
    @Bean
    CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOrigins(Arrays.asList("http://localhost:3000"));
        configuration.setAllowedMethods(Arrays.asList("GET","POST","PUT","DELETE","OPTIONS"));
        configuration.setAllowedHeaders(Arrays.asList("Authorization","Cache-Control","Content-Type","X-XSRF-TOKEN","X-Requested-With"));
        configuration.setAllowCredentials(true);
        configuration.setMaxAge(3600L);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }

    // -------------------- Password Encoder --------------------
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    // -------------------- Authentication Manager --------------------
    @Bean
    public AuthenticationManager authenticationManager(HttpSecurity http) throws Exception {
        return http.getSharedObject(AuthenticationManager.class);
    }
}
