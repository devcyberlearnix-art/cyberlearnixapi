package com.user.register.dto;

import com.fasterxml.jackson.annotation.JsonAlias;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class InstructorApplyRequest {
    @JsonAlias({"subject", "contentType"})
    private String subject;

    private String specialization;
    private String experience;
    private String bio;
    private String phone;

    @JsonAlias({"linkedinUrl", "linkedIn"})
    private String linkedinUrl;

    @JsonAlias({"websiteUrl", "website"})
    private String websiteUrl;

    private String bankAccountNumber;
    private String bankIfsc;
    private String bankName;
    private String panNumber;
    private String additionalNotes;
}
