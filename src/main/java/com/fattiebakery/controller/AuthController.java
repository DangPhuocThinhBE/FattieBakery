package com.fattiebakery.controller;

import com.fattiebakery.model.User;
import com.fattiebakery.service.UserService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.security.Principal;

@Controller
public class AuthController {

    @Autowired
    private UserService userService;

    @GetMapping("/login")
    public String loginPage(@RequestParam(required = false) String error,
                             @RequestParam(required = false) String logout,
                             Model model) {
        if (error != null) model.addAttribute("error", "Tên đăng nhập hoặc mật khẩu không đúng!");
        if (logout != null) model.addAttribute("message", "Đăng xuất thành công!");
        return "auth/login";
    }

    @GetMapping("/register")
    public String registerPage(Model model) {
        model.addAttribute("user", new User());
        return "auth/register";
    }

    @PostMapping("/register")
    public String register(@Valid @ModelAttribute("user") User user,
                           BindingResult result,
                           @RequestParam String confirmPassword,
                           RedirectAttributes redirectAttributes,
                           Model model) {
        if (result.hasErrors()) return "auth/register";

        if (!user.getPassword().equals(confirmPassword)) {
            model.addAttribute("passwordError", "Mật khẩu xác nhận không khớp!");
            return "auth/register";
        }
        if (userService.existsByUsername(user.getUsername())) {
            model.addAttribute("usernameError", "Tên đăng nhập đã tồn tại!");
            return "auth/register";
        }
        if (userService.existsByEmail(user.getEmail())) {
            model.addAttribute("emailError", "Email đã được sử dụng!");
            return "auth/register";
        }

        userService.registerUser(user);
        redirectAttributes.addFlashAttribute("success", "Đăng ký thành công! Vui lòng đăng nhập.");
        return "redirect:/login";
    }
    // --- 1. HIỂN THỊ TRANG QUÊN MẬT KHẨU ---
    @GetMapping("/forgot-password")
    public String forgotPasswordPage() {
        return "auth/forgot-password";
    }

    // --- 2. XỬ LÝ GỬI YÊU CẦU QUÊN MẬT KHẨU ---
    @PostMapping("/forgot-password")
    public String processForgotPassword(@RequestParam("emailOrPhone") String input, RedirectAttributes ra) {
        try {
            boolean exists = userService.findByEmail(input).isPresent()
                    || userService.findByUsername(input).isPresent();

            if (!exists) {
                throw new RuntimeException("Thông tin tài khoản không tồn tại trong hệ thống!");
            }

            // Chuyển hướng sang trang đặt lại mật khẩu mới và truyền kèm thông tin định danh
            return "redirect:/reset-password?email=" + input;
        } catch (Exception e) {
            ra.addFlashAttribute("error", e.getMessage());
            return "redirect:/forgot-password";
        }
    }

    // --- HIỂN THỊ TRANG ĐẶT LẠI MẬT KHẨU MỚI ---
    @GetMapping("/reset-password")
    public String resetPasswordPage(@RequestParam("email") String email, Model model) {
        model.addAttribute("email", email);
        return "auth/reset-password";
    }

    // --- XỬ LÝ LƯU MẬT KHẨU MỚI ---
    @PostMapping("/reset-password")
    public String processResetPassword(@RequestParam("email") String email,
                                       @RequestParam("newPassword") String newPassword,
                                       @RequestParam("confirmPassword") String confirmPassword,
                                       RedirectAttributes ra) {
        if (!newPassword.equals(confirmPassword)) {
            ra.addFlashAttribute("error", "Mật khẩu xác nhận không khớp!");
            return "redirect:/reset-password?email=" + email;
        }

        try {
            userService.updatePasswordByEmail(email, newPassword);
            ra.addFlashAttribute("success", "Đổi mật khẩu thành công! Vui lòng đăng nhập lại.");
            return "redirect:/login";
        } catch (Exception e) {
            ra.addFlashAttribute("error", e.getMessage());
            return "redirect:/reset-password?email=" + email;
        }
    }

    // --- 3. HIỂN THỊ TRANG ĐỔI MẬT KHẨU ---
    @GetMapping("/change-password")
    public String changePasswordPage() {
        return "auth/change-password";
    }

    // --- 4. XỬ LÝ ĐỔI MẬT KHẨU ---
    @PostMapping("/change-password")
    public String processChangePassword(@RequestParam("oldPassword") String oldPassword,
                                        @RequestParam("newPassword") String newPassword,
                                        @RequestParam("confirmPassword") String confirmPassword,
                                        Principal principal,
                                        RedirectAttributes ra) {
        if (principal == null) {
            return "redirect:/login";
        }

        if (!newPassword.equals(confirmPassword)) {
            ra.addFlashAttribute("error", "Mật khẩu mới và xác nhận mật khẩu không khớp nhau!");
            return "redirect:/change-password";
        }

        try {
            String currentUsername = principal.getName();
            userService.changePassword(currentUsername, oldPassword, newPassword);
            ra.addFlashAttribute("success", "Đổi mật khẩu thành công!");
        } catch (Exception e) {
            ra.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/change-password";
    }
}
