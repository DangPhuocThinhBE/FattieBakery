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
}
