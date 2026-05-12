package com.campus.activity.controller;

import com.campus.activity.common.PageResult;
import com.campus.activity.common.Result;
import com.campus.activity.entity.Person;
import com.campus.activity.entity.Registration;
import com.campus.activity.service.RegistrationService;
import jakarta.servlet.http.HttpSession;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/registration")
public class RegistrationController {

    private final RegistrationService registrationService;

    public RegistrationController(RegistrationService registrationService) {
        this.registrationService = registrationService;
    }

    @PostMapping("/register")
    public Result register(@RequestParam Integer activityId, HttpSession session) {
        Person user = (Person) session.getAttribute("loginUser");
        if (user == null) return Result.error(401, "请先登录");
        boolean ok = registrationService.register(activityId, user.getPersonId());
        return ok ? Result.ok("报名成功") : Result.error("报名失败，可能已报名或名额已满");
    }

    @PostMapping("/cancel/{regId}")
    public Result cancel(@PathVariable Integer regId, HttpSession session) {
        Person user = (Person) session.getAttribute("loginUser");
        if (user == null) return Result.error(401, "请先登录");
        boolean ok = registrationService.cancel(regId, user.getPersonId());
        return ok ? Result.ok("已取消报名") : Result.error("取消失败");
    }

    @GetMapping("/my")
    public Result myRegistrations(HttpSession session) {
        Person user = (Person) session.getAttribute("loginUser");
        if (user == null) return Result.error(401, "请先登录");
        List<Registration> list = registrationService.listByPerson(user.getPersonId());
        return Result.ok(list);
    }

    /** 分页版我的报名 */
    @GetMapping("/my/page")
    public Result myRegistrationsPage(@RequestParam(defaultValue = "1") int page,
                                      @RequestParam(defaultValue = "10") int pageSize,
                                      HttpSession session) {
        Person user = (Person) session.getAttribute("loginUser");
        if (user == null) return Result.error(401, "请先登录");
        PageResult<Registration> result = registrationService.listByPersonPage(page, pageSize, user.getPersonId());
        return Result.ok(result);
    }

    @GetMapping("/activity/{activityId}")
    public Result listByActivity(@PathVariable Integer activityId) {
        List<Registration> list = registrationService.listByActivity(activityId);
        return Result.ok(list);
    }

    @GetMapping("/check")
    public Result check(@RequestParam Integer activityId, HttpSession session) {
        Person user = (Person) session.getAttribute("loginUser");
        if (user == null) return Result.ok(false);
        boolean registered = registrationService.isRegistered(activityId, user.getPersonId());
        return Result.ok(registered);
    }

    @GetMapping("/count/{activityId}")
    public Result count(@PathVariable Integer activityId) {
        long count = registrationService.countByActivity(activityId);
        return Result.ok(count);
    }
}
