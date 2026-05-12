package com.campus.activity.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@TableName("activity")
public class Activity {

    @TableId(type = IdType.AUTO)
    private Integer activityId;

    private String name;
    private String type;              // 学术讲座/文体活动/志愿服务/其他
    private String organizer;         // 发起单位
    private LocalDate activityDate;   // 活动日期
    private String startTime;         // HH:mm
    private String endTime;           // HH:mm
    private Integer venueId;
    private String description;
    private Integer maxParticipants;
    private Integer currentParticipants;
    private String status;            // 待审核/已通过/已拒绝

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;

    // 连表查询字段（非数据库字段）
    @TableField(exist = false)
    private String venueName;

    @TableField(exist = false)
    private String venueLocation;
}
