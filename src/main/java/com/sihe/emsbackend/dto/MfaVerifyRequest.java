package com.sihe.emsbackend.dto;

public class MfaVerifyRequest {
    private String challengeId;
    private String code;

    public String getChallengeId() {
        return challengeId;
    }

    public void setChallengeId(String challengeId) {
        this.challengeId = challengeId;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }
}
