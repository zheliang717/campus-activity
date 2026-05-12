package com.campus.activity.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("venue")
public class Venue {

    @TableId(type = IdType.AUTO)
    private Integer venueId;

    private String name;
    private String location;
    private Integer capacity;
    private String openTime;
    private Integer status;          // 1-可用 0-不可用

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;
}
