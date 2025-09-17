package com.cts.ecommerce.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import com.cts.ecommerce.service.JwtUtil;
import com.cts.ecommerce.service.UserDetailsServiceImpl;

import org.springframework.security.config.http.SessionCreationPolicy;

@Configuration
public class SecurityConfig {

    @Autowired 
    private JwtUtil jwtUtil;

    @Autowired 
    private UserDetailsServiceImpl userDetailsService;

    // JWT filter bean
    @Bean
    public JwtAuthFilter jwtAuthFilter() {
        return new JwtAuthFilter(jwtUtil, userDetailsService);
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                .csrf(csrf -> csrf.disable())
                .sessionManagement(sm -> sm.sessionCreationPolicy(SessionCreationPolicy.IF_REQUIRED))
                .authorizeHttpRequests(requests -> requests
                        // Public endpoints
                        .requestMatchers("/", "/landing", "/login", "/register", "/products",
                                "/css/**", "/js/**", "/images/**", "/api/auth/**").permitAll()
                        // Admin endpoints
                        .requestMatchers("/products/api/admin/**", "/admin/**", "/admin/api/**").hasAuthority("ROLE_ADMIN")
                        // User endpoints that need authentication
                        .requestMatchers("/profile/**", "/orders", "/cart/**", "/api/orders/**").authenticated()
                        // Everything else needs authentication
                        .anyRequest().authenticated())
                .addFilterBefore(jwtAuthFilter(), UsernamePasswordAuthenticationFilter.class)
                .formLogin(login -> login
                        .loginPage("/login")
                        .defaultSuccessUrl("/", true)
                        .failureUrl("/login?error=true")
                        .permitAll())
                .logout(logout -> logout
                        .logoutUrl("/logout")
                        .logoutSuccessUrl("/login?logout") // redirect after logout
                        .invalidateHttpSession(true)
                        .deleteCookies("JSESSIONID", "jwt") // also remove JWT cookie
                        .permitAll()
                );


        return http.build();
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration authConfig) throws Exception {
        return authConfig.getAuthenticationManager();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
