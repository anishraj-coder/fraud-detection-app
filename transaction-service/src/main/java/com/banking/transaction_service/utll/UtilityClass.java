package com.banking.transaction_service.utll;

import org.springframework.stereotype.Component;

import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Component
public class UtilityClass {
    private static final String ALPHA_NUMERIC = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
    private static final SecureRandom random = new SecureRandom();
    private static final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMddHHmmss");

    public String generateReferenceNumber(){
        String date= LocalDateTime.now().format(formatter);
        StringBuilder ref=new StringBuilder("TXN");
        ref.append(date).append("-");
        StringBuilder suffix=new StringBuilder(6);
        for (int i = 0; i < 6; i++) {
            suffix.append(ALPHA_NUMERIC.charAt(random.nextInt(ALPHA_NUMERIC.length())));
        }
        return  ref.append("-").append(suffix).toString();
    }

    public String generateOtp(){
        return String.format("%06d",(int)(Math.random()*900000+100000));
    }
}
