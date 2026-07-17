package com.fattiebakery.service;

import com.fattiebakery.model.Role;
import com.fattiebakery.model.User;
import com.fattiebakery.repository.RoleRepository;
import com.fattiebakery.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.*;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.*;
import java.util.Optional;
import java.util.UUID;

@Service
@Transactional
public class UserService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    // 1. Đăng ký người dùng mới (Dùng cho Form đăng ký)
    public User registerUser(User user) {
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        Role userRole = roleRepository.findByName("ROLE_USER")
                .orElseThrow(() -> new RuntimeException("Lỗi: Không tìm thấy Role USER"));
        user.getRoles().add(userRole);
        user.setActive(true);
        return userRepository.save(user);
    }

    // 2. Tìm kiếm người dùng (Quan trọng để fix lỗi Controller)
    public Optional<User> findByUsername(String username) {
        return userRepository.findByUsername(username);
    }

    public Optional<User> findByEmail(String email) {
        return userRepository.findByEmail(email);
    }

    public Optional<User> findById(Long id) {
        return userRepository.findById(id);
    }

    // 3. Kiểm tra tồn tại
    public boolean existsByUsername(String username) {
        return userRepository.existsByUsername(username);
    }

    public boolean existsByEmail(String email) {
        return userRepository.existsByEmail(email);
    }

    // 4. Cập nhật hồ sơ (Tích hợp xử lý ảnh và hỗ trợ OAuth2)
    public void updateUser(String loginId, User userDetails, MultipartFile file) {
        // Tìm user dựa trên loginId (có thể là username hoặc email từ Google)
        User user = userRepository.findByUsername(loginId)
                .orElseGet(() -> userRepository.findByEmail(loginId)
                        .orElseThrow(() -> new RuntimeException("Không tìm thấy người dùng: " + loginId)));

        // Cập nhật thông tin cơ bản
        user.setFullName(userDetails.getFullName());
        user.setPhone(userDetails.getPhone());
        user.setAddress(userDetails.getAddress());

        // Xử lý upload ảnh đại diện
        if (file != null && !file.isEmpty()) {
            try {
                String uploadDir = "uploads/avatars/";
                Path uploadPath = Paths.get(uploadDir);

                if (!Files.exists(uploadPath)) {
                    Files.createDirectories(uploadPath);
                }

                String fileName = UUID.randomUUID().toString() + "_" + file.getOriginalFilename();
                Path filePath = uploadPath.resolve(fileName);
                Files.copy(file.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);

                // Lưu đường dẫn vào database
                user.setAvatarUrl("/uploads/avatars/" + fileName);
            } catch (IOException e) {
                throw new RuntimeException("Lỗi hệ thống khi lưu ảnh: " + e.getMessage());
            }
        }
        userRepository.save(user);
    }

    // 5. Đổi mật khẩu
    public void changePassword(User user, String newPassword) {
        user.setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(user);
    }

    // 6. Các hàm hỗ trợ Admin
    public Page<User> searchUsers(String keyword, int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("username").ascending());
        if (keyword != null && !keyword.isBlank()) {
            return userRepository.searchUsers(keyword, pageable);
        }
        return userRepository.findAll(pageable);
    }

    public void toggleUserStatus(Long id) {
        userRepository.findById(id).ifPresent(u -> {
            u.setActive(!u.getActive());
            userRepository.save(u);
        });
    }

    public long countActiveUsers() {
        return userRepository.countByActiveTrue();
    }

    public User saveUser(User user) {
        return userRepository.save(user);
    }
}