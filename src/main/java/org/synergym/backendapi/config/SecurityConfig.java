package org.synergym.backendapi.config;

import jakarta.servlet.http.HttpServletResponse;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.synergym.backendapi.filter.JwtAuthenticationFilter;
import org.synergym.backendapi.handler.OAuth2AuthenticationSuccessHandler;
import org.synergym.backendapi.service.oauth.CustomOAuth2UserService;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    // PasswordEncoder
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public AuthenticationEntryPoint jwtAuthenticationEntryPoint() {
        return (request, response, authException) -> {
            // 유효한 자격증명을 제공하지 않고 접근하려 할때 401 Unauthorized 에러를 리턴
            response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Unauthorized");
        };
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http, CustomOAuth2UserService customOAuth2UserService, OAuth2AuthenticationSuccessHandler oAuth2AuthenticationSuccessHandler, JwtAuthenticationFilter jwtAuthenticationFilter) throws Exception {
        http
                .cors(Customizer.withDefaults())
                // CSRF(Cross-Site Request Forgery) 보호 비활성화
                .csrf(AbstractHttpConfigurer::disable)

                // 기본 HTTP 인증 및 폼 로그인 비활성화
                .httpBasic(AbstractHttpConfigurer::disable)
                .formLogin(AbstractHttpConfigurer::disable)

                // 세션 관리 정책을 STATELESS로 설정
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class)

                .exceptionHandling(exceptions -> exceptions
                        .authenticationEntryPoint((request, response, authException) -> {
                            // 유효한 자격증명을 제공하지 않고 접근하려 할때 401
                            response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Unauthorized");
                        })
                )

                // API 엔드포인트별 접근 권한 설정
                .authorizeHttpRequests(authorize -> authorize
                        // 인증 불필요 - 모든 사용자 접근 가능
                        .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()
                        .requestMatchers("/api/auth/**").permitAll()
                        .requestMatchers("/oauth2/**", "/login/oauth2/**").permitAll()
                        .requestMatchers("/static/**", "/css/**", "/js/**", "/images/**", "/favicon.ico").permitAll()

                        // 관리자 전용
                        .requestMatchers("/api/admin/**").hasRole("ADMIN")
                        .requestMatchers("/api/users").hasRole("ADMIN") // 모든 사용자 목록 조회

                        // 회원 권한 (MEMBER, ADMIN)
                        .requestMatchers("/api/exercises/**").hasAnyRole("MEMBER", "ADMIN")
                        .requestMatchers("/api/ai/**").hasAnyRole("MEMBER", "ADMIN")
                        .requestMatchers("/api/routines/**").hasAnyRole("MEMBER", "ADMIN")
                        .requestMatchers("/api/exercise-logs/**").hasAnyRole("MEMBER", "ADMIN")
                        .requestMatchers("/api/posts/**").hasAnyRole("MEMBER", "ADMIN")
                        .requestMatchers("/api/comments/**").hasAnyRole("MEMBER", "ADMIN")
                        .requestMatchers("/api/post-likes/**").hasAnyRole("MEMBER", "ADMIN")
                        .requestMatchers("/api/notifications/**").hasAnyRole("MEMBER", "ADMIN")
                        .requestMatchers("/api/analysis-histories/**").permitAll()
                        .requestMatchers("/api/categories/**").hasAnyRole("MEMBER", "ADMIN")
                        .requestMatchers("/api/cloudinary/**").hasAnyRole("MEMBER", "ADMIN")

                        // 본인 또는 관리자만 접근 가능 (컨트롤러에서 추가 검증)
                        .requestMatchers("/api/users/**").hasAnyRole("MEMBER", "ADMIN")

                        // 그 외 모든 요청은 인증 필요
                        .anyRequest().authenticated()
                )
                .oauth2Login(oauth2 -> oauth2
                        .userInfoEndpoint(userInfo -> userInfo
                                .userService(customOAuth2UserService) // 커스텀 유저 서비스 등록
                        )
                        .successHandler(oAuth2AuthenticationSuccessHandler) // 커스텀 성공 핸들러 등록
                );

        return http.build();
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration authenticationConfiguration) throws Exception {
        return authenticationConfiguration.getAuthenticationManager();
    }
}
