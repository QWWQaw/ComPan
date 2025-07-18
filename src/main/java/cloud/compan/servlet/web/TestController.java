package cloud.compan.servlet.web;

import cloud.compan.servlet.annotations.component.Controller;
import cloud.compan.servlet.annotations.component.GetMapping;
import cloud.compan.servlet.annotations.component.PostMapping;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;

import cloud.compan.servlet.annotations.component.PathVariable;
import cloud.compan.servlet.annotations.component.RequestMapping;
import cloud.compan.servlet.annotations.component.RequestMethod;

@Controller
@RequestMapping(path = "/api")
public class TestController {

    @GetMapping(path = "/test")
    public void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        resp.getWriter().write("GET request successful");
    }

    @PostMapping(path = "/test")
    public void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        resp.getWriter().write("POST request successful");
    }

    @RequestMapping(path = "/any", method = {RequestMethod.GET, RequestMethod.POST})
    public void doAny(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        resp.getWriter().write("Request successful for any method");
    }

    @GetMapping(path = "/user/{id}")
    public void getUserById(@PathVariable("id") int id, HttpServletResponse resp) throws IOException {
        resp.getWriter().write("User ID: " + id);
    }
}