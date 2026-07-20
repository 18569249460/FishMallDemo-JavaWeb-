package com.fishshop.dto;

import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;

/**
 * 当前用户修改个人资料的请求参数。
 */
@Data
public class UserUpdateRequest {
    @NotBlank(message = "请输入昵称")
    @Size(max = 50, message = "昵称长度不能超过 50 个字符")
    private String nickname;

    @Size(max = 255, message = "头像地址不能超过 255 个字符")
    private String avatar;
}
