package com.campus.activity.controller;

import com.campus.activity.common.PageResult;
import com.campus.activity.common.Result;
import com.campus.activity.entity.Activity;
import com.campus.activity.service.ActivityService;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/activity")
public class ActivityController {

    private final ActivityService activityService;

    public ActivityController(ActivityService activityService) {
        this.activityService = activityService;
    }

    @GetMapping("/list")
    public Result list(@RequestParam(required = false) String name,
                       @RequestParam(required = false) String type,
                       @RequestParam(required = false) String date,
                       @RequestParam(required = false) String status) {
        List<Activity> list = activityService.listAll(name, type, date, status);
        return Result.ok(list);
    }

    /** 分页列表 */
    @GetMapping("/page")
    public Result page(@RequestParam(defaultValue = "1") int page,
                       @RequestParam(defaultValue = "12") int pageSize,
                       @RequestParam(required = false) String name,
                       @RequestParam(required = false) String type,
                       @RequestParam(required = false) String date,
                       @RequestParam(required = false) String status) {
        PageResult<Activity> result = activityService.listPage(page, pageSize, name, type, date, status);
        return Result.ok(result);
    }

    @GetMapping("/{id}")
    public Result getById(@PathVariable Integer id) {
        Activity activity = activityService.getById(id);
        return activity != null ? Result.ok(activity) : Result.error("活动不存在");
    }

    @PostMapping("/add")
    public Result add(@RequestBody Activity activity) {
        activity.setStatus("待审核");
        activity.setCurrentParticipants(0);
        boolean ok = activityService.save(activity);
        return ok ? Result.ok("提交成功，等待审核") : Result.error("提交失败");
    }

    @PutMapping("/update")
    public Result update(@RequestBody Activity activity) {
        boolean ok = activityService.update(activity);
        return ok ? Result.ok("修改成功") : Result.error("修改失败");
    }

    @PutMapping("/review/{id}")
    public Result review(@PathVariable Integer id, @RequestParam String status) {
        if (!"已通过".equals(status) && !"已拒绝".equals(status)) {
            return Result.error(400, "审核状态只能为: 已通过/已拒绝");
        }
        boolean ok = activityService.review(id, status);
        return ok ? Result.ok("审核完成") : Result.error("操作失败");
    }

    @DeleteMapping("/{id}")
    public Result delete(@PathVariable Integer id) {
        boolean ok = activityService.delete(id);
        return ok ? Result.ok("删除成功") : Result.error("删除失败");
    }

    @GetMapping("/schedule")
    public Result schedule(@RequestParam(required = false) String date) {
        List<Map<String, Object>> schedule = activityService.getSchedule(date);
        return Result.ok(schedule);
    }
}
