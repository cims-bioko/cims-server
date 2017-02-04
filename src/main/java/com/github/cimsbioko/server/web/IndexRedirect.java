package com.github.cimsbioko.server.web;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

@Controller
@RequestMapping("/")
public class IndexRedirect {
    @RequestMapping(method = RequestMethod.GET)
    public String showWelcome() {
        return "redirect:welcome.faces";
    }
}
