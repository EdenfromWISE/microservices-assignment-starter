package com.carrental.rental.dto;

import lombok.Data;

@Data
public class InspectionDTO {
    private boolean hasDamage;
    private String inspectionNotes;
    private String damageReportId;
}
