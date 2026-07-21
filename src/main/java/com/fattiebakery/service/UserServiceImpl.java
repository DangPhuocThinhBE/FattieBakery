package com.fattiebakery.service;

import com.fattiebakery.model.User;
import com.fattiebakery.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Optional;
import java.util.UUID;

@Service
public class UserServiceImpl extends UserService {

    @Autowired
    private UserRepository userRepository;

    @Override
    public Optional<User> findByUsername(String username) {
        return userRepository.findByUsername(username);
    }

    @Override
    public void updateUser(String loginId, User userDetails, MultipartFile file) {
        // Tìm user theo username hoặc email
        User user = userRepository.findByUsername(loginId)
                .orElseGet(() -> userRepository.findByEmail(loginId)
                        .orElseThrow(() -> new RuntimeException("Không tìm thấy người dùng: " + loginId)));

        // 1. Cập nhật thông tin chữ cơ bản
        user.setFullName(userDetails.getFullName());
        user.setPhone(userDetails.getPhone());
        user.setAddress(userDetails.getAddress());

        // 👉 CẬP NHẬT NGÀY SINH TỪ FORM GỬI LÊN
        user.setDateOfBirth(userDetails.getDateOfBirth());

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

        // 3. Lưu toàn bộ xuống Database TiDB
        userRepository.save(user);
    }
}