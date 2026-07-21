package com.fattiebakery.controller;

import com.fattiebakery.model.User;
import com.fattiebakery.repository.UserRepository;
import com.fattiebakery.service.UserService;
import com.fattiebakery.service.CartService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.security.Principal;

@Controller
@RequestMapping("/user")
public class UserController {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private UserService userService;

    @Autowired
    private CartService cartService;

    // 1. Hiển thị trang thông tin cá nhân
    @GetMapping("/profile")
    public String showProfile(Model model, Principal principal) {
        if (principal == null) {
            return "redirect:/login";
        }

        String loginId = principal.getName();
        User user = userRepository.findByUsername(loginId)
                .orElseGet(() -> userRepository.findByEmail(loginId).orElse(null));

        if (user == null) {
            return "redirect:/login?error=usernotfound";
        }

        model.addAttribute("user", user);
        return "user/profile";
    }

    // 2. Mở trang chỉnh sửa hồ sơ (Khớp với file edit_profile.html của bác)
    @GetMapping("/profile/edit")
    public String editProfile(Model model, Principal principal) {
        if (principal == null) {
            return "redirect:/login";
        }

        String loginId = principal.getName();
        User user = userService.findByUsername(loginId)
                .orElseGet(() -> userService.findByEmail(loginId).orElse(null));

        if (user == null) {
            return "redirect:/user/profile";
        }

        model.addAttribute("user", user);
        return "user/edit_profile";
    }

    // 3. Xử lý lưu thay đổi hồ sơ & upload ảnh đại diện (Đã fix chuẩn đường dẫn POST)
    @PostMapping("/profile/update")
    public String updateProfile(@ModelAttribute("user") User userDetails,
                                @RequestParam(value = "avatarFile", required = false) MultipartFile file,
                                Principal principal) {
        if (principal == null) {
            return "redirect:/login";
        }

        String loginId = principal.getName();

        // Gọi Service xử lý lưu thông tin vào database và upload ảnh
        userService.updateUser(loginId, userDetails, file);

        return "redirect:/user/profile?success=true";
    }
}