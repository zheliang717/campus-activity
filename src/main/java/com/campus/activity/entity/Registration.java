package com.campus.activity.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("registration")
public class Registration {

    @TableId(type = IdType.AUTO)
    private Integer regId;

    private Integer activityId;
    private Integer personId;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime regTime;

    private String status;            // 已报名/已取消

    // 连表查询字段（非数据库字段）
    @TableField(exist = false)
    private String activityName;

    @TableField(exist = false)
    private String personName;

    @TableField(exist = false)
    private String activityDate;

    @TableField(exist = false)
    private String activityType;
}
