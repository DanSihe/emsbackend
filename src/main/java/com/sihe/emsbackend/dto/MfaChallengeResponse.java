package com.sihe.emsbackend.dto;

import java.time.LocalDateTime;

public class MfaChallengeResponse {
    private String challengeId;
    private LocalDateTime expiresAt;
    private String deliveryMode;
    private String maskedEmail;
    private String message;
    private String demoCode;

    public MfaChallengeResponse() {
    }

    public MfaChallengeResponse(
            String challengeId,
            LocalDateTime expiresAt,
            String deliveryMode,
            String maskedEmail,
            String message,
            String demoCode
    ) {
        this.challengeId = challengeId;
        this.expiresAt = expiresAt;
        this.deliveryMode = deliveryMode;
        this.maskedEmail = maskedEmail;
        this.message = message;
        this.demoCode = demoCode;
    }

    public String getChallengeId() {
        return challengeId;
    }

    public void setChallengeId(String challengeId) {
        this.challengeId = challengeId;
    }

    public LocalDateTime getExpiresAt() {
        return expiresAt;
    }

    public void setExpiresAt(LocalDateTime expiresAt) {
        this.expiresAt = expiresAt;
    }

    public String getDeliveryMode() {
        return deliveryMode;
    }

    public void setDeliveryMode(String deliveryMode) {
        this.deliveryMode = deliveryMode;
    }

    public String getMaskedEmail() {
        return maskedEmail;
    }

    public void setMaskedEmail(String maskedEmail) {
        this.maskedEmail = maskedEmail;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getDemoCode() {
        return demoCode;
    }

    public void setDemoCode(String demoCode) {
        this.demoCode = demoCode;
    }
}
