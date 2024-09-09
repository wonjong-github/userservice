package com.example.userservice.security;

import com.example.userservice.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.security.servlet.PathRequest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authorization.AuthorizationDecision;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.annotation.web.configurers.HeadersConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

import java.util.Arrays;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
@Slf4j
public class WebSecurity {

    private final Environment env;

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http,
                                           AuthenticationManager authenticationManager,
                                           UserService userService) throws Exception {

        http.csrf(AbstractHttpConfigurer::disable)
                .authorizeHttpRequests(authorize ->
                        authorize.requestMatchers("/users/**").permitAll()
                                .requestMatchers("/actuator/**").permitAll()
                                .requestMatchers("/health-check/**").permitAll()
                                .requestMatchers("/error/**").permitAll()
                                .requestMatchers("/**").access((authentication, request) -> {
                                    String clientIp = request.getRequest().getRemoteAddr();
                                    log.debug("client ip is = {}", clientIp);

                                    // 허용된 IP 리스트
                                    String[] allowedIps = {"192.168.35.194", "192.168.35.195", "192.168.35.196"};

                                    // IP가 허용된 리스트에 포함되어 있는지 확인
                                    boolean isAllowed = Arrays.asList(allowedIps).contains(clientIp);

                                    return new AuthorizationDecision(isAllowed);
                                })
                )
                .authenticationManager(authenticationManager)
                // 사용자 세션 저장하지 않음
                .sessionManagement(configurer -> configurer.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                // 응답 헤더에 X-Frame-Options 추가 - 클릭재킹 공격 방어 - 동일 출처만 <iframe> 로드 가능.
                .headers(headers -> headers.frameOptions(HeadersConfigurer.FrameOptionsConfig::sameOrigin));
//                .addFilter(customAuthenticationFilter(authenticationManager, userService));

        return http.build();
    }

//
//    private AuthenticationFilter getAuthenticationFilter(AuthenticationManager authenticationManager) {
//        return new AuthenticationFilter(authenticationManager, userService, env);
//    }

    @Bean
    public AuthenticationManager authenticationManager(HttpSecurity http,
                                                       UserService userService,
                                                       BCryptPasswordEncoder passwordEncoder) throws Exception {
        AuthenticationManagerBuilder authenticationManagerBuilder = http.getSharedObject(AuthenticationManagerBuilder.class);
        authenticationManagerBuilder.userDetailsService(userService).passwordEncoder(passwordEncoder);

        return authenticationManagerBuilder.build();
    }

//    @Bean
//    public CustomAuthenticationFilter customAuthenticationFilter(AuthenticationManager authenticationManager,
//                                                                 UserService userService) {
//        return new CustomAuthenticationFilter(authenticationManager, userService, env);
//    }


    @Bean
    public BCryptPasswordEncoder bCryptPasswordEncoder() {
        return new BCryptPasswordEncoder();
    }
}

