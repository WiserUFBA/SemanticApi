package com.eudes.semanticMake.web;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class MainController {

    @GetMapping("/")
    public String index() {
        return "index";
    }

    @GetMapping("/SemanticApiInterface")
    public String saveFromRDFModel2() {
        return "semanticApiInterface";
    }

}
