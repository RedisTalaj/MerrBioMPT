package com.mpt.merrbiompt.controller;

import com.mpt.merrbiompt.entity.User;
import com.mpt.merrbiompt.service.UserService;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

@Controller
@RequestMapping("/users")
public class UserController {
    @Autowired
    private UserService userService;

    @GetMapping
    public String getAllUsers(Model model){
        List<User> users = userService.getAllUsers();
        model.addAttribute("users", users);
        return "AdminPage/index";
    }

    @GetMapping("/signup")
    public String goToAddUsers(Model model){
        model.addAttribute("user", new User());
        return "signup/signup";
    }

    @PostMapping("/signup")
    public String addUser(@ModelAttribute("user")User user, Model model){
        User userExists = userService.getUserByEmail(user.getEmail());

        if (userExists != null && userExists.getEmail().equals(user.getEmail())) {
            model.addAttribute("errorMessageEmail", "Email already in use");
            return "signup/signup";
        }
        if (user.getPassword().length() < 7) {
            model.addAttribute("errorMessagePassword", "Password must be at least 7 characters long.");
            return "signup/signup";
        }
        if (!user.getPassword().matches(".*[A-Z].*")) {
            model.addAttribute("errorMessagePassword", "Password must contain at least one uppercase letter.");
            return "signup/signup";
        }
        userService.addUser(user);
        return "redirect:/users/login";
    }

    @GetMapping("/login")
    public String goToLogin(Model model){
        model.addAttribute("user", new User());
        return "login/login";
    }

    @PostMapping("/login")
    public String login(@ModelAttribute("user")User user, RedirectAttributes redirectAttributes, HttpSession session) {
        User logedInUser = userService.getUserByEmail(user.getEmail());
        boolean passwordAndEmailIncorrect = userService.getUserByEmailAndPassword(user.getEmail(), user.getPassword());
        if(!passwordAndEmailIncorrect){
            redirectAttributes.addFlashAttribute("errorMessageEmailOrPassword", "Email or password incorrect");
            return "redirect:/users/login";
        }
        else{
            if (logedInUser.getStatus().equals("Inactive")) {
                redirectAttributes.addFlashAttribute("errorMessageEmailOrPassword", "User is inactive");
                return "redirect:/users/login";
            }
            else if (logedInUser.getStatus().equals("Active")) {
                if (logedInUser.getPassword().equals(user.getPassword())) {
                    user.setRole(logedInUser.getRole());
                    if (logedInUser.getRole().equals("Admin")) {
                        session.setAttribute("user", logedInUser);
                        return "redirect:/main/MainPage";
                    } else {
                        redirectAttributes.addFlashAttribute("errorMessageEmailOrPassword",
                                "Logged in successfully");
                        session.setAttribute("user", logedInUser);
                        return "redirect:/main/MainPage";
                    }
                }
            }
            else{
                redirectAttributes.addFlashAttribute("errorMessageEmailOrPassword", "Email or password incorrect");
                return "redirect:/users/login";
            }
        }
        return "";
    }

    @GetMapping("/create")
    public String goToCreateUser(Model model){
        model.addAttribute("user", new User());
        return "AdminPage/create_user";
    }

    @PostMapping("/create")
    public String createUser(@ModelAttribute("user") User user, Model model) {
        User userExists = userService.getUserByEmail(user.getEmail());

        if (userExists != null && userExists.getEmail().equals(user.getEmail())) {
            model.addAttribute("errorMessage", "Email already in use");
            return "AdminPage/create_user";
        }
        if(user.getPassword().length() < 7){
            model.addAttribute("errorMessage", "Password must be at least 7 characters long.");
            return "AdminPage/create_user";
        }
        if (!user.getPassword().matches(".*[A-Z].*")) {
            model.addAttribute("errorMessage", "Password must contain at least one uppercase letter.");
            return "AdminPage/create_user";
        }
        else {
            userService.createUser(user);
            return "redirect:/users";
        }
    }

    @GetMapping("/delete/{id}")
    public String goToDeleteUser(Model model){
        model.addAttribute("user", new User());
        return "AdminPage/index";
    }

    @PostMapping("/delete/{id}")
    public String deleteUser(@ModelAttribute("user") User user, @PathVariable Long id, RedirectAttributes redirectAttributes){
        User userAdmin = userService.getUserById(id);
        if (!"Admin".equals(userAdmin.getRole())) {
            userService.deleteUser(id);
            return "redirect:/users";
        } else {
            redirectAttributes.addFlashAttribute("errorMessageAdmin",
                    "This user is Admin and can't be deleted");
            return "redirect:/users";
        }
    }

    @GetMapping("/search")
    public String searchUsers(@RequestParam("keyword") String keyword, Model model) {
        List<User> users = userService.searchByUsername(keyword);
        if (users.isEmpty()) {
            model.addAttribute("errorMessageSearch", "No users found with the given keyword.");
        } else {
            model.addAttribute("users", users);
        }
        model.addAttribute("keyword", keyword);
        return "AdminPage/index";
    }

    @GetMapping("/update/{id}")
    public String goToUpdateUser(Model model, @PathVariable Long id){
        model.addAttribute("user", userService.getUserById(id));
        return "AdminPage/update_user";
    }

    @PostMapping("/update/{id}")
    public String updateUser(@ModelAttribute User user, @PathVariable Long id, RedirectAttributes redirectAttributes){
        User userAdmin = userService.getUserById(id);
        if("Admin".equals(userAdmin.getRole())){
            redirectAttributes.addFlashAttribute("errorMessageAdmin", "This user is admin and can't change its data.");
            return "redirect:/users/update/{id}";
        }else{
            userService.updateUser(id, user);
            return "redirect:/users";
        }
    }
}
