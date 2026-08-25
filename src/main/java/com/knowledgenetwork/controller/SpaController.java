package com.knowledgenetwork.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class SpaController {

    @GetMapping(value = {
            "/",
            "/{path:^(?!api|actuator|v3|swagger-ui|assets|favicon\\.ico).*$}",
            "/{path:^(?!api|actuator|v3|swagger-ui|assets|favicon\\.ico).*$}/**"
    })
    public String forwardToIndex() {
        return "forward:/index.html";
    }
}
