package com.fattiebakery.service;

import com.fattiebakery.model.User;
import com.fattiebakery.model.Role; // Quan trọng: Import Entity Role
import com.fattiebakery.repository.UserRepository;
import com.fattiebakery.repository.RoleRepository; // Quan trọng: Import Repository
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.DefaultOAuth2User;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.HashSet; // Thêm cái này
import java.util.Map;
import java.util.Optional;
import java.util.Set; // Thêm cái này

@Service
@Transactional
public class OAuth2UserService extends DefaultOAuth2UserService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RoleRepository roleRepository; // Đã hết đỏ nhờ dòng import ở trên

    @Override
    public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {
        OAuth2User oauth2User = super.loadUser(userRequest);
        String clientName = userRequest.getClientRegistration().getRegistrationId();

        // 1. Lấy thông tin thô
        String email = oauth2User.getAttribute("email");
        String name = oauth2User.getAttribute("name");
        String picture = null;

        // 2. Phân loại theo Provider
        if ("google".equals(clientName)) {
            picture = oauth2User.getAttribute("picture");
        } else if ("github".equals(clientName)) {
            picture = oauth2User.getAttribute("avatar_url");
            if (name == null) name = oauth2User.getAttribute("login");
        }

        // 3. Chống NULL Email (Cực kỳ quan trọng cho GitHub)
        if (email == null || email.isEmpty()) {
            email = (name != null ? name.replaceAll("\\s+", "").toLowerCase() : "user") + "@github.com";
        }

        // 4. Lưu hoặc cập nhật Database
        processOAuth2User(email, name, picture);

        // 5. Đóng gói lại Attributes để chắc chắn hệ thống luôn thấy "email"
        Map<String, Object> attributes = new HashMap<>(oauth2User.getAttributes());
        attributes.put("email", email);
        if (name != null) attributes.put("name", name); // Đảm bảo hiển thị tên thật

        return new DefaultOAuth2User(
                oauth2User.getAuthorities(),
                attributes,
                "email" // CHỐT HẠ: Luôn dùng email làm ID cho cả hai
        );
    }
    
    private void processOAuth2User(String email, String name, String picture) {
        // 1. CHỐNG LỖI NULL EMAIL: Nếu GitHub giấu email, lấy tạm name làm email giả định
        if (email == null || email.isEmpty()) {
            // Tạo email tạm để không bị lỗi DB và logic bên dưới
            email = (name != null ? name.replaceAll("\\s+", "").toLowerCase() : "github_user") + "@github.com";
        }

        Optional<User> existUser = userRepository.findByEmail(email);

        if (existUser.isEmpty()) {
            User newUser = new User();

            // 2. XỬ LÝ USERNAME AN TOÀN
            String customUsername;
            if (email.contains("@")) {
                customUsername = email.substring(0, email.indexOf("@"));
            } else {
                customUsername = email;
            }

            newUser.setUsername(customUsername);
            newUser.setEmail(email);
            newUser.setFullName(name != null ? name : customUsername);
            newUser.setAvatarUrl(picture);
            newUser.setPassword("");
            newUser.setActive(true);

            // Cấp Role (như cũ)
            roleRepository.findById(2L).ifPresent(role -> {
                Set<Role> roles = new HashSet<>();
                roles.add(role);
                newUser.setRoles(roles);
            });

            userRepository.save(newUser);
        } else {
            User user = existUser.get();
            user.setAvatarUrl(picture);
            userRepository.save(user);
        }
    }
}