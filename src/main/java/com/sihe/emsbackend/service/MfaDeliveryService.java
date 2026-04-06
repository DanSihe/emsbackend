package com.sihe.emsbackend.service;

import jakarta.mail.internet.MimeMessage;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

@Service
public class MfaDeliveryService {

    private final JavaMailSender mailSender;

    @Value("${app.mail.enabled:false}")
    private boolean mailEnabled;

    @Value("${app.mail.from:no-reply@ems-demo.local}")
    private String fromEmail;

    public MfaDeliveryService(JavaMailSender mailSender) {
        this.mailSender = mailSender;
    }

    public String sendLoginCode(String email, String code, String principalType) {
        if (!mailEnabled) {
            System.out.printf("[MFA-%s] Demo login code for %s is %s%n", principalType, email, code);
            return "CONSOLE";
        }

        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, false, "UTF-8");
            helper.setFrom(fromEmail);
            helper.setTo(email);
            helper.setSubject("Your EMS verification code");
            helper.setText(buildHtmlMessage(code, principalType), true);
            mailSender.send(message);
            return "EMAIL";
        } catch (Exception ex) {
            System.out.printf("[MFA-%s] Email send failed for %s. Demo login code is %s. Reason: %s%n",
                    principalType, email, code, ex.getMessage());
            return "CONSOLE";
        }
    }

    private String buildHtmlMessage(String code, String principalType) {
        return "<div style='font-family:Segoe UI,Tahoma,sans-serif;padding:24px;color:#102a43;'>"
                + "<h2 style='margin-bottom:12px;'>EMS Sign-In Verification</h2>"
                + "<p>Your " + principalType.toLowerCase() + " login code is:</p>"
                + "<div style='display:inline-block;padding:12px 18px;background:#eff6ff;"
                + "border:1px solid #bfdbfe;border-radius:12px;font-size:28px;font-weight:700;"
                + "letter-spacing:6px;color:#1d4ed8;'>" + code + "</div>"
                + "<p style='margin-top:16px;'>This code expires in 10 minutes.</p>"
                + "</div>";
    }
}
