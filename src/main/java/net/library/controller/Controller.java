package net.library.controller;

import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping()
public class Controller {

    @GetMapping()
    public String get(@RequestParam(value = "get", defaultValue = "GET METHOD") String get) {
        return String.format("Hello %s!", get);
    }

    @PostMapping()
    public String post(@RequestParam(value = "post", defaultValue = "POST METHOD") String post) {
        return String.format("Hello %s!", post);
    }

    @PutMapping()
    public String put(@RequestParam(value = "put", defaultValue = "PUT METHOD") String put) {
        return String.format("Hello %s!", put);
    }

    @DeleteMapping()
    public String delete(@RequestParam(value = "delete", defaultValue = "DELETE METHOD") String delete) {
        return String.format("Hello %s!", delete);
    }

    @PatchMapping()
    public String patch(@RequestParam(value = "patch", defaultValue = "PATCH METHOD") String patch) {
        return String.format("Hello %s!", patch);
    }
}
