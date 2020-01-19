package com.example.demo.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class HelloController {

    @GetMapping(path = "hello", produces = "text/html")
    public String hello() {
        return "hello";
    }

    @GetMapping(path = "success", produces = "text/html")
    public String success() {
        return "成功";
    }

    @GetMapping(path = "page1", produces = "text/html")
    public String page1() {
        return "ページ1";
    }

    

}