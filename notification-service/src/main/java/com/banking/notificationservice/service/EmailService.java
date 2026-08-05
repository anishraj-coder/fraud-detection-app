package com.banking.notificationservice.service;

import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import java.math.BigDecimal;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

@Service
@RequiredArgsConstructor
@Slf4j
public class EmailService {

    private final JavaMailSender mailSender;

    public Mono<Void> sendOtpEmail(String toEmail, String otp, BigDecimal amount, String referenceNumber) {
        if (toEmail == null || toEmail.isBlank()) {
            log.error(">> Cannot send OTP email for reference {}: Recipient email is null or blank", referenceNumber);
            return Mono.empty();
        }

        return Mono.fromRunnable(() -> {
                    try {
                        MimeMessage mimeMessage = mailSender.createMimeMessage();
                        MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, true, "UTF-8");

                        helper.setFrom("no-reply@banking.com");
                        helper.setTo(toEmail.trim());
                        helper.setSubject("Security Verification Code - Transaction #" + referenceNumber);

                        String encodedRef = URLEncoder.encode(referenceNumber, StandardCharsets.UTF_8);
                        String encodedOtp = URLEncoder.encode(otp, StandardCharsets.UTF_8);
                        String verifyUrl = String.format("http://localhost:8089/api/v1/transactions/transaction/verify/%s?otp=%s", encodedRef, encodedOtp);

                        String htmlContent = buildHtmlTemplate(otp, amount, referenceNumber, verifyUrl);
                        helper.setText(htmlContent, true);

                        mailSender.send(mimeMessage);
                        log.info(">> Successfully sent rich HTML OTP email to {} for ref: {}", toEmail, referenceNumber);
                    } catch (Exception e) {
                        log.error(">> Failed to construct/send OTP email to {}: {}", toEmail, e.getMessage(), e);
                        throw new RuntimeException("Failed to send email", e);
                    }
                })
                .subscribeOn(Schedulers.boundedElastic())
                .doOnError(e -> log.error(">> Error in sendOtpEmail reactive pipeline: {}", e.getMessage()))
                .then();
    }

    private String buildHtmlTemplate(String otp, BigDecimal amount, String referenceNumber, String verifyUrl) {
        String fraudUrl = verifyUrl + "1";

        String template = """
            <!DOCTYPE html>
            <html lang="en">
            <head>
              <meta charset="UTF-8">
              <meta name="viewport" content="width=device-width, initial-scale=1.0">
              <title>Verify Your Transaction</title>
            </head>
            <body style="margin: 0; padding: 0; background-color: #f4f6f9; font-family: 'Helvetica Neue', Helvetica, Arial, sans-serif; color: #333333;">
              <table role="presentation" width="100%" cellspacing="0" cellpadding="0" border="0" style="background-color: #f4f6f9; padding: 40px 0;">
                <tr>
                  <td align="center">
                    <table role="presentation" width="100%" style="max-width: 520px; background-color: #ffffff; border-radius: 12px; overflow: hidden; box-shadow: 0 4px 12px rgba(0,0,0,0.08); border: 1px solid #e1e8ed;">
                      
                      <!-- Header -->
                      <tr>
                        <td style="background-color: #0f172a; padding: 28px 32px; text-align: center;">
                          <h1 style="color: #ffffff; font-size: 20px; margin: 0; font-weight: 600; letter-spacing: 0.5px;">SECURE BANKING</h1>
                        </td>
                      </tr>

                      <!-- Body -->
                      <tr>
                        <td style="padding: 32px;">
                          <h2 style="margin: 0 0 12px 0; color: #0f172a; font-size: 18px; font-weight: 600;">Transaction Verification</h2>
                          <p style="margin: 0 0 24px 0; color: #64748b; font-size: 14px; line-height: 1.5;">
                            A transaction attempt of <strong style="color: #0f172a;">$${AMOUNT}</strong> requires your authorization.
                          </p>

                          <!-- Transaction Info Box -->
                          <div style="background-color: #f8fafc; border: 1px dashed #cbd5e1; border-radius: 8px; padding: 16px; margin-bottom: 24px;">
                            <span style="display: block; font-size: 11px; text-transform: uppercase; color: #94a3b8; font-weight: 700; letter-spacing: 0.5px; margin-bottom: 4px;">Reference ID</span>
                            <span style="font-family: monospace; font-size: 14px; font-weight: 600; color: #334155;">${REF_NUM}</span>
                          </div>

                          <!-- OTP Display -->
                          <div style="text-align: center; margin-bottom: 28px;">
                            <span style="display: block; font-size: 12px; font-weight: 600; color: #64748b; margin-bottom: 8px; text-transform: uppercase; letter-spacing: 0.5px;">Your One-Time Password</span>
                            <div style="font-size: 32px; font-weight: 800; letter-spacing: 8px; color: #2563eb; background-color: #eff6ff; padding: 14px 20px; border-radius: 8px; display: inline-block;">
                              ${OTP}
                            </div>
                          </div>

                          <!-- Action Button -->
                          <div style="text-align: center; margin-bottom: 24px;">
                            <a href="${VERIFY_URL}" style="background-color: #2563eb; color: #ffffff; text-decoration: none; padding: 12px 28px; border-radius: 6px; font-weight: 600; font-size: 14px; display: inline-block; box-shadow: 0 2px 4px rgba(37, 99, 235, 0.2);">
                              Verify Transaction
                            </a>
                          </div>

                          <!-- Direct Link Fallback -->
                          <p style="margin: 0 0 8px 0; color: #94a3b8; font-size: 12px;">
                            If the button above does not work, click or copy this URL into your browser:
                          </p>
                          <p style="margin: 0 0 16px 0; word-break: break-all; font-size: 11px;">
                            <a href="${VERIFY_URL}" style="color: #2563eb; text-decoration: underline;">${VERIFY_URL}</a>
                          </p>
                          <p style="margin: 0 0 8px 0; color: #94a3b8; font-size: 12px;">
                            If it wasn't you, click or copy this URL into your browser:
                          </p>
                          <p style="margin: 0; word-break: break-all; font-size: 11px;">
                            <a href="${FRAUD_URL}" style="color: #ef4444; text-decoration: underline;">${FRAUD_URL}</a>
                          </p>
                        </td>
                      </tr>

                      <!-- Footer -->
                      <tr>
                        <td style="background-color: #f8fafc; padding: 20px 32px; text-align: center; border-top: 1px solid #f1f5f9;">
                          <p style="margin: 0; font-size: 12px; color: #94a3b8; line-height: 1.4;">
                            This code is valid for 5 minutes. Never share your OTP with anyone. <br>
                            If you did not initiate this request, please report it immediately.
                          </p>
                        </td>
                      </tr>

                    </table>
                  </td>
                </tr>
              </table>
            </body>
            </html>
            """;

        return template
                .replace("${AMOUNT}", String.valueOf(amount))
                .replace("${REF_NUM}", referenceNumber)
                .replace("${OTP}", otp)
                .replace("${VERIFY_URL}", verifyUrl)
                .replace("${FRAUD_URL}", fraudUrl);
    }
}