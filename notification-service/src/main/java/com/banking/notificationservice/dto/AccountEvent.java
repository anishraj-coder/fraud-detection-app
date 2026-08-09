package com.banking.notificationservice.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AccountEvent {

    @JsonProperty("account")
    private Account account;

    @JsonProperty("eventType")
    private EventType eventType;

    public Account account() { return account; }
    public EventType eventType() { return eventType; }
}
