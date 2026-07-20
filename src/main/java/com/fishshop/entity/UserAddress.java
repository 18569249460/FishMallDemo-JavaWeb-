package com.fishshop.entity;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * 用户常用收货地址实体。
 */
@Data
public class UserAddress {
    private Integer id;
    private Integer userId;
    private String receiverName;
    private String receiverPhone;
    private String addressDetail;
    private Integer isDefault;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
