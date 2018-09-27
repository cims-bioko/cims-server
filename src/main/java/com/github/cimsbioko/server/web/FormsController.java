package com.github.cimsbioko.server.web;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class FormsController {
    @GetMapping("/forms")
    public String forms() {
        return "forms";
    }
}
