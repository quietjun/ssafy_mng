package com.quietjun.ssafymng.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class SpaForwardController {

    /**
     * SPA History Mode 지원:
     * /api, /assets, /uploads, /favicon, 확장자가 있는 파일 등을 제외한 모든 클라이언트 라우트를 index.html로 포워딩
     */
    @GetMapping(value = {
        "/{path:[^\\.]*}",
        "/**/{path:[^\\.]*}"
    })
    public String forwardSpaRoutes() {
        return "forward:/index.html";
    }
}
