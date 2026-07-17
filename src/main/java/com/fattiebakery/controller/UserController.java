package com.fattiebakery.controller;

import com.fattiebakery.model.User;
import com.fattiebakery.repository.UserRepository;
import com.fattiebakery.service.UserService;
import com.fattiebakery.service.CartService; // Added to show cart count on profile
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

    @GetMapping("/profile")
    public String showProfile(Model model, Principal principal) {
        if (principal == null) {
            return "redirect:/login";
        }

        // 1. Lấy định danh từ Google (thường là Email)
        String loginId = principal.getName();
        System.out.println("===> Dang tim User trong DB voi ID: " + loginId);

        // 2. Tìm User: Phải check cả Username và Email bác nhé!
        User user = userRepository.findByUsername(loginId)
                .orElseGet(() -> userRepository.findByEmail(loginId).orElse(null));

        // 3. Nếu vẫn không thấy (Đây là lý do bác bị đá ra ngoài)
        if (user == null) {
            System.out.println("===> LOI: Khong tim thay User " + loginId + " trong Database!");
            return "redirect:/login?error=usernotfound";
        }

        model.addAttribute("user", user);
        return "user/profile"; // Hoặc đường dẫn file HTML profile của bác
    }

    @GetMapping("/profile/edit")
    public String editProfile(Model model, Principal principal) {
        if (principal == null) return "redirect:/login";

        String loginId = principal.getName();
        User user = userService.findByUsername(loginId)
                .orElseGet(() -> userService.findByEmail(loginId).orElse(null));

        if (user == null) return "redirect:/user/profile";

        model.addAttribute("user", user);
        return "user/edit_profile";
    }

    @PostMapping("/profile/update")
    public String updateProfile(@ModelAttribute("user") User userDetails,
                                @RequestParam(value = "avatarFile", required = false) MultipartFile file,
                                Principal principal) {
        if (principal == null) return "redirect:/login";

        String loginId = principal.getName();
        userService.updateUser(loginId, userDetails, file);

        return "redirect:/user/profile?success=true";
    }
}