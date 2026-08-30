package com.quietjun.ssafymng.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class ViewController {

    @GetMapping({"/", "/login"})
    public String index() {
        return "forward:/index.html";
    }
}
