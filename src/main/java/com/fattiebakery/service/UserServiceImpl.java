package com.fattiebakery.service;

import com.fattiebakery.model.User;
import com.fattiebakery.repository.UserRepository;
import com.fattiebakery.service.UserService;
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
    public void updateUser(String username, User userDetails, MultipartFile file) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy người dùng: " + username));

        // 1. Cập nhật thông tin chữ
        user.setFullName(userDetails.getFullName());
        user.setPhone(userDetails.getPhone());
        user.setAddress(userDetails.getAddress());

        // 2. Xử lý File ảnh nếu bác Hải Nam có tải lên
        if (file != null && !file.isEmpty()) {
            try {
                // Định nghĩa thư mục lưu (phải khớp với cấu hình static của bác)
                String uploadDir = "src/main/resources/static/uploads/avatars/";
                Path uploadPath = Paths.get(uploadDir);

                // Tự tạo thư mục nếu chưa có
                if (!Files.exists(uploadPath)) {
                    Files.createDirectories(uploadPath);
                }

                // Đổi tên file để không bị trùng (dùng UUID)
                String fileName = UUID.randomUUID().toString() + "_" + file.getOriginalFilename();
                Path filePath = uploadPath.resolve(fileName);

                // Lưu file vào ổ cứng
                Files.copy(file.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);

                // Lưu đường dẫn vào database để hiển thị ra HTML
                user.setAvatarUrl("/uploads/avatars/" + fileName);

            } catch (IOException e) {
                throw new RuntimeException("Bác Hải Nam ơi, lỗi lưu ảnh rồi: " + e.getMessage());
            }
        }

        userRepository.save(user);
    }
}