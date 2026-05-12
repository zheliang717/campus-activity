package com.campus.activity.controller;

import com.campus.activity.common.Result;
import com.campus.activity.entity.Person;
import com.campus.activity.service.PersonService;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@RestController
@RequestMapping("/api/person")
public class PersonController {

    private final PersonService personService;

    // 免登录Token存储 (token -> personId)
    public static final ConcurrentHashMap<String, Integer> AUTO_LOGIN_TOKENS = new ConcurrentHashMap<>();

    public PersonController(PersonService personService) {
        this.personService = personService;
    }

    /**
     * 登录 (支持"记住我")
     */
    @PostMapping("/login")
    public Result login(@RequestBody Map<String, String> params,
                        HttpSession session, HttpServletResponse response) {
        String username = params.get("username");
        String password = params.get("password");
        Boolean remember = Boolean.parseBoolean(params.get("remember"));

        if (username == null || password == null) {
            return Result.error(400, "用户名和密码不能为空");
        }
        Person person = personService.login(username, password);
        if (person == null) {
            return Result.error(400, "用户名或密码错误");
        }
        person.setPassword(null);
        session.setAttribute("loginUser", person);

        // 七天免登录
        if (Boolean.TRUE.equals(remember)) {
            String token = UUID.randomUUID().toString().replace("-", "");
            AUTO_LOGIN_TOKENS.put(token, person.getPersonId());
            Cookie cookie = new Cookie("auto_token", token);
            cookie.setMaxAge(7 * 24 * 60 * 60); // 7天
            cookie.setPath("/");
            cookie.setHttpOnly(true);
            response.addCookie(cookie);
        }

        return Result.ok("登录成功", person);
    }

    /**
     * 根据Token自动登录
     */
    @GetMapping("/auto-login")
    public Result autoLogin(@CookieValue(value = "auto_token", required = false) String token,
                            HttpSession session) {
        if (token == null || !AUTO_LOGIN_TOKENS.containsKey(token)) {
            return Result.error(401, "无效的登录凭证");
        }
        Integer personId = AUTO_LOGIN_TOKENS.get(token);
        Person person = personService.getById(personId);
        if (person == null) {
            AUTO_LOGIN_TOKENS.remove(token);
            return Result.error(401, "用户不存在");
        }
        person.setPassword(null);
        session.setAttribute("loginUser", person);
        return Result.ok(person);
    }

    @GetMapping("/current")
    public Result current(HttpSession session) {
        Person person = (Person) session.getAttribute("loginUser");
        if (person == null) return Result.error(401, "未登录");
        return Result.ok(person);
    }

    @PostMapping("/logout")
    public Result logout(HttpSession session, HttpServletResponse response,
                         @CookieValue(value = "auto_token", required = false) String token) {
        if (token != null) {
            AUTO_LOGIN_TOKENS.remove(token);
            Cookie cookie = new Cookie("auto_token", "");
            cookie.setMaxAge(0);
            cookie.setPath("/");
            response.addCookie(cookie);
        }
        session.invalidate();
        return Result.ok("已退出登录");
    }

    @PostMapping("/register")
    public Result register(@RequestBody Person person) {
        person.setRole("student");
        boolean ok = personService.register(person);
        return ok ? Result.ok("注册成功") : Result.error("注册失败");
    }

    @PutMapping("/update")
    public Result update(@RequestBody Person person) {
        personService.update(person);
        return Result.ok("修改成功");
    }
}
