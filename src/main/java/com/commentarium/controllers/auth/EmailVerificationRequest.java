package com.commentarium.controllers.auth;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class EmailVerificationRequest {
    private String email;
    private String verificationCode;
}
