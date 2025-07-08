package org.synergym.backendapi.service.oauth;

import lombok.Getter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority; // 💡 1. SimpleGrantedAuthority 임포트 추가
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.synergym.backendapi.entity.Role;
import org.synergym.backendapi.entity.User;

import java.util.Collection;
import java.util.Collections;
import java.util.Map;

@Getter
public class CustomOAuth2User implements OAuth2User {

    private final String email;
    private final Role role;
    private final boolean isNewUser; // 신규 사용자인지 여부를 판단하는 플래그
    private final Map<String, Object> attributes;

    // 기존 사용자를 위한 생성자
    public CustomOAuth2User(User user, Map<String, Object> attributes) {
        this.email = user.getEmail();
        this.role = user.getRole();
        this.attributes = attributes;
        this.isNewUser = false;
    }

    // 추가 정보 입력이 필요한 신규 사용자를 위한 생성자
    public CustomOAuth2User(String email, Map<String, Object> attributes) {
        this.email = email;
        this.role = Role.MEMBER; // 임시 역할 부여
        this.attributes = attributes;
        this.isNewUser = true;
    }

    @Override
    public Map<String, Object> getAttributes() {
        return attributes;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        // 💡 2. Spring Security의 'hasRole()' 메서드가 인식할 수 있도록 "ROLE_" 접두사를 붙여 권한을 생성합니다.
        // 이것이 가장 표준적이고 권장되는 방식입니다.
        return Collections.singleton(new SimpleGrantedAuthority("ROLE_" + role.name()));
    }

    @Override
    public String getName() {
        // OAuth2 표준에서 name은 사용자를 식별하는 고유 ID를 의미합니다.
        // 여기서는 이메일을 사용합니다.
        return this.email;
    }
}