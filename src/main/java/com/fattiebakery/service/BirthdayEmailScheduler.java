package com.fattiebakery.service;

import com.fattiebakery.model.User;
import com.fattiebakery.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.List;

@Component
public class BirthdayEmailScheduler {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private EmailService emailService;

    // Hàm này sẽ tự động chạy vào lúc 00:00 sáng mỗi ngày
    @Scheduled(cron = "0 0 0  * * *")
    public void sendBirthdayGreetings() {
        LocalDate today = LocalDate.now();
        int day = today.getDayOfMonth();
        int month = today.getMonthValue();

        System.out.println("===> Đang quét danh sách khách hàng sinh nhật ngày: " + day + "/" + month);

        // 👉 Sửa lại tên hàm cho đúng với UserRepository vừa viết
        List<User> birthdayUsers = userRepository.findByDayAndMonthOfBirth(day, month);

        for (User user : birthdayUsers) {
            if (user.getEmail() != null && !user.getEmail().isEmpty()) {
                String subject = "🎁 Chúc mừng sinh nhật từ Fattie Bakery!";
                String content = "Chào " + (user.getFullName() != null ? user.getFullName() : "quý khách") +
                        ",\n\nFattie Bakery chúc bạn có một ngày sinh nhật thật ngọt ngào và hạnh phúc! " +
                        "Món quà nhỏ gửi tặng bạn là mã giảm giá 20% cho đơn hàng hôm nay tại tiệm.";

                try {
                    emailService.sendEmail(user.getEmail(), subject, content);
                    System.out.println("===> Đã gửi email chúc mừng sinh nhật tới: " + user.getEmail());
                } catch (Exception e) {
                    System.out.println("===> Lỗi gửi email cho " + user.getEmail() + ": " + e.getMessage());
                }
            }
        }
    }
}