package com.banking.notificationservice.dto;

import lombok.Builder;

@Builder
public record AccountEvent (Account account,EventType eventType){
}
