package com.fishshop.entity;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * 用户实体，对应 user 表。
 */
@Data
public class User {
    private Integer id;
    private String avatar;
    private String nickname;
    private String phone;
    private String password;
    private Integer role;
    private Integer status;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
