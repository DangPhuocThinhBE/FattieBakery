package com.fattiebakery.service;

import com.fattiebakery.model.User;
import com.fattiebakery.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.*;
import org.springframework.stereotype.Service;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class CustomUserDetailsService implements UserDetailsService {

    @Autowired
    private UserRepository userRepository;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        // Tìm user theo username (admin)
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("Không tìm thấy tài khoản: " + username));

        // Kiểm tra trạng thái active
        if (user.getActive() == null || !user.getActive()) {
            throw new UsernameNotFoundException("Tài khoản đã bị khóa hoặc không tồn tại: " + username);
        }

        // Chuyển đổi Roles từ DB sang GrantedAuthority của Spring Security
        Set<GrantedAuthority> authorities = user.getRoles().stream()
                .map(role -> {
                    String roleName = role.getName();
                    // Nếu DB là 'ADMIN' -> Tự động chuyển thành 'ROLE_ADMIN' để khớp với SecurityConfig
                    if (roleName != null && !roleName.startsWith("ROLE_")) {
                        roleName = "ROLE_" + roleName;
                    }
                    return new SimpleGrantedAuthority(roleName);
                })
                .collect(Collectors.toSet());

        return new org.springframework.security.core.userdetails.User(
                user.getUsername(),
                user.getPassword(),
                authorities
        );
    }
}