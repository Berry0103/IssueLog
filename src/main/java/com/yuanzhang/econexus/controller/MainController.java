package com.yuanzhang.econexus.controller;

import com.yuanzhang.econexus.model.User;
import com.yuanzhang.econexus.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;

@Controller
@RequiredArgsConstructor
public class MainController {
    private final UserService userService;

    @GetMapping("/main")
    public String home() {
        return "redirect:/index.html";
    }

    @GetMapping("/home")
    public String login() {
        return "redirect:/index.html";
    }

    @GetMapping("/register")
    public String register(Model model) {
        model.addAttribute("user", new User());
        return "register";
    }

    @PostMapping("/register")
    public String registerUser(@ModelAttribute User user, Model model) {
        try {
            userService.registerUser(user);
            return "redirect:/login?success";
        } catch (IllegalArgumentException e) {
            model.addAttribute("error", e.getMessage());
            return "register";
        }
    }

    @GetMapping("/dashboard")
    public String dashboard(Model model, Authentication authentication) {
        User user = (User) authentication.getPrincipal();
        model.addAttribute("user", user);
        return "dashboard";
    }

    @GetMapping("/profile")
    public String profile(Model model, Authentication authentication) {
        User user = (User) authentication.getPrincipal();
        model.addAttribute("user", user);
        return "profile";
    }

    @PostMapping("/profile")
    public String updateProfile(@ModelAttribute User user, Authentication authentication, Model model) {
        try {
            User currentUser = (User) authentication.getPrincipal();
            user.setUserIndex(currentUser.getUserIndex());
            user.setUsername(currentUser.getUsername()); // 不允许修改用户名
            userService.updateUser(user);
            return "redirect:/dashboard?updated";
        } catch (IllegalArgumentException e) {
            model.addAttribute("error", e.getMessage());
            return "profile";
        }
    }
}
