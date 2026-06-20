package com.idec.invoicesystem.dto;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.HashSet;
import java.util.Set;

/**
 * Carries the admin's charge-entry form submission before PDF generation.
 * Contains every charge amount that can appear on an invoice PLUS the set
 * of charge keys the admin selected to actually print on the PDF.
 */
@Data
@NoArgsConstructor
public class InvoiceGenerateRequest {

    // ── Charge Amounts ──────────────────────────────────────────────────────
    private Double doCharges             = 0.0;
    private Double customDutyAmount      = 0.0;
    private Double hipgCharges           = 0.0;
    private Double cictCharges           = 0.0;
    private Double doExtension           = 0.0;
    private Double valuationExpences     = 0.0;
    private Double plantQuarantine       = 0.0;
    private Double rctCharges            = 0.0;
    private Double importControlDebit    = 0.0;
    private Double slpaBondingCharges    = 0.0;
    private Double amendmentCharges      = 0.0;
    private Double handlingExpenses      = 0.0;
    private Double documentationExpences = 0.0;
    private Double examination           = 0.0;
    private Double weighBridgeCharges    = 0.0;
    private Double labour                = 0.0;
    private Double additional            = 0.0;
    private Double vatRegistration       = 0.0;
    private Double tradeRegistration     = 0.0;
    private Double transport             = 0.0;
    private Double other                 = 0.0;
    private Double agencyFee             = 0.0;
    private String customChargeName;
    private Double customChargeValue     = 0.0;

    // ── Totals ──────────────────────────────────────────────────────────────
    private Double blAmount  = 0.0;
    private Double advance   = 0.0;
    private Double balance   = 0.0;

    // ── Selection ───────────────────────────────────────────────────────────
    // Set of field-key names the admin checked to include as rows in the PDF
    private Set<String> selectedCharges = new HashSet<>();
}
