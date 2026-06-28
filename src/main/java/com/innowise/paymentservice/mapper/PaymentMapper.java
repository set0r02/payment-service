package com.innowise.paymentservice.mapper;

import com.innowise.paymentservice.dto.input.PaymentInputDto;
import com.innowise.paymentservice.dto.output.PaymentOutputDto;
import com.innowise.paymentservice.model.Payment;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface PaymentMapper {

    PaymentOutputDto toDto(Payment payment);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "status", ignore = true)
    @Mapping(target = "timestamp",ignore = true)
    @Mapping(target = "userId",ignore = true)
    Payment toEntity(PaymentInputDto paymentInputDto);

}
