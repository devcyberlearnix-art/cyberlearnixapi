package com.example.instructorservice.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class MonthlyEarningDTO {

    private String month;
    private Double revenue;
}