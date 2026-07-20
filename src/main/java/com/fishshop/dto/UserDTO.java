package com.fishshop.dto;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * 返回给前端的用户信息，刻意不包含 password 字段。
 */
@Data
public class UserDTO {
    private Integer id;
    private String avatar;
    private String nickname;
    private String phone;
    private String maskedPhone;
    private Integer role;
    private Integer status;
    private LocalDateTime createdAt;
}
