package com.banking.notificationservice.config;


import com.banking.notificationservice.dto.Account;
import com.banking.notificationservice.dto.AccountEvent;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;


@Slf4j
@Component
public class AccountEventHandler {

    private void handleAccountCreation(Account account){
        log.info(">>Handler function: Account successfully created: {}",account.getAccountNumber());
    }

    private void handleBlockAccount(Account account){
        log.info(">>Handler function: Account successfully Blocked: {}",account.getAccountNumber());
    }
    private void handleUnblockAccount(Account account){
        log.info(">>Handler function: Account successfully Unblocked: {}",account.getAccountNumber());
    }


    public void handleAccountEvent(AccountEvent event){
        switch (event.eventType()){
            case BLOCKED -> handleBlockAccount(event.account());
            case CREATED -> handleAccountCreation(event.account());
            case UNBLOCKED -> handleUnblockAccount(event.account());
        }
    }
}
