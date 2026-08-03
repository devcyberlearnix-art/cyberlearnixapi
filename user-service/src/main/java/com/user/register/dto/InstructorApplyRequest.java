package com.user.register.dto;

import lombok.Data;

@Data
public class InstructorApplyRequest {
    private String bankAccountNumber;
    private String bankIfsc;
    private String bankName;
    private String panNumber;
    private String additionalNotes;
}
