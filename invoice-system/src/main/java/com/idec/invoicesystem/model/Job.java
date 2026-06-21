package com.idec.invoicesystem.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.Transient;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "jobs")
public class Job {

    @Id
    private String id;

    // ── Section A: Job Info ──────────────────────────────────────
    private String jobNo;

    @DateTimeFormat(pattern = "yyyy-MM-dd")
    private LocalDate date;

    private String companyId;
    private String companyName;
    private String shipper;
    private String vesselName;

    // ── Section B: Vehicle / Shipment ────────────────────────────
    private String description;          // vehicle model / goods
    private String chassisContainerNo;
    private String blNumber;
    private String invoiceNo;
    private String remarks;
    private String customsRegister;
    private String entrySubmit;
    private String driveType;           // HYBRID / PETROL / DIESEL / ELECTRIC
    private String duty;

    @DateTimeFormat(pattern = "yyyy-MM-dd")
    private LocalDate deliveryDate;

    // ── Section C: Financials (LKR) ──────────────────────────────
    private Double doCharges              = 0.0;
    private Double customDutyAmount       = 0.0;
    private Double hipgCharges            = 0.0;
    private Double cictCharges            = 0.0;
    private Double doExtension            = 0.0;
    private Double valuationExpences      = 0.0;
    private Double plantQuarantine        = 0.0;
    private Double rctCharges             = 0.0;
    private Double importControlDebit     = 0.0;
    private Double slpaBondingCharges     = 0.0;
    private Double amendmentCharges       = 0.0;
    private Double handlingExpenses       = 0.0;
    private Double documentationExpences  = 0.0;
    private Double examination            = 0.0;
    private Double weighBridgeCharges     = 0.0;
    private Double labour                 = 0.0;
    private Double additional             = 0.0;
    private Double vatRegistration        = 0.0;
    private Double tradeRegistration      = 0.0;
    private Double transport              = 0.0;
    private Double other                  = 0.0;
    private Double agencyFee              = 0.0;
    private Double entryPassing           = 0.0;
    private Double deliveryExpenses       = 0.0;
    private Double commission             = 0.0;
    private String customChargeName;
    private Double customChargeValue      = 0.0;
    private Double blAmount               = 0.0;
    private Double advance                = 0.0;
    private Double balance                = 0.0;

    // ── Status ───────────────────────────────────────────────────
    private String status = "PENDING"; // PENDING / IN_PROGRESS / COMPLETED
    private String invoiceStatus = "PENDING"; // PENDING / PAID

    // ── Audit ────────────────────────────────────────────────────
    private LocalDateTime createdAt = LocalDateTime.now();
    private LocalDateTime updatedAt = LocalDateTime.now();

    // ── Transient fields for reports ──────────────────────────────
    @Transient
    private String tempTinNo;

    @Transient
    private Double tempExpenses;

    @Transient
    private Double tempProfit;
}
