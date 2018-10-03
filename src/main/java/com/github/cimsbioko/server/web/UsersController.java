package com.github.cimsbioko.server.web;

import com.github.cimsbioko.server.dao.UserRepository;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class UsersController {

    private UserRepository repo;

    UsersController(UserRepository userRepo) {
        this.repo = userRepo;
    }

    @GetMapping("/users")
    public String users(ModelMap model, @RequestParam(name = "p", defaultValue = "0") Integer page) {
        model.addAttribute("users", repo.findAll(new PageRequest(page, 10)));
        return "users";
    }
}
