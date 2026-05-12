package com.campus.activity.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("person")
public class Person {

    @TableId(type = IdType.AUTO)
    private Integer personId;

    private String username;
    private String password;
    private String name;
    private String role;              // admin-管理员  student-学生
    private String phone;
    private String email;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
}
