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

    public User registerUser(User user) {
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        Role userRole = roleRepository.findByName("ROLE_USER").orElseThrow(() -> new RuntimeException("Role USER không tồn tại"));
        user.getRoles().add(userRole);
        user.setActive(true);
        return userRepository.save(user);
    }

    public Optional<User> findByUsername(String username) {
        return userRepository.findByUsername(username);
    }

    public Optional<User> findByEmail(String email) {
        return userRepository.findByEmail(email);
    }

    public Optional<User> findById(Long id) {
        return userRepository.findById(id);
    }

    public boolean existsByUsername(String username) {
        return userRepository.existsByUsername(username);
    }

    public boolean existsByEmail(String email) {
        return userRepository.existsByEmail(email);
    }

    // HÀM CẬP NHẬT THÔNG TIN & ẢNH ĐẠI DIỆN ĐÃ ĐƯỢC GỘP CHUẨN ĐẦY ĐỦ
    public void updateUser(String loginId, User userDetails, MultipartFile file) {
        // Tìm user theo username hoặc email
        User user = userRepository.findByUsername(loginId)
                .orElseGet(() -> userRepository.findByEmail(loginId)
                        .orElseThrow(() -> new RuntimeException("Không tìm thấy người dùng: " + loginId)));

        // 1. Cập nhật đầy đủ các trường thông tin cơ bản
        user.setFullName(userDetails.getFullName());
        user.setPhone(userDetails.getPhone());
        user.setAddress(userDetails.getAddress());
        user.setDateOfBirth(userDetails.getDateOfBirth()); // Đã có ngày sinh

        // 2. Xử lý File ảnh đại diện nếu có tải lên
        if (file != null && !file.isEmpty()) {
            try {
                String uploadDir = "src/main/resources/static/uploads/avatars/";
                Path uploadPath = Paths.get(uploadDir);

                if (!Files.exists(uploadPath)) {
                    Files.createDirectories(uploadPath);
                }

                String fileName = UUID.randomUUID().toString() + "_" + file.getOriginalFilename();
                Path filePath = uploadPath.resolve(fileName);

                Files.copy(file.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);

                user.setAvatarUrl("/uploads/avatars/" + fileName);

            } catch (IOException e) {
                throw new RuntimeException("Lỗi lưu file ảnh đại diện: " + e.getMessage());
            }
        }

        // 3. Lưu xuống Database
        userRepository.save(user);
    }

    public void changePassword(User user, String newPassword) {
        user.setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(user);
    }

    public Page<User> searchUsers(String keyword, int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("username").ascending());
        return (keyword != null && !keyword.isBlank()) ? userRepository.searchUsers(keyword, pageable) : userRepository.findAll(pageable);
    }

    public void toggleUserStatus(Long id) {
        userRepository.findById(id).ifPresent(u -> {
            u.setActive(!u.getActive());
            userRepository.save(u);
        });
    }

    public long countActiveUsers() { return userRepository.countByActiveTrue(); }
    public long countAllUsers() { return userRepository.count(); }
    public User saveUser(User user) { return userRepository.save(user); }
}