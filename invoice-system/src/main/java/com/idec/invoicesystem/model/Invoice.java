package com.idec.invoicesystem.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Document(collection = "invoices")
public class Invoice {

    @Id
    private String id;
    private String customerName;
    private double amount;

    // Getters and Setters
}