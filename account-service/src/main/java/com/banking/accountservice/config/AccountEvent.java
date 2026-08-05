package com.banking.accountservice.config;

import com.banking.accountservice.entity.Account;
import lombok.Builder;

@Builder
public record AccountEvent (Account account,EventType eventType){
}
