package com.dahaiwuliang.config;

import com.alibaba.fastjson2.JSON;
import com.dahaiwuliang.common.R;
import com.dahaiwuliang.security.JwtAuthFilter;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.Collections;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtAuthFilter jwtAuthFilter;

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration c = new CorsConfiguration();
        c.setAllowedOriginPatterns(Collections.singletonList("*"));
        c.setAllowedMethods(Collections.singletonList("*"));
        c.setAllowedHeaders(Collections.singletonList("*"));
        c.setAllowCredentials(true);
        UrlBasedCorsConfigurationSource src = new UrlBasedCorsConfigurationSource();
        src.registerCorsConfiguration("/**", c);
        return src;
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            .cors().and()
            // Stateless JWT API: auth lives in `Authorization: Bearer` header (not cookies),
            // so CSRF tokens are not applicable. See SecurityFilterChain configuration.
            .csrf().disable()
            .sessionManagement().sessionCreationPolicy(SessionCreationPolicy.STATELESS).and()
            .authorizeRequests()
                .antMatchers("/api/auth/**", "/uploads/**", "/static/**", "/h2-console/**",
                             "/error", "/favicon.ico").permitAll()
                .antMatchers("/api/admin/**").hasRole("ADMIN")
                .antMatchers("/api/doctor/**").hasAnyRole("DOCTOR", "ADMIN")
                .antMatchers("/api/patient/**").hasAnyRole("PATIENT", "ADMIN")
                .anyRequest().authenticated()
            .and()
            .headers().frameOptions().disable().and()
            .exceptionHandling()
                .authenticationEntryPoint((req, resp, e) -> {
                    resp.setStatus(401);
                    resp.setContentType("application/json;charset=UTF-8");
                    resp.getWriter().write(JSON.toJSONString(R.fail(401, "请先登录")));
                })
                .accessDeniedHandler((req, resp, e) -> {
                    resp.setStatus(403);
                    resp.setContentType("application/json;charset=UTF-8");
                    resp.getWriter().write(JSON.toJSONString(R.fail(403, "没有访问权限")));
                })
            .and()
            .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class);
        return http.build();
    }
}
