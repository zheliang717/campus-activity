package com.campus.activity.config;

import com.campus.activity.controller.PersonController;
import com.campus.activity.entity.Person;
import com.campus.activity.mapper.PersonMapper;
import jakarta.servlet.*;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
public class LoginFilter implements Filter {

    private final PersonMapper personMapper;

    public LoginFilter(PersonMapper personMapper) {
        this.personMapper = personMapper;
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        HttpServletRequest req = (HttpServletRequest) request;
        HttpServletResponse resp = (HttpServletResponse) response;
        String path = req.getRequestURI();

        // 放行静态资源、API、登录页
        if (path.startsWith("/pages/login") || path.startsWith("/api/") || path.endsWith(".css") || path.endsWith(".js")
                || path.endsWith(".ico") || path.endsWith(".png")) {
            chain.doFilter(request, response);
            return;
        }

        if ("/".equals(path) || path.isEmpty()) {
            resp.sendRedirect("/pages/login.html");
            return;
        }

        HttpSession session = req.getSession(false);
        if (session != null && session.getAttribute("loginUser") != null) {
            chain.doFilter(request, response);
            return;
        }

        // 尝试Cookie自动登录
        Cookie[] cookies = req.getCookies();
        if (cookies != null) {
            for (Cookie cookie : cookies) {
                if ("auto_token".equals(cookie.getName())) {
                    String token = cookie.getValue();
                    Integer personId = PersonController.AUTO_LOGIN_TOKENS.get(token);
                    if (personId != null) {
                        Person person = personMapper.selectById(personId);
                        if (person != null) {
                            person.setPassword(null);
                            req.getSession(true).setAttribute("loginUser", person);
                            chain.doFilter(request, response);
                            return;
                        }
                    }
                }
            }
        }

        resp.sendRedirect("/pages/login.html");
    }
}
