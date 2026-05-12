package com.campus.activity.controller;

import com.campus.activity.common.PageResult;
import com.campus.activity.common.Result;
import com.campus.activity.entity.Venue;
import com.campus.activity.service.VenueService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/venue")
public class VenueController {

    private final VenueService venueService;

    public VenueController(VenueService venueService) {
        this.venueService = venueService;
    }

    @GetMapping("/list")
    public Result list() {
        List<Venue> list = venueService.listAll();
        return Result.ok(list);
    }

    /** 分页列表 */
    @GetMapping("/page")
    public Result page(@RequestParam(defaultValue = "1") int page,
                       @RequestParam(defaultValue = "10") int pageSize,
                       @RequestParam(required = false) String name) {
        PageResult<Venue> result = venueService.listPage(page, pageSize, name);
        return Result.ok(result);
    }

    @GetMapping("/available")
    public Result available() {
        List<Venue> list = venueService.listAvailable();
        return Result.ok(list);
    }

    @GetMapping("/{id}")
    public Result getById(@PathVariable Integer id) {
        Venue venue = venueService.getById(id);
        return venue != null ? Result.ok(venue) : Result.error("场地不存在");
    }

    @PostMapping("/add")
    public Result add(@RequestBody Venue venue) {
        boolean ok = venueService.save(venue);
        return ok ? Result.ok("添加成功") : Result.error("添加失败");
    }

    @PutMapping("/update")
    public Result update(@RequestBody Venue venue) {
        boolean ok = venueService.update(venue);
        return ok ? Result.ok("修改成功") : Result.error("修改失败");
    }

    @DeleteMapping("/{id}")
    public Result delete(@PathVariable Integer id) {
        boolean ok = venueService.delete(id);
        return ok ? Result.ok("删除成功") : Result.error("删除失败");
    }
}
