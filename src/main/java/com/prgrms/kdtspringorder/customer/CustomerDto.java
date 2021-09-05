package com.prgrms.kdtspringorder.customer;

import java.time.LocalDateTime;
import java.util.UUID;

public record CustomerDto(
        UUID customerId,
        String name,
        String email,
        LocalDateTime lastLoginAt,
        LocalDateTime createdAt
) {
    static CustomerDto of(Customer customer) {
        return new CustomerDto(customer.getCustomerId(),
                customer.getName(),
                customer.getEmail(),
                customer.getLastLoginAt(),
                customer.getCreatedAt());
    }

    static CustomerDto to(CustomerDto dto) { //생성. 여기서 만들 수도 있지만 service에서 원래 만드는거기 때문에 여기서 하는게 올바른 방법인지?? 선택해서 하기
        return new CustomerDto(dto.customerId(),
                dto.name(),
                dto.email(),
                dto.lastLoginAt(),
                dto.createdAt());
    }
}
