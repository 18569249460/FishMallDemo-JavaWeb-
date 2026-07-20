package com.fishshop.util;

import com.fishshop.dto.UserDTO;
import com.fishshop.entity.User;

/**
 * 用户 DTO 转换工具。
 *
 * <p>统一把数据库用户实体转换成前端可见信息，避免把 password 等敏感字段返回给前端。</p>
 */
public final class UserDtoConverter {
    private UserDtoConverter() {
    }

    /**
     * 转换用户信息，并额外生成脱敏手机号。
     */
    public static UserDTO toUserDTO(User user) {
        if (user == null) {
            return null;
        }
        UserDTO dto = new UserDTO();
        dto.setId(user.getId());
        dto.setAvatar(user.getAvatar());
        dto.setNickname(user.getNickname());
        dto.setPhone(user.getPhone());
        dto.setMaskedPhone(PhoneMaskUtil.mask(user.getPhone()));
        dto.setRole(user.getRole());
        dto.setStatus(user.getStatus());
        dto.setCreatedAt(user.getCreatedAt());
        return dto;
    }
}
